package samsung_sentiment_module;

import java.util.List;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {
	private enum Module {
		SENTIMENT, TARGET, HIERARCHY
	};

	public static void main(String[] args) throws ClassNotFoundException {
		// 옵션 parser 구현
		ArgumentParser parser = ArgumentParsers.newArgumentParser(
				"sentiment module").description("samsung sentiment module");
		parser.addArgument("-i", "--inputDir")
				.metavar("<file_path>")
				.type(String.class)
				.nargs("?")
				.help("the path of input directory \n(required for senti and target module)");
		parser.addArgument("outputDir").metavar("outputDir").type(String.class)
				.nargs("?").help("the path of output directory");
		parser.addArgument("-number_of_classes", "--classnum")
				.metavar("N")
				.type(Integer.class)
				.nargs("?")
				.setDefault(2)
				.help("the number of sentiment classes to be used\n(2: [+/-] OR 3: [+/-/0])\n(used in senti_module)");
		parser.addArgument("-targetDomainFile", "--tdf")
				.metavar("<file_path>")
				.type(String.class)
				.nargs("?")
				.help("the path of target domain file\n(used in target_module)");
		parser.addArgument("-pmi_threshold", "--pmi_thres")
				.metavar("<thres_val>").type(Float.class).nargs("?")
				.setDefault(0.0F)
				.help("the PMI threshold\n(used in target_module)");
		parser.addArgument("-co_occurrence_threshold", "--co_thres")
				.metavar("<thres_val>").type(Float.class).nargs("?")
				.setDefault(0.0F)
				.help("the co-occurrence threshold\n(used in target_module)");
		parser.addArgument("-centroidFile", "--cf").metavar("<file_path>")
				.type(String.class).nargs("?")
				.help("the path of centroid file\n(used in hierarchy_module)");

		String example = "example:\n" + "senti_module: java -jar <jar_file> "
				+ "target_module: java -jar <jar_file> "
				+ "hierarchy_module: java -jar <jar_file> ";
		parser.epilog(example);

		try {
			Namespace res = parser.parseArgs(args);
			System.out.println(res);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
		}

		//
		// // 실행 모드
		// Module mode = Module.HIERARCHY;
		//
		// // switch 문으로 실행할 모듈 선택
		// SamsungModule module = null;
		//
		// switch (mode) {
		// case SENTIMENT: // 감성 분석 모듈
		// module = new SentimentPipeline();
		// module.run(args);
		// break;
		// case TARGET: // 타겟 추출 모듈
		// module = new SentimentPipeline();
		// module.run(args);
		// break;
		// case HIERARCHY: // 타겟 구조 추정 모듈
		// module = new HierarchyModuleRunner();
		// module.run(args);
		// break;
		// default:
		// break;
		// }
	}
}
