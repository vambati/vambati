package crowdsource.validate;

/* How do we validate that this is not noise - Account for Human Errors
 * 1. Within ranges of Sentence lengths (source and target)
 * 2. Compare with Gold Standard   
 * */

public class AlignmentValidator implements Validator {
	
	public AlignmentValidator() {
		 
	}
	/*
	 * Score by Meteor 
	 */
	public double score(String hyp,String ref){
		if(isValid(hyp,ref)){
		
		}
		return 0;
	}
	
	/* Is this a valid translation */
	// Perhaps INPUT and OUTPUT need to be abstracted 
	 public boolean isValid(String input, String output){
		 if(output.equals("") && !input.equals("")){ // EMPTY
			 return false;
		 }else if(input.equalsIgnoreCase(output)){ // COPY 
			 return false;
		 }
		 return true;
	 }
	@Override
	public boolean match(String one, String two) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean matchGold(String one, String two) {
		// TODO Auto-generated method stub
		return false;
	}
}
