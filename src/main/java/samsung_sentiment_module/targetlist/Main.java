package samsung_sentiment_module.targetlist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
		
		target("reviewdata", "result", "camera_Canon_G3(test)" ,"domainFile.txt" ,0.506, 0.80);
	}

	public static void target(String inputDirPath, String outputDirPath, String productFileName, String domainFilePath, double pmi, double co_occurrence) throws IOException{
		// TODO Auto-generated method stub

		StanfordCoreNLP pipeline = getPipeline();
		
		String dirName = inputDirPath;

		int NumOfDomain = 4; // 총 도메인 수
		
		fileIO io = new fileIO();
		List<File> productFileList = io.addTxtFile(dirName);
		int numOfProduct = productFileList.size();
		String allDoc = "";
		
	    String outputFileName = outputDirPath +"/"+ productFileName;
		BufferedWriter output = new BufferedWriter(new FileWriter(outputFileName)); //output directory	

		String domainEntity[] = new String[numOfProduct];
		String domainList[]  = readDomainFile(domainFilePath);
		NumOfDomain = domainList.length;
		String targetDomain = "";
		
		double[][] dv = new double[NumOfDomain][];
		
		for(int i = 0 ; i < NumOfDomain; i++){
			dv[i] = new double[numOfProduct];
			
		}
		//matching domain string to number 
		HashMap<String, Integer> domainToNum = new HashMap<String, Integer>();
		for( int i = 0 ; i < NumOfDomain; i++){
			if(!domainToNum.containsKey(domainList[i]))
				domainToNum.put(domainList[i], i);
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
					//System.out.println(domainEntity[i]);
				}

				
				String[] Review = allDoc.split("\\[t\\]");

	
				
				ArrayList<wordINFO> nounList = new ArrayList<wordINFO>();
				for(int i = 0; i < productFileList.size() ; i++){
					
					/*
					 *  평가할 테스트 파일 선택
					 * */
					int targetN = -1;
					if( productFileList.get(i).getName().contains(productFileName) ){ //domain i 만 성능펴가 하기 위함
						Tagger_IRNLP irnlp = new Tagger_IRNLP();
						irnlp.tagger(productFileList.get(i).getPath(), i, nounList, pipeline); 
						targetN = i;			
					}
					
					text[i] = TxtReader.readFile2(productFileList.get(i).getPath());

					for(int j = 0 ; j < NumOfDomain; j++)
						dv[j][i] = computePMI.pmiScore(text[i], domainList[j], domainEntity[i], Review);
					
					if(targetN == i	)
						targetDomain = domainEntity[i];
				}		
				coOccurOpinion.txtToSenti(text, pipeline);
				
				System.out.println("input target domain : "+ targetDomain);
							
//				phone[3] = 0.4;
//				phone[6] += 2;
//				phone[7] += 2;
				
				
				
				
				StringBuilder sb = new StringBuilder();
				
				//coOccurOpinion.co_occur();
				
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
						a.setSimilairty(dv);
						
						// 1 평가시 도메인 단어 바꿔야됨
						if( a.pmiVectorSimilarity(dv[domainToNum.get(targetDomain)]) > pmi && coOccurOpinion.probOfSenti(a.word) > co_occurrence ){  //phone -> target 파일의 도메인을 domain list 번호로 매치해서
							
							jsonResult.append("{\"opinion target\":\"" + a.word + "\"},");
							
							totalPrintWords++;
							
							sb.append("\n");
							sb.append(a);
							sb.append("\n");
							
							// 뽑혀진 단어마다 스코어 체크용
							//sb.append(a.pmiVectorSimilarity(dv[domainToNum.get(targetDomain)])+"  " + coOccurOpinion.probOfSenti(a.word));
							sb.append("\n");
							sb.append("\n");
						}
					}
				}

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
	  
	private static String[] readDomainFile(String domainFilePath) throws IOException{
		
		int numOfDomain =0;
		
		BufferedReader reader = new BufferedReader(new FileReader(domainFilePath));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		line = reader.readLine();
		
		numOfDomain = Integer.parseInt(line.trim());
		String[] domainList = new String[numOfDomain];
		for(int i = 0 ; i < numOfDomain ; i++){
			domainList[i] = reader.readLine();
		}
		return domainList;
		
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
