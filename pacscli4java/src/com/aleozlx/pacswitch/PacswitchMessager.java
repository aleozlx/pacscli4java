// Fork me on GitHub! https://github.com/aleozlx/pacswitch
package com.aleozlx.pacswitch;
import java.util.*;
import java.io.*;

/**
 * Message protocol based on paswitch protocol
 * Facilitates both tracked and untracked messages
 * transmission and provide request-response mapping.
 * @author Alex
 * @version 1.3.1
 * @since June 27, 2014
 */
public abstract class PacswitchMessager extends PacswitchClient {
//	protected static final String SVRRES_STREAM="SVRRES_STREAM";
//
//	protected static final String STREAM_DENIED="";
	
	/**
	 * FutureObject tracker with ID format specifications
	 * @param <E> Element type being mapped from a String.
	 */
	protected static final class Tracker<E> extends FutureTracker<E>{
		private static final long serialVersionUID = 1L;
		
		/**
		 * ID Range
		 */
		public static final char[] RIDRANGE="0123456789abcdefghjkmnpqrstuvwxyz".toCharArray();
		
		/**
		 * Constructor
		 */
		public Tracker() { super(MessageProtocol.RIDLEN,RIDRANGE); }	
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

	/**
	 * Device name
	 */
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
	
	/**
	 * Affirmative response message sequence
	 */
	public static final byte[] AFFIRMATIVE=new byte[]{10,15};
	
	/**
	 * Give a response
	 * @param to Receiver ID
	 * @param buffer Request buffer
	 * @param response Response message
	 */
	protected final void respond(String to,byte[] buffer,String response){
		try{ _sendResponse(to,buffer,response.getBytes(MessageProtocol.ENC)); }
		catch(UnsupportedEncodingException e){ }
	}
	
	/**
	 * Give an affirmative message
	 * @param to Receiver ID
	 * @param buffer Request buffer
	 */
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
		try{ message=new String(buffer,MessageProtocol.RIDLEN+1,buffer.length-MessageProtocol.RIDLEN-1,MessageProtocol.ENC); }
		catch(UnsupportedEncodingException e){ return; }
		switch(MessageType.fromByte(buffer[0])){
		case Request:
			String response=this.handleMessage(sender,message);
			respond(sender,buffer,response);
			break;
		case Ping:	
			this.affirm(sender, buffer);
			break;
			
		// ===== Untracked Message =====
		case Response:
			try{
				String requestID=new String(buffer,1,MessageProtocol.RIDLEN,ASCII);
				msgtracker.set(requestID, message);
			}
			catch(UnsupportedEncodingException e){ }
			break;
		case Signal:
			try{
				if(buffer[1]!=-1){
					String requestID=new String(buffer,1,MessageProtocol.RIDLEN,ASCII);
					this.handleUntrackedMessage(sender,requestID,message);
				}
				else this.handleUntrackedMessage(sender, message);
			}
			catch(UnsupportedEncodingException e){ }
			break;
		default: break;
			
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
//		else if(title.equals("STREAM")){
//			FutureObject<String> svrres=msgtracker.get(SVRRES_STREAM);
//			if(svrres!=null)svrres.set(msg);
//		}
	}

	/**
	 * Send a tracked message synchronously
	 * @param to Receiver ID
	 * @param message Message content
	 * @return Response from other side
	 * @throws PacswitchException When there is an connection error, server down or receiver absence.
	 * Refers to the exception message for a specified reason.
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
				if(MessageProtocol.send(this,msgtype,to,message.getBytes(MessageProtocol.ENC),requestID.getBytes(ASCII))) return mr;
				else return mr.exceptionSugar(new UnreachableException(to,"Offline"));
			}
			catch(UnsupportedEncodingException e){ return mr.exceptionSugar(new PacswitchException("Unsupported encoding",e)); }
		}
		else return mr.exceptionSugar(new PacswitchException("Not authenticated"));
	}
	
	private final void _sendResponse(String to,byte[] buffer,byte[] response){
		MessageProtocol.send(this,MessageType.Response,to,response,Arrays.copyOfRange(buffer,1,MessageProtocol.RIDLEN+1));
	}
	
	/**
	 * Send an untracked message without an ID.
	 * @param msgtype Message type
	 * @param to Receiver ID
	 * @param message
	 * @return Whether message was sent successfully
	 */
	protected final boolean sendUntracked(MessageType msgtype,String to, String message){
		try { return MessageProtocol.send(this,msgtype,to,message.getBytes(MessageProtocol.ENC),MessageProtocol.SANSID); } 
		catch (UnsupportedEncodingException e1) { return false; }
	}
	
	/**
	 * Send an untracked message without a target ID.
	 * @param msgtype Message type
	 * @param to Receiver ID
	 * @param message 
	 * @param id Target ID. This is for message dispatching at the other side
	 * @return Whether message was sent successfully
	 */
	protected final boolean sendUntracked(MessageType msgtype,String to, String message,byte[] id){
		try { return MessageProtocol.send(this,msgtype,to,message.getBytes(MessageProtocol.ENC),id); } 
		catch (UnsupportedEncodingException e1) { return false; }
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
	
	/**
	 * Handle an untracked message with an ID.
	 * @param from Sender ID
	 * @param id Target ID
	 * @param message
	 */
	protected abstract void handleUntrackedMessage(String from,String id, String message);
	
	/**
	 * Handle an untracked message without an ID
	 * @param from Sender ID
	 * @param message
	 */
	protected abstract void handleUntrackedMessage(String from, String message);
	
	/**
	 * Get a standard ID
	 * @return A standard ID
	 */
	public static final String generateID(){
		return FutureTracker.generateID(MessageProtocol.RIDLEN, Tracker.RIDRANGE);
	}
	
	/**
	 * Message protocol implementation
	 */
	static class MessageProtocol extends PacswitchProtocol {
		/**
		 * Message encoding
		 */
		public static final String ENC="utf-8";

		/**
		 * Request ID length
		 */
		public static final int RIDLEN=20;
		
		/**
		 * An ID, which dosen't exist because this cannot
		 * be decoded with ASCII encoding, acts as a place 
		 * holder for those untracked messages without target ID.
		 */
		public static final byte[] SANSID={
			-1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1
		};
		
		static{ assert SANSID.length==RIDLEN; }
		
		/**
		 * Send method for message protocol
		 * @param cli Abstract client
		 * @param msgtype Message type
		 * @param to Receiver ID
		 * @param message Message content
		 * @param rId Request ID or target ID(for some untracked messages)
		 * @return Whether sent successfully
		 */
		public static final boolean send(
			PacswitchMessager cli,
			MessageType msgtype, String to,
			byte[] message, byte[] rId
		){
			if(!msgtype.isTracked()){
				if(msgtype.isResponse())return cli.pacSendData(to,MessageType.Response.getData(),rId,message);
				else if(cli.isAuthenticated())return cli.pacSendData(to,msgtype.getData(),rId,message);
				else return false;
			}
			else if(cli.isAuthenticated()) return cli.pacSendData(to,msgtype.getData(),rId,message);
			else return false;
		}
	}
}
