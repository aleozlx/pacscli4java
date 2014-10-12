package alx.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class HexdumpOutputStream extends OutputStream {
	protected long i=0;
	protected final PrintStream out;
	protected StringBuilder line=new StringBuilder(80);
	protected ByteArrayOutputStream ascii=new ByteArrayOutputStream(16);
	private boolean isClosed=false;
	private boolean lineNumber=false;
	
	public HexdumpOutputStream(){ this.out=System.out; }
	public HexdumpOutputStream(OutputStream out){ this.out=new PrintStream(out); }
	public long length(){ return i; }

	@Override
	public synchronized void write(int b) throws IOException {
		if(isClosed)throw new IOException();
		final long addr=i++;
		if(!lineNumber){
			line.append(hexAlign(Long.toHexString(addr),8));
			line.append(' ');
			lineNumber=true;
		}
		if((addr&0x7)==0)line.append(' ');
		line.append(hexAlign(Integer.toHexString(b&0xFF),2));
		line.append(' ');
		ascii.write(b);
		if(((addr+1)&0xF)==0){
			line.append(" |");
			line.append(lineToString());
			line.append('|');
			out.println(line.toString());
			line=new StringBuilder(80);
			ascii=new ByteArrayOutputStream(16);
			lineNumber=false;
		}
	}
	
	protected String lineToString(){
		byte[] r=ascii.toByteArray();
		for(int i=0;i<r.length;i++)if(r[i]<0x20||r[i]==127)r[i]=46;
		return new String(r);
	}
	
	@Override
	public synchronized void flush() throws IOException {	
		byte[] r=ascii.toByteArray();
		if(r.length==0)return;
		int sps=(16-r.length)*3;
		if(r.length<=8)sps++;
		for(int i=0;i<sps;i++)line.append(' ');
		line.append(" |");
		line.append(lineToString());
		line.append('|');
		out.println(line.toString());
		line=new StringBuilder(80);
		ascii=new ByteArrayOutputStream(16);
		lineNumber=false;
    }
	
	@Override
	public void close() throws IOException {
		if(!isClosed)this.isClosed=true;
		else return;
		flush();
    }
	
	public static String hexAlign(String hex,int length){
		int d=length-hex.length();
		StringBuilder sb=new StringBuilder();
		if(d>0)while((d--)!=0)sb.append('0');
		sb.append(hex);
		return sb.toString();
	}
}
