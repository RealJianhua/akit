package wenjh.akit.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;

import wenjh.akit.common.util.ContextUtil;
import wenjh.akit.common.util.LogUtil;

/**
 * 不支持下拉刷新
 * @author wenjianhua
 *
 */
public class RefreshOnOverScrollListView extends HandyListView implements
		OnTouchListener {
	protected float mAutoOverscrollMultipier;
	protected boolean autoRefresh;
	protected boolean waitingForRefresh;
	protected boolean mIsBeingDragged;
	protected boolean mIsBeingScrolled;
	protected boolean mIsGingerbread;
	protected boolean mIsScrollLocked;
	protected boolean mIsHideAnimation;
	protected boolean mIsZeroVisible;
	private float mLastDownY;
	protected float mLastMotionY;
	protected int mMinVelocity;
	protected int mOverScrollAmount;
	protected View mOverScrollView;
	protected OverScrollListener mOverscrollListener;
	protected boolean mPostingSelection;
	protected boolean mPreventInvalidate;
	protected boolean mPreventOverscroll;
	protected boolean mEnableOverscroll;
	private int mScrollY;
	protected Scroller mScroller;
	private float mTopY;
	private OnTouchListener mTouchListener;
	protected int mTouchSlop;
	protected VelocityTracker mVelocityTracker;
	protected boolean mWaitingForTop;
	protected boolean mWasScrollingJustUnlocked;
	
	protected LogUtil log = new LogUtil(getClass().getSimpleName());

	public RefreshOnOverScrollListView(Context paramContext, View headView) {
		super(paramContext);
		this.mTouchListener = this;
		this.mScrollY = 0;
		initOverScrollingListView();
		if(headView != null)
			addHeaderView(headView);
	}
	
	public RefreshOnOverScrollListView(Context paramContext) {
		super(paramContext);
	}
	
	public RefreshOnOverScrollListView(Context paramContext,
			AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		this.mTouchListener = this;
		this.mScrollY = 0;
		initOverScrollingListView();
	}

	public RefreshOnOverScrollListView(Context paramContext,
			AttributeSet paramAttributeSet, int style) {
		super(paramContext, paramAttributeSet, style);
		this.mTouchListener = this;
		this.mScrollY = 0;
		initOverScrollingListView();
	}

	private void initOverScrollingListView() {
		setFadingEdgeLength(0);
		ViewConfiguration viewconfiguration = ViewConfiguration
				.get(getContext());
		mTouchSlop = viewconfiguration.getScaledTouchSlop();
		mMinVelocity = viewconfiguration.getScaledMinimumFlingVelocity();
		Scroller scroller = new Scroller(getContext(), new DecelerateInterpolator());
//		Scroller scroller = new Scroller(getContext(), new AccelerateInterpolator());
//		Scroller scroller = new Scroller(getContext(), new LinearInterpolator());
		mScroller = scroller;
		mLastMotionY = 0F;
		mIsBeingScrolled = false;
		mPreventInvalidate = false;
		mIsBeingDragged = false;
		mOverScrollAmount = 0;
		mWaitingForTop = false;
		mOverScrollView = null;
		mIsScrollLocked = false;
		mWasScrollingJustUnlocked = false;
		mPreventOverscroll = false;
		mEnableOverscroll = true;
		mAutoOverscrollMultipier = 0.7F;
		
		View view = new View(getContext());
		view.setLayoutParams(new android.widget.AbsListView.LayoutParams(
				AbsListView.LayoutParams.FILL_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
		addHeaderView(view);
		super.setOnTouchListener(mTouchListener);
	}
	
	private OnTouchListener onTouchListener = null;
	@Override
	public void setOnTouchListener(OnTouchListener l) {
		this.onTouchListener = l;
	}
	
	private void updateOverScrollListener(boolean calllistener) {
		if (!mIsScrollLocked && getCustomScrollY() <= 0
				&& mOverscrollListener != null) {
			if(calllistener) {
				mOverscrollListener.onOverscroll(-getCustomScrollY(),
						mOverScrollAmount);
			}

			if(autoRefresh) {
				if (-getCustomScrollY() >= mOverScrollAmount && !mIsBeingDragged)
					mIsScrollLocked = true;
				else
					mIsScrollLocked = false;
			} else {
				if (-getCustomScrollY() <= mOverScrollAmount && !mIsBeingDragged && waitingForRefresh)
					mIsScrollLocked = true;
				else
					mIsScrollLocked = false;
			}
			
//			log.i("mIsScrollLocked="+mIsScrollLocked);
			
			if (mIsScrollLocked) {
				if (!this.mScroller.isFinished()) {
					this.mScroller.abortAnimation();
				}
//				customSmoothScrollTo(0, -mOverScrollAmount);
				//scrollTo(0, 0);
				this.mScrollY = 0;
				mOverscrollListener.onRefreshRequested();
				waitingForRefresh = false;
				autoRefresh = false;
			}
		}
	}
	
	public void computeScroll() {
		if (mIsGingerbread) {
			return;
		}

		if ((this.mScroller.computeScrollOffset()) && (!this.mWaitingForTop)) {
			this.mIsBeingScrolled = true;
			int i = this.mScroller.getCurrY();
			if(-i < mOverScrollAmount && waitingForRefresh) {
				i = -mOverScrollAmount;
			}
			setPreventInvalidate(true);
			scrollTo(0, i);
			setPreventInvalidate(false);
			postInvalidate();
		} else {
			if(mIsHideAnimation) {
				setPreventInvalidate(true);
				hideLoading();
				mScrollY = 0;
				setPreventInvalidate(false);
				setPreventOverScroll(false);
				mIsHideAnimation = false;
			} else {
				if ((!this.mIsBeingDragged) && (!this.mIsScrollLocked)
						&& (getCustomScrollY() < 0)) {
					customSmoothScrollTo(0, 0);
				} else {
					this.mIsBeingScrolled = false;
				}
			}
		}
	}

	protected void hideLoading() {
	}
	
	public void customSmoothScrollBy(int dx, int dy) {
		if (!this.mScroller.isFinished()) { 
			this.mScroller.abortAnimation();
		}
		mScroller.startScroll(getScrollX(), getCustomScrollY(), dx, dy);
		invalidate();
	}

	public void customSmoothScrollBy(int dx, int dy, int duration) {
		if (!this.mScroller.isFinished())
			this.mScroller.abortAnimation();
		
		mScroller.startScroll(getScrollX(), getCustomScrollY(), dx, dy, duration);
		invalidate();
	}

	public void customSmoothScrollTo(int dx, int dy) {
		customSmoothScrollBy(dx-getScrollX(), dy-getCustomScrollY());
	}

	public void customSmoothScrollTo(int dx, int dy, int duration) {
		customSmoothScrollBy(dx-getScrollX(), dy-getCustomScrollY(), duration);
	}

	public int getCustomScrollY() {
		return this.mScrollY;
	}

	public void invalidate() {
		if (!this.mPreventInvalidate)
			super.invalidate();
	}

	public void invalidate(int paramInt1, int paramInt2, int paramInt3,
			int paramInt4) {
		if (!this.mPreventInvalidate)
			super.invalidate(paramInt1, paramInt2, paramInt3, paramInt4);
	}

	public void invalidate(Rect paramRect) {
		if (!this.mPreventInvalidate)
			super.invalidate(paramRect);
	}

	public void invalidateDrawable(Drawable paramDrawable) {
		if (!this.mPreventInvalidate)
			super.invalidateDrawable(paramDrawable);
	}

	public boolean isBeingDragged() {
		return this.mIsBeingDragged;
	}

	public boolean isInvalidatePrevented() {
		return this.mPreventInvalidate;
	}

	public boolean isOverscrollPrevented() {
		return this.mPreventOverscroll;
	}

	public boolean isScrollingLocked() {
		return this.mIsScrollLocked;
	}

	public void lockScrolling() {
		this.mIsScrollLocked = true;
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mIsGingerbread) {
			return;
		}
		
		if (mScrollY < 0) {
			canvas.translate(0F, -mScrollY);
		}
		
		if (getFirstVisiblePosition() == 0)
			mIsZeroVisible = true;
		else
			mIsZeroVisible = false;

		if (mIsScrollLocked || !mIsZeroVisible || getCustomScrollY() > 0) {
			if (mIsScrollLocked) {
				if (!mIsZeroVisible)
					scrollTo(0, 0);
				else if (mWaitingForTop && !mScroller.isFinished()) {
					mWaitingForTop = false;
					customSmoothScrollTo(0, -mOverScrollAmount);
				}
			}
			
		} else {
			if (getCustomScrollY() == 0 && mWaitingForTop
					&& !mScroller.isFinished()) {
				mWaitingForTop = false;
				int i = (int) (mOverScrollAmount * (-mAutoOverscrollMultipier));
				customSmoothScrollTo(0, i);
			} 
			
			if (mWasScrollingJustUnlocked)
				mWasScrollingJustUnlocked = false;
			
			if (mOverScrollView != null && !mPreventOverscroll) {
//				mOverScrollView.setBackgroundResource(R.drawable.bg_loadbar);
				mOverScrollView.draw(canvas);
			}
			updateOverScrollListener(true);
		}
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (this.mOverScrollView != null) {
			mOverScrollView.layout(0, mOverScrollAmount, r, 0);
		}
		super.onLayout(changed, l, t, r, b);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (this.mOverScrollView != null) {
			int model = View.MeasureSpec.makeMeasureSpec(this.mOverScrollAmount,
					View.MeasureSpec.EXACTLY);
			mOverScrollView.measure(widthMeasureSpec, model);
		}
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public void scrollBy(int paramInt1, int paramInt2) {
		int i = getCustomScrollY() + paramInt2;
		scrollTo(0, i);
	}

	public void scrollTo(int paramInt1, int paramInt2) {
		this.mScrollY = paramInt2;
		invalidate();
	}
	
	public void setAutoOverScrollMultiplier(float autoOverscrollMultipier) {
		this.mAutoOverscrollMultipier = autoOverscrollMultipier;
	}

	public void setIsGingerbread(boolean paramBoolean) {
		this.mIsGingerbread = paramBoolean;
	}

	public void setOverScrollListener(OverScrollListener paramOverScrollListener) {
		this.mOverscrollListener = paramOverScrollListener;
	}

	public void setOverScrollView(View paramView) {
		setOverScrollView(paramView, 0);
	}

	public void setOverScrollView(View view, int dip) {
		int i = ContextUtil.dip2Pixels(dip);
		this.mOverScrollAmount = i;
		this.mOverScrollView = view;
	}

	public void setPreventInvalidate(boolean paramBoolean) {
		this.mPreventInvalidate = paramBoolean;
	}

	public void setPreventOverScroll(boolean paramBoolean) {
		mPreventOverscroll = paramBoolean;
		mWaitingForTop = false;
		mIsScrollLocked = false;
		if (getCustomScrollY() != 0)
			customSmoothScrollTo(0, 0);
	}
	
	public void setEnableOverscroll(boolean enable) {
		this.mEnableOverscroll = enable;
	}

	public void unlockScrolling() {
		if (mIsScrollLocked) {
			mIsScrollLocked = false;
			mWasScrollingJustUnlocked = true;
			if (getCustomScrollY() < 0)
				customSmoothScrollTo(0, 0);
			else
				invalidate();
		}
	}
	
	public interface OnReflushCancelListener {
		void onLoadingCancel();
	}

	public abstract interface OverScrollListener {
		public abstract void onOverscroll(int scrollY, int scrollAmount);

		public abstract void onRefreshRequested();
	}

	
	float fling = 0;
	boolean moveTag = false;
	boolean badTouchUp = false;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(onTouchListener != null) {
			onTouchListener.onTouch(v, event);
		}
		
		if (this.mPreventOverscroll || !this.mEnableOverscroll) {
			return false;
		}

		if (getFirstVisiblePosition() == 0)
			this.mIsZeroVisible = true;
		else
			this.mIsZeroVisible = false;
		
		if (this.mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}

		mVelocityTracker.addMovement(event);

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			this.mIsBeingDragged = mIsZeroVisible;
			mWaitingForTop = false;
			mIsBeingScrolled = false;
			badTouchUp = false;
			waitingForRefresh = false;

			if (!mScroller.isFinished())
				mScroller.abortAnimation();

			this.mLastMotionY = event.getY();
			this.mLastDownY = event.getY();
			mTopY = 0f;

			return false;
		case MotionEvent.ACTION_MOVE:
			float fI = mLastMotionY - event.getY();
			int i = (int) fI;
			
			mWaitingForTop = false;
			if (mIsBeingDragged || mLastMotionY != 0F) {
				mLastMotionY = event.getY();
				if (mIsBeingDragged) {
					// 开始
					if (mIsZeroVisible && getCustomScrollY() + i < 0) {
						// ---------------------
						boolean b = getCustomScrollY() + i >= -mOverScrollAmount;
						
						if (!mIsScrollLocked || b) {
							boolean flag = false;
							if (i >= 0 && mTopY == 0F) {
								flag = true;
								this.mTopY = event.getY();
							}
							i = (int) (fI / 2);
							if (mLastDownY < 0F || !mIsZeroVisible) {
								scrollBy(0, i);
							} else {
								float f1 = Math.abs(mLastDownY - event.getY());
								if (f1 <= mTouchSlop) {
									scrollBy(0, i);
								} else {
									mLastDownY = -1F;
									scrollBy(0, i);
									if(fling < -3) {
										fling = -3;
									}
									
									/**
									 * 拦截ListView对MOVE的处理，给它传一个假的事件过去。
									 * 主要针对某些三星手机的优化，例如它们会自带过度拖拽的效果...
									 */
									if(!moveTag) {
										moveTag = true;
										int position = pointToPosition((int)event.getX(), (int)event.getY());
										if(position >= 0) {
											badTouchUp = true;
											setInterceptItemClick(true);
											MotionEvent motionEvent = MotionEvent.obtain(event.getDownTime(), event.getDownTime()+100, 
														MotionEvent.ACTION_CANCEL, event.getX(), event.getY(), KeyEvent.META_SHIFT_ON);
											dispatchTouchEvent(motionEvent);
										}
									}
								}
							}
							
						} else {
							this.scrollTo(0, -mOverScrollAmount);
						}
						// -----------------------
					} else {
						mIsBeingDragged = false;
						if (!mIsScrollLocked && getCustomScrollY() < 0) {
							scrollTo(0, 0);
							setSelection(0);
						}
						fling = 0;
					}
					
					if(fling < -3) {
						fling = -3;
					}
					return true;
				} else {
					if(mIsZeroVisible && i< 0) {
						mIsBeingDragged = true;
						return true;
					} else {
						if(mIsZeroVisible && mTopY > 0) {
							return true;
						}
						
						return false;
					}
				}

			} else {
				this.mLastMotionY = event.getY();
				return false;
			}

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if(badTouchUp) {
				badTouchUp = false;
				break;
			}
			
			mTopY = 0f;
			mLastDownY = -1f;
			mLastMotionY = 0f;
			mVelocityTracker.computeCurrentVelocity(1000);
			fling = 0;
			moveTag = false;
			setInterceptItemClick(false);


			int yVelocity = (int) mVelocityTracker.getYVelocity();
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}

			if (!mIsBeingDragged) {
				if (!mScroller.isFinished())
					mScroller.abortAnimation();

				/*
				 * 已经结束触摸，但是列表还在滚动，不显示"顶部内容"（空白或下拉刷新）。不采用此功能则拿掉注释。
				 */
				/*if (yVelocity > 0) {
					mScroller.fling(getScrollX(), getCustomScrollY(), 0, -yVelocity, 0, 0, -mOverScrollAmount, 0x7fffffff);
					mWaitingForTop = true;
				}*/
				
				return false;
				
			} else {
				mIsBeingDragged = false;
				if (!mIsScrollLocked) {
					if (-getCustomScrollY() >= mOverScrollAmount) {
						waitingForRefresh = true;
					}
					customSmoothScrollTo(0, 0);
				}
			}
			
			
		default:
			break;
		}
		
		return false;
	}
}
