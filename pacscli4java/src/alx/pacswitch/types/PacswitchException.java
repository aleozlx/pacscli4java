package alx.pacswitch.types;
public class PacswitchException extends Exception {
	private static final long serialVersionUID = 1L;
	public PacswitchException(){ super(); }
	public PacswitchException(String message){ super(message); }
	public PacswitchException(String message, Throwable cause){ super(message,cause); }
}