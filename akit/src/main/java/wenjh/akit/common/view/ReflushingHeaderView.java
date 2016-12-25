package wenjh.akit.common.view;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import wenjh.akit.common.util.ContextUtil;
import wenjh.akit.common.util.LogUtil;

public class ReflushingHeaderView extends LinearLayout {
	private LogUtil log = new LogUtil(this);
	private boolean doHide;
	private int forcedHeight;
	private int height;
	
	public ReflushingHeaderView(Context context) {
		super(context);
	}
	public ReflushingHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@SuppressLint("NewApi")
	public ReflushingHeaderView(Context context, AttributeSet attrs, int style) {
		super(context, attrs, style);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(doHide) {
			setMeasuredDimension(widthMeasureSpec, forcedHeight);
			setPadding(0, 0, 0, height-forcedHeight);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
	
	@Override
	public void setVisibility(int visibility) {
		if(visibility != getVisibility()) {
			if(visibility == View.GONE) {
				hide();
			} else {
				super.setVisibility(visibility);
			}
		} else {
			super.setVisibility(visibility);
		}
	}

	public void hideNoAnimation() {
		super.setVisibility(View.GONE);
	}
	
	@SuppressLint("NewApi")
	protected void hide() {
		if(ContextUtil.isIcsVsersion()) {
			doHide = true;
			height = getMeasuredHeight();
			ObjectAnimator.ofInt(this, "forcedHeight", height, 0).setDuration(500).start();
		} else {
			super.setVisibility(View.GONE);
		}
	}
	
	public int getForcedHeight() {
		return forcedHeight;
	}
	public void setForcedHeight(int height) {
		if(height == 0) {
			doHide = false;
			super.setVisibility(View.GONE);
			setPadding(0, 0, 0, 0);
		} else if (doHide && this.forcedHeight != height) {
        	this.forcedHeight = height;
            requestLayout();
        }
	}
}
