package wenjh.akit.demo.img.ui;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.TextView;

import wenjh.akit.activity.base.TransparentActionBarActivity;
import wenjh.akit.common.util.FileUtil;
import wenjh.akit.common.util.StringUtil;
import wenjh.akit.common.view.MProgressDialog;
import wenjh.akit.common.view.ScrollViewPager;
import wenjh.akit.common.view.Toaster;
import wenjh.akit.common.view.photoview.PhotoView;
import wenjh.akit.common.asynctask.BaseTask;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.util.PicassoUtil;
import wenjh.akit.demo.ContextUtil;
import wenjh.akit.demo.config.StorageConfigs;

import com.squareup.picasso.MemoryPolicy;
import com.wenjh.akit.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageBrowserActivity extends TransparentActionBarActivity {
	private LogUtil log = new LogUtil("ImageBrowserActivity");
	public final static String KEY_IMAGEARRAY_SMALL_URL = "small_url_array";
	public final static String KEY_IMAGEARRAY_LARGE_URL = "large_url_array";
	public final static String KEY_INDEX = "index";
	public final static String KEY_CAN_SAVE = "save";
	public final static String KEY_SHOWTITLE = "showtitle";

	private ImageItemViewAdapter mAdapter = null;
	private ScrollViewPager mViewPager = null;
	private TextView mPageTextView = null;
	private View mTitleViewLayout = null;
	private View mSaveImageButton = null;
	private List<ImageItem> mImageList = null;
	private Handler mHandler = new Handler();
	private boolean mShowTitle = true;
	private int mLastSelectedPosition = -1;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setContentView(R.layout.activity_imagebrowser);
		initViews();
		initEvents();
		initDatas();
	}

	public void initViews() {
		mViewPager = (ScrollViewPager) findViewById(R.id.viewpager);
		mPageTextView = (TextView) findViewById(R.id.imagebrower_tv_page);
		mTitleViewLayout = findViewById(R.id.imagebrower_layout_pagelayout);
		mSaveImageButton = findViewById(R.id.imagebrower_iv_save);
	}

	public void initEvents() {
		mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				if (mImageList.size() == 2) {
					twoImagesSizePageSelected(position);
				} else {
					int imageIndex = convertPagerPositionToImageIndex(position);
					onImageItemSelected(mViewPager.findViewById(imageIndex), position);
				}
			}
		});
		
		boolean canSave = getIntent().getBooleanExtra(KEY_CAN_SAVE, false);
		if (canSave) {
			mSaveImageButton.setVisibility(canSave ? View.VISIBLE : View.GONE);
			mSaveImageButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					execAsyncTask(new SaveImageToLocalTask(ImageBrowserActivity.this));
				}
			});
		}

		mShowTitle = getIntent().getBooleanExtra(KEY_SHOWTITLE, true);
		mTitleViewLayout.setVisibility(mShowTitle ? View.VISIBLE : View.GONE);
	}
	
	protected void initDatas() {
		// read images, uri
		List<ImageItem> images = new ArrayList<ImageItem>();
		String[] thumbArray = getIntent().getStringArrayExtra(KEY_IMAGEARRAY_SMALL_URL);
		String[] largeArray = getIntent().getStringArrayExtra(KEY_IMAGEARRAY_LARGE_URL);
		if (thumbArray == null && largeArray == null) {
			finish();
			return;
		}
		
		int thumbCount = thumbArray != null ? thumbArray.length : 0;
		int largeCount = largeArray != null ? largeArray.length : 0;
		int count = thumbCount > largeCount ? thumbCount : largeCount;
		if(count == 0) {
			finish();
			return;
		}
		
		for (int i = 0; i < count; i++) {
			ImageItem item = new ImageItem();
			if(i < thumbArray.length) {
				item.thumbUrl = thumbArray[i];
			}
			if (i < largeArray.length) {
				item.largeUrl = largeArray[i];
			}
			if(!StringUtil.isEmpty(item.thumbUrl) || !StringUtil.isEmpty(item.largeUrl)) {
				images.add(item);
			}
		}

		// read init index
		int index = getIntent().getIntExtra(KEY_INDEX, 0);
		index = index >= images.size() ? images.size() - 1 : index;
		index = index < 0 ? 0 : index;
		
		if (images.size() > 1) {
			index += (images.size() * 100);
		}

		this.mImageList = images;
		log.i(images);

		mAdapter = new ImageItemViewAdapter();
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(index, false);
		
		// call init index, onselected
		callItemSelected(index);
	}

	private void callItemSelected(final int index) {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				onImageItemSelected(mViewPager.findViewById(convertPagerPositionToImageIndex(index)), convertPagerPositionToImageIndex(index));
			}
		}, 100);
	}

	@Override
	public void onBackPressed() {
		// overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
		super.onBackPressed();
	}

	private void onImageItemSelected(final View v, int position) {
		if (v == null) {
			log.w("onImageItemSelected, v == null, position="+position);
			return;
		}

		position = convertPagerPositionToImageIndex(position);
		if (mShowTitle) {
			String page = (position + 1) + "/" + mImageList.size();
			SpannableString ss = new SpannableString(page);
			ss.setSpan(new RelativeSizeSpan(1.5f), 0, page.indexOf('/'), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			mPageTextView.setText(ss);
		}

		final ImageItem imageItem = mImageList.get(position);
		PhotoView currentPhotoView = (PhotoView) v;
		Drawable drawable = currentPhotoView.getDrawable();
		if (drawable != null && drawable instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			Bitmap b = bitmapDrawable.getBitmap();
			if (b != null && b == peekMemoryBitmap(imageItem, true)) {
				log.d("big image already shown.");
				return;
			}
		}

		currentPhotoView.setFillPlace(false);
		currentPhotoView.load(imageItem.largeUrl);
		log.i("onItemSelected, position=" + position + ", largeUrl=" + imageItem.largeUrl);
	}

	private void twoImagesSizePageSelected(int position) {
		int imageIndex = convertPagerPositionToImageIndex(position);
		if (mLastSelectedPosition == -1) {
			mLastSelectedPosition = position;
		}

		boolean scrollToLeft = mLastSelectedPosition > position;
		mLastSelectedPosition = position;

		if (scrollToLeft) {
			for (int i = mViewPager.getChildCount() - 1; i >= 0; i--) {
				View v = mViewPager.getChildAt(i);
				if (v.getId() == imageIndex) {
					onImageItemSelected(v, position);
				}
			}
		} else {
			for (int i = 0; i < mViewPager.getChildCount(); i++) {
				View v = mViewPager.getChildAt(i);
				if (v.getId() == imageIndex) {
					onImageItemSelected(v, position);
				}
			}
		}
	}

	private int convertPagerPositionToImageIndex(int position) {
		if (position < mImageList.size()) {
			return position;
		} else {
			return position % mImageList.size();
		}
	}
	
	private Bitmap peekMemoryBitmap(ImageItem imageItem, boolean large) {
		Bitmap largeBitmap = null;
		try {
			largeBitmap = PicassoUtil.picasso().load(large ? imageItem.largeUrl : imageItem.thumbUrl)
					.memoryPolicy(MemoryPolicy.MEMORY_ONLY)
					.get();
		} catch (IOException e) {
			log.e(e);
		}
		return largeBitmap;
	}

	private class ImageItemViewAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return mImageList.size() > 1 ? Integer.MAX_VALUE : mImageList.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			position = convertPagerPositionToImageIndex(position);
			PhotoView photoView = (PhotoView) ContextUtil.getLayoutInflater().inflate(R.layout.include_imagebrower_item, null);
			photoView.setId(position);

			Bitmap largeBitmap = peekMemoryBitmap(mImageList.get(position), true);
			if (largeBitmap == null) {
				photoView.load(mImageList.get(position).thumbUrl);
			} else {
				photoView.setImageBitmap(largeBitmap);
			}
			container.addView(photoView);
			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}

	private static class ImageItem {
		String largeUrl;
		String thumbUrl;

		@Override
		public String toString() {
			return "largeUrl:"+largeUrl+", thumbUrl:"+thumbUrl;
		}
	}

	private class SaveImageToLocalTask extends BaseTask<Object, Object, Boolean> {
		ImageItem image = null;
		File targetFile = null;

		public SaveImageToLocalTask(Context context) {
			super(context);
			this.image = mImageList.get(mViewPager.getCurrentItem());
		}

		@Override
		protected void onPreTask() {
			showDialog(new MProgressDialog(ImageBrowserActivity.this, "Save..."));
		}

		@Override
		protected Boolean executeTask(Object... params) throws Exception {
			if(peekMemoryBitmap(image, true) == null) {
				// downloading or download failed.
				return false;
			}
			
			File dir = StorageConfigs.getUserImageSaveDir();
			targetFile = new File(dir, URLUtil.guessFileName(image.largeUrl, null, "image/jpeg"));
			File imageCacheFile = StorageConfigs.getImageCacheFileWithURL(image.largeUrl);
			if (!imageCacheFile.exists() || imageCacheFile.length() <= 0) {
				Toaster.showInvalidate("Image is downloading. Save failed.");
			} else {
				FileUtil.copyFile(imageCacheFile, targetFile);
				putBitmapIntoGalleryAndGetUri(getApplicationContext(), targetFile);
				return true;
			}

			return false;
		}

		@Override
		protected void onTaskError(Exception e) {
			log.e(e);
			toast("Save failed.");
		}

		@Override
		protected void onTaskFinish() {
			closeDialog();
		}
		
		@Override
		protected void onTaskSuccess(Boolean result) {
			if(result) {
				toast("Save success: " + targetFile.getPath());
			}
		}

		private Uri putBitmapIntoGalleryAndGetUri(Context c, File imageFile) {
			if (imageFile.exists() && imageFile.isFile()) {
				try {
					ContentValues values = new ContentValues();
					values.put(Images.Media.MIME_TYPE, "image/jpeg");
					values.put(Images.Media.DATA, imageFile.getAbsolutePath());
					values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
					values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());

					return c.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}
}