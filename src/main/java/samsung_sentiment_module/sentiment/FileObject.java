package samsung_sentiment_module.sentiment;

import java.util.List;
/**
 * 
 * FileObject 객체는 입력된 파일 단위로 감성 분석 결과를 저장하기 위해 정의되었다.
 * 입력된 파일 이름과 해당 파일에 존재하는 문장들 각각에 대해 감성 분석 결과를
 * 저장하기 위해 SentObject형 리스트를 포함하고 있다. 
 * 
 * @author SeungYong
 *
 */
public class FileObject {
	
	private String fileName; 
	static private List<SentObject> se;
	
	FileObject(String fileName, List<SentObject> se){
		this.fileName = fileName;
		this.se = se;
	}
	
	public String getName(){
		return this.fileName;
	}
	
	public List<SentObject> getSentObject(){
		return this.se;
	}

}
