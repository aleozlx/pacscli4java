package com.aleozlx.pacswitch;

public enum MessageType{
	Request(20),Response(0),StreamReq(30),StreamRes(31),
	Ping(32),Signal(-20),Unknown;
	private byte code;
	private MessageType(){ code=-1; }
	private MessageType(int code){ this.code=(byte)code; }
	public final byte getByte(){ return code; }
	public final byte[] getData(){ return new byte[]{code}; }
	public final boolean isTracked(){ return code>0; }
	public final boolean isResponse(){ return code==0; }
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