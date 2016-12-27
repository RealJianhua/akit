package wenjh.akit.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import wenjh.akit.R;

import java.util.Date;

import wenjh.akit.common.util.ContextUtil;
import wenjh.akit.common.util.DateUtil;
import wenjh.akit.common.view.RefreshOnOverScrollListView.OverScrollListener;

public class PullToRefreshListView extends RefreshOnOverScrollListView implements OverScrollListener {
	public static final int OS_STATE_LOADING = 3;
	public static final int OS_STATE_NO_MORE_MESSAGES = 1;
	public static final int OS_STATE_PULL_DOWN_TO_LOAD = 4;
	public static final int OS_STATE_RELEASE_TO_LOAD = 2;
	private static final int HEADER_HEIGHT = 60;

	protected int mOverScrollState;
	private int loadingHeigth; // loading控件的高度
	protected boolean mIsLoaderVisible;
	private OnPullToRefreshListener mListener;
	protected LinearLayout mLoadingContainer;
	protected ImageView loadingImageView = null;
	protected TextView mRefreshTimeTextView;
	protected TextView loadingTimeTextView, lodingMessageTextView;
	protected TextView mOverScrollTextView;
	protected LinearLayout mOverScrollView;
	protected RotatingImageView mOverscrollImage;
	protected RefreshOnOverScrollListView.OverScrollListener mOverscrollListener;
	private int overScrollPadding;
	private LoadingButton loadingButton = null;
	private boolean mEnableLoadMoreFoolter = false;
	private boolean mCompletedScrollTop = true;
	private Animation loadingAnimation = null;
	private View loadMoreFooterView = null;
	private boolean mAutoLoadMore;

	public PullToRefreshListView(Context paramContext) {
		super(paramContext);
		this.mOverscrollListener = this;
		initRefreshOnOverscrollListView();
	}

	public PullToRefreshListView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		this.mOverscrollListener = this;
		initRefreshOnOverscrollListView();
	}

	public PullToRefreshListView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		this.mOverscrollListener = this;
		initRefreshOnOverscrollListView();
	}

	private void initRefreshOnOverscrollListView() {
		this.mListener = null;

		LayoutInflater inflater = ContextUtil.getLayoutInflater();
		View v = inflater.inflate(R.layout.common_list_loadmore, null);
		this.loadMoreFooterView = v;
		this.loadingButton = (LoadingButton) v.findViewById(R.id.listitem_btn_loadmore);

		loadingAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.loading);
		setupOverscroll();
		setLoadMoreButtonBackground(R.color.background_normal);
	}

	public LoadingButton getFooterViewButton() {
		return loadingButton;
	}

	@Override
	protected boolean isAddPaddingBottomView() {
		return false;
	}

	/**
	 * 获取loading控件高度（px）
	 * 
	 * @return
	 */
	public int getLoadingHeigth() {
		return loadingHeigth;
	}

	/**
	 * 设置是否显示一个“加载更多”的List底部栏目
	 * 
	 * @param enableLoadMoreFoolter
	 *            true表示显示，false反之
	 */
	public void setEnableLoadMoreFoolter(boolean enableLoadMoreFoolter) {
		this.mEnableLoadMoreFoolter = enableLoadMoreFoolter;
	}

	public void setAutoLoadMore(boolean mAutoLoadMore) {
		this.mAutoLoadMore = mAutoLoadMore;
	}

	public void removeLoadMoreFoolter() {
		removeFooterView(loadMoreFooterView);
	}

	public void setLoadMoreButtonBackground(int colorResid) {
		if (loadMoreFooterView != null) {
			loadMoreFooterView.setBackgroundColor(getResources().getColor(colorResid));
		}
	}

	public boolean isEnableLoadMoreFoolter() {
		return mEnableLoadMoreFoolter;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		removeFooterView(loadMoreFooterView);
		if (adapter != null && mEnableLoadMoreFoolter) {
			addFooterView(loadMoreFooterView);
		}

		if (getListPaddingBottom() > 0) {
			addPaddingBottomView();
		}
	}

	protected void setOverScrollState(int state) {
		if (state == mOverScrollState)
			return;

		switch (state) {
		case OS_STATE_NO_MORE_MESSAGES:
			mOverScrollTextView.setText(R.string.pull_to_refresh_pull_label);
			break;
		case OS_STATE_RELEASE_TO_LOAD:
			mOverScrollTextView.setText(R.string.pull_to_refresh_release_label);
			break;
		case OS_STATE_LOADING:
			mOverScrollTextView.setText(R.string.pull_to_refresh_refreshing_label);
			break;
		case OS_STATE_PULL_DOWN_TO_LOAD:
			mOverScrollTextView.setText(R.string.pull_to_refresh_pull_label);
			break;
		default:
			break;
		}
		this.mOverScrollState = state;
	}
	
	public void setEnablePullToRefresh(boolean enable) {
		if(enable) {
			if(mOverScrollView == null) {
				setupOverscroll();
			}
		} else {
			setOverScrollView(null);
			setOverScrollListener(null);
		}
	}

	@Override
	protected void onScrollEnd() {
		if(mAutoLoadMore && loadingButton != null && loadingButton.getVisibility() == View.VISIBLE) {
			if(getAdapter() != null && getLastVisiblePosition() >= getAdapter().getCount() - 3) {
				if(!loadingButton.isLoading()) {
					loadingButton.performClick();
				}
			}
		}
	}
	

	protected int getRefreshLayout() {
		return R.layout.common_pullrefreshlistview_normalheader;
	}

	protected int getRefreshingLayout() {
		return R.layout.common_pullrefreshlistview_loadingheader;
	}

	protected void setupOverscroll() {
		this.mOverScrollView = (LinearLayout) ContextUtil.getLayoutInflater().inflate(getRefreshLayout(), this, false);
		this.mOverScrollTextView = (TextView) mOverScrollView.findViewById(R.id.pullrefreshlist_tv_label);
		this.mOverscrollImage = (RotatingImageView) this.mOverScrollView.findViewById(R.id.pullrefreshlist_iv_image);
		this.mRefreshTimeTextView = (TextView) this.mOverScrollView.findViewById(R.id.pullrefreshlist_tv_time);
		setOverScrollView(mOverScrollView, HEADER_HEIGHT);
		setOverScrollListener(mOverscrollListener);
		setAutoOverScrollMultiplier(0.4F);

		LinearLayout layout = (LinearLayout) ContextUtil.getLayoutInflater().inflate(getRefreshingLayout(), this, false);
		mLoadingContainer = (LinearLayout) layout.findViewById(R.id.pullrefreshlist_layout_container);
		mLoadingContainer.setVisibility(GONE);
		loadingImageView = (ImageView) layout.findViewById(R.id.pullrefreshlist_iv_loading);
		loadingTimeTextView = (TextView) layout.findViewById(R.id.pullrefreshlist_tv_time);
		lodingMessageTextView = (TextView) layout.findViewById(R.id.pullrefreshlist_tv_label);
		mLoadingContainer.findViewById(R.id.pullrefreshlist_iv_cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (cancelListener != null) {
					setLoadingVisible(false);
					cancelListener.onLoadingCancel();
				}
			}
		});

		loadingHeigth = ContextUtil.dip2Pixels(HEADER_HEIGHT);
		addHeaderView(layout);
		setFadingEdgeColor(0);
		setFadingEdgeLength(0);
	}

	public void setLoadingViewText(int resid) {
		lodingMessageTextView.setText(resid);
	}

	public LinearLayout getLoadingContainer() {
		return this.mLoadingContainer;
	}

	public boolean isLoadingVisible() {
		return this.mIsLoaderVisible;
	}

	public void setLoadingVisible(boolean visibility) {
		if (mLoadingContainer != null && mIsLoaderVisible != visibility) {
			mIsLoaderVisible = visibility;
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			if (visibility) {
				mLoadingContainer.setAnimation(null);
				mLoadingContainer.setVisibility(VISIBLE);

				loadingImageView.startAnimation(loadingAnimation);

				if (isOverscrollPrevented() != visibility) {
					setPreventOverScroll(visibility);
				}
			} else {
				if (getCustomScrollY() != 0) {
					setPreventInvalidate(true);
					scrollTo(0, 0);
					setPreventInvalidate(false);
				}

				loadingImageView.clearAnimation();
				mLoadingContainer.setVisibility(GONE);
				if (isOverscrollPrevented() != false) {
					setPreventOverScroll(false);
				}

				if (mCompletedScrollTop) {
					postDelayed(new Runnable() {
						@Override
						public void run() {
							scrollToTop();
						}
					}, 500);
				}
			}
		}
	}

	public void setLastRefreshTime(Date date) {
		String time = getContext().getString(R.string.pullrefreshlist_lastTime);
		if (date != null) {
			time = time + DateUtil.betweenWithCurrentTime(date);
		}
		mRefreshTimeTextView.setText(time);
		loadingTimeTextView.setText(time);
	}

	public void setTimeViewVisibility(boolean visible) {
		if (visible) {
			mRefreshTimeTextView.setVisibility(VISIBLE);
			loadingTimeTextView.setVisibility(VISIBLE);
		} else {
			mRefreshTimeTextView.setVisibility(GONE);
			loadingTimeTextView.setVisibility(GONE);
		}
	}

	public void setOnPullToRefreshListener(OnPullToRefreshListener onPullToRefreshListener) {
		this.mListener = onPullToRefreshListener;
	}

	@Override
	public void onOverscroll(int scrollY, int scrollAmount) {
		if (overScrollPadding == 0) {
			this.overScrollPadding = ContextUtil.dip2Pixels(30);
		}

		if (scrollY > 0) {
			if (scrollY >= scrollAmount) {
				if (isBeingDragged()) {
					setOverScrollState(OS_STATE_RELEASE_TO_LOAD);
				} else {
					setOverScrollState(OS_STATE_LOADING);
				}
			} else {
				setOverScrollState(OS_STATE_PULL_DOWN_TO_LOAD);
			}
		}

		// overScrollPadding 就是被忽略的高度，在这个高度范围内，不旋转。
		if (scrollY > overScrollPadding) {
			// 求旋转的角度 (已拖拉的高度 / Header的总高度)
			float f = scrollY - overScrollPadding;
			float f1 = scrollAmount - overScrollPadding;
			int degrees = (int) (180F * Math.min(1F, f / f1));
			// int degrees = (int) (360 * (f / f1) * 0.5);

			mOverscrollImage.setDegress(degrees);
		} else {
			mOverscrollImage.setDegress(0);
		}
	}

	@Override
	public void onRefreshRequested() {
		unlockScrolling();
		if (mListener != null && !isLoadingVisible()) {
			setLoadingVisible(true);
			mListener.onPullToRefresh();
		}
	}

	public void pullToReflush() {
		postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!mIsLoaderVisible) {
					if (getFirstVisiblePosition() > 0) {
						scrollToTop();
					}
					autoRefresh = true;
					customSmoothScrollTo(0, -mOverScrollAmount, mOverScrollAmount);
				} else {
					if (cancelListener != null) {
						cancelListener.onLoadingCancel();
					}

					scrollToTop();
					if (mListener != null) {
						mListener.onPullToRefresh();
					}
				}
			}
		}, 200);
	}

	public void requestRefresh() {
		onRefreshRequested();
	}

	/**
	 * 刷新完成时调用此方法。隐藏“Loading” 控件，隐藏时将播放动画
	 */
	public void refreshComplete() {
		setLoadingVisible(false);
	}

	public void setCompleteScrollTop(boolean scrollTop) {
		this.mCompletedScrollTop = scrollTop;
	}

	/**
	 * 隐藏“Loading” 控件,不播放动画
	 */
	public void hideLoading() {
		if (mIsLoaderVisible && mLoadingContainer != null) {
			mIsLoaderVisible = false;
			if (mLoadingContainer instanceof ReflushingHeaderView) {
				((ReflushingHeaderView) mLoadingContainer).hideNoAnimation();
			} else {
				mLoadingContainer.setVisibility(GONE);
			}
			if (isOverscrollPrevented() != false)
				setPreventOverScroll(false);
		}
	}

	public void hideCancleButton() {
		mLoadingContainer.findViewById(R.id.pullrefreshlist_iv_cancel).setVisibility(GONE);
	}

	private OnReflushCancelListener cancelListener = null;

	public void setOnCancelListener(OnReflushCancelListener cancelListener) {
		this.cancelListener = cancelListener;
	}

	public OnReflushCancelListener getOnCancelListener() {
		return cancelListener;
	}

	public interface OnPullToRefreshListener {
		/**
		 * 当触发下拉刷新时调用
		 */
		void onPullToRefresh();
	}
}