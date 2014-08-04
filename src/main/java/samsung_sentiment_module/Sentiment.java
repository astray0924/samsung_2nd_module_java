package samsung_sentiment_module;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import samsung_sentiment_module.abs.ModuleRunner;
import samsung_sentiment_module.sentiment.SentimentPipeline;

public class Sentiment {
	public static void main(String[] args) {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("Sentiment");

		parser.addArgument("-i", "--inputDirPath")
				.metavar("<dir_path>").type(String.class).nargs("?")
				.help("the input dir path");
		
		parser.addArgument("-o", "--outputDirPath").metavar("<dir_path>")
				.type(String.class).nargs("?").help("the output dir path");
		
		parser.addArgument("-fine", "--fineGrained")
				.metavar("<-option>").type(String.class).nargs("?")
				.help("finedGrained option");

		Namespace parsedArgs = null;
		try {
			parsedArgs = parser.parseArgs(args);
			System.out.println(parsedArgs);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
		}

		// 만약 output 디렉토리가 존재하지 않으면 새로 생성
		Path outputPath = null;
		try {
			outputPath = Paths.get(parsedArgs.getString("outputDirPath"));
			if (!Files.exists(outputPath)) {
				Files.createDirectories(outputPath);
			}

		} catch (NullPointerException e) {
			// System.err.println();
		} catch (InvalidPathException e) {

		} catch (IOException e) {
			e.printStackTrace();
		}

		// 실행
		SentimentPipeline runner = new SentimentPipeline();
		runner.run(args, parsedArgs);
	}
}
