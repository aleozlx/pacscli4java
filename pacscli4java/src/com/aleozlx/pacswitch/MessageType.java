package com.aleozlx.pacswitch;

/**
 * Message type in message protocol
 */
public enum MessageType{
	/**
	 * Request message
	 */
	Request(20),
	
	/**
	 * Response message
	 */
	Response(0),
	
	/**
	 * Stream request
	 */
	StreamReq(30),
	
	/**
	 * Stream response
	 */
	StreamRes(31),
	
	/**
	 * Ping message
	 */
	Ping(32),
	
	/**
	 * Ordinary untracked message
	 */
	Signal(-20),
	
	/**
	 * Message of unknown type
	 */
	Unknown;
	

	private byte code;
	private MessageType(){ code=-1; }
	private MessageType(int code){ this.code=(byte)code; }
	public final byte getByte(){ return code; }
	public final byte[] getData(){ return new byte[]{code}; }
	public final boolean isTracked(){ return code>0; }
	public final boolean isResponse(){ return code==0; }
	
	/**
	 * Convert byte code to MessageType
	 * @return MessageType object corresponding to the specified byte code
	 */
	public final static MessageType fromByte(byte code){
		switch(code){
			case 20: return Request; case 0: return Response;
			case 30: return StreamReq; case 31: return StreamRes;
			case 32: return Ping; 
			case -20: return Signal;
			default: return Unknown;
		}
	}
}