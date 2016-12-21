package wenjh.akit.common.view;

import wenjh.akit.common.util.ContextUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

/**
 * Toast显示器。如果你要使用静态方法的方式显示Toast，那么请务必保证之前调用过{@link #doEnable(Context)}
 * 
 * 
 * @author <a href="mailt:wenlin56@sina.com"> wjh </a>
 */
public class Toaster {
	private final static boolean DEBUG = false;
	private final static int MSG_SHOWTOAST = 1366;
	private final static int MSG_SHOWTOAST_D = 1367;
	private final static int DEFAULT_DURATION = Toast.LENGTH_SHORT;
	
	public final static int LENGTH_LONG = Toast.LENGTH_LONG;
	public final static int LENGTH_SHORT = Toast.LENGTH_SHORT;
	
	protected Toast mToast = null;
	private static Context sContext = null;
	private static Toaster sToasterInstance = null;

	/**
	 * 将 Toaster 激活，在Toaster使用之前必须要进行激活。
	 * 通常情况下，Toaster的整个生命周期中只需要被激活一次。
	 */
	public static void initApplicationContext(Context context) {
		Toaster.sContext = context;	
		sToasterInstance = new Toaster();
	}
	
	protected Toaster() {
		if(sContext == null)
			throw new RuntimeException("Showner not been activated. You must call 'doEnable(Context c)' method before");
		makeNewToast();
	}
	
	public static void release() {
		sToasterInstance = null;
	}
	
	public void setGravity(int gravity, int xOffset, int yOffset) {
		mToast.setGravity(gravity, xOffset, yOffset);
	}
	
	public void showMsg(String msg) {
		showMsg(msg, false);
	}
	
	public void showMsg(Object msg) {
		showMsg(msg.toString());
	}
	
	public void showMsg(int stringResId) {
		showMsg(sContext.getString(stringResId));
	}
	
	public void showMsg(String msg, boolean makeNew) {
		showMsg(msg, false, DEFAULT_DURATION);
	}
	
	public void showMsg(String msg, boolean makeNewToast, int duration) {
		if(!ContextUtil.isIcsVsersion()) {
			mToast.cancel();
		}
		
		if(makeNewToast) {
			makeNewToast();
		}
		
		mToast.setText(msg);
		mToast.setDuration(duration);
		mToast.show();
	}
	
	public int getDuration() {
		return mToast.getDuration();
	}
	
	public void setDuration(int duration) {
		mToast.setDuration(duration);
	}
	
	public void setView(View view) {
		mToast.setView(view);
	}
	
	public static void show(Object obj, int duration) {
		show(obj.toString(), duration);
	}
	
	public static void show(int resId, int duration) {
		show(sContext.getString(resId), duration);
	}
	
	public static void show(Object text) {
		show(text, DEFAULT_DURATION);
	}
	
	public static void show(String text) {
		show(text, DEFAULT_DURATION);
	}
	
	
	public static void debug(String text) {
		if(DEBUG)
			show(text, DEFAULT_DURATION);
	}
	
	public static void show(String text, int duration) {
		int d = sToasterInstance.getDuration();
		sToasterInstance.showMsg(text, false, duration);
		sToasterInstance.setDuration(d);
	}
	public static void show(int resId){
		String msg = sContext.getString(resId);
		show(msg);
	}
	
	/**
	 * 设置ToastShowner的View。注意此修改是针对全局的，即，将影响到下一次ToastShwoner.show()的调用。
	 * @param view
	 */
	public static void changeView(View view) {
		sToasterInstance.setView(view);
	}
	
	
	@SuppressLint("ShowToast")
	protected void makeNewToast() {
		mToast = Toast.makeText(sContext, "", DEFAULT_DURATION);
	}
	
	public static void showInvalidate(CharSequence message) {
		Message msg = new Message();
		msg.what = MSG_SHOWTOAST;
		msg.obj = message;
		handler.sendMessage(msg);
	}
	
	public static void showInvalidate(CharSequence message, int duration) {
		Message msg = new Message();
		msg.what = MSG_SHOWTOAST_D;
		msg.obj = message;
		msg.arg1 = duration;
		handler.sendMessage(msg);
	}
	
	public static void showInvalidate(int resId, int duration){
		String str = sContext.getString(resId);
		showInvalidate(str, duration);
	}
	
	public static void debugInvalidate(CharSequence message) {
		if(!DEBUG) return;
		Message msg = new Message();
		msg.what = MSG_SHOWTOAST;
		msg.obj = message;
		handler.sendMessage(msg);
	}
	
	public static void showInvalidate(int resId) {
		String str = sContext.getString(resId);
		Message msg = new Message();
		msg.what = MSG_SHOWTOAST;
		msg.obj = str;
		handler.sendMessage(msg);
	}
	
	private static Handler handler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(android.os.Message msg) {
			if(msg.what == MSG_SHOWTOAST) {
				sToasterInstance.showMsg((String)msg.obj);
			} else if(msg.what == MSG_SHOWTOAST_D) {
				int d = sToasterInstance.getDuration();
				sToasterInstance.showMsg((String)msg.obj, false, msg.arg1);
				sToasterInstance.setDuration(d);
			}
		};
	};
	
}
