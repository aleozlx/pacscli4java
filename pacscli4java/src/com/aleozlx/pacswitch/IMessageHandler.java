package com.aleozlx.pacswitch;
import java.util.*;
public interface IMessageHandler {
	public String handleMessage(String from, String message);
	public void refreshPendingCounts(Map<String,Integer> ct);
}
