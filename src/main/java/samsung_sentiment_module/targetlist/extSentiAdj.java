package samsung_sentiment_module.targetlist;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import samsung_sentiment_module.sentiment.SentimentPipeline;



public class extSentiAdj {
	
	public extSentiAdj(){

	}
    static enum Output {
	    PENNTREES, VECTORS, ROOT, PROBABILITIES
	}
    static Tree outputLabelTree(PrintStream out, CoreMap sentence, List<Output> outputFormats) {

	    Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
	    
	    Tree copy = tree.deepCopy();
	    setSentimentLabels(copy);
	     
	    return copy;
	        
  }
    static void setSentimentLabels(Tree tree) {
        if (tree.isLeaf()) {
          return;
        }
         
        for (Tree child : tree.children()) {
          setSentimentLabels(child);
        }

        Label label = tree.label();
        if (!(label instanceof CoreLabel)) {
          throw new IllegalArgumentException("Required a tree with CoreLabels");
        }
        CoreLabel cl = (CoreLabel) label;
        cl.setValue(Integer.toString(RNNCoreAnnotations.getPredictedClass(tree)));
      }
	
	public static String sentiResult(String sent, StanfordCoreNLP pipeline){
		
		
		String sentResult = null;
		List<Output> outputFormats = Arrays.asList(new Output[] { Output.PROBABILITIES});
		
		
		String line = sent;
		String result = "";
		if (line.length() > 0) {
		 
		  Annotation annotation = pipeline.process(line);
		  

		  
		  int check = 0;
		  for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
			 Tree t = outputLabelTree(System.out, sentence, outputFormats);		    		 
			 result = t.toString();

		  } 	

		}
		
		sentResult = result;
		return sentResult;
	}
	
	public static void xmlToSentiAdj(String xmlString){
		

	}
	

	

}
