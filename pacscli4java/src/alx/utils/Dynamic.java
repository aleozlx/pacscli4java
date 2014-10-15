package alx.utils;

import java.util.HashMap;

public class Dynamic extends HashMap<String,Object>{
	private static final long serialVersionUID = 1L;
	public static final String K_RET="return";
	public void ret(Object r){ this.put(K_RET, r); }
	public Object getRet(){ return this.get(K_RET); }
}

class Dynamic2<E extends Enum<E>,R> extends HashMap<Enum<E>,Object>{
	private static final long serialVersionUID = 1L;
	public R retValue=null;
}
