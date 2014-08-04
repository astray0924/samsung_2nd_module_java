package samsung_sentiment_module.targetlist;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Main {
	public static void main(String[] args) throws IOException{
		
		senti("-targetlist", "reviewdata", "result", "camera_Canon_G3(test)" , 0.706, 0.80);
	}

	public static void senti(String mode, String inputDirPath, String outputDirPath, String productFileName ,double pmi, double co_occurrence) throws IOException{
		// TODO Auto-generated method stub

		StanfordCoreNLP pipeline = getPipeline();
		
		String dirName = inputDirPath;

		int NumOfDomain = 4;
		
		fileIO io = new fileIO();
		List<File> productFileList = io.addTxtFile(dirName);
		int numOfProduct = productFileList.size();
		String allDoc = "";
		
	    String outputFileName = outputDirPath +"/"+ productFileName;
		BufferedWriter output = new BufferedWriter(new FileWriter(outputFileName)); //output directory	

		
		String domainEntity[] = new String[numOfProduct];
		String domainList[]  = {"camera","mp3","phone","router"};
		double[][] dv = new double[NumOfDomain][];
		
		for(int i = 0 ; i < NumOfDomain; i++){
			dv[i] = new double[numOfProduct];
			
		}
		
		double[] temp  = new double[numOfProduct];

		
		// stop word 리스트 맵 함수 불러오기
		StopWordFilter.runStopWord();
		/*
		 *  
		 */
			String text[] = new String[productFileList.size()];
			try {
				
				//review is seperated for tfidf
				for(int i = 0; i < productFileList.size() ; i++)
				{
					//if( i != 4 ) // SD500과 Ipod 는 리뷰 구분이 안되있는 문서라서 일단 배제
						allDoc += TxtReader.readFileToReview(productFileList.get(i).getPath(),domainEntity, i);
					System.out.println(domainEntity[i]);
				}
				//System.exit(0);

				
				String[] Review = allDoc.split("\\[t\\]");

				

				
				
				ArrayList<wordINFO> nounList = new ArrayList<wordINFO>();
				for(int i = 0; i < productFileList.size() ; i++){
					
					/*
					 * 
					 *  평가할 테스트 파일 선택
					 * 
					 * */
					
					if( productFileList.get(i).getName().contains(productFileName) ){ //domain i 만 성능펴가 하기 위함
						Tagger_IRNLP irnlp = new Tagger_IRNLP();
						irnlp.tagger(productFileList.get(i).getPath(), i, nounList, pipeline); 
						
					}
					
					text[i] = TxtReader.readFile2(productFileList.get(i).getPath());
					
					for(int j = 0 ; j < NumOfDomain; j++)
						dv[j][i] = computePMI.pmiScore(text[i], domainList[j], domainEntity[i], Review);
					
				}			
							
//				phone[3] = 0.4;
//				phone[6] += 2;
//				phone[7] += 2;
				
				
				
				StringBuilder sb = new StringBuilder();
				
				coOccurOpinion.co_occur();
				
				Map<String, String> map = new HashMap<String, String>();
				 
				int totalPrintWords= 0; 
				
				
				StringBuilder jsonResult =  new StringBuilder();
				jsonResult.append("{\"document\":{\"file name\":\"" + productFileName+"\",");
				jsonResult.append("\"target_list\":[");
				
				for(int i = 0 ; i < nounList.size(); i++){
					wordINFO a = nounList.get(i);
					
					if(!map.containsKey(a.word)){
						map.put(a.word, "m");

						for(int j = 0 ; j < productFileList.size() ; j++){
							temp[j] = computePMI.pmiScore( text[j], a.word, domainEntity[j], Review);
							
						}
						// 카메라 벡터와만 비교 (카메라 리뷰만 성능 평가 시)

						a.setPmiScore(temp.clone());
						a.setSimilairty(camera,mp3,phone,router);
						
						// 1 평가시 도메인 단어 바꿔야됨
						if( a.pmiVectorSimilarity(phone) > pmi){  //phone -> target 파일의 도메인을 domain list 번호로 매치해서
							
							jsonResult.append("{\"opinion target\":\"" + a.word + "\"},");
							
//							System.out.println(a);
//							a.printVector(a.getPmiScore());
//							System.out.println("similarity is : " + a.pmiVectorSimilarity(camera) );
//							System.out.println();
							totalPrintWords++;
							
							sb.append("\n");
							sb.append(a);
							sb.append("\n");
							
							// 2 평가시 도메인 단어 바꿔야됨
							sb.append(a.pmiVectorSimilarity(phone)+"  " + coOccurOpinion.probOfSenti(a.word));
							//System.out.println(coOccurOpinion.probOfSenti(":: probOfSenti :: " + a.word));
							sb.append("\n");
							sb.append("\n");
						}
					}
				}
				//System.out.println(jsonResult.substring( 0, jsonResult.length()-1));

				System.out.println(jsonResult.substring( 0, jsonResult.length()-1)+"]}}");
				System.out.println(totalPrintWords);
				output.write(sb.toString());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			output.close();
	}
  static enum Output {
	    PENNTREES, VECTORS, ROOT, PROBABILITIES
	  }

	  static enum Input {
	    TEXT, TREES
	  }
	private static StanfordCoreNLP getPipeline(){
		
		
	    boolean stdin = false;
	    List<Output> outputFormats = Arrays.asList(new Output[] { Output.PROBABILITIES});
	    Input inputFormat = Input.TEXT;


	    Properties props = new Properties();

	    if (stdin) {
	      props.setProperty("ssplit.eolonly", "true");
	    }
	    if (inputFormat == Input.TREES) {
	      props.setProperty("annotators", "sentiment");
	      props.setProperty("enforceRequirements", "false");
	    } else {
	      props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
	    }
	    
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    
	    return pipeline;
	}
	


}
