package alx.pacswitch.test;
//import java.awt.Toolkit;
//import java.awt.datatransfer.StringSelection;
import java.io.UnsupportedEncodingException;
//import java.lang.reflect.InvocationTargetException;
import java.util.Date;

//import javax.swing.SwingUtilities;






import alx.pacswitch.GatewayMessager;
import alx.pacswitch.types.IEventListener;
import alx.utils.Dynamic;

public class Messager extends GatewayMessager{
	public static final String HOST="222.69.93.107";
	public static final String CLIENTTYPE="messager1.0";
	
	public Messager(){
		this.addEventListener(new IEventListener(){
			@Override
			public Type getType() { return Type.MessageAllowed; }

			@Override
			public void run(Dynamic args) {
				String from=(String)args.get(K_FROM);
				String message=(String)args.get(K_MSG);
				String output=format(from,message,true);
				new Storage(Messager.this.getUserID()).msgHistory.write(from, output);
			}	
		});
		
		this.addEventListener(new IEventListener(){
			@Override
			public Type getType() { return Type.MessageSending; }

			@Override
			public void run(Dynamic args) {
				String to=(String)args.get(K_TO);
				String message=(String)args.get(K_MSG);
				String output=format(to,message,false);
				new Storage(Messager.this.getUserID()).msgHistory.write(to, output);
			}	
		});
	}
	
	public boolean connect(String userid,String password){
		return connect(userid, password, null, HOST,CLIENTTYPE);
	}
	
//	@Override
//	protected void onMessageAllowed(String from, String message) {
//		String output=format(from,message,true);
//		new Storage(this.getUserID()).msgHistory.write(from, output);
//		
//	}
	
//	@Override
//	protected void onMessageSending(String to, String message) {
//		String output=format(to,message,false);
//		new Storage(this.getUserID()).msgHistory.write(to, output);
//	}
	
//	@Override
//	protected void onMessageBlocked(String from, String message) { }
	
	private String format(String with,String message,boolean isIncoming){
		return String.format("%1$s %3$s: %2$s\n",
				(isIncoming?with:this.getUserID()),
				message.trim(),
				Main.getDateFormat().format(new Date()));
	}
	
//	@Override
//	protected void onUntrackedMessageHandlerMissing(final String from,final String id,final String message){
//		try {
//			SwingUtilities.invokeAndWait(new Runnable() {
//				@Override
//			    public void run() { newTextViewWindow(id); }
//			});
//		} catch (InvocationTargetException | InterruptedException e) { }
//	}
	
//	private void newTextViewWindow(final String id){
//		TextViewWindow tvw=new TextViewWindow(this,id);
//		untrackedHandlers.add(id, tvw);
//		tvw.setVisible(true);
//	}
	
	/**
	 * Specifies the untracked message (with a target ID) protocol
	 * @param to Receiver ID
	 * @param id Target ID
	 * @param type Custom message type (supposed to correspond to different handlers)
	 * @param func Functionality (to perform different actions)
	 * @param msg Message content
	 */
	public void send(String to,String id,String type,String func,String msg){
		try { 
			sendSignal(
				to, String.format("%1$s %2$s+%3$s", type,func,msg), 
				id.getBytes(ASCII)); 
		} 
		catch (UnsupportedEncodingException e) { }
	}
	
	/**
	 * Specifies the untracked message protocol
	 * @param to Receiver ID
	 * @param type Custom message type (supposed to correspond to different handlers)
	 * @param func Functionality (to perform different actions)
	 * @param msg Message content
	 */
	public void send(String to,String type,String func,String msg){
		sendSignal(
			to, String.format("%1$s %2$s+%3$s", type,func,msg)); 
	}

//	@Override
//	protected void handleSignal(String from, String message) {
//		int ii=message.indexOf("+");
//		String header=message.substring(0, ii);
//		String[] args=header.split(" ");
//		if(args.length==2&&args[0].equals("clipboard")){
//			if(args[1].equals("copy")){
//				String content=message.substring(ii+1);
//				StringSelection stringSelection = new StringSelection(content);
//				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
//			}
//		}
//	}
}