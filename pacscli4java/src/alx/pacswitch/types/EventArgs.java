package alx.pacswitch.types;
public class EventArgs extends alx.utils.Dynamic<EventArgs.K> {
	private static final long serialVersionUID = 1L;
	public static enum K{ FROM,TO,ID,DEVICE,MSG,RESULT,EXCEPTION; }
}