package wenjh.akit.common.receiver;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;

public class ReceiverDispatcher {
	private Map<String, ReceiverList> receivers = new HashMap<String, ReceiverList>();
	
	public void registerReceiver(AbsMessageReceiver receiver) {
		if(receiver.getActions().size() == 0) {
			throw new IllegalArgumentException("actions.length==0");
		}
		for (String action : receiver.getActions()) {
			ReceiverList receiverList = getReceiverList(action);
			if(receiverList == null) {
				receiverList = new ReceiverList(action);
				receivers.put(action, receiverList);
			}
			receiverList.add(receiver);
		}
	}
	
	public boolean unregisterReceiver(AbsMessageReceiver receiver) {
		boolean r = false;
		for (String action : receiver.getActions()) {
			ReceiverList receiverList = getReceiverList(action);
			if(receiverList != null) {
				if(receiverList.remove(receiver)) {
					r = true;
				}
			}
		}
		return r;
	}
	
	public boolean dispatch(Bundle bundle, String action) {
		ReceiverList receiverList = getReceiverList(action);
		if(receiverList == null || receiverList.isEmpty()) {
			return false;
		} else {
			receiverList.dispatch(bundle);
			return true;
		}
	}
	
	protected final ReceiverList getReceiverList(String key) {
		return receivers.get(key);
	}
	
}
