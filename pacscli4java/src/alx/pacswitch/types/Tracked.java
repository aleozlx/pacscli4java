package alx.pacswitch.types;

/**
 * FutureObject
 * @author Alex
 * @since July 3, 2014
 */
public class Tracked<V>{
	private final V initvalue;
	protected volatile V value=null;
	protected volatile boolean _isAvailable=false;
	protected static final int INTERVAL_DEFAULT=50;
	public String tag="";
	
	public Tracked(){ initvalue=null; this.reset(); }
	public Tracked(V init){ initvalue=init; this.reset(); }
	public synchronized void reset(){ this.value=initvalue; this._isAvailable=false; }
	public void force(V val){ this.value=val; }
	public V getValueNow(){ return this.value; }
	
	public synchronized V get(){ 
		while(!this._isAvailable){
			try { this.wait(); } 
			catch (InterruptedException e) { }
		}
		return this.value;
	}
	
	public synchronized V get(long timeout){ 
		if(!this._isAvailable){
			try { this.wait(timeout); } 
			catch (InterruptedException e) { }
		}
		return this._isAvailable?this.value:null; 
	}

	public synchronized void set(V val){ 
		if(!this._isAvailable){
			this.value=val; 
			this._isAvailable=true; 
			this.notifyAll();
		}
	}
}
