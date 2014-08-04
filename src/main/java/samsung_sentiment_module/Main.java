package samsung_sentiment_module;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import samsung_sentiment_module.hierarchy.HierarchyModuleRunner;
import samsung_sentiment_module.sentiment.SentimentPipeline;

public class Main {
	private enum Module {
		SENTIMENT, TARGET, HIERARCHY
	};

	public static void main(String[] args) throws ClassNotFoundException {
		// 옵션 parser 구현
		ArgumentParser parser = ArgumentParsers.newArgumentParser(
				"sentiment module").description("samsung sentiment module");
		parser.addArgument("inputDir").metavar("<input_dir>")
				.type(String.class).nargs("?")
				.help("the path of input directory");
		parser.addArgument("outputDir").metavar("<output_dir>")
				.type(String.class).nargs("?")
				.help("the path of output directory");
		parser.addArgument("classnum").metavar("N")
				.type(Integer.class).nargs("?")
				.help("the path of output directory");

		// 실행 모드
		Module mode = Module.HIERARCHY;

		// switch 문으로 실행할 모듈 선택
		SamsungModule module = null;

		switch (mode) {
		case SENTIMENT: // 감성 분석 모듈
			module = new SentimentPipeline();
			module.run(args);
			break;
		case TARGET: // 타겟 추출 모듈
			module = new SentimentPipeline();
			module.run(args);
			break;
		case HIERARCHY: // 타겟 구조 추정 모듈
			module = new HierarchyModuleRunner();
			module.run(args);
			break;
		default:
			break;
		}
	}
}
