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
import java.io.*;
import java.util.*;

import utils.MathTools;
import utils.lm.wordsequence.*;

// to be used for up to 3-grams
// to be used for vocabulary < 1M words
// to be used by only one LM (because of ID conflict)
// to be used with only TokenWordOnly or derivates
// not to be used as server LM
public class VocabularyBackOffLM extends BackOffLM
{
//	// nanochronometerring
//	public static ProfilerChronometer nanoClock = new ProfilerChronometer("VocabularyBackOffLM", GenericLMProcessor.nanoClock);
//
	
	public boolean allowFastCall()
	{
		return allowFastCall;
	}
	
	public boolean isVocabularyBasedLM()
	{
		return true;
	}
	public Vocabulary getVocabulary()
	{
		return vocabulary;
	}
	public int getN()
	{
		return n;
	}
	
	private int n;
	private HashMap<Long,Double>[] logProbability;
	private HashMap<Long,Double>[] logBOW;
	private Vocabulary vocabulary;
	private final boolean allowFastCall;
	
	public VocabularyBackOffLM(boolean allowFastCall)
	{
		this.allowFastCall = allowFastCall;
	}
	
	
	/** Loads the model from an ARPA file
	 *
	 */
	public void loadArpa(Reader r) throws IOException
	{
		BufferedReader inputFile = new BufferedReader(r);
		String lineFile;
		// scan for \data\
		
		if (!readTill(inputFile, "\\data\\"))
			throw new EOFException("No model found");
		
		// look for ngram counts
        Vector<Integer> list = new Vector<Integer>();
        while ((lineFile = inputFile.readLine()) != null)
            if (lineFile.startsWith("ngram"))
				list.add(Integer.parseInt(lineFile.substring(lineFile.indexOf('=') + 1)));
			else if (lineFile.equals("\\1-grams:"))
                break;
		
		// prepare data structures
		logProbability = new HashMap[list.size()];
		logBOW = new HashMap[list.size() - 1];
		vocabulary = new Vocabulary();
		
		// n from n-gram
		n = list.size();
		for (int i = 0; i < logProbability.length; i++)
		{
			System.err.println("Loading " + (1 + i) + "-grams");
			
//			if(i > 2)
//				continue;
			
			// skip to the next section, if not first
			if (i > 0)
				if (!readTill(inputFile , "\\" + (i + 1) + "-grams:"))
					throw new EOFException("LM file ended too soon");
			
			// create
			logProbability[i] = new HashMap<Long,Double>();
			if (i < logBOW.length)
				logBOW[i] = new HashMap<Long,Double>();
			
			// keep handy pointer
			HashMap<Long,Double> lProbability = logProbability[i];
			HashMap<Long,Double> lBOW = i < logBOW.length ? logBOW[i] : null;
			
			int ngramOrder = i + 1;
			int ngramCount = list.get(i);
			String[] str = new String[ngramOrder];
			for (int j = 0; j < ngramCount; j++)
			{
				// read
				lineFile = inputFile.readLine();
				StringTokenizer st = new StringTokenizer(lineFile);
//				if (st.countTokens() != ngramOrder + 1 && st.countTokens() != ngramOrder + 2)
//					throw new IOException("Bad n-gram file: " + lineFile);
				
				double lP10 = Double.parseDouble(st.nextToken());
				// reverse order
				for (int k = ngramOrder - 1; k >= 0 ; k--)
					str[k] = st.nextToken().intern();
				long wSeq;
				if (ngramOrder == 1)
				{
					// word in vocabulary
					wSeq = vocabulary.add(str[0]) + SlowIdWordSequence.DELTA;
				}
				else
					wSeq = SlowIdWordSequence.getSequenceID(vocabulary , str);
				
				//WordSequence wSeq = new WordSequence(false, str, 0, ngramOrder);
				
				if (lP10 == -99)
					lProbability.put(wSeq , Double.NEGATIVE_INFINITY);
				else
					lProbability.put(wSeq , MathTools.log10toLog(lP10));
				
				// has backoff weight?
				double lB10 = 0;
				if (st.hasMoreTokens())
				{
					lB10 = Double.parseDouble(st.nextToken());
					
					if (lB10 == -99)
						lBOW.put(wSeq , Double.NEGATIVE_INFINITY);
					else
						lBOW.put(wSeq , MathTools.log10toLog(lB10));
				}
			}
		}
		if (!readTill(inputFile , "\\end\\"))
			throw new IOException("Bad n-gram file: no end marker found");
		
		// leave file open
	}
	
	
	/** Loads the model from a proprietary binary file.<br><br>
	 * Format:
	 * <pre>
	 * 1) order (3 for 3-gram) - int32
	 * 2) serialized vocabulary (see Vocabulary)
	 * 3) probability ( 1..order )
	 * 3.a) size (number of instances) - int32
	 * 3.b) probability instances (1..size)
	 * 3.b. i) key ( encodes the n-gram) - long64
	 * 3.b.ii) probability (prob. of that n-gram) - double64
	 * 4) backoff (1..order-1)
	 * ( same as (3) )
	 * </pre>
	 */
	public void loadBinary(InputStream is , int type) throws IOException
	{
		DataInputStream input = new DataInputStream(new BufferedInputStream(is));
		// order
		int order = input.readInt();
		n = order;
		System.err.println("Loading binary " + order + "-gram LM...");
		logProbability = new HashMap[order];
		logBOW = new HashMap[order - 1];
		// read vocabulary first
		vocabulary = new Vocabulary();
		vocabulary.loadBinary(input);
		
		for (int i = 0; i < logProbability.length; i++)
		{
			int size = input.readInt();
			logProbability[i] = new HashMap<Long,Double>(size);
			for (int j = 0; j < size; j++)
			{
				long key = input.readLong();
				double value = input.readDouble();
				logProbability[i].put(key, value);
			}
		}
		
		for (int i = 0; i < logBOW.length; i++)
		{
			int size = input.readInt();
			logBOW[i] = new HashMap<Long,Double>(size);
			for (int j = 0; j < size; j++)
			{
				long key = input.readLong();
				double value = input.readDouble();
				logBOW[i].put(key, value);
			}
		}
	}
	/** Saves the model into a proprietary binary file
	 *
	 */
	public void saveBinary(OutputStream os, int type) throws IOException
	{
		DataOutputStream output = new DataOutputStream(new BufferedOutputStream(os));
		// order
		output.writeInt(logProbability.length);
		// write vocabulary first
		vocabulary.saveBinary(output);
		// save hashes
		
		for (int i = 0; i < logProbability.length; i++)
		{
			output.writeInt(logProbability[i].size());
			Iterator<Long> iter = logProbability[i].keySet().iterator();
			while (iter.hasNext())
			{
				Long key = iter.next();
				double value = logProbability[i].get(key);
				output.writeLong(key);
				output.writeDouble(value);
			}
		}
		
		for (int i = 0; i < logBOW.length; i++)
		{
			output.writeInt(logBOW[i].size());
			Iterator<Long> iter = logBOW[i].keySet().iterator();
			while (iter.hasNext())
			{
				Long key = iter.next();
				double value = logBOW[i].get(key);
				output.writeLong(key);
				output.writeDouble(value);
			}
		}
		output.close();
	}
	
	public static boolean readTill(BufferedReader inputFile, String marker) throws IOException
	{
		String lineFile;
		while ((lineFile = inputFile.readLine()) != null)
			if (marker.equals(lineFile))
				return true;
		return false;
	}
	
	
	/**
	 * Returns the log probability for a word in a particular context. <br>
	 ** The string is wn, wn-1, wn-2, ...
	 */
//	int cnt = 0;
	public double getLogProbabilityWord(WordSequence sequence)
	{
//		nanoClock.resume();
////		cnt++;
//		if (cnt % 1000 == 0)
//			System.err.print('*');
		
		// trim sequence if it is too long
		if (sequence.length() > n)
			sequence = sequence.trimToSize(n);
		
		double value = fastGetLogProbabilityWord(((IdWordSequence)sequence).getSequenceID() , sequence.length());
		return value;
//
//		double v = logProbabilityWord(sequence);
//		nanoClock.pause();
//		return v;
	}
	/*	private final double logProbabilityWord(final WordSequence sequence)
	 throws OOVException
	 {
	 Double prob = probGram(sequence);
	 if (prob != null) // found
	 return prob;
	 
	 // OOV?
	 if (sequence.length() == 1)
	 throw new OOVException();
	 //	return MathTools.LOG_ZERO;
	 
	 // do backoff
	 return logProbabilityWord(sequence.getNearSequence())
	 + backoffCoeficient(sequence.getFarSequence());
	 }
	 
	 private final Double probGram(final WordSequence sequence)
	 {
	 //Double t =
	 return (Double)logProbability[sequence.length() - 1].get(((IdWordSequence)sequence).getSequenceID());
	 //System.out.println( "[probGram] " + ((IdWordSequence)sequence).getSequenceID() + " -> " + t );
	 //return t;
	 }
	 private final double backoffCoeficient(final WordSequence sequence)
	 {
	 Double coef = (Double)logBOW[sequence.length() - 1].get(((IdWordSequence)sequence).getSequenceID());
	 if (coef != null)
	 {
	 //System.out.println("[backoffC] " + ((IdWordSequence)sequence).getSequenceID() + " -> " + coef);
	 return coef;
	 }
	 //System.out.println("[backoffC] " + ((IdWordSequence)sequence).getSequenceID() + " -> 0");
	 return 0f;
	 }
	 */
	
	
	
	
	
	public double fastGetLogProbabilityWord(long sequenceID , int len)
	{
		assert len <= 3;
		// len up to 3
		Double t , b;
		double backoff = 0f;
//		System.out.println(len-1 + " gram " + sequenceID);
		t = logProbability[len - 1].get(sequenceID);
		if (t != null)
			return t.doubleValue();
		
		if (len == 1){
//			System.out.println("OOV ! " + len + " " + sequenceID);
			return 2;// OOV
		}
		
		// backoff
		b = logBOW[len - 2].get(FastIdWordSequence.farSequence(sequenceID, len));
		if (b != null){
//			System.out.println("Backing off !");
			backoff += b;
		}
		
		// trim and calculate
		sequenceID = FastIdWordSequence.nearSequence(sequenceID);

//		System.out.println(len-2 + " gram " + sequenceID);

		t = logProbability[len - 2].get(sequenceID);
		if (t != null)
			return t.doubleValue() + backoff;
		
		if (len == 2){
//			System.out.println("OOV ! " + len + " " + sequenceID);
			return 2;// OOV

		}
		
		
		b = logBOW[len - 3].get(FastIdWordSequence.farSequence(sequenceID, len - 1));
		if (b != null)
			backoff += b;
		
		
		// trim and calculate
		sequenceID = FastIdWordSequence.nearSequence(sequenceID);
//		System.out.println(len-3 + " gram " + sequenceID);

		t = logProbability[len - 3].get(sequenceID);
		if (t != null)
			return t.doubleValue() + backoff;
		
		return 2;// OOV
	}
}


