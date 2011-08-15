package classifiers;

public interface Classifier {

	public void train(DataSet ds) throws Exception;
	public void testFile(String modelPath, String testFile) throws Exception;	
}
