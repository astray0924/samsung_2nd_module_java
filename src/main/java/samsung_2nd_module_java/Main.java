package samsung_2nd_module_java;

import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		FeatureExtractor extractor = new FeatureExtractor();
		try {
			extractor.extract();
			extractor.vectorize();
			extractor.debug();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
