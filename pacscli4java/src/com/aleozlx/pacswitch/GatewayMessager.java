package com.aleozlx.pacswitch;

import java.util.*;

/**
 * GatewayMessager
 * @author Alex
 * @version 1.3.1
 * @since July 14, 2014
 */
public abstract class GatewayMessager extends PacswitchMessager {
	/**
	 * Tracked messages handlers
	 */
	public List<IMessageHandler> handlers=new LinkedList<IMessageHandler>();
	
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
			String res=this.dispatch(from, message);
			if(res==null){
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
	protected void handleUntrackedMessage(String from,String id, String message){
		if(ENAllowUntracked&&(!ENFilterUnTracked||filter(from,message)))dispatchUntracked(from,id,message);
	}
	
	/**
	 * Asynchronously notify all tracked messages handlers of change of 
	 * counts of pending messages
	 */
	public void notifyPendingCounts(){
		new Thread(){
			@Override
			public void run(){
				Map<String,Integer> ct=inbox.count();
				if(handlers!=null)for(IMessageHandler i:handlers) 
					i.refreshPendingCounts(ct);
			}
		}.start();
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
	 * Dispatch tracked messages
	 * @param from Sender ID
	 * @param message
	 * @return Whatever the first responsible handler returns, or null if none does
	 */
	protected String dispatch(String from, String message){
		String res=null;
		for(IMessageHandler i:handlers){
			String s=i.handleMessage(from, message);
			if(res==null)res=s;
		}
		return res;
	}
	
	/**
	 * Untracked message handlers
	 */
	public UntrackedMessagerHandlerMapping untrackedHandlers=new UntrackedMessagerHandlerMapping(){
		private static final long serialVersionUID = 1L;
		@Override
		protected void miss(String from,String id,String messager) { onUntrackedMessageHandlerMissing(from,id,messager); }	
	};
	
	/**
	 * Dispatch untracked messages
	 * @param from Sender ID
	 * @param id Target ID
	 * @param message
	 */
	protected void dispatchUntracked(final String from,final String id,final String message){
		untrackedHandlers.hint(from,id,message);
		new Thread(){
			@Override
			public void run(){
				untrackedHandlers.handleMessage(from, id, message);}}.start();
	}
	
	@Override
	public String send(String to, String message) throws PacswitchException{
		onMessageSending(to,message);
		return super.send(to, message);
	}
	
	/**
	 * Message outgoing event
	 * @param to Sender ID
	 * @param message
	 */
	protected abstract void onMessageSending(String to,String message);
	
	/**
	 * Message blocked event
	 * @param from Receiver ID
	 * @param message
	 */
	protected abstract void onMessageBlocked(String from,String message);
	
	/**
	 * Message incoming event
	 * @param from Sender ID
	 * @param message
	 */
	protected abstract void onMessageAllowed(String from,String message);
	
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
	protected abstract void onUntrackedMessageHandlerMissing(String from,String id,String message);
}
