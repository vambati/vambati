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
package utils.lm;
import java.util.*;

import utils.lm.wordsequence.*;

public class LMTools
{
	static double minLogProbability = -15;
	static double oovLogProbability = -15;
	
	public static double getSentenceLogProbability(String sentence , NgramLanguageModel lm, int startIndex)
	{
		StringTokenizer st = new StringTokenizer(sentence);
		String[] s = new String[ st.countTokens() ];
		for (int i = 0; i < s.length; i++)
			s[i] = st.nextToken();
//		return getSentenceLogProbability(s , null , lm , oovLogProbability , minLogProbability);
		return getSegmentLogProbability(s , null , lm, startIndex);
	}
	
	
	public static double getSentenceLogProbability(String[] sentence , int[] sentenceId , NgramLanguageModel lm , double oovLogProbability , double minLogProbability)
	{
		assert sentenceId == null || sentence.length == sentenceId.length;
		String[] xSentence = new String[sentence.length + 2];
		xSentence[0] = LMConstants.START_OF_SENTENCE;
		xSentence[sentence.length + 1] = LMConstants.END_OF_SENTENCE;
		System.arraycopy(sentence , 0 , xSentence , 1 , sentence.length);
		int[] xSentenceID = null;
		if (lm.isVocabularyBasedLM())
		{
			if (sentenceId == null)
			{
				// make it
				sentenceId = new int[sentence.length];
				for (int i = 0; i < sentence.length; i++)
					sentenceId[i] = lm.getVocabulary().get(sentence[i]);
			}
			xSentenceID = new int[sentenceId.length + 2];
			
			xSentenceID[0] = lm.getVocabulary().get(LMConstants.START_OF_SENTENCE);
			xSentenceID[sentence.length + 1] = lm.getVocabulary().get(LMConstants.END_OF_SENTENCE);
			System.arraycopy(sentenceId , 0 , xSentenceID , 1 , sentenceId.length);
		}
		// now iterate through it to compute the probability
		return getPhraseLogProbability(xSentence, xSentenceID , 1, xSentence.length - 1, 1, lm , oovLogProbability , minLogProbability);
	}

	public static double getSegmentLogProbability(String[] sentence , int[] sentenceId , NgramLanguageModel lm, int startIndex)
	{
		assert sentenceId == null || sentence.length == sentenceId.length;
		String[] xSentence = new String[sentence.length];
//		xSentence[0] = LMConstants.START_OF_SENTENCE;
//		xSentence[sentence.length + 1] = LMConstants.END_OF_SENTENCE;
		System.arraycopy(sentence , 0 , xSentence , 0 , sentence.length);
		int[] xSentenceID = null;
		if (lm.isVocabularyBasedLM())
		{
			if (sentenceId == null)
			{
				// make it
				sentenceId = new int[sentence.length];
				for (int i = 0; i < sentence.length; i++)
					sentenceId[i] = lm.getVocabulary().get(sentence[i]);
			}
			xSentenceID = new int[sentenceId.length];
			
//			xSentenceID[0] = lm.getVocabulary().get(LMConstants.START_OF_SENTENCE);
//			xSentenceID[sentence.length + 1] = lm.getVocabulary().get(LMConstants.END_OF_SENTENCE);
			System.arraycopy(sentenceId , 0 , xSentenceID , 0 , sentenceId.length);
		}
		// now iterate through it to compute the probability
//		return getPhraseLogProbability(xSentence, xSentenceID , 1, xSentence.length - 1, 1, lm , oovLogProbability , minLogProbability);
		return getNoStartPhraseLogProbability(xSentence, xSentenceID , startIndex, xSentence.length - 1, lm);
	}

	public static double getNoStartPhraseLogProbability(String[] xSentence , int[] xSentenceID , int idx , int length , NgramLanguageModel lm)
	{
		assert !lm.isVocabularyBasedLM() || xSentenceID != null;
	
		double [] scores = new double[2];
		
		int countOOV=0;
		int len = 3;
//		System.out.println(idx + " " + length);

		for (int i = idx; i <= length; i++)
		{
			// length
			if(i == 0)
				len = 1;
			else if(i == 1)
				len = 2;
			else
				len = 3;
			
			WordSequence seq = SlowIdWordSequence.buildReversed(lm.getVocabulary() , xSentenceID , i - len + 1 , len);
						
			double p = lm.getLogProbabilityWord(seq);
						
			if (p > 1)
			{
				p = oovLogProbability;
				countOOV++;
			}
			else{
				if (p < minLogProbability)
					p = minLogProbability;
			}

			scores[0] += p;

			if(i <= idx+1)
				scores[1] += p;

		}
//		System.out.println("OOVs: " + countOOV);
		return scores[0];
	}
	
	public static double getPhraseLogProbability(String[] xSentence , int[] xSentenceID , int idx , int length , int lookBack , NgramLanguageModel lm , double oovLogProbability , double minLogProbability)
	{
		assert !lm.isVocabularyBasedLM() || xSentenceID != null;
		
		int countOOV=0;
		double sumProbability = 0f;
		int maxLen = 3;//lm.getN();
		int k = lookBack + 1;
		double first2 = 0;
		for (int i = idx; i < idx + length; i++)
		{
			// length
			int len = Math.min(k , maxLen);
			k++;
			
			
			// get WordSequence
			WordSequence seq;
			if (lm.isVocabularyBasedLM())
				seq = SlowIdWordSequence.buildReversed(lm.getVocabulary() , xSentenceID , i - len + 1 , len);
			else
				seq = StringWordSequence.buildReversed(xSentence , i - len + 1 , len);
			
			double p = lm.getLogProbabilityWord(seq);
			if (p > 1)
			{
				p = oovLogProbability;
				countOOV++;
			}
			else
			if (p < minLogProbability)
				p = minLogProbability;
			sumProbability += p;
		}
//		System.out.println("OOVs: " + countOOV);
		return sumProbability;
	}
}
