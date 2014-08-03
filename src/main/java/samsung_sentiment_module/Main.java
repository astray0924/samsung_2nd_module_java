package samsung_sentiment_module;

import samsung_sentiment_module.hierarchy.HierarchyModuleRunner;
import samsung_sentiment_module.sentiment.SentimentPipeline;

public class Main {
	private enum Module {
		SENTIMENT, TARGET, HIERARCHY
	};

	public static void main(String[] args) throws ClassNotFoundException {
		// 옵션 파싱하여 실행 모듈 및 input 파악
		Module mode = Module.HIERARCHY;

		// switch 문으로 실행할 모듈 선택
		SamsungModule module = null;
		
		switch (mode) {
		case SENTIMENT:	// 감성 분석 모듈 
			module = new SentimentPipeline();
			module.run(args);
			break;
		case TARGET:	// 타겟 추출 모듈 
			module = new SentimentPipeline();
			module.run(args);
			break;
		case HIERARCHY:	// 타겟 구조 추정 모듈 
			module = new HierarchyModuleRunner();
			module.run(args);
			break;
		default:
			break;
		}
	}
}
