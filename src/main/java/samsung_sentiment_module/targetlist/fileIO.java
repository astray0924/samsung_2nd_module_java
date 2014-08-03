package samsung_sentiment_module.targetlist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class fileIO {
	
	public static List<File> addTxtFile(String dirName) {
		File root = new File(dirName);
		List<File> files = new ArrayList<File>();
		for (File child : root.listFiles()) {
			/*if (child.isFile() && child.getName().endsWith(".txt")) {
				files.add(child); 
			} */	
			if (child.isFile() ) {
				files.add(child); 
			} 
		}
		return files;
	}
	
	public static void writeFile(String outputString, String outputFileName) {
		try {

			BufferedWriter out = new BufferedWriter(new FileWriter(
					outputFileName));
			out.write(outputString);
			out.newLine();
			out.close();

		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
	}

}
