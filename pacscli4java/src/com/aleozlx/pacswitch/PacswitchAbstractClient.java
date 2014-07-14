package com.aleozlx.pacswitch;

import java.net.Socket;

interface IPacswitchAPI{	
	/**
	 * Package start sequence
	 */
	static final byte[] PACKAGE_START={5,65,76,88,80,65,67,83,86,82};

	/**
	 * Package end sequence
	 */
	static final byte[] PACKAGE_END={23,67,69,83,84,70,73,78,73,4};

	/**
	 * Protocol method start sequence
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
	 * Encoding
	 */
	static final String ASCII="ascii";
	
	void pacClose();
	void pacEnd();
	boolean pacInit(String user,String password,String host,String clienttype);
	void pacLoop() throws InterruptedException;
	boolean pacSendData(String recv,byte[] ... buffer);
	int pacSocketno();
	void pacStart(String recv);
}

abstract class PacswitchAbstractClient implements IPacswitchAPI {
	abstract Socket getSocket();
}
