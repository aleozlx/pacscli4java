package com.aleozlx.pacswitch.types;

import java.util.LinkedList;

public class EventListenerList extends LinkedList<IEventListener> {
	private static final long serialVersionUID = 1L;
	public void fireEvent(IEventListener.Type type,IEventListener.Args args){
		for(IEventListener listener:this)
			if(listener.getType().equals(type))listener.run(args);
	}
	
	public int countEvent(IEventListener.Type type){
		int sum=0;
		for(IEventListener listener:this)
			if(listener.getType().equals(type))sum++;
		return sum;
	}
}