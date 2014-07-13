// Fork me on GitHub! https://github.com/aleozlx/pacswitch
package com.aleozlx.pacswitch;
import java.io.*;
import java.net.*;

/**
 * Pacswitch client
 * @author Alex
 * @version 1.2.2
 * @since June 3, 2014
 */
public abstract class PacswitchClient {
	/**
	 * Literal new line
	 */
	public static final byte[] NEWLINE={10};

	/**
	 * Sender separator
	 */
	public static final byte[] SENDER_SEP={62,32};
	
	/**
	 * Encoding
	 */
	public static final String ASCII="ascii";
	
	/**
	 * Port
	 */
	protected int port=3512;

	/**
	 * Data buffer
	 */
	protected Mybuffer mybuffer=new Mybuffer();

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
	public boolean autoReconnect=true;

	/**
	 * Flag for the unique receive event loop
	 */
	private boolean loopStarted=false;

	/**
	 * Socket object
	 */
	Socket socket;

	/**
	 * Initiate a connection.
	 * @param user User ID
	 * @param password Password
	 * @param host Host IP Address
	 * @param clienttype A unique string that distinguishes different kind of clients
	 * @return Whether a connection is sucessfully made.
	 */
	public final boolean pacInit(String user,String password,String host,String clienttype){
		try{
			this.closeSocket();
			socket=new Socket(host,port);
			this.user=user;
			this.password=password;
			this.host=host;
			this.clienttype=clienttype;
			PacswitchProtocol.AUTH(socket,user,password,clienttype);
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
				PacswitchProtocol.AUTH(socket,user,password,clienttype);
				return true;
			}
			catch(IOException e){ Synchronizer.wait(800); }
		}
		return false;
	} 

	/**
	 * Send user data, which will be automatically wrapped in a packet.
	 * @param buffer User data
	 * @param recv Receiver ID
	 * @return Whether the packet is successfully sent after, if necessary, multiple retries.
	 */
	public final boolean pacSendData(String recv,byte[] ... buffer) {
		for(int tries=0;tries<3;tries++){
			try{ PacswitchProtocol.data(socket,recv,buffer); return true; }
			catch(IOException e){ Synchronizer.wait(2000); }
		}
		return false;
	}

	/**
	 * Start the event loop for response data.
	 * @throws InterruptedException
	 */
	public final void pacLoop() throws InterruptedException{
		byte[] _mybuffer=new byte[2048]; int sz_mybuffer,iI,iII,iIII;
		this.loopStarted=true;
		do{
			while(mybuffer.size!=0
				&&(iI=mybuffer.find(PacswitchProtocol.PACKAGE_START))!=-1
				&&(iII=mybuffer.find(PacswitchProtocol.PACKAGE_END))!=-1
			){
				iIII=mybuffer.find(SENDER_SEP,iI+PacswitchProtocol.PACKAGE_START.length);
				String sender=new String(mybuffer.buffer,
						iI+PacswitchProtocol.PACKAGE_START.length,
						iIII-(iI+PacswitchProtocol.PACKAGE_START.length));
				byte[] data=new byte[iII-(iIII+SENDER_SEP.length)];
				System.arraycopy(mybuffer.buffer,iIII+SENDER_SEP.length,data,0,data.length);
				if(sender.equals("pacswitch")){
					try{ 
						final String TSEP=": "; String msg=new String(data,ASCII);
						int ii=msg.indexOf(TSEP);
						onServerResponse(msg.substring(0,ii),msg.substring(ii+TSEP.length()));
					}
					catch(UnsupportedEncodingException ee){ }
				}
				else onDataReceived(sender,data); 
				iII+=PacswitchProtocol.PACKAGE_END.length;
				System.arraycopy(mybuffer.buffer,iII,mybuffer.buffer,0,mybuffer.size-=iII);
			}
			try{
				InputStream is=socket.getInputStream();
				sz_mybuffer=is.read(_mybuffer);
				if(sz_mybuffer<=0)throw new IOException("Connection lost");
				else if(sz_mybuffer>0&&sz_mybuffer+mybuffer.size<Mybuffer.SZ_BUFFER-1){
					System.arraycopy(_mybuffer,0,mybuffer.buffer,mybuffer.size,sz_mybuffer);
					mybuffer.size+=sz_mybuffer;
				}
				else sz_mybuffer=0;
			}
			catch(IOException e){ 
				Thread.sleep(600); 
				if(!reconnect()) break;
			}
		} while(true);
	}

	/**
	 * Start an event loop asynchronously for response data.
	 */
	public void start(){
		if(!this.loopStarted){
			new Thread(){
				@Override
				public void run(){
					try{ pacLoop(); }
					catch(InterruptedException e){ this.interrupt(); }
				}
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
	protected void onServerResponse(String title, String msg){ }

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

	/**
	 * Close the connection permanently.
	 */
	@Deprecated
	public void pacClose(){ this.close(); }

	/**
	 * Get the file discriptor of the socket
	 * @return The file discriptor of the socket
	 */
	@Deprecated
	public int pacSocketno(){ throw new Error("Not implemented"); }

	/**
	 * Send a package start sequence
	 * @param recv Receiver ID
	 */
	@Deprecated
	public void pacStart(String recv){ throw new Error("Not implemented"); }

	/**
	 * Send a package end sequence
	 */
	@Deprecated
	public void pacEnd(){ throw new Error("Not implemented"); }
}
