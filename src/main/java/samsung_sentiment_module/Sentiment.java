package samsung_sentiment_module;

import net.sourceforge.argparse4j.inf.Namespace;
import samsung_sentiment_module.abs.ModuleRunner;
import samsung_sentiment_module.sentiment.SentimentPipeline;

public class Sentiment implements ModuleRunner {

	@Override
	public void run(String[] args, Namespace parsedArgs) {
		// 실행
		SentimentPipeline runner = new SentimentPipeline();
		runner.run(args, parsedArgs);

	}
}
