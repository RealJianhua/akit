package wenjh.akit.common.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

/**
 * 支持非焦点状态下自动滚动的 TextView
 * 
 * @author wenjianhua
 *
 */
public class ScrollingTextView extends EmoteTextView implements View.OnClickListener {
	public ScrollingTextView(Context paramContext) {
		super(paramContext);
		initScrollingTextView();
	}

	public ScrollingTextView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		initScrollingTextView();
	}

	public ScrollingTextView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		initScrollingTextView();
	}

	private void initScrollingTextView() {
		setLines(1);
		setFocusable(true);
		setFocusableInTouchMode(true);
		setHorizontallyScrolling(true);
		TextUtils.TruncateAt localTruncateAt = TextUtils.TruncateAt.MARQUEE;
		setEllipsize(localTruncateAt);

		setOnClickListener(this);
	}

	public boolean isFocused() {
		return true;
	}

	public void onClick(View paramView) {
		TextUtils.TruncateAt localTruncateAt = TextUtils.TruncateAt.MARQUEE;
		setEllipsize(localTruncateAt);
		invalidate();
	}
}