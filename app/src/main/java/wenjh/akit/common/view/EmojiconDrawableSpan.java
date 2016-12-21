package wenjh.akit.common.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class EmojiconDrawableSpan extends OOMCatchedImageSpan {
    private final Context mContext;
    private final int mResourceId;
    private final int mSize;
    private Drawable mDrawable;
    
    public EmojiconDrawableSpan(Context context, int resourceId, int size) {
        super(ALIGN_BASELINE);
        mContext = context;
        mResourceId = resourceId;
        mSize = size;
    }
    
    public EmojiconDrawableSpan(Context context, int resourceId, int verticalAlignment, int size) {
        super(verticalAlignment);
        mContext = context;
        mResourceId = resourceId;
        mSize = size;
    }

    public Drawable getDrawable() {
        if (mDrawable == null) {
            try {
                mDrawable = mContext.getResources().getDrawable(mResourceId);
                int size = mSize;
                if(size > 0) {
                	mDrawable.setBounds(0, 0, size, size);
                } else {
                	mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
                }
            } catch (Exception e) {
                // swallow
            	Log.e("emojispan","Failed to loaded emoji resource: " + mResourceId, e);
            } catch (OutOfMemoryError e) {
            	Log.e("emojispan","Failed to loaded emoji resource: " + mResourceId, e);
			}
        }
        return mDrawable;
    }
}