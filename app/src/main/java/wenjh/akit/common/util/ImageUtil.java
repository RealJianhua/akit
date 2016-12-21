package wenjh.akit.common.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;

import wenjh.akit.config.ImageConfigs;
import wenjh.akit.config.StorageConfigs;

/**
 * 图片处理类
 *
 * @author lijun
 *
 */
public class ImageUtil {

	public static Bitmap decodeFile(String filePath) {
		try {
			return BitmapFactory.decodeFile(filePath);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap decodeResource(int resId) {
		try {
			return BitmapFactory.decodeResource(ContextUtil.getResources(), resId);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap decodeByteArray(byte[] data, int offset, int length) {
		try {
			return BitmapFactory.decodeByteArray(data, offset, length);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 图片缩放
	 *
	 * @param bitmap
	 * @param w
	 *            宽度
	 * @param h
	 *            高度
	 * @return
	 */
	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
		if (bitmap == null)
			return null;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		return newbmp;
	}

	/**
	 * 将Drawable转换为Bitmap对象
	 *
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);

		return bitmap;
	}

	public static Drawable bitmap2Drawable(Bitmap b) {
		return new BitmapDrawable(b);
	}

	/**
	 * 为Bitmap图片添加圆角效果
	 *
	 * @param bitmap
	 *            原始图片
	 * @param roundPx
	 *            圆角大小
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		if (bitmap == null)
			return null;
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int leftTopPx, int leftBottomPx, int rightTopPx, int rightBottomPx) {
		if (bitmap == null)
			return null;

		if (leftTopPx == leftBottomPx && leftTopPx == rightTopPx && rightTopPx == rightBottomPx) {
			return getRoundedCornerBitmap(bitmap, leftTopPx);
		}

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);

		int maxCorner = Math.max(leftTopPx, leftBottomPx);
		maxCorner = Math.max(maxCorner, rightTopPx);
		maxCorner = Math.max(maxCorner, rightBottomPx);

		canvas.drawRoundRect(rectF, maxCorner, maxCorner, paint);

		// left top right bottom

		RectF leftTopRect = new RectF(0, 0, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
		RectF leftBottomRect = new RectF(0, bitmap.getHeight() / 2, bitmap.getWidth() / 2, bitmap.getHeight());
		RectF rightTopRect = new RectF(bitmap.getWidth() / 2, 0, bitmap.getWidth(), bitmap.getHeight() / 2);
		RectF rightBottomRect = new RectF(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth(), bitmap.getHeight());

		canvas.drawRoundRect(leftTopRect, leftTopPx, leftTopPx, paint);
		canvas.drawRoundRect(leftBottomRect, leftBottomPx, leftBottomPx, paint);
		canvas.drawRoundRect(rightTopRect, rightTopPx, rightTopPx, paint);
		canvas.drawRoundRect(rightBottomRect, rightBottomPx, rightBottomPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	/**
	 * 获得带倒影效果的图像
	 *
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
		final int reflectionGap = 4;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);
		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2, width, height / 2, matrix, false);
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height / 2), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);
		Paint deafalutPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0, bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
				0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));// Set the
																// Transfer mode
																// to be porter
																// duff and
																// destination
																// in
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);// Draw
																									// a
																									// rectangle
																									// using
																									// the
																									// paint
																									// with
																									// our
																									// linear
																									// gradient

		return bitmapWithReflection;
	}

	/**
	 * 将bitmap变成 字节码数组
	 *
	 * @param bitmap
	 * @return
	 */
	public static byte[] bitmap2Bytes(Bitmap bitmap) {
		if (bitmap == null)
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * 获取指定大小的图片,将原图 等比例缩放, 返回
	 * 
	 * @param context
	 * @param cr
	 * @param uri
	 * @param maxPixels
	 *            最大像素宽度/高度
	 * @return 返回位图
	 */
	public static Bitmap getScaleBitmap(Context context, ContentResolver cr, final Uri uri, int maxWidth, int maxHeight) {
		Bitmap bitmap = null;
		InputStream is = null;
		Bitmap result = null;
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			is = cr.openInputStream(uri);
			BitmapFactory.decodeStream(is, null, opts);
			int srcWidth = opts.outWidth;
			int srcHeight = opts.outHeight;
			is.close();

			if (srcWidth * srcHeight <= maxWidth * maxHeight) {
				opts.inJustDecodeBounds = false;
				opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
				is = cr.openInputStream(uri);
				result = BitmapFactory.decodeStream(is, null, opts);
				is.close();
				return result;
			}

			// 缩放比例
			int scale = 1;
			while (srcWidth / scale * srcHeight / scale > maxWidth * maxHeight) {
				scale++;
			}
			scale--;
			
			// 设置输出宽度、高度
			opts.inSampleSize = scale;
			opts.inJustDecodeBounds = false;
			opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
			opts.inPurgeable = true;
			is = cr.openInputStream(uri);
			bitmap = BitmapFactory.decodeStream(is, null, opts);

			if (bitmap == null) {
				return null;
			}
			
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			double newWidth;
			double newHeight;
			if ((double) width / maxWidth < (double) height / maxHeight) {
				newHeight = maxHeight;
				newWidth = (newHeight / height) * width;
			} else {
				newWidth = maxWidth;
				newHeight = (newWidth / width) * height;
			}

			Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, Math.round((float) newWidth), Math.round((float) newHeight), true);
			bitmap.recycle();
			result = scaledBitmap;
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(is);
		}
		return result;
	}

	public static File getSquareImageFile(File sourceFile) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(sourceFile.getPath(), options);

			// get size
			Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getPath());
			bitmap = ImageUtil.getSquaredBitmap(bitmap, 150, true);

			// save
			File file = new File(sourceFile.getParent(), sourceFile.getName() + "_");
			OutputStream os = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 85, os);
			bitmap.recycle();
			IOUtils.closeQuietly(os);

			return file;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getScaleBitmap(String path, int maxWidth, int maxHeight) {
		return loadResizedImage(new File(path), maxWidth, maxHeight);
	}

	/**
	 * 获取指定size的图片
	 * 
	 * @param srcImg
	 * @param mContext
	 * @param maxPixels
	 * @return bitmap 处理后的结果
	 */
	public static Bitmap getScaledBitmapByPixel(Bitmap srcImg, int maxPixels, boolean isRecycleSrc) {
		Bitmap result = null;
		int srcW = srcImg.getWidth();
		int srcH = srcImg.getHeight();
		int max = srcW > srcH ? srcW : srcH;
		if (max == maxPixels) {
			return srcImg;
		}
		float radius = ((float) srcW) / ((float) srcH);
		float scaledW;
		float scaledH;
		int BC_PHOTO_H = 0;
		if (srcW >= srcH) {
			BC_PHOTO_H = (int) (maxPixels / radius);
			scaledW = (float) maxPixels / srcW;
			scaledH = (float) BC_PHOTO_H / srcH;
		} else {
			BC_PHOTO_H = (int) (maxPixels * radius);
			scaledW = (float) BC_PHOTO_H / srcW;
			scaledH = (float) maxPixels / srcH;
		}
		Matrix matrix = new Matrix();
		matrix.postScale(scaledW, scaledH);
		result = Bitmap.createBitmap(srcImg, 0, 0, srcW, srcH, matrix, true); // 抗锯齿、缩放处理
		if (srcImg != null && isRecycleSrc) {
			srcImg.recycle();
			srcImg = null;
		}
		return result;
	}

	/** 截取图片为正方形 **/
	public static Bitmap getSquaredBitmap(final Bitmap bitmap, final float ICON_LANGTH, boolean isRecycle) {
		Bitmap bi = null;
		if (bitmap != null) {
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			int nw = w > h ? h : w;
			int nh = nw;
			Matrix matrix = new Matrix();
			matrix.setScale(ICON_LANGTH / nw, ICON_LANGTH / nh);
			bi = Bitmap.createBitmap(bitmap, (w - nw) / 2, (h - nh) / 2, nw, nh, matrix, true);
			// 图片不需要裁切的情况下，返回的bitmap本身
			if ((bi.hashCode() != bitmap.hashCode()) && isRecycle) {
				bitmap.recycle();
			}
		}
		return bi;
	}

	public static Bitmap getRoundBitmap(int pix, int color) {
		int size = pix;
		Bitmap bitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);
		canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
		return bitmap;
	}

	public static Bitmap getRoundRectBitmap(int widthDip, int heightDip, int round, int color) {
		int width = ContextUtil.dip2Pixels(widthDip);
		int height = ContextUtil.dip2Pixels(heightDip);
		round = ContextUtil.dip2Pixels(round);

		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);

		RectF rect = new RectF(0, 0, width, height);
		canvas.drawRoundRect(rect, round, round, paint);
		return bitmap;
	}

	public static Bitmap loadResizedImage(final File imageFile, int maxWidth, int maxHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
		
		int scale = 1;
		while (options.outWidth/scale * options.outHeight/scale  > maxWidth * maxHeight) {
			scale++;
		}
		
		Bitmap bitmap = null;
		if (scale > 1) {
			scale--;
			options = new BitmapFactory.Options();
			options.inSampleSize = scale;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			options.inPurgeable = true;
			bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
			if (bitmap == null) {
				return null;
			}
			
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			double newWidth;
			double newHeight;
			if ((double) width / maxWidth < (double) height / maxHeight) {
				newHeight = maxHeight;
				newWidth = (newHeight / height) * width;
			} else {
				newWidth = maxWidth;
				newHeight = (newWidth / width) * height;
			}

			Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, Math.round((float) newWidth), Math.round((float) newHeight), true);
			bitmap.recycle();
			bitmap = scaledBitmap;
		} else {
			bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
		}

		return rotateImage(bitmap, imageFile);
	}

	/**
	 * 旋转bitmap为正确的方向
	 * 
	 * @param bitmap
	 * @param fileWithExifInfo
	 * @return
	 */
	private static Bitmap rotateImage(final Bitmap bitmap, final File fileWithExifInfo) {
		if (bitmap == null) {
			return null;
		}
		Bitmap rotatedBitmap = bitmap;
		int orientation = 0;
		try {
			orientation = getImageOrientation(fileWithExifInfo.getAbsolutePath());
			if (orientation != 0) {
				Matrix matrix = new Matrix();
				matrix.postRotate(orientation);
				rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				bitmap.recycle();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rotatedBitmap;
	}

	/**
	 * 获得图片方向
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static int getImageOrientation(final String file) throws IOException {
		ExifInterface exif = new ExifInterface(file);
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
		switch (orientation) {
		case ExifInterface.ORIENTATION_NORMAL:
			return 0;
		case ExifInterface.ORIENTATION_ROTATE_90:
			return 90;
		case ExifInterface.ORIENTATION_ROTATE_180:
			return 180;
		case ExifInterface.ORIENTATION_ROTATE_270:
			return 270;
		default:
			return 0;
		}
	}

	public static Bitmap loadResizedBitmap(Uri uri, Context context, int maxWidth, int maxHeight) {
		if (uri.getPath().startsWith("file://") || new File(uri.getPath()).exists()) {
			Bitmap bitmap = loadResizedImage(new File(uri.getPath()), maxWidth, maxHeight);
			if (bitmap != null) {
				return bitmap;
			}
		} else {
			Bitmap bitmap = getScaleBitmap(context, context.getContentResolver(), uri, maxWidth, maxHeight);
			if (bitmap != null) {
				return bitmap;
			}
		}

		// reload smaller image
		if (maxWidth * maxHeight > 480 * 480) {
			maxWidth -= 200;
			maxHeight -= 200;
			if (maxWidth < 480) {
				maxWidth = 480;
			}
			if (maxHeight < 480) {
				maxHeight = 480;
			}
			new LogUtil("ImageUtil").i("loadResizedBitmap, load smaller image maxWidth="+maxWidth+", maxHeight="+maxHeight); 
			System.gc();
			return loadResizedBitmap(uri, context, maxWidth, maxHeight);
		}
		
		return null;
	}

	public static class ImageCornerParames {
		public int leftTopCornerRoundPx = 0;
		public int leftBottomCornerRoundPx = 0;
		public int rightTopCornerRoundPx = 0;
		public int rightBottomCornerRoundPx = 0;

		public ImageCornerParames(int leftTopCornerRoundPx, int leftBottomCornerRoundPx, int rightTopCornerRoundPx, int rightBottomCornerRoundPx) {
			this.leftTopCornerRoundPx = leftTopCornerRoundPx;
			this.leftBottomCornerRoundPx = leftBottomCornerRoundPx;
			this.rightTopCornerRoundPx = rightTopCornerRoundPx;
			this.rightBottomCornerRoundPx = rightBottomCornerRoundPx;
		}

		@Override
		public String toString() {
			return leftTopCornerRoundPx + "" + leftBottomCornerRoundPx + "" + rightTopCornerRoundPx + "" + rightBottomCornerRoundPx;
		}
	}
	
	public static void renameImageFileWithGUID(File imageFile, String guid) {
		File cacheBigFile = StorageConfigs.getImageFileWithGUID(guid, 3);
		imageFile.renameTo(cacheBigFile);
		
		File cacheMiddleFile = StorageConfigs.getImageFileWithGUID(guid, 2);
		compressedAndSaveAs(cacheBigFile, cacheMiddleFile, ImageConfigs.IMAGE_MIDDLE_WIDTH, ImageConfigs.IMAGE_MIDDLE_HEIGHT);
		
		File cacheSmallFile = StorageConfigs.getImageFileWithGUID(guid, 1);
		compressedAndSaveAs(cacheBigFile, cacheSmallFile, ImageConfigs.IMAGE_SMALL_WIDTH, ImageConfigs.IMAGE_SMALL_HEIGHT);
	}
	
	public static boolean compressedAndSaveAs(File originFile, File targetFile, int targetWidth, int targetHeight) {
		try {
			// compress origin file, and decode to bitmap
			Bitmap compressedBitmap = ImageUtil.loadResizedImage(originFile, targetWidth, targetHeight);
			if(compressedBitmap != null) {
				boolean result = storeBitmapWithJPEG(compressedBitmap, targetFile);
				compressedBitmap.recycle();
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean compressedAndSaveAs(Uri originUri, File targetFile, int targetWidth, int targetHeight) {
		try {
			// compress origin file, and decode to bitmap
			Bitmap compressedBitmap = ImageUtil.loadResizedBitmap(originUri, ContextUtil.getContext(), targetWidth, targetHeight);
			if(compressedBitmap != null) {
				boolean result = storeBitmapWithJPEG(compressedBitmap, targetFile);
				compressedBitmap.recycle();
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean storeBitmapWithJPEG(Bitmap bitmap, File file) {
		return storeBitmap(bitmap, file, Bitmap.CompressFormat.JPEG);
	}

	public static boolean storeBitmap(Bitmap bitmap, File file, Bitmap.CompressFormat format) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			bitmap.compress(format, ImageConfigs.JPEG_QUALITY, os);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
