package samsung_sentiment_module.sentiment;

import java.util.List;

/**
 * 
 *  SentObject는 감성 분석의 결과를 문장 단위로 저장하기 위해 정의된 객체이다.
 *  문장 단위의 결과로 입력된 본래 문장(original sentence), 해당 문장의 극성(sentence polarity),
 *  해당 문장의 단어 단위의 감성 분석 결과들을 저장하기위해 WordObject형 리스트가 존재한다.
 *  
 * 
 * @author SeungYong
 *
 */
public class SentObject{
	private String original_sent;
	private String sent_polarity;
	private List<WordObject> w;
	/**
	 * 기본 생성자
	 * @param original_sent
	 * @param sent_polarity
	 * @param w
	 */
	public SentObject(String original_sent, String sent_polarity, List<WordObject> w ) {
		this.original_sent = original_sent;
		this.sent_polarity = sent_polarity;
		this.w = w;			
	}
	/**
	 * 본래의 문장 문자열을 리턴하는 메소드
	 * @return String
	 */
	public String getSentence(){
		return this.original_sent;
	}
	/**
	 * 문장의 극성을 리턴하는 메소드
	 * @return String
	 */
	
	public String getSentPolarity(){
		return this.sent_polarity;
	}
	/**
	 * 문장에서 단어 단위의 감성 분석 결과를 저장하고 있는 WordObject형 리스트를 리턴
	 * @return List<WordObject>
	 */
	public List<WordObject> getWordObject(){
		return this.w;
	}
	
	
}
