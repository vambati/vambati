package drivers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import taruDecoder.TaruTrainingWrapper;
import ml.online.Perceptron;

public class PerceptronDriver {
	public static void main(String[] args) {
		
		// Create a perceptron on top of a Taru decoder
		//Perceptron p = new Perceptron(new TaruTrainingWrapper());
		Perceptron p = new Perceptron(new TaruTrainingWrapper(args[0],null));
		
		// Training data
//		String srcTrainFile = "train.src"; // contains parses
//		String refTrainFile = "train.ref"; // contains plain text
		String srcTrainFile = "test.src"; // contains parses
		String refTrainFile = "test.ref"; // contains plain text
		
		// Read in the training data
		ArrayList<String> srcData = new ArrayList<String>(5000);
		ArrayList<String> refData = new ArrayList<String>(5000);
		
		try{
			BufferedReader sbr = new BufferedReader(new FileReader(srcTrainFile));
			BufferedReader rbr = new BufferedReader(new FileReader(refTrainFile));
			String ref = "";
			while((ref = rbr.readLine()) != null){
				String src = sbr.readLine();
				if(ref.split(" +").length < 31){
					srcData.add(src);
					refData.add(ref.toLowerCase());
				}
				
			}
			sbr.close();
			rbr.close();
		}catch(FileNotFoundException e){e.printStackTrace();} catch (IOException e) { e.printStackTrace();}
		System.out.println("Training on " + srcData.size() + " sentences.");
		// run the training
		p.trainAverage(srcData, refData, 5);
	}
}
