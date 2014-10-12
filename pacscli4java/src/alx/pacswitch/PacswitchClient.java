package alx.pacswitch;
import java.io.*;
import java.net.*;

import alx.pacswitch.types.*;
import alx.utils.Synchronizer;

/**
 * Pacswitch client implementation<br/><br/>
 * 
 * Fork me on github: <br/>
 * <a href="https://github.com/aleozlx/pacscli4java">https://github.com/aleozlx/pacscli4java</a><br/>
 * 
 * And feel free to pay a visit to the server project:<br/>
 * <a href="https://github.com/aleozlx/pacswitch">https://github.com/aleozlx/pacswitch</a><br/>
 * 
 * @author Alex
 * @version 1.3.2
 * @since June 3, 2014
 */
public abstract class PacswitchClient implements PacswitchAPI {
	public static final int MAX_SEND_TRIES = 3;

	/**
	 * Port
	 */
	protected int port=3512;

	/**
	 * Data buffer
	 */
	protected Pacbuffer mybuffer=new Pacbuffer();

	/**
	 * User ID
	 */
	protected String user;

	/**
	 * User password
	 */
	protected String password;

	/**
	 * Host IP address
	 */
	protected String host;

	/**
	 * Client type
	 */
	protected String clienttype;

	/**
	 * Enable auto-reconnection
	 */
	public volatile boolean autoReconnect=true;

	/**
	 * Flag for the unique receive event loop
	 */
	private boolean loopStarted=false;

	/**
	 * Socket object
	 */
	Socket socket;
	
	@Override
	public Socket getSocket(){ return socket; }

	@Override
	public final boolean pacInit(String user,String password,String host,String clienttype){
		try{
			this.closeSocket();
			socket=new Socket(host,port);
			this.user=user;
			this.password=password;
			this.host=host;
			this.clienttype=clienttype;
			PacswitchProtocol.AUTH(this,user,password,clienttype);
		}
		catch(IOException e){ return false; }
		return true;
	}

	/**
	 * Reconnect to server.
	 * @return Whether a reconnection is successfully made after, if necessary, multiple retries.
	 */
	protected boolean reconnect(){
		while(autoReconnect){
			this.closeSocket();
			try{ 
				socket=new Socket(host,port);
				PacswitchProtocol.AUTH(this,user,password,clienttype);
				return true;
			}
			catch(IOException e){ Synchronizer.wait(800); }
		}
		return false;
	} 

	@Override
	public final boolean pacSendData(String recv,byte[] ... buffer) {
		for(int tries=0;tries<MAX_SEND_TRIES;tries++){
			try{ PacswitchProtocol.data(this,recv,buffer); return true; }
			catch(IOException e){ Synchronizer.wait(2000); }
		}
		return false;
	}
	
	public static final String SERVER_SIGNATURE="pacswitch";

	@Override
	public final void pacLoop(){
		byte[] _mybuffer=new byte[2048]; int sz_mybuffer,iI,iII,iIII;
		this.loopStarted=true;
		do{
			while(mybuffer.size!=0
				&&(iI=mybuffer.find(PACKAGE_START))!=-1
				&&(iII=mybuffer.find(PACKAGE_END))!=-1
			){
				iIII=mybuffer.find(SENDER_SEP,iI+PACKAGE_START.length);
				String sender=new String(mybuffer.buffer,
						iI+PACKAGE_START.length,
						iIII-(iI+PACKAGE_START.length));
				byte[] data=new byte[iII-(iIII+SENDER_SEP.length)];
				System.arraycopy(mybuffer.buffer,iIII+SENDER_SEP.length,data,0,data.length);
				if(sender.equals(SERVER_SIGNATURE)){
					try{ 
						final String TSEP=": "; String msg=new String(data,ASCII);
						int ii=msg.indexOf(TSEP);
						onServerResponse(msg.substring(0,ii),msg.substring(ii+TSEP.length()));
					}
					catch(UnsupportedEncodingException ee){ }
				}
				else onDataReceived(sender,data); 
				iII+=PACKAGE_END.length;
				System.arraycopy(mybuffer.buffer,iII,mybuffer.buffer,0,mybuffer.size-=iII);
			}
			try{
				InputStream is=socket.getInputStream();
				sz_mybuffer=is.read(_mybuffer);
				if(sz_mybuffer<=0)throw new IOException("Connection lost");
				else if(sz_mybuffer>0&&sz_mybuffer+mybuffer.size<Pacbuffer.SZ_BUFFER-1){
					System.arraycopy(_mybuffer,0,mybuffer.buffer,mybuffer.size,sz_mybuffer);
					mybuffer.size+=sz_mybuffer;
				}
				else sz_mybuffer=0;
			}
			catch(IOException e){ 
				try { Thread.sleep(600); } 
				catch (InterruptedException e1) { } 
				if(!reconnect()) break;
			}
		} while(true);
	}
	
	public static final String T_PACSWITCH_RECV="Pacswitch recv";

	/**
	 * Start an event loop asynchronously for response data.
	 */
	public void start(){
		if(!this.loopStarted){
			new Thread(T_PACSWITCH_RECV){
				@Override
				public void run(){ pacLoop(); }
			}.start();
		}
	}

	/**
	 * Implement this to handle data received.
	 * @param sender Sender ID
	 * @param buffer User data
	 */
	protected abstract void onDataReceived(String sender,byte[] buffer);

	/**
	 * Override this to handle server response messages
	 * @param title Server response title
	 * @param msg Server response message
	 */
	protected abstract void onServerResponse(String title, String msg);

	/**
	 * Close the connection permanently.
	 */
	public void close(){ 
		this.autoReconnect=false;
		this.closeSocket(); 
	}

	/**
	 * Close the socket.
	 */
	protected final void closeSocket(){
		try{ if(socket!=null)socket.close(); }
		catch(Exception e){ }
	}

	@Override @Deprecated
	public void pacClose(){ this.close(); }

	/**
	 * Get the file descriptor of the socket
	 * This is not actually implemented since it's considered unnecessary.
	 */
	@Override @Deprecated
	public int pacSocketno(){ throw new Error(); }

	/**
	 * Send a package start sequence.
	 * This is not actually implemented since it's both inefficient and unnecessary.
	 */
	@Override @Deprecated
	public void pacStart(String recv){ throw new Error(); }

	/**
	 * Send a package end sequence.
	 * This is not actually implemented since it's both inefficient and unnecessary.
	 */
	@Override @Deprecated
	public void pacEnd(){ throw new Error(); }
	
	/**
	 * Pacswitch protocol implementation
	 */
	public static class PacswitchProtocol {
		public static final String M_AUTH="AUTH";
		public static final String M_STREAM="STREAM";
		public static final String M_LOOKUP="LOOKUP";
		public static final String M_LOGIN="LOGIN";
		public static final String M_POINTER="POINTER";
		public static final String M_REGISTER="REGISTER";
		public static final String M_TEST="TEST";
		public static final String M_PASSWD="PASSWD";

		public static enum SvrResponseType{
			STREAM(M_STREAM),LOOKUP(M_LOOKUP),LOGIN(M_LOGIN),AUTH(M_AUTH),TEST(M_TEST),
			REGISTER(M_REGISTER),PASSWD(M_PASSWD),POINTER(M_POINTER),UNKNOWN("");
			private final String text;
			private SvrResponseType(String text){ this.text=text; }
			public String getText(){ return this.text; }
			public final static SvrResponseType fromString(String text){
				if(text.equals(M_AUTH))return AUTH;
				else if(text.equals(M_STREAM))return STREAM;
				else if(text.equals(M_LOOKUP))return LOOKUP;
				else if(text.equals(M_LOGIN))return LOGIN;
				else if(text.equals(M_TEST))return TEST;
				else if(text.equals(M_REGISTER))return REGISTER;
				else if(text.equals(M_PASSWD))return PASSWD;
				else if(text.equals(M_POINTER))return POINTER;
				else return UNKNOWN;
			}
		}
		
		/**
		 * Request for authentication
		 * @param cli Abstract client
		 * @param username User ID
		 * @param password Password
		 * @param clienttype A unique string that distinguishes different kind of clients
		 * @throws IOException
		 */
		public static final void AUTH(PacswitchAPI cli,String username,String password,String clienttype) throws IOException{	
			call(cli,M_AUTH,username,password,clienttype);
		}

		/**
		 * Request for a separate stream
		 * @param cli Abstract client
		 * @param id 
		 * @throws IOException
		 */
		@Deprecated
		public static final void STREAM(PacswitchAPI cli,String id) throws IOException{
			call(cli,M_STREAM,id);
		}
		
		/**
		 * Look up to see if the user exists
		 * @param cli Abstract client
		 * @param id 
		 * @throws IOException
		 */
		public static final void LOOKUP(PacswitchAPI cli,String id) throws IOException{
			call(cli,M_LOOKUP,id);
		}
		
		/**
		 * Call a protocol method.
		 * @param cli Client
		 * @param ss Protocol method and arguments
		 * @throws IOException When data cannot be sent due to network issues
		 */
		public static final void call(PacswitchAPI cli,String ... ss) throws IOException{
			Socket s=cli.getSocket();
			synchronized(s){
				OutputStream out=s.getOutputStream();
				out.write(PACKAGE_START);
				out.write(PACKAGE_TEXT);
				for(String str:ss){
					out.write(str.getBytes(ASCII));
					out.write(32);
				}
				out.write(PACKAGE_END);
			}
		}
		
		/**
		 * Send a data packet.
		 * @param cli Client
		 * @param recv Receiver ID
		 * @param buffer Data to be sent
		 * @throws IOException When data cannot be sent due to network issues
		 */
		public static final void data(PacswitchAPI cli,String recv,byte[] ... buffer) throws IOException {
			Socket s=cli.getSocket();
			synchronized(s){
				OutputStream os=s.getOutputStream(); 
				os.write(PACKAGE_START);
				os.write(recv.getBytes(ASCII));
				os.write(10);
				for(byte[] data:buffer)os.write(data);
				os.write(PACKAGE_END);
			}
		}
	}
}
