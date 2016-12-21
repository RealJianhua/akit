package wenjh.akit.common.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import wenjh.akit.config.HostConfigs;
import wenjh.akit.common.util.Image;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.util.PicassoUtil;
import wenjh.akit.common.util.StringUtil;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.RequestCreator;

import java.io.File;

public class SmartImageView extends ImageView {
	private Uri mImageLoadUri = null;
	private boolean mMemoryOnly = false;
	private com.squareup.picasso.Callback mCallback = null;
	private LogUtil log = new LogUtil("SmartImageView");
	private int placeholderRes = -1;
	private boolean fillPlace = true;
	private Drawable placeholderDrawable;
	private int errorResId;
	private Drawable errorDrawable;
//	private int resizeWidth = ImageConfigs.AVATARIMAGE_MAX_WIDTH, resizeHeight = ImageConfigs.AVATARIMAGE_MAX_HEIGHT;

	public SmartImageView(Context context) {
		super(context);
	}

	public SmartImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SmartImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setPlaceholder(int placeholderDrawable) {
		this.placeholderRes = placeholderDrawable;
	}
	
	public void setPlaceholder(Drawable placeholderDrawable) {
		this.placeholderDrawable = placeholderDrawable;
	}
	
	public void setErrorDrawable(int drawableResId) {
		this.errorResId = drawableResId;
	}
	
	public void setErrorDrawable(Drawable drawable) {
		this.errorDrawable = drawable;
	}
	
	public void load() {
		RequestCreator requestCreator = PicassoUtil.picasso().load(mImageLoadUri);
		if(errorDrawable != null) {
			requestCreator.error(errorDrawable);
		} else if(errorResId > 0) {
			requestCreator.error(errorResId);
		}
		if(placeholderDrawable != null) {
			requestCreator.placeholder(placeholderDrawable);
		} else if(placeholderRes > 0) {
			requestCreator.placeholder(placeholderRes);
		}

		if(mMemoryOnly) {
			log.i("memory only");
			requestCreator.memoryPolicy(MemoryPolicy.MEMORY_ONLY);
		}

//		if(resizeWidth > 0 && resizeHeight > 0) {
//			requestCreator.resize(resizeWidth, resizeHeight);
//		}

		if(!fillPlace) {
			requestCreator.noPlaceholder();
		}
		requestCreator.into(this, mCallback);
	}

//	public void setResize(int targetWidth, int targetHeight) {
//		this.resizeWidth = targetWidth;
//		this.resizeHeight = targetHeight;
//	}
	
	public void clear() {
		mImageLoadUri = null;
		mMemoryOnly = false;
		setImageDrawable(null);
	}
	
	public void load(Image image) {
		if(image == null) {
			clear();
		} else {
			load(image.getImageUri());
		}
	}
	
	/**
	 * call before load() methond
	 * @param callback
	 */
	public void setCallback(com.squareup.picasso.Callback callback) {
		this.mCallback = callback;
	}

	public void load(String uriString) {
		if(StringUtil.isEmpty(uriString)) {
			clear();
		} else {
			load(Uri.parse(uriString));
		}
	}
	
	public void load(File loadFile) {
		if(loadFile == null) {
			clear();
		} else {
			load(Uri.fromFile(loadFile));
		}
	}

	public void load(Uri uri) {
		this.mImageLoadUri = uri;
		load();
	}
	
	/**
	 * defalut size is Middle
	 * @param imageGuid
	 */
	public void loadImageGuid(String imageGuid) {
		setImageGuid(imageGuid);
		load();
	}
	
	public void setImageGuid(String imageGuid) {
		if(StringUtil.isEmpty(imageGuid)) {
			this.mImageLoadUri = null;
		} else {
			String imageUrl = HostConfigs.getImageUrlWithGUID(imageGuid);
			setLoadImageUri(imageUrl);
		}
	}
	
	public void setMemoryOnly(boolean memoryOnly) {
		this.mMemoryOnly = memoryOnly;
	}
	
	public void setFillPlace(boolean fillPlace) {
		this.fillPlace = fillPlace;
	}
	
	public void setLoadImageUri(Uri uri) {
		this.mImageLoadUri = uri;
	}
	
	public void setLoadImageObject(Image image) {
		if(image != null) {
			this.mImageLoadUri = image.getImageUri();
		} else {
			this.mImageLoadUri = null;
		}
	}
	
	public void setLoadImageUri(String uriString) {
		this.mImageLoadUri = Uri.parse(uriString);
	}
}
