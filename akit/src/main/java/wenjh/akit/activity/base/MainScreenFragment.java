package wenjh.akit.activity.base;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import wenjh.akit.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import wenjh.akit.common.receiver.AbsMessageReceiver;
import wenjh.akit.common.util.ContextUtil;

public abstract class MainScreenFragment extends BaseFragment implements ITipsHandler {
	public static final int MSG_HIDETOPBAR = 123;
	private boolean mFirstResume = false;
	private boolean mCallResumeAfterCreated = false;

	private TipsList mTipsList = new TipsList();

	private View mTopTipView = null;
	private TextView mToptipTextView = null;
	private ImageView mLeftIconView = null, mRightIconView = null;
	private View mIndicatorView;

	@Override
	protected void onBeforeCreated(Bundle savedInstanceState) {
		super.onBeforeCreated(savedInstanceState);
		log.i("TabOptionFragment onActivityCreated--" + getClass().getName());
		if (mCallResumeAfterCreated) {
			dispatchResume();
			onFirstResume();
		}
	}

	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		super.onFragmentCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	protected void onPostCreated(Bundle savedInstanceState) {
		ViewGroup contentRootLayout = (ViewGroup) findViewById(R.id.layout_content);
		if (contentRootLayout != null) {
			mTopTipView = getTopTipView(contentRootLayout);
			fillTopTip();
			refreshTips();
		}
	}

	public String getFrom() {
		return getBaseActivity().getFrom();
	}

	public void onFirstResume() {
		mFirstResume = true;
	}

	public boolean isFirstResumed() {
		return mFirstResume;
	}

	public void setCallResumeAfterCreated(boolean callResumeAfterCreated) {
		this.mCallResumeAfterCreated = callResumeAfterCreated;
	}

	public void onFragmentPauseByTabChanged() {

	}

	public boolean isCallResumeAfterCreated() {
		return mCallResumeAfterCreated;
	}

	/**
	 * 类似maintabactivity，用户按back键也不会退出前台。这种情况再打开应用，实际上先当于home键退出、又打开。是一种轻量级的重绘。
	 * 这种时候，activity会通知所有fragment。
	 */
	public void dispatchActivityHomeResume() {
		if (isCreated()) {
			if (mFirstResume) { // 如果已经resume触发过了，要重置
				mFirstResume = false;
			}
			onActivityHomeResume();
			scrollToTop();
		}
	}

	/**
	 * 和 {@link #dispatchActivityHomeResume()}相反
	 */
	public void dispatchActivityHomePause() {
		if (isCreated()) {
			setForeground(false);
			onActivityHomePause();
		}
	}

	protected void onActivityHomeResume() {
	}

	protected void onActivityHomePause() {

	}

	public void notifyDataChanaged() {
	}

	protected View getTopTipView(ViewGroup root) {
		return ContextUtil.getLayoutInflater().inflate(R.layout.common_toptip, root, false);
	}

	public View getIndicatorView() {
		return mIndicatorView;
	}

	public void setIndicatorView(View indicatorView) {
		this.mIndicatorView = indicatorView;
	}

	public void setInitArguments(Map<String, Object> map) {

	}

	private void fillTopTip() {
		onFillTopTip();
		if (mTopTipView != null) {
			mToptipTextView = (TextView) mTopTipView.findViewById(R.id.toptip_text);
			mLeftIconView = (ImageView) mTopTipView.findViewById(R.id.toptip_icon_left);
			mRightIconView = (ImageView) mTopTipView.findViewById(R.id.toptip_icon_right);
			mTopTipView.setOnClickListener(baseClickListener);
			mToptipTextView.setClickable(false);
		}
	}

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
	}

	List<AbsMessageReceiver> receivers = new ArrayList<AbsMessageReceiver>();

	protected AbsMessageReceiver registerMessageReceiver(int priority, String... actions) {
		AbsMessageReceiver messageReceiver = new AbsMessageReceiver(priority, actions) {
			@Override
			public boolean onReceive(Bundle bundle, String action) {
				if (isCreated()) {
					return onMessageReceive(bundle, action);
				} else {
					log.w("!!!!!!fragment not created");
					return false;
				}
			}
		};
		receivers.add(messageReceiver);
		getApp().getReceiverDispatcher().registerReceiver(messageReceiver);
		return messageReceiver;
	}

	protected boolean onMessageReceive(Bundle bundle, String action) {
		return false;
	}

	public void hideToptipDelayed(long time) {
		handler.sendEmptyMessageDelayed(MSG_HIDETOPBAR, time);
	}

	protected TextView getTopTipTextView() {
		return this.mToptipTextView;
	}

	protected void showTopTip(String msg) {
		showTopTip(null, msg);
	}

	protected void showTopTip(Drawable background, String msg) {
		showTopTip(background, msg, null, null, null, null);
	}

	protected void showTopTip(String msg, int leftIconResId, int rightIconResid) {
		Bitmap leftIcon = leftIconResId > 0 ? BitmapFactory.decodeResource(getResources(), leftIconResId) : null;
		Bitmap rightIcon = rightIconResid > 0 ? BitmapFactory.decodeResource(getResources(), rightIconResid) : null;
		showTopTip(null, msg, leftIcon, rightIcon, null, null);
	}

	protected void showTopTip(Drawable background, String msg, int leftIconResId, int rightIconResId, OnClickListener leftIconOnclickListener,
			OnClickListener rightIconOnClickListener) {
		Bitmap leftIcon = leftIconResId > 0 ? BitmapFactory.decodeResource(getResources(), leftIconResId) : null;
		Bitmap rightIcon = rightIconResId > 0 ? BitmapFactory.decodeResource(getResources(), rightIconResId) : null;

		showTopTip(background, msg, leftIcon, rightIcon, leftIconOnclickListener, rightIconOnClickListener);
	}

	protected void showTopTip(Drawable background, String msg, Bitmap leftIcon, Bitmap rightIcon, OnClickListener leftIconOnclickListener,
			OnClickListener rightIconOnClickListener) {
		if (mTopTipView == null) {
			return;
		}

		log.i("reflushTips, showTopTip, msg=" + msg);

		if (background != null) {
			final Drawable drawable = background;
			mTopTipView.setBackgroundDrawable(drawable);
			if (drawable instanceof AnimationDrawable) {
				handler.post(new Runnable() {
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

			mLeftIconView.setOnClickListener(leftIconOnclickListener != null ? leftIconOnclickListener : baseClickListener);
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

			mRightIconView.setOnClickListener(rightIconOnClickListener != null ? rightIconOnClickListener : baseClickListener);
		}

		mTopTipView.setTag(R.id.tag_item, null);
		onToptipShown(mTopTipView);
	}

	public void setToptipClickable(boolean ck) {
		if (mTopTipView != null) {
			mTopTipView.setClickable(ck);
		}
	}

	private OnClickListener baseClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			/*
			 * 点击顶部tip时
			 */
			if (v == mTopTipView) {
				onToptipClick(v);

				TipsMessage tipsMessage = (TipsMessage) v.getTag(R.id.tag_item);
				if (tipsMessage != null && tipsMessage.id == TipsMessage.ID_IMJSON_OTHER) {
				}

				onToptipClick(v, (TipsMessage) v.getTag(R.id.tag_item));
			}
		}
	};

	/**
	 * @deprecated {@link #onToptipClick(View, TipsMessage)}
	 * @param v
	 */
	protected void onToptipClick(View v) {

	}

	protected void onToptipClick(View v, TipsMessage tipsMsg) {

	}

	protected void onToptipShown(View toptipView) {
		if (toptipView == null) {
			return;
		}

		if (!toptipView.isShown()) {
			toptipView.setVisibility(View.VISIBLE);
		}

		handler.removeMessages(MSG_HIDETOPBAR);
	}

	protected void onToptipHide(View toptipView) {
		toptipView.setVisibility(View.GONE);
	}

	protected boolean monitorIMJState() {
		return false;
	}

	public boolean isToptipViewShown() {
		return mTopTipView != null && mTopTipView.getVisibility() == View.VISIBLE;
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

	public void refreshTips() {
		if (mTopTipView == null) {
			log.w("topTipView==null");
			return;
		}
		log.i("reflushTips, tipsList=" + mTipsList);
		TipsMessage msg = mTipsList.peek();
		if (msg != null) {
			showTopTip(msg.message, 0, msg.clickable ? R.drawable.ic_common_arrow_toptip_right : 0);
			setToptipClickable(msg.clickable);
			mTopTipView.setTag(R.id.tag_item, msg);
		} else {
			hideToptipDelayed(1000);
		}
	}

	public void removeTips(TipsMessage message) {
		mTipsList.remove(message);
		refreshTips();
	}

	public void removeTips(int id) {
		mTipsList.remove(new TipsMessage(id));
		refreshTips();
	}

	public final void dispatchResume() {
		if (isCreated()) {
			setForeground(true);
			onFragmentResume();
		} else {
			setCallResumeAfterCreated(true);
		}
	}

	protected void onFragmentResume() {
	}

	public final void dispatchPause() {
		setForeground(false);
		if (isCreated()) {
			onFragmentPause();
		}
	}

	protected void onFragmentPause() {
	}

	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
		return false;
	}

	public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
		return false;
	}

	public void fillingHeader(final Context context, ActionBar headerBar) {
	}

	public void execAsyncTask(AsyncTask<?, ?, ?> task) {
		if (getActivity() != null) {
			getBaseActivity().execAsyncTask(task);
		}
	}

	/**
	 * 回到顶部
	 */
	public void scrollToTop() {

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (receivers.size() > 0) {
			for (AbsMessageReceiver receiver : receivers) {
				boolean b = getApp().getReceiverDispatcher().unregisterReceiver(receiver);
				log.i("unregister " + receiver.getActions() + ", b=" + b);
			}
		}
		receivers.clear();
		mFirstResume = false;
		mCallResumeAfterCreated = false;
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_HIDETOPBAR:
				onToptipHide(mTopTipView);
				break;
			}
		};
	};

	public void onNewIntent(Intent intent) {
		
	}
}
