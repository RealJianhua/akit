package wenjh.akit.demo.people.ui;

import android.os.Bundle;

import com.wenjh.akit.R;

import wenjh.akit.activity.base.TransparentActionBarActivity;

public class UserProfileActivity extends TransparentActionBarActivity {
	public final static String KEY_USERID = "userid";
	
	@Override
	protected void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setContentView(R.layout.activity_simpleframelayout);
		if(savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.framelayout,new UserProfileFragment(), UserProfileFragment.class.getName()).commit();
		}
	}
	
	@Override
	protected void initViews() {
	}
	
	@Override
	protected void initEvents() {
	}

	@Override
	protected void initDatas() {
	}

}
