package alx.pacswitch.test;

import java.text.*;

public class Main {	
	public static void main(String[] args) {
		final Messager pm=new Messager();
	
		new LoginWindow(){
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean check(String userid,String password){
				if(pm.connect(userid, password)&&pm.isAuthenticated()){
					CommWindow first=new CommWindow(pm);
					first.setVisible(true);
					return true;
				}
				else return false; 
			}
		}.setVisible(true); 
	}

	public static final DateFormat getDateFormat(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format;
	}
}


