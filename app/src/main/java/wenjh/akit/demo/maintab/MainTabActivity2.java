package wenjh.akit.demo.maintab;


import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.wenjh.akit.R;

import wenjh.akit.demo.account.AccountApi;
import wenjh.akit.demo.account.ui.AccountProfileFragment;
import wenjh.akit.activity.base.ScrollTabGroupActivity;
import wenjh.akit.demo.chat.ui.ChatSessionListFragment;
import wenjh.akit.common.receiver.MessageKeys;
import wenjh.akit.common.view.SmartImageView;
import wenjh.akit.demo.people.model.UserService;
import wenjh.akit.activity.base.ActivityHandler;
import wenjh.akit.activity.base.ActivityHandler.ApplicationEventListener;
import wenjh.akit.common.util.ContextUtil;

public class MainTabActivity2 extends ScrollTabGroupActivity implements ApplicationEventListener {
	private SmartImageView mLogoImageView = null;
	private TextView mTitleView = null;
	
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
		registerMessageReceiver(100, MessageKeys.Action_MyProfileUpdate);
	}
	
	@Override
	protected boolean onMessageReceive(Bundle bundle, String action) {
		if(MessageKeys.Action_MyProfileUpdate.equals(action)) {
			refreshCurrentUserInfo();
		}
		return super.onMessageReceive(bundle, action);
	}
	
	private void refreshCurrentUserInfo() {
		mLogoImageView.loadImageGuid(currentUser.getAvatar());
		mTitleView.setText(currentUser.getDisplayName());
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
					userService.getUser(currentUser);
					getApp().dispatchMessage(new Bundle(), MessageKeys.Action_MyProfileUpdate);
					
					AccountApi.getInstance().downloadMyProfile(currentUser, userPreference);
					
					userService.saveFullUser(currentUser);
					userPreference.saveAll();
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
