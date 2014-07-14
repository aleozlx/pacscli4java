package com.aleozlx.pacswitch;

import java.net.Socket;

/**
 * The platform independent pacswitch service client API,
 * which is used to facilitate WAN connections by communicating
 * with a pacswitch server, thereby user data are switched
 * so as to pass NAT routers both ways. Here are the standards
 * of the lower-level API.
 * 
 * Fork me on github:
 * https://github.com/aleozlx/pacscli4java
 * 
 * And pay a visit to the server project:
 * https://github.com/aleozlx/pacswitch
 * 
 * @author Alex
 * @since May, 2014
 */
interface IPacswitchAPI{	
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
	 * @throws InterruptedException
	 */
	void pacLoop() throws InterruptedException;
	
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
}

/**
 * PacswitchAbstractClient
 * @author Alex
 */
abstract class PacswitchAbstractClient implements IPacswitchAPI {
	/**
	 * The socket from which I/O streams can be fetched,
	 * also is necessary for protocol implementation.
	 * @return The socket from which I/O streams can be fetched
	 */
	abstract Socket getSocket();
}
