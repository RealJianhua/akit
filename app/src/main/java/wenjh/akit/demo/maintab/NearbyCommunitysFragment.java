package wenjh.akit.demo.maintab;


import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.wenjh.akit.R;

import wenjh.akit.demo.ContextUtil;
import wenjh.akit.demo.account.model.Preference;
import wenjh.akit.activity.base.BaseListFragment;
import wenjh.akit.common.http.NetworkBaseException;
import wenjh.akit.demo.location.model.LatLng;
import wenjh.akit.demo.location.model.LocationHelper;
import wenjh.akit.common.asynctask.BaseTask;


public class NearbyCommunitysFragment extends BaseListFragment<Community> {
	private static final String NEARBY_COMMUNITY_LAST_REFRESHTIME = "nearbycommunitylastrefreshtime";
	private static final int PAGE_COUNT = 20;
	private static int mPageCounter = 0;
	private LatLng mLocation = null;
//	private CommunityService communityService = null;
	
	@Override
	protected void onCreated(Bundle savedInstanceState) {
		initViews();
		initDatas();
	}

	@Override
	protected void onPullToRefresh() {
		mPageCounter = 0;
		execAsyncTask(new GetNearByCommunityTask(getActivity(), false));
	}

	@Override
	protected void onLoadMore() {
		execAsyncTask(new GetNearByCommunityTask(getActivity(), true));
	}

	@Override
	protected void initDatas() {
//		communityService = new CommunityService();
		postAsyncRunnable(new Runnable() {
			@Override
			public void run() {
				initDataAsync();
			}
		});
	}
	
	private void initDataAsync() {
		/*

		final List<Community> communityList = communityService.getNearbyCommunities();
		final Date lastRefreshTime = Preference.getTimesPreference(getContext()).getTime(NEARBY_COMMUNITY_LAST_REFRESHTIME, null);
		log.i("lastRefreshTime="+lastRefreshTime);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setListAdapter(new CommunitiesAdapter(getActivity(), communityList));
				mList.setLastRefreshTime(lastRefreshTime);
				requestPullToRefresh();
			}
		});

		*
		*/
	}
	

	class GetNearByCommunityTask extends BaseTask<Object, Object, List<Community>> {
		boolean append;
		public GetNearByCommunityTask(Context context, boolean append) {
			super(context);
			this.append = append;
		}

		@Override
		protected List<Community> executeTask(Object... params) throws Exception {
			if(mLocation == null || mPageCounter == 0) {
				LocationHelper helper = new LocationHelper(getActivity());
				LatLng location = helper.getLocation();
				mLocation = location;
			}
			
			if(mLocation == null) {
				throw new NetworkBaseException(getString(R.string.error_locate_failed));
			}
			ContextUtil.getApp().getCurrentAccount().updateCurrentLocation(mLocation);

			List<Community> list = null;//CommunityApi.getInstance().findNearyByCommunities(mLocation, PAGE_COUNT, mPageCounter);
			
			if(mPageCounter == 0) {
				//communityService.saveNearbyCommunties(list);
			}
			
			mPageCounter++;
			return list;
		}
		
		@Override
		protected void onTaskFinish() {
			if(append) {
				setLoadMoreComplete();
			} else {
				setRefreshComplete();
			}
		}

		@Override
		protected void onTaskSuccess(List<Community> result) {
			super.onTaskSuccess(result);
			if(append) {
				// load more
				mAdapter.addAll(result);
			} else {
				// refresh
				mAdapter.replace(result);
				mList.setLastRefreshTime(new Date());
				Preference.getTimesPreference(getContext()).saveTime(NEARBY_COMMUNITY_LAST_REFRESHTIME, new Date());
			}
			setLoadMoreButtonVisibility(result.size() >= PAGE_COUNT);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
//		Intent intent = new Intent(getActivity(), CommunityProfileActivity.class);
//		intent.putExtra(CommunityProfileActivity.KEY_COMMUNITYID, mAdapter.getItem(position).getId());
//		startActivity(intent);
	}
	
	@Override
	protected void onListItemLongClick(ListView l, View v, int position, long id) {
		toast("test edit community profile");
//		Intent intent = new Intent(getActivity(), EditCommunityProfileActivity.class);
//		intent.putExtra(EditCommunityProfileActivity.KEY_COMMUNITYID, mAdapter.getItem(position).getId());
//		startActivity(intent);
	}
	
	@Override
	protected void initViews() {
	}

	@Override
	protected void initEvents() {
	}

}
