package samsung_sentiment_module.hierarchy;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class OpinionTargetClassifierTest {
	OpinionTargetClassifier extractor;

	@Before
	public void init() throws IOException {
		extractor = new OpinionTargetClassifier(
				"C:\\Users\\kyoungrok.jang\\Desktop\\hotels_contents_tag.txt",
				"./output", "C:\\Users\\kyoungrok.jang\\Desktop\\centroid.txt");
	}

	@Test
	public void testExtract() {
		try {
			extractor.extractContexts();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	@Test
	public void testSanitize() throws IOException {
		String[] testTokens = new String[] { "test", "test2", "nice resturant",
				"nice\tresturant", "Chinese restaurant", "decor" };

		for (String token : testTokens) {
			String newToken = extractor.correctSpelling(token);

			System.out.println(newToken);
		}
	}

	@Test
	public void testPopulateCentroids() throws IOException {
		extractor.populateCentroids();
		extractor.getCentroids();
	}

}
