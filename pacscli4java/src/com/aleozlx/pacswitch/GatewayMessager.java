package com.aleozlx.pacswitch;

import java.util.*;

public abstract class GatewayMessager extends PacswitchMessager {
	public List<IMessageHandler> handlers=new LinkedList<IMessageHandler>();
	public MessageInbox inbox=new MessageInbox();
	public String ACK="ACK";
	public String NAK="NAK";
	public boolean ENAllowTracked=true;
	public boolean ENAllowUntracked=true;
	public boolean ENFilterTracked=false;
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
	
	protected boolean filter(String from,String message){
		return true;
	}
	
	protected String dispatch(String from, String message){
		String res=null;
		for(IMessageHandler i:handlers){
			String s=i.handleMessage(from, message);
			if(res==null)res=s;
		}
		return res;
	}
	
	public UntrackedMessagerHandlerMapping untrackedHandlers=new UntrackedMessagerHandlerMapping(){
		private static final long serialVersionUID = 1L;
		@Override
		protected void miss(String id) { onUntrackedMessageHandlerMissing(id); }	
	};
	
	protected void dispatchUntracked(final String from,final String id,final String message){
		untrackedHandlers.hint(id);
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
	
	protected abstract void onMessageSending(String to,String message);
	protected abstract void onMessageBlocked(String from,String message);
	protected abstract void onMessageAllowed(String from,String message);
	protected abstract void onUntrackedMessageHandlerMissing(String id);
}
