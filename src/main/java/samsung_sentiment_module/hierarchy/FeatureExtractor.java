package samsung_sentiment_module.hierarchy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.NormalizedDotProductMetric;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Multisets;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

public class FeatureExtractor {
	private static final String DATA_DIR = "resources/data";
	private static final String DIC_DIR = "resources/dictionary";
	private static final String DATA_POS = "pos_tagging.txt";

	// 정규표현식
	private static final String SENT_PATTERN_STRING = "\\b(\\w+/NN\\s?)+\\w+/VB[PZ]? (\\w+/RB\\s)?\\w+/JJ(\\s*,/, (\\w+/RB\\s)?\\w+/JJ(\\s,/,)?)*(\\s\\w+/CC (\\w+/RB\\s)?\\w+/JJ)?\\s";
	private static final Pattern SENT_PATTERN = Pattern
			.compile(SENT_PATTERN_STRING);

	private static final String NP_PATTERN_STRING = "(\\w+/NN\\s)+";
	private static final Pattern NP_PATTERN = Pattern
			.compile(NP_PATTERN_STRING);

	private static final String JJ_PATTERN_STRING = "(\\w+/JJ)+";
	private static final Pattern JJ_PATTERN = Pattern
			.compile(JJ_PATTERN_STRING);

	private static final String NP_TAG_PATTERN = "(?i)/NN[PS]{0,2}";
	private static final String JJ_TAG_PATTERN = "(?i)/JJ[RS]{0,1}";

	// stopwords
	private Set<String> stopwords = new HashSet<String>();

	// Feature 추출
	private Multiset<String> tokens = HashMultiset.create();
	private Multiset<String> allNPs = HashMultiset.create();

	// 벡터화의 (중간) 결과물들
	private Alphabet vocabulary = new Alphabet();
	private Map<String, Multiset<String>> countContexts = new HashMap<String, Multiset<String>>();
	private Map<String, HashMap<String, Double>> ppmiContexts = new HashMap<String, HashMap<String, Double>>();
	private Map<String, FeatureVector> vectors = new HashMap<String, FeatureVector>();

	public FeatureExtractor() throws IOException {
		// populate stopwords
		Path stopFile = Paths.get(DIC_DIR).resolve("stopwords.txt");
		try (BufferedReader reader = Files.newBufferedReader(stopFile,
				StandardCharsets.UTF_8)) {
			String line = "";

			while ((line = reader.readLine()) != null) {
				String stopWord = line.trim();
				stopwords.add(stopWord);
			}
		}
	}

	public Map<String, Multiset<String>> getCountContexts() {
		return countContexts;
	}

	public ImmutableSet<Entry<String>> getTokens() {
		return Multisets.copyHighestCountFirst(tokens).entrySet();
	}

	public ImmutableSet<Entry<String>> getNPs() {
		return Multisets.copyHighestCountFirst(allNPs).entrySet();
	}

	public void vectorize() {
		// 형용사(context) vocabulary 생성
		vocabulary = new Alphabet();
		for (java.util.Map.Entry<String, Multiset<String>> entry : countContexts
				.entrySet()) {
			Multiset<String> context = entry.getValue();
			vocabulary.lookupIndices(context.toArray(), true);
		}
		vocabulary.stopGrowth();

		// 각 feature의 PPMI 값 계산하여 벡터 형태로 저장
		double normalizingComp = tokens.size();
		for (java.util.Map.Entry<String, Multiset<String>> entry : countContexts
				.entrySet()) {
			// p_n: text에서 해당 명사가 출현할 확률
			String np = entry.getKey();
			double p_n = tokens.count(np) / normalizingComp;

			// context를 구성하는 형용사들의 ppmi 값을 계산하여 맵 형태로 저장
			HashMap<String, Double> ppmiContext = new HashMap<String, Double>();
			String cxt = "";
			double cxtCount = 0;
			for (Entry<String> contextAndCount : entry.getValue().entrySet()) {
				/*
				 * cxt: 형용사 cxtCount: 형용사가 np와 함께 나타난 빈도
				 */
				cxt = contextAndCount.getElement();
				cxtCount = contextAndCount.getCount();

				// p_a: text에서 해당 형용사가 출현할 확률
				double p_a = tokens.count(cxt) / normalizingComp;

				// p_na: text에서 해당 명사와 형용사가 동시에 출현할 확률
				double p_na = cxtCount / normalizingComp;

				// PMI: log( p_na / (p_n * p_a) )
				double pmi = Math.log(p_na / (p_n * p_a));

				// PPMI: 0 if PMI = 0 else PMI
				double ppmi = (pmi < 0) ? 0 : pmi;

				// 계산된 PPMI 값 저장
				ppmiContext.put(cxt, ppmi);
			}

			ppmiContexts.put(np, ppmiContext);

		}
		// System.out.println(countContexts);
		// System.out.println(ppmiContexts);
		// System.out.println("");

		// PPMI 벡터를 FeatureVector 형태로 변환
		for (java.util.Map.Entry<String, HashMap<String, Double>> entry : ppmiContexts
				.entrySet()) {
			Map<String, Double> context = entry.getValue();
			Object[] adjs = context.keySet().toArray();

			int[] indices = new int[adjs.length];
			double[] ppmis = new double[context.values().size()];
			for (int i = 0; i < adjs.length; i++) {
				Object adj = adjs[i];
				indices[i] = vocabulary.lookupIndex(adj, false);
				ppmis[i] = context.get(adj);
			}

			FeatureVector vector = new FeatureVector(vocabulary, indices, ppmis);
			vectors.put(entry.getKey(), vector);
		}

	}

	@SuppressWarnings("unchecked")
	public void deserializeOutputs() throws ClassNotFoundException, IOException {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		Path dir = Paths.get("output/serializables");

		if (!Files.exists(dir)) {
			throw new IllegalStateException("Serialized outputs aren't exist!");
		}

		// vocabulary
		fis = new FileInputStream(dir.resolve("vocabulary.dat").toFile());
		ois = new ObjectInputStream(fis);
		vocabulary = (Alphabet) ois.readObject();
		fis.close();

		// countContext
		fis = new FileInputStream(dir.resolve("countContexts.dat").toFile());
		ois = new ObjectInputStream(fis);
		countContexts = (HashMap<String, Multiset<String>>) ois.readObject();
		fis.close();

		// ppmiContexts
		fis = new FileInputStream(dir.resolve("ppmiContexts.dat").toFile());
		ois = new ObjectInputStream(fis);
		ppmiContexts = (HashMap<String, HashMap<String, Double>>) ois
				.readObject();
		fis.close();

		// vectors
		fis = new FileInputStream(dir.resolve("vectors.dat").toFile());
		ois = new ObjectInputStream(fis);
		vectors = (HashMap<String, FeatureVector>) ois.readObject();
		fis.close();
	}

	public void serializeOutputs() throws IOException {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		Path dir = Paths.get("output/serializables");
		if (!Files.isDirectory(dir)) {
			Files.createDirectories(dir);
		}

		// vocabulary 저장
		fos = new FileOutputStream(dir.resolve("vocabulary.dat").toFile());
		oos = new ObjectOutputStream(fos);
		oos.writeObject(vocabulary);
		fos.close();

		// countContexts 저장
		fos = new FileOutputStream(dir.resolve("countContexts.dat").toFile());
		oos = new ObjectOutputStream(fos);
		oos.writeObject(countContexts);
		fos.close();

		// ppmiContexts 저장
		fos = new FileOutputStream(dir.resolve("ppmiContexts.dat").toFile());
		oos = new ObjectOutputStream(fos);
		oos.writeObject(ppmiContexts);
		fos.close();

		// vectors 저장
		fos = new FileOutputStream(dir.resolve("vectors.dat").toFile());
		oos = new ObjectOutputStream(fos);
		oos.writeObject(vectors);
		fos.close();

		// close the stream
		oos.close();
	}

	public void debugClassification(String centroid) {
		if (!vectors.containsKey(centroid)) {
			throw new IllegalArgumentException("No such a target: " + centroid);
		}

		// 유사도 계산
		// TODO: 만약 두 target의 유사도가 같으면 덮어 씌워지는 문제가 있음
		NormalizedDotProductMetric metric = new NormalizedDotProductMetric();
		FeatureVector targetVector = vectors.get(centroid);
		Multimap<Double, String> sortedBySim = TreeMultimap.create(Ordering
				.natural().reverse(), Ordering.natural());
		for (String t : vectors.keySet()) {
			FeatureVector otherVector = vectors.get(t);
			double sim = 1 - metric.distance(targetVector, otherVector);
			sortedBySim.put(sim, t);
		}

		for (Double t : sortedBySim.keySet()) {
			System.out.printf("%s: %s\n", t, sortedBySim.get(t));
		}
	}

	public void debug() {
		System.out.println(countContexts);
		System.out.println(ppmiContexts);
		System.out.println(vectors);
	}

	public String sanitize(String token) {
		String sanitizedToken = token;

		if (token.contains(" ") || token.contains("\t")) { // 두 단어 이상으로 이루어진 구일
															// 경우
			StringBuilder sBuilder = new StringBuilder();
			String[] tokens = token.split("\\W+");

			for (String t : tokens) {
				if (!stopwords.contains(t)) {
					sBuilder.append(" ");
					sBuilder.append(sanitize(t));
				}
			}

			sanitizedToken = sBuilder.toString();

		} else {
			try {
				sanitizedToken = SpellCheckerManager.getSuggestion(token);
			} catch (NegativeArraySizeException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return sanitizedToken.trim().toLowerCase();
	}

	public void extract() throws IOException {
		Path dir = Paths.get(DATA_DIR);
		Path dataFile = dir.resolve(DATA_POS);
		try (BufferedReader reader = Files.newBufferedReader(dataFile,
				StandardCharsets.UTF_8)) {
			String line = "";
			int i = 0;
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				// 문장이 비어있지 않을 때에만 처리
				if (!line.isEmpty()) {
					Matcher sentMatcher = SENT_PATTERN.matcher(line);
					while (sentMatcher.find()) {
						String sent = sentMatcher.group().trim();

						// NP 추출
						Set<String> nps = new HashSet<String>();
						Matcher npMatcher = NP_PATTERN.matcher(sent);
						while (npMatcher.find()) {
							String np = npMatcher.group()
									.replaceAll(NP_TAG_PATTERN, "").trim();

							// 언어적 처리
							np = np.toLowerCase();
							np = np.replaceAll("\\bthe\\b", "").trim(); // 전치사
																		// the
																		// 제거
							np = sanitize(np);

							// 저장
							nps.add(np);
						}
						allNPs.addAll(nps);
						tokens.addAll(nps);

						// JJ 추출
						Multiset<String> jjs = HashMultiset.create();
						Matcher jjMatcher = JJ_PATTERN.matcher(sent);
						while (jjMatcher.find()) {
							String jj = jjMatcher.group().trim()
									.replaceAll(JJ_TAG_PATTERN, "");

							// 언어적 처리
							jj = jj.toLowerCase();
							jj = sanitize(jj);
							Stemmer stemmer = new Stemmer();
							stemmer.add(jj.toCharArray(), jj.length());
							stemmer.stem();
							jj = stemmer.toString();

							// Multiset에 추가
							jj = jj.toLowerCase();
							jjs.add(jj);
						}
						tokens.addAll(jjs);

						// NP context 구축
						for (String np : nps) {
							if (countContexts.containsKey(np)) {
								Multiset<String> ctx = countContexts.get(np);
								Iterator<String> iter = jjs.iterator();
								while (iter.hasNext()) {
									ctx.add(iter.next());
								}
							} else {
								countContexts.put(np, jjs);
							}
						}
					}
				}

				// 처리한 라인 개수 출력
				++i;
				if ((i % 1000) == 0) {
					System.out.println(i);
				}
			}
		}
	}

	// private void listDataDir() {
	// try {
	// Path dir = Paths.get(DATA_DIR);
	// DirectoryStream<Path> stream = Files.newDirectoryStream(dir,
	// "*.txt");
	// for (Path entry : stream) {
	// System.out.println(entry.getFileName());
	// }
	// } catch (IOException e) {
	// System.out.println(e.getMessage());
	// }
	// }
}
