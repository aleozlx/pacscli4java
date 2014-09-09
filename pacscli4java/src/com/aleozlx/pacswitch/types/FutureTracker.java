package com.aleozlx.pacswitch.types;

import java.util.*;

public class FutureTracker<E> extends HashMap<String,FutureObject<E>>{
	private static final long serialVersionUID = 1L;
	protected final int IDLEN;
	protected final char[] IDRANGE;
	public FutureTracker(int idlen,char[] range){
		IDLEN=idlen;
		IDRANGE=range;
	}
	
	public final boolean set(String id,E val){
		FutureObject<E> r=get(id);
		if(r!=null){
			r.set(val);
			return true;
		}
		else return false;
	}
	
	public final String create(FutureObject<E> fo){
		String id=genID();
		put(id,fo);
		return id;
	}
	
	protected final String genID(){
		String id=null;
		do{ id = generateID(IDLEN,IDRANGE); }
		while(containsKey(id));
		return id;
	}

	public static final String generateID(int len,char[] range) {
		String id;
		Random r=new Random();
		char[] ridbuffer=new char[len];
		for(int i=0;i<len;i++)ridbuffer[i]=range[r.nextInt(range.length)];
		id=new String(ridbuffer);
		return id;
	}
}