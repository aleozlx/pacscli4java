package alx.utils;

/**
 * Synchronizer
 * @author Alex
 */
public class Synchronizer{
	/**
	 * Wait for some milliseconds
	 * @param ms A specific time
	 */
	public static final void wait(int ms){
		try{ Thread.sleep(ms); }
		catch(InterruptedException e){ Thread.currentThread().interrupt(); }
	}
}