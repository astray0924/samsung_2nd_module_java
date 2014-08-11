package samsung_sentiment_module.targetlist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;



class computePMI {

	// tf idf 추가됨
	protected static double pmiScore(String doc, String word1, String word2, String review[]) throws IOException{
		
		StringReader sr = new StringReader(doc);
		BufferedReader br = new BufferedReader(sr);
		String line = null;
		
		double num_word1 = 0;
		double num_word2 = 0;
		double collocation = 0;
		double num_sent = 0;
		
		double t1 = TFIDF.getTFIDF(review, word1);
		double t2 = TFIDF.getTFIDF(review, word2);
		
		double tfidf = t1*t2;
		
		
		word1 = " "+word1+" ";
		word2 = " "+word2+" ";
		
		while( (line = br.readLine()) != null){
			
			boolean a1=false;
			boolean a2=false;
			if( line.toLowerCase().contains(word1.toLowerCase()) ){
				num_word1++;
				a1 = true;
			}
			if( line.toLowerCase().contains(word2.toLowerCase())){
				num_word2++;
				a2 = true;
			}
			
			if( a1 && a2){
				collocation++;
			}
			num_sent++;
			
			
		}
		
		double denominator = num_word1* num_word2;
		double numernator = collocation*tfidf*num_sent;
		

//		System.out.println("first word : "+num_word1);
//		System.out.println("second word: "+num_word2);
//		System.out.println("collocation: "+collocation);
//		System.out.println("total sent : "+num_sent);
//		System.out.println();
		double x;
		if(num_sent == 0 || num_word1==0 || num_word2 == 0 || collocation == 0 || numernator ==0)
			x = 0;
		else{
			x = numernator/denominator;
			x = Math.log(x)/Math.log(2);
		}
		
		return x;

	}
	
}
