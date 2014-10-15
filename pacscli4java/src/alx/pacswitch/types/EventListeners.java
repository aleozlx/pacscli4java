package alx.pacswitch.types;

import java.util.*;

public class EventListeners extends HashMap<IEventListener.Type,LinkedList<IEventListener>> {
	private static final long serialVersionUID = 1L;
	public synchronized void fireEvent(IEventListener.Type type,Dynamic args){
		LinkedList<IEventListener> elist=get(type);
		if(elist!=null) for(IEventListener listener:elist) listener.run(args);
	}
	
	public void fireEvent(IEventListener.Type type){
		fireEvent(type,null);
	}
	
	public synchronized int countEvent(IEventListener.Type type){
		LinkedList<IEventListener> elist=get(type);
		if(elist!=null)return elist.size();
		else return 0;
	}
	
	public synchronized void add(IEventListener e){
		IEventListener.Type type=e.getType();
		if(!containsKey(type)){
			LinkedList<IEventListener> elist=new LinkedList<IEventListener>();
			elist.add(e);
			put(type,elist);
		}
	}
	
	public synchronized void remove(IEventListener e){
		LinkedList<IEventListener> elist=get(e.getType());
		if(elist!=null)elist.remove(e);
	}
}