package alx.pacswitch.test;

import java.awt.Rectangle;
import java.awt.event.*;

import javax.swing.*;

import alx.pacswitch.types.*;

public class TextViewWindow extends ModernFrame implements ISignalHandler {
	private static final long serialVersionUID = 1L;
	private String id="";
	private String to="";
	Messager pm;
	private JScrollPane panOutput;
	private JTextArea txtOutput; 
	
	public TextViewWindow(Messager pm,String to,String id){
		this.to=to;
		this.id=id;
		this.pm=pm;
		
		txtOutput.addKeyListener(new KeyAdapter(){
			@Override
			public void keyReleased(KeyEvent e) { sendText(txtOutput.getText()); }});
		
		panOutput.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {sendScrollValue(panOutput.getVerticalScrollBar().getValue());}});
		
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent arg0) { sendCloseSig();close(false); }});
		this.getRootPane().addComponentListener(new ComponentAdapter() {
			@Override
            public void componentResized(ComponentEvent e) { sendResize();} });
	}


	public TextViewWindow(Messager pm,String id){
		this.id=id;
		this.pm=pm;
		
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent arg0) { close(true); }});
	}
	
	@Override
	protected void initializeComponents() {
		txtOutput=new JTextArea();
		panOutput=new JScrollPane(txtOutput);
		panOutput.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		panOutput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		layout(panOutput,"6 6 6 6");
		
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setBounds(300,200,400,500);
	}
	
	private void close(boolean incoming){
		if(incoming)pm.removeSignalHandler(id, this);
		this.dispose();
	}
	
	public static final String TVTYPE="txtv";
	
	private void send(String v,String m){ pm.send(to, id, TVTYPE, v, m); }
	private void sendText(String content){ send("t",content); }
	private void sendScrollValue(int val){ send("s",Integer.toString(val)); }
	private void sendCloseSig(){ send("c","."); }
	private void sendResize() {
		Rectangle sz=this.getBounds();
		int w=sz.width,h=sz.height;
		send("r",String.format("%1$d,%2$d", w,h));
	}

	@Override
	public void handleSignal(final String from, final String message) {
		final int ii=message.indexOf("+");
		final String header=message.substring(0, ii);
		final String[] args=header.split(" ");
		SwingUtilities.invokeLater(new Runnable(){
			@Override
		    public void run() { update(message, ii, args); }
		});
	}


	private void update(String message, int ii, String[] args) {
		if(args.length==2&&args[0].equals(TVTYPE)){
			String content=message.substring(ii+1);
			if(args[1].equals("t"))
				synchronized(txtOutput){ txtOutput.setText(content); }
			else if(args[1].equals("s")){
				try{ panOutput.getVerticalScrollBar().setValue(Integer.parseInt(content)); }
				catch(NullPointerException e){ }
			}
			else if(args[1].equals("r")){
				String[] sz=content.split(",");
				int w=Integer.parseInt(sz[0]),h=Integer.parseInt(sz[1]);
				Rectangle tsz=this.getBounds();
				this.setBounds(tsz.x, tsz.y, w, h);
			}
			else close(true);
		}
	}
}
