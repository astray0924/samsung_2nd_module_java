package samsung_sentiment_module.targetlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
//import org.apache.xpath.operations.String;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class coOccurOpinion {

	public static String text[];
		
	public static void txtToSenti(String[] ori, StanfordCoreNLP pipeline){
		
		text = new String[ori.length];

		for(int i = 0; i < ori.length ; i++){
			
			StringReader sr = new StringReader(ori[i]);
			BufferedReader br = new BufferedReader(sr);
			String line = null;
		
			try {
				
				while( (line = br.readLine()) != null){
					text[i] += extSentiAdj.sentiResult(line, pipeline);
					
					
				}
			}
			catch(Exception e){
			}
			
		}
		
		
	}
	
	public static void co_occur(){
				
		String dirName = "sentiTreeResult";
		
		fileIO io = new fileIO();
		List<File> productFileList = io.addTxtFile(dirName);
		int numOfProduct = productFileList.size();
		
		text = new String[productFileList.size()];
					
			for(int i = 0; i < productFileList.size() ; i++){
				
				//System.out.println(productFileList.get(i).getName());
				try {
					text[i] = readFile(productFileList.get(i).getPath());
					//System.out.println("treeText " + text[i]);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
	}
	public static double probOfSenti(String word){
		
		double wordCount = 0;
		double sentiCount = 0;
		for(int i = 0; i < text.length ; i++){
			
			StringReader sr = new StringReader(text[i]);
			BufferedReader br = new BufferedReader(sr);
			String line = null;
			
			
			try {
				
				while( (line = br.readLine()) != null){
						if(line.contains(word+")")){
							wordCount++;
							if(!line.startsWith("(2"))
								sentiCount++;
						}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		
		if(wordCount != 0  && sentiCount != 0 ){
			//System.out.println(sentiCount/wordCount);
			return sentiCount/wordCount;
		}
		else{
			return 0;
		}
		
		
	}
	
	public static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		
		int i = 0; 
		while ((line = reader.readLine()) != null) {
			if( line.contains("[t]"))  // 아마 리뷰 경계를 나타냄 
				continue;
			if( line.length() < 5)
				continue;		
			
			stringBuilder.append(line);
			stringBuilder.append(ls);

		}
		reader.close();
		return stringBuilder.toString();

	}
	
	
	
}
