package samsung_sentiment_module;

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

public class ArgumentHandler {
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
		// property 파일 처리를 위한 옵션
		parser.addArgument("-p", "--propertyFile")
				.metavar("<property_file>")
				.type(String.class)
				.nargs("?")
				.help("the property file path\n(it overrides the command line arguments)");

		Namespace parsedArgs = null;

		try {
			parsedArgs = parser.parseArgs(args);
//			System.out.println(parsedArgs);

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
		}

		// 만약 output 디렉토리가 존재하지 않으면 새로 생성
		Path outputPath = null;
		try {
			outputPath = Paths.get(parsedArgs.getString("outputDirPath"));
			if (!Files.exists(outputPath)) {
				Files.createDirectories(outputPath);
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
