package samsung_2nd_module_java;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws ClassNotFoundException {
		FeatureExtractor extractor = new FeatureExtractor();
		try {
			extractor.extract();
			extractor.vectorize();
			extractor.serializeOutputs();
			extractor.deserializeOutputs();
		} catch (IOException e) {
			e.printStackTrace();
		}
		extractor.debugClassification("restaurant");
	}
}
