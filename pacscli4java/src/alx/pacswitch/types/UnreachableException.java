package alx.pacswitch.types;

public class UnreachableException extends PacswitchException {
	private static final long serialVersionUID = 1L;
	public String target;
	public UnreachableException(String to,String errmsg){
		super(errmsg);
		this.target=to;
	}
}
