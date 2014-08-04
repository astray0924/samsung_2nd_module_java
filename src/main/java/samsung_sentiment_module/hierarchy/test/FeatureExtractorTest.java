package samsung_sentiment_module.hierarchy.test;

import java.io.IOException;

import org.junit.Test;

import samsung_sentiment_module.hierarchy.FeatureExtractor;

public class FeatureExtractorTest {

	@Test
	public void testSanitize() throws IOException {
		String[] testTokens = new String[] { "test", "test2", "nice resturant",
				"nice\tresturant", "Chinese restaurant"};

		FeatureExtractor extractor = new FeatureExtractor();
		for (String token : testTokens) {
			String newToken = extractor.sanitize(token);

			System.out.println(newToken);
		}
	}

}
