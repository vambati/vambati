package crowdsource.validate;

public interface Validator {

	// Score an output against a crowd output
	public double score(String hyp,String ref);
	
	// Match an output against a crowd output
	 public boolean match(String one, String two);
	 public boolean matchGold(String one, String two);
	 
	/* Is the crowd data valid ?*/
	 public boolean isValid(String input, String output);
}
