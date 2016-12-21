package wenjh.akit.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class HandyTextView extends TextView{

	public HandyTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public HandyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public HandyTextView(Context context) {
		super(context);
	}
	
	/**
	 * 在某些厂商重写的textview代码里，有致命的bug（通过跟踪堆栈发现是处理emoji引起的——那么很可能他们没考虑到中文字库）。
	 * 会导致在处理某些字符时，将抛出 StringIndexOutOfBoundsException。因此重写此方法。
	 * 
	 * 建议使用。
	 */
	@Override
	public void setText(CharSequence text, BufferType type) {
		try {
			if(text == null) {
				text = "";
			}
			
			super.setText(replaceInputText(text), type);
		} catch (StringIndexOutOfBoundsException e) {
			Log.e("HandyTextView", "text error", e);
		}
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		try {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		} catch (Exception e) { // 有某些手机，text异常
			setText("");
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
	
	/**
	 * 在此替换某些不允许显示的文字
	 * @param text
	 * @return
	 */
	protected CharSequence replaceInputText(CharSequence text) {
		return text;
	}

}
