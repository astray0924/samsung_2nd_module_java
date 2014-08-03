package samsung_sentiment_module.targetlist;

import java.util.List;

public class wordINFO {
	
	String fileName;
	int fileNum;
	int sentNum;
	int tokenNum;
	String word;
	String pos;
	String sentence;
	String setiTaggedSent;
	double pmiScore[];
	double similarity[];
	double maxSimilarity;
	List<Integer> adjList;


	public wordINFO(int sentNum, String word){
		this.sentNum = sentNum;
		this.word = word;
	}
	
	public wordINFO(int sentNum, String word, String fileName){
		this.sentNum = sentNum;
		this.word = word;
		this.fileName = fileName;
	}
	
	public wordINFO(int sentNum, String word, int fileNum){
		this.sentNum = sentNum;
		this.word = word;
		this.fileNum = fileNum;
	}
	
	public wordINFO(int fileNum, int sentNum, int tokenNum, String word, String pos , String rawSent, List<Integer> adjList){
		this.fileNum = fileNum;
		this.sentNum = sentNum;
		this.tokenNum = tokenNum;
		this.word = word;
		this.pos = pos;
		this.sentence = rawSent;
		this.adjList = adjList;
	}
	
	public void setPmiScore(double[] a){
		this.pmiScore = a;		
	}
	
	public double[] getPmiScore(){
		return this.pmiScore;
	}
	
	
	public void setSimilairty(double[] a, double[] b, double[] c, double[] d) {
		double[] si =  new double[4];
		
		si[0] = pmiVectorSimilarity(a);
		si[1] = pmiVectorSimilarity(b);
		si[2] = pmiVectorSimilarity(c);
		si[3] = pmiVectorSimilarity(d);		
		
		this.similarity = si;		
	}
	public double getMaxSimilarity(){
		
		double inf = -1000;
		for(int i = 0 ; i < this.similarity.length ; i++)
		{
			if( this.similarity[i] > inf)
				inf = this.similarity[i];
		}
		this.maxSimilarity = inf;
		return inf;

	}
	
	public double[] getSimilarity(){
		return this.similarity;
	}
	
	
	public String toString(){
		String s = sentence + "\n" + 
			   "<file Num, sent Num, token Num, word, pos> : <"+fileNum +"," + sentNum +"," + tokenNum+","+word+","+pos+">" +"\n";
		

		for(int i = 0 ; i < adjList.size() ; i++){
			s = s + " "+adjList.get(i);
		}
		return s;
		
		//this.sentence + "\n" +
	}
	public boolean hasSentiAdj(){
		
		if(this.adjList.size() > 0)
			return true;
		else
			return false;
	}
	
	public void printVector(double[] a){
		
		System.out.print("(");
		for(int i =  0 ; i < a.length ; i++){
			if(i < (a.length-1))
				System.out.print(a[i]+", ");
			else
				System.out.print(a[i]);
		}
		System.out.print(")");
		System.out.println();
		
	}
	
	public double pmiVectorSimilarity(double[] dv){
		
		double a = 0;
		
		for( int i = 0 ; i < pmiScore.length ; i++){
			a += dv[i] * pmiScore[i];	
		}
		
		double b = 0;
		for( int i = 0 ; i < pmiScore.length ; i++){
			b += dv[i] * dv[i];	
		}		
		b = Math.sqrt(b);
		
		double c = 0;
		for( int i = 0 ; i < pmiScore.length ; i++){
			c += pmiScore[i] * pmiScore[i];	
		}	
		c = Math.sqrt(c);
		
		return a/(b*c);
	}
		
}
