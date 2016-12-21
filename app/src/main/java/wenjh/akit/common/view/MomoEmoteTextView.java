package wenjh.akit.common.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 支持陌陌表情
 * @author wenjianhua
 *
 */
public class MomoEmoteTextView extends EmoteTextView {
	private int mMomoEmojiSize = 0;
	
	public MomoEmoteTextView(Context context) {
		super(context);
		init();
	}
	public MomoEmoteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public MomoEmoteTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		// 默认情况下，水滴表情比emoji大1.5倍。
		mMomoEmojiSize = (int) (getEmojiSize() * 1.5f);
	}
	
	@Override
	public void setEmojiSizeMultiplier(float emojisizeMult) {
		super.setEmojiSizeMultiplier(emojisizeMult);
		mMomoEmojiSize = getEmojiSize();
	}
	
	@Override
	public void setCustomEmojiSize(int emojiSize) {
		super.setCustomEmojiSize(emojiSize);
		mMomoEmojiSize = getEmojiSize();
	}

	@Override
	public CharSequence getEmoteDrawableSpan(CharSequence text) {
		return MomoEmotionUtil.getEmoteSpan(super.getEmoteDrawableSpan(text), mMomoEmojiSize);
	}
}
