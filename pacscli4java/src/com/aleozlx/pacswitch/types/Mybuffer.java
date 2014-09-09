package com.aleozlx.pacswitch.types;

/**
 * Data buffer for PacswitchClient
 * @author Alex
 */
public class Mybuffer{
	/**
	 * Buffer size
	 */
	public static final int SZ_BUFFER=32768;

	/**
	 * Data bufferred
	 */
	public byte[] buffer=new byte[SZ_BUFFER];

	/**
	 * Data size
	 */
	public int size=0;

	/**
	 * Find position of specific sequence.
	 * @param s2 A sequence
	 * @param start Position to get started
	 * @return The position of specific sequence or -1 if not found.
	 */
	public final int find(byte[] s2,int start){
		for(int i=start;i<this.size;i++)
			for(int j=0;j<s2.length&&i+j<this.size&&this.buffer[i+j]==s2[j];j++)
				if(j==s2.length-1)return i;
		return -1;
	}

	/**
	 * Find position of specific sequence.
	 * @param s2 A sequence
	 * @return The position of specific sequence or -1 if not found.
	 */
	public final int find(byte[] s2){ return find(s2,0); }	
}