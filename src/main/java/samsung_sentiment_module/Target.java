package samsung_sentiment_module;

import net.sourceforge.argparse4j.inf.Namespace;
import samsung_sentiment_module.abs.ModuleRunner;
import samsung_sentiment_module.targetlist.targetModuleRunner;

public class Target implements ModuleRunner {
	public static void main(String[] args) {
		// ArgumentParser parser = ArgumentParsers.newArgumentParser("Target");
		//
		//
		//
		// // 실행
		// targetModuleRunner runner = new targetModuleRunner();
		// runner.run(args, parsedArgs);
	}

	@Override
	public void run(String[] args, Namespace parsedArgs) {
		// 실행
		targetModuleRunner runner = new targetModuleRunner();
		runner.run(args, parsedArgs);

	}
}
