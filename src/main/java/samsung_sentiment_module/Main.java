package samsung_sentiment_module;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;

public class Main {

	public static void main(String[] args) {
		ArgumentParser parser = ArgumentParsers
				.newArgumentParser("Sentiment Module");

		parser.addArgument("Module")
				.choices("Sentiment", "Target", "Hierarchy")
				.metavar("Sentiment | Target | Hierarchy").type(String.class)
				.nargs("?").help("the module to run");

		// Property 파일
	}

}
