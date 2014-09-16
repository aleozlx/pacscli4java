package com.aleozlx.pacswitch.types;
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
	public String tag="";

	public FutureObject(){ initvalue=null; this.reset(); }
	public FutureObject(V init){ initvalue=init; this.reset(); }
	public void reset(){
		this.value=initvalue;
		this._isAvailable=false;
		this._isCancelled=false;
		this._pendingCancellation=false;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning){
		if(this._isAvailable||this._isCancelled)return false;
		else{ this._pendingCancellation=true; return true; }
	}

	@Override
	public V get(){ this.until(); return this.value;}
	public V get(long timeout){ this.until(timeout); return this._isAvailable?this.value:null; }
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
	
	public synchronized void force(V val){ 
		this.value=val; 
	}

	@Override
	public boolean isDone(){return this.isAvailable();}
	public boolean isAvailable(){return this._isAvailable; }
	@Override
	public boolean isCancelled(){return this._isCancelled;}
	public String getTag(){ return this.tag; }

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
