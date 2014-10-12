package alx.utils;

import java.io.IOException;
import java.io.InputStream;

public class TestInputStream extends InputStream {
	public final int length;
	protected int i=0;
	public TestInputStream(int length){ this.length=length; }
	@Override
	public int available(){ return (int)(this.length-this.i); }
	@Override
	public int read() throws IOException { return i<length?i++:-1; }
}
