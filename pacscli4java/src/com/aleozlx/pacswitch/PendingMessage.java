package com.aleozlx.pacswitch;
import java.util.*;
public class PendingMessage {
	protected Date recvtime;
	protected String from;
	protected String message;
	public PendingMessage(String from,String message){
		this.recvtime=new Date();
		this.from=from;
		this.message=message;
	}
	public Date getTime(){
		return recvtime;
	}
	public String getFrom(){
		return from;
	}
	public String getMessage(){
		return message;
	}
}
