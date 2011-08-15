package crowdsource.validate;


public class MTEvalValidator implements Validator {

	// Using automatic metrics to do the matching (METEOR)
	double THRESH = 1; 
	
	public MTEvalValidator() {

	}

	// Match two strings (Ratings on the scale of 1-5)
	 public boolean match(String one, String two){
		 if(isValid("",one) && isValid("",two)){
			 
			 if(one.equalsIgnoreCase(two)){
				 return true;
			 }else{
				 int s1 = Integer.parseInt(one);
				 int s2 = Integer.parseInt(two);
				 if(Math.abs(s1-s2)<= THRESH){
					 return true; 
				 }
			 }
		 }
		 return false;
	 }
	 
	
	/* Is this a valid score: (Ratings on the scale of 1-5)*/
	 public boolean isValid(String input, String output){

		 if(output.equals("")){
			 return false; 
		 }
		int rating = Integer.parseInt(output);
		if(rating<=5 && rating>=1)
			return true;
		else{
			System.err.println("Wrong annotation:"+output);
			return false;
		}
	 }

	@Override
	public double score(String hyp, String ref) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean matchGold(String one, String two) {
		// TODO Auto-generated method stub
		return false;
	}
}
