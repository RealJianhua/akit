package wenjh.akit.demo.maintab;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.wenjh.akit.R;

import wenjh.akit.activity.base.TransparentActionBarActivity;

public class SplashActivity extends TransparentActionBarActivity {
	private static final String FACEBOOK_FRAGMENT_TAG = "facebook_fragment";
	private LoginAccountFragment mFacebookLoginFragment;
	private FragmentManager mFragmentManager;

	@Override
	protected void onActivityCreated(Bundle savedInstanceState) {
		initViews();
		initEvents();
		initDatas();

		if (getApp().isOnline()) {
			initOnlineActivity();
		} else {
			initOfflineActivity();
		}
	}
	
	private void initOfflineActivity() {
		setContentView(R.layout.activity_splash);
		mFacebookLoginFragment = new LoginAccountFragment();
		mFragmentManager = getFragmentManager();
		mFragmentManager.beginTransaction().add(R.id.framelayout, mFacebookLoginFragment, FACEBOOK_FRAGMENT_TAG).commit();
	}

	private void initOnlineActivity() {
		Intent intent = new Intent(getApplicationContext(), MainTabActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		getApplicationContext().startActivity(intent);
		finish();
	}

	@Override
	protected void initEvents() {}

	@Override
	protected void initViews() {}

	@Override
	protected void initDatas() {}
	
	@Override
	public void onAccountLogout() {
	}
}
