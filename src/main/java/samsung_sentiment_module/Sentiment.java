package samsung_sentiment_module;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import samsung_sentiment_module.sentiment.SentimentPipeline;

public class Sentiment {
	public static void main(String[] args) {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("Sentiment");

		parser.addArgument("-i", "--inputDirPath").metavar("<dir_path>")
				.type(String.class).nargs("?").help("the input dir path");

		parser.addArgument("-o", "--outputDirPath").metavar("<dir_path>")
				.type(String.class).nargs("?").help("the output dir path");

		parser.addArgument("-fine", "--fineGrained").metavar("<-option>")
				.type(String.class).nargs("?").help("finedGrained option");

		Namespace parsedArgs = ArgumentHandler.handleArgumentString(args,
				parser);

		// 실행
		SentimentPipeline runner = new SentimentPipeline();
		runner.run(args, parsedArgs);
	}
}
