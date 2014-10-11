package com.aleozlx.pacswitch.types;

import java.util.LinkedList;

public class EventListenerList extends LinkedList<IEventListener> {
	private static final long serialVersionUID = 1L;
	public synchronized void fireEvent(IEventListener.Type type,IEventListener.Args args){
		for(IEventListener listener:this)
			if(listener.getType().equals(type))listener.run(args);
	}
	
	public void fireEvent(IEventListener.Type type){
		fireEvent(type,null);
	}
	
	public synchronized int countEvent(IEventListener.Type type){
		int sum=0;
		for(IEventListener listener:this)
			if(listener.getType().equals(type))sum++;
		return sum;
	}
	
	@Override
	public synchronized boolean add(IEventListener e){
		return super.add(e);
	}
	
	@Override
	public synchronized boolean remove(Object e){
		return super.remove(e);
	}
}