package com.aleozlx.pacswitch.test;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import com.aleozlx.pacswitch.*;

public class CommWindow extends ModernFrame implements IMessageHandler {
	private static final long serialVersionUID = -5725770541408790450L;
	private final GatewayMessager pm;
	private final Storage storage;
	private String target="";
	private JMenu mnFriend,mnSend,mnWindow; 
	private JMenuItem miViewNew,miViewExit,miSendClipboard,miSendTextView;
	private JScrollPane panOutput;
	private JTextField txtInput;
	private JTextArea txtOutput;
	private JButton btnOK;
	private Map<String,JMenuItem> friendsmap;
	
	public CommWindow(GatewayMessager messager){
		this.pm=messager;
		this.storage=new Storage(pm.getUserID());
		this.loadFriends();
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent arg0) {
				if(pm.handlers.size()==1)confirmExit(); else onClose();}});
		this.setBounds(300,200,400,500);
		setTarget(null);
	}

	private void loadFriends() {
		List<String> friends=storage.friends.get();
		this.friendsmap=new HashMap<String,JMenuItem>();
		for(final String friend:friends){
			JMenuItem miFriend=new JMenuItem(friend);
			miFriend.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					setTarget(friend);}});
			this.friendsmap.put(friend,miFriend);
			menuItem(mnFriend,miFriend);
		}
	}
	
	@Override
	protected void initializeComponents(){
		mnFriend=new JMenu("Friend");
		mnSend=new JMenu("Send");
		mnWindow=new JMenu("Window"); 
		miViewNew=new JMenuItem("New window");
		miViewExit=new JMenuItem("Exit Application");
		miSendClipboard=new JMenuItem("Clipboard");
		miSendTextView=new JMenuItem("TextView");
		txtInput=new JTextField();
		txtOutput=new JTextArea();
		panOutput=new JScrollPane(txtOutput);
		btnOK=new JButton("OK");
		
		layout(txtInput,"auto 50 10 10",
				panOutput,"10 10 48 10",
				btnOK,"auto 10 10 auto");	
		
		txtOutput.setLineWrap(true);
		menuItem(mnSend,miSendClipboard,miSendTextView);
		menuItem(mnWindow,miViewNew,miViewExit);
		menu(mnFriend,mnSend,mnWindow);
		miViewNew.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onNewWindow();}});
		miViewExit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				confirmExit();}});
		ActionListener alSend=new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				onSend();}};
		btnOK.addActionListener(alSend);
		txtInput.addActionListener(alSend);
	}
	
	protected void setTarget(String name){
		if(name!=null&&!name.equals("")){
			setTitle("To "+name);
			target=name;
			loadHistoryMessages(name);
			loadPendingMessages(name);
		}
		else {
			setTitle("To <nobody>");
			target="";
			txtOutput.setText("");
		}
		scrollToEnd();
	}

	private void loadPendingMessages(String name) {
		if(pm.inbox.count(name)>0){
			LinkedList<PendingMessage> msgs=pm.inbox.get(name);
			for(PendingMessage msg:msgs)
				print(msg.getFrom(),msg.getMessage(),msg.getTime());
			msgs.clear();
			pm.notifyPendingCounts();
		}
	}

	private void scrollToEnd() {
		JScrollBar sb=panOutput.getVerticalScrollBar();
		sb.setValue(sb.getMaximum());
	}

	private void loadHistoryMessages(String name) {
		List<String> history=storage.msgHistory.read(name);
		txtOutput.setText("");
		for(String line:history){
			txtOutput.append(line);
			txtOutput.append("\n");
		}
	}
	
	private void print(String who,String message){ print(who,message,null); }
	private void print(String who,String message,Date d){
		String output=String.format("%1$s %3$s: %2$s\n",
			who,message.trim(),Main.getDateFormat().format(d==null?new Date():d));
		txtOutput.append(output);
		scrollToEnd();
	}

	@Override
	public String handleMessage(String from, String message) {
		if(target.equals(from)){
			print(from,message);
			return "ACK";
		}
		else return null;
	}
	
	@Override
	public void refreshPendingCounts(Map<String,Integer> ct){
		for(String friend:friendsmap.keySet()){
			JMenuItem fmi=friendsmap.get(friend);
			if(ct.containsKey(friend)){
				fmi.setText(String.format("%1$s (%2$d)", friend,ct.get(friend)));
			}
			else fmi.setText(friend);			
		}
	}
	
	private void onSend(){
		final String msg=txtInput.getText();
		txtInput.setText("");
		if(msg.trim().equals(""))return;
		if(!target.equals("")){
			new Thread(){
				@Override
				public void run(){
					try {
						print(pm.getUserID(),msg);
						if(!pm.send(target, msg).equals("ACK"))
							print("system","No response");
					} 
					catch (PacswitchException e) { print("system",target+" offline."); }
				}
			}.start();
		}
	}
	private void onNewWindow() {
		CommWindow cw=new CommWindow(pm);
		pm.handlers.add(cw);
		cw.setVisible(true);
		cw.setTarget(this.target);
	}
	private void onClose(){
		setVisible(false);
		dispose();
		pm.handlers.remove(this);
	}
	private void confirmExit(){
		if (JOptionPane.showConfirmDialog(this,
				"Are you sure to exit this app?", 
				"Exit", 
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) 
				== JOptionPane.YES_OPTION)
			onExit();
	}
	private void onExit(){
		pm.close();
		System.exit(0);
	}
}