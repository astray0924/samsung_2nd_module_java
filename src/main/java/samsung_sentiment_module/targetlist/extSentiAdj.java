package samsung_sentiment_module.targetlist;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import samsung_sentiment_module.sentiment.SentimentPipeline;

public class extSentiAdj {
	
	public static String sentiResult(String sent){
		
		
		String sentResult = null;
		try {
			sentResult = SentimentPipeline.getAdjfromSent(sent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sentResult;
	}
	
	public static void xmlToSentiAdj(String xmlString){
		

	}
	

	

}
