package wenjh.akit.demo.chat.ui;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import wenjh.akit.demo.chat.model.Message;
import wenjh.akit.common.util.BaseListAdapter;
import wenjh.akit.common.util.LogUtil;

public class BaseChatMessageAdapter extends BaseListAdapter<Message> {
	private static int viewTypeCount = 0;
	private final static int VIEWTYPE_SEND_TEXT = viewTypeCount++;
	private final static int VIEWTYPE_SEND_IMAGE = viewTypeCount++;
	private final static int VIEWTYPE_SEND_MAP = viewTypeCount++;
	private final static int VIEWTYPE_REVEIVE_TEXT = viewTypeCount++;
	private final static int VIEWTYPE_REVEIVE_IMAGE = viewTypeCount++;
	private final static int VIEWTYPE_REVEIVE_MAP = viewTypeCount++;
	private final static int VIEWTYPE_TIME = viewTypeCount++;
	private final static int VIEWTYPE_NOTICE = viewTypeCount++;
	private LogUtil log = new LogUtil(this);
	
	public BaseChatMessageAdapter(Context context, List<Message> list) {
		super(context, list);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Message message = getItem(position);
		int itemType = getItemViewType(position);
		
		AbsChatMessageItemView chatMessageItemView = null;
		if(itemType == VIEWTYPE_SEND_TEXT || itemType == VIEWTYPE_REVEIVE_TEXT) {
			if(convertView == null) {
				chatMessageItemView = new TextChatMessageItemView(getContext(), message.isReceived());
				convertView = chatMessageItemView;
			} else {
				chatMessageItemView = (TextChatMessageItemView) convertView;
			}
		} else if(itemType == VIEWTYPE_SEND_IMAGE || itemType == VIEWTYPE_REVEIVE_IMAGE) {
			if(convertView == null) {
				chatMessageItemView = new ImageChatMessageItemView(getContext(), message.isReceived());
				convertView = chatMessageItemView;
			} else {
				chatMessageItemView = (ImageChatMessageItemView) convertView;
			}
		} else if(itemType == VIEWTYPE_SEND_MAP || itemType == VIEWTYPE_REVEIVE_MAP) {
			if(convertView == null) {
				chatMessageItemView = new MapChatMessageItemView(getContext(), message.isReceived());
				convertView = chatMessageItemView;
			} else {
				chatMessageItemView = (MapChatMessageItemView) convertView;
			}
		} else if(itemType == VIEWTYPE_TIME) {
			if(convertView == null) {
				chatMessageItemView = new TimestampMessageItemView(getContext());
				convertView = chatMessageItemView;
			} else {
				chatMessageItemView = (TimestampMessageItemView) convertView;
			}
		} else {
			if(convertView == null) {
				chatMessageItemView = new NoticeMessageItemView(getContext());
				convertView = chatMessageItemView;
			} else {
				chatMessageItemView = (NoticeMessageItemView) convertView;
			}
		}
		
		chatMessageItemView.setMessage(message);
		chatMessageItemView.setAllAdapterMessages(getItems());
		
		return convertView;
	}

	@Override
	public int getItemViewType(int position) {
		Message message = getItem(position);
		switch (message.getContentType()) {
		case Message.CONTENTTYPE_MESSAGE_IMAGE:
			return message.isReceived() ? VIEWTYPE_REVEIVE_IMAGE : VIEWTYPE_SEND_IMAGE;
		case Message.CONTENTTYPE_MESSAGE_TEXT:
			return message.isReceived() ? VIEWTYPE_REVEIVE_TEXT : VIEWTYPE_SEND_TEXT;
		case Message.CONTENTTYPE_MESSAGE_MAP:
			return message.isReceived() ? VIEWTYPE_REVEIVE_MAP : VIEWTYPE_SEND_MAP;
		case BaseChatFragment.TimestampMessage.CONTENTTYPE_MESSAGETIME:
			return VIEWTYPE_TIME;
		case Message.CONTENTTYPE_MESSAGE_NOTICE:
			return VIEWTYPE_NOTICE;
		default:
			return -1;
		}
	}
	
	@Override
	public int getViewTypeCount() {
		return viewTypeCount;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}
	
	@Override
	public boolean isEnabled(int position) {
		int viewType = getItemViewType(position);
		return viewType != VIEWTYPE_NOTICE && viewType != VIEWTYPE_TIME;
	}
}
