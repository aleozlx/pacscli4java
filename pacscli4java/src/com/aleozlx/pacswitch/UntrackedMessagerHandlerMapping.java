package com.aleozlx.pacswitch;
import java.util.*;
public abstract class UntrackedMessagerHandlerMapping extends HashMap<String,LinkedList<IUntrackedMessageHandler>> {
	private static final long serialVersionUID = 1L;

	public void handleMessage(String from,String id, String message){
		if(!containsKey(id))miss(id);
		if(containsKey(id))
			for(IUntrackedMessageHandler i:get(id))
				i.handleMessage(from, message);
	}
	
	public void hint(String id){
		if(!containsKey(id))miss(id);
	}
	
	protected abstract void miss(String id);
	
	public void add(String id,IUntrackedMessageHandler h){
		if(!containsKey(id))put(id,new LinkedList<IUntrackedMessageHandler>());
		get(id).add(h);
	}
	
	public void remove(String id,IUntrackedMessageHandler h){
		if(containsKey(id))get(id).remove(h);
	}
}
