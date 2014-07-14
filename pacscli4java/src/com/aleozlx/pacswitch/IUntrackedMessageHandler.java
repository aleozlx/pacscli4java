package com.aleozlx.pacswitch;
/**
 * IUntrackedMessageHandler
 */
public interface IUntrackedMessageHandler {
	/**
	 * Handle an untracked message
	 * @param from Sender ID
	 * @param message Message content
	 */
	public void handleMessage(String from, String message);
}
