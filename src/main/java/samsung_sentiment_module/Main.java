package samsung_sentiment_module;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import samsung_sentiment_module.hierarchy.HierarchyModuleRunner;
import samsung_sentiment_module.sentiment.SentimentPipeline;

public class Main {
	private static ArgumentParser parser = null;

	private enum Module {
		SENTI, TARGET, HIERARCHY
	};

	public static void main(String[] args) throws ClassNotFoundException {
		// 옵션 parser 구현
		parser = initArgumentParser();
		Namespace parsedArgs = null;

		try {
			parsedArgs = parser.parseArgs(args);
			System.out.println(parsedArgs);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
		}

		// 실행할 모듈 선택
		String modeString = null;
		Module mode = null;
		try {
			modeString = parsedArgs.getString("mode");
			mode = Module.valueOf(modeString.toUpperCase());
		} catch (NullPointerException e) {
			System.err
					.println("The running mode is not specified! (-m [senti|target|hierarchy])");
			System.exit(1);
		} catch (IllegalArgumentException e) {
			System.err.println("Unidentified mode: " + modeString);
			System.exit(1);
		}

		// switch 문으로 선택된 모듈 실행
		SamsungModuleRunner module = null;

		switch (mode) {
		case SENTI: // 감성 분석 모듈
			module = new SentimentPipeline();
			module.run(args, parsedArgs);
			break;
		case TARGET: // 타겟 추출 모듈
			module = new SentimentPipeline();
			module.run(args, parsedArgs);
			break;
		case HIERARCHY: // 타겟 구조 추정 모듈
			module = new HierarchyModuleRunner();
			module.run(args, parsedArgs);
			break;
		default:
			break;
		}
	}

	private static ArgumentParser initArgumentParser() {
		ArgumentParser parser = ArgumentParsers.newArgumentParser(
				"sentiment module").description("samsung sentiment module");

		parser.addArgument("-m", "--mode")
				.metavar("senti | target | hierarchy").type(String.class)
				.nargs("?")
				.help("the module to run: senti | target | hierarchy");
		parser.addArgument("-i", "--inputDirPath")
				.metavar("<file_path>")
				.type(String.class)
				.nargs("?")
				.help("the path of input directory \n(required for senti and target module)");
		parser.addArgument("-o", "--outputDirPath").metavar("outputDirPath")
				.type(String.class).nargs("?")
				.help("the path of output directory");
		parser.addArgument("-classnum", "--number_of_classes")
				.dest("classNum")
				.metavar("N")
				.type(Integer.class)
				.nargs("?")
				.setDefault(2)
				.help("the number of sentiment classes to be used\n(2: [+/-] OR 3: [+/-/0])\n(used in senti_module)");
		parser.addArgument("-target_domain", "--targetDomainFilePath")
				.metavar("<file_path>")
				.type(String.class)
				.nargs("?")
				.help("the path of target domain file\n(used in target_module)");
		parser.addArgument("-pmi_thres", "--pmi_threshold")
				.dest("pmiThreshold").metavar("<thres_val>").type(Float.class)
				.nargs("?").setDefault(0.0F)
				.help("the PMI threshold\n(used in target_module)");
		parser.addArgument("-co_thres", "--co_occurrence_threshold")
				.dest("coThreshold").metavar("<thres_val>").type(Float.class)
				.nargs("?").setDefault(0.0F)
				.help("the co-occurrence threshold\n(used in target_module)");
		parser.addArgument("-centroid", "--centroidFilePath")
				.metavar("<file_path>").type(String.class).nargs("?")
				.help("the path of centroid file\n(used in hierarchy_module)");
		parser.addArgument("-cache", "--cachedVectorDirPath")
				.metavar("<cache_dir>").type(String.class).nargs("?")
				.help("the path of cached vectors' directory");

		String example = "example:\n"
				+ "\tsenti_module: java -jar <jar_file> -m senti -i ./input -o ./output -classnum 3\n"
				+ "\ttarget_module: java -jar <jar_file> \n"
				+ "\thierarchy_module: java -jar <jar_file> -m hierarchy -centroid centroids.txt -o ./output\n"
				+ "java -jar <jar_file> -m hierarchy -centroid centroids.txt -o ./output -cache ./output/vectors";
		parser.epilog(example);

		return parser;
	}
}
