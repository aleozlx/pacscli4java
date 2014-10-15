package alx.pacswitch.types;
import java.util.*;

public class MessageInbox extends HashMap<String,LinkedList<Dynamic>> {
	private static final long serialVersionUID = 1L;
	public static final String K_FROM="from";
	public static final String K_MSG="message";
	public static final String K_RECVTIME="recvtime";

	public void pend(String from, String message){
		if(!containsKey(from))this.put(from, new LinkedList<Dynamic>());
		LinkedList<Dynamic> list=get(from);
		Dynamic pending_message=new Dynamic();
		pending_message.put(K_FROM, from);
		pending_message.put(K_MSG, message);
		pending_message.put(K_RECVTIME, new Date());
		list.add(pending_message);
	}
	
	public synchronized Map<String,Integer> count(){
		HashMap<String,Integer> r=new HashMap<String,Integer>();
		for(String key:this.keySet()){
			LinkedList<Dynamic> list=get(key);
			int sz=list.size();
			if(sz!=0)r.put(key, sz);
		}
		return r;
	}
	
	public int count(String who){
		LinkedList<Dynamic> list=get(who);
		if(list!=null)return list.size();
		else return 0;
	}
	
	@Override
	public synchronized LinkedList<Dynamic> put(String key, LinkedList<Dynamic> value){
		return super.put(key, value); 
	}
	
	@Override
	public synchronized LinkedList<Dynamic> remove(Object key){
		return super.remove(key);
	}

}
