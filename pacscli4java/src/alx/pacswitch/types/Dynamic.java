package alx.pacswitch.types;

import java.util.HashMap;

public class Dynamic extends HashMap<String,Object>{
	private static final long serialVersionUID = 1L;
	public void ret(Object r){ this.put(IEventListener.K_RET, r); }
	public Object getRet(){ return this.get(IEventListener.K_RET); }
}