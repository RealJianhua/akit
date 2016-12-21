package wenjh.akit.demo.chat.ui;

import com.wenjh.akit.R;

import android.content.Context;
import android.widget.TextView;

public class TimestampMessageItemView extends AbsChatMessageItemView {
	
	TextView contentView = null;
	
	public TimestampMessageItemView(Context context) {
		super(context, false);
		setContentView(R.layout.lisitem_chatmessage_time);
		contentView = (TextView) findViewById(R.id.chatmessageitem_tv_textcontent);
	}
	
	@Override
	public void refreshViews() {
		super.refreshViews();
		contentView.setText(getMessage().getMessageTimestampString());
	}
	
	
}
