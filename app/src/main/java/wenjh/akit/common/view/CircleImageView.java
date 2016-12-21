package wenjh.akit.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.wenjh.akit.R;

import wenjh.akit.common.util.LogUtil;

public class CircleImageView extends SmartImageView {

	private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
	private static final int COLORDRAWABLE_DIMENSION = 1;

	private static final int DEFAULT_BORDER_WIDTH = 0;
	private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

	private final RectF mDrawableRect = new RectF();
	private final RectF mBorderRect = new RectF();

	private final Matrix mShaderMatrix = new Matrix();
	private final Paint mBitmapPaint = new Paint();
	private final Paint mBorderPaint = new Paint();

	private int mBorderColor = DEFAULT_BORDER_COLOR;
	private int mBorderWidth = DEFAULT_BORDER_WIDTH;

	private Bitmap mBitmap;
	private BitmapShader mBitmapShader;
	private int mBitmapWidth;
	private int mBitmapHeight;

	private float mDrawableRadius;
	private float mBorderRadius;

	private boolean mReady;
	private boolean mSetupPending;
	
	private LogUtil log = new LogUtil(this);

	public CircleImageView(Context context) {
		super(context);
		init();
	}

	public CircleImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0);
		mBorderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_border_width, DEFAULT_BORDER_WIDTH);
		mBorderColor = a.getColor(R.styleable.CircleImageView_border_color, DEFAULT_BORDER_COLOR);
        a.recycle();
		init();
	}

	private void init() {
		if(getScaleType() != ScaleType.CENTER_CROP || getScaleType() != ScaleType.FIT_XY) {
			super.setScaleType(ScaleType.CENTER_CROP);
		}
		mReady = true;

		if (mSetupPending) {
			setup();
			mSetupPending = false;
		}
	}

	@Override
	public void setScaleType(ScaleType scaleType) {
		if (scaleType != ScaleType.CENTER_CROP && scaleType != ScaleType.FIT_XY) {
			throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
		}
		super.setScaleType(scaleType);
	}

	protected Paint getBitmapPaint() {
		return mBitmapPaint;
	}
	
	protected int getBitmapWidth() {
		return mBitmapWidth;
	}

	protected int getBitmapHeight() {
		return mBitmapHeight;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap == null) {
			super.onDraw(canvas);
			return; 
		}

		Drawable drawable = getDrawable();
		if (drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
			super.onDraw(canvas);
			return; 
		}
		
		int saveCount = canvas.getSaveCount();
		canvas.save();
		canvas.translate(getPaddingLeft(), getPaddingTop());
		drawDrawable(canvas);
		canvas.restoreToCount(saveCount);
	}

	protected void drawDrawable(Canvas canvas) {
		int width = getWidth() - getPaddingLeft() - getPaddingRight();
		int height = getHeight() - getPaddingBottom() - getPaddingTop();
		
		canvas.drawCircle(width / 2, height / 2, mDrawableRadius, mBitmapPaint);
		if (mBorderWidth != 0) {
			canvas.drawCircle(width / 2, height / 2, mBorderRadius, mBorderPaint);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(isClickable()) {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
				mBitmapPaint.setColorFilter(ColorFilterTools.getVibranceColorFilter());
				invalidate();
			} else if(event.getAction() == MotionEvent.ACTION_UP
					|| event.getAction() == MotionEvent.ACTION_CANCEL) {
				mBitmapPaint.setColorFilter(null);
				invalidate();
			}
		}
		return super.onTouchEvent(event);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		setup();
	}

	public int getBorderColor() {
		return mBorderColor;
	}

	public void setBorderColor(int borderColor) {
		if (borderColor == mBorderColor) {
			return;
		}

		mBorderColor = borderColor;
		mBorderPaint.setColor(mBorderColor);
		invalidate();
	}

	public int getBorderWidth() {
		return mBorderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		if (borderWidth == mBorderWidth) {
			return;
		}

		mBorderWidth = borderWidth;
		setup();
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		mBitmap = bm;
		setup();
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		mBitmap = getBitmapFromDrawable(drawable);
		setup();
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		mBitmap = getBitmapFromDrawable(getDrawable());
		setup();
	}

	@Override
	public void setImageURI(Uri uri) {
		super.setImageURI(uri);
		mBitmap = getBitmapFromDrawable(getDrawable());
		setup();
	}

	private Bitmap getBitmapFromDrawable(Drawable drawable) {
		if (drawable == null) {
			return null;
		}

		if(drawable instanceof AnimationDrawable) {
			return null;
		}
		
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		try {
			Bitmap bitmap;

			if (drawable instanceof ColorDrawable) {
				bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
			} else {
				bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
			}

			Canvas canvas = new Canvas(bitmap);
			
			drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
			drawable.draw(canvas);
			return bitmap;
		} catch (OutOfMemoryError e) {
			return null;
		}
	}

	protected void setup() {
		if (!mReady) {
			mSetupPending = true;
			return;
		}

		if (mBitmap == null) {
			return;
		}

		mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

		mBitmapPaint.setAntiAlias(true);
		mBitmapPaint.setShader(mBitmapShader);

		mBorderPaint.setStyle(Paint.Style.STROKE);
		mBorderPaint.setAntiAlias(true);
		mBorderPaint.setColor(mBorderColor);
		mBorderPaint.setStrokeWidth(mBorderWidth);

		mBitmapHeight = mBitmap.getHeight();
		mBitmapWidth = mBitmap.getWidth();

		mBorderRect.set(0, 0, getWidth(), getHeight());
		mBorderRadius = Math.min((mBorderRect.height() - mBorderWidth) / 2, (mBorderRect.width() - mBorderWidth) / 2);

		mDrawableRect.set(mBorderWidth, mBorderWidth, mBorderRect.width() - mBorderWidth - getPaddingLeft() - getPaddingRight(), mBorderRect.height() - mBorderWidth - getPaddingTop() - getPaddingBottom());
		mDrawableRadius = Math.min(mDrawableRect.height() / 2, mDrawableRect.width() / 2);

		updateShaderMatrix();
		invalidate();
	}

	private void updateShaderMatrix() {
		float scale;
		float dx = 0;
		float dy = 0;

		mShaderMatrix.set(null);

		if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
			scale = mDrawableRect.height() / (float) mBitmapHeight;
			dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
		} else {
			scale = mDrawableRect.width() / (float) mBitmapWidth;
			dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
		}

		mShaderMatrix.setScale(scale, scale);
		mShaderMatrix.postTranslate((int) (dx + 0.5f) + mBorderWidth, (int) (dy + 0.5f) + mBorderWidth);

		mBitmapShader.setLocalMatrix(mShaderMatrix);
	}

	private static class ColorFilterTools {
		private static int minInteger(int i1, int j1) {
			return Math.min(j1, Math.max(-j1, i1));
		}

		private static float a[];

		static {
			float af[] = new float[101];
			af[0] = 0.0F;
			af[1] = 0.01F;
			af[2] = 0.02F;
			af[3] = 0.04F;
			af[4] = 0.05F;
			af[5] = 0.06F;
			af[6] = 0.07F;
			af[7] = 0.08F;
			af[8] = 0.1F;
			af[9] = 0.11F;
			af[10] = 0.12F;
			af[11] = 0.14F;
			af[12] = 0.15F;
			af[13] = 0.16F;
			af[14] = 0.17F;
			af[15] = 0.18F;
			af[16] = 0.2F;
			af[17] = 0.21F;
			af[18] = 0.22F;
			af[19] = 0.24F;
			af[20] = 0.25F;
			af[21] = 0.27F;
			af[22] = 0.28F;
			af[23] = 0.3F;
			af[24] = 0.32F;
			af[25] = 0.34F;
			af[26] = 0.36F;
			af[27] = 0.38F;
			af[28] = 0.4F;
			af[29] = 0.42F;
			af[30] = 0.44F;
			af[31] = 0.46F;
			af[32] = 0.48F;
			af[33] = 0.5F;
			af[34] = 0.53F;
			af[35] = 0.56F;
			af[36] = 0.59F;
			af[37] = 0.62F;
			af[38] = 0.65F;
			af[39] = 0.68F;
			af[40] = 0.71F;
			af[41] = 0.74F;
			af[42] = 0.77F;
			af[43] = 0.8F;
			af[44] = 0.83F;
			af[45] = 0.86F;
			af[46] = 0.89F;
			af[47] = 0.92F;
			af[48] = 0.95F;
			af[49] = 0.98F;
			af[50] = 1.0F;
			af[51] = 1.06F;
			af[52] = 1.12F;
			af[53] = 1.18F;
			af[54] = 1.24F;
			af[55] = 1.3F;
			af[56] = 1.36F;
			af[57] = 1.42F;
			af[58] = 1.48F;
			af[59] = 1.54F;
			af[60] = 1.6F;
			af[61] = 1.66F;
			af[62] = 1.72F;
			af[63] = 1.78F;
			af[64] = 1.84F;
			af[65] = 1.9F;
			af[66] = 1.96F;
			af[67] = 2.0F;
			af[68] = 2.12F;
			af[69] = 2.25F;
			af[70] = 2.37F;
			af[71] = 2.5F;
			af[72] = 2.62F;
			af[73] = 2.75F;
			af[74] = 2.87F;
			af[75] = 3F;
			af[76] = 3.2F;
			af[77] = 3.4F;
			af[78] = 3.6F;
			af[79] = 3.8F;
			af[80] = 4F;
			af[81] = 4.3F;
			af[82] = 4.7F;
			af[83] = 4.9F;
			af[84] = 5F;
			af[85] = 5.5F;
			af[86] = 6F;
			af[87] = 6.5F;
			af[88] = 6.8F;
			af[89] = 7F;
			af[90] = 7.3F;
			af[91] = 7.5F;
			af[92] = 7.8F;
			af[93] = 8F;
			af[94] = 8.4F;
			af[95] = 8.7F;
			af[96] = 9F;
			af[97] = 9.4F;
			af[98] = 9.6F;
			af[99] = 9.8F;
			af[100] = 10F;
			a = af;
		}

		public static ColorMatrixColorFilter getVibranceColorFilter() {
			ColorMatrix colormatrix = new ColorMatrix();
			a(colormatrix, -70);
			return new ColorMatrixColorFilter(colormatrix);
		}

		public static ColorMatrixColorFilter getSepiaColorFilter() {
			ColorMatrix colormatrix = new ColorMatrix();
			float af[] = new float[20];
			af[0] = 0.393F;
			af[1] = 0.769F;
			af[2] = 0.189F;
			af[3] = 0.0F;
			af[4] = 0.0F;
			af[5] = 0.349F;
			af[6] = 0.686F;
			af[7] = 0.168F;
			af[8] = 0.0F;
			af[9] = 0.0F;
			af[10] = 0.272F;
			af[11] = 0.534F;
			af[12] = 0.131F;
			af[13] = 0.0F;
			af[14] = 0.0F;
			af[15] = 0.0F;
			af[16] = 0.0F;
			af[17] = 0.0F;
			af[18] = 1.0F;
			af[19] = 0.0F;
			colormatrix.set(af);
			return new ColorMatrixColorFilter(colormatrix);
		}

		private static void a(ColorMatrix colormatrix, int i1) {
			int j1 = minInteger(i1, 100);
			if (j1 != 0) {
				float af[] = new float[20];
				af[0] = 1.0F;
				af[1] = 0.0F;
				af[2] = 0.0F;
				af[3] = 0.0F;
				af[4] = j1;
				af[5] = 0.0F;
				af[6] = 1.0F;
				af[7] = 0.0F;
				af[8] = 0.0F;
				af[9] = j1;
				af[10] = 0.0F;
				af[11] = 0.0F;
				af[12] = 1.0F;
				af[13] = 0.0F;
				af[14] = j1;
				af[15] = 0.0F;
				af[16] = 0.0F;
				af[17] = 0.0F;
				af[18] = 1.0F;
				af[19] = 0.0F;
				colormatrix.postConcat(new ColorMatrix(af));
			}
		}
	}
}