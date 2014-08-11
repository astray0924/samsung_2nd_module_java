package samsung_sentiment_module.targetlist;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

class TFIDF {
	
	//리뷰 단위 다큐먼트 배열
	protected static double getTFIDF(String[] doc , String word){
		

		int df = 0;
		ArrayList<Double> tfList = new ArrayList<Double>();
		
		for(int i = 0 ; i < doc.length ; i++){
			
			double tf = computeTF(doc[i],  word );		
			
			tfList.add(tf);
			if(tf != 0 )
				df++;
		}
		
		double idf = 0;
		
		if(df !=0)
		{
			idf = Math.log(doc.length / df)/Math.log(2);
		}		
		

		
		double score = 0 ; 
		
		for(int i = 0 ; i < tfList.size() ; i++){
			score += tfList.get(i)*idf ;	
		}
		
		score = score;
		return score;
			
	}
	// 라인당 중복 고려
	protected static double computeTF(String doc, String word){

		double count  = 0;
		double totalWords = 0;
		
		
		
			StringReader sr = new StringReader(doc);
			BufferedReader br = new BufferedReader(sr);
			String line = null;
			
			try {
				
				while( (line = br.readLine()) != null){
					
					
						
						if( line.toLowerCase().contains(word.toLowerCase()) ){
							line = line+" ";
							count += line.split(word).length-1;		
							
						}
	
						String trim = line.trim();
						if(trim.isEmpty())
							continue;
						else{
							totalWords += trim.split("\\s+").length-1;
						}
						
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			//System.out.println(totalWords + " " + count);
		if(totalWords != 0)
			return count/totalWords;	
		else
			return 0;
	}
}
