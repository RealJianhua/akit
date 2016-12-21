package wenjh.akit.demo.chat.ui;

import com.wenjh.akit.R;

import android.content.Context;
import android.widget.TextView;

public class TextChatMessageItemView extends AbsChatMessageItemView {
	
	TextView contentView = null;
	
	public TextChatMessageItemView(Context context, boolean reveive) {
		super(context, reveive);
		setContentView(reveive ? R.layout.lisitem_chatmessage_text_receive : 
			R.layout.lisitem_chatmessage_text_send);
		contentView = (TextView) findViewById(R.id.chatmessageitem_tv_textcontent);
	}
	
	@Override
	public void refreshViews() {
		super.refreshViews();
		contentView.setText(getMessage().getTextContent());
	}
	
	
}
