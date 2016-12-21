package wenjh.akit.activity.base;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.wenjh.akit.R;
import wenjh.akit.common.view.ScrollViewPager;

public abstract class ScrollTabGroupActivity extends BaseActivity {
	public static final String KEY_TABINDEX = "index";
	private final ArrayList<TabInfo> tabs = new ArrayList<TabInfo>();
	private ScrollViewPager viewPager;
	private boolean pagerTouchScroll = true;
	private Map<String, MainScreenFragment> fragments = new HashMap<String, MainScreenFragment>();
	private int currentTab = -1;
	private int offscreenPageLimit = 1;
	private boolean retainInstance = true;
	private TabsAdapter adapter = null;
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		viewPager = (ScrollViewPager) findViewById(R.id.tabcontent);
		viewPager.setEnableTouchScroll(pagerTouchScroll);
		viewPager.setOffscreenPageLimit(offscreenPageLimit);

		new TabsAdapter(viewPager);
		
		if(savedInstanceState != null) {
			currentTab = savedInstanceState.getInt("tab", 0);
		}

		if (currentTab != -1) {
			viewPager.setCurrentItem(currentTab);
		} else {
			viewPager.setCurrentItem(0);
			int index = getIntent().getIntExtra(KEY_TABINDEX, 0);
			if (index < 0) {
				index = 0;
			} else if (index >= tabs.size()) {
				index = tabs.size()-1;
			}
			setCurrentTab(index);
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if(intent.hasExtra(KEY_TABINDEX)) {
			int index = intent.getIntExtra(KEY_TABINDEX, 0);
			if (index < 0) {
				index = 0;
			} else if (index >= tabs.size()) {
				index = tabs.size()-1;
			}
			setCurrentTab(index);
		}
		
		MainScreenFragment fragment = getCurrentFragment();
		if(fragment != null && fragment.isCreated()) {
			fragment.onNewIntent(intent);
		}
	}
	
	public void setRetainInstance(boolean retainInstance) {
		this.retainInstance = retainInstance;
	}
	
	protected void reflushAdapter() {
		adapter.notifyDataSetChanged();
		if (currentTab != -1) {
			viewPager.setCurrentItem(currentTab);
		} else {
			viewPager.setCurrentItem(0);
		}
	}

	public void setOffscreenPageLimit(int limit) {
		this.offscreenPageLimit = limit;
		if (viewPager != null) {
			 viewPager.setOffscreenPageLimit(limit);
		}
	}

	public MainScreenFragment getFragment(int index) {
		if(index >= 0 && index < tabs.size()) {
			TabInfo info = tabs.get(index);
			return fragments.get(info.clazz.getName());
		}
		return null;
	}

	public void setCurrentTab(int currentTab) {
		if (viewPager != null) {
			if(currentTab == this.currentTab && getCurrentFragment() != null && getCurrentFragment().isCreated()) {
				getCurrentFragment().scrollToTop();
			}
			viewPager.setCurrentItem(currentTab);
		}
		this.currentTab = currentTab;
	}

	public MainScreenFragment getCurrentFragment() {
		return getFragment(getCurrentTab());
	}

	public int getCurrentTab() {
		return viewPager.getCurrentItem();
	}
	
	public ScrollViewPager getViewPager() {
		return viewPager;
	}

	public boolean isPagerTouchScroll() {
		return pagerTouchScroll;
	}

	public void setPagerTouchScroll(boolean touchScroll) {
		this.pagerTouchScroll = touchScroll;
		if (viewPager != null) {
			viewPager.setEnableTouchScroll(pagerTouchScroll);
		}
	}
	
	protected void onFragmentCreated(MainScreenFragment fragment, int index) {
		TabInfo info = tabs.get(index);
		if(info.getIndicatorView() != null) {
			fragment.setIndicatorView(info.getIndicatorView());
		}
	}

	public void addTab(TabInfo tab) {
		tabs.add(tab);
	}

	public void addIndicatorView(int viewId, int index) {
		if(viewId > 0) {
			tabs.get(index).setIndicatorView(findViewById(viewId));
		}
	}
	
	public void addTab(Class<? extends MainScreenFragment>... classes) {
		for (Class<? extends MainScreenFragment> clazz : classes) {
			addTab(new TabInfo(clazz));
		}
	}
	
	public void addIndicatorViews(int ... viewids) {
		for (int i = 0; i < viewids.length; i++) {
			if(tabs.size() > i && viewids[i] > 0) {
				View v = findViewById(viewids[i]);
				if(v != null) {
					tabs.get(i).setIndicatorView(v);
					v.setOnClickListener(tabItemViewClickedListener);
				} else {
					tabs.get(i).setIndicatorView(null);
				}
			}
		}
	}
	
	private View.OnClickListener tabItemViewClickedListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			for (int i = 0; i < tabs.size(); i++) {
				if(tabs.get(i).getIndicatorView() == v) {
					if(currentTab == i) {
						getFragment(i).scrollToTop();
					} else {
						setCurrentTab(i);
					}
					break;
				}
			}
		}
	};
	
	public void reloadFragments() {
		new TabsAdapter(viewPager);

		if (currentTab != -1 && currentTab < tabs.size()) {
			viewPager.setCurrentItem(currentTab);
		} else {
			viewPager.setCurrentItem(0);
		}
	}
	
	public void notifyAllFragmens() {
		for (int i = 0; i < tabs.size(); i++) {
			MainScreenFragment fragment = getFragment(i);
			if(fragment != null && fragment.isCreated()) {
				fragment.notifyDataChanaged();
			}
		}
	}
	
	public void clearFragments() {
		tabs.clear();
	}

	public int getInitedItemCount() {
		return fragments.size();
	}

	public boolean isAllInited() {
		return getInitedItemCount() == tabs.size();
	}

	protected void onTabChanged(MainScreenFragment fragment, int position) {
		for (int i = 0; i < tabs.size(); i++) {
			TabInfo tabInfo = tabs.get(i);
			if(tabInfo.getIndicatorView() != null) {
				tabInfo.getIndicatorView().setSelected(position == i);
				if(position == i) {
					fragment.setIndicatorView(tabInfo.getIndicatorView());
				}
			}
		}
		
		if (!((MainScreenFragment) fragment).isFirstResumed()) {
			((MainScreenFragment) fragment).onFirstResume();
		}
	}
	
	@Override
	public void onBackPressed() {
		MainScreenFragment currentFragment = getFragment(currentTab);
		if(currentFragment != null && currentFragment.isCreated()) {
			if(currentFragment.onBackPressed()) {
				return;
			}
		}
		super.onBackPressed();
	}


	@Override
	protected void onResume() {
		super.onResume();
		MainScreenFragment currentFragment = getFragment(currentTab);
		if(currentFragment != null && currentFragment.isCreated() && !currentFragment.isForeground()) {
			currentFragment.dispatchResume();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MainScreenFragment currentFragment = getFragment(currentTab);
		if(currentFragment != null && currentFragment.isCreated()) {
			currentFragment.dispatchPause();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Fragment fragment = getFragment(currentTab);
		if (fragment != null && fragment instanceof MainScreenFragment) {
			if (((MainScreenFragment) fragment).onKeyDown(keyCode, event)) {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Fragment fragment = getFragment(currentTab);
		if (fragment != null && fragment instanceof MainScreenFragment) {
			if (((MainScreenFragment) fragment).onKeyUp(keyCode, event)) {
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("tab", viewPager.getCurrentItem());
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	static final class TabInfo {
		private final String tag;
		private final Class<? extends MainScreenFragment> clazz;
		private final Bundle args;
		private WeakReference<View> indicatorView;

		public TabInfo(String tag, Class<? extends MainScreenFragment> clazz, Bundle args) {
			this.tag = tag;
			this.clazz = clazz;
			this.args = args;
		}
		public TabInfo(String tag, Class<? extends MainScreenFragment> clazz) {
			this.tag = tag;
			this.clazz = clazz;
			this.args = null;
		}

		public TabInfo(Class<? extends MainScreenFragment> clazz) {
			this.tag = clazz.getName();
			this.clazz = clazz;
			this.args = null;
		}
		
		public View getIndicatorView() {
			return indicatorView != null ? indicatorView.get() : null;
		}
		
		public void setIndicatorView(View indicatorView) {
			if(indicatorView != null) {
				this.indicatorView = new WeakReference<View>(indicatorView);
			} else {
				this.indicatorView = null;
			}
		}
		@Override
		public String toString() {
			return "TabInfo [tag=" + tag + ", clazz=" + clazz + ", args=" + args + ", indicatorView=" + indicatorView + "]";
		}
	}

	public class TabsAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {
		private final ViewPager viewPager;

		public TabsAdapter(ViewPager pager) {
			super(thisActivity().getFragmentManager());
			viewPager = pager;
			viewPager.setOnPageChangeListener(this);
			viewPager.setAdapter(this);
			adapter = this;
		}

		public void addTab(String tag, Class<? extends MainScreenFragment> clss, Bundle args) {
			TabInfo info = new TabInfo(tag, clss, args);
			tabs.add(info);
		}

		@Override
		public int getCount() {
			return tabs.size();
		}

		@Override
		public void startUpdate(ViewGroup container) {
			super.startUpdate(container);
		}
		
		private boolean fristUpdated = true;

		@Override
		public void finishUpdate(ViewGroup container) {
			super.finishUpdate(container);

			if (fristUpdated) {
				fristUpdated = false;
				onPageSelected(viewPager.getCurrentItem());
			}
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = tabs.get(position);
			MainScreenFragment fragment = (MainScreenFragment) Fragment.instantiate(getApplicationContext(), info.clazz.getName());
			fragment.setRetainInstance(retainInstance);
			onFragmentCreated(fragment, position);
			return fragment;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Object object = super.instantiateItem(container, position);
			fragments.put(object.getClass().getName(), (MainScreenFragment) object);
			log.w("instantiateItem, position="+position);
			return object;
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			MainScreenFragment f = getFragment(currentTab);
			// call生命周期方法
			if(currentTab >= 0 && currentTab != position && f != null) {
				f.dispatchPause();
				f.onFragmentPauseByTabChanged();
			}
			
			MainScreenFragment fragment = getFragment(position);
			if(fragment != null) {
				fragment.dispatchResume();
				ScrollTabGroupActivity.this.onTabChanged(fragment, position);
				currentTab = position;
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
		}
	}

	@Override
	protected abstract void initViews();
	@Override
	protected abstract void initEvents();
	
	@Override
	protected abstract void initDatas();
}
