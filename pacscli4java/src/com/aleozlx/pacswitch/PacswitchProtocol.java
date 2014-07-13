// Fork me on GitHub! https://github.com/aleozlx/pacswitch
package com.aleozlx.pacswitch;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public final class PacswitchProtocol {
	/**
	 * Package start sequence
	 */
	public static final byte[] PACKAGE_START={5,65,76,88,80,65,67,83,86,82};

	/**
	 * Package end sequence
	 */
	public static final byte[] PACKAGE_END={23,67,69,83,84,70,73,78,73,4};

	/**
	 * Protocol method start sequence
	 */
	public static final byte[] PACKAGE_TEXT={2,40,84,88,84,80,65,67,41,10};
	
	public static final void AUTH(Socket s,String username,String password,String clienttype) throws IOException{	
		call(s,"AUTH",username,password,clienttype);
	}

	@Deprecated
	public static final void STREAM(Socket s,String id) throws IOException{
		call(s,"STREAM",id);
	}
	
	/**
	 * Call a protocol method.
	 * @param s Socket
	 * @param ss Protocol method and arguments
	 * @throws IOException
	 */
	private static final void call(Socket s,String ... ss) throws IOException{
		synchronized(s){
			OutputStream out=s.getOutputStream();
			out.write(PACKAGE_START);
			out.write(PACKAGE_TEXT);
			for(String str:ss){
				out.write(str.getBytes("ascii"));
				out.write(32);
			}
			out.write(PACKAGE_END);
		}
	}
	
	/**
	 * Send a data packet.
	 * @param s Socket
	 * @param recv Receiver ID
	 * @param buffer Data to be sent
	 * @throws IOException
	 */
	public static final void data(Socket s,String recv,byte[] ... buffer) throws IOException {
		synchronized(s){
			OutputStream os=s.getOutputStream(); 
			os.write(PACKAGE_START);
			os.write(recv.getBytes("ascii"));
			os.write(10);
			for(byte[] data:buffer)os.write(data);
			os.write(PACKAGE_END);
		}
	}
}
