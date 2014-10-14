package alx.pacswitch.types;
import java.util.concurrent.*;

import alx.utils.Synchronizer;

/**
 * FutureObject
 * @author Alex
 * @since July 3, 2014
 * [Concurrent]
 */
public class FutureObject<V> implements Future<V>{
	protected V value=null;
	private final V initvalue;
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
	
	public void force(V val){ 
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
	//TODO consider wait() & notify() for message tracking - `wait` `sleep` `until`

	
	/* TODO 
indefinite wait:
pacswitch/PacswitchMessager.java:			_isAuthenticated.until(3000);
pacswitch/PacswitchMessager.java:				mr.until(5000);
pacswitch/types/FutureObject.java:	public V get(){ this.until(); return this.value;}
pacswitch/types/FutureObject.java:	public V get(long timeout){ this.until(timeout); return this._isAvailable?this.value:null; }
pacswitch/types/FutureObject.java:		this.until(unit.convert(timeout,TimeUnit.MILLISECONDS));
pacswitch/types/FutureObject.java:	public final void until(int interval, int maxRetry){ 
pacswitch/types/FutureObject.java:		for(int tries=0;tries<maxRetry&&!this._isAvailable&&!this._isCancelled;tries++)Synchronizer.wait(interval); 
pacswitch/types/FutureObject.java:	public final void until(long timeout){ this.until(INTERVAL_DEFAULT,(int)(timeout/INTERVAL_DEFAULT)); }
pacswitch/types/FutureObject.java:	public final void until(){ while(!this._isAvailable)Synchronizer.wait(INTERVAL_DEFAULT); }

thread pool:
pacswitch/test/CommWindow.java:			new Thread("SendAsync"){
pacswitch/PacswitchMessager.java:		Thread t_send=new Thread(T_PACSWITCH_SEND){
pacswitch/GatewayMessager.java://		new Thread("GM Notifier"){
pacswitch/GatewayMessager.java:		new Thread("GM SigDispatcher"){

pacswitch/PacswitchClient.java:			catch(IOException e){ Synchronizer.wait(800); }
pacswitch/PacswitchClient.java:			catch(IOException e){ Synchronizer.wait(2000); }
pacswitch/PacswitchClient.java:				try { Thread.sleep(600); } 
utils/Synchronizer.java:	public static final void wait(int ms){
utils/Synchronizer.java:		try{ Thread.sleep(ms); }
pacswitch/PacswitchClient.java:				try { Thread.sleep(600); } 
utils/Synchronizer.java:		try{ Thread.sleep(ms); }
utils/Synchronizer.java:		catch(InterruptedException e){ Thread.currentThread().interrupt(); }
pacswitch/PacswitchClient.java:			new Thread(T_PACSWITCH_RECV){
	 * */
	
	public final boolean valueEquals(V val){
		if(val==null)return this._isAvailable&&this.value==null;
		else return this._isAvailable&&this.value!=null&&this.value.equals(val);
	}
}
