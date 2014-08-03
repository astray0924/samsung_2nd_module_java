package samsung_sentiment_module.targetlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TxtReader {

	public static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		line = reader.readLine();
		
// 두문장 테스트위해 ,나중에 두번반복 삭제	
		
		int i = 0; 
		while ((line = reader.readLine()) != null) {
			if( line.contains("[t]"))  // 리뷰 경계를 나타냄 
				continue;
			if( line.length() < 5)
				continue;
		
			line = rmNumber(line);
			
			stringBuilder.append(line);
			stringBuilder.append(ls);

		}
		reader.close();
		return stringBuilder.toString();

	}
	public static String readFile2(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		line = reader.readLine();
		
// 두문장 테스트위해 ,나중에 두번반복 삭제	
		
		int i = 0; 
		while ((line = reader.readLine()) != null) {
			if( line.contains("[t]"))  // 리뷰 경계를 나타냄 
				continue;
			if( line.length() < 5)
				continue;
		
			line = rmStopNumber(line);
			
			stringBuilder.append(line);
			stringBuilder.append(ls);

		}
		reader.close();
		return stringBuilder.toString();

	}
	
	public static String readFileToReview(String file,String[] domainEntity,int a) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		//line = reader.readLine();
		
// 두문장 테스트위해 ,나중에 두번반복 삭제	
		
		int i = 0; 
		
		domainEntity[a] = reader.readLine();
		
		while ((line = reader.readLine()) != null) {

			if( line.length() < 5 && !line.contains("[t]"))
				continue;
			
			if( !line.contains("[t]"))
				line = rmStopNumber(line);
			else
				line= "[t]";  // 제목 삭제
			
			stringBuilder.append(line);
			stringBuilder.append(ls);

		}
		reader.close();
		return stringBuilder.toString();

	}
	
	
	
	// opnion word 뽑아 낼 때 사용
	public static String readFile(String file, int tempNum) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		line = reader.readLine();
		
// 두문장 테스트위해 ,나중에 두번반복 삭제	
		
		int i = 0; 
		while ((line = reader.readLine()) != null) {
			if( line.contains("[t]"))  // 아마 리뷰 경계를 나타냄 
				continue;
			if( line.length() < 5)
				continue;
		
			line = rmStopNumber(line);
			
			/* 임시 결과 뽑기*/
			line = extSentiAdj.sentiResult(line);
			stringBuilder.append(line);
			stringBuilder.append(ls);
			
			
			/*
			stringBuilder.append(line);
			stringBuilder.append(ls);

			 */
		}
		reader.close();
		fileIO.writeFile(stringBuilder.toString(), "file"+Integer.toString(tempNum));
		return stringBuilder.toString();

	}
	
	// 타겟과 #을 지우고 원래의 문장만 남기기
	
	private static String rmNumber(String s){
		
		s = s.substring(s.indexOf('#')+2);
		return s;
							
	}
	private static String rmStopNumber(String s){
		
		StringBuilder stringBuilder = new StringBuilder();
		
		s = s.substring(s.indexOf('#')+2);
		String text[] = s.split(" ");
		for( int i = 0 ; i < text.length ; i++){
			if(!StopWordFilter.mapa.containsKey(text[i])){
				stringBuilder.append(text[i]);
				stringBuilder.append(" ");
			}
			
		}
		return stringBuilder.toString();
							
	}
}
