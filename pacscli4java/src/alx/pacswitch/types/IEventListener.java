package alx.pacswitch.types;
public interface IEventListener extends alx.utils.EventListener<IEventListener.Type,EventArgs>{
	public static enum Type{
		Undefined, PingEcho, AuthenticationEvent,
		MessageReceived, SignalReceived, PendingCountChanged,
		MessageSending, MessageSent, MessageAllowed, MessageBlocked, SignalHandlerMissing,
		NoRouteToServer, NoResponse, InvalidOperation, AsyncException
	}
}