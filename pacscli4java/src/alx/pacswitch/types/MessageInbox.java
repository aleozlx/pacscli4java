package alx.pacswitch.types;
import java.util.*;
public class MessageInbox extends HashMap<String,LinkedList<MessageInbox.Msg>> {
	private static final long serialVersionUID = 1L;
	public static class Msg extends alx.utils.Dynamic<Msg.MsgParam>{
		static enum MsgParam{ FROM,MSG,RECVTIME }
		private static final long serialVersionUID = 1L;
		public Msg(String from,String message){
			this.put(MsgParam.FROM, from);
			this.put(MsgParam.MSG, message);
			this.put(MsgParam.RECVTIME, new Date());
		}
		public String getFrom(){return (String)this.get(MsgParam.FROM);}
		public String getMessage(){return (String)this.get(MsgParam.MSG);}
		public Date getRecvtime(){return (Date)this.get(MsgParam.RECVTIME);}
	}
	
	@Override
	public synchronized LinkedList<Msg> put(String key, LinkedList<Msg> value){ return super.put(key, value);  }
	@Override
	public synchronized LinkedList<Msg> remove(Object key){ return super.remove(key); }

	public void pend(String from, String message){
		if(!containsKey(from))this.put(from, new LinkedList<Msg>());
		LinkedList<Msg> list=get(from);
		list.add(new Msg(from,message));
	}
	
	public synchronized Map<String,Integer> count(){
		HashMap<String,Integer> r=new HashMap<String,Integer>();
		for(String key:this.keySet()){
			LinkedList<Msg> list=get(key);
			int sz=list.size();
			if(sz!=0)r.put(key, sz);
		}
		return r;
	}
	
	public int count(String who){
		LinkedList<Msg> list=get(who);
		if(list!=null)return list.size();
		else return 0;
	}
}