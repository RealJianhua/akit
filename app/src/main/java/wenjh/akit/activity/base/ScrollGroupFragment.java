package wenjh.akit.activity.base;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.wenjh.akit.R;

import wenjh.akit.common.view.ScrollViewPager;

public abstract class ScrollGroupFragment extends MainScreenFragment {
	private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
	private ScrollViewPager mViewPager;
	private boolean mPagerTouchScroll = true;
	private Map<Integer, MainScreenFragment> mFragments = new HashMap<Integer, MainScreenFragment>();
	private int mCurrentTab = -1;
	private int mOffscreenPageLimit = 1;

	@Override
	protected void onPostCreated(Bundle savedInstanceState) {
		super.onPostCreated(savedInstanceState);
		mViewPager = (ScrollViewPager) findViewById(R.id.pagertabcontent);
		mViewPager.setEnableTouchScroll(mPagerTouchScroll);
		mViewPager.setOffscreenPageLimit(mOffscreenPageLimit);
		new TabsAdapter(this, mViewPager, mTabs);
		
		if(savedInstanceState != null) {
			mCurrentTab = savedInstanceState.getInt("tab", 0);
		}
		
		if (mCurrentTab != -1) {
			mViewPager.setCurrentItem(mCurrentTab);
		} else {
			mViewPager.setCurrentItem(0);
		}
	}
	
	public void setOffscreenPageLimit(int limit) {
		this.mOffscreenPageLimit = limit;
		if (mViewPager != null) {
			 mViewPager.setOffscreenPageLimit(limit);
		}
	}

	public MainScreenFragment getFragment(int index) {
		return mFragments.get(index);
	}

	public void setCurrentTab(int currentTab) {
		if (mViewPager != null) {
			mViewPager.setCurrentItem(currentTab);
		}
		this.mCurrentTab = currentTab;
	}

	public MainScreenFragment getCurrentFragment() {
		return mFragments.get(getCurrentTab());
	}

	public int getCurrentTab() {
		return mViewPager.getCurrentItem();
	}

	public boolean isPagerTouchScroll() {
		return mPagerTouchScroll;
	}

	public void setPagerTouchScroll(boolean touchScroll) {
		this.mPagerTouchScroll = touchScroll;
		if (mViewPager != null) {
			mViewPager.setEnableTouchScroll(mPagerTouchScroll);
		}
	}
	
	public void addTab(TabInfo tab) {
		mTabs.add(tab);
	}
	
	public void addIndicatorView(int viewId, int index) {
		if(viewId > 0) {
			mTabs.get(index).setIndicatorView(findViewById(viewId));
		}
	}

	public void addTab(Class<? extends MainScreenFragment>... classes) {
		for (Class<? extends MainScreenFragment> clazz : classes) {
			addTab(new TabInfo(clazz));
		}
	}
	
	@Override
	public void scrollToTop() {
		super.scrollToTop();
		mFragments.get(mCurrentTab).scrollToTop();
	}
	
	public void addIndicatorViews(int ... viewids) {
		for (int i = 0; i < viewids.length; i++) {
			if(mTabs.size() > i && viewids[i] > 0) {
				View v = findViewById(viewids[i]);
				if(v != null) {
					mTabs.get(i).setIndicatorView(v);
					v.setOnClickListener(tabItemViewClickedListener);
				} else {
					mTabs.get(i).setIndicatorView(null);
				}
			}
		}
	}

	private View.OnClickListener tabItemViewClickedListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			for (int i = 0; i < mTabs.size(); i++) {
				if(mTabs.get(i).getIndicatorView() == v) {
					setCurrentTab(i);
					break;
				}
			}
		}
	};
	
	public void reloadFragments() {
		new TabsAdapter(this, mViewPager, mTabs);

		if (mCurrentTab != -1) {
			mViewPager.setCurrentItem(mCurrentTab);
		} else {
			mViewPager.setCurrentItem(0);
		}
	}
	
	public int getInitedItemCount() {
		return mFragments.size();
	}

	public boolean isAllInited() {
		return getInitedItemCount() == mTabs.size();
	}

	protected void onTabChanged(MainScreenFragment fragment, int position) {
		for (int i = 0; i < mTabs.size(); i++) {
			TabInfo tabInfo = mTabs.get(i);
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
	public void dispatchActivityHomePause() {
		super.dispatchActivityHomePause();
		for (MainScreenFragment fragment : mFragments.values()) {
			if(fragment != null) {
				fragment.dispatchActivityHomePause();
			}
		}
	}
	
	@Override
	public void dispatchActivityHomeResume() {
		super.dispatchActivityHomeResume();
		for (MainScreenFragment fragment : mFragments.values()) {
			if(fragment != null) {
				fragment.dispatchActivityHomeResume();
			}
		}
		setCurrentTab(0);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Fragment fragment = mFragments.get(mCurrentTab);
		if (fragment != null && fragment instanceof MainScreenFragment) {
			if (((MainScreenFragment) fragment).onKeyDown(keyCode, event)) {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Fragment fragment = mFragments.get(mCurrentTab);
		if (fragment != null && fragment instanceof MainScreenFragment) {
			if (((MainScreenFragment) fragment).onKeyUp(keyCode, event)) {
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if(mViewPager != null) {
			outState.putInt("tab", getCurrentTab());
		}
		super.onSaveInstanceState(outState);
	}
	
	protected void onFragmentCreated(MainScreenFragment fragment, int index) {
		TabInfo info = mTabs.get(index);
		if(info.getIndicatorView() != null) {
			fragment.setIndicatorView(info.getIndicatorView());
		}
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
	}

	public class TabsAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {
		private final Context context;
		private final ViewPager viewPager;
		private ArrayList<TabInfo> tabs = null;

		public TabsAdapter(Fragment fragment, ViewPager pager, ArrayList<TabInfo> tabInfos) {
			super(fragment.getFragmentManager()); //TOOD fragment.getchildfragmentmanager
			this.tabs = new ArrayList<TabInfo>();

			context = fragment.getActivity();
			viewPager = pager;

			if (tabInfos != null) {
				for (TabInfo tabInfo : tabInfos) {
					tabs.add(tabInfo);
				}
			}

			viewPager.setOnPageChangeListener(this);
			viewPager.setAdapter(this);
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
			MainScreenFragment fragment = (MainScreenFragment) instantiate(context, info.clazz.getName());
			fragment.setIndicatorView(getIndicatorView());
			onFragmentCreated(fragment, position);
			return fragment;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Object object = super.instantiateItem(container, position);
			mFragments.put(position, (MainScreenFragment) object);
			return object;
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			// call生命周期方法
			if(mCurrentTab >= 0 && mCurrentTab != position && mFragments.get(mCurrentTab) != null) {
				mFragments.get(mCurrentTab).dispatchPause();
			}
			MainScreenFragment fragment = mFragments.get(position);
			if(fragment != null) {
				if(isForeground()) {
					fragment.dispatchResume();
				}
				ScrollGroupFragment.this.onTabChanged(fragment, position);
				mCurrentTab = position;
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
		}
	}
}
