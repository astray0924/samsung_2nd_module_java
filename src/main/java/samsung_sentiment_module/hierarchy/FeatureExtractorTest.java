package samsung_sentiment_module.hierarchy;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class FeatureExtractorTest {
	FeatureExtractor extractor;

	@Before
	public void init() throws IOException {
		extractor = new FeatureExtractor("./output", "./centroids.txt");
	}

	@Test
	public void testExtract() {
		try {
			extractor.extract();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	@Test
	public void testSanitize() throws IOException {
		String[] testTokens = new String[] { "test", "test2", "nice resturant",
				"nice\tresturant", "Chinese restaurant" };

		for (String token : testTokens) {
			String newToken = extractor.sanitize(token);

			System.out.println(newToken);
		}
	}

	@Test
	public void testPopulateCentroids() throws IOException {
		extractor.populateCentroids();
		extractor.debugCentroids();
	}

}
