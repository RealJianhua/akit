package wenjh.akit.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifIOException;
import wenjh.akit.R;

public class GifEmojiconDrawableSpan extends OOMCatchedImageSpan {
    private final Context mContext;
    private final int mResourceId;
    private final int mSize;
    private Drawable mDrawable;

    public GifEmojiconDrawableSpan(Context context, int resourceId, int size) {
        super(ALIGN_BASELINE);
        mContext = context;
        mResourceId = resourceId;
        mSize = size;
    }

    public GifEmojiconDrawableSpan(Context context, int resourceId, int verticalAlignment, int size) {
        super(verticalAlignment);
        mContext = context;
        mResourceId = resourceId;
        mSize = size;
    }

    public Drawable getDrawable() {
        if (mDrawable == null) {
            try {
                GifDrawable gifDrawable = new GifDrawable(mContext.getResources(), mResourceId);
                int size = mSize;
                if(size > 0) {
                    gifDrawable.setBounds(0, 0, size, size);
                } else {
                    gifDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
                }
                mDrawable = gifDrawable;
            } catch(GifIOException e) {
                mDrawable = mContext.getResources().getDrawable(mResourceId);
                Log.e("emojispan","Failed to loaded emoji resource: " + mResourceId, e);
            } catch (Exception e) {
                // swallow
            	Log.e("emojispan","Failed to loaded emoji resource: " + mResourceId, e);
            } catch (OutOfMemoryError e) {
            	Log.e("emojispan","Failed to loaded emoji resource: " + mResourceId, e);
			}
        }

        if(mDrawable == null) {
            mDrawable = mContext.getResources().getDrawable(R.drawable.zemoji_error);
        }

        return mDrawable;
    }
}