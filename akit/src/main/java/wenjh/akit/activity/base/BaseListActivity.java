/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wenjh.akit.activity.base;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import wenjh.akit.R;

import wenjh.akit.common.view.LoadingButton;
import wenjh.akit.common.view.PullToRefreshListView;

public abstract class BaseListActivity extends BaseActivity {
    protected ListAdapter mAdapter;
    protected PullToRefreshListView mList;

    private Handler mHandler = new Handler();
    private boolean mFinishedStart = false;
    private LoadingButton loadMoreButton = null;
    
    private Runnable mRequestFocus = new Runnable() {
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };

    protected void setCanPullToRefresh(boolean enable) {
    	getListView().setEnablePullToRefresh(enable);
    }
    
    protected void onListItemClick(ListView l, View v, int position, long id) {
    }
    
    protected void onListItemLongClick(ListView l, View v, int position, long id) {
    }

    protected void onPullToRefresh() {}
    protected void onLoadMore() {}
    
    protected void setRefreshComplete() {
    	getListView().refreshComplete();
    }
    
    protected void setLoadMoreComplete() {
    	loadMoreButton.stopLoading();
    }
    
    protected void requestPullToRefresh() {
    	getListView().pullToReflush();
    }
    
    protected void requestLoadMore() {
    	loadMoreButton.startLoding();
    	onLoadMore();
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        ensureList();
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mRequestFocus);
        super.onDestroy();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
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
    
    /**
     * Provide the cursor for the list view.
     */
    public void setListAdapter(ListAdapter adapter) {
        synchronized (this) {
            ensureList();
            mAdapter = adapter;
            mList.setAdapter(adapter);
        }
    }

    /**
     * Set the currently selected list item to the specified
     * position with the adapter's data
     *
     * @param position
     */
    public void setSelection(int position) {
        mList.setSelection(position);
    }

    /**
     * Get the position of the currently selected list item.
     */
    public int getSelectedItemPosition() {
        return mList.getSelectedItemPosition();
    }

    /**
     * Get the cursor row ID of the currently selected list item.
     */
    public long getSelectedItemId() {
        return mList.getSelectedItemId();
    }

    /**
     * Get the activity's list view widget.
     */
    public PullToRefreshListView getListView() {
        ensureList();
        return mList;
    }

    /**
     * Get the ListAdapter associated with this activity's ListView.
     */
    public ListAdapter getListAdapter() {
        return mAdapter;
    }

    private void ensureList() {
        if (mList != null) {
            return;
        }
        setContentView(R.layout.activity_samplelist);

    }

    private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
        {
            onListItemClick((ListView)parent, v, position, id);
        }
    };
    
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
				BaseListActivity.this.onPullToRefresh();
			}
		}
	};
	
	private View.OnClickListener onLoadingButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			onLoadMore();
		}
	};
}
