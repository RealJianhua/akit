package wenjh.akit.common.view;

import android.content.Context;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.EditText;

import wenjh.akit.common.util.StringUtil;

/**
 * 支持 emoji 表情输入的文本框，表情大小可通过 {@link #setCustomEmojiSize(int)} 和 {@link #setEmojiSizeMultiplier(float)} 设置。
 * 表情的默认大小就是文字的大小
 *
 * @author wjh
 */
public class EmoteEditeText extends EditText {
    private int mEmojiSize = 0;
    private int mCustomEmojiSize = 0;
    private float mEmojisizeMult = 1;
    private boolean useSystemEmoji = false;
    private IEmoteBuilder mEmoteBuilder = EmoteTextView.sEmoteBuilder;

    public EmoteEditeText(Context context) {
        this(context, null);
        init();
    }

    public EmoteEditeText(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.editTextStyle);
        init();
    }

    public EmoteEditeText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mEmojiSize = (int) getTextSize();

        // 使粘贴时支持将内容变成表情图后再贴进去
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

    /**
     * @see IEmoteBuilder.EmojiType
     */
    public void addEmojiType(IEmoteBuilder.EmojiType type) {
        mEmoteBuilder.addEmojiType(type);
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
        if (mEmojiSize <= 0) {
            this.mEmojiSize = Math.round(mEmojiSize * mEmojisizeMult);
        }
    }

    public void setText(CharSequence text, BufferType type) {
        if (text == null) {
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
        if(mEmoteBuilder == null) {
            return text;
        }

        if (text == null || text.length() == 0) {
            return "";
        }

        SpannableStringBuilder emojiSpanbuilder;
        if (text instanceof SpannableStringBuilder) {
            emojiSpanbuilder = (SpannableStringBuilder) text;
        } else {
            emojiSpanbuilder = new SpannableStringBuilder(text);
        }

        boolean changed = mEmoteBuilder.addEmotionSpan(getContext(), emojiSpanbuilder, mEmojiSize);

        if (changed) {
            text = emojiSpanbuilder;
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
