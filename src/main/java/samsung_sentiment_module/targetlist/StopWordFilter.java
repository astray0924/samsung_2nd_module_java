package samsung_sentiment_module.targetlist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

class StopWordFilter {
	
	
	static Map<String,String> mapa = new HashMap<String,String>();
	
	
	protected static void runStopWord(){
		
		readFile("stopwordlist");
	}
	
	protected String stopRemoval(String line){
		return line;
		

	}
	
	protected static void readFile(String inputFileName){
		
		InputStream is = StopWordFilter.class
				.getResourceAsStream("/stopwordlist");
		
	    try(BufferedReader in = new BufferedReader(new InputStreamReader(
				is, "UTF-8")) ) {

	       // BufferedReader in = new BufferedReader(new FileReader(inputFileName));
	        String s ="";

	        while ((s = in.readLine()) != null) {
	          String text[] = s.split("\t");
	          for(int i = 0 ; i < text.length ; i++){
	        	  if(!mapa.containsKey(text)){
	        		  mapa.put(text[i], text[i]);
	        	  }
	          }
	        }
	        in.close();
	      } catch (IOException e) {
	          System.err.println(e); // 에러가 있다면 메시지 출력
	          System.exit(1);
	      }
		
		
	}

}
