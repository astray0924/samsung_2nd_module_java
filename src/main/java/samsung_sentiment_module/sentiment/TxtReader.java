package samsung_sentiment_module.sentiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


class TxtReader {

	protected static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		reader.close();
		return stringBuilder.toString();

	}
}
