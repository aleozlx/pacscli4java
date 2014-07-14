package com.aleozlx.pacswitch;
import java.util.*;
/**
 * IMessageHandler 
 */
public interface IMessageHandler {
	/**
	 * Handle a tracked message
	 * @param from Sender ID
	 * @param message Message content
	 * @return Response to the message
	 */
	public String handleMessage(String from, String message);

	/**
	 * Refresh the pending messages counts
	 * @param ct New counts mapping
	 */
	public void refreshPendingCounts(Map<String,Integer> ct);
}
