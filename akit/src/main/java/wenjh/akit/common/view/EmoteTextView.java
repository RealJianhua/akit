package wenjh.akit.common.view;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.LruCache;
import android.widget.TextView;

import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.util.StringUtil;

/**
 * 支持 emoji 表情的文本显示控件，表情大小可通过 {@link #setCustomEmojiSize(int)} 和 {@link #setEmojiSizeMultiplier(float)} 设置。
 * 表情的默认大小是文字的大小
 * @author wjh
 *
 */
public class EmoteTextView extends HandyTextView {
	private static LruCache<CharSequence, Spannable> sSpannableLruCache = new LruCache<CharSequence, Spannable>(200);
	static IEmoteBuilder sEmoteBuilder = new AKitEmotionBuilder();
	private int mEmojiSize = 0;
	private int mCustomEmojiSize = 0;
	private float mEmojisizeMult = 1;
	private boolean useSystemEmoji = false;
	private IEmoteBuilder mEmoteBuilder = sEmoteBuilder;
	
	public EmoteTextView(Context context) {
		super(context);
		init();
	}

	public EmoteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public EmoteTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void setText(CharSequence text, TextView.BufferType type) {
		super.setText(getEmoteDrawableSpan(text), type);
	}

	public void setCustomEmojiSize(int emojiSize) {
		this.mEmojiSize = emojiSize;
		this.mCustomEmojiSize = emojiSize;
	}

	public int getEmojiSize() {
		return mEmojiSize;
	}
	
	public int getCustomEmojiSize() {
		return mCustomEmojiSize;
	}
	
	public void setEmojiSizeMultiplier(float emojisizeMult) {
		this.mEmojiSize = Math.round(mEmojiSize * emojisizeMult);
		this.mEmojisizeMult = emojisizeMult;
	}

	public void setmEmoteBuilder(IEmoteBuilder mEmoteBuilder) {
		this.mEmoteBuilder = mEmoteBuilder;
	}

	public float getEmojisizeMultiplier() {
		return mEmojisizeMult;
	}

	private void init() {
		mEmojiSize = (int) getTextSize();
	}

	/**
	 * @see IEmoteBuilder.EmojiType
	 */
	public void addEmojiType(IEmoteBuilder.EmojiType... types) {
		mEmoteBuilder.addEmojiType(types);
	}

	/**
	 * @see IEmoteBuilder.EmojiType
	 */
	public void removeEmojiType(IEmoteBuilder.EmojiType type) {
		mEmoteBuilder.removeEmojiType(type);
	}

	@Override
	public void setTextSize(int unit, float size) {
		super.setTextSize(unit, size);
		// 如果没有指定的emoji大小，才使用文字大小
		if(mEmojiSize <= 0) {
			this.mEmojiSize = Math.round(mEmojiSize * mEmojisizeMult);
		}
	}
	
	protected CharSequence getEmoteDrawableSpan(CharSequence text) {
		if(mEmoteBuilder == null) {
			return text;
		}

		if(StringUtil.isEmpty(text)) {
			return "";
		}

		CharSequence cachedSpannable = sSpannableLruCache.get(text);
		if(cachedSpannable != null) {
			text = cachedSpannable;
		} else if(text instanceof EmojiconSpannableBuilder) {
			//continue;
			sSpannableLruCache.put(text, (Spannable) text);
		} else {
			SpannableStringBuilder builder = new EmojiconSpannableBuilder(text);
			mEmoteBuilder.addEmotionSpan(getContext(), builder, getEmojiSize());
			sSpannableLruCache.put(text, builder);
			text = builder;
		}
		return text;
	}
	
	@Override
	public void append(CharSequence text, int start, int end) {
		super.append(replaceInputText(getEmoteDrawableSpan(text)), start, end);
	}
	
	private static class EmojiconSpannableBuilder extends  SpannableStringBuilder {
		public EmojiconSpannableBuilder() {
			super();
		}

		public EmojiconSpannableBuilder(CharSequence text) {
			super(text);
		}

		public EmojiconSpannableBuilder(CharSequence text, int start, int end) {
			super(text, start, end);
		}
	}
}
