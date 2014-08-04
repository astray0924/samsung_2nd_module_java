package samsung_sentiment_module;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import samsung_sentiment_module.abs.ModuleRunner;
import samsung_sentiment_module.hierarchy.HierarchyModuleRunner;

public class Hierarchy {

	public static void main(String[] args) {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("Hierarchy");

		parser.addArgument("-centroid", "--centroidFilePath")
				.metavar("<file_path>").type(String.class).nargs("?")
				.help("the centroid file path");
		parser.addArgument("-o", "--outputDirPath").metavar("<dir_path>")
				.type(String.class).nargs("?").help("the output dir path");
		parser.addArgument("-cache", "--cacheDirPath")
				.metavar("<cache_dir>")
				.type(String.class)
				.nargs("?")
				.help("the cache dir path (if generated in previous iteration)");

		Namespace parsedArgs = ArgumentHandler.handleArgumentString(args,
				parser);

		// 실행
		ModuleRunner runner = new HierarchyModuleRunner();
		runner.run(args, parsedArgs);
	}
}
