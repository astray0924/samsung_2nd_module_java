package samsung_sentiment_module;

import net.sourceforge.argparse4j.inf.Namespace;
import samsung_sentiment_module.abs.ModuleRunner;
import samsung_sentiment_module.hierarchy.HierarchyModuleMain;

public class Hierarchy implements ModuleRunner {

	@Override
	public void run(String[] args, Namespace parsedArgs) {
		// 실행
		ModuleRunner runner = new HierarchyModuleMain();
		runner.run(args, parsedArgs);

	}
}
