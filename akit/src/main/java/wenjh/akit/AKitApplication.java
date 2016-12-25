package wenjh.akit;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import wenjh.akit.common.receiver.ReceiverDispatcher;
import wenjh.akit.common.util.ContextUtil;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.view.Toaster;

public class AKitApplication extends Application {
	private final static int Msg_DispatchMessage = 954;
	private LogUtil log = new LogUtil(this);
	private ReceiverDispatcher receiverDispatcher = null;

	@Override
	public void onCreate() {
		super.onCreate();
		ContextUtil.initApplicationContext(this);
		Toaster.initApplicationContext(this);
		receiverDispatcher = new ReceiverDispatcher();
	}

	public void dispatchMessage(Bundle bundle, String action) {
		android.os.Message message = new android.os.Message();
		message.what = Msg_DispatchMessage;
		message.obj = action;
		message.setData(bundle);
		handler.sendMessage(message);
	}

	public ReceiverDispatcher getReceiverDispatcher() {
		return receiverDispatcher;
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if(msg.what == Msg_DispatchMessage) {
				Bundle bundle = msg.getData();
				if(bundle == null) {
					bundle = new Bundle();
				}
				if(msg.obj == null){
					log.w("dispatcher receive， action is null");
					return;
				}
				receiverDispatcher.dispatch(bundle, msg.obj+"");
			}
		};
	};

	public void onApplicationOpened() {

	}
}
