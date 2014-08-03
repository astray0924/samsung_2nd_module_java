package samsung_2nd_module_java.hierarchy;

import java.io.IOException;

import samsung_2nd_module_java.SamsungModule;

public class HierarchyModuleRunner implements SamsungModule {

	@Override
	public void run() {
		FeatureExtractor extractor = new FeatureExtractor();
		try {
			extractor.extract();
			extractor.vectorize();
			extractor.serializeOutputs();
			extractor.deserializeOutputs();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		extractor.debugClassification("restaurant");

	}

}
