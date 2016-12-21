package wenjh.akit.common.view;

import android.content.Context;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;

/**
 * 支持 emoji 表情的文本显示控件，表情大小可通过 {@link #setCustomEmojiSize(int)} 和 {@link #setEmojiSizeMultiplier(float)} 设置。
 * 表情的默认大小就是文字的大小
 * @author wjh
 *
 */
public class EmoteTextView extends HandyTextView {
	boolean mNeedParseEmoji = true;
	private int mEmojiSize = 0;
	private boolean mEmojiSizeCustoming = false;
	private float mEmojisizeMult = 1;
	
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

	public void setText(CharSequence text, BufferType type) {
		if(mNeedParseEmoji) {
			super.setText(getEmoteDrawableSpan(text), type);
		} else {
			super.setText(text, type);
		}
	}
	
	public void setCustomEmojiSize(int emojiSize) {
		this.mEmojiSize = emojiSize;
		mEmojiSizeCustoming = true;
	}
	
	public int getEmojiSize() {
		return mEmojiSize;
	}
	
	public boolean isEmojiSizeCustoming() {
		return mEmojiSizeCustoming;
	}
	
	public void setEmojiSizeMultiplier(float emojisizeMult) {
		this.mEmojiSize = Math.round(mEmojiSize * emojisizeMult);
		this.mEmojisizeMult = emojisizeMult;
		mEmojiSizeCustoming = true;
	}
	
	public float getEmojisizeMultiplier() {
		return mEmojisizeMult;
	}
	
	/**
	 * 为了避免重复计算 emoji 相关，所以如果通过这个方法设置Text，会缓存emoji的计算结果
	 * @param emoteText
	 */
	public void setText(EmoteText emoteText) {
		if(emoteText.isEmojiInited()) {
			mNeedParseEmoji = false;
			setText(emoteText.emoteText);
			mNeedParseEmoji = true;
		} else {
			emoteText.initEmoji(getEmoteDrawableSpan(emoteText.sourceText));
			setText(emoteText);
		}
	}

	private void init() {
		mEmojiSize = (int) getTextSize();
	}
	
	@Override
	public void setTextSize(int unit, float size) {
		super.setTextSize(unit, size);
		if(!mEmojiSizeCustoming) {
			this.mEmojiSize = Math.round(mEmojiSize * mEmojisizeMult);
		}
	}
	
	protected CharSequence getEmoteDrawableSpan(CharSequence text) {
		if(text == null) {
			return "";
		}
		
		// android4.3以下不支持原生 emoji，从 4.3 开始已经原生支持了所以不需要再处理（提高性能）
		if (Build.VERSION.SDK_INT <= 17) {
			SpannableStringBuilder builder;
			if(text instanceof SpannableStringBuilder) {
				builder = (SpannableStringBuilder) text;
			} else {
				builder = new SpannableStringBuilder(text);
			}
			
			boolean changed = EmojiconHandler.addEmojis(getContext(), builder, getEmojiSize());
			if(changed) {
				text = builder;
			}
		}
		
		return text;
		
	}
	
	@Override
	public void append(CharSequence text, int start, int end) {
		super.append(replaceInputText(getEmoteDrawableSpan(text)), start, end);
	}
	
	public static class EmoteText {
		private CharSequence sourceText = "";
		private CharSequence emoteText = "";
		
		boolean inited = false;
		
		public boolean isEmojiInited() {
			return inited;
		}
		
		public void reset() {
			inited = false;
			emoteText = "";
		}
		
		public void setSourceText(CharSequence sourceText) {
			this.sourceText = sourceText;
			if(sourceText == null) {
				sourceText = "";
			}
		}
		
		public CharSequence getSourceText() {
			return sourceText;
		}
		
		public void initEmoji(CharSequence emoteText) {
			this.emoteText = emoteText;
			inited = true;
		}
		
		public CharSequence getEmoteText() {
			return emoteText;
		}

		@Override
		public String toString() {
			return "EmoteText [sourceText=" + sourceText + ", emoteText="
					+ emoteText + ", inited=" + inited + "]";
		}
	}
}
