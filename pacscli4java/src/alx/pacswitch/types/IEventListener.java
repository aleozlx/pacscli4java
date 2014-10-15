package alx.pacswitch.types;

import alx.utils.Dynamic;

public interface IEventListener{
	public enum Type{
		Undefined, PingEcho, AuthenticationEvent,
		MessageReceived, SignalReceived, PendingCountChanged,
		MessageSending, MessageSent, MessageAllowed, MessageBlocked, SignalHandlerMissing,
		NoRouteToServer, NoResponse, InvalidOperation, AsyncException
	}
	
	public static final String K_FROM="from";
	public static final String K_TO="to";
	public static final String K_ID="id";
	public static final String K_DEVICE="device";
	public static final String K_MSG="message";
	public static final String K_RESULT="result";
	public static final String K_EXCEPTION="exception";
	
	Type getType();
	void run(Dynamic args);
}
