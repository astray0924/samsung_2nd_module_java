package samsung_sentiment_module.sentiment;
/**
 * 
 * WordObject 클래스는 단어 단위의 감성 분석 결과를 저장하기 위해 정의되었다.
 * 단어 단위의 감성 분석 결과로 의견 단어(opinion word), 의견 단어의 대상이 되는
 * 의견 대상(opinion target), 해당 의견 단어의 극성(polarity) 세가지가 있다. 
 * 
 * @author SeungYong
 *
 */
public class WordObject{
	String opinionWord;
	String opinionTarget;
	String polarity;
	
	/**
	 * 기본 생성자
	 * @param opinionWord
	 * @param opinionTarget
	 * @param polarity
	 */
	public WordObject(String opinionWord, String opinionTarget, String polarity) {
		this.opinionWord = opinionWord;
		this.opinionTarget = opinionTarget;
		this.polarity = polarity;
	}
	/**
	 * 의견 단어(opinion word) 문자열을 리턴
	 * @return String
	 */
	public String getWord(){
		return this.opinionWord;
	}
	/**
	 * 의견 대상(opinion target) 문자열을 리턴
	 * @return String
	 */
	public String getTarget(){
		return this.opinionTarget;
	}
	/**
	 * 의견 단어의 극성(polarity)를 리턴
	 * -fineGrained 옵션 일 경우 {Strong positive, positive, neutral, negative, strong negative} 중 하나를 리턴
	 * 아닐 경우 {positive, neutral, strong} 세가지중 하나를 리턴
	 * @return String
	 */
	public String getPolarity(){
		return this.polarity;
	}
}
