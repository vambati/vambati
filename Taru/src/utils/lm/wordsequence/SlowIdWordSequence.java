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
package utils.lm.wordsequence;
import java.util.*;

import utils.lm.*;

public class SlowIdWordSequence implements IdWordSequence
{
	protected static final int bitsPerWord = 20;
	public static final int DELTA = 2;
//	private static final long UNK = 0xFFFFFl;
	private final Vocabulary v;
	
	public static long getSequenceID(int[] seq, int idx, int len)
	{
		long seqID = 0;
		// make sequence out of it
		for (int k = 0; k < len; k++)
		{
			long thisID = seq[idx + k] + DELTA;
//			if (thisID == 0)
//				thisID = UNK;
			
			seqID <<= bitsPerWord;
			seqID |= thisID;
		}
		return seqID;
	}
	
	public static long getSequenceID(Vocabulary vocabulary , String[] str)
	{
		long seqID = 0;
		// make sequence out of it
		for (int k = 0; k < str.length; k++)
		{
			long thisID = vocabulary.get(str[k]) + DELTA;
//			if (thisID == 0)
//				thisID = UNK;

			seqID <<= bitsPerWord;
			seqID |= thisID;
		}
		return seqID;
	}
	public long getSequenceID()
	{
		return getSequenceID( seq , idx , len );
	}
	
	
	
	
	public static WordSequence buildReversed(Vocabulary vocabulary , int[] seq , int idx , int len)
	{
		int[] str = new int[len];
		for (int i = 0; i < str.length; i++)
			str[i] = seq[idx + (str.length - i) - 1];
		return new SlowIdWordSequence(vocabulary , false , str , 0 , len);
	}
	private final int[] seq;
	private final int idx , len;
	/**
	 * Constructor. <br>
	 * newOne = do not reuse the object "string" <br>
	 * string = contains <br>
	 * The string is provided as wn, wn-1, wn-2, ... <br>
	 */
	public SlowIdWordSequence(Vocabulary vocabulary , boolean newOne , int[] seq , int idx , int len)
	{
		v = vocabulary;
		if (newOne)
		{
			// copy
			this.seq = new int[len];
			this.idx = 0;
			this.len = len;
			System.arraycopy(seq , idx , this.seq , 0 , len);
		}
		else
		{
			this.seq = seq;
			this.idx = idx;
			this.len = len;
		}
	}
	
	public int length()
	{
		return len;
	}
	
	public boolean equals(Object e)
	{
		assert e instanceof SlowIdWordSequence;
		SlowIdWordSequence w = (SlowIdWordSequence)e;
		
		if (w.len != this.len)
			return false;
		for (int i = 0; i < len; i++)
			if (w.seq[w.idx + i] != this.seq[this.idx + i])
				return false;
		assert w.serialize().equals(this.serialize());
		return true;
	}
	
	/** Returns the subsequence wn, wn-1, ... wn-k+1
	 */
	public WordSequence getNearSequence()
	{
		return new SlowIdWordSequence(v , false , seq , idx , len - 1);
	}
	/** Returns the subsequence wn-1, wn-2, ... wn-k
	 */
	public WordSequence getFarSequence()
	{
		return new SlowIdWordSequence(v , false , seq , idx + 1 , len - 1);
	}
	
	/** Reduce the sequence to a certain length */
	public WordSequence trimToSize(int n)
	{
		assert n < length();
		return new SlowIdWordSequence(v , false , seq , idx , n);
	}
	
	
	/**
	 * Serialization the word sequence into a string. <br>
	 * To be deserialized only using <i>deserialize</i> method.
	 */
	public String serialize()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++)
		{
			String k = v.get(seq[idx + i]);
			if (k == null)
				k = "_UNK_";
			sb.append(k).append(StringWordSequence.SEPARATOR);
		}
		//sb.setLength( sb.length()-1 );
		return sb.toString();
	}
	/** Rebuilds the word sequence from the serialized form. <br>
	 * The opposite of <i>serialize</i> method.
	 **/
	public static WordSequence deserialize(Vocabulary v , String serialized)
	{
		StringTokenizer st = new StringTokenizer(serialized, StringWordSequence.SEPARATOR);
		int[] k = new int[st.countTokens()];
		for (int i = 0; i < k.length; i++)
			k[i] = v.get(st.nextToken());
		
		return new SlowIdWordSequence(v , false , k , 0 , k.length);
	}
	
	public String getTheSingleWord()
	{
		assert length() == 1;
		String k = v.get(seq[idx]);
		if (k == null)
			k = "_UNK_";
		return k;
	}
	
}
