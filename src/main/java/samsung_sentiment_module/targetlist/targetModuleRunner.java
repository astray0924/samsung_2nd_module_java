package samsung_sentiment_module.targetlist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sourceforge.argparse4j.inf.Namespace;
import samsung_sentiment_module.abs.ModuleRunner;
import samsung_sentiment_module.sentiment.SentimentPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * 
 * 의견 대상의 추출을 수행하는 클래스
 * 
 * @author SeungYong
 *
 */
public class targetModuleRunner implements ModuleRunner {
	
	public void run(String[] args, Namespace parsedArgs){
		
		String corpusDirPath = parsedArgs.getString("corpusDirPath");
		String outputDirPath = parsedArgs.getString("outputDirPath");
		String inputFilePath = parsedArgs.getString("inputFilePath");
		String domainFilePath = parsedArgs.getString("domainFilePath");
		String pmiThreshold = parsedArgs.getString("pmiThreshold");
		String coThreshold = parsedArgs.getString("coThreshold");
		
		try {
			target(corpusDirPath, inputFilePath, domainFilePath, outputDirPath, Double.parseDouble(pmiThreshold), Double.parseDouble(coThreshold));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/**
 * 의견 대상 추출을 수행하는 메소드
 * 
 * @param corpusDirPath 상호 도메인 PMI 분석을 위해 사용할 여러 도메인의 상품 리뷰 파일이 저장되어 있는 디렉토리의 절대경로/상대경로를 입력 받는 변수
 * @param inputFilePath 의견 대상을 추출하고자 하는 상품 리뷰 파일의 절대경로/상대경로를 입력 받는 변수
 * @param domainFilePath 도메인 대표 단어 목록 파일의 절대경로/상대경로를 입력 받는 변수
 * @param outputDirPath 감성 분석 결과가 저장될 디렉토리의 절대경로/상대경로를 입력 받는 변수이다
 * @param pmiThreshold 의견 대상 추출 기준이 되는 PMI 점수의 임계값(0~1 사이의 소수값)을 입력 받는 변수
 * @param coThreshold 의견 대상 추출 기준이 되는 공기 점수의 임계값(0~1 사이의 소수값)을 입력 받는 변수
 * @return 의견 대상 문자열(String)을 리스트 형태로 리턴
 * @throws IOException
 */
	public static List<String> target(String corpusDirPath, String inputFilePath, String domainFilePath, String outputDirPath, double pmiThreshold, double coThreshold) throws IOException{
		// TODO Auto-generated method stub

		StanfordCoreNLP pipeline = getPipeline();
		
		String dirName = corpusDirPath;
		List<String> tl = new ArrayList<String>();
		int NumOfDomain = 4; // 총 도메인 수
		
		fileIO io = new fileIO();
		List<File> productFileList = io.addTxtFile(dirName);
		int numOfProduct = productFileList.size();
		String allDoc = "";
		
	    String outputFileName = outputDirPath +"/"+ inputFilePath;
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
						allDoc += TxtReader.readFileToReview(productFileList.get(i).getPath(),domainEntity, i);
				}

				
				String[] Review = allDoc.split("\\[t\\]");

	
				
				ArrayList<wordINFO> nounList = new ArrayList<wordINFO>();
				for(int i = 0; i < productFileList.size() ; i++){
					
					/*
					 *  평가할 테스트 파일 선택
					 * */
					int targetN = -1;
					if( productFileList.get(i).getName().contains(inputFilePath) ){ //domain i 만 성능펴가 하기 위함
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
				
				
				
				StringBuilder sb = new StringBuilder();
				
				//coOccurOpinion.co_occur();
				
				Map<String, String> map = new HashMap<String, String>();
				 
				int totalPrintWords= 0; 
				
				
				StringBuilder jsonResult =  new StringBuilder();
				jsonResult.append("{\"document\":{\"file name\":\"" + inputFilePath+"\",");
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
						if( a.pmiVectorSimilarity(dv[domainToNum.get(targetDomain)]) > pmiThreshold && coOccurOpinion.probOfSenti(a.word) > coThreshold ){  //phone -> target 파일의 도메인을 domain list 번호로 매치해서
							
							jsonResult.append("{\"opinion target\":\"" + a.word + "\"},");							
							totalPrintWords++;			
							sb.append("\n");
							sb.append(a);
							sb.append("\n");

						}
					}
				}
				System.out.println("The anlysis has been done.");
				output.write(SentimentPipeline.jsonBeautifier(sb.toString()));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			output.close();
			return null;
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
