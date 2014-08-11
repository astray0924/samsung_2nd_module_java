package samsung_sentiment_module.targetlist;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.tagger.maxent.TaggerConfig;



class Tagger_IRNLP {
	
	static MaxentTagger tagger;
	
	protected Tagger_IRNLP(){
		String model = "english-left3words-distsim.tagger";
		TaggerConfig config = new TaggerConfig("-outputFormat","xml",
				"-model", model);

		tagger = new MaxentTagger(model, config);
		
	}
	
	protected static String tagger(String inputFile, int fileNum, ArrayList<wordINFO> nounList, StanfordCoreNLP pipeline) throws IOException {

		String raw = TxtReader.readFile(inputFile);  // 각각의 file path

		// 문장 태깅 
		String tagged = tagger.tagString(raw);
		tagged = setRootTag(tagged);	
		// 명사 추출 
		readXML(tagged, nounList, fileNum , pipeline);
	
		return tagged;

	}
	protected static String setRootTag(String st){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<review>");
		stringBuilder.append(st);
		stringBuilder.append("</review>");
		return stringBuilder.toString();
		
	}
	
	protected static void readXML(String xmlString, ArrayList<wordINFO> nounList ,int fileNum,StanfordCoreNLP pipeline ){
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			// convert String into InputStream
			InputStream is = new ByteArrayInputStream(xmlString.getBytes());
			Document doc = dBuilder.parse(is);
			
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("sentence");
			
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				 
				Node nNode = nList.item(temp);	 
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) { 
					Element eElement = (Element) nNode;	 			
					int sentNum = Integer.parseInt(eElement.getAttribute("id"));
					
					NodeList words =  eElement.getElementsByTagName("word");
					int num= words.getLength();
					
					String rawSent = newLineToSpace(eElement.getTextContent());
					
					List<Integer> adjList = getSentiAdj(words, rawSent, pipeline);
					
					for( int i = 0; i < num ; i++){
						Element node = (Element) words.item(i);
						listAllAttributes(node, nounList , fileNum, sentNum , rawSent, adjList);
					}

				}
			}			
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	protected static List<Integer> getSentiAdj(NodeList words, String rawSent,StanfordCoreNLP pipeline){
		
	    NamedNodeMap attributes;
	    // sentiment 모듈에 의한 트리 결과 얻기
	    extSentiAdj esa = new extSentiAdj();
        String sentiTree = 	esa.sentiResult(rawSent,pipeline);
        List<Integer> sentiPosition = new ArrayList<Integer>();
        String savedSentiTree = sentiTree;
        
        if(sentiTree.length() > 6){
		    for( int i = 0 ; i < words.getLength() ; i++){
				Element node = (Element) words.item(i);
				attributes = node.getAttributes();
	            Attr attr = (Attr) attributes.item(0);
	            String posTag = attr.getNodeValue();
	            attr = (Attr) attributes.item(1);
	            int tokenNumber = Integer.parseInt(attr.getNodeValue());
	            String word = node.getTextContent();
	            
	            // 형용사나 동사일 경우 트리 결과에서 sentiment를 갖고 있는지 확인 
	            if( posTag.contains("JJ") || posTag.contains("VB") ){
	            	
	            	char s = 0;
	            //	System.out.println(sentiTree);
	            //	System.out.println(word);
	            	if(sentiTree.contains(word)){
		            	while(sentiTree.charAt(sentiTree.indexOf(word+")")-3) != '('){
		            		
		            		sentiTree = sentiTree.substring(sentiTree.indexOf(word+")")+1);
		            		
		            	}
		        		s = sentiTree.charAt(sentiTree.indexOf(word+")")-2);	            	
		            		
		            	int polarity = Integer.parseInt(Character.toString(s));
		            	
		            	if (polarity != 2){ // neutral 은 감성 단어로 생각 X
		            		sentiPosition.add(tokenNumber);
		            	}
	            	}
	            }
	            sentiTree = savedSentiTree;
			}
        }
		return sentiPosition;
		
	}
	
	
	protected static void listAllAttributes(Element element, ArrayList<wordINFO> nounList ,int fileNum,int sentNum, String rawSent, List<Integer> adjList){
 //       System.out.println("List attributes for node: " + element.getNodeName());

        NamedNodeMap attributes = element.getAttributes();
// item 0 번이 pos, 1번이 position(token Number)
            Attr attr = (Attr) attributes.item(0);
            String posTag = attr.getNodeValue();
            attr = (Attr) attributes.item(1);
            int tokenNumber = Integer.parseInt(attr.getNodeValue());
            String word = element.getTextContent();

            
            /*
             * 명사만 추출 ( 일단 pos에 NN을 포함하는 단어만 )
             * */
           
            if( posTag.contains("NN") && !StopWordFilter.mapa.containsKey(word)){
            	nounList.add(new wordINFO(fileNum, sentNum, tokenNumber, word, posTag , rawSent, adjList));
            }
            
	}
	
	
	private static String newLineToSpace(String sent){
		
		sent = sent.replaceAll("\n"," ");
		
		return sent;
	}
	

	protected static HashMap<Integer, String[]> stringToMap(String input) {
		String[] token1 = deli(input, " ");
		HashMap<Integer, String[]> map = new HashMap<Integer, String[]>();

		for (int i = 0; i < token1.length; i++) {
			map.put(i, deli(token1[i], "_"));
		}

		return map;
	}

	protected static String[] deli(String f_name, String de) {
		StringTokenizer st = new StringTokenizer(f_name, de);
		String arr[] = new String[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens()) {
			arr[i] = st.nextToken();
			i++;
		}
		return arr;

	}

}
