package wenjh.akit.demo.chat.ui;

import android.os.Bundle;

import com.wenjh.akit.R;
import wenjh.akit.activity.base.BaseActivity;
import wenjh.akit.demo.chat.model.ChatSession;

public class PeopleChatActivity extends BaseActivity {
	public final static String KEY_REMOTEUSERID = "remoteuserid";
	
	@Override
	protected void onActivityCreated(Bundle savedInstanceState) {
		setContentView(R.layout.activity_chat);
		initViews();
		initEvents();
		initDatas();
	}

	@Override
	protected void initViews() {
		BaseChatFragment chatFragment = new PeopleChatFragment();
		chatFragment.setSessionChatType(ChatSession.TYPE_PEOPLE);
		getFragmentManager()
			.beginTransaction()
			.add(R.id.tabcontent, chatFragment, chatFragment.getClass().getName())
			.commit();
	}

	@Override
	protected void initEvents() {
		
	}

	@Override
	protected void initDatas() {
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
}
