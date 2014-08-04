package samsung_sentiment_module;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import samsung_sentiment_module.abs.ModuleRunner;
import samsung_sentiment_module.util.ArgumentHelper;

public class Main {
	private enum Module {
		SENTIMENT, TARGET, HIERARCHY
	};

	public static void main(String[] args) {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("");
		parser.addArgument("-p", "--propertyFile")
				.metavar("<property_file>")
				.type(String.class)
				.nargs("?")
				.help("the property file path\n(it overrides the command line arguments)");

		Subparsers subparsers = parser.addSubparsers().help("module to run")
				.dest("Module");

		// Sentiment
		Subparser parserSentiment = subparsers.addParser("Sentiment").help(
				"Sentiment module");
		parserSentiment.addArgument("-i", "--inputDirPath")
				.metavar("<dir_path>").type(String.class).nargs("?")
				.help("the input dir path");

		parserSentiment.addArgument("-o", "--outputDirPath")
				.metavar("<dir_path>").type(String.class).nargs("?")
				.help("the output dir path");

		parserSentiment.addArgument("-fine", "--fineGrained")
				.metavar("<-option>").type(String.class).nargs("?")
				.help("finedGrained option");

		// Target
		Subparser parserTarget = subparsers.addParser("Target").help(
				"Target module");
		parserTarget.addArgument("-corpus", "--corpusDirPath")
				.metavar("<file_path>").type(String.class).nargs("?")
				.help("the corpus dir path");

		parserTarget.addArgument("-o", "--outputDirPath").metavar("<dir_path>")
				.type(String.class).nargs("?").help("the output dir path");

		parserTarget.addArgument("-if", "--inputFilePath")
				.metavar("<cache_dir>").type(String.class).nargs("?")
				.help("the input File path");

		parserTarget.addArgument("-df", "--domainFilePath")
				.metavar("<cache_dir>").type(String.class).nargs("?")
				.help("the domain File path");

		parserTarget.addArgument("-pmi", "--pmiThreshold")
				.metavar("<cache_dir>").type(String.class).nargs("?")
				.help("the pmi score");

		parserTarget.addArgument("-co", "--coThreshold").metavar("<cache_dir>")
				.type(String.class).nargs("?").help("the co_occurrence score");

		// Hierarchy
		Subparser parserHierarchy = subparsers.addParser("Hierarchy").help(
				"Hierarchy module");
		parserHierarchy.addArgument("-centroid", "--centroidFilePath")
				.metavar("<file_path>").type(String.class).nargs("?")
				.help("the centroid file path");
		parserHierarchy.addArgument("-o", "--outputDirPath")
				.metavar("<dir_path>").type(String.class).nargs("?")
				.help("the output dir path");
		parserHierarchy
				.addArgument("-cache", "--cacheDirPath")
				.metavar("<cache_dir>")
				.type(String.class)
				.nargs("?")
				.help("the cache dir path (if generated in previous iteration)");

		Namespace parsedArgs = ArgumentHelper
				.handleArgumentString(args, parser);

		// 선택된 모듈 실행
		Module module = Module.valueOf(parsedArgs.getString("Module")
				.toUpperCase());
		ModuleRunner runner = null;
		switch (module) {
		case SENTIMENT:
			runner = new Sentiment();
			runner.run(args, parsedArgs);
			break;
		case TARGET:
			runner = new Target();
			runner.run(args, parsedArgs);
			break;
		case HIERARCHY:
			runner = new Hierarchy();
			runner.run(args, parsedArgs);
			break;
		default:
			System.err.println("Wrong module is selected");
			System.exit(1);
			break;
		}
	}
}
