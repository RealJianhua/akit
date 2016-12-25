package wenjh.akit.common.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import wenjh.akit.R;

import wenjh.akit.common.util.ContextUtil;
import wenjh.akit.common.util.LogUtil;

public class HandyListView extends ListView {
	public interface OnSizeChangedListener {
		public void onSizeChanged(HandyListView listView, int w, int h, int oldw, int oldh);
	}

	public final static int PADDINT_BOTTOM_DEFAULT = -3;
	private View addOfEmptyListView = null;
	private View mLoadingListView = null;
	private OnSizeChangedListener mSizeListener = null;
	private int mFadingEdgeColor = -1;
	private int mListViewHiddenValue = View.GONE;

	private boolean scrolling = false;
	private boolean scorllEndReflush = true;

	private LogUtil log = new LogUtil("HandyListView");
	private ListAdapter listAdapter = null;
	private OnScrollListener onScrollListener = null;
	private OnItemLongClickListener itemLongClickListener = null;
	private GestureDetector detector = null;
	private float lastVelocityY = 0;
	private int fastVelocity = 0;
	private int paddingBottom = 0;
	private View paddingView = null;
	private Drawable paddingBackground = null;

	public HandyListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initHandyListView();
	}

	public HandyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHandyListView();
	}

	public HandyListView(Context context) {
		super(context);
		initHandyListView();
	}

	@Override
	protected void layoutChildren() {
		try {
			super.layoutChildren();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置当List滑动结束时，是否刷新界面
	 * 
	 * @param scorllEndReflush
	 *            true表示开启特性，false关闭
	 */
	public void setScorllEndReflush(boolean scorllEndReflush) {
		this.scorllEndReflush = scorllEndReflush;
	}

	public boolean isScorllEndReflush() {
		return scorllEndReflush;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		detector.onTouchEvent(ev);
		try {
			return super.onTouchEvent(ev);
		} catch (IllegalStateException e) { // 部分手机的奇怪异常
			// The content of the adapter has changed but ListView did not
			// receive a notification. Make sure the content of your adapter is
			// not modified from a background thread, but only from the UI
			// thread. [in ListView(2131165257, class
			e.printStackTrace();
			return false;
		}
	}

	public void setListPaddingBottom(int paddingBottom) {
		if (paddingBottom == PADDINT_BOTTOM_DEFAULT) {
			this.paddingBottom = (int) getContext().getResources().getDimension(R.dimen.bottomtab_height);
		} else {
			this.paddingBottom = paddingBottom;
		}
	}

	public void setListPaddingBackground(Drawable drawable) {
		this.paddingBackground = drawable;
		if (paddingView != null && paddingBackground != null) {
			paddingView.setBackgroundDrawable(drawable);
		}
	}

	public int getListPaddingBottom() {
		return paddingBottom;
	}

	protected boolean isAddPaddingBottomView() {
		return true;
	}

	private class GestureDetectorListener extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			HandyListView.this.lastVelocityY = velocityY;
			// log.i("HandyListView.this.lastVelocityY="+HandyListView.this.lastVelocityY);
			return false;
		}
	}

	/**
	 * 是否在快速拖动
	 * 
	 * @return
	 */
	public boolean isFastScrolling() {
		return scrolling && Math.abs(lastVelocityY) >= fastVelocity;
	}

	/**
	 * 设置快速滑动的标志，默认值是5000，即滑动速度大于5000，调用{@link #isFastScrolling()}会得到true
	 * 
	 * @param fastVelocity
	 */
	public void setFastVelocity(int fastVelocity) {
		this.fastVelocity = fastVelocity;
	}

	public int getFastVelocity() {
		return fastVelocity;
	}

	/**
	 * 获得滑动速度
	 * 
	 * @return
	 */
	public float getScrollVelocity() {
		return lastVelocityY;
	}

	/**
	 * 是否正在拖动
	 * 
	 * @return
	 */
	public boolean isScrolling() {
		return scrolling;
	}

	public void setScrolling(boolean scrolling) {
		this.scrolling = scrolling;
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		this.onScrollListener = l;
	}

	@Override
	public void addFooterView(View v) {
		super.addFooterView(v);
	}

	private void initHandyListView() {
		detector = new GestureDetector(getContext(), new GestureDetectorListener());
		super.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (onScrollListener != null) {
					onScrollListener.onScrollStateChanged(view, scrollState);
				}
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:
					scrolling = false;
					lastVelocityY = 0;
					if (isScorllEndReflush() && listAdapter != null && listAdapter instanceof BaseAdapter) {
						((BaseAdapter) listAdapter).notifyDataSetChanged();
					} else {
					}

					boolean b = isOpaque();
					log.i("!!!!!------!!!!!~~~~~~~~~~~~~~~" + b);
					break;

				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					scrolling = true;
					break;

				case OnScrollListener.SCROLL_STATE_FLING:
					scrolling = true;
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (onScrollListener != null) {
					onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
				}
			}
		});
	}

	/**
	 * 滚动到列表顶部
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void scrollToTop() {
		log.i("scrollToTop");
		/*
		 * 数据为空
		 */
		if (getAdapter() == null || getAdapter().getCount() < 1) {
			return;
		}

		/*
		 * 至少从第5项位置开始滚动
		 */
		if (getFirstVisiblePosition() > 5) {
			setSelection(5);
		}

		if (ContextUtil.isIcsVsersion()) { // 4.0的API
			smoothScrollToPositionFromTop(0, 0);
		} else {
			smoothScrollToPosition(0);
		}
	}

	public void scrollToBottom() {
		post(new Runnable() {
			@Override
			public void run() {
				setSelection(listAdapter.getCount());
			}
		});
	}

	public void setListViewHiddenValue(int hiddenValue) {
		mListViewHiddenValue = hiddenValue;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (mLoadingListView != null) {
			mLoadingListView.setVisibility(View.GONE);
		}
		scrolling = false;
		super.setAdapter(adapter);
		onAdapterDataChanged(adapter == null || adapter.isEmpty());

		if (adapter != null) {
			adapter.registerDataSetObserver(new DataSetObserver() {
				@Override
				public void onChanged() {
					onAdapterDataChanged(listAdapter.isEmpty());
				}
			});
		}
		this.listAdapter = adapter;

		if (paddingBottom > 0 && isAddPaddingBottomView()) {
			log.i("addPaddingBottomView~~~~~~~~~~~~~~~~~~~~~~~~~~");
			addPaddingBottomView();
		}
	}

	public ListAdapter getListAdapter() {
		return listAdapter;
	}

	public void addPaddingBottomView() {
		if (paddingView == null) {
			paddingView = inflate(getContext(), R.layout.listitem_blank, null);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, (int) paddingBottom);
			paddingView.setLayoutParams(layoutParams);
			addFooterView(paddingView);
			if (paddingBackground != null) {
				paddingView.setBackgroundDrawable(paddingBackground);
			}
		} else {
			removeFooterView(paddingView);
			addFooterView(paddingView);
		}
	}

	protected void onAdapterDataChanged(boolean empty) {
		if (addOfEmptyListView != null) {
			if (empty && isShowEmptyView()) {
				addOfEmptyListView.setVisibility(View.VISIBLE);
			} else {
				addOfEmptyListView.setVisibility(View.GONE);
			}
		}
	}

	protected boolean isShowEmptyView() {
		return true;
	}

	public void addEmptyView(View emptyView) {
		addOfEmptyListView = emptyView;
		if (addOfEmptyListView != null) {
			addOfEmptyListView.setVisibility(View.GONE);

			LinearLayout linearLayout = new LinearLayout(getContext());
			linearLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			linearLayout.setGravity(Gravity.CENTER);
			linearLayout.addView(addOfEmptyListView);
			linearLayout.setBackgroundDrawable(emptyView.getBackground());

			addHeaderView(linearLayout);
		}
	}

	public void setEmptyViewVisible(boolean isVisible) {
		if (addOfEmptyListView != null) {
			addOfEmptyListView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
		}
	}

	public void setLoadingListView(View loadingView) {
		mLoadingListView = loadingView;
		if (mLoadingListView != null) {
			setVisibility(mListViewHiddenValue);
			mLoadingListView.setVisibility(View.VISIBLE);
		}
	}

	public void setOnSizeChangedListener(OnSizeChangedListener listener) {
		mSizeListener = listener;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mSizeListener != null)
			mSizeListener.onSizeChanged(this, w, h, oldw, oldh);
	}

	public ListAdapter getBaseAdapter() {
		ListAdapter adapter = super.getAdapter();
		if (adapter instanceof HeaderViewListAdapter)
			return ((HeaderViewListAdapter) adapter).getWrappedAdapter();
		return adapter;
	}

	public void setFadingEdgeColor(int color) {
		mFadingEdgeColor = color;
	}

	@Override
	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		this.itemLongClickListener = listener;

		super.setOnItemLongClickListener(new ItemLongclickListenerWrapper(itemLongClickListener));
	}

	public OnItemLongClickListener getOnItemLongClickListenerInWrapper() {
		return this.itemLongClickListener;
	}

	public final class ItemLongclickListenerWrapper implements OnItemLongClickListener {

		private OnItemLongClickListener listener = null;

		public ItemLongclickListenerWrapper(OnItemLongClickListener listener) {
			this.listener = listener;
		}

		public OnItemLongClickListener getWapperListener() {
			return listener;
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			/*
			 * 如果有headerView的话，position要减去hedaerView的个数
			 */

			if (listener != null) {
				int topOffset = getHeaderViewsCount();

				position -= topOffset;

				if (interceptItemClick || listAdapter == null || position < 0 || position >= listAdapter.getCount()) {
					return true;
				}

				return listener.onItemLongClick(parent, view, position, id);
			}
			return false;
		}
	}

	private boolean interceptItemClick = false;

	/**
	 * 设置拦截所有Item点击事件
	 * 
	 * @param interceptItemClick
	 */
	public void setInterceptItemClick(boolean interceptItemClick) {
		this.interceptItemClick = interceptItemClick;
	}

	/**
	 * 是否已拦截Item点击事件
	 * 
	 * @return
	 */
	public boolean isInterceptItemClick() {
		return interceptItemClick;
	}

	public boolean performItemClick(View view, int position, long id) {
		/*
		 * add了headerview之后，点击的position要减去headerView的个数.
		 */
		int topOffset = getHeaderViewsCount();

		position -= topOffset;

		if (interceptItemClick || listAdapter == null || position < 0 || position >= listAdapter.getCount()) {
			if (listAdapter != null) {
				log.i("performItemClick position=" + position + ",count=" + listAdapter.getCount());
			} else {
				log.i("performItemClick position=" + position);
			}
			return true;
		}

		return super.performItemClick(view, position, id);
	}

	@Override
	public int getSolidColor() {
		if (mFadingEdgeColor == -1)
			return super.getSolidColor();
		return mFadingEdgeColor;
	}

	public interface OnInterceptTouchListener {
		boolean onInterceptTouchEvent(MotionEvent ev);
	}

	private OnInterceptTouchListener interceptTouchListener = null;

	public void setOnInterceptTouchListener(OnInterceptTouchListener listener) {
		this.interceptTouchListener = listener;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (interceptTouchListener != null) {
			return interceptTouchListener.onInterceptTouchEvent(ev);
		}
		return super.onInterceptTouchEvent(ev);
	}

}
