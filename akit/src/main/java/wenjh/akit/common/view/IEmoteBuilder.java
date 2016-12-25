package wenjh.akit.common.view;


import android.content.Context;
import android.text.Spannable;

public interface IEmoteBuilder {
    boolean addEmotionSpan(Context context, Spannable text, int emojiSize);
    void addEmojiType(EmojiType... types);
    void removeEmojiType(EmojiType type);

    enum EmojiType {
        Emoji(1),
        QQ(2),
        GIF(4);

        final int val;

        private EmojiType(int val) {
            this.val = val;
        }

        public boolean test(int types) {
            return (types & this.val) == this.val;
        }
    }

}
