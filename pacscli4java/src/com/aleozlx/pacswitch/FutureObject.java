package com.aleozlx.pacswitch;
import java.util.*;
import java.util.concurrent.*;

/**
 * FutureObject
 * @author Alex
 * @since July 3, 2014
 */
public class FutureObject<V> implements Future<V>{
	protected V value=null;
	private V initvalue=null;
	protected boolean _isAvailable=false;
	protected boolean _isCancelled=false;
	protected boolean _pendingCancellation=false;
	protected static final int INTERVAL_DEFAULT=50;
	protected String tag="";
	protected PacswitchException exception=null;

	public FutureObject(){ initvalue=null; this.reset(); }
	public FutureObject(V init){ initvalue=init; this.reset(); }
	public void reset(){
		this.value=initvalue;
		this._isAvailable=false;
		this._isCancelled=false;
		this._pendingCancellation=false;
		this.exception=null;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning){
		if(this._isAvailable||this._isCancelled)return false;
		else{ this._pendingCancellation=true; return true; }
	}

	@Override
	public V get(){ this.until(); return this.value;}
	public V get(long timeout){ this.until(timeout); return this._isAvailable?this.value:null; }
	public V get(long timeout, UnreachableException e) throws UnreachableException{ 
		this.until(timeout); 
		if(!this._isAvailable)throw e;
		else return this.value; 
	}
	@Override
	public V get(long timeout, TimeUnit unit){
		this.until(unit.convert(timeout,TimeUnit.MILLISECONDS));
		return this._isAvailable?this.value:null;
	}

	public synchronized void set(V val){ 
		if(!this._isAvailable&&!this._isCancelled){
			this.value=val; 
			this._isAvailable=true; 
		}
	}

	@Override
	public boolean isDone(){return this.isAvailable();}
	public boolean isAvailable(){return this._isAvailable; }
	@Override
	public boolean isCancelled(){return this._isCancelled;}
	public String getTag(){ return this.tag; }
	public PacswitchException getException(){ return this.exception; }
	public FutureObject<V> exceptionSugar(PacswitchException e){ this.exception=e; return this; }

	public final void until(int interval, int maxRetry){ 
		for(int tries=0;tries<maxRetry&&!this._isAvailable&&!this._isCancelled;tries++)Synchronizer.wait(interval); 
	}
	public final void until(long timeout){ this.until(INTERVAL_DEFAULT,(int)(timeout/INTERVAL_DEFAULT)); }
	public final void until(){ while(!this._isAvailable)Synchronizer.wait(INTERVAL_DEFAULT); }

	public final boolean valueEquals(V val){
		if(val==null)return this._isAvailable&&this.value==null;
		else return this._isAvailable&&this.value!=null&&this.value.equals(val);
	}
}

class FutureTracker<E> extends HashMap<String,FutureObject<E>>{
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

/**
 * Synchronizer
 * @author Alex
 */
class Synchronizer{
	public static final int TIMEOUT_DEFALUT=3000;

	/**
	 * Wait for some milliseconds
	 * @param ms A specific time
	 */
	public static final void wait(int ms){
		try{ Thread.sleep(ms); }
		catch(InterruptedException e){ Thread.currentThread().interrupt(); }
	}

	public static final boolean isAuthenticated(FutureObject<String> _isAuthenticated){
		if(_isAuthenticated.value.equals("unconnected"))return false;
		else{
			_isAuthenticated.until(TIMEOUT_DEFALUT);
			return _isAuthenticated.valueEquals("OK");
		}
	}
}

/**
 * Data buffer for PacswitchClient
 * @author Alex
 */
class Mybuffer{
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
