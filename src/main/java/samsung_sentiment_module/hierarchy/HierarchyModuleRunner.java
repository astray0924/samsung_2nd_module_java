package samsung_sentiment_module.hierarchy;

import java.io.IOException;

import net.sourceforge.argparse4j.inf.Namespace;
import samsung_sentiment_module.abs.ModuleRunner;

public class HierarchyModuleRunner implements ModuleRunner {

	@Override
	public void run(String[] args, Namespace parsedArgs) {
		FeatureExtractor extractor = null;
		String outputDirPath = parsedArgs.getString("outputDirPath");
		String centroidFilePath = parsedArgs.getString("centroidFilePath");
		String cacheDirPath = parsedArgs.getString("cacheDirPath");

		try {
			extractor = new FeatureExtractor(outputDirPath, centroidFilePath);

			if (cacheDirPath != null) {

				extractor.loadCache(cacheDirPath);
			} else {

				extractor.extract();
				extractor.vectorize();
				extractor.saveCache();
			}

			extractor.classifyAll();

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

}
