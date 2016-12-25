package wenjh.akit.common.receiver;

import android.os.Bundle;

import java.util.TreeSet;


public class ReceiverList extends TreeSet<AbsMessageReceiver>{

	private static final long serialVersionUID = 1L;

	String action = null;
	
	public ReceiverList(String action) {
		this.action = action;
	}
	
	public void dispatch(Bundle bundle) {
		for (AbsMessageReceiver receiver : this) {
			if(receiver.onReceive(bundle, this.action)) {
				break;
			}
		}
	}
}
