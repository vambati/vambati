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

public class Vocabulary
{
	public static final int NOT_SET = -2;
	public static final int OOV = -1;
	public Vocabulary()
	{
		vocabulary = new HashMap<String,Integer>();
		invVocabulary = new Vector<String>();
	}
	private int nextID = 0;
	private boolean lock = false;
	private HashMap<String,Integer> vocabulary;
	private Vector<String> invVocabulary;
	
	
	public void lock()
	{
		lock = true;
	}
	public boolean isLocked()
	{
		return lock;
	}
	
	public void saveBinary(DataOutputStream output) throws IOException
	{
		output.writeInt(invVocabulary.size());
		for (String w : invVocabulary)
			output.writeUTF(w);
	}
	public void loadBinary(DataInputStream input) throws IOException
	{
		lock = false;
		int n = input.readInt();
		for (int i = 0; i < n; i++)
			add(input.readUTF());
	}
	public int add(String word)
	{
		if( lock )
			throw new RuntimeException( "Vocabulary locked" );
		vocabulary.put(word, nextID);
		invVocabulary.add(word);
		return nextID++;
	}
	public int get(String word)
	{
		if (vocabulary.containsKey(word))
			return vocabulary.get(word);
		return -1;
	}
	public String get(int wordID)
	{
		if (wordID == -1)
			return null;
		return invVocabulary.get(wordID);
	}
	public int size()
	{
		return nextID;
	}
}
