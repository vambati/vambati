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
package utils;

public class MathTools
{
	/* Base e */
	
	
	public static double numberToLog(double a)
	{
		return Math.log(a);
	}
	public static double log10toLog(double log)
	{
		return log * LN_10;
	}
	
	public static double logToLog10(double log)
	{
		return log / LN_10;
	}
	public static double lnToLog(double logProbabilityUnk)
	{
		return logProbabilityUnk;
	}
	
	public static double logToNumber(double a)
	{
		return Math.exp(a);
	}
	/* Base 10 */
	/*
	 public static double numberToLog(double n)
	 {
	 return Math.log10(n);
	 }
	 public static double log10toLog(double n)
	 {
	 return n;
	 }
	 
	 public static double logToLog10(double n)
	 {
	 return n;
	 }
	 public static double lnToLog(double n)
	 {
	 return n / LN_10;
	 }
	 
	 public static double logToNumber(double n)
	 {
	 return Math.pow(10,n);
	 }
	 
	 */
	private static final double LN_10 = Math.log(10f);
	
	public static final double LOG_ZERO = Double.NEGATIVE_INFINITY;
	public static final double LOG_ONE = 0f;
}
