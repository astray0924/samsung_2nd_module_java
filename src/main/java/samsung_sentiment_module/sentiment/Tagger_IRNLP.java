package samsung_sentiment_module.sentiment;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.tagger.maxent.TaggerConfig;

/**
 * POS tagging을 위한 클래스
 * 
 * @author SeungYong
 *
 */

public class Tagger_IRNLP {
	
	static MaxentTagger tagger;
	
	/**
	 * 기본 생성자
	 * pos 모델을 로딩함
	 */
	public Tagger_IRNLP(){
		String model = "english-left3words-distsim.tagger";
		TaggerConfig config = new TaggerConfig("-outputFormat","xml",
				"-model", model);

		tagger = new MaxentTagger(model, config);
		
	}
	
	/**
	 * 입력 파일의 경로를 받아서 텍스트를 읽은 후 pos 태깅된 문자열을 반환
	 * @param inputFile
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static String tagger(String inputFile) throws IOException,
			ClassNotFoundException {


		String raw = TxtReader.readFile(inputFile);  // 각각의 file path
		String tagged = tagger.tagString(raw);
		return tagged;

	}
	public static String tagger2(String inputFile) throws IOException,
	ClassNotFoundException {


	String raw = TxtReader.readFile(inputFile);  // 각각의 file path
	//System.out.println(tagged);
	return raw;
	
	}	

	public static void writeFile(String outputString, String outputFileName) {
		try {

			BufferedWriter out = new BufferedWriter(new FileWriter(
					outputFileName));
			out.write(outputString);
			out.newLine();
			out.close();

		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
	}
	

}
