package wenjh.akit.demo.maintab;


import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.wenjh.akit.R;

import wenjh.akit.activity.base.ActivityHandler;
import wenjh.akit.activity.base.ActivityHandler.ApplicationEventListener;
import wenjh.akit.activity.base.ScrollTabGroupActivity;
import wenjh.akit.common.view.SmartImageView;
import wenjh.akit.demo.ContextUtil;
import wenjh.akit.demo.MessageKeys;
import wenjh.akit.demo.account.AccountApi;
import wenjh.akit.demo.account.ui.AccountProfileFragment;
import wenjh.akit.demo.chat.ui.ChatSessionListFragment;
import wenjh.akit.demo.people.model.UserService;

public class MainTabActivity2 extends ScrollTabGroupActivity implements ApplicationEventListener {
	SmartImageView mLogoImageView = null;
	TextView mTitleView = null;
	
	// 不带透明主题，使用它，记得改SplashActivity.initOnlineActivity
	
	@Override
	protected void onActivityCreated(Bundle savedInstanceState) {
		setContentView(R.layout.activity_maintabs);
		initViews();
		initEvents();
		initDatas();
		initApplication();
	}

	@Override
	protected void initViews() {
		getActionBar().setHomeButtonEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setDisplayShowCustomEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(false);

		addTab(MainActvtyFragment.class, ChatSessionListFragment.class, AccountProfileFragment.class);
		addIndicatorViews(R.id.maintab_layout_1, R.id.maintab_layout_2, R.id.maintab_layout_3);

		View view = ContextUtil.getLayoutInflater().inflate(R.layout.include_actionbar_userlogo, null);
		mLogoImageView = (SmartImageView) view.findViewById(R.id.actionbar_iv_logo);
		mTitleView = (TextView) view.findViewById(R.id.actionbar_tv_title);
		getActionBar().setCustomView(view);
		registerMessageReceiver(100, MessageKeys.Action_MyProfileUpdate, MessageKeys.Action_Account_Logout);
	}

	@Override
	protected boolean onMessageReceive(Bundle bundle, String action) {
		if (MessageKeys.Action_MyProfileUpdate.equals(action)) {
			refreshCurrentUserInfo();
		} else if (MessageKeys.Action_Account_Logout.equals(action)) {
			finish();
		}
		return super.onMessageReceive(bundle, action);
	}

	private void refreshCurrentUserInfo() {
		mLogoImageView.load(ContextUtil.getCurrentUser().getAvatarImage());
		if (getCurrentTab() != 2) {
			mTitleView.setText(ContextUtil.getCurrentUser().getDisplayName());
		}

//		try {
//			GifDrawable gifDrawable = new GifDrawable(getResources(), R.drawable.f002);
//			mLogoImageView.setImageDrawable(gifDrawable);
//			gifDrawable.start();
//			log.i("refreshCurrentUserInfo===="+gifDrawable);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	protected void initEvents() {
	}

	@Override
	protected void initDatas() {
		postAsyncRunable(new Runnable() {
			@Override
			public void run() {
				try {
					UserService userService = new UserService();
					userService.getUser(ContextUtil.getCurrentUser());
					getApp().dispatchMessage(new Bundle(), MessageKeys.Action_MyProfileUpdate);

					AccountApi.getInstance().downloadMyProfile(ContextUtil.getCurrentUser(), ContextUtil.getCurrentPreference());

					userService.saveFullUser(ContextUtil.getCurrentUser());
					ContextUtil.getCurrentPreference().saveAll();
					getApp().dispatchMessage(new Bundle(), MessageKeys.Action_MyProfileUpdate);
				} catch (Exception e) {
					log.e(e);
				}
			}
		});


	}

	private void initApplication() {
		getApp().onApplicationOpened();
		ActivityHandler.addEventListner(getClass().getName(), this);
		// bind location service TODO
	}

	@Override
	public void onAppExit() {
		// stop service TODO
	}

	@Override
	public void onAppEnter() {
		// bind location service TODO
	}
}
