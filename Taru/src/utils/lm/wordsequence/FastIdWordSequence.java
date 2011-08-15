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

import utils.lm.*;

public class FastIdWordSequence implements IdWordSequence
{
	private static int MAX_SIZE = 3;
	private static long[] maskByLen;
	static
	{
		maskByLen = new long[MAX_SIZE + 1];
		
		long value = 0;
		for (int i = 0; i < maskByLen.length; i++)
		{
			maskByLen[i] = value;
			for (int j = 0; j < SlowIdWordSequence.bitsPerWord; j++)
			{
				value <<= 1;
				value |= 1;
			}
		}
	}
	
	public static long nearSequence(long sequenceID)
	{
		return sequenceID >>> SlowIdWordSequence.bitsPerWord;
	}
	public static long farSequence(long sequenceID, int len )
	{
		return sequenceID & maskByLen[len - 1];
	}
	
	public static WordSequence buildReversed(Vocabulary vocabulary , int[] seq , int idx , int len)
	{
		int[] str = new int[len];
		for (int i = 0; i < str.length; i++)
			str[i] = seq[idx + (str.length - i) - 1];
		return new FastIdWordSequence(vocabulary , str , 0 , len);
	}
	
	private FastIdWordSequence(Vocabulary vocabulary , long sequenceID , int len)
	{
		this.v = vocabulary;
		this.len = len;
		this.sequenceID = sequenceID;
	}
	
	public FastIdWordSequence(Vocabulary vocabulary , int[] seq , int idx , int len)
	{
		if (len > MAX_SIZE)
			len = MAX_SIZE;
		this.v = vocabulary;
		this.len = len;
		this.sequenceID = SlowIdWordSequence.getSequenceID(seq , idx , len);
	}
	
	
	private final long sequenceID;
	private final int len;
	private final Vocabulary v;
	public long getSequenceID()
	{
		return sequenceID;
	}
	public int length()
	{
		return len;
	}
	
	public boolean equals(Object e)
	{
		assert e instanceof FastIdWordSequence;
		FastIdWordSequence w = (FastIdWordSequence)e;
		if (w.len != len)
			return false;
		if (w.sequenceID != this.sequenceID)
			return false;
		return true;
	}
	/** Returns the subsequence wn, wn-1, ... wn-k+1
	 */
	public WordSequence getNearSequence()
	{
		return new FastIdWordSequence(v , nearSequence(sequenceID) , len - 1);
	}
	/** Returns the subsequence wn-1, wn-2, ... wn-k
	 */
	public WordSequence getFarSequence()
	{
		return new FastIdWordSequence(v , farSequence(sequenceID,len), len - 1);
	}
	
	/** Reduce the sequence to a certain length */
	public WordSequence trimToSize(int n)
	{
		assert n < length();
		return new FastIdWordSequence(v ,
									  sequenceID >>>
									  (SlowIdWordSequence.bitsPerWord * (length() - n))
									  , n);
	}
	
	
	/**
	 * Serialization the word sequence into a string. <br>
	 * To be deserialized only using <i>deserialize</i> method.
	 */
	public String serialize()
	{
		// TODO
		throw new Error("TODO");
	}
	/** Rebuilds the word sequence from the serialized form. <br>
	 * The opposite of <i>serialize</i> method.
	 **/
	public static WordSequence deserialize(Vocabulary v , String serialized)
	{
		// TODO
		throw new Error("TODO");
	}
	
	public String getTheSingleWord()
	{
		assert length() == 1;
		String k = v.get((int)sequenceID);
		if (k == null)
			k = "_UNK_";
		return k;
	}
}
