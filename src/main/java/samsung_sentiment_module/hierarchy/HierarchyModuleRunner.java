package samsung_sentiment_module.hierarchy;

import java.io.IOException;

import samsung_sentiment_module.SamsungModule;

public class HierarchyModuleRunner implements SamsungModule {

	@Override
	public void run(String[] args) {
		FeatureExtractor extractor = null;
		try {
			extractor = new FeatureExtractor();
			 extractor.extract();
			 extractor.vectorize();
			 extractor.serializeOutputs();
			extractor.deserializeOutputs();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		extractor.debugClassification("location");

	}

}
