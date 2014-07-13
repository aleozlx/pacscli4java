package com.aleozlx.pacswitch.test;

import java.io.*;
import java.util.*;

public final class Storage {
	static final String ENC="utf-8";
	static final String APPBASE="profile/";
	static final String[] profileDirs={"history"};
	
	String userid;
	String getBase(){ return APPBASE+this.userid+"/"; }
	File getFile(String fname){ return new File(getBase()+fname); }
	Friends friends=new Friends();
	MessageHistroy msgHistory=new MessageHistroy();

	Storage(String user){ 
		this.userid=user; 
		for(String dir:profileDirs)getFile(dir).mkdirs();
	}
	
	class Friends{
		public List<String> get(){
			List<String> r=new ArrayList<String>();
			try{
				Reader sr;
				FileInputStream is=new FileInputStream(getFile("friends.txt"));
				try { sr = new InputStreamReader(is,ENC); } 
				catch (UnsupportedEncodingException e) {sr = new InputStreamReader(is); } 
				
				BufferedReader in=new BufferedReader(sr);
				String line;
				try {
					line = in.readLine();
					while (line != null){
						String trimmed=line.trim();
						if(!trimmed.equals("")&&!trimmed.startsWith("#"))r.add(trimmed);
						line = in.readLine();
					}
				} 
				catch (IOException e) {  }
				finally{ 
					if(in!=null)
					try { in.close(); } 
					catch (IOException e) { } 
				}
			}
			catch (FileNotFoundException e) { } 
			return r;
		}
	}
	
	
	class MessageHistroy{
		public void write(String whom, String output){
			try {
				Writer sw;
				FileOutputStream os=new FileOutputStream(getFile("history/"+whom+".txt"),true);
				try { sw =new OutputStreamWriter(os,ENC); } 
				catch (UnsupportedEncodingException e) {sw = new OutputStreamWriter(os); } 
				
				PrintWriter out=new PrintWriter(sw,true);
				out.print(output);
				out.close();
			} catch (FileNotFoundException e) { }
		}
		
		public List<String> read(String whom){
			return tail(getFile("history/"+whom+".txt"),10);
		}
	}
	
	private static List<String> tail(File file, int ct) {
		int demand=ct;
		LinkedList<String> r=new LinkedList<String>(); 
		if(ct<=0)return r;
		Stack<Long> sp=new Stack<Long>();
		if (!file.exists() || file.isDirectory() || !file.canRead()) return r;
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "r");
			long len = raf.length();
			if (len == 0L) return r;
			else {
				long pos = len - 1;
				while (pos > 0) {
					pos--;
					raf.seek(pos);
					if (raf.readByte() == '\n') {
						sp.push(pos);
						if(--ct>=0)continue;
						else break;
					}
				}
				if (pos == 0) sp.push(-1L);
				try{
					if(sp.size()>0){
						long start=sp.peek();
						raf.seek(start+1);
						byte[] bytes = new byte[(int)(len-start)-1];
						raf.read(bytes);
						String[] lines=new String(bytes,ENC).split("\n");
						for(int i=0;i<=demand&&i<lines.length;i++)r.add(lines[i]);
					}
//					while(sp.size()>0){
//						long start=sp.pop(),end;
//						if(sp.size()>0){
//							end=sp.peek();
//							raf.seek(start+1);
//							byte[] bytes = new byte[(int)(end-start)-1];
//							raf.read(bytes);
//							r.add(new String(bytes,ENC));
//						}
//						else{
//							int rem=(int)(len-start)-1;
//							if(rem>0){
//								raf.seek(start+1);
//								byte[] bytes = new byte[rem];
//								raf.read(bytes);
//								r.add(new String(bytes,ENC));
//							}
//							break;
//						}
//					}
				}
				catch(UnsupportedEncodingException e){ }
				if(r.size()>0){
					String lastline=r.get(r.size()-1).trim();
					if(lastline.equals("")) r.remove(r.size()-1);
					else if(r.size()>demand) r.remove(0);
				}
				return r;
			}
		} 
		catch (FileNotFoundException e) { } 
		catch (IOException e) { } 
		finally {
			if (raf != null) {
				try { raf.close(); } 
				catch (Exception e2) { }
			}
		}
		return r;
	}
}
