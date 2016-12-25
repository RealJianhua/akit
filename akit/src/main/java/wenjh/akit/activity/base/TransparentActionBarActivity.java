package wenjh.akit.activity.base;

import android.os.Bundle;

public abstract class TransparentActionBarActivity extends BaseActivity {

	@Override
	protected void onActivityCreated(Bundle savedInstanceState) {
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayUseLogoEnabled(false);		
	}

	@Override
	protected void initSystemStatusBar() {
	}

}
