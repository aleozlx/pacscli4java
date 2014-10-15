package alx.pacswitch;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;

import alx.pacswitch.types.EventArgs;
import alx.pacswitch.types.EventArgs.K;
import alx.pacswitch.types.EventListeners;
import alx.pacswitch.types.IEventListener;
import alx.pacswitch.types.PacswitchException;
import alx.pacswitch.types.Tracked;
import alx.pacswitch.types.Tracker;
import alx.pacswitch.types.UnreachableException;

/**
 * Message protocol based on paswitch protocol
 * facilitates both tracked and untracked messages
 * transmission and provide request-response mapping.<br/><br/>
 * 
 * Fork me on github: <br/>
 * <a href="https://github.com/aleozlx/pacscli4java">https://github.com/aleozlx/pacscli4java</a><br/>
 * 
 * And feel free to pay a visit to the server project:<br/>
 * <a href="https://github.com/aleozlx/pacswitch">https://github.com/aleozlx/pacswitch</a><br/>
 * 
 * @author Alex
 * @version 1.3.2
 * @since June 27, 2014
 */
public class PacswitchMessager extends PacswitchClient {
	
	/**
	 * FutureObject tracker with ID format specifications
	 * @param <E> Element type being mapped from a String.
	 */
	protected static final class MessageTracker<E> extends Tracker<E>{
		private static final long serialVersionUID = 1L;
		
		/**
		 * ID Range
		 */
		public static final char[] RIDRANGE="0123456789abcdefghjkmnpqrstuvwxyz".toCharArray();
		
		/**
		 * Constructor
		 */
		public MessageTracker() { super(MessageProtocol.RIDLEN,RIDRANGE); }	
	}

	static enum AuthenticationState{ Unconnected, Pending, OK, Failed; }
	Tracked<AuthenticationState> _isAuthenticated=new Tracked<AuthenticationState>(AuthenticationState.Unconnected);
	
	/**
	 * Request/response mapping
	 */
	protected Tracker<String> msgtracker=new MessageTracker<String>();

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
		if(!this.pacInit(userid,password,host,clienttype))
			return false;
		else{ 
			this.start();
			this._isAuthenticated.force(AuthenticationState.Pending); 
			return true;
		}
	}

	/**
	 * Authentication state.
	 * @return msg Authentication state
	 */
	public final boolean isAuthenticated(){
		if(_isAuthenticated.getValueNow().equals(AuthenticationState.Unconnected))
			return false;
		else{
			AuthenticationState s=_isAuthenticated.get(3000);
			if(s!=null)return s.equals(AuthenticationState.OK);
			else return false;
		}
	}

	public final String getDeviceName(){ return this.device; }
	public final String getUserID(){ return this.user; }
	
//	/**
//	 * Affirmative response message sequence
//	 */
//	public static final byte[] AFFIRMATIVE=new byte[]{10,15};
	
	/**
	 * Give a response
	 * @param to Receiver ID
	 * @param buffer Request buffer
	 * @param response Response message
	 */
	protected final void respond(String to,byte[] buffer,String response){
		try{
			byte[] resdata=response.getBytes(MessageProtocol.ENC);
			MessageProtocol.send(this,MessageProtocol.Type.Response,to,resdata,Arrays.copyOfRange(buffer,1,MessageProtocol.RIDLEN+1));
		}
		catch(UnsupportedEncodingException e){ }
	}
	
//	/**
//	 * Give an affirmative message
//	 * @param to Receiver ID
//	 * @param buffer Request buffer
//	 */
//	protected final void affirm(String to,byte[] buffer){
//		_sendResponse(to,buffer,AFFIRMATIVE);
//	}

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
		switch(MessageProtocol.Type.fromByte(buffer[0])){
		case Request:{
			String response=this.handleMessage(sender,message);
			respond(sender,buffer,response);
			break;
		}
		
		// ===== Signals =====
		case Response:
			try{
				String requestID=new String(buffer,1,MessageProtocol.RIDLEN,ASCII);
				msgtracker.set(requestID, message);
			}
			catch(UnsupportedEncodingException e){ }
			break;
		case Signal: //develop a UDP version of Signal
			try{
				if(buffer[1]!=-1){
					String requestID=new String(buffer,1,MessageProtocol.RIDLEN,ASCII);
					this.handleSignal(sender,requestID,message);
				}
				else this.handleSignal(sender, message);
			}
			catch(UnsupportedEncodingException e){ }
			break;
		case Ping:
			if(message.equals(MessageProtocol.PingReq)){
				String response=String.format("%1$s\t%2$s", this.getUserID(),this.getDeviceName());
				this.sendPing(sender, response);
			}
			else{
				EventArgs args=new EventArgs();
				String[] from_dev=message.split("\t");
				args.put(K.FROM, from_dev[0]);
				args.put(K.DEVICE, from_dev[1]);
				this.listeners.fireEvent(IEventListener.Type.PingEcho, args);
			}
			break;
		default: break;
		}
	}

	/**
	 * Handle a server response.
	 * @param msg Server response message
	 */
	@Override
	protected final void onServerResponse(String title, String msg){
		switch(PacswitchProtocol.SvrResponseType.fromString(title)){
		case LOGIN:
			EventArgs args_login=new EventArgs();
			args_login.put(K.RESULT, msg.equals(OK));
			this.listeners.fireEvent(IEventListener.Type.AuthenticationEvent, args_login);
			this._isAuthenticated.set(msg.equals(OK)?AuthenticationState.OK:AuthenticationState.Failed);
			break;
		case LOOKUP:
			Tracked<String> svrres=msgtracker.get(PacswitchProtocol.M_LOOKUP);
			if(svrres!=null)svrres.set(msg);
			break;
//		case STREAM:
//			FutureObject<String> svrres=msgtracker.get(SVRRES_STREAM);
//			if(svrres!=null)svrres.set(msg);
//			break;
		default:
			break;	
		}
	}

	/**
	 * Send a tracked message synchronously
	 * @param to Receiver ID
	 * @param message Message content
	 * @return Response from other side
	 * @throws PacswitchException When there is an connection error, server down or receiver absence.
	 * Refer to the exception message for a specified reason.
	 */
	@Deprecated
	public String send(String to, String message) throws PacswitchException{
		Tracked<String> mr=null;
		try{ 	
			 mr=new Tracked<String>();
			if(this.isAuthenticated()){
				String requestID=msgtracker.create(mr);
				mr.tag=requestID;
				try{
					if(!MessageProtocol.send(this,MessageProtocol.Type.Request,to,message.getBytes(MessageProtocol.ENC),requestID.getBytes(ASCII)))
						throw new UnreachableException(to,"Offline");
				}
				catch(UnsupportedEncodingException e){ 
					throw new PacswitchException("Unsupported encoding",e);
				}
			}
			else throw new PacswitchException("Not authenticated");
			
			String res=mr.get(3000);
			if(res==null)throw new UnreachableException(to,"No response");
			else return res;
		} 
		finally{
			if(mr!=null)this.msgtracker.remove(mr.tag);
		}
	}
	
	ExecutorService sendingPool = Executors.newSingleThreadExecutor(); 
	
	public void sendAsync(final String to, final String message){
		sendingPool.execute(new Runnable(){
			@Override
			public void run(){
				Tracked<String> mr=null;
				try{ 	
					mr=new Tracked<String>();
					if(PacswitchMessager.this.isAuthenticated()){
						final String requestID=msgtracker.create(mr);
						mr.tag=requestID;
						try{
							if(!MessageProtocol.send(PacswitchMessager.this,MessageProtocol.Type.Request,to,message.getBytes(MessageProtocol.ENC),requestID.getBytes(ASCII))){
								PacswitchMessager.this.listeners.fireEvent(IEventListener.Type.NoRouteToServer);
								return;
							}							
						}
						catch(UnsupportedEncodingException e){ 
							EventArgs args=new EventArgs();
							args.put(K.EXCEPTION, e);
							PacswitchMessager.this.listeners.fireEvent(IEventListener.Type.AsyncException, args);
							return;
						}
					}
					else {
						PacswitchMessager.this.listeners.fireEvent(IEventListener.Type.InvalidOperation);
						return;
					}
					
					String res=mr.get(8500);
					if(res==null){
						EventArgs args=new EventArgs();
						args.put(K.TO, to);
						args.put(K.MSG, message);
						PacswitchMessager.this.listeners.fireEvent(IEventListener.Type.NoResponse, args);
						return;
					}
					else{
						EventArgs args=new EventArgs();
						args.put(K.TO, to);
						args.put(K.MSG, message);
						PacswitchMessager.this.listeners.fireEvent(IEventListener.Type.MessageSent, args);
					}
				} 
				finally{
					if(mr!=null)PacswitchMessager.this.msgtracker.remove(mr.tag);
				}
			}
		});
	}
	
	@Override
	public void close(){
		sendingPool.shutdown();
		super.close();
	}
	
	/**
	 * Send an untracked message without an ID.
	 * @param to Receiver ID
	 * @param message
	 * @return Whether message was sent successfully
	 */
	public final boolean sendSignal(String to,String message){
		try { return MessageProtocol.send(this,MessageProtocol.Type.Signal,to,message.getBytes(MessageProtocol.ENC),MessageProtocol.SANSID); } 
		catch (UnsupportedEncodingException e1) { return false; }
	}
	
	protected final void sendPing(String to,String response){
		try { MessageProtocol.send(this,MessageProtocol.Type.Ping,to,response.getBytes(MessageProtocol.ENC),MessageProtocol.SANSID); } 
		catch (UnsupportedEncodingException e1) { }
	}
	
	public final void sendPing(String to){
		try { MessageProtocol.send(this,MessageProtocol.Type.Ping,to,MessageProtocol.PingReq.getBytes(MessageProtocol.ENC),MessageProtocol.SANSID); } 
		catch (UnsupportedEncodingException e1) { }
	}
	
	@Deprecated
	public boolean lookup(String username) {
		Tracked<String> mr=new Tracked<String>();
		if(this.isAuthenticated()){
			String requestID=PacswitchProtocol.M_LOOKUP;
			mr.tag=requestID;
			msgtracker.put(requestID, mr);
			try{
				PacswitchProtocol.LOOKUP(this, username);
				String res=mr.get(5000);
				if(res!=null)return res.equals(OK);
				else return false;
			}
			catch(IOException e){ return false; }
			finally{ if(mr!=null)this.msgtracker.remove(mr.tag); }
		}
		else return false;
	}
	
	public void lookupAsync(final String username,final IEventListener callback){
		sendingPool.execute(new Runnable(){
			private void ret(Boolean r){
				EventArgs args=new EventArgs();
				args.put(K.RESULT, r);
				callback.run(args);
			}
			
			@Override
			public void run() {
				Tracked<String> mr=new Tracked<String>();
				if(isAuthenticated()){
					String requestID=PacswitchProtocol.M_LOOKUP;
					mr.tag=requestID;
					msgtracker.put(requestID, mr);
					try{
						PacswitchProtocol.LOOKUP(PacswitchMessager.this, username);
						String res=mr.get(5000);
						if(res!=null)this.ret(res.equals(OK));
						else this.ret(false);
					}
					catch(IOException e){ this.ret(false); }
					finally{ if(mr!=null)msgtracker.remove(mr.tag); }
				}
				else this.ret(false);
			}
		});
	}
	
	/**
	 * Send an untracked message without a target ID.
	 * @param to Receiver ID
	 * @param message 
	 * @param id Target ID. This is for message dispatching at the other side
	 * @return Whether message was sent successfully
	 */
	protected final boolean sendSignal(String to,String message, byte[] id){
		try { return MessageProtocol.send(this,MessageProtocol.Type.Signal,to,message.getBytes(MessageProtocol.ENC),id); } 
		catch (UnsupportedEncodingException e1) { return false; }
	}
	
	protected EventListeners listeners=new EventListeners();
	public void addEventListener(IEventListener listener){
		this.listeners.add(listener);
	}
	
	public void removeEventListener(IEventListener listener){
		this.listeners.remove(listener);
	}
	
	public int countEventListener(IEventListener.Type type){
		return this.listeners.countEvent(type);
	}

	/**
	 * Handle a message.
	 * @param from Sender ID
	 * @param message Message content
	 * @return Response to the message
	 */
	protected String handleMessage(String from, String message){
		EventArgs args=new EventArgs();
		args.put(K.FROM, from);
		args.put(K.MSG, message);
		this.listeners.fireEvent(IEventListener.Type.MessageReceived, args);
		Object ret=args.getRet();
		if(ret instanceof String)return (String)ret;
		else return "";
	}
	
	/**
	 * Handle an untracked message with an ID.
	 * @param from Sender ID
	 * @param id Target ID
	 * @param message
	 */
	protected void handleSignal(String from,String id, String message){
		EventArgs args=new EventArgs();
		args.put(K.FROM, from);
		args.put(K.ID, id);
		args.put(K.MSG, message);
		this.listeners.fireEvent(IEventListener.Type.SignalReceived, args);
	}
	
	/**
	 * Handle an untracked message without an ID
	 * @param from Sender ID
	 * @param message
	 */
	protected void handleSignal(String from, String message){
		EventArgs args=new EventArgs();
		args.put(K.FROM, from);
		args.put(K.MSG, message);
		this.listeners.fireEvent(IEventListener.Type.SignalReceived, args);
	}
	
	/**
	 * Get a standard ID
	 * @return A standard ID
	 */
	public static final String generateID(){
		return Tracker.generateID(MessageProtocol.RIDLEN, MessageTracker.RIDRANGE);
	}
	
	/**
	 * Message protocol implementation
	 */
	static class MessageProtocol extends PacswitchProtocol {
		/**
		 * Message encoding
		 */
		public static final String ENC="utf-8";
		
		public static final String PingReq="DEVICES";

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
			Type msgtype, String to,
			byte[] message, byte[] rId
		){
			if(!msgtype.isTracked()){
				if(msgtype.isResponse())return cli.pacSendData(to,Type.Response.getData(),rId,message);
				else if(cli.isAuthenticated())return cli.pacSendData(to,msgtype.getData(),rId,message);
				else return false;
			}
			else if(cli.isAuthenticated()) return cli.pacSendData(to,msgtype.getData(),rId,message);
			else return false;
		}
		
		/**
		 * Message type in message protocol
		 */
		static enum Type{
			/**
			 * Request message
			 */
			Request(1),
			
			/**
			 * Response message
			 */
			Response(0),
			
			/**
			 * Message of unknown type
			 */
			Unknown(-1),
			
			/**
			 * Stream request
			 */
			StreamReq(2),
			
			/**
			 * Stream response
			 */
			StreamRes(-2),
			
			/**
			 * Ordinary untracked message
			 */
			Signal(-128),
			
			/**
			 * Ping message
			 */
			Ping(-127)
			
			;

			private final byte code;
			private Type(int code){ this.code=(byte)code; }
			public final byte getByte(){ return code; }
			public final byte[] getData(){ return new byte[]{code}; }
			public final boolean isTracked(){ return code>0; }
			public final boolean isResponse(){ return code==0; }
			
			/**
			 * Convert byte code to MessageType
			 * @return MessageType object corresponding to the specified byte code
			 */
			public final static Type fromByte(byte code){
				switch(code){
					case 1: return Request; case 0: return Response;
					case 2: return StreamReq; case -2: return StreamRes;
					case -128: return Signal; case -127: return Ping; 
					default: return Unknown;
				}
			}
		}
	}
}

