/**
 * 의견 대상 분류기
 * @version : 1.0
 * @author  : Kyoungrok Jang (kyoungrok.jang@kaist.ac.kr)
 */

package samsung_sentiment_module.hierarchy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import samsung_sentiment_module.util.SpellCheckerManager;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class OpinionTargetClassifier {
	private String inputFilePath = null;
	private String outputDirPath = null;
	private String centroidFilePath = null;

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
	private Multiset<String> tokenSet = HashMultiset.create();
	private Multiset<String> npSet = HashMultiset.create();

	// 벡터화의 (중간) 결과물들
	private Alphabet adjVocabulary = new Alphabet();
	private Map<String, Multiset<String>> countContexts = new HashMap<String, Multiset<String>>();
	private Map<String, HashMap<String, Double>> ppmiContexts = new HashMap<String, HashMap<String, Double>>();
	private Map<String, FeatureVector> contextVectors = new HashMap<String, FeatureVector>();

	// 분류를 위한 centroid
	private Map<String, String> centroids = new HashMap<String, String>();

	/**
	 * 생성자
	 * 
	 * @param inputFilePath
	 *            POS 태깅된 문장들이 줄(line) 별로 나뉘어져 있는 데이터 파일의 경로 <br />
	 *            (반드시 한 줄당 한 문장이 나와야 합니다)
	 * @param outputDirPath
	 *            분류 결과를 출력할 디렉토리의 경로
	 * @param centroidFilePath
	 *            상위 의견 대상(클래스)과 각각의 상위 의견 대상을 나타내는 centroid가 될 의견 대상의 이름을 포함하는
	 *            파일의 경로
	 *            <p>
	 *            대상 centroid 파일은
	 *            <code><b>"상위 의견 대상(클래스)"="centroid 의견 대상"</b></code> 형태로 기재되어
	 *            있어야 함. 여러 줄에 걸쳐 기재 가능함.
	 *            </p>
	 *            <p>
	 *            예시: <code>Food=restaurant</code>
	 *            </p>
	 *            <p>
	 *            centroid로 사용하고자 한 의견 대상이 말뭉치에서 추출되지 않았을 경우, 에러를 출력함 말뭉치에서 어떤
	 *            의견 대상들이 추출되었는지는 {@link #getAllNPs()}를 사용하여 확인 가능
	 *            </p>
	 * @throws IOException
	 */
	public OpinionTargetClassifier(String inputFilePath, String outputDirPath,
			String centroidFilePath) throws IOException {
		// output & centroid path
		this.inputFilePath = inputFilePath;
		this.outputDirPath = outputDirPath;
		this.centroidFilePath = centroidFilePath;

		// populate stopwords
		populateStopWords();

		// populate centroids
		populateCentroids();
	}

	/**
	 * 말뭉치에서 등장하는 의견 대상들을 그것들을 수식하는 형용사들로 구성된 context로 추출
	 * <p>
	 * 추출된 의견 대상들의 context는 {@link #getCountContexts()}를 이용해서 가져올 수 있음
	 * </p>
	 * 
	 * @throws IOException
	 */
	public void extractContexts() throws IOException {
		Path dataFile = Paths.get(inputFilePath);

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
							np = sanitizeToken(np);

							// 저장
							nps.add(np);
						}
						npSet.addAll(nps);
						tokenSet.addAll(nps);

						// JJ 추출
						Multiset<String> jjs = HashMultiset.create();
						Matcher jjMatcher = JJ_PATTERN.matcher(sent);
						while (jjMatcher.find()) {
							String jj = jjMatcher.group().trim()
									.replaceAll(JJ_TAG_PATTERN, "");

							// 언어적 처리
							jj = jj.toLowerCase();
							jj = sanitizeToken(jj);
							Stemmer stemmer = new Stemmer();
							stemmer.add(jj.toCharArray(), jj.length());
							stemmer.stem();
							jj = stemmer.toString();

							// Multiset에 추가
							jj = jj.toLowerCase();
							jjs.add(jj);
						}
						tokenSet.addAll(jjs);

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

	/**
	 * {@link #extractContexts()}를 이용해서 추출한 의견 대상의 context들을 빠른 벡터 연산을 위해 벡터로
	 * 변환함 Mallet의 {@link cc.mallet.types.FeatureVector} 클래스를 활용
	 * <p>
	 * 생성된 벡터는 {@link #getVectors()}를 이용해서 가져올 수 있음
	 * </p>
	 * 
	 * @see #getVectors()
	 * @see cc.mallet.types.FeatureVector
	 */
	public void vectorizeContexts() {
		// 형용사(context) vocabulary 생성
		adjVocabulary = new Alphabet();
		for (java.util.Map.Entry<String, Multiset<String>> entry : countContexts
				.entrySet()) {
			Multiset<String> context = entry.getValue();
			adjVocabulary.lookupIndices(context.toArray(), true);
		}
		adjVocabulary.stopGrowth();

		// 각 feature의 PPMI 값 계산하여 벡터 형태로 저장
		double normalizingComp = tokenSet.size();
		for (java.util.Map.Entry<String, Multiset<String>> entry : countContexts
				.entrySet()) {
			// p_n: text에서 해당 명사가 출현할 확률
			String np = entry.getKey();
			double p_n = tokenSet.count(np) / normalizingComp;

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
				double p_a = tokenSet.count(cxt) / normalizingComp;

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

		// PPMI 벡터를 FeatureVector 형태로 변환
		for (java.util.Map.Entry<String, HashMap<String, Double>> entry : ppmiContexts
				.entrySet()) {
			Map<String, Double> context = entry.getValue();
			Object[] adjs = context.keySet().toArray();

			int[] indices = new int[adjs.length];
			double[] ppmis = new double[context.values().size()];
			for (int i = 0; i < adjs.length; i++) {
				Object adj = adjs[i];
				indices[i] = adjVocabulary.lookupIndex(adj, false);
				ppmis[i] = context.get(adj);
			}

			FeatureVector vector = new FeatureVector(adjVocabulary, indices,
					ppmis);
			contextVectors.put(entry.getKey(), vector);
		}

	}

	/**
	 * 말뭉치에서 등장하는 의견 대상들을 사용자가 제공한 각각의 상위 의견 대상들과 유사한 순으로 정렬하여 출력
	 * 
	 * @throws IOException
	 */
	public void classifyAll() throws IOException {
		for (String className : centroids.keySet()) {
			String centroid = centroids.get(className);

			classifySingle(className, centroid);
		}
	}

	/**
	 * 형용사로 구성된 의견 대상 벡터를 반환. 벡터의 component는 형용사이고, 값은 형용사가 의견 대상을 수식한 횟수
	 * 
	 * @return 형용사로 구성된 의견 대상 벡터(count)
	 */
	public Map<String, Multiset<String>> getCountContexts() {
		return countContexts;
	}

	/**
	 * 형용사로 구성된 의견 대상 벡터를 반환. 벡터의 component는 형용사이고, 값은 형용사와 의견 대상 간의 Positive
	 * PMI(PPMI) 값. PPMI는 원본 PMI값이 음수일 때에는 0의 값을 갖고, 0보다 크거나 같을 경우 원본 PMI와 동일한
	 * 값을 갖는다
	 * 
	 * @return 형용사로 구성된 의견 대상 벡터(PPMI)
	 */
	public Map<String, HashMap<String, Double>> getNormalizedContexts() {
		return ppmiContexts;
	}

	/**
	 * 빠른 벡터 연산을 위해 변환된 의견 대상 벡터들을 반환
	 * 
	 * @return 의견 대상들의 벡터
	 * @see cc.mallet.types.FeatureVector
	 */
	public Map<String, FeatureVector> getVectors() {
		return contextVectors;
	}

	/**
	 * 말뭉치에서 등장한 명사구(NP) - 의견 대상 후보 - 들을 출현 빈도 순으로 정렬하여 반환
	 * 
	 * @return 말뭉치에서 등장한 명사구(NP)들을 출현 빈도 순으로 정렬한 목록
	 */
	public ImmutableSet<Entry<String>> getAllNPs() {
		return Multisets.copyHighestCountFirst(npSet).entrySet();
	}

	/**
	 * 분류를 위해 생성된 의견 대상 벡터 등의 중간 결과물들을 cache 디렉토리에 저장
	 * 
	 * @throws IOException
	 */
	public void storeVectorsAsCache() throws IOException {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		Path dir = Paths.get("output/hierarchy_cache");
		if (!Files.isDirectory(dir)) {
			Files.createDirectories(dir);
		}

		// vocabulary 저장
		fos = new FileOutputStream(dir.resolve("vocabulary.dat").toFile());
		oos = new ObjectOutputStream(fos);
		oos.writeObject(adjVocabulary);
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
		oos.writeObject(contextVectors);
		fos.close();

		// close the stream
		oos.close();

		System.out.println("Processed vectors are cached at: "
				+ dir.toAbsolutePath());
		System.out.println("");
	}

	/**
	 * 분류를 위해 생성된 의견 대상 벡터 등의 중간 결과물들을 cache 디렉토리에서 로드
	 * 
	 * @param cacheDir
	 *            중간 결과물들이 저장되어 있는 cache 디렉토리
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void loadVectorsFromCache(String cacheDir)
			throws ClassNotFoundException, IOException {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		Path dir = Paths.get(cacheDir);

		if (!Files.exists(dir)) {
			throw new IllegalStateException(
					"Provided cache directory is invalid!");
		}

		// vocabulary
		fis = new FileInputStream(dir.resolve("vocabulary.dat").toFile());
		ois = new ObjectInputStream(fis);
		adjVocabulary = (Alphabet) ois.readObject();
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
		contextVectors = (HashMap<String, FeatureVector>) ois.readObject();
		fis.close();
	}

	protected ImmutableSet<Entry<String>> getAllTokens() {
		return Multisets.copyHighestCountFirst(tokenSet).entrySet();
	}

	protected Map<String, String> getCentroids() {
		return centroids;
	}

	protected void classifySingle(String className, String centroid)
			throws IOException {
		if (!contextVectors.containsKey(centroid)) {
			throw new IllegalArgumentException(
					"Centroid is invalid (no such a target: " + centroid + ")");
		}

		// 유사도 계산
		NormalizedDotProductMetric metric = new NormalizedDotProductMetric();
		FeatureVector targetVector = contextVectors.get(centroid);
		Multimap<Double, String> sortedBySim = TreeMultimap.create(Ordering
				.natural().reverse(), Ordering.natural());
		for (String t : contextVectors.keySet()) {
			FeatureVector otherVector = contextVectors.get(t);
			Double sim = 1 - metric.distance(targetVector, otherVector);
			if (!t.isEmpty() && !sim.isNaN()) {
				sortedBySim.put(sim, t);
			}
		}

		// 출력
		Path dir = Paths.get(outputDirPath).resolve("hierarchy");
		if (!Files.exists(dir)) {
			Files.createDirectories(dir);
		}

		Path outputFile = dir.resolve(className + ".txt");
		JSONObject json = new JSONObject();

		try (BufferedWriter writer = Files.newBufferedWriter(outputFile,
				StandardCharsets.UTF_8, StandardOpenOption.WRITE,
				StandardOpenOption.CREATE)) {
			for (Double t : sortedBySim.keySet()) {
				JSONArray array = new JSONArray();
				array.addAll(sortedBySim.get(t));

				json.put(t, array);
			}

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String outputString = gson.toJson(json);

			writer.write(outputString);

			System.out.println(outputFile.toString()
					+ " is generated as output");
		}

	}

	protected String sanitizeToken(String token) {
		String sanitizedToken = token;

		if (token.contains(" ") || token.contains("\t")) { // 두 단어 이상으로 이루어진 구일
															// 경우
			StringBuilder sBuilder = new StringBuilder();
			String[] tokens = token.split("\\W+");

			for (String t : tokens) {
				if (!stopwords.contains(t)) {
					sBuilder.append(" ");
					sBuilder.append(sanitizeToken(t));
				}
			}

			sanitizedToken = sBuilder.toString();

		} else {
			try {
				sanitizedToken = SpellCheckerManager.getSuggestion(token);
			} catch (NegativeArraySizeException e) {
				// e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return sanitizedToken.trim().toLowerCase();
	}

	protected void populateStopWords() throws IOException {
		InputStream is = OpinionTargetClassifier.class
				.getResourceAsStream("/stopwords.txt");

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				is, "UTF-8"))) {
			String line = "";

			while ((line = reader.readLine()) != null) {
				String stopWord = line.trim();
				stopwords.add(stopWord);
			}
		}

	}

	protected void populateCentroids() throws IOException {
		Path centroidFile = Paths.get(centroidFilePath);
		try (BufferedReader reader = Files.newBufferedReader(centroidFile,
				StandardCharsets.UTF_8)) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				try {
					String[] centroidTargetPair = line.trim().split("=");
					String centroid = centroidTargetPair[0].trim();
					String target = centroidTargetPair[1].toLowerCase().trim();

					centroids.put(centroid, target);
				} catch (IndexOutOfBoundsException e) {
					System.out.println("Failed to parse the line: " + line);
				} catch (NullPointerException e) {
					System.out.println("Failed to parse the line: " + line);
				}
			}
		}
	}
}
