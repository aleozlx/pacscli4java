package com.aleozlx.pacswitch.test;

import javax.swing.*;

import java.awt.Component;
import java.awt.Dimension;

public abstract class ModernFrame extends JFrame {
	private static final long serialVersionUID = -9001603519181428664L;
	public JPanel panContent=new JPanel();
	public SpringLayout layout=new SpringLayout();
	public ModernFrame(){
		try{ UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
		catch(Exception e){ }
		panContent.setLayout(layout);
		this.add(panContent);
		initializeComponents();
	}
	
	protected abstract void initializeComponents();

	protected final void setMargin(Component control,Long top,Long right,Long bottom,Long left){
		if(top!=null)layout.putConstraint(SpringLayout.NORTH,control,top.intValue(),SpringLayout.NORTH,panContent);
		if(right!=null)layout.putConstraint(SpringLayout.EAST,control,-right.intValue(),SpringLayout.EAST,panContent);
		if(bottom!=null)layout.putConstraint(SpringLayout.SOUTH,control,-bottom.intValue(),SpringLayout.SOUTH,panContent);
		if(left!=null)layout.putConstraint(SpringLayout.WEST,control,left.intValue(),SpringLayout.WEST,panContent);
	}
	
	protected Long convertMargin(String val){
		return val.equals("auto")?null:Long.parseLong(val);
	}
	
	protected final void layout(Object ... params){
		assert params.length%2==0;
		for(int i=0;i<params.length/2;i++){
			Component control=(Component)params[i*2];
			String[] margin=((String)params[i*2+1]).split("\\s+");
			setMargin(control,
				convertMargin(margin[0]), 
				convertMargin(margin[1]), 
				convertMargin(margin[2]), 
				convertMargin(margin[3]));
			panContent.add(control);
		}
	}
	
	protected final void menu(JMenu ... menus){
		JMenuBar bar=new JMenuBar();
		for(JMenu i:menus)bar.add(i);
		this.setJMenuBar(bar);
	}
	
	protected final void menuItem(JMenu mn, JMenuItem ... menus){
		for(JMenuItem i:menus){
			i.setPreferredSize(new Dimension(120,30));
			mn.add(i);
		}
	}
}
