package wenjh.akit.demo.maintab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.View;

import com.wenjh.akit.R;

import wenjh.akit.demo.ContextUtil;
import wenjh.akit.demo.account.AccountApi;
import wenjh.akit.demo.account.model.Account;
import wenjh.akit.activity.base.BaseFragment;
import wenjh.akit.common.asynctask.BaseTask;
import wenjh.akit.common.view.MProgressDialog;
import wenjh.akit.demo.people.model.UserService;

public class LoginAccountFragment extends BaseFragment {
	private Account mAccountInfo = new Account();

	@Override
	protected void onCreated(Bundle savedInstanceState) {
		initViews();
		initEvents();
		initDatas();
	}

	@Override
	protected int getContentViewResourceIdAndCreateView() {
		return R.layout.fragment_login;
	}
	
	@Override
	protected void initViews() {
	}

	@Override
	protected void initEvents() {
		findViewById(R.id.login_btn_login).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mAccountInfo.setDeviceId(Secure.getString(getActivity().getContentResolver(), Secure.ANDROID_ID));
				execAsyncTask(new LoginTask(getActivity()));
			}
		});
	}

	@Override
	protected void initDatas() {
		
	}

	private class LoginTask extends BaseTask<Object, Object, Object> {
		public LoginTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreTask() {
			showDialog(new MProgressDialog(getContext(), this));
		}
		
		@Override
		protected void onTaskFinish() {
			closeDialog();
		}

		@Override
		protected Object executeTask(Object... params) throws Exception {
			AccountApi.getInstance().loginWithFacebookAccount(mAccountInfo);
			UserService userService = new UserService(mAccountInfo.getUserId());
			userService.saveFullUser(mAccountInfo.getAccountUser());
			mAccountInfo.getAccountPreference().saveAll();
			userService.getDb().close();
			return null;
		}

		@Override
		protected void onTaskSuccess(Object result) {
			super.onTaskSuccess(result);
			ContextUtil.getApp().login(mAccountInfo);
			log.d("session:" + mAccountInfo.getSessionId()+", loginuser="+mAccountInfo.getAccountUser());
			Intent intent = new Intent(getContext(), MainTabActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			getContext().startActivity(intent);
			finish();
		}
	}
}
