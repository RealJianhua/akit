package wenjh.akit.demo.maintab;


import android.os.Build;
import android.view.View;

import com.wenjh.akit.R;

import wenjh.akit.activity.base.MainScreenFragment;
import wenjh.akit.common.util.SystemBarTintManager;


public class MainTabActivity extends MainTabActivity2 {
    // 带透明主题

    @Override
    protected void onTabChanged(MainScreenFragment fragment, int position) {
        if (position == 2) {
            getViewPager().setPadding(0, 0, 0, 0);
            getActionBar().setBackgroundDrawable(null);
            mLogoImageView.setVisibility(View.GONE);
            mTitleView.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getStatusBarManager().setStatusBarTintEnabled(false);
            }
        } else {
            SystemBarTintManager tintManager = getStatusBarManager();
            int paddingTop = tintManager.getConfig().getActionBarHeight();
            getActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.akit_green));
            mLogoImageView.setVisibility(View.VISIBLE);
            mTitleView.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getStatusBarManager().setStatusBarTintEnabled(true);
                paddingTop += tintManager.getConfig().getStatusBarHeight();
            }
            getViewPager().setPadding(0, paddingTop, 0, 0);
        }

        super.onTabChanged(fragment, position);
    }

    @Override
    protected void initViews() {
        super.initViews();
    }
}
