package alx.pacswitch.types;
import java.util.*;

public interface IEventListener{
	public enum Type{
		Undefined, PingEcho,
		MessageReceived, SignalReceived, PendingCountChanged,
		MessageSending, MessageSent, MessageAllowed, MessageBlocked, SignalHandlerMissing,
		NoRouteToServer, NoResponse, InvalidOperation, AsyncException
	}
	
	public static final String K_FROM="from";
	public static final String K_TO="to";
	public static final String K_ID="id";
	public static final String K_DEVICE="device";
	public static final String K_MSG="message";
	public static final String K_EXCEPTION="exception";
	public static final String K_RET="return";
	
	public static class Args extends HashMap<String,Object>{
		private static final long serialVersionUID = 1L;
		
//		public Args(Object ... params){
//			int i=0;
//			while(i<params.length){
//				String key=(String)params[i++];
//				Object val=params[i++];
//				this.put(key,val);
//			}				
//		}
		
		public void ret(Object r){
			this.put(K_RET, r);
		}
		public Object getRet(){
			return this.get(K_RET);
		}
	}
	
	Type getType();
	void run(Args args);
}
