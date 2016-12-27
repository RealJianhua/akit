package wenjh.akit.activity.base;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import wenjh.akit.R;

import wenjh.akit.common.util.BaseListAdapter;
import wenjh.akit.common.view.LoadingButton;
import wenjh.akit.common.view.PullToRefreshListView;

public abstract class BaseListFragment<T> extends MainScreenFragment {
    protected BaseListAdapter<T> mAdapter;
    protected PullToRefreshListView mList;

    private Handler mHandler = new Handler();
    private boolean mFinishedStart = false;
    private LoadingButton loadMoreButton = null;

    private Runnable mRequestFocus = new Runnable() {
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };

    protected void onListItemClick(ListView l, View v, int position, long id) {
    }
    protected void onListItemLongClick(ListView l, View v, int position, long id) {
    }
    
    protected void setCanPullToRefresh(boolean enable) {
    	getListView().setEnablePullToRefresh(enable);
    }
    
    protected void onPullToRefresh() {}
    protected void onLoadMore() {}
    
    protected void setRefreshComplete() {
    	mList.refreshComplete();
    }
    
    protected void setLoadMoreComplete() {
    	loadMoreButton.stopLoading();
    }
    
    protected void requestPullToRefresh() {
    	if(mAdapter != null) {
    		mList.pullToReflush();
    	} else {
    		log.w("Not call setAdapter before");
    	}
    }
    
    protected void requestLoadMore() {
    	loadMoreButton.startLoding();
    	onLoadMore();
    }
    
    @Override
    protected void onBeforeCreated(Bundle savedInstanceState) {
    	super.onBeforeCreated(savedInstanceState);
    	onContentChanged();
    }
    
    @Override
    public void onDetach() {
    	super.onDetach();
    	mHandler.removeCallbacks(mRequestFocus);
    }

    private void onContentChanged() {
        View emptyView = findViewById(R.id.listemptyview);
        mList = (PullToRefreshListView)findViewById(R.id.listview);
        if (mList == null) {
            throw new RuntimeException(
                    "Your content must have a ListView whose id attribute is " +
                    "'android.R.id.list'");
        }
        if (emptyView != null) {
            mList.setEmptyView(emptyView);
        }
        mList.setOnItemClickListener(mOnClickListener);
        mList.setOnItemLongClickListener(mOnLongClickListener);
        mList.setOnPullToRefreshListener(onPullToRefreshListener);
        mList.setEnableLoadMoreFoolter(true);
        loadMoreButton = mList.getFooterViewButton();
        loadMoreButton.setVisibility(View.GONE);
        loadMoreButton.setOnClickListener(onLoadingButtonClickListener);
        if (mFinishedStart) {
            setListAdapter(mAdapter);
        }
        mHandler.post(mRequestFocus);
        mFinishedStart = true;
    }

    public void setLoadMoreButtonVisibility(boolean visible) {
    	loadMoreButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    
    public void setListAdapter(BaseListAdapter<T> adapter) {
        synchronized (this) {
            mAdapter = adapter;
            mList.setAdapter(adapter);
        }
    }

    public void setSelection(int position) {
        mList.setSelection(position);
    }

    public int getSelectedItemPosition() {
        return mList.getSelectedItemPosition();
    }

    public long getSelectedItemId() {
        return mList.getSelectedItemId();
    }

    public PullToRefreshListView getListView() {
        return mList;
    }

    public BaseListAdapter<T> getListAdapter() {
        return mAdapter;
    }
    
    @Override
    protected int getContentViewResourceIdAndCreateView() {
    	return R.layout.activity_samplelist;
    }
    
    public AdapterView.OnItemClickListener getOnItemClickListener() {
		return mOnClickListener;
	}
    
    public AdapterView.OnItemLongClickListener getOnItemLongClickListener() {
		return mOnLongClickListener;
	}

    private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
        {
            onListItemClick((ListView)parent, v, position, id);
        }
    };
    
    public void scrollToTop() {
    	mList.scrollToTop();
    };

    public void setAutoLoadMore(boolean autoLoadMore) {
        mList.setAutoLoadMore(autoLoadMore);
    }

    private AdapterView.OnItemLongClickListener mOnLongClickListener = new AdapterView.OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			onListItemLongClick((ListView)parent, view, position, id);
			return true;
		}
    };
    
    private PullToRefreshListView.OnPullToRefreshListener onPullToRefreshListener = new PullToRefreshListView.OnPullToRefreshListener() {
		@Override
		public void onPullToRefresh() {
			if(mAdapter != null) {
				BaseListFragment.this.onPullToRefresh();
			}
		}
	};
	
	private View.OnClickListener onLoadingButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			onLoadMore();
		}
	};

    public void notifyDataChanged() {
        mAdapter.notifyDataSetChanged();
    }
}
