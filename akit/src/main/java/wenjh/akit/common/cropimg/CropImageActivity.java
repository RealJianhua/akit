package wenjh.akit.common.cropimg;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import wenjh.akit.R;
import wenjh.akit.common.util.ContextUtil;
import wenjh.akit.common.util.ImageUtil;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.view.MProgressDialog;

public class CropImageActivity extends MonitoredActivity {
	private static final int SIZE_CROP_MAX = 960;
	public static final int RESULT_CROP_ERROR = 1000;
	public static final int RESULT_SAVE_ERROR = 1001;
	public static final int RESULT_IMAGE_LOAD_ERROR = 1002;
	public static final int RESULT_SIZE_ERROR = 1003;
	public static final String KEY_CROP_ASPECT_X = "aspectX";
	public static final String KEY_CROP_ASPECT_Y = "aspectY";
	public static final String KEY_OUTPUT_WIDTH = "outputX";
	public static final String KEY_OUTPUT_HEIGHT = "outputY";
	public static final String KEY_OUTPUT_CANSCALE = "scale";
	public static final String KEY_OUTPUT_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
	public static final String KEY_OUTPUT_SAVE_QUALITY = "saveQuality";
	public static final String KEY_OUTPUT_FILE_PATH = "outputFilePath";
	public static final String KEY_CROP_MIN_SIZE = "minsize";
	public static final String KEY_ORIGIN_MAX_WIDTH = "maxwidth";
	public static final String KEY_ORIGIN_MAX_HEIGHT = "maxheight";

	// Params
	private boolean mCircleCrop = false;
	private Uri mOriginalImageUri;
	private int mOriginReadMaxHeight = ContextUtil.getScreenWidth() * 2;
	private int mOriginReadMaxWidth = mOriginReadMaxHeight;
	private float mCropAspectX, mCropAspectY;
	private int mOutputWidth, mOutputHeight;
	private String mOutputFilePath;
	private boolean mOutputCanScale;
	private boolean mOutputScaleUp;
	private int mOutputImageQuality;
	private int mCropMinPix;
	private Bitmap mOriginBitmap;
	private int mSrcImageWidth, mSrcImageHeight = 0;

	private float mCurrentRotate = 0;
	private CropImageView mImageView;
	private HighlightView mCrop;
	private LogUtil log = new LogUtil("CropImageActivity");
	private final Handler mHandler = new Handler();
	private boolean mSaving;

	@Override
	protected void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setContentView(R.layout.activity_imagecrop);
		System.gc();
		mImageView = (CropImageView) findViewById(R.id.imageview);
		readParams(getIntent());
	}

	@Override
	protected void initViews() {

	}

	@Override
	protected void initDatas() {

	}

	@Override
	protected void initEvents() {

	}

	private void readParams(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras == null) {
			setResult(RESULT_IMAGE_LOAD_ERROR);
			finish();
			return;
		}

		mOriginalImageUri = intent.getData();
		mOutputWidth = extras.getInt(KEY_OUTPUT_WIDTH);
		mOutputHeight = extras.getInt(KEY_OUTPUT_HEIGHT);
		mOutputCanScale = extras.getBoolean(KEY_OUTPUT_CANSCALE, true);
		mOutputScaleUp = extras.getBoolean(KEY_OUTPUT_SCALE_UP_IF_NEEDED, true);
		mOutputImageQuality = extras.getInt(KEY_OUTPUT_SAVE_QUALITY, 85);
		mOutputFilePath = (String) extras.get(KEY_OUTPUT_FILE_PATH);
		mOriginReadMaxWidth = extras.getInt(KEY_ORIGIN_MAX_WIDTH, mOriginReadMaxWidth);
		mOriginReadMaxHeight = extras.getInt(KEY_ORIGIN_MAX_HEIGHT, mOriginReadMaxHeight);

		mCropAspectX = extras.getFloat(KEY_CROP_ASPECT_X, 1f);
		mCropAspectY = extras.getFloat(KEY_CROP_ASPECT_Y, 1);
		mCropMinPix = extras.getInt(KEY_CROP_MIN_SIZE, 0);
		if (mCropMinPix < 0) {
			mCropMinPix = 0;
		}

		if (mOriginalImageUri == null) {
			setResult(RESULT_IMAGE_LOAD_ERROR);
			finish();
			return;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(getApplicationContext());
		inflater.inflate(R.menu.cropimage, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.cropimage_menu_rotate) {
			rotate();
			return true;
		} else if (item.getItemId() == R.id.cropimage_menu_save) {
			onSaveClicked();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onStart() {
		super.onStart();
		if (mOriginBitmap == null || mOriginBitmap.isRecycled()) {
			mOriginBitmap = decodeBitmap();
		}

		if (mOriginBitmap == null) {
			setResult(RESULT_IMAGE_LOAD_ERROR);
			finish();
			return;
		}

		mCurrentRotate = 0;
		startFaceDetection();
	}

	public void rotate() {
		mCurrentRotate += 90;
		if (mCurrentRotate >= 360) {
			mCurrentRotate = 0;
		}

		Matrix matrix = new Matrix();
		matrix.setRotate(90, 0.5f, 0.5f);
		Bitmap b = Bitmap.createBitmap(mOriginBitmap, 0, 0, mOriginBitmap.getWidth(), mOriginBitmap.getHeight(), matrix, true);

		Bitmap old = mOriginBitmap;
		mOriginBitmap = b;

		mImageView.reset();

		startFaceDetection();
		old.recycle();
		old = null;
		System.gc();
	}

	public void onStop() {
		super.onStop();
		if (mOriginBitmap != null) {
			mOriginBitmap.recycle();
		}
	}

	private Bitmap decodeBitmap() {
		log.i("decodeBitmap, mOriginReadMaxWidth="+mOriginReadMaxWidth+", mOriginReadMaxHeight="+mOriginReadMaxHeight);
		return ImageUtil.loadResizedBitmap(mOriginalImageUri, getApplicationContext(), mOriginReadMaxWidth, mOriginReadMaxHeight);
	}

	private void startFaceDetection() {
		if (isFinishing()) {
			return;
		}

		mSrcImageHeight = mOriginBitmap.getHeight();
		mSrcImageWidth = mOriginBitmap.getWidth();
		mImageView.setSrcSize(mSrcImageWidth, mSrcImageHeight);
		mImageView.setMinpix(mCropMinPix);

		log.d("startFaceDetection, srcHeight=" + mSrcImageHeight + ", srcWidth=" + mSrcImageWidth);
		log.d("startFaceDetection, mImageView.getMinpix()=" + mImageView.getMinpix());
		if (mSrcImageHeight < mImageView.getMinpix() || mSrcImageWidth < mImageView.getMinpix()) {
			setResult(RESULT_SIZE_ERROR);
			finish();
			return;
		}

		mImageView.setImageBitmapResetBase(mOriginBitmap, true);
		startBackgroundJob(this, null, getResources().getString(R.string.progress), new Runnable() {
			public void run() {
				final CountDownLatch latch = new CountDownLatch(1);
				final Bitmap b = mOriginBitmap;
				mHandler.post(new Runnable() {
					public void run() {
						if (b != mOriginBitmap && b != null) {
							mImageView.setImageBitmapResetBase(b, true);
							if (mOriginBitmap != null) {
								mOriginBitmap.recycle();
							}
							mOriginBitmap = b;
						}
						if (mImageView.getScale() == 1F) {
							mImageView.center(true, true);
						}
						latch.countDown();
					}
				});

				try {
					latch.await();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				mRunFaceDetection.run();
			}
		}, mHandler);
	}

	private static class BackgroundJob extends MonitoredActivity.LifeCycleAdapter implements Runnable {
		private final MonitoredActivity mActivity;
		private final ProgressDialog mDialog;
		private final Runnable mJob;
		private final Handler mHandler;
		private final Runnable mCleanupRunner = new Runnable() {
			public void run() {
				mActivity.removeLifeCycleListener(BackgroundJob.this);
				if (mDialog.getWindow() != null)
					mDialog.dismiss();
			}
		};

		public BackgroundJob(MonitoredActivity activity, Runnable job, ProgressDialog dialog, Handler handler) {
			mActivity = activity;
			mDialog = dialog;
			mJob = job;
			mActivity.addLifeCycleListener(this);
			mHandler = handler;
		}

		public void run() {
			try {
				mJob.run();
			} finally {
				mHandler.post(mCleanupRunner);
			}
		}

		@Override
		public void onActivityDestroyed(MonitoredActivity activity) {
			// We get here only when the onDestroyed being called before
			// the mCleanupRunner. So, run it now and remove it from the queue
			mCleanupRunner.run();
			mHandler.removeCallbacks(mCleanupRunner);
		}

		@Override
		public void onActivityStopped(MonitoredActivity activity) {
			mDialog.hide();
		}

		@Override
		public void onActivityStarted(MonitoredActivity activity) {
			mDialog.show();
		}
	}

	private static void startBackgroundJob(MonitoredActivity activity, String title, String message, Runnable job, Handler handler) {
		// Make the progress dialog uncancelable, so that we can gurantee
		// the thread will be done before the activity getting destroyed.
		ProgressDialog dialog = ProgressDialog.show(activity, title, message, true, false);
		new Thread(new BackgroundJob(activity, job, dialog, handler)).start();
	}

	Runnable mRunFaceDetection = new Runnable() {
		float mScale = 1F;
		Matrix mImageMatrix;

		// Create a default HightlightView if we found no face in the picture.
		private void makeDefault() {
			HighlightView hv = new HighlightView(mImageView);

			int width = mOriginBitmap.getWidth();
			int height = mOriginBitmap.getHeight();

			Rect imageRect = new Rect(0, 0, width, height);

			// make the default size about 4/5 of the width or height
			int cropWidth = Math.min(width, height) * 4 / 5;
			int cropHeight = cropWidth;

			if (mCropAspectX != 0 && mCropAspectY != 0) {
				if (mCropAspectX > mCropAspectY) {
					cropHeight = (int) (cropWidth * mCropAspectY / mCropAspectX);
				} else {
					cropWidth = (int) (cropHeight * mCropAspectX / mCropAspectY);
				}
			}

			int x = (width - cropWidth) / 2;
			int y = (height - cropHeight) / 2;

			RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
			hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop, mCropAspectX != 0 && mCropAspectY != 0);
			mImageView.add(hv);
		}

		public void run() {
			mImageMatrix = mImageView.getImageMatrix();

			mScale = 1.0F / mScale;
			mHandler.post(new Runnable() {
				public void run() {
					if (mImageView.mHighlightViews.size() < 1) {
						makeDefault();
					}

					mImageView.invalidate();
					if (mImageView.mHighlightViews.size() == 1) {
						mCrop = mImageView.mHighlightViews.get(0);
						mCrop.setFocus(true);
					}
				}
			});
		}
	};

	private void onSaveClicked() {
		if (mCrop == null) {
			return;
		}
		if (mSaving) {
			return;
		}

		mSaving = true;
		new CropTask().execute();
	}

	private class CropTask extends AsyncTask<Object, Object, Object> {
		Uri returnedUri = null;
		File returnedFile = null;

		@Override
		protected void onPreExecute() {
			mImageView.clear();
			mImageView.mHighlightViews.clear();
			mOriginBitmap.recycle();
			mOriginBitmap = null;
			System.gc();
			showDialog(new MProgressDialog(CropImageActivity.this, getString(R.string.progress)));
		}

		@Override
		protected Object doInBackground(Object... params) {
			doCropAndSave();
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			closeDialog();

			Intent i = new Intent();

			boolean cropSuccess = true;
			if (returnedUri != null) {
				i.setData(returnedUri);
				setResult(RESULT_OK, i);
			} else if (returnedFile != null) {
				i.putExtra(KEY_OUTPUT_FILE_PATH, returnedFile.getAbsolutePath());
				setResult(RESULT_OK, i);
			} else {
				cropSuccess = false;
				setResult(RESULT_SAVE_ERROR, i);
			}

			finish();

			super.onPostExecute(result);
		}

		private void doCropAndSave() {
			Bitmap croppedImage;
			int load_scale = 1;

			try {
				mOriginBitmap = decodeBitmap();
			} catch (Throwable e) {
				setResult(RESULT_CROP_ERROR);
				finish();
				return;
			}
			if (mCurrentRotate > 0) {
				Matrix m = new Matrix();
				m.setRotate(mCurrentRotate, 0.5f, 0.5f);
				Bitmap old = mOriginBitmap;
				Bitmap bitmap = Bitmap.createBitmap(mOriginBitmap, 0, 0, mOriginBitmap.getWidth(), mOriginBitmap.getHeight(), m, true);

				old.recycle();
				old = null;

				mOriginBitmap = bitmap;
				System.gc();
			}

			if (mOriginBitmap == null) {
				Intent i = new Intent();
				setResult(RESULT_CROP_ERROR, i);
				return;
			}

			if (mOutputWidth != 0 && mOutputHeight != 0 && !mOutputCanScale) {
				croppedImage = Bitmap.createBitmap(mOutputWidth, mOutputHeight, Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(croppedImage);

				Rect srcRect = mCrop.getCropRect();
				Rect dstRect = new Rect(0, 0, mOutputWidth, mOutputHeight);

				int dx = (srcRect.width() - dstRect.width()) / 2;
				int dy = (srcRect.height() - dstRect.height()) / 2;

				// If the srcRect is too big, use the center part of it.
				srcRect.inset(Math.max(0, dx), Math.max(0, dy));
				// If the dstRect is too big, use the center part of it.
				dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));
				// Draw the cropped bitmap in the center
				canvas.drawBitmap(mOriginBitmap, srcRect, dstRect, null);

				mOriginBitmap.recycle();
				System.gc();
			} else {
				Rect r = mCrop.getCropRect();
				r.set(r.left / load_scale, r.top / load_scale, r.right / load_scale, r.bottom / load_scale);
				int width = r.width();
				int height = r.height();

				croppedImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

				Canvas canvas = new Canvas(croppedImage);
				Rect dstRect = new Rect(0, 0, width, height);
				canvas.drawBitmap(mOriginBitmap, r, dstRect, null);

				mOriginBitmap.recycle();
				System.gc();

				if (mOutputWidth != 0 && mOutputHeight != 0 && mOutputCanScale) {
					croppedImage = transform(new Matrix(), croppedImage, mOutputWidth, mOutputHeight, mOutputScaleUp, true);
				}

			}

			boolean addToGallery = (mOutputFilePath == null);

			try {
				if (addToGallery) {
					returnedUri = ImageTricks.putBitmapIntoGalleryAndGetUri(CropImageActivity.this, croppedImage, true);
				} else {
					File tmpFile = new File(mOutputFilePath);
					log.i("croppedImage.getWidth()=" + croppedImage.getWidth() + ",croppedImage.getHeight()=" + croppedImage.getHeight());
					FileOutputStream outputStream = new FileOutputStream(tmpFile);
					croppedImage.compress(CompressFormat.JPEG, mOutputImageQuality, outputStream);
					outputStream.close();
					croppedImage.recycle();
					returnedFile = tmpFile;

				}
				System.gc();
			} catch (Exception e) {
				log.e(e);
				try {
					croppedImage.recycle();
					System.gc();
				} catch (Exception e1) {
				}
			}
		}

	}

	private static Bitmap transform(Matrix scaler, Bitmap source, int targetWidth, int targetHeight, boolean scaleUp, boolean recycle) {
		int deltaX = source.getWidth() - targetWidth;
		int deltaY = source.getHeight() - targetHeight;
		if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
			Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b2);

			int deltaXHalf = Math.max(0, deltaX / 2);
			int deltaYHalf = Math.max(0, deltaY / 2);
			Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf + Math.min(targetWidth, source.getWidth()), deltaYHalf
					+ Math.min(targetHeight, source.getHeight()));
			int dstX = (targetWidth - src.width()) / 2;
			int dstY = (targetHeight - src.height()) / 2;
			Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY);
			c.drawBitmap(source, src, dst, null);
			if (recycle) {
				source.recycle();
				System.gc();
			}
			return b2;
		}
		float bitmapWidthF = source.getWidth();
		float bitmapHeightF = source.getHeight();

		float bitmapAspect = bitmapWidthF / bitmapHeightF;
		float viewAspect = (float) targetWidth / targetHeight;

		if (bitmapAspect > viewAspect) {
			float scale = targetHeight / bitmapHeightF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		} else {
			float scale = targetWidth / bitmapWidthF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		}

		Bitmap b1;
		if (scaler != null) {
			// this is used for minithumb and crop, so we want to filter here.
			b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), scaler, true);
		} else {
			b1 = source;
		}

		if (recycle && b1 != source) {
			source.recycle();
		}

		int dx1 = Math.max(0, b1.getWidth() - targetWidth);
		int dy1 = Math.max(0, b1.getHeight() - targetHeight);

		Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth, targetHeight);

		if (b2 != b1) {
			if (recycle || b1 != source) {
				b1.recycle();
			}
		}

		return b2;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mImageView.clear();
		mImageView.mHighlightViews.clear();
		if (mOriginBitmap != null) {
			mOriginBitmap.recycle();
			mOriginBitmap = null;
		}

		System.gc();
	}

}

class CropImageView extends ImageViewTouchBase {
	ArrayList<HighlightView> mHighlightViews = new ArrayList<HighlightView>();
	HighlightView mMotionHighlightView = null;
	float mLastX, mLastY;
	int mMotionEdge;
	int minpix = 0;
	int srcWidth, srcHeight = 0;

	public CropImageView(Context context) {
		super(context);
		if (ContextUtil.isIcsVsersion()) {
			setLayerType(LAYER_TYPE_SOFTWARE, null);
		}
	}

	public CropImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (ContextUtil.isIcsVsersion()) {
			setLayerType(LAYER_TYPE_SOFTWARE, null);
		}
	}

	@SuppressLint("Instantiatable")
	public CropImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		if (ContextUtil.isIcsVsersion()) {
			setLayerType(LAYER_TYPE_SOFTWARE, null);
		}
	}

	void reset() {
		mHighlightViews.clear();
		mMotionHighlightView = null;
		mLastX = 0;
		mLastY = 0;
		mMotionEdge = 0;
		minpix = 0;
		srcWidth = 0;
		srcHeight = 0;
	}

	public void setSrcSize(int srcWidth, int srcHeight) {
		this.srcHeight = srcHeight;
		this.srcWidth = srcWidth;
	}

	/**
	 * wjh添加。设置被框选的部分最小的size（像素单位），当被框选宽或高小于此值时，操作被弹回。
	 *
	 * @param minpix
	 */
	public void setMinpix(int minpix) {
		this.minpix = minpix;
	}

	public int getMinpix() {
		return minpix;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mBitmapDisplayed.getBitmap() != null) {
			for (HighlightView hv : mHighlightViews) {
				hv.mMatrix.set(getImageMatrix());
				hv.invalidate();
				if (hv.mIsFocused) {
					centerBasedOnHighlightView(hv);
				}
			}
		}
	}

	@Override
	protected void zoomTo(float scale, float centerX, float centerY) {
		super.zoomTo(scale, centerX, centerY);
		for (HighlightView hv : mHighlightViews) {
			hv.mMatrix.set(getImageMatrix());
			hv.invalidate();
		}
	}

	@Override
	protected void zoomIn() {
		super.zoomIn();
		for (HighlightView hv : mHighlightViews) {
			hv.mMatrix.set(getImageMatrix());
			hv.invalidate();
		}
	}

	@Override
	protected void zoomOut() {
		super.zoomOut();
		for (HighlightView hv : mHighlightViews) {
			hv.mMatrix.set(getImageMatrix());
			hv.invalidate();
		}
	}

	@Override
	protected void postTranslate(float deltaX, float deltaY) {
		super.postTranslate(deltaX, deltaY);
		for (int i = 0; i < mHighlightViews.size(); i++) {
			HighlightView hv = mHighlightViews.get(i);
			hv.mMatrix.postTranslate(deltaX, deltaY);
			hv.invalidate();
		}
	}

	float rotate = 0;

	protected void rotate() {
		// Matrix tmp = new Matrix(mSuppMatrix);
		mSuppMatrix.postRotate(90, 0.5f, 0.5f);
		setImageMatrix(mSuppMatrix);
		center(true, true);

		for (HighlightView hv : mHighlightViews) {
			hv.mMatrix.set(mSuppMatrix);
			hv.invalidate();
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			for (int i = 0; i < mHighlightViews.size(); i++) {
				HighlightView hv = mHighlightViews.get(i);
				int edge = hv.getHit(event.getX(), event.getY());
				if (edge != HighlightView.GROW_NONE) {
					mMotionEdge = edge;
					mMotionHighlightView = hv;
					mLastX = event.getX();
					mLastY = event.getY();
					mMotionHighlightView.setMode((edge == HighlightView.MOVE) ? HighlightView.ModifyMode.Move : HighlightView.ModifyMode.Grow);
					break;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mMotionHighlightView != null) {
				centerBasedOnHighlightView(mMotionHighlightView);
				mMotionHighlightView.setMode(HighlightView.ModifyMode.None);
			}
			mMotionHighlightView = null;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMotionHighlightView != null) {
				mMotionHighlightView.handleMotion(mMotionEdge, event.getX() - mLastX, event.getY() - mLastY);
				mLastX = event.getX();
				mLastY = event.getY();

				if (true) {
					// This section of code is optional. It has some user
					// benefit in that moving the crop rectangle against
					// the edge of the screen causes scrolling but it means
					// that the crop rectangle is no longer fixed under
					// the user's finger.
					ensureVisible(mMotionHighlightView);
				}
			}
			break;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			center(true, true);
			break;
		case MotionEvent.ACTION_MOVE:
			// if we're not zoomed then there's no point in even allowing
			// the user to move the image around. This call to center puts
			// it back to the normalized location (with false meaning don't
			// animate).
			if (getScale() == 1F) {
				center(true, true);
			}
			break;
		}

		return true;
	}

	// Pan the displayed image to make sure the cropping rectangle is visible.
	private void ensureVisible(HighlightView hv) {
		Rect r = hv.mDrawRect;

		int panDeltaX1 = Math.max(0, getLeft() - r.left);
		int panDeltaX2 = Math.min(0, getRight() - r.right);

		int panDeltaY1 = Math.max(0, getTop() - r.top);
		int panDeltaY2 = Math.min(0, getBottom() - r.bottom);

		int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
		int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

		if (panDeltaX != 0 || panDeltaY != 0) {
			panBy(panDeltaX, panDeltaY);
		}
	}

	// If the cropping rectangle's size changed significantly, change the
	// view's center and scale according to the cropping rectangle.
	private void centerBasedOnHighlightView(HighlightView hv) {
		Rect drawRect = hv.mDrawRect;
		float width = drawRect.width();
		float height = drawRect.height();

		// 当被框选宽或高小于minpix时，操作被弹回。---
		Rect rc = hv.getCropRect();
		float hWidth = rc.width();
		float hHeight = rc.height();
		if (hWidth < minpix && mMotionHighlightView != null) {
			int r = (int) (minpix - hWidth);
			mMotionEdge = HighlightView.GROW_LEFT_EDGE;
			mMotionHighlightView.handleMotion(mMotionEdge, -r, -r);

			ensureVisible(mMotionHighlightView);

			drawRect = hv.mDrawRect;
			width = drawRect.width();
			height = drawRect.height();
		} else if (hHeight < minpix && mMotionHighlightView != null) {
			int r = (int) (minpix - hHeight);
			mMotionEdge = HighlightView.GROW_BOTTOM_EDGE;
			mMotionHighlightView.handleMotion(mMotionEdge, -r, r);

			ensureVisible(mMotionHighlightView);

			drawRect = hv.mDrawRect;
			width = drawRect.width();
			height = drawRect.height();
		}

		float thisWidth = getWidth();
		float thisHeight = getHeight();

		float z1 = thisWidth / width * .6F;
		float z2 = thisHeight / height * .6F;

		float zoom = Math.min(z1, z2);
		zoom = zoom * this.getScale();
		zoom = Math.max(1F, zoom);

		if ((Math.abs(zoom - getScale()) / zoom) > .1) {
			float[] coordinates = new float[] { hv.mCropRect.centerX(), hv.mCropRect.centerY() };
			getImageMatrix().mapPoints(coordinates);
			zoomTo(zoom, coordinates[0], coordinates[1], 300F);
		}

		ensureVisible(hv);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			super.onDraw(canvas);
			for (int i = 0; i < mHighlightViews.size(); i++) {
				mHighlightViews.get(i).draw(canvas);
			}
		} catch (Exception e) {
		}
	}

	public void add(HighlightView hv) {
		mHighlightViews.add(hv);
		invalidate();
	}
}
