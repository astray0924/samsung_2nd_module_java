package samsung_sentiment_module.hierarchy;

import java.io.IOException;

import net.sourceforge.argparse4j.inf.Namespace;
import samsung_sentiment_module.SamsungModuleRunner;

public class HierarchyModuleRunner implements SamsungModuleRunner {

	@Override
	public void run(String[] args, Namespace parsedArgs) {
		FeatureExtractor extractor = null;
		String outputDirPath = parsedArgs.getString("outputDirPath");
		String centroidFilePath = parsedArgs.getString("centroidFilePath");
		String cachePath = parsedArgs.getString("cachedVectorDirPath");

		try {
			extractor = new FeatureExtractor(outputDirPath, centroidFilePath);

			if (cachePath != null) {
				extractor.deserializeOutputs(cachePath);
			} else {
				extractor.extract();
				extractor.vectorize();
				extractor.serializeOutputs();
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		extractor.debugClassification("staff");

	}

}
