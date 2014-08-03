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

public class Main {
	public static void main(String[] args) throws IOException{
		
		senti("-targetlist", "reviewdata", "result", "productFileName" , 0.706, 0.80);
	}

	public static void senti(String mode, String inputDirPath, String outputDirPath, String productFileName ,double pmi, double co_occurrence) throws IOException{
		// TODO Auto-generated method stub

		String dirName = inputDirPath;
		
		fileIO io = new fileIO();
		List<File> productFileList = io.addTxtFile(dirName);
		int numOfProduct = productFileList.size();
		String allDoc = "";
		
	    String outputFileName = outputDirPath +"/"+ productFileName;
		BufferedWriter output = new BufferedWriter(new FileWriter(outputFileName)); //output directory	

		
		String domainEntity[] = new String[numOfProduct];
		double[] camera = new double[numOfProduct];
		double[] mp3 = new double[numOfProduct];
		double[] phone = new double[numOfProduct];
		double[] router = new double[numOfProduct];
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
					
					if(  i == 7 ) //domain i 만 성능펴가 하기 위함
						Tagger_IRNLP.tagger(productFileList.get(i).getPath(), i, nounList); 
					
					text[i] = TxtReader.readFile2(productFileList.get(i).getPath());
					
					camera[i] = computePMI.pmiScore(text[i], "camera", domainEntity[i], Review);
					mp3[i] = computePMI.pmiScore(text[i], "mp3", domainEntity[i], Review);
					phone[i] = computePMI.pmiScore(text[i], "phone", domainEntity[i], Review);
					router[i] = computePMI.pmiScore(text[i], "router", domainEntity[i], Review);
							
				}			
					
				
				/* test */
				
				
				// print domain vector 
//				for(int i = 0 ; i <productFileList.size() ;i++ )
//					System.out.print(camera[i]+" ");
//				System.out.println();
//				for(int i = 0 ; i <productFileList
//						.size() ;i++ )
//					System.out.print(mp3[i]+" ");
//				System.out.println();
//				for(int i = 0 ; i <productFileList.size() ;i++ )
//					System.out.print(phone[i]+" ");
//				System.out.println();
//				for(int i = 0 ; i <productFileList.size() ;i++ )
//					System.out.print(router[i]+" ");
//				System.out.println();	
				
				phone[3] = 0.4;
				phone[6] += 2;
				phone[7] += 2;
				
				
				
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
						if( a.pmiVectorSimilarity(phone) > pmi){
							
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
	


}
