package samsung_2nd_module_java;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Multisets;

public class FeatureExtractor {
	private static final String DATA_DIR = "data";
	private static final String DATA_POS = "sample_pos_tagging.txt";
	private static final String DATA_TARGET = "sample_senti_target.txt";
	private static final String DATA_SENTI = "sample_sentiment.txt";

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

	// 결과물
	private Multiset<String> tokens = HashMultiset.create();
	private Multiset<String> allNP = HashMultiset.create();
	private Map<String, Multiset<String>> np_context = new HashMap<String, Multiset<String>>();

	public Map<String, Multiset<String>> getCountContexts() {
		return np_context;
	}

	public ImmutableSet<Entry<String>> getTokens() {
		return Multisets.copyHighestCountFirst(tokens).entrySet();
	}

	public ImmutableSet<Entry<String>> getNPs() {
		return Multisets.copyHighestCountFirst(allNP).entrySet();
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
				// TODO: 소문자화, Stemming
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
							np = np.replaceAll("\\bthe\\b", "").trim();

							// 저장
							nps.add(np);
						}
						allNP.addAll(nps);
						tokens.addAll(nps);

						// JJ 추출
						Multiset<String> jjs = HashMultiset.create();
						Matcher jjMatcher = JJ_PATTERN.matcher(sent);
						while (jjMatcher.find()) {
							String jj = jjMatcher.group().trim()
									.replaceAll(JJ_TAG_PATTERN, "");

							// 언어적 처리
							jj = jj.toLowerCase();
							jj = SpellCheckerManager.getSuggestion(jj);
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
							if (np_context.containsKey(np)) {
								Multiset<String> ctx = np_context.get(np);
								Iterator<String> iter = jjs.iterator();
								while (iter.hasNext()) {
									ctx.add(iter.next());
								}
							} else {
								np_context.put(np, jjs);
							}
						}
					}
				}

				// 처리한 라인 개수 출력
				System.out.println(++i);
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
