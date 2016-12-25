package wenjh.akit.common.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import java.io.File;
import java.util.Random;

import wenjh.akit.R;
import wenjh.akit.common.asynctask.AsyncCallback;
import wenjh.akit.common.cropimg.CropImageActivity;
import wenjh.akit.common.view.Toaster;

/**
 * call {@link #onActivityResult(int, int, Intent)}
 * {@link #onRestoreInstanceState(Bundle)}
 * {@link #onSaveInstanceState(Bundle)}
 * {@link #onDestory()}
 * @author wjh
 *
 */
public class TakePictureHelper {
	private int mTakepictureRequestcode;
	private int mSelectpictureRequestcode;
	private int mCropRequestcode;
	
	private File mCameraTempFile;
	private File mCropTempFile;
	private int mCompressWidth = ImageConfigs.AVATARIMAGE_MAX_WIDTH, mCompressHeight = ImageConfigs.AVATARIMAGE_MAX_HEIGHT;
	private AsyncCallback<File> mCallback = null;
	private LogUtil log = new LogUtil(this);
	private boolean mCropImage;
	private Activity mActivity = null;
	private Fragment mFragment = null;
	private float mAspectX = 1, mAspectY = 1;
	
	public TakePictureHelper(Activity activity) {
		this.mActivity = activity;
		init();
	}
	
	public TakePictureHelper(Fragment fragment) {
		this.mFragment = fragment;
		init();
	}
	
	private void init() {
		Random random = new Random();
		mTakepictureRequestcode = random.nextInt(250);
		mSelectpictureRequestcode = random.nextInt(250);
		mCropRequestcode = random.nextInt(250);
	}
	
	public void setCompressSize(int compressWidth, int compressHeight) {
		this.mCompressHeight = compressHeight;
		this.mCompressWidth = compressWidth;
	}
	
	public void setCropImage(boolean cropImage) {
		this.mCropImage = cropImage;
	}
	
	public void setCropAspect(float aspectX, float aspectY) {
		this.mAspectX = aspectX;
		this.mAspectY = aspectY;
	}
	
	private void startActivityForResult(Intent intent, int requestCode) {
		if(mActivity != null) {
			mActivity.startActivityForResult(intent, requestCode);
		} else if(mFragment != null) {
			mFragment.startActivityForResult(intent, requestCode);
		} else {
			throw new NullPointerException("Activity and Fragment is null");
		}
	}
	
	private Activity getActivity() {
		if(mActivity != null) {
			return mActivity;
		} else if(mFragment != null) {
			return mFragment.getActivity();
		} else {
			throw new NullPointerException("Activity and Fragment is null");
		}
	}
	
	public void takeNewPictureFromCamera() {
		try {
			mCameraTempFile = new File(ContextUtil.getCacheDir(), System.currentTimeMillis()+"");
			Intent getImageByCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCameraTempFile));
			startActivityForResult(getImageByCamera, mTakepictureRequestcode);
 		} catch (ActivityNotFoundException e) {
			Toaster.show(R.string.error_no_camera);
		}
	}
	
	public void takeNewPictureFromAlbum() {
		try {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(intent, mSelectpictureRequestcode);
		} catch (ActivityNotFoundException e) {
			Toaster.show(R.string.error_photoalbum);
		}
	}
	
	public void setCallback(AsyncCallback<File> callback) {
		this.mCallback = callback;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		log.d("onActivityResult, requestCode="+requestCode+", resultCode="+resultCode+", data="+data);
		if(resultCode == Activity.RESULT_OK) {
			if(requestCode == mSelectpictureRequestcode) {
				Uri originalUri = null;
				if (data != null && (originalUri = data.getData()) != null) {
					if(mCropImage) {
						gotoCropImage(originalUri);
					} else {
						compressImageAndCallback(originalUri);
					}
				}
			} else if(requestCode == mTakepictureRequestcode) {
				if(mCameraTempFile != null) {
					if(mCropImage) {
						gotoCropImage(Uri.fromFile(mCameraTempFile));
					} else {
						compressImageAndCallback(Uri.fromFile(mCameraTempFile));
					}
					mCameraTempFile = null;
				}
			} else if(requestCode == mCropRequestcode) {
				if(mCropTempFile != null) {
					compressImageAndCallback(Uri.fromFile(mCropTempFile));
				}
			}
		}
	}
	
	private void compressImageAndCallback(Uri imageUri) {
		File storeFile = new File(ContextUtil.getCacheDir(),System.currentTimeMillis()+"");
		boolean result = ImageUtil.compressedAndSaveAs(imageUri, storeFile, mCompressWidth, mCompressHeight);
		if(result) {
			callback(storeFile);
		} else {
			Toaster.show(R.string.error_compress_failed);
		}
	}
	
	private void gotoCropImage(Uri uri) {
		mCropTempFile = new File(ContextUtil.getCacheDir(),System.currentTimeMillis()+"");
		Intent intent = new Intent(ContextUtil.getContext(), CropImageActivity.class);
		intent.setData(uri);
		intent.putExtra(CropImageActivity.KEY_OUTPUT_FILE_PATH, mCropTempFile.getPath());
		intent.putExtra(CropImageActivity.KEY_CROP_MIN_SIZE, 150);
		intent.putExtra(CropImageActivity.KEY_CROP_ASPECT_X, 2);
		intent.putExtra(CropImageActivity.KEY_CROP_ASPECT_Y, 1);
		startActivityForResult(intent, mCropRequestcode);
	}
	
	private void callback(File file) {
		if(mCallback != null) {
			mCallback.callback(file);
		} else {
			log.w(new Exception("Callback missed. Callback is null."));
		}
	}
	
	public void onDestory() {
		mActivity = null;
		mFragment = null;
	}
	
	public void onSaveInstanceState(Bundle savedInstanceState) {
		if(mCameraTempFile != null) {
			savedInstanceState.putString("mCameraTempFile", mCameraTempFile.getPath());
		}
		if(mCropTempFile != null) {
			savedInstanceState.putString("mCropTempFile", mCropTempFile.getPath());
		}
		savedInstanceState.putBoolean("mCropImage", mCropImage);
		savedInstanceState.putInt("selectpictureRequestcode", mSelectpictureRequestcode);
		savedInstanceState.putInt("takepictureRequestcode", mTakepictureRequestcode);
		savedInstanceState.putInt("cropRequestcode", mCropRequestcode);
		savedInstanceState.putFloat("mAspectX", mAspectX);
		savedInstanceState.putFloat("mAspectY", mAspectY);
	}
	
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		String cameraTempFilePath = savedInstanceState.getString("mCameraTempFile");
		if(!StringUtil.isEmpty(cameraTempFilePath)) {
			mCameraTempFile = new File(cameraTempFilePath);
		}
		String cropTempFilePath = savedInstanceState.getString("mCropTempFile");
		if(!StringUtil.isEmpty(cropTempFilePath)) {
			mCropTempFile = new File(cropTempFilePath);
		}
		mCropImage = savedInstanceState.getBoolean("mCropImage");
		mSelectpictureRequestcode = savedInstanceState.getInt("selectpictureRequestcode");
		mTakepictureRequestcode = savedInstanceState.getInt("takepictureRequestcode");
		mCropRequestcode = savedInstanceState.getInt("cropRequestcode");
		mAspectY = savedInstanceState.getFloat("mAspectY");
		mAspectX = savedInstanceState.getFloat("mAspectX");
	}
	
	public void chooseOrTake(String title) {
		String[] items = new String[]{"Camera", "Photo Gallery"};
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 1:
					takeNewPictureFromAlbum();
					break;
				case 0:
					takeNewPictureFromCamera();
					break;
				default:
					break;
				}
				dialog.dismiss();
			}
		});
		if(title != null) {
			builder.setTitle(title);
		} else {
			builder.setTitle(R.string.choose_picture);
		}
		builder.create().show();
	}

	abstract class ImageConfigs {
		public final static int JPEG_QUALITY = 85;
		public final static int CHATIMAGE_MAX_WIDTH = 720;
		public final static int CHATIMAGE_MAX_HEIGHT = 1000;
		public final static int AVATARIMAGE_MAX_WIDTH = 720;
		public final static int AVATARIMAGE_MAX_HEIGHT = 720;
		public final static int IMAGE_MIDDLE_WIDTH = 200;
		public final static int IMAGE_MIDDLE_HEIGHT = 200;
		public final static int IMAGE_SMALL_WIDTH = 96;
		public final static int IMAGE_SMALL_HEIGHT = 96;
		public final static int USERCOVER_WIDTH = 720;
		public final static int USERCOVER_HEIGHT = 720;
	}
}
