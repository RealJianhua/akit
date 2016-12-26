package wenjh.akit.demo.account.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.wenjh.akit.R;

import java.util.ArrayList;

import wenjh.akit.activity.base.MainScreenFragment;
import wenjh.akit.common.util.AvatarAndName;
import wenjh.akit.common.view.AvatarsLayout;
import wenjh.akit.common.view.SmartImageView;
import wenjh.akit.common.view.TagsLayout;
import wenjh.akit.demo.ContextUtil;
import wenjh.akit.demo.MessageKeys;
import wenjh.akit.demo.people.model.User;
import wenjh.akit.demo.people.ui.UserProfileActivity;

public class AccountProfileFragment extends MainScreenFragment {
	SmartImageView mCoverImageView = null;
	SmartImageView mAvatarImageView = null;
	TextView mAboutTextView = null;
	TagsLayout mInterestsLayout = null;
	AvatarsLayout mFriendsLayout = null;
	User currentUser = null;
	
	@Override
	protected void onCreated(Bundle savedInstanceState) {
		currentUser = ContextUtil.getCurrentUser();
		initViews();
		initEvents();
		initDatas();
	}

	@Override
	protected int getContentViewResourceIdAndCreateView() {
		return R.layout.fragment_me;
	}

	@Override
	protected void initViews() {
		mCoverImageView = (SmartImageView) findViewById(R.id.myprofile_iv_cover);
		mAvatarImageView = (SmartImageView) findViewById(R.id.myprofile_iv_avatar);
		mAboutTextView = (TextView) findViewById(R.id.myprofile_tv_about);
		mInterestsLayout = (TagsLayout) findViewById(R.id.myprofile_layout_interests);
		mFriendsLayout = (AvatarsLayout) findViewById(R.id.myprofile_layout_friends);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.accountsetting, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.accountsetting_menu_settings) {
			new AlertDialog.Builder(getActivity()).setItems(new String[]{"Edit info","Settings"}, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						openEditProfile();
						break;
					case 1:
						openSettings();
						break;
					default:
						break;
					}
				}
			}).create().show();;
		}
		return true;
	}
	
	private void openSettings() {
		Intent intent = new Intent(getContext(), AccountSettingsActivity.class);
		startActivity(intent);
	}
	
	private void openEditProfile() {
		Intent intent = new Intent(getContext(), EditAccountProfileActivity.class);
		startActivity(intent);
	}

	@Override
	protected void initEvents() {
		mFriendsLayout.setOnAvatarItemClickedLsitener(new AvatarsLayout.OnAvatarItemClickedLsitener() {
			@Override
			public void onAvatarItemClicked(Object object, int position) {
				toast("onclick:"+position);
				Intent intent = new Intent();
				intent.setClass(getContext(), UserProfileActivity.class);
				intent.putExtra(UserProfileActivity.KEY_USERID, currentUser.getFriends().get(position).getId());
				startActivity(intent);
			}
		});
	}

	@Override
	protected void initDatas() {
		refreshMyProfileData();
		registerMessageReceiver(100, MessageKeys.Action_MyProfileUpdate);

//		try {
//			MultiCallback multiCallback = new MultiCallback(true);
//			multiCallback.addView(mAvatarImageView);
//			GifDrawable gifDrawable = new GifDrawable(getResources(), R.drawable.f002);
//			mAvatarImageView.setImageDrawable(gifDrawable);
//			gifDrawable.start();
//			gifDrawable.setCallback(multiCallback);
//			log.i("refreshCurrentUserInfo===="+gifDrawable);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	@Override
	protected boolean onMessageReceive(Bundle bundle, String action) {
		if(MessageKeys.Action_MyProfileUpdate.equals(action)) {
			log.i("myprifilechanged onMessageReceive currentuser="+currentUser);
			refreshMyProfileData();
		}
		return super.onMessageReceive(bundle, action);
	}
	
	private void refreshMyProfileData() {
		mCoverImageView.load(currentUser.getCoverImage());
		mAvatarImageView.load(currentUser.getAvatarImage());
		mAboutTextView.setText(currentUser.getAbout());
		mInterestsLayout.setTags(currentUser.getInterests());
		if(currentUser.getFriends() != null) {
			mFriendsLayout.setAvatars(new ArrayList<AvatarAndName>(currentUser.getFriends()));
		} else {
			mFriendsLayout.setAvatars(null);
		}


	}
}
