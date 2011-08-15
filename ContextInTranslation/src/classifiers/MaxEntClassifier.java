package classifiers;

import opennlp.maxent.*;
import opennlp.maxent.io.*;
import java.io.*;

import tasks.gls.GLS;

public class MaxEntClassifier implements Classifier{

	 public static boolean USE_SMOOTHING = true;
	 public static double SMOOTHING_OBSERVATION = 0.1;
	 
	 public static int ITERATION_COUNT = 10;
	 public static int FEATURE_CUTOFF = 0; // Include all
	 
	public class Predict {
	    MaxentModel _model;
	    ContextGenerator _cg = new BasicContextGenerator();

	    public Predict (MaxentModel m) {
	        _model = m;
	    }

	    private void eval (String predicates) {
	        double[] ocs = _model.eval(_cg.getContext(predicates));
	        System.out.println("For context: " + predicates+ "\n" + _model.getAllOutcomes(ocs) + "\n");

	    }
	}
	 
	public void testFile(String modelFileName, String testFileName) throws IOException {
		   GISModel m = new SuffixSensitiveGISModelReader(new File(modelFileName)).getModel();
		   Predict predictor = new Predict(m);
           DataStream ds = new PlainTextByLineDataStream(new FileReader(new File(testFileName)));
           while (ds.hasNext()) {
               String s = (String)ds.nextToken();
               predictor.eval(s.substring(0, s.lastIndexOf(' ')));
           }
	}
	
	public void testEntry(String modelFileName, Entry instance) throws IOException {
		   GISModel m = new SuffixSensitiveGISModelReader(new File(modelFileName)).getModel();
		   Predict predictor = new Predict(m);
		   // Test over the particular instance Entry 
		   // TODO 
	}

	public void train(DataSet ds) throws Exception {
		// Write to directory 
		ds.printToDirectory(GLS.modelDir);
		String dataFileName = ds.filepath;
		String modelFileName = GLS.modelDir+"/"+ds.event+".model";

		//Now train a classifier for this dataPath and store it in modelPath   
	      try {
	    	System.err.println("Datafile:"+dataFileName);
	    	System.err.println("Modelfile:"+modelFileName);
	    	
	        FileReader datafr = new FileReader(new File(dataFileName));
	        EventStream es = new BasicEventStream(new PlainTextByLineDataStream(datafr));
	        GIS.SMOOTHING_OBSERVATION = SMOOTHING_OBSERVATION;
	        // Print msgs while training
	        GISModel model = GIS.trainModel(es,ITERATION_COUNT, FEATURE_CUTOFF,USE_SMOOTHING,true);
	        // Don't Print msgs while training
//	        GISModel model = GIS.trainModel(es,ITERATION_COUNT, FEATURE_CUTOFF,USE_SMOOTHING,false);
	        
	        GISModelWriter writer = new SuffixSensitiveGISModelWriter(model, new File(modelFileName));
	        writer.persist();
	      } catch (Exception e) {
	        System.out.print("Unable to create model due to exception: ");
	        System.out.println(e);
	      }
	}
}
