package samsung_sentiment_module.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class ArgumentHelper {
	private static Namespace loadProperties(String propPath) throws IOException {
		Map<String, Object> properties = new HashMap<String, Object>();
		Path propFile = Paths.get(propPath);

		try (BufferedReader reader = Files.newBufferedReader(propFile,
				StandardCharsets.UTF_8)) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")) {
					continue;
				}

				try {
					String[] pair = line.trim().split("=");
					String key = pair[0].trim();
					String value = pair[1].toLowerCase().trim();

					properties.put(key, value);
				} catch (IndexOutOfBoundsException e) {
					System.out.println("Failed to parse the line: " + line);
				} catch (NullPointerException e) {
					System.out.println("Failed to parse the line: " + line);
				}
			}
		}

		System.out.println(properties);

		return new Namespace(properties);
	}

	public static Namespace handleArgumentString(String[] args,
			ArgumentParser parser) {
		Namespace parsedArgs = null;

		try {
			parsedArgs = parser.parseArgs(args);
			// System.out.println(parsedArgs);

			try {
				String propPath = parsedArgs.getString("propertyFile");
				if (propPath != null) {
					parsedArgs = loadProperties(propPath);
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}

		// 만약 output 디렉토리가 존재하지 않으면 새로 생성
		Path outputPath = null;
		try {
			String outputPathString = parsedArgs.getString("outputDirPath");

			if (outputPathString != null) {
				outputPath = Paths.get(outputPathString);
				if (!Files.exists(outputPath)) {
					Files.createDirectories(outputPath);
				}

			}

		} catch (NullPointerException e) {
			// System.err.println();
		} catch (InvalidPathException e) {

		} catch (IOException e) {
			e.printStackTrace();
		}

		return parsedArgs;
	}
}
