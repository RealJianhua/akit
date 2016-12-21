package wenjh.akit.demo.maintab;

import java.util.Date;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import wenjh.akit.demo.account.model.Preference;
import wenjh.akit.activity.base.BaseListFragment;

public class NewsFragment extends BaseListFragment<Community> {
	private static final String NEARBY_ACTVTY_LAST_REFRESHTIME = "nearbycommunitylastrefreshtime";
	
	@Override
	protected void onCreated(Bundle savedInstanceState) {
		initViews();
	}

	@Override
	protected void onPullToRefresh() {
	}

	@Override
	protected void onLoadMore() {
	}

	@Override
	protected void initDatas() {
		postAsyncRunnable(new Runnable() {
			@Override
			public void run() {
				initDataAsync();
			}
		});
	}
	
	@Override
	public void onFirstResume() {
		super.onFirstResume();
		initDatas();
	}
	
	private void initDataAsync() {
		final Date lastRefreshTime = Preference.getTimesPreference(getContext()).getTime(NEARBY_ACTVTY_LAST_REFRESHTIME, null);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mList.setLastRefreshTime(lastRefreshTime);
				requestPullToRefresh();
			}
		});
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
	}
	
	@Override
	protected void initViews() {
	}

	@Override
	protected void initEvents() {
		
	}

}
