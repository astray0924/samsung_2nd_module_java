package samsung_sentiment_module;

import net.sourceforge.argparse4j.inf.Namespace;
import samsung_sentiment_module.abs.ModuleRunner;
import samsung_sentiment_module.hierarchy.HierarchyModuleRunner;

public class Hierarchy implements ModuleRunner {

	@Override
	public void run(String[] args, Namespace parsedArgs) {
		// 실행
		ModuleRunner runner = new HierarchyModuleRunner();
		runner.run(args, parsedArgs);

	}
}
