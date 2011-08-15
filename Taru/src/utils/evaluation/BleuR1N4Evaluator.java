/* Copyright (c) 2006-2007, Marian Olteanu <marian_DOT_olteanu_AT_gmail_DOT_com>
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this list
 of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice, this
 list of conditions and the following disclaimer in the documentation and/or
 other materials provided with the distribution.
 - Neither the name of the University of Texas at Dallas nor the names of its
 contributors may be used to endorse or promote products derived from this
 software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package utils.evaluation;

public class BleuR1N4Evaluator 
{
	public boolean printDebug = false;
	// Performance: 200 sentences: ~ 100k evaluations/sec
	public double getScore(BleuSentenceErrorStatistics[] errorStatistics)
	{
		int lengthReference = 0, lengthTranslation = 0;
		int countGood[] = new int[4];
		int countTotal[] = new int[4];
		
		for (int i = 0; i < errorStatistics.length; i++)
		{
			BleuSentenceErrorStatistics e = (BleuSentenceErrorStatistics)errorStatistics[i];
			
			lengthReference += e.refLength;
			lengthTranslation += e.tranLength;
			countGood[0] += e.countGood[0];
			countGood[1] += e.countGood[1];
			countGood[2] += e.countGood[2];
			countGood[3] += e.countGood[3];
			countTotal[0] += e.countTotal[0];
			countTotal[1] += e.countTotal[1];
			countTotal[2] += e.countTotal[2];
			countTotal[3] += e.countTotal[3];
		}
		
		double brevity = 1;
		
		if (lengthTranslation < lengthReference)
			brevity = Math.exp(1 - lengthReference * 1d / lengthTranslation);
		
		// fix to accomodate 0-counts
		double sum = 0;
		for (int i = 0; i < countGood.length; i++)
		if( countGood[i] > 0 )
				sum += Math.log(((double)countGood[i]) / countTotal[i]);
		double bleu = brevity * Math.exp(sum / 4);

		// old way: assume no count is 0
//		double bleu = brevity * Math.exp((Math.log(((double)countGood[0]) / countTotal[0])
//										 + Math.log(((double)countGood[1]) / countTotal[1])
//										 + Math.log(((double)countGood[2]) / countTotal[2])
//										 + Math.log(((double)countGood[3]) / countTotal[3])) / 4);
		
		//BLEU for (3946/3124  1623/3124 425/2924 142/2724 49/2524) = 0.0718668101284349
		if (printDebug)
			System.out.println("Bleu: " + bleu + " (" + lengthReference + "/" + lengthTranslation + " "
							   + countGood[0] + "/" + countTotal[0] + " "
							   + countGood[1] + "/" + countTotal[1] + " "
							   + countGood[2] + "/" + countTotal[2] + " "
							   + countGood[3] + "/" + countTotal[3] + ";"
							   + " BP = " + brevity + ")");
		
		return bleu;
	}
	
	public double getSentenceLevelSmoothBleu(BleuSentenceErrorStatistics e)
	{
		
		int lengthReference = 0, lengthTranslation = 0;
//		int countGood[] = new int[4];
//		int countTotal[] = new int[4];
				
		lengthReference += e.refLength;
		lengthTranslation += e.tranLength;
		
		double logBrevity = 0;
		
		if (lengthTranslation < lengthReference)
			logBrevity = 1 - lengthReference * 1d / lengthTranslation;
		
		double sum = 0;
		
		double [] prec = {-1,-1,-1,-1};
		
		for (int i = 0; i < e.countGood.length; i++){
			if( e.countGood[i] > 0 )
				prec[i] = Math.log(((double)e.countGood[i]) / e.countTotal[i]);
		}
		for(int i = 3; i >= 0; i--){
			if(prec[i] == -1){
				sum += -20;
				continue;
			}
			double psum = 0;
			for(int j = i; j >= 0; j--){	
				psum += prec[j];
			}
			sum += (psum / (i+1)) + logBrevity - 4 + i;
		}

//		System.err.println(" (" + lengthReference + "/" + lengthTranslation + " "
//				   + e.countGood[0] + "/" + e.countTotal[0] + " "
//				   + e.countGood[1] + "/" + e.countTotal[1] + " "
//				   + e.countGood[2] + "/" + e.countTotal[2] + " "
//				   + e.countGood[3] + "/" + e.countTotal[3] + ";"
//				   + " BP = " + logBrevity + ") " + sum);

		return sum;
	}
	
	// performance: ref[0].len=23 transl.len=17, 100k in ~ 5.8 sec
	// if replaced with intern(), time down to 4.3 sec
	// if input is already intern()-alized, time down to 5.5 sec
	public BleuSentenceErrorStatistics getErrorStatistics(Item[] ref, Item test)
	{
		// mono-ref now
		assert ref.length == 1;
		int rID = 0;
		
		String[] translation = ((ESentence)test).value;
		String[] reference = ((ESentence)ref[rID]).value;
		
		BleuSentenceErrorStatistics e = new BleuSentenceErrorStatistics(reference.length , translation.length , 4);
		boolean refUsed[][] = new boolean[reference.length][4];
		
		for (int len = 1; len <= 4; len++)
			for (int i = 0; i + len <= translation.length; i++)
			{
				e.countTotal[len - 1]++;
				// check if it's good
				boolean found = false;
				x:
				for (int j = 0; j + len <= reference.length && !found; j++)
					if (!refUsed[j][len - 1])
					{
						for (int k = 0; k < len; k++)
							if (!translation[i + k].equals(reference[j + k]))
								continue x;
						
						found = true;
						refUsed[j][len - 1] = true;
					}
				if (found)
					e.countGood[len - 1]++;
			}
		
		return e;
	}
	public final boolean bestIsSmall()
	{
		return false;
	}
	
	
}
