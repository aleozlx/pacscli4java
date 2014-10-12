package alx.pacswitch.test;

import java.awt.event.*;
import javax.swing.*;

public abstract class LoginWindow extends ModernFrame {
	private static final long serialVersionUID = -2801374118636568178L;
	private JTextField txtUser;
	private JPasswordField txtPasswd;
	private JButton btnOK;
	
	public LoginWindow(){
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setBounds(300,300,300,200);
		this.setTitle("Login");	
	}
	
	@Override
	protected void initializeComponents(){
		txtUser=new JTextField();
		txtPasswd=new JPasswordField();
		btnOK=new JButton("OK");
		
		layout(new JLabel("User ID"),"20 auto auto 10",
			new JLabel("Password"),"50 auto auto 10",
			txtUser,"20 10 auto 90",
			txtPasswd,"50 10 auto 90",
			btnOK,"auto 10 10 auto");
		
		btnOK.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				onBtnOKClicked();}});
	}
	
	private void onBtnOKClicked(){
		if(!check(txtUser.getText(),new String(txtPasswd.getPassword())))
			JOptionPane.showMessageDialog(this, "Wrong ID or password");
		else dispose();
	}
	protected abstract boolean check(String userid,String password);
}	
