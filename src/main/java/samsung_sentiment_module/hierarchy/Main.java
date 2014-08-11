package samsung_sentiment_module.hierarchy;

import java.io.IOException;

import net.sourceforge.argparse4j.inf.Namespace;
import samsung_sentiment_module.abs.ModuleRunner;

public class Main implements ModuleRunner {

	@Override
	public void run(String[] args, Namespace parsedArgs) {
		OpinionTargetClassifier vectorizer = null;
		String inputFilePath = parsedArgs.getString("inputFilePath");
		String outputDirPath = parsedArgs.getString("outputDirPath");
		String centroidFilePath = parsedArgs.getString("centroidFilePath");
		String cacheDirPath = parsedArgs.getString("cacheDirPath");

		try {
			vectorizer = new OpinionTargetClassifier(inputFilePath, outputDirPath,
					centroidFilePath);
			if (cacheDirPath != null) {
				vectorizer.loadVectorsFromCache(cacheDirPath);
			} else {
				vectorizer.extractContexts();
				vectorizer.vectorizeContexts();
				vectorizer.storeVectorsAsCache();
			}

			vectorizer.classifyAll();

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

}
