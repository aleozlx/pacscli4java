package com.aleozlx.pacswitch;
import java.util.*;

public class MessageInbox extends HashMap<String,LinkedList<PendingMessage>> {
	private static final long serialVersionUID = 1L;

	protected void pend(String from, String message){
		if(!containsKey(from))this.put(from, new LinkedList<PendingMessage>());
		LinkedList<PendingMessage> list=get(from);
		list.add(new PendingMessage(from, message));
	}
	
	public Map<String,Integer> count(){
		HashMap<String,Integer> r=new HashMap<String,Integer>();
		for(String key:this.keySet()){
			LinkedList<PendingMessage> list=get(key);
			int sz=list.size();
			if(sz!=0)r.put(key, sz);
		}
		return r;
	}
	
	public int count(String who){
		LinkedList<PendingMessage> list=get(who);
		if(list!=null)return list.size();
		else return 0;
	}

}
