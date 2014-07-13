// Fork me on GitHub! https://github.com/aleozlx/pacswitch
package com.aleozlx.pacswitch;
import java.util.*;
import java.io.*;

/**
 * PacswitchMessager
 * @author Alex
 * @version 1.2.2
 * @since June 27, 2014
 */
public abstract class PacswitchMessager extends PacswitchClient {
	/**
	 * Message encoding
	 */
	public static final String ENC="utf-8";

	protected static final String SVRRES_STREAM="SVRRES_STREAM";

	protected static final String STREAM_DENIED="";

	/**
	 * Request ID length
	 */
	private static final int RIDLEN=20;
	public static final byte[] SANSID={
		-1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1
	};
	static{ assert SANSID.length==RIDLEN; }
	
	protected static final class Tracker<E> extends FutureTracker<E>{
		private static final long serialVersionUID = 1L;
		private static final char[] RIDRANGE="0123456789abcdefghjkmnpqrstuvwxyz".toCharArray();
		public Tracker() { super(RIDLEN,RIDRANGE); }	
	}

	/**
	 * Authentication state
	 */
	FutureObject<String> _isAuthenticated=new FutureObject<String>("unconnected");

	/**
	 * Request/response mapping
	 */
	protected FutureTracker<String> msgtracker=new Tracker<String>();
	
	//protected FutureTracker<String> stream_tracker=new Tracker<String>();

//	/**
//	 * Incoming connection mapping
//	 */
//	protected FutureTracker<PacswitchSocket> socktracker=new Tracker<PacswitchSocket>();

	protected String device;

	/**
	 * Request ID length
	 * @param userid User ID
	 * @param password User password
	 * @param host Host IP address
	 * @param clienttype Client type
	 * @return Whether initialized successfully.
	 */
	public boolean connect(String userid,String password,String device,String host,String clienttype){
		this.device=device;
		this._isAuthenticated.reset();
		if(!this.pacInit(userid,password,host,clienttype))return false;
		else{ this.start(); this._isAuthenticated.value=""; return true; }
	}

	/**
	 * Authentication state.
	 * @return msg Authentication state
	 */
	public final boolean isAuthenticated(){ return Synchronizer.isAuthenticated(this._isAuthenticated); }

	public final String getDeviceName(){ return this.device; }
	public final String getUserID(){ return this.user; }
	public static final byte[] AFFIRMATIVE=new byte[]{10,15};
	
	protected final void respond(String to,byte[] buffer,String response){
		try{ _sendResponse(to,buffer,response.getBytes(ENC)); }
		catch(UnsupportedEncodingException e){ }
	}
	
	protected final void affirm(String to,byte[] buffer){
		_sendResponse(to,buffer,AFFIRMATIVE);
	}

	/**
	 * Handle a response or a request.
	 * @param sender Sender ID
	 * @param buffer User data
	 */
	@Override
	protected final void onDataReceived(String sender,byte[] buffer){
		String message;
		try{ message=new String(buffer,RIDLEN+1,buffer.length-RIDLEN-1,ENC); }
		catch(UnsupportedEncodingException e){ return; }
		switch(MessageType.fromByte(buffer[0])){
		case Request:
			String response=this.handleMessage(sender,message);
			respond(sender,buffer,response);
			break;
		case Response:
			try{
				String requestID=new String(buffer,1,RIDLEN,ASCII);
				msgtracker.set(requestID, message);
			}
			catch(UnsupportedEncodingException e){ }
			break;
		case ClipTrans: 
			this.affirm(sender, buffer);
			break;
		case Ping:	
			this.affirm(sender, buffer);
			break;
			
//		case STREAMREQ:
//			if(message.equals(this.getDeviceName())){
//				try{ 
//					String requestID=new String(buffer,1,RIDLEN,ASCII);
//					FutureObject<String> svrres=new FutureObject<String>();
//					stream_tracker.put(requestID, svrres);
//					PacswitchProtocol.STREAM(socket,requestID); 
//					svrres.until(10,60);
//					if(svrres.isAvailable()){
//						String[] args=svrres.get().split(" ");
//						String code1=args[1],code2=args[2];
//						PacswitchSocket ps=new PacswitchSocket(this);
//						/*
//						 - create socket and initialize it
//						 - initiate new thread and call acceptSocket
//						 - response the invite code to other side
//						*/
//					}
//				}
//				catch(IOException e){ }
//			}
//			else{
//				try{ 
//					pacSendData(sender,
//						MessageType.STREAMRES.getData(),
//						Arrays.copyOfRange(buffer,1,RIDLEN+1),
//						STREAM_DENIED.getBytes(ENC)); 
//				}
//				catch(UnsupportedEncodingException e){ }
//			}
//			break;
//		case STREAMRES:
//			// - create socket and initialize it
//			break;
		}
	}

	/**
	 * Handle a server response.
	 * @param msg Server response message
	 */
	@Override
	protected final void onServerResponse(String title, String msg){
		if(title.equals("LOGIN"))this._isAuthenticated.set(msg);
		else if(title.equals("STREAM")){
			FutureObject<String> svrres=msgtracker.get(SVRRES_STREAM);
			if(svrres!=null)svrres.set(msg);
		}
	}

	/**
	 * Send a message.
	 * @param to Receiver ID
	 * @param message Message content
	 * @return Response from other side
	 * @throw PacswitchException
	 */
	public String send(String to, String message) throws PacswitchException{
		FutureObject<String> mr=null;
		PacswitchException e;
		try{ 
			mr=this._send(MessageType.Request,to,message); 
			e=mr.getException();
			if(e!=null)throw e;
			return mr.get(Synchronizer.TIMEOUT_DEFALUT,new UnreachableException(to,"No response"));
		}
		finally{ if(mr!=null)this.msgtracker.remove(mr.getTag()); }
	}

	private final FutureObject<String> _send(MessageType msgtype,String to, String message){
		FutureObject<String> mr=new FutureObject<String>();
		if(this.isAuthenticated()){
			String requestID=msgtracker.create(mr);
			mr.tag=requestID;
			try{
				if(pacSendData(to,
					msgtype.getData(),
					requestID.getBytes(ASCII),
					message.getBytes(ENC))) return mr;
				else return mr.exceptionSugar(new UnreachableException(to,"Offline"));
			}
			catch(UnsupportedEncodingException e){ return mr.exceptionSugar(new PacswitchException("Unsupported encoding",e)); }
		}
		else return mr.exceptionSugar(new PacswitchException("Not authenticated"));
	}
	
	private final void _sendResponse(String to,byte[] buffer,byte[] response){
		_sendUntracked(MessageType.Response,to,response,Arrays.copyOfRange(buffer,1,RIDLEN+1));
	}
	
	private final boolean _sendUntracked(MessageType msgtype,String to, String message){
		assert !msgtype.isResponse();
		try { return _sendUntracked(msgtype,to,message.getBytes(ENC),null); } 
		catch (UnsupportedEncodingException e1) { return false; }
	}
	
	private final boolean _sendUntracked(MessageType msgtype,String to,byte[] message,byte[] rId){
		if(!msgtype.isTracked()){
			if(msgtype.isResponse())return pacSendData(to,MessageType.Response.getData(),rId,message);
			else if(this.isAuthenticated())return pacSendData(to,msgtype.getData(),SANSID,message);
			else return false;
		}
		else return false;
	}

//	protected final FutureObject<PacswitchSocket> _punch(String to, String device){
//		FutureObject<PacswitchSocket> r=new FutureObject<PacswitchSocket>();
//		FutureObject<String> sres=this._send(MessageType.STREAMREQ,to,device);
//		PacswitchException e=sres.getException();
//		r.tag=sres.getTag();
//		if(e!=null)return r.exceptionSugar(e);
//		else{ socktracker.put(sres.getTag(),r); return r; }
//	}

//	public void acceptSocket(String from, PacswitchSocket s){ }

	/**
	 * Handle a message.
	 * @param from Sender ID
	 * @param message Message content
	 * @return Response to the message
	 */
	protected abstract String handleMessage(String from, String message);
}
