package wenjh.akit.demo.maintab;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.widget.SearchView;

import com.wenjh.akit.R;
import wenjh.akit.activity.base.ScrollGroupFragment;

/**
 * 'Actvty' equals 'Activity', it contains 'nearby' and 'what's new'
 * 
 * @author wjh
 *
 */
public class MainActvtyFragment extends ScrollGroupFragment {
	private SearchView mSearchView = null;

	@Override
	protected void onCreated(Bundle savedInstanceState) {
		initViews();
		initEvents();
		initDatas();
	}

	@Override
	protected int getContentViewResourceIdAndCreateView() {
		return R.layout.activity_mainactvty;
	}

	@Override
	protected void initViews() {
		addTab(NearbyCommunitysFragment.class, NewsFragment.class);
		addIndicatorViews(R.id.mainactvty_layout_tabnearby, R.id.mainactvty_layout_tabnews);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.mainactvty, menu);
		MenuItem searchMenu = menu.findItem(R.id.mainactvty_menu_search);
		this.mSearchView = (SearchView) searchMenu.getActionView();
		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
		SearchableInfo searchableInfo = searchManager.getSearchableInfo(getActivity().getComponentName());
		mSearchView.setSearchableInfo(searchableInfo);
		mSearchView.setIconifiedByDefault(true);
		searchMenu.setOnActionExpandListener(new OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				log.i("onMenuItemActionExpand");
				return true;
			}
			
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				log.i("onMenuItemActionCollapse");
				getActivity().invalidateOptionsMenu();
				return true;
			}
		});
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	        String query = intent.getStringExtra(SearchManager.QUERY);
	        if(mSearchView != null) {
	        	mSearchView.setQuery(query, false);
	        }
	    }
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (mSearchView != null) {
			menu.findItem(R.id.mainactvty_menu_create).setVisible(mSearchView.isIconified());
		}
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onBackPressed() {
		if (mSearchView != null && !mSearchView.isIconified()) {
			if(mSearchView.getQuery().length() > 0) {
				mSearchView.setQuery("", false);
			}
			mSearchView.setIconified(true);
			return true;
		}
		return super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.mainactvty_menu_create) {
			onCreateMenuSelected();
		} else if (item.getItemId() == R.id.mainactvty_menu_search) {
			getActivity().invalidateOptionsMenu();
		}
		return true;
	}

	private void onCreateMenuSelected() {
		toast("create community");
	}

	@Override
	protected void initEvents() {
	}

	@Override
	protected void initDatas() {
	}

}
