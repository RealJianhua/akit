package wenjh.akit.activity.base;

import java.lang.ref.WeakReference;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import wenjh.akit.common.asynctask.HandyThreadPool;
import wenjh.akit.AKitApplication;
import wenjh.akit.demo.account.model.AccountSettingPreference;
import wenjh.akit.common.util.ContextUtil;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.view.Toaster;
import wenjh.akit.config.DebugConfigs;
import wenjh.akit.demo.people.model.User;

public abstract class BaseFragment extends Fragment {
	protected static final int RESULT_OK = Activity.RESULT_OK;
	protected static final int RESULT_CANCELED = Activity.RESULT_CANCELED;
	private WeakReference<View> mContentViewReference = null;
	private SparseArray<WeakReference<View>> mViewFounds = null;
	final protected LogUtil log = new LogUtil(getClass().getSimpleName());
	protected User currentUser = null;
	protected AccountSettingPreference userPreference = null;
	private boolean mCreated = false;
	private boolean mCallOnResult = false;
	protected Handler mHandler;

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log.i("onCreate");
		mHandler = new Handler();
		mViewFounds = new SparseArray<WeakReference<View>>();
		mContentViewReference = null;
		mCreated = false;
		onFragmentCreated(savedInstanceState);
	}

	public BaseActivity getBaseActivity() {
		return (BaseActivity) getActivity();
	}

	public Intent getIntent() {
		return getActivity().getIntent();
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		log.w("onCreateView, savedInstanceState="+savedInstanceState);
		if(mContentViewReference != null && mContentViewReference.get() != null && getRetainInstance()) {
			return mContentViewReference.get();
		} else {
			View view = null;
			if (getContentViewResourceIdAndCreateView() > 0) {
				view = inflater.inflate(getContentViewResourceIdAndCreateView(), container, false);
			}
			mContentViewReference = new WeakReference<View>(view);
			return view;
		}
	}

	protected abstract void onCreated(Bundle savedInstanceState);

	/**
	 * call on onCreateView method
	 * 
	 * @return
	 */
	protected abstract int getContentViewResourceIdAndCreateView();

	protected abstract void initViews();

	protected abstract void initEvents();

	protected abstract void initDatas();

	public boolean onBackPressed() {
		return false;
	}

	public View getContentView() {
		return mContentViewReference != null ? mContentViewReference.get() : null;
	}

	public Context getContext() {
		return ContextUtil.getContext();
	}

	public void unregisterReceiver(BroadcastReceiver receiver) {
		getActivity().unregisterReceiver(receiver);
	}

	public void overridePendingTransition(int enterAnim, int exitAnim) {
		getActivity().overridePendingTransition(enterAnim, exitAnim);
	}

	public final void runOnUiThread(Runnable action) {
		if(isCreated()) {
			if (Looper.myLooper() != getContext().getMainLooper()) {
				mHandler.post(action);
			} else {
				action.run();
			}
		} else {
			log.i("runOnUiThread, but activity not create");
		}
	}

	protected void finish() {
		getActivity().finish();
	}

	public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
		getActivity().registerReceiver(receiver, filter);
	}

	public void sendBroadcast(Intent intent) {
		getContext().sendBroadcast(intent);
	}

	public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
		getContext().sendOrderedBroadcast(intent, receiverPermission);
	}

	public ActionBar getActionBar() {
		return ((BaseActivity) getActivity()).getActionBar();
	}
	
	public View findViewById(int id) {
		View v = mViewFounds.get(id) != null ? mViewFounds.get(id).get() : null;
		if (v == null) {
			v = getContentView() == null ? null : getContentView().findViewById(id);
			if (v != null) {
				mViewFounds.put(id, new WeakReference<View>(v));
			}
		}

		return v;
	}

	@Override
	public final void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		log.i("onActivityCreated");
		currentUser = getBaseActivity().getCurrentUser();
		userPreference = getBaseActivity().getUserPreference();

		onBeforeCreated(savedInstanceState);
		
		if (savedInstanceState != null) {
			onRestoreInstanceState(savedInstanceState);
		}

		mCreated = true;
		onCreated(savedInstanceState);
		onPostCreated(savedInstanceState);

		if (mCallOnResult) {
			onActivityResultReceived(requestCode, resultCode, data);
			mCallOnResult = false;
		}
	}
	
	protected void onFragmentCreated(Bundle savedInstanceState) {
		
	}

	protected void onBeforeCreated(Bundle savedInstanceState) {
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
	}

	protected void callSuperActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCreated = false;
		log.i("-----onDetach");
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		log.i("-----onAttach");
	}
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		log.i("-----onDestoryView");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		log.i("-----onDestroy");
	}

	public boolean isCreated() {
		return mCreated;
	}

	public AKitApplication getApp() {
		return ContextUtil.getApp();
	}

	private boolean foreground = false;

	public boolean isForeground() {
		return foreground;
	}

	public void setForeground(boolean foreground) {
		this.foreground = foreground;
	}
	
	public void toast(int resId) {
		if (isForeground()) {
			Toaster.show(resId);
		}
	}

	public void toast(String message) {
		if (isForeground()) {
			Toaster.show(message);
		}
	}

	public void toastInvalidate(String message) {
		if (isForeground()) {
			Toaster.showInvalidate(message);
		}
	}

	public void toastInvalidate(int resId) {
		if (isForeground()) {
			Toaster.showInvalidate(resId);
		}
	}

	public void debugToast(String message) {
		if (DebugConfigs.DEBUGGABLE) {
			toast(message);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public void onResume() {
		super.onResume();
	};

	int requestCode;
	int resultCode;
	Intent data;

	@Override
	public final void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mCreated) {
			onActivityResultReceived(requestCode, resultCode, data);
		} else {
			log.w("requestCode=" + requestCode + ", resultCode=" + resultCode + ", fragment not created");
			mCallOnResult = true;
			this.requestCode = requestCode;
			this.resultCode = resultCode;
			this.data = data;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	protected void onActivityResultReceived(int requestCode, int resultCode, Intent data) {
	}

	private Dialog dialog = null;

	public synchronized void showDialog(Dialog dialog) {
		closeDialog();
		this.dialog = dialog;
		if (getActivity() != null && !getActivity().isFinishing()) {
			dialog.show();
		}
	}

	/**
	 * 关闭当前显示的Dialog。
	 */
	public synchronized void closeDialog() {
		if (dialog != null && dialog.isShowing() && getActivity() != null && !getActivity().isFinishing()) {
			dialog.dismiss();
			dialog = null;
		}
	}

	public void execAsyncTask(AsyncTask<?, ?, ?> task) {
		if (getActivity() != null) {
			getBaseActivity().execAsyncTask(task);
		}
	}

	public void postAsyncRunnable(Runnable runnable) {
		HandyThreadPool.getGlobalThreadPool().execute(runnable);
	}

	protected void onPostCreated(Bundle savedInstanceState) {
		// null
	}
	
	protected void hideInputMethod() {
		InputMethodManager im = ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
		View curFocusView = getActivity().getCurrentFocus();
		if (curFocusView != null) {
			im.hideSoftInputFromWindow(curFocusView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}
