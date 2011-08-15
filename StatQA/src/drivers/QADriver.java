package drivers;

import qa.IRIndexer;
import java.io.*;

public class QADriver {
     public static void main(String[] argv) throws Exception {

    	System.err.println("Question Answering System\n");
    	IRIndexer ir = new IRIndexer("c:/testindex");
    	String qfile = "C:/Users/Vamshi Ambati/Desktop/ATT Internship/QA_sample/questions.txt";
    	String afile = "C:/Users/Vamshi Ambati/Desktop/ATT Internship/QA_sample/answers.txt";
    	ir.indexData(qfile,afile);

    	System.err.println("Enter q : \n");
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(inp);

        String querystr = br.readLine(); 
    	ir.retrieveAnswers(querystr);
    	
    	// Expand query by
    	// 1. Compute Mutual info
    	 
    	 // 2. Translation features   
    	 
    	 // Compute other features
    }
}
