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

import utils.lm.wordsequence.*;

public class StringWordSequence implements WordSequence
{
	
	public static WordSequence buildReversed(String[] string , int idx , int len)
	{
		String[] str = new String[len];
		for (int i = 0; i < str.length; i++)
			str[i] = string[idx + (str.length - i) - 1];
		return new StringWordSequence(false , str , 0 , len);
	}
	private final String[] string;
	private final int idx , len;
	/**
	 * Constructor. <br>
	 * newOne = do not reuse the object "string" <br>
	 * string = contains <br>
	 * The string is provided as wn, wn-1, wn-2, ... <br>
	 */
	public StringWordSequence(boolean newOne , String[] string , int idx , int len)
	{
		if (newOne)
		{
			// copy
			this.string = new String[len];
			this.idx = 0;
			this.len = len;
			System.arraycopy(string , idx , this.string , 0 , len);
		}
		else
		{
			this.string = string;
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
		assert e instanceof StringWordSequence;
		StringWordSequence w = (StringWordSequence)e;
		
		if (w.len != this.len)
			return false;
		for (int i = 0; i < len; i++)
			if (!w.string[w.idx + i].equals(this.string[this.idx + i]))
				return false;
		assert w.serialize().equals(this.serialize());
		return true;
	}
	private int cacheHashCode = Integer.MIN_VALUE;
	private static final String cnst = " ";
	public int hashCode()
	{
		if (cacheHashCode == Integer.MIN_VALUE)
		{
			// compute
			int prod = 1;
			for (int i = 0; i < len; i++)
			{
				prod *= string[idx + i].hashCode();
				prod *= cnst.hashCode();
			}
			cacheHashCode = prod;
		}
		return cacheHashCode;
	}
	/** Returns the subsequence wn, wn-1, ... wn-k+1
	 */
	public WordSequence getNearSequence()
	{
		return new StringWordSequence(false , string , idx , len - 1);
	}
	/** Returns the subsequence wn-1, wn-2, ... wn-k
	 */
	public WordSequence getFarSequence()
	{
		return new StringWordSequence(false , string , idx + 1 , len - 1);
	}
	
	/** Reduce the sequence to a certain length */
	public WordSequence trimToSize(int n)
	{
		assert n < length();
		return new StringWordSequence(false , string , idx , n);
	}
	
	
	
	//unoptimized
	public String toString()
	{
		String k = "";
		for (int i = 0; i < len; i++)
			k = "[" + string[idx + i] + "] " + k;
		return k;
	}
	
	
	// separator for serialization
	protected static final String SEPARATOR = "\t";
	/** Serialization the word sequence into a string. <br>
	 * To be deserialized only using <i>deserialize</i> method.
	 **/
	public String serialize()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++)
			sb.append(string[idx + i]).append(SEPARATOR);
		//sb.setLength( sb.length()-1 );
		return sb.toString();
	}
	/** Rebuilds the word sequence from the serialized form. <br>
	 * The opposite of <i>serialize</i> method.
	 **/
	public static WordSequence deserialize(String serialized)
	{
		StringTokenizer st = new StringTokenizer(serialized, SEPARATOR);
		String[] k = new String[st.countTokens()];
		for (int i = 0; i < k.length; i++)
			k[i] = st.nextToken().intern();
		
		return new StringWordSequence(false , k , 0 , k.length);
	}
	
	public String getTheSingleWord()
	{
		assert length() == 1;
		return string[idx];
	}
}
