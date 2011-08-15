// Adapted from TransferRule.java, written initially by 
// (c) by Greg Hanneman.  Written for 10-701.
// Last modified November 3, 2008.

// Vamshi Ambati


package grammar;

import java.util.Arrays;

import features.FeatureSet;

public class Rule
{	
	public int id=-1;
	public String sLHS;
	public String tLHS;
	
	public String sRHS;
	public String tRHS;

	private String[] sourceRHS;
	private String[] targetRHS;
	
	public int sLength = 0;
	public int tLength = 0;
	
	private int sNTCount=0;
	private int tNTCount=0;
	
	public int sLITCount=0;
	public int tLITCount=0; 
	
    private int[] constitMap;
	private double sgtScore;
	private double tgsScore;
	public int freq;
	
	// Features 
	public FeatureSet feats; 
	
	// Constructors: ////////////////////////////////////////////////////////
	public Rule(String sLHS, String tLHS, String sRHS, String tRHS)
	{
		// Copy left-hand sides directly:
		this.sLHS = sLHS;
		this.tLHS = tLHS;
		this.sRHS = sRHS; 
		this.tRHS = tRHS; 
		
		// Tokenize the right-hand sides:
		this.sourceRHS = sRHS.trim().split("\\s+");
		this.targetRHS = tRHS.trim().split("\\s+");
		
		// We can fill in some features now too, like rank, although rank is
		//already captured by getting the size of the RHSs.
		
		sLength = sourceRHS.length;
		tLength = targetRHS.length;
		
		for(int i=0;i<sourceRHS.length;i++) {
			if(sourceRHS[i].startsWith("\"")){
				sLITCount++;
			}
		}
		
		for(int i=0;i<targetRHS.length;i++) {
			if(targetRHS[i].startsWith("\"")){
				tLITCount++;
			}
		}
		
		setSNTCount(sLength - sLITCount); 
		setTNTCount(tLength - tLITCount); 

		// Init other features:
		setConstitMap(new int[this.targetRHS.length]);
		for(int i = 0; i < getConstitMap().length; i++)
			getConstitMap()[i] = -1;
		sgtScore = 0.0;
		tgsScore = 0.0;
		freq = 0;
		
		// Initialize features 
		feats = new FeatureSet();
	}
	
	public void addFeatureSet(String str){
		feats.initialize(str); 
	}
	
	// Public functions: ////////////////////////////////////////////////////
	
	public String sLHS() { return sLHS; }
	public String tLHS() { return tLHS; }
	
	public String[] sRHS() { return sourceRHS; }
	public String[] tRHS() { return targetRHS; }
	
	public double GetSGTScore() { return sgtScore; }
	public void SetSGTScore(double sgtScore) { this.sgtScore = sgtScore; }
	
	public double GetTGSScore() { return tgsScore; }
	public void SetTGSScore(double tgsScore) { this.tgsScore = tgsScore; }
	
	public int GetFrequency() { return freq; }
	public void SetFrequency(int freq) { this.freq = freq; }
	public void setID(int id) { this.id = id; }
	
	// The consituent map gives the source position of each of the target-side
	// constituents.  For example, for the rules
	//     NP::NP [NNS "de" "l'" NN JJ] -> [JJ NN NNS]
	//     NP::NP [JJ NN NNS] -> [NNS "de" "l'" NN JJ]
	// the entries are
	//     4, 3, 0
	//     2, -1, -1, 1, 0
	// where -1 denotes the case where the constituent has no alignment.
	public int[] GetConstituentMap() { return getConstitMap(); }
	
	public void AddConstituentAlignment(int srcIndex, int tgtIndex)
	{
		if(tgtIndex <= getConstitMap().length) {
			getConstitMap()[tgtIndex-1] = srcIndex - 1;
			
			// Every alignment link corresponds to a non-terminal introduction on both sides
			//sNTCount++; 
			//tNTCount++;
		}
		else{
			System.err.println("Alignment can not exist -"+srcIndex+":"+tgtIndex);
		}
	}
	
	
	// Returns whether the provided source or target right-hand side has any
	// terminals (literals) in it.  Terminals are assumed to be any constituent
	// that is surrounded by quotes.
	public boolean HasTerminal(String[] rhs)
	{
		for(String s : rhs)
		{
			if(IsTerminal(s))
				return true;
		}
		return false;
	}

	// Returns whether the provided source or target right-hand side piece is
	// a terminal (literal) or not.  Terminals are assumed to be any
	// constituent that is surrounded by quotes.
	public boolean IsTerminal(String rhsEntry)
	{
		if(rhsEntry.startsWith("\"") && rhsEntry.endsWith("\"") &&
		   (rhsEntry.length() >= 3))
			return true;
		else
			return false;
	}
	
	
	// Override Object function to provide a way to compare rules:
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof Rule))
			return false;
		
		// Check member variables:
		Rule rule = (Rule)o;
		if((rule.sLHS.equals(sLHS)) &&
		   (rule.tLHS.equals(tLHS)) &&
		   (Arrays.equals(rule.sourceRHS, sourceRHS)) &&
		   (Arrays.equals(rule.targetRHS, targetRHS)) &&
		   (Arrays.equals(rule.getConstitMap(), getConstitMap())) &&
		   (rule.sgtScore == sgtScore) &&
		   (rule.tgsScore == tgsScore) &&
		   (rule.freq == freq))
			return true;
		else
			return false;
	}
	
	// We check to see if the SOURCE side of the RHS is abstract , as that is important during parsing
	public boolean isAbstract()
	{
		if(getSNTCount()==sourceRHS.length){
			return true;
		}
		return false;
	}
	
	// Override Object function to be consistent with TransferRule.equals():
	@Override
	public int hashCode()
	{
		// Add up hash codes of the member variables:
		int result =  sLHS.hashCode();
		result += tLHS.hashCode();
		result += Arrays.hashCode(sourceRHS);
		result += Arrays.hashCode(targetRHS);
		result += Arrays.hashCode(getConstitMap());
		result += (sgtScore * Integer.MAX_VALUE);
		result += (tgsScore * Integer.MAX_VALUE);
		result += freq;
		
		return result;		
	}	
	
	// Print to oneline string format, with all the features 
	public String toOneline()
	{
		String str="";
		str+=id+"\t"+sLHS+"\t"+tLHS+"\t"+sRHS+"\t"+tRHS+"\t";
		 
		// Features
		if(sgtScore!=-1){
			str+=sgtScore+"\t"+tgsScore+"\t"+freq+"\t";
		}
		// Binary features from Hash  
		for(String key: feats.featHash.keySet()){
			String val = feats.getFeatureValue(key);
				str+=val+"\t";
		}
		str+="\n";
		return str; 
	}
	
	public String toString()
	{
		String str="";
		
		str="{"+sLHS+","+id+"}\n";
		str+=sLHS+"::"+tLHS+" ["+sRHS+"] -> ["+tRHS+"]\n";
		str+="(\n";
		// Features
		if(sgtScore!=-1){
			str+="\t(*sgtrule* "+sgtScore+")\n";
			str+="\t(*tgsrule* "+tgsScore+")\n";
			//str+="\t;(freq *"+tgsScore+"*)\n";
		}
		// Alignment 
		for(int i=0;i<getConstitMap().length;i++){
			if(getConstitMap()[i]!=-1)
				str+="\t(X"+(getConstitMap()[i]+1)+"::Y"+(i+1)+")\n";
		}
		str+=")\n";
		return str; 
	}

	public void setConstitMap(int[] constitMap) {
		this.constitMap = constitMap;
	}

	public int[] getConstitMap() {
		return constitMap;
	}

	public void setSNTCount(int sNTCount) {
		this.sNTCount = sNTCount;
	}

	public int getSNTCount() {
		return sNTCount;
	}

	public void setTNTCount(int tNTCount) {
		this.tNTCount = tNTCount;
	}

	public int getTNTCount() {
		return tNTCount;
	}
}

