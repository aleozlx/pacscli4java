package com.aleozlx.pacswitch.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Main {	
	public static void main(String[] args) {
		final Messager pm=new Messager();
		
		@SuppressWarnings("serial")
		LoginWindow lw=new LoginWindow(){
			@Override
			protected boolean check(String userid,String password){
				if(pm.connect(userid, password)&&pm.isAuthenticated()){
					CommWindow first=new CommWindow(pm);
					pm.handlers.add(first);
					first.setVisible(true);
					return true;
				}
				else return false; 
			}
		};
		lw.setVisible(true);

	}
	
	public static final DateFormat getDateFormat(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format;
	}
}


