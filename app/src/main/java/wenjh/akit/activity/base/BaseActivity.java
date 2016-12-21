package wenjh.akit.activity.base;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wenjh.akit.R;

import wenjh.akit.demo.account.model.Account;
import wenjh.akit.demo.account.model.AccountSettingPreference;
import wenjh.akit.common.asynctask.HandyThreadPool;
import wenjh.akit.common.receiver.MessageKeys;
import wenjh.akit.common.util.SystemBarTintManager;
import wenjh.akit.demo.people.model.User;
import wenjh.akit.AKitApplication;
import wenjh.akit.common.asynctask.BaseTask;
import wenjh.akit.common.receiver.AbsMessageReceiver;
import wenjh.akit.common.util.ContextUtil;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.view.Toaster;
import wenjh.akit.config.DebugConfigs;

public abstract class BaseActivity extends Activity implements ITipsHandler {
	private static final int MSG_HIDETOPBAR = 123;
	public final static String KEY_FROM = "afrom";
	public final static String KEY_FROMNAME = "afromname";
	public final static String KEY_KEEPEXTRAS_NETPAGE = "keep_extars_to_nexpage";

	@SuppressLint("UseSparseArrays")
	private Map<Integer, WeakReference<View>> mViewCache = new HashMap<Integer, WeakReference<View>>();
	protected LogUtil log = new LogUtil(getClass().getSimpleName());

	protected User currentUser = null;
	protected AccountSettingPreference userPreference = null;

	@SuppressWarnings("rawtypes")
	private List<AsyncTask> mAsyncTasks = null;
	private Dialog mDialog = null;
	private String mFrom = "";

	private View mTopTipView = null;
	private TextView mToptipTextView = null;
	private ImageView mLeftIconView = null, mRightIconView = null;

	private boolean mCreated = false;
	private boolean mForeground = false;
	private boolean mDestroyed = false;
	private boolean mKeepIntentExtras = false;

	private TipsList mTipsList = new TipsList();
	private List<AbsMessageReceiver> mReceivers = new ArrayList<AbsMessageReceiver>();
	private int mActivityResumCount = 0;
	private SystemBarTintManager mStatusBarManager = null;

	@SuppressWarnings("rawtypes")
	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		onActivityBeforeCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
		mStatusBarManager = new SystemBarTintManager(this);
		initSystemStatusBar();
		mCreated = false;
		mDestroyed = false;
		mAsyncTasks = new ArrayList<AsyncTask>();
		mFrom = getIntent().getStringExtra(KEY_FROM);
		mKeepIntentExtras = getIntent().getBooleanExtra(KEY_KEEPEXTRAS_NETPAGE, false);
		Account localAccount = getApp().getCurrentAccount();
		if (localAccount != null) {
			currentUser = localAccount.getAccountUser();
			userPreference = localAccount.getAccountPreference();
			registerMessageReceiver(100, MessageKeys.Action_Account_Logout);
		}
		if (getActionBar() != null) {
			getActionBar().setDisplayUseLogoEnabled(false);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setIcon(R.color.transparent);
		}
		onActivityCreated(savedInstanceState);
	}

	protected void initSystemStatusBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mStatusBarManager.setStatusBarTintEnabled(true);
			mStatusBarManager.setStatusBarTintResource(R.color.blupe_green);
		}
	}
	
	public SystemBarTintManager getStatusBarManager() {
		return mStatusBarManager;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (findViewById(R.id.layout_content) != null) {
			fillTopTip();
			refreshTips();
		}

		mCreated = true;
	}
	
	public void setKeepIntentExtras(boolean keepIntentExtras) {
		this.mKeepIntentExtras = keepIntentExtras;
	}

	public boolean isForeground() {
		return mForeground;
	}

	public boolean isCreated() {
		return mCreated;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// log.i("onResume");
		mForeground = true;
		ActivityHandler.onActivityResume();

		mActivityResumCount++;
		if (mActivityResumCount == 1) {
			onFristResume();
		} else if (mActivityResumCount == 2) {
			onSecondResume();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		// log.i("onStop");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		// log.i("onRestart");
	}

	@Override
	protected void onStart() {
		super.onStart();
		// log.i("onStart");
	}

	protected void onFristResume() {
	}

	protected void onSecondResume() {
	}

	public void toast(int resId) {
		toast(resId, Toaster.LENGTH_LONG);
	}

	public void toast(int resId, int duration) {
		if (isForeground()) {
			Toaster.show(resId, duration);
		}
	}

	public void toast(CharSequence message) {
		toast(message, Toaster.LENGTH_LONG);
	}

	public void debugToast(CharSequence message) {
		if (DebugConfigs.DEBUGGABLE) {
			toast(message, Toaster.LENGTH_LONG);
		}
	}

	public void toast(CharSequence message, int duration) {
		if (isForeground()) {
			Toaster.show(message, duration);
		}
	}

	public void toastInvalidate(CharSequence message) {
		if (isForeground()) {
			Toaster.showInvalidate(message, Toaster.LENGTH_LONG);
		}
	}

	public void toastInvalidate(int resId) {
		if (isForeground()) {
			Toaster.showInvalidate(resId, Toaster.LENGTH_LONG);
		}
	}

	public String getFrom() {
		return mFrom;
	}

	public BaseActivity thisActivity() {
		return this;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public AccountSettingPreference getUserPreference() {
		return userPreference;
	}

	public void showDialog(Dialog dialog) {
		closeDialog();
		if (!isFinishing()) {
			this.mDialog = dialog;
			dialog.show();
		}
	}

	public void onAccountLogout() {
		finish();
	}

	/**
	 * 关闭当前显示的Dialog。
	 */
	public void closeDialog() {
		Dialog dialog = mDialog;
		if (dialog != null && dialog.isShowing() && !isFinishing()) {
			dialog.dismiss();
			dialog = null;
		}
	}

	public void closeDialogInvalidate() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				closeDialog();
			}
		});
	}

	public void showDialogInvalidate(final Dialog dialog) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				showDialog(dialog);
			}
		});
	}

	@Override
	public void unregisterReceiver(BroadcastReceiver receiver) {
		if (receiver != null) {
			super.unregisterReceiver(receiver);
		}
	}

	@SuppressWarnings("rawtypes")
	public AsyncTask putAsyncTask(AsyncTask asyncTask) {
		if (!mDestroyed) {
			mAsyncTasks.add(asyncTask);
		}
		return asyncTask;
	}

	public AsyncTask removeAsyncTask(AsyncTask asyncTask) {
		mAsyncTasks.remove(asyncTask);
		return asyncTask;
	}

	protected void onActivityBeforeCreate(Bundle savedInstanceState) {

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void execAsyncTask(AsyncTask asyncTask) {
		putAsyncTask(asyncTask).execute();
	}

	protected AbsMessageReceiver registerMessageReceiver(int priority, String... actions) {
		return registerMessageReceiver(new AbsMessageReceiver(priority, actions) {
			@Override
			public boolean onReceive(Bundle bundle, String action) {
				return onMessageReceive(bundle, action);
			}
		});
	}

	protected AbsMessageReceiver registerMessageReceiver(AbsMessageReceiver messageReceiver) {
		if (!mDestroyed) {
			mReceivers.add(messageReceiver);
			getApp().getReceiverDispatcher().registerReceiver(messageReceiver);
		} else {
			log.w("registerMessageReceiver, action=" + messageReceiver.getActions() + ", but activity destoryed.");
		}

		return messageReceiver;
	}

	protected boolean onMessageReceive(Bundle bundle, String action) {
		if (MessageKeys.Action_Account_Logout.equals(action)) {
			log.i("receve " + MessageKeys.Action_Account_Logout);
			onAccountLogout();
		}
		return false;
	}

	private void fillTopTip() {
		onFillTopTip();
	}

	/**
	 * 当toptip被填充到界面来时调用此方法。<br>
	 * 其实现原理是将 {@link #getTopTipView()} 获取到的添加到HeaderBar下方(addView).<br>
	 * 如果子类已放弃了这种实现（例如，直接将此空间写到当前布局文件里），可以覆盖此方法的实现<br>
	 */
	protected void onFillTopTip() {
		try {
			ViewGroup contentRootLayout = (ViewGroup) findViewById(R.id.layout_content);
			mTopTipView = getTopTipView(contentRootLayout);
			if (contentRootLayout != null && mTopTipView != null) {
				if (contentRootLayout instanceof LinearLayout) {
					((LinearLayout) contentRootLayout).setOrientation(LinearLayout.VERTICAL);
					contentRootLayout.addView(mTopTipView, 0);
				} else {
					contentRootLayout.addView(mTopTipView);
				}

				log.i("onFillTopTip, true");
			} else {
				log.i("onFillTopTip, false");
			}
		} catch (Exception e) {
			log.e(e);
		}

		if (mTopTipView != null) {
			mToptipTextView = (TextView) mTopTipView.findViewById(R.id.toptip_text);
			mLeftIconView = (ImageView) mTopTipView.findViewById(R.id.toptip_icon_left);
			mRightIconView = (ImageView) mTopTipView.findViewById(R.id.toptip_icon_right);
			mTopTipView.setOnClickListener(mClickListener);
			mToptipTextView.setClickable(false);
		}
	}

	protected View getTopTipView(ViewGroup root) {
		return ContextUtil.getLayoutInflater().inflate(R.layout.common_toptip, root, false);
	}

	public void reload() {
		getLoaderManager().getLoader(0).forceLoad();
	}

	/**
	 * 和 {@link #isFinishing()}一样的
	 */
	public boolean isDestroyed() {
		return mDestroyed || isFinishing();
	}

	/**
	 * 和 {@link #isDestroyed()} 一样的
	 */
	@Override
	public boolean isFinishing() {
		return super.isFinishing() || mDestroyed;
	}

	@Override
	protected void onDestroy() {
		log.i("onDestroy");
		mDestroyed = true;
		super.onDestroy();
		cancelAllAysncTasks();
		if (mReceivers.size() > 0) {
			for (AbsMessageReceiver receiver : mReceivers) {
				boolean b = getApp().getReceiverDispatcher().unregisterReceiver(receiver);
				log.i("unregister " + receiver.getActions() + ", b=" + b);
			}
		}
		mReceivers.clear();
	}

	protected void cancelAllAysncTasks() {
		for (@SuppressWarnings("rawtypes")
		AsyncTask task : mAsyncTasks) {
			if (task != null) {
				if (task instanceof BaseTask) {
					((BaseTask) task).killAsynctask();
				} else {
					if (!task.isCancelled()) {
						task.cancel(true);
					}
				}
			}
		}
		mAsyncTasks.clear();
	}

	@Override
	public View findViewById(int id) {
		View findView = mViewCache.get(id) != null ? mViewCache.get(id).get() : null;
		if (findView == null) {
			findView = super.findViewById(id);
			mViewCache.put(id, new WeakReference<View>(findView));
		}
		return findView;
	}

	public <T extends View> T findViewById(int id, Class<T> t) {
		return (T) findViewById(id);
	}

	@Override
	public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
		log.i("~~~~~~~~KEY_FROM=" + fragment.getClass().getName());
		intent.putExtra(KEY_FROM, fragment.getClass().getName());
		
		if(mKeepIntentExtras && getIntent().getExtras() != null) {
			intent.putExtras(getIntent().getExtras());
		}
		
		super.startActivityFromFragment(fragment, intent, requestCode);
	}

	@Override
	protected void onPause() {
		super.onPause();
		log.i("onPause");
		mForeground = false;
		ActivityHandler.onActivityPause();
	}

	public void setForeground(boolean foreground) {
		this.mForeground = foreground;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		try {
			super.onSaveInstanceState(outState);
		} catch (IllegalStateException e) {
			// super mFragments.saveAllState()
			// 这个代码通常因为多线程问题，而导致fragments出现诡异的状态判断问题
			// 因此catch这个异常
			log.e(e);
		}
	}

	protected AKitApplication getApp() {
		return (AKitApplication) getApplication();
	}

	protected abstract void onActivityCreated(Bundle savedInstanceState);

	protected abstract void initViews();

	protected abstract void initDatas();

	protected abstract void initEvents();

	public void startActivityForResult(Intent intent, int requestCode, String from) {
		startActivityForResult(intent, requestCode, null, from);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void startActivity(Intent intent, Bundle options) {
		super.startActivity(intent, options);
	}

	/**
	 * 所有的请求，都会自动附带{@link #KEY_FROM}为当前class的名字
	 */
	@Override
	public final void startActivityForResult(Intent intent, int requestCode, Bundle options) {
		startActivityForResult(intent, requestCode, options, getClass().getName());
	}

	@SuppressLint("NewApi")
	public void startActivityForResult(Intent intent, int requestCode, Bundle options, String from) {
		if (intent.getExtras() == null || !intent.getExtras().containsKey(KEY_FROM)) {
			intent.putExtra(KEY_FROM, from);
		}
		
		if(mKeepIntentExtras && getIntent().getExtras() != null) {
			intent.putExtras(getIntent().getExtras());
		}
		
		if (ContextUtil.isJBVsersion()) {
			super.startActivityForResult(intent, requestCode, options);
		} else {
			super.startActivityForResult(intent, requestCode);
		}
		// overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	public final void startActivity(Intent intent, String from) {
		startActivityForResult(intent, -1, from);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 4.1 以前的版本没有 {@link #startActivityForResult(Intent, int, Bundle)}方法
	 * 但是4.1以后这个方法最后会调用到{@link #startActivityForResult(Intent, int, Bundle)}
	 * 为了让startactivity的方法统一出口（最后都调用到
	 * {@link #startActivityForResult(Intent, int, Bundle, String)}）
	 */
	@Override
	public final void startActivityForResult(Intent intent, int requestCode) {
		startActivityForResult(intent, requestCode, null, getClass().getName());
	}

	/**
	 * 新版本检测。 24小时内只开启一次网络异步检测任务
	 */
	public void checkUpdateVersion() {
		// TODO
	}

	private OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			/*
			 * 点击顶部tip时
			 */
			if (v == mTopTipView) {
				TipsMessage tipsMessage = (TipsMessage) v.getTag(R.id.tag_item);
				onToptipClick(v, tipsMessage);
			}
		}
	};

	protected void onToptipClick(View v, TipsMessage tipsMsg) {

	}

	protected void showTopTip(String msg) {
		showTopTip(null, msg);
	}

	protected void showTopTip(String msg, int leftIconResId, int rightIconResid) {
		Bitmap leftIcon = leftIconResId > 0 ? BitmapFactory.decodeResource(getResources(), leftIconResId) : null;
		Bitmap rightIcon = rightIconResid > 0 ? BitmapFactory.decodeResource(getResources(), rightIconResid) : null;
		showTopTip(null, msg, leftIcon, rightIcon, null, null);
	}

	/**
	 * 显示顶部tip。 并设置一张背景图片和文字
	 * 
	 * @param drawable
	 */
	protected void showTopTip(Drawable background, String msg) {
		showTopTip(background, msg, null, null, null, null);
	}

	// public User getCurrentUser() {
	// return currentUser;
	// }

	/**
	 * 显示顶部tip。 并设置一张背景图片和文字和左右的图标
	 * 
	 * @param drawable
	 * @param msg
	 *            消息文字
	 * @param leftIconResId
	 *            左边图标的resId 不设置就设置为 -1
	 * @param rightIconResId
	 *            右边图标的resId 不设置就设置为 -1
	 * @param leftIconOnclickListener
	 *            左边图标的点击事件，可以为null
	 * @param rightIconOnClickListener
	 *            右边图标的点击事件，可以为null
	 */
	protected void showTopTip(Drawable background, String msg, int leftIconResId, int rightIconResId, OnClickListener leftIconOnclickListener,
			OnClickListener rightIconOnClickListener) {
		Bitmap leftIcon = leftIconResId > 0 ? BitmapFactory.decodeResource(getResources(), leftIconResId) : null;
		Bitmap rightIcon = rightIconResId > 0 ? BitmapFactory.decodeResource(getResources(), rightIconResId) : null;

		showTopTip(background, msg, leftIcon, rightIcon, leftIconOnclickListener, rightIconOnClickListener);
	}

	/**
	 * 显示顶部tip。 并设置一张背景图片和文字和左右的图标
	 * 
	 * @param drawable
	 * @param msg
	 *            消息文字
	 * @param leftIconResId
	 *            左边图标 ，可以设置为null，表示不显示
	 * @param rightIconResId
	 *            右边图标 不设置就设置为null，表示不显示
	 * @param leftIconOnclickListener
	 *            左边图标的点击事件, 可以为null
	 * @param rightIconOnClickListener
	 *            右边图标的点击事件, 可以为null
	 */
	protected void showTopTip(Drawable background, String msg, Bitmap leftIcon, Bitmap rightIcon, OnClickListener leftIconOnclickListener,
			OnClickListener rightIconOnClickListener) {
		if (mTopTipView == null) {
			return;
		}

		if (background != null) {
			final Drawable drawable = background;
			mTopTipView.setBackgroundDrawable(drawable);
			if (drawable instanceof AnimationDrawable) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						((AnimationDrawable) drawable).start();
					}
				});
			}
		}

		if (mToptipTextView != null) {
			mToptipTextView.setText(msg == null ? "" : msg);
		}

		/*
		 * 设置左边图片
		 */
		if (mLeftIconView != null) {
			if (leftIcon != null) {
				mLeftIconView.setImageBitmap(leftIcon);
				mLeftIconView.setVisibility(View.VISIBLE);
			} else {
				mLeftIconView.setVisibility(View.GONE);
			}

			mLeftIconView.setOnClickListener(leftIconOnclickListener != null ? leftIconOnclickListener : mClickListener);
		}

		/*
		 * 设置右边图片
		 */
		if (mRightIconView != null) {
			if (rightIcon != null) {
				mRightIconView.setImageBitmap(rightIcon);
				mRightIconView.setVisibility(View.VISIBLE);
			} else {
				mRightIconView.setVisibility(View.GONE);
			}

			mRightIconView.setOnClickListener(rightIconOnClickListener != null ? rightIconOnClickListener : mClickListener);
		}

		mTopTipView.setTag(R.id.tag_item, null);
		onToptipShown();
	}

	public void hideInputMethod() {
		InputMethodManager im = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
		View curFocusView = getCurrentFocus();
		if (curFocusView != null) {
			im.hideSoftInputFromWindow(curFocusView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public boolean isToptipViewShown() {
		return mTopTipView != null && mTopTipView.getVisibility() == View.VISIBLE;
	}

	public void setToptipClickable(boolean ck) {
		if (mTopTipView != null) {
			mTopTipView.setClickable(ck);
		}
	}

	protected void onToptipShown() {
		if (mTopTipView == null) {
			return;
		}

		if (!mTopTipView.isShown()) {
			mTopTipView.setVisibility(View.VISIBLE);
		}

		mHandler.removeMessages(MSG_HIDETOPBAR);
	}

	public void refreshTips() {
		if (mTopTipView == null) {
			log.w("topTipView==null");
		} else {
			TipsMessage msg = mTipsList.peek();
			if (msg != null) {
				showTopTip(msg.message, 0, msg.clickable ? R.drawable.ic_common_arrow_toptip_right : 0);
				setToptipClickable(msg.clickable);
				mTopTipView.setTag(R.id.tag_item, msg);
			} else {
				hideToptipDelayed(1000);
			}
		}
	}

	private int tipsCursor = 1;

	public void addTips(TipsMessage message) {
		log.i("message=" + message);
		if (message.priority <= 0) {
			message.priority = tipsCursor++;
		}

		int index = mTipsList.indexOf(message);
		if (index < 0) {
			mTipsList.add(message);
		} else {
			// 队列里已经存在此消息
			mTipsList.remove(message);
			addTips(message);
		}

		refreshTips();
	}

	protected void onToptipHide() {
		mTopTipView.setVisibility(View.GONE);
	}

	public void hideToptipDelayed(long time) {
		mHandler.sendEmptyMessageDelayed(MSG_HIDETOPBAR, time);
	}

	public void removeTips(TipsMessage message) {
		mTipsList.remove(message);
		refreshTips();
	}

	public void removeTips(int id) {
		mTipsList.remove(new TipsMessage(id));
		refreshTips();
	}

	public void postAsyncRunable(Runnable runnable) {
		HandyThreadPool.getGlobalThreadPool().execute(runnable);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_HIDETOPBAR:
				onToptipHide();
				break;
			}
		};
	};
}
