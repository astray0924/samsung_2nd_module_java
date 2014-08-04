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
import samsung_sentiment_module.targetlist.*;

public class Target {
	public static void main(String[] args) {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("Target");

		parser.addArgument("-corpus", "--corpusDirPath").metavar("<file_path>")
				.type(String.class).nargs("?").help("the corpus dir path");

		parser.addArgument("-o", "--outputDirPath").metavar("<dir_path>")
				.type(String.class).nargs("?").help("the output dir path");

		parser.addArgument("-if", "--inputFilePath").metavar("<cache_dir>")
				.type(String.class).nargs("?").help("the input File path");

		parser.addArgument("-df", "--domainFilePath").metavar("<cache_dir>")
				.type(String.class).nargs("?").help("the domain File path");

		parser.addArgument("-pmi", "--pmiThreshold").metavar("<cache_dir>")
				.type(String.class).nargs("?").help("the pmi score");

		parser.addArgument("-co", "--coThreshold").metavar("<cache_dir>")
				.type(String.class).nargs("?").help("the co_occurrence score");

		Namespace parsedArgs = ArgumentHandler.handleArgumentString(args,
				parser);

		// 실행
		targetModuleRunner runner = new targetModuleRunner();
		runner.run(args, parsedArgs);
	}
}
