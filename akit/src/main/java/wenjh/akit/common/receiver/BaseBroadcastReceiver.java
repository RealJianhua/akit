package wenjh.akit.common.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BaseBroadcastReceiver extends BroadcastReceiver {
	private final Context context;
	private IBroadcastReceiveListener receiveListener = new IBroadcastReceiveListener() {
		@Override
		public void onReceive(Intent intent) {
			
		}
	};
	
	public BaseBroadcastReceiver(Context context, String action) {
		this.context = context;
		register(action);
	}
	
	public BaseBroadcastReceiver(Context context, IntentFilter filter) {
		this.context = context;
		register(filter);
	}
	
	public BaseBroadcastReceiver(Context context) {
		this.context = context;
	}
	
	@Override
	public final void onReceive(Context context, Intent intent) {
		receiveListener.onReceive(intent);
		doReceive(context, intent);
		
	}
	
	public void doReceive(Context context, Intent intent){
	}
	
	public void register(String... action) {
		IntentFilter filter = new IntentFilter();
		for (String string : action) {
			filter.addAction(string);
		}
		register(filter);
	}
	
	public void register(IntentFilter filter) {
		context.registerReceiver(this, filter);
	}

	public void setReceiveListener(IBroadcastReceiveListener receiveListener) {
		this.receiveListener = receiveListener;
	}
	
	
	public interface IBroadcastReceiveListener {
		void onReceive(Intent intent);
	}
	
}