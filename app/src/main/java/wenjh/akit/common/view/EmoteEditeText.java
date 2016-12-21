package wenjh.akit.common.view;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * 支持 emoji 表情输入的文本框，表情大小可通过 {@link #setCustomEmojiSize(int)} 和 {@link #setEmojiSizeMultiplier(float)} 设置。
 * 表情的默认大小就是文字的大小
 * @author wjh
 *
 */
public class EmoteEditeText extends EditText {
	private int mEmojiSize = 0;
	private boolean mEmojiSizeCustoming = false;
	private float mEmojisizeMult = 0;

	public EmoteEditeText(Context context) {
		this(context, null);
		init();
	}

	public EmoteEditeText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public EmoteEditeText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mEmojiSize = (int) getTextSize();

		// 重写 Editable 工厂，使粘贴时支持将内容变成表情图后再贴进去
		setEditableFactory(new Editable.Factory() {
			@Override
			public Editable newEditable(CharSequence source) {
				return new SpannableStringBuilder(source) {
					@Override
					public SpannableStringBuilder replace(int start, int end, CharSequence tb, int tbstart, int tbend) {
						if (tb != null && tb.length() > 0) {
							tb = processInputAndAddEmojis(tb);
						}
						return super.replace(start, end, tb, tbstart, tbend);
					}
				};
			}
		});
	}

	@Override
	public void setTextSize(int unit, float size) {
		super.setTextSize(unit, size);
		if(!mEmojiSizeCustoming) {
			this.mEmojiSize = Math.round(mEmojiSize * mEmojisizeMult);
		}
	}

	public void setCustomEmojiSize(int emojiSize) {
		this.mEmojiSize = emojiSize;
		mEmojiSizeCustoming = true;
	}

	public void setEmojiSizeMultiplier(float emojisizeMult) {
		this.mEmojiSize = Math.round(mEmojiSize * emojisizeMult);
		this.mEmojisizeMult = emojisizeMult;
		mEmojiSizeCustoming = true;
	}

	public boolean isEmojiSizeCustoming() {
		return mEmojiSizeCustoming;
	}

	public float getEmojisizeMultiplier() {
		return mEmojisizeMult;
	}

	public int getEmojiSize() {
		return mEmojiSize;
	}

	/**
	 * settext(null) = settext("")
	 */
	public void setText(CharSequence text, BufferType type) {
		if(text == null) {
			text = "";
		}
		
		super.setText(processInputAndAddEmojis(text), type);
	}
	
	private CharSequence processInputAndAddEmojis(CharSequence inputText) {
		CharSequence emojiAddedText = getEmoteDrawableSpan(inputText);
		CharSequence replacedText = replaceInputText(emojiAddedText);
		return replacedText;
	}

	protected CharSequence getEmoteDrawableSpan(CharSequence text) {
		// android4.3以下不支持原生 emoji，从 4.3 开始已经原生支持了所以不需要再处理（提高性能）
		if (Build.VERSION.SDK_INT <= 17) {
			if (text.length() == 0) {
				return text;
			}

			SpannableStringBuilder emojiSpanbuilder;
			if (text instanceof SpannableStringBuilder) {
				emojiSpanbuilder = (SpannableStringBuilder) text;
			} else {
				emojiSpanbuilder = new SpannableStringBuilder(text);
			}

			boolean changed = EmojiconHandler.addEmojis(getContext(), emojiSpanbuilder, mEmojiSize);

			if (changed) {
				text = emojiSpanbuilder;
			}
		}

		return text;
	}

	protected CharSequence replaceInputText(CharSequence text) {
		return text;
	}

	public void insert(CharSequence text, int position) {
		((Editable) getText()).insert(position, text);
	}
}
