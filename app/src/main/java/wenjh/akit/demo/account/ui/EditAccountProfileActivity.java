package wenjh.akit.demo.account.ui;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.wenjh.akit.R;

import wenjh.akit.activity.base.BaseActivity;
import wenjh.akit.demo.ContextUtil;
import wenjh.akit.demo.account.model.AccountSettingPreference;
import wenjh.akit.common.http.NetworkBaseException;
import wenjh.akit.common.http.RangeUploadHandler;
import wenjh.akit.common.receiver.MessageKeys;
import wenjh.akit.common.util.StringUtil;
import wenjh.akit.common.util.TakePictureHelper;
import wenjh.akit.common.view.MProgressDialog;
import wenjh.akit.common.view.SmartImageView;
import wenjh.akit.demo.config.ImageConfigs;
import wenjh.akit.demo.people.UserApi;
import wenjh.akit.demo.people.model.User;
import wenjh.akit.demo.people.model.UserService;
import wenjh.akit.common.asynctask.AsyncCallback;
import wenjh.akit.common.asynctask.BaseTask;
import wenjh.akit.common.view.TagsEditText;

public class EditAccountProfileActivity extends BaseActivity {
	private static final int MENUITEM_SUBMIT = 12;
	private EditText mAboutView;
	private TagsEditText mTagsEditText;
	private SmartImageView mCoverImageView = null;
	private SmartImageView mAvatarImageView = null;
	private User mEditUser;
	private AccountSettingPreference mEditPreference;
	private UserService mUserService;
	private RangeUploadHandler.ResumableUploadData mEditCoverData;
	private RangeUploadHandler.ResumableUploadData mEditAvatarData;
	private TakePictureHelper mTakeCoverPictureHelper;
	private TakePictureHelper mTakeAvatarPictureHelper;
	private User currentUser;
	private AccountSettingPreference userPreference;
	
	// TODO check change
	
	@Override
	protected void onActivityCreated(Bundle savedInstanceState) {
		setContentView(R.layout.activity_editaccountprofile);
		mTakeCoverPictureHelper = new TakePictureHelper(this);
		mTakeAvatarPictureHelper = new TakePictureHelper(this);
		currentUser = ContextUtil.getCurrentUser();
		userPreference = ContextUtil.getCurrentPreference();
		initViews();
		initEvents();
		initDatas();
	}

	@Override
	protected void initViews() {
		mAboutView = (EditText) findViewById(R.id.edituserprofile_et_about);
		mTagsEditText = (TagsEditText) findViewById(R.id.edituserprofile_et_tags);
		mAvatarImageView = (SmartImageView) findViewById(R.id.edituserprofile_iv_avatar);
		mCoverImageView = (SmartImageView) findViewById(R.id.edituserprofile_iv_cover);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem = menu.add(1, MENUITEM_SUBMIT, 1, "Submit changed");
		menuItem.setIcon(android.R.drawable.ic_menu_save);
		menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == MENUITEM_SUBMIT) {
			doUpload();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void initDatas() {
		mUserService = new UserService();
		mEditUser = mUserService.getUser(currentUser.getId());
		if(mEditUser == null) {
			finish();
			return;
		}
		
		mEditPreference = new AccountSettingPreference(ContextUtil.getContext(), currentUser.getId());
		refreshUI();
	}

	private void refreshUI() {
		if(mEditUser.getAbout() != null) {
			mAboutView.setText(mEditUser.getAbout());
		} else {
			mAboutView.setText("");
		}
		
		mTagsEditText.getText().clear();
		if(mEditUser.getInterests() != null) {
			for (String interest : mEditUser.getInterests()) {
				mTagsEditText.append("{"+interest+"}");
			}
		}
		
		refreshAvatar();
		refreshCover();
	}
	
	private void refreshAvatar() {
		if(mEditAvatarData != null) {
			mAvatarImageView.load(mEditAvatarData.file);
		} else {
			mAvatarImageView.load(mEditUser.getAvatarImage());
		}
	}
	
	private void refreshCover() {
		if(mEditCoverData != null) {
			mCoverImageView.load(mEditCoverData.file);
		} else {
			mCoverImageView.load(mEditUser.getCoverImage());
		}
	}
	
	private void doUpload() {
		hideInputMethod();
		
		String editAbout = mAboutView.getText().toString();
		mEditUser.setAbout(editAbout);
		mEditUser.setInterests(mTagsEditText.getTags());
		execAsyncTask(new UploadEditedProfile(this, mEditCoverData, mEditAvatarData));
	}
	
	@Override
	protected void initEvents() {
		mAvatarImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mTakeAvatarPictureHelper.chooseOrTake(getString(R.string.edit_avatar));
				mTakeAvatarPictureHelper.setCompressSize(ImageConfigs.AVATARIMAGE_MAX_WIDTH, ImageConfigs.AVATARIMAGE_MAX_HEIGHT);
			}
		});
		
		mCoverImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mTakeCoverPictureHelper.setCompressSize(ImageConfigs.USERCOVER_WIDTH, ImageConfigs.USERCOVER_HEIGHT);
				mTakeCoverPictureHelper.chooseOrTake(getString(R.string.edit_cover));
			}
		});
		
		mTakeAvatarPictureHelper.setCallback(new AsyncCallback<File>() {
			@Override
			public void callback(File result) {
				log.d("mTakeAvatarPictureHelper callback, result="+result);
				if(result != null && result.exists()) {
					mEditAvatarData = new RangeUploadHandler.ResumableUploadData();
					mEditAvatarData.file = result;
					refreshAvatar();
				}
			}
		});
		
		mTakeCoverPictureHelper.setCallback(new AsyncCallback<File>() {
			@Override
			public void callback(File result) {
				log.d("mTakeCoverPictureHelper callback, result="+result);
				if(result != null && result.exists()) {
					mEditCoverData = new RangeUploadHandler.ResumableUploadData();
					mEditCoverData.file = result;
					refreshCover();
				}
			}
		});
		
		mTakeAvatarPictureHelper.setCropImage(true);
		mTakeCoverPictureHelper.setCropImage(true);
		mTakeCoverPictureHelper.setCropAspect(1, 1.5f);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mTakeCoverPictureHelper.onActivityResult(requestCode, resultCode, data);
		mTakeAvatarPictureHelper.onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mTakeCoverPictureHelper.onSaveInstanceState(outState);
		mTakeAvatarPictureHelper.onSaveInstanceState(outState);
		outState.putString("about", mAboutView.getText().toString());
		if(mEditAvatarData != null) {
			outState.putString("avatar", mEditAvatarData.toJson());
		}
		if(mEditCoverData != null) {
			outState.putString("cover", mEditCoverData.toJson());
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mTakeCoverPictureHelper.onSaveInstanceState(savedInstanceState);
		mTakeAvatarPictureHelper.onSaveInstanceState(savedInstanceState);
		mAboutView.setText(savedInstanceState.getString("about"));
		String avatarString = savedInstanceState.getString("avatar");
		if(!StringUtil.isEmpty(avatarString)) {
			mEditAvatarData = RangeUploadHandler.ResumableUploadData.parseJson(avatarString);
		}
		
		String coverString = savedInstanceState.getString("cover");
		if(!StringUtil.isEmpty(coverString)) {
			mEditCoverData = RangeUploadHandler.ResumableUploadData.parseJson(coverString);
		}
		
		refreshUI();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTakeAvatarPictureHelper.onDestory();
		mTakeCoverPictureHelper.onDestory();
	}
	
	private class UploadEditedProfile extends BaseTask<Object, Object, Object> {
		RangeUploadHandler.ResumableUploadData editCoverdata = null;
		RangeUploadHandler.ResumableUploadData editAvatardata = null;
		
		public UploadEditedProfile(Context context, RangeUploadHandler.ResumableUploadData editCover, RangeUploadHandler.ResumableUploadData editAvatar) {
			super(context);
			this.editAvatardata = editAvatar;
			this.editCoverdata = editCover;
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
			if(editCoverdata != null) {
				String guid = RangeUploadHandler.uploadUserCoverImage(editCoverdata);
				if(StringUtil.isEmpty(guid)) {
					throw new NetworkBaseException("Cover upload failed.");
				}
				mEditUser.setCover(guid);
				// reset upload data
				EditAccountProfileActivity.this.mEditCoverData = null;
			}
			
			if(editAvatardata != null) {
				String guid = RangeUploadHandler.uploadUserAvatarImage(editAvatardata);
				if(StringUtil.isEmpty(guid)) {
					throw new NetworkBaseException("Avatar upload failed.");
				}
				mEditUser.setAvatar(guid);
				// reset upload data
				EditAccountProfileActivity.this.mEditAvatarData = null;
			}
			
			UserApi.getInstance().updateUserProfile(mEditUser, mEditPreference);
			mUserService.saveFullUser(mEditUser);
			mEditPreference.saveAll();
			
			// Read changed profile to current account. !!
			mUserService.getUser(currentUser);
			userPreference.init();
			log.i("edit currentuser="+currentUser);
			
			// notify
			getApp().dispatchMessage(new Bundle(), MessageKeys.Action_MyProfileUpdate);
			return null;
		}
		
		@Override
		protected void onTaskSuccess(Object result) {
			toast(R.string.edit_success);
			finish();
		}
	}

}
