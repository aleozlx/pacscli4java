package alx.pacswitch;

import java.net.Socket;

/**
 * The platform independent pacswitch service client API,
 * which is used to facilitate WAN connections by communicating
 * with a pacswitch server, thereby user data are switched
 * so as to pass NAT routers both ways. Here are the standards
 * of the lower-level API.<br/><br/>
 * 
 * Fork me on github: <br/>
 * <a href="https://github.com/aleozlx/pacscli4java">https://github.com/aleozlx/pacscli4java</a><br/>
 * 
 * And feel free to pay a visit to the server project:<br/>
 * <a href="https://github.com/aleozlx/pacswitch">https://github.com/aleozlx/pacswitch</a><br/>
 * 
 * @author Alex
 * @since May 20, 2014
 */
public interface PacswitchAPI{	
	/**
	 * Package start sequence: (dec) 5,65,76,88,80,65,67,83,86,82
	 */
	static final byte[] PACKAGE_START={5,65,76,88,80,65,67,83,86,82};

	/**
	 * Package end sequence: (dec) 23,67,69,83,84,70,73,78,73,4
	 */
	static final byte[] PACKAGE_END={23,67,69,83,84,70,73,78,73,4};

	/**
	 * Protocol method start sequence: (dec) 2,40,84,88,84,80,65,67,41,10
	 */
	static final byte[] PACKAGE_TEXT={2,40,84,88,84,80,65,67,41,10};
	
	/**
	 * Literal new line
	 */
	static final byte[] NEWLINE={10};

	/**
	 * Sender separator
	 */
	static final byte[] SENDER_SEP={62,32};
	
	/**
	 * Protocol encoding: ASCII
	 */
	static final String ASCII="ascii";
	
	/**
	 * Constant string "OK"
	 */
	static final String OK="OK";
	
	/**
	 * Close the connection permanently.
	 */
	void pacClose();
	
	/**
	 * Send a package end sequence
	 */
	void pacEnd();
	
	/**
	 * Initiate a connection.
	 * @param user User ID
	 * @param password Password
	 * @param host Server IP Address
	 * @param clienttype A unique string that distinguishes different kind of clients
	 * @return Whether a connection is successfully made.
	 */
	boolean pacInit(String user,String password,String host,String clienttype);
	
	/**
	 * Start the event loop for response data.
	 */
	void pacLoop();
	
	/**
	 * Send user data, which will be automatically wrapped in a packet.
	 * @param recv Receiver ID
	 * @param buffer User data
	 * @return Whether the packet is successfully sent after, if necessary, multiple retries.
	 */
	boolean pacSendData(String recv,byte[] ... buffer);
	
	/**
	 * Get the file descriptor of the socket
	 * @return The file descriptor of the socket
	 */
	int pacSocketno();
	
	/**
	 * Send a package start sequence
	 * @param recv Receiver ID
	 */
	void pacStart(String recv);
	
	/**
	 * The socket from which I/O streams can be fetched,
	 * also is necessary for protocol implementation.
	 * This is not actually part of pacswitch API standard
	 * @return The socket from which I/O streams can be fetched
	 */
	Socket getSocket();
}
