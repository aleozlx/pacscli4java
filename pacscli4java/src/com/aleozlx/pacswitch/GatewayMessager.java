package com.aleozlx.pacswitch;

import java.util.*;

public abstract class GatewayMessager extends PacswitchMessager {
	public List<IMessageHandler> handlers=new LinkedList<IMessageHandler>();
	public MessageInbox inbox=new MessageInbox(handlers);
	public String ACK="ACK";
	public String NAK="NAK";
	
	@Override
	protected String handleMessage(String from, String message) {
		String res=this.dispatch(from, message);
		if(res==null){
			if(filter(from,message)){ 
				inbox.pend(from,message); 
				notifyPendingCounts();
				onMessageAllowed(from,message);
				return ACK; 
			}
			else{
				onMessageBlocked(from,message);
				return NAK;
			}
		}
		else{
			onMessageAllowed(from,message);
			return res;
		}
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
		if(handlers!=null)for(IMessageHandler i:handlers){
			String s=i.handleMessage(from, message);
			if(res==null)res=s;
		}
		return res;
	}
	
	@Override
	public String send(String to, String message) throws PacswitchException{
		onMessageSending(to,message);
		return super.send(to, message);
	}
	
	protected abstract void onMessageSending(String to,String message);
	protected abstract void onMessageBlocked(String from,String message);
	protected abstract void onMessageAllowed(String from,String message);
}
