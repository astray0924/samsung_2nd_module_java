package samsung_sentiment_module.abs;

import net.sourceforge.argparse4j.inf.Namespace;

public interface ModuleRunner {
	public void run(String[] args, Namespace parsedArgs);
}
