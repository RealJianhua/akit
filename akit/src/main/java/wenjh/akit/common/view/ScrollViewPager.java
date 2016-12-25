package wenjh.akit.common.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ScrollViewPager extends ViewPager {
	private boolean enableTouchScroll = true;
	
	public ScrollViewPager(Context context) {
		super(context);
	}

	public ScrollViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(enableTouchScroll) {
			return super.onTouchEvent(ev);
		}
		
		return false;
	}
	
	/**
	 * 设置是否允许滑动
	 * @param enableTouchScroll
	 */
	public void setEnableTouchScroll(boolean enableTouchScroll) {
		this.enableTouchScroll = enableTouchScroll;
	}
	
	/**
	 * 是否允许滑动
	 * @return
	 */
	public boolean isEnableTouchScroll() {
		return enableTouchScroll;
	}
	
	
	@Override
	public void setAdapter(PagerAdapter adapter) {
		super.setAdapter(adapter);
	}
	
	public void setAdapter(PagerAdapter adapter, int index) {
		setAdapter(adapter);
		setCurrentItem(index, false);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		try {
			if(enableTouchScroll) {
				return super.onInterceptTouchEvent(arg0);
			}
		} catch (Exception e) {
		}
		return false;
	}
}
