package wenjh.akit.demo.people.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.wenjh.akit.R;

import wenjh.akit.config.HostConfigs;
import wenjh.akit.demo.people.model.User;
import wenjh.akit.demo.people.UserApi;
import wenjh.akit.activity.base.MainScreenFragment;
import wenjh.akit.demo.chat.ui.PeopleChatActivity;
import wenjh.akit.common.asynctask.BaseTask;
import wenjh.akit.common.util.StringUtil;
import wenjh.akit.common.view.SmartImageView;
import wenjh.akit.common.view.TagsLayout;
import wenjh.akit.demo.img.ui.ImageBrowserActivity;
import wenjh.akit.demo.people.model.UserService;

public class UserProfileFragment extends MainScreenFragment {
	String mUserId = null;
	UserService mUserService = null;
	SmartImageView mCoverImageView = null;
	SmartImageView mAvatarImageView = null;
	TextView mAboutTextView = null;
	Button mChatButton = null;
	TagsLayout mInterestsLayout = null;
	User mUser = null;

	@Override
	protected void onCreated(Bundle savedInstanceState) {
		mUserId = getIntent().getStringExtra(UserProfileActivity.KEY_USERID);
		initViews();
		initEvents();
		initDatas();
	}

	@Override
	protected int getContentViewResourceIdAndCreateView() {
		return R.layout.fragment_otheruserprofile;
	}

	@Override
	protected void initViews() {
		mCoverImageView = (SmartImageView) findViewById(R.id.userprofile_iv_cover);
		mAvatarImageView = (SmartImageView) findViewById(R.id.userprofile_iv_avatar);
		mAboutTextView = (TextView) findViewById(R.id.userprofile_tv_about);
		mInterestsLayout = (TagsLayout) findViewById(R.id.userprofile_layout_interests);
		mChatButton = (Button) findViewById(R.id.userprofile_btn_chat);
	}

	@Override
	protected void initEvents() {
		mChatButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), PeopleChatActivity.class);
				intent.putExtra(PeopleChatActivity.KEY_REMOTEUSERID, mUserId);
				startActivity(intent);
			}
		});
		mCoverImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), ImageBrowserActivity.class);
				if(!mUser.getCover().equals("") && mUser.getCover()!=null){
				String[] smallImageArray = new String[]{HostConfigs.getImageUrlWithGUID(mUser.getCover(),1)};
				String[] largeImageArray = new String[]{HostConfigs.getImageUrlWithGUID(mUser.getCover(),3)};
				intent.putExtra(ImageBrowserActivity.KEY_IMAGEARRAY_SMALL_URL, smallImageArray);
		        intent.putExtra(ImageBrowserActivity.KEY_IMAGEARRAY_LARGE_URL, largeImageArray);
		        intent.putExtra(ImageBrowserActivity.KEY_INDEX, 0);
		        startActivity(intent);
				} 
		    }
		});
		mAvatarImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), ImageBrowserActivity.class);
				if(!mUser.getCover().equals("") && mUser.getCover()!=null){
				String[] smallImageArray = new String[]{HostConfigs.getImageUrlWithGUID(mUser.getAvatar(),1)}; 
				String[] largeImageArray = new String[]{HostConfigs.getImageUrlWithGUID(mUser.getAvatar(),3)};
				intent.putExtra(ImageBrowserActivity.KEY_IMAGEARRAY_SMALL_URL, smallImageArray);
		        intent.putExtra(ImageBrowserActivity.KEY_IMAGEARRAY_LARGE_URL, largeImageArray);
		        intent.putExtra(ImageBrowserActivity.KEY_INDEX, 0);
		        startActivity(intent);
				}
			}
		});
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if(!currentUser.getId().equals(mUserId)) {
			// show report/block menus
			inflater.inflate(R.menu.userprofile, menu);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.userprofile_menu_blockandreport) {
			onReportMenuSelected();
			return true;
		} else if(item.getItemId() == R.id.userprofile_menu_block) {
			onBlockMenuSelected();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void initDatas() {
		mUserService = new UserService();
		mUser = new User(mUserId);
		postAsyncRunnable(new Runnable() {
			@Override
			public void run() {
				initDataSync();
			}
		});
		execAsyncTask(new RefreshUserProfileTask(getActivity()));
	}
	
	private void initDataSync() {
		boolean cached = mUserService.getUser(mUser);
		if(cached) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					refreshProfileUI();
				}
			});
		}
	}
	
	private void onBlockMenuSelected() {
	}
	
	private void onReportMenuSelected() {
	}
	
	private void refreshProfileUI() {
		mCoverImageView.loadImageGuid(mUser.getCover());
		mAvatarImageView.loadImageGuid(mUser.getAvatar());
		mAboutTextView.setText(mUser.getAbout());
		mInterestsLayout.setTags(mUser.getInterests());
		if(!StringUtil.isEmpty(mUser.getDisplayName())) {
			getActionBar().setTitle(mUser.getDisplayName());
		} else {
			getActionBar().setTitle("");
		}
		
		boolean canChat = mUser.canChat() && !mUserId.equals(currentUser.getId());
		mChatButton.setVisibility(canChat ? View.VISIBLE : View.GONE);
	}
	
	private class RefreshUserProfileTask extends BaseTask<Object, Object, Object> {
		public RefreshUserProfileTask(Context context) {
			super(context);
		}

		@Override
		protected Object executeTask(Object... params) throws Exception {
			UserApi.getInstance().downloadUserProfile(mUser);
			mUserService.saveFullUser(mUser);
			return null;
		}

		@Override
		protected void onTaskSuccess(Object o) {
			refreshProfileUI();
		}
	}
}
