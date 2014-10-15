package alx.pacswitch;

import java.util.*;
import java.util.concurrent.*;

import alx.pacswitch.types.*;
import alx.utils.Dynamic;

/**
 * GatewayMessager
 * @author Alex
 * @since July 14, 2014
 */
public class GatewayMessager extends PacswitchMessager {
//	/**
//	 * Tracked messages handlers
//	 */
//	public List<IMessageHandler> handlers=new LinkedList<IMessageHandler>();
	
	/**
	 * Message inbox for messages not read immediately
	 */
	public MessageInbox inbox=new MessageInbox();
	
	/**
	 * Acknowledgement
	 */
	public String ACK="ACK";
	
	/**
	 * Negative acknowledgement
	 */
	public String NAK="NAK";
	
	/**
	 * Enable tracked messages to pass through
	 */
	public boolean ENAllowTracked=true;
	
	/**
	 * Enable untracked messages to pass through
	 */
	public boolean ENAllowUntracked=true;
	
	/**
	 * Enable tracked messages filtering
	 */
	public boolean ENFilterTracked=false;
	
	/**
	 * Enable untracked messages filtering
	 */
	public boolean ENFilterUnTracked=false;
	
	@Override
	protected String handleMessage(String from, String message) {
		if(!ENAllowTracked)return NAK;
		else if(!ENFilterTracked||filter(from,message)){ 
			String res=super.handleMessage(from, message);
			if(!res.equals(ACK)){
				inbox.pend(from,message); 
				notifyPendingCounts();
				onMessageAllowed(from,message);
				return ACK; 
			}
			else{ 
				onMessageAllowed(from,message);
				return res;
			}
		}
		else{
			onMessageBlocked(from,message);
			return NAK;
		}
	}
	
	@Override
	protected void handleSignal(String from,String id, String message){
		if(ENAllowUntracked&&(!ENFilterUnTracked||filter(from,message)))dispatchUntracked(from,id,message);
	}
	
	/**
	 * Asynchronously notify all tracked messages handlers of change of 
	 * counts of pending messages
	 */
	protected void notifyPendingCounts(){
		this.listeners.fireEvent(IEventListener.Type.PendingCountChanged);	
	}
	
	/**
	 * Message filter
	 * @param from Sender ID
	 * @param message Message content
	 * @return Allow the message to pass or not
	 */
	protected boolean filter(String from,String message){
		return true;
	}
	
	/**
	 * Untracked message handlers
	 */
	SignalHandlerMap sighandlers=new SignalHandlerMap();
	
	ThreadPoolExecutor sigDispatcherPool = new ThreadPoolExecutor(1,8,200,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(20));
	
	/**
	 * Dispatch untracked messages
	 * @param from Sender ID
	 * @param id Target ID
	 * @param message
	 */
	protected void dispatchUntracked(final String from,final String id,final String message){
		sighandlers.hint(from,id,message);
		sigDispatcherPool.execute(new Runnable(){
			@Override
			public void run(){ sighandlers.handleMessage(from, id, message); }
		});
	}
	
	class SignalHandlerMap extends HashMap<String,LinkedList<ISignalHandler>> {
		private static final long serialVersionUID = 1L;

		public void handleMessage(String from,String id, String message){
			if(!containsKey(id))onSignalHandlerMissing(from,id,message);
			if(containsKey(id))
				for(ISignalHandler i:get(id))
					i.handleSignal(from, message);
		}
		
		public void hint(String from, String id, String message){
			if(!containsKey(id))onSignalHandlerMissing(from,id,message);
		}
		
		public void add(String id,ISignalHandler h){
			if(!containsKey(id))put(id,new LinkedList<ISignalHandler>());
			get(id).add(h);
		}
		
		public void remove(String id,ISignalHandler h){
			if(containsKey(id))get(id).remove(h);
		}
	}
	
	public void addSignalHandler(String id,ISignalHandler h){
		this.sighandlers.add(id, h);
	}
	
	public void removeSignalHandler(String id,ISignalHandler h){
		this.sighandlers.remove(id, h);
	}
	
	/**
	 * Asynchronously send a message.
	 * @param to Receiver ID
	 * @param message
	 * @return ACK or NAK
	 */
	@Deprecated
	@Override
	public String send(String to, String message) throws PacswitchException{
		onMessageSending(to,message);
		String r=super.send(to, message);
		if(r.equals(ACK))onMessageSent(to,message);
		return r;
	}
	
	/**
	 * Asynchronously send a message.
	 * IEventListener.Type.MessageSent shall be fired directly from {@link PacswitchMessager#sendAsync} if it comes up.
	 * @param to Receiver ID
	 * @param message
	 */
	@Override
	public void sendAsync(final String to, final String message){
		onMessageSending(to,message);
		super.sendAsync(to, message);
	}
	
	@Override
	public void close(){
		sigDispatcherPool.shutdown();
		super.close();
	}
	
	/**
	 * Message outgoing event
	 * @param to Receiver ID
	 * @param message
	 */
	protected void onMessageSending(String to,String message){
		Dynamic args=new Dynamic();
		args.put(IEventListener.K_TO, to);
		args.put(IEventListener.K_MSG, message);
		this.listeners.fireEvent(IEventListener.Type.MessageSending, args);
	}
	
	/**
	 * Message outgoing event (only successful ones)
	 * @param to Receiver ID
	 * @param message
	 */
	protected void onMessageSent(String to,String message){
		Dynamic args=new Dynamic();
		args.put(IEventListener.K_TO, to);
		args.put(IEventListener.K_MSG, message);
		this.listeners.fireEvent(IEventListener.Type.MessageSent, args);
	}
	
	/**
	 * Message blocked event
	 * @param from Sender ID
	 * @param message
	 */
	protected void onMessageBlocked(String from,String message){
		Dynamic args=new Dynamic();
		args.put(IEventListener.K_FROM, from);
		args.put(IEventListener.K_MSG, message);
		this.listeners.fireEvent(IEventListener.Type.MessageBlocked, args);
	}
	
	/**
	 * Message incoming event
	 * @param from Sender ID
	 * @param message
	 */
	protected void onMessageAllowed(String from,String message){
		Dynamic args=new Dynamic();
		args.put(IEventListener.K_FROM, from);
		args.put(IEventListener.K_MSG, message);
		this.listeners.fireEvent(IEventListener.Type.MessageAllowed, args);
	}
	
	/**
	 * Untracked message handler missing event,
	 * which occurs when there's an incoming untracked
	 * message whose target ID points to nothing
	 * in the (untracked) handlers mapping, expecting
	 * to be handled after this event.
	 * @param from Sender ID
	 * @param id Target ID
	 * @param message
	 */
	protected void onSignalHandlerMissing(String from,String id,String message){
		Dynamic args=new Dynamic();
		args.put(IEventListener.K_FROM, from);
		args.put(IEventListener.K_ID, id);
		args.put(IEventListener.K_MSG, message);
		this.listeners.fireEvent(IEventListener.Type.SignalHandlerMissing, args);
	}
	

}
