package samsung_sentiment_module.sentiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.argparse4j.inf.Namespace;

import org.ejml.simple.SimpleMatrix;

import samsung_sentiment_module.abs.ModuleRunner;
import samsung_sentiment_module.hierarchy.OpinionTargetClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentUtils;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Generics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


/**
 * 
 * 문장 단위와 단어 단위의 감성 분석 결과를 수행하기 위해 정의된 클래스.
 * 
 * 
 * @author SeungYong
 *
 */

public class SentimentPipeline implements ModuleRunner {
	private static final NumberFormat NF = new DecimalFormat("0.0000");

	private static List<String> targetList = new ArrayList<String>();
	private final static String posPath = "./temp/" + "tagged.pos";

	static LexicalizedParser parser;
	static StanfordCoreNLP pipeline;
	static Tagger_IRNLP irnlp;
	
	public void run(String[] args, Namespace parsedArgs) {

		String inputDirPath = parsedArgs.getString("inputDirPath");
		String outputDirPath = parsedArgs.getString("outputDirPath");
		String fineGrained = parsedArgs.getString("fineGrained");

		boolean threeClass = true;
		if (fineGrained == null)
			threeClass = true;
		else if (fineGrained.contains("Y"))
			threeClass = false;

		try {
			List<FileObject> t = senti(inputDirPath, outputDirPath, threeClass);
			
			for(int i =0 ; i < t.size() ; i++){
				List<SentObject> a =  t.get(i).getSentObject();
				for(int j = 0 ; j < a.size() ; j++){
					System.out.println(a.get(j).getSentence());
					System.out.println((a.get(j).getSentPolarity()));
					List<WordObject> w = a.get(j).getWordObject();
					for(int k = 0 ; k < w.size() ; k++){
						System.out.println(w.get(k).opinionTarget);
						System.out.println(w.get(k).opinionWord);
						System.out.println(w.get(k).polarity);
					}
					System.out.println("--w--");
				}
				System.out.println("------");
			}
			// targetListWrite("temp");
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * 기본 생성자
	 * 감성 분석을 위한 파서 모델을 로딩
	 */
	public SentimentPipeline() {

		boolean stdin = false;

		List<Output> outputFormats = Arrays
				.asList(new Output[] { Output.PROBABILITIES });
		Input inputFormat = Input.TEXT;

		Properties props = new Properties();

		if (stdin) {
			props.setProperty("ssplit.eolonly", "true");
		}
		if (inputFormat == Input.TREES) {
			props.setProperty("annotators", "sentiment");
			props.setProperty("enforceRequirements", "false");
		} else {
			props.setProperty("annotators",
					"tokenize, ssplit, parse, sentiment");
		}
		this.parser = LexicalizedParser.getParserFromFile(
				DefaultPaths.DEFAULT_PARSER_MODEL, new Options());
		this.pipeline = new StanfordCoreNLP(props);
		this.irnlp = new Tagger_IRNLP();

	}
	
	static class adj_info {
		 private String adj;
		 private int index;
		  
		 adj_info(String adj, int index){
			this.adj = adj;
			this.index = index;		 
		 }
		 
		 private String get_adj(){
			 return adj;
		 }
		 private int get_index(){
			 return index;
		 }
		
	}

	static class PolarityProb {
		float polar[] = new float[5];

		PolarityProb() {

		}
	}

	static enum Output {
		PENNTREES, VECTORS, ROOT, PROBABILITIES
	}

	static enum Input {
		TEXT, TREES
	}


	static void setSentimentLabels(Tree tree) {
		if (tree.isLeaf()) {
			return;
		}

		for (Tree child : tree.children()) {
			setSentimentLabels(child);
		}

		Label label = tree.label();
		if (!(label instanceof CoreLabel)) {
			throw new IllegalArgumentException(
					"Required a tree with CoreLabels");
		}
		CoreLabel cl = (CoreLabel) label;
		cl.setValue(Integer.toString(RNNCoreAnnotations.getPredictedClass(tree)));
	}


	static int setIndexLabels(Tree tree, int index) {
		if (tree.isLeaf()) {
			return index;
		}

		tree.label().setValue(Integer.toString(index));
		index++;
		for (Tree child : tree.children()) {
			index = setIndexLabels(child, index);
		}
		return index;
	}


	static int outputTreeVectors(PrintStream out, Tree tree, int index) {
		if (tree.isLeaf()) {
			return index;
		}

		index++;
		for (Tree child : tree.children()) {
			index = outputTreeVectors(out, child, index);
		}
		return index;
	}


	static int outputTreeScores(PrintStream out, Tree tree, int index) {
		if (tree.isLeaf()) {
			return index;
		}

		out.print("  " + index + ":");
		SimpleMatrix vector = RNNCoreAnnotations.getPredictions(tree);

		for (int i = 0; i < vector.getNumElements(); ++i) {
			out.print("  " + NF.format(vector.get(i)));

		}
		out.println();
		index++;
		for (Tree child : tree.children()) {
			index = outputTreeScores(out, child, index);
		}
		return index;
	}


	static int revOutputTreeScores(PrintStream out, Tree tree, int index,
			PolarityProb c, int num) {
		if (tree.isLeaf()) {
			return index;
		}

		SimpleMatrix vector = RNNCoreAnnotations.getPredictions(tree);

		if (index == num) {

			for (int i = 0; i < vector.getNumElements(); ++i) {
				c.polar[i] = Float.parseFloat(NF.format(vector.get(i)));
			}
		}
		// out.println();
		index++;
		for (Tree child : tree.children()) {
			index = revOutputTreeScores(out, child, index, c, num);
		}
		return index;
	}

	/* n */
	static PolarityProb outputProbability(PrintStream out, CoreMap sentence,
			List<Output> outputFormats, int num) {
		Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
		PolarityProb c = new PolarityProb();

		revOutputTreeScores(out, tree, 0, c, num);
		return c;
	}

	static void outputTree(PrintStream out, CoreMap sentence,
			List<Output> outputFormats) {
		Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
		for (Output output : outputFormats) {
			switch (output) {
			case PENNTREES: {
				Tree copy = tree.deepCopy();
				setSentimentLabels(copy);
				// out.println(copy);
				break;
			}
			case VECTORS: {
				Tree copy = tree.deepCopy();
				setIndexLabels(copy, 0);
				// out.println(copy);
				outputTreeVectors(out, tree, 0);
				break;
			}
			case ROOT: {
				// out.println("  " +
				// sentence.get(SentimentCoreAnnotations.ClassName.class));
				break;
			}
			case PROBABILITIES: {
				Tree copy = tree.deepCopy();
				setIndexLabels(copy, 0);
				// out.println(copy);
				outputTreeScores(out, tree, 0);
				break;
			}
			default:
				throw new IllegalArgumentException("Unknown output format "
						+ output);
			}
		}
	}

	static Tree outputLabelTree(PrintStream out, CoreMap sentence,
			List<Output> outputFormats) {

		Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);

		Tree copy = tree.deepCopy();
		setSentimentLabels(copy);

		return copy;

	}

	private static void help() {
		System.err.println("Known command line arguments:");
		System.err.println("  -sentimentModel <model>: Which model to use");
		System.err.println("  -parserModel <model>: Which parser to use");
		System.err.println("  -file <filename>: Which file to process");
		System.err
				.println("  -fileList <file>,<file>,...: Comma separated list of files to process.  Output goes to file.out");
		System.err.println("  -stdin: Process stdin instead of a file");
		System.err
				.println("  -input <format>: Which format to input, TEXT or TREES.  Will not process stdin as trees.  Trees need to be binarized");
		System.err
				.println("  -output <format>: Which format to output, PENNTREES, VECTOR, PROBABILITIES, or ROOT.  Multiple formats can be specified as a comma separated list.");
		System.err
				.println("  -filterUnknown: remove neutral and unknown trees from the input.  Only applies to TREES input");
	}


	private static Annotation getAnnotation(Input inputFormat, String filename,
			boolean filterUnknown) {
		switch (inputFormat) {
		case TEXT: {
			String text = IOUtils.slurpFileNoExceptions(filename);
			Annotation annotation = new Annotation(text);
			return annotation;
		}
		case TREES: {
			List<Tree> trees = SentimentUtils.readTreesWithGoldLabels(filename);
			if (filterUnknown) {
				trees = SentimentUtils.filterUnknownRoots(trees);
			}
			List<CoreMap> sentences = Generics.newArrayList();

			for (Tree tree : trees) {
				CoreMap sentence = new Annotation(Sentence.listToString(tree
						.yield()));
				sentence.set(TreeCoreAnnotations.BinarizedTreeAnnotation.class,
						tree);
				sentences.add(sentence);
			}
			Annotation annotation = new Annotation("");
			annotation
					.set(CoreAnnotations.SentencesAnnotation.class, sentences);
			return annotation;
		}
		default:
			throw new IllegalArgumentException("Unknown format " + inputFormat);
		}
	}

	public static List<SentObject> parsing_sentence(String textDoc, String outputFileName,
			StringBuffer sb, boolean threeClass) throws IOException {

		List<SentObject> sentList = new ArrayList<SentObject>();
		String regex = "(<sentence)(.+?)(</sentence>)"; // <S> ~ </S>

		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE
				| Pattern.DOTALL | Pattern.MULTILINE);
		Matcher m = p.matcher(textDoc);

		ArrayList<adj_info> adj_list = new ArrayList<adj_info>();
		HashMap<String, Integer> nnMap = new HashMap<String, Integer>();

		m.find();
		while (true) {
			String g = m.group(); // input sentence <S>~</S>

			/* POS information */
			String regex2 = "(<word)(.+?)(word>)";
			Pattern p2 = Pattern.compile(regex2, Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL | Pattern.MULTILINE);
			Matcher m2 = p2.matcher(g);
			int index = 0;
			while (m2.find()) {

				String a = m2.group();
				if (a.contains("\"JJ")) {
					a = a.replaceAll("<[^>]*>", "");
					a = a.trim();
					adj_list.add(new adj_info(a,index));
					
				} else if (a.contains("\"NN")) {
					a = a.replaceAll("<[^>]*>", "");
					a = a.trim();
					nnMap.put(a , index);
				}
				index++;

			}


			String regex3 = "<[^>]*>"; // 모든 tag 제거
			g = g.replaceAll(regex3, "");
			g = g.replaceAll("\n", " ");
			g = g.replaceAll("   ", " ");
			g = g.trim();

			System.out.println(g);
			int non = 0;
			non = treeBank(g, adj_list,  nnMap, outputFileName, sb,
					threeClass, sentList);

			if (m.find()) {
				if (non == 0)
					sb.append(",");
			} else
				break;

			adj_list.clear();
			//adjMap.clear();
			nnMap.clear();

		}
		return sentList;
	}

	private static int treeBank(String s, ArrayList<adj_info> adj_list,
			 HashMap<String, Integer> nnMap,
			String outputFileName, StringBuffer sb, boolean threeClass, List<SentObject> sentList)
			throws IOException {

		List<Output> outputFormats = Arrays
				.asList(new Output[] { Output.PROBABILITIES });

		String line = s;
		String result = "";
		if (line.length() > 0) {

			Annotation annotation = pipeline.process(line);

			int numSen = 0;
			int adjNumOfFirst = 0;
			StringBuilder stb = new StringBuilder();
			for (CoreMap sentence : annotation
					.get(CoreAnnotations.SentencesAnnotation.class)) {
				if (numSen == 0) {
					for (int k = 0; k < adj_list.size(); k++) {
						String adj = adj_list.get(k).get_adj();
						if (sentence.toString().contains(adj))
							adjNumOfFirst++;
					}
				}
				numSen++;
			}

			if (numSen > 1) // exception handling
				return 1;

			// sentObject List

			int check = 0;
			for (CoreMap sentence : annotation
					.get(CoreAnnotations.SentencesAnnotation.class)) {

				//Tree parseTree = parser.parse(sentence.toString());

				Tree t = outputLabelTree(System.out, sentence, outputFormats);

				result = t.toString();

				String sentPolarity = result.substring(1, 2);

				if (check > 0) // 예외처리 . ? 가 문장으로 인싱되서 json 꺠짐
					break;
				check++;

				sb.append("{\"original sentence is\":" + "\"" + sentence
						+ "\","); // original sentence 객체 시작
				sb.append("\"sentence polarity\":" + "\""
						+ polarityToString(Integer.parseInt(sentPolarity))
						+ "\",");
				sb.append("\"word\":["); // word 배열 시작

				
				List<WordObject> wordList = new ArrayList<WordObject>();
				// 모든 형용사에 대하여.
				int start = 0;
				for (int i = 0; i < adj_list.size(); i++) {
					String adj = adj_list.get(i).get_adj();
					// 형용사의 확률 구함

					int endResult = result.indexOf(adj_list.get(i).get_adj());
					int adj_index = 0;
					if (endResult != -1) {
						String st = result.substring(0, endResult); // tree
																	// 출력상에서
																	// 형용사의
																	// index를
																	// 계산함

						for (int j = 0; j < st.length(); j++) {
							if (st.charAt(j) == '(')
								adj_index++;
						}
					}

					PolarityProb c = outputProbability(System.out, sentence,
							outputFormats, adj_index - 1);

					int sub_end = result.indexOf(adj, start);

					if (sub_end == -1) {
						System.out.println("failed search for " + adj);
					} else {
						String senti = result.substring(sub_end - 2,
								sub_end - 1);


						int indexOfAdj = adj_list.get(i).get_index();
						

						Set<Entry<String, Integer>> set = nnMap.entrySet();
						Iterator<Entry<String, Integer>> it = set.iterator();

						/* tree상에서 가장 가까운 명사 찾기 */
						int min = 1000;
						String noun = "";
						while (it.hasNext()) {
							Map.Entry<String, Integer> e = (Map.Entry<String, Integer>) it
									.next();

							int dis = findShortestNoun(t, indexOfAdj,
									e.getValue());

							if (dis <= min) { // 등호로 인해 같은 거리 일때 나중에 것을 선택하도록 됨
								noun = e.getKey();
								min = dis;
							}

						}

						// 문자 변환
						if (senti.contains("0"))
							senti = "--";
						else if (senti.contains("1"))
							senti = "-";
						else if (senti.contains("2"))
							senti = "neutral";
						else if (senti.contains("3"))
							senti = "+";
						else if (senti.contains("4"))
							senti = "++";

						/* 파일 출력 */
						if (isNetral(c)) {

							if (!targetList.contains(noun))
								targetList.add(noun);

							String pol = null;
							if (threeClass == true) {
								pol = "\"positive\":"
										+ "\""
										+ Float.toString((c.polar[3] + c.polar[4]))
										+ "\""
										+ ","
										+ "\"neutral\":"
										+ "\""
										+ Float.toString((c.polar[2]))
										+ "\""
										+ ","
										+ "\"negative\":"
										+ "\""
										+ Float.toString((c.polar[0] + c.polar[1]))
										+ "\"";
							} else {
								pol = "\"strong positive\":" + "\""
										+ Float.toString((c.polar[4])) + "\""
										+ "," + "\"positive\":" + "\""
										+ Float.toString((c.polar[3])) + "\""
										+ "," + "\"neutral\":" + "\""
										+ Float.toString((c.polar[2])) + "\""
										+ "," + "\"negative\":" + "\""
										+ Float.toString((c.polar[1])) + "\""
										+ "," + "\"strong negative\":" + "\""
										+ Float.toString((c.polar[0])) + "\"";
							}

							String out = "{" + "\"opinion word\":" + "\"" + adj
									+ "\"" + "," + "\"opinion target\":" + "\""
									+ noun + "\"" + "," + "\"polarity score\":"
									+ "[{" + pol + "}]" + "}";

							if (i != (adj_list.size() - 1)
									&& i < adjNumOfFirst - 1) { // numSen<2 전처리
								out = out + ",";
							}
							sb.append(out);
							
							wordList.add(new WordObject(adj,noun,pol));
							System.out.println("("+noun+","+adj+","+senti +")" );

						}
	

					}

					start = sub_end;

				}// adj for end

				sb.append("]"); // word 배열 끝
				if ((sb.substring(sb.length() - 2)).contains(",]")) {
					sb.deleteCharAt(sb.length() - 2);
				}
				sentList.add(new SentObject(sentence.toString(),polarityToString(Integer.parseInt(sentPolarity)),wordList));

			}// sentence for end
			sb.append("}");// original sentence 객체 종료

		} else {
			// Output blank lines for blank lines so the tool can be
			// used for line-by-line text processing
			// System.out.println("");
		}

		return 0;

	}

	/* xml 결과 폴더 안에 .txt파일 모두 불러오기 */
	private static List<File> addTxtFile(String dirName) {
		File root = new File(dirName);
		List<File> files = new ArrayList<File>();
		for (File child : root.listFiles()) {
			if (child.isFile() && child.getName().endsWith(".txt")) {
				files.add(child);
			}

		}
		return files;
	}

	private static int findShortestNoun(Tree t, int adj, int candidate) {

		List<Tree> tr = t.getLeaves();
		List<Tree> adjTreeList = new ArrayList<Tree>();
		List<Tree> nounTreeList = new ArrayList<Tree>();
		Tree a = tr.get(adj).parent(t); // change index to input variable
		while (!a.equals(t)) {
			// insert data
			a = a.parent(t);
			adjTreeList.add(a);
		}
		a = tr.get(candidate).parent(t); // change index to input variable
		while (!a.equals(t)) {
			// insert data
			a = a.parent(t);
			nounTreeList.add(a);
		}
		int distance = 100;
		for (int i = 0; i < adjTreeList.size(); i++) {
			for (int j = 0; j < nounTreeList.size(); j++) {
				if (adjTreeList.get(i).equals(nounTreeList.get(j))
						&& (i + j) < distance) {
					distance = i + j;
				}
			}
		}
		return distance;
	}

	private static boolean isNetral(PolarityProb a) {

		for (int i = 0; i < 5; i++) {
			if (a.polar[2] < a.polar[i])
				return true;
		}

		return false;

	}
	/**
	 * 
	 * 문장 단위의 감성 분석 결과를 수행하는 메소드.
	 * 
	 * @param inputDirPath 입력될 파일이 저장되어 있는 입력 디렉토리의 절대/상대경로를 입력받는 변수
	 * @param outputDirPath 감성 분석 결과가 저장될 절대/상대경로를 입력 받는 변수
	 * @param fineGrained 감성 분석 결과의 극성을 결정. true 일 경우 5가지 {strong positive, positive, neutral, negative, strong negative}의 클래스로 감성 분석을 시행하고 false 일 경우 3가지 {positive, neutral, negative} 클래스로 감성 분석 시행
	 * @return List<FileOjbect> FileOjbect의 리스트 형태로 리턴. 입력된 파일 단위로 감성 분석 결과를 저장하기 위해 정의된 FileObject 클래스를 참조
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static List<FileObject> senti(String inputDirPath, String outputDirPath,
			Boolean fineGrained) throws IOException, ClassNotFoundException {
		List<File> files = addTxtFile(inputDirPath); // input directory
		List<FileObject> resultList = new ArrayList<FileObject>();
		
		
		for (int i = 0; i < files.size(); i++) {
			StringBuffer sb = new StringBuffer();
			File file = files.get(i);
			String outputFileName = outputDirPath + "/" + file.getName();
			BufferedWriter output = new BufferedWriter(new FileWriter(
					outputFileName)); // output directory
			sb.append("{\"document\":["); // document 배열 시작

			BufferedReader input = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			String taggedDoc = irnlp.tagger(file.getCanonicalPath());

			// temp 파일 없으면 생성
			Path outputPath = null;
			try {
				outputPath = Paths.get("temp");
				if (!Files.exists(outputPath)) {
					Files.createDirectories(outputPath);
				}

			} catch (NullPointerException e) {
				// System.err.println();
			} catch (InvalidPathException e) {

			} catch (IOException e) {
				e.printStackTrace();
			}

			/* pos tagging, module 3에 전달 */
			String t = Tagger_IRNLP.tagger2(file.getCanonicalPath());
			Annotation annotation = pipeline.process(t);

			StringBuilder stb = new StringBuilder();
			for (CoreMap sentence : annotation
					.get(CoreAnnotations.SentencesAnnotation.class)) {
				for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

					String word = token.get(TextAnnotation.class);
					String pos = token.get(PartOfSpeechAnnotation.class);
					stb.append(word + "/" + pos + " ");
				}
				stb.append("\n");
			}
			
			// System.out.println(stb.toString());

			BufferedWriter outputPOS = new BufferedWriter(new FileWriter(
					posPath));
			outputPOS.write(stb.toString());
			outputPOS.close();

			String fileName = "\"" + file.getName() + "\""; // file path value
			sb.append("{\"file name\":" + fileName); // file path 객체 시작
			sb.append(",\"sentence\":["); // sentence 배열 시작

			List<SentObject> so = parsing_sentence(taggedDoc, outputFileName, sb, fineGrained);
			
			resultList.add(new FileObject(file.getName(),so));
			
			if ((sb.substring(sb.length() - 1)).contains(",")) {
				sb.deleteCharAt(sb.length() - 1); // 마지막 문장 , 예외처리

			}

			sb.append("]"); // sentence 배열 끝
			input.close();
			sb.append("}"); // file path 객체 끝

			sb.append("]}"); // document 배열,객체 끝
			
			
			
			output.write(jsonBeautifier(sb.toString()));
			output.close();

		}
		System.out.println("The anlysis has been done.");
		return resultList;


	}
	/**
	 * json 형태의 문자열을 indentation이 삽입된 형식으로 리턴
	 * 
	 * @param s
	 * @return
	 */
	public static String jsonBeautifier(String s){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(s);
		String prettyJsonString = gson.toJson(je);	
	
		return prettyJsonString;
	}

	private static void targetListWrite(String outputDirPath)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		List<File> files = addPosFile(outputDirPath);

		String doc = "";
		for (int i = 0; i < files.size(); i++) {
			File f = files.get(i);

			doc += TxtReader.readFile(f.getCanonicalPath());
		}

		for (int i = 0; i < targetList.size(); i++) {
			int freq = doc.split(targetList.get(i)).length - 1;
			sb.append(targetList.get(i) + "\t" + freq + "\n");

		}

		BufferedWriter output = new BufferedWriter(new FileWriter(outputDirPath+"/targetlist"));

		output.write(sb.toString());
		output.close();
	}

	private static List<File> addPosFile(String dirName) {
		File root = new File(dirName);
		List<File> files = new ArrayList<File>();
		for (File child : root.listFiles()) {
			if (child.isFile() && child.getName().endsWith(".pos")) {
				files.add(child);
			}/*
			 * if (child.isFile() ) { files.add(child); }
			 */
		}
		return files;
	}

	private static void senti_sentResult(String inputDirPath,
			String outputDirPath, boolean threeClass) throws IOException,
			ClassNotFoundException {

		File outputD = new File(outputDirPath);
		if (!outputD.exists()) {
			outputD.mkdir();
		}

		List<File> files = addTxtFile(inputDirPath); // input directory

		for (int i = 0; i < files.size(); i++) {
			StringBuffer sb = new StringBuffer();
			File file = files.get(i);
			String outputFileName = outputDirPath + "/" + file.getName();

			BufferedWriter output = new BufferedWriter(new FileWriter(
					outputFileName)); // output directory
			sb.append("{\"document\":["); // document 배열 시작

			BufferedReader input = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			String taggedDoc = irnlp.tagger(file.getCanonicalPath()); // file 의
																		// path를
																		// 받아서
																		// tagged
																		// string
																		// 리턴

			String fileName = "\"" + file.getName() + "\""; // file path value
			sb.append("{\"file name\":" + fileName); // file path 객체 시작
			sb.append(",\"sentence\":["); // sentence 배열 시작

			parsing_sentence(taggedDoc, outputFileName, sb, threeClass);

			if ((sb.substring(sb.length() - 1)).contains(",")) {
				sb.deleteCharAt(sb.length() - 1); // 마지막 문장 , 예외처리

			}

			sb.append("]"); // sentence 배열 끝
			input.close();
			sb.append("}"); // file path 객체 끝

			sb.append("]}"); // document 배열,객체 끝
			output.write(sb.toString());
			output.close();

		}
		System.out.println("The anlysis has been done.");
	}

	private static String printTree(Tree tree, String format) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		TreePrint oTreePrint = new TreePrint(format);
		oTreePrint.printTree(tree, pw);
		return sw.toString().trim();
	}

	private static String polarityToString(int polarity) {

		String a = "";
		switch (polarity) {

		case 0:
			a = "strong negative";
			break;

		case 1:
			a = "negative";
			break;

		case 2:
			a = "neutral";
			break;

		case 3:
			a = "positive";
			break;

		case 4:
			a = "strong positive";
			break;

		default:
			a = "neutral";
		}

		return a;

	}

	public static String getAdjfromSent(String s) throws IOException {

		List<Output> outputFormats = Arrays
				.asList(new Output[] { Output.PROBABILITIES });

		String line = s;
		String result = "";
		if (line.length() > 0) {

			Annotation annotation = pipeline.process(line);

			int check = 0;
			for (CoreMap sentence : annotation
					.get(CoreAnnotations.SentencesAnnotation.class)) {
				Tree t = outputLabelTree(System.out, sentence, outputFormats);
				result = t.toString();

			}

		}
		return result;
	}

}
