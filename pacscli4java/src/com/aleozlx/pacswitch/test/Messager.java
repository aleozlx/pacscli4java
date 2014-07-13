package com.aleozlx.pacswitch.test;
import java.util.Date;

import com.aleozlx.pacswitch.GatewayMessager;

public class Messager extends GatewayMessager{
	public static final String HOST="222.69.93.107";
	public static final String CLIENTTYPE="messager1.0";
	
	public boolean connect(String userid,String password){
		return connect(userid, password, null, HOST,CLIENTTYPE);
	}
	
	@Override
	protected void onMessageAllowed(String from, String message) {
		String output=format(from,message,true);
		new Storage(this.getUserID()).msgHistory.write(from, output);
		
	}
	
	@Override
	protected void onMessageSending(String to, String message) {
		String output=format(to,message,false);
		new Storage(this.getUserID()).msgHistory.write(to, output);
	}
	
	@Override
	protected void onMessageBlocked(String from, String message) { }
	
	protected String format(String with,String message,boolean isIncoming){
		return String.format("%1$s %3$s: %2$s\n",
				(isIncoming?with:this.getUserID()),
				message.trim(),
				Main.getDateFormat().format(new Date()));
	}	

}