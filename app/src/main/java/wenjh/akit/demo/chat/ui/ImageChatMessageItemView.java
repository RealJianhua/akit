package wenjh.akit.demo.chat.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;

import com.wenjh.akit.R;

import wenjh.akit.demo.chat.model.Message;
import wenjh.akit.common.view.SmartImageView;
import wenjh.akit.demo.chat.model.MessageServiceWrapper;
import wenjh.akit.common.util.Image;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.config.StorageConfigs;
import wenjh.akit.demo.img.ui.ImageBrowserActivity;

import com.squareup.picasso.Picasso.LoadedFrom;

public class ImageChatMessageItemView extends AbsChatMessageItemView implements com.squareup.picasso.Callback {
	private static Set<Image> sDownloadingImages = new HashSet<Image>();
	SmartImageView mContentView = null;
	LogUtil log = new LogUtil(this);
	ImageView progressImageView = null;
	
	public ImageChatMessageItemView(Context context, boolean reveive) {
		super(context, reveive);
		setContentView(reveive ? R.layout.lisitem_chatmessage_image_receive : 
			R.layout.lisitem_chatmessage_image_send);
		mContentView = (SmartImageView) findViewById(R.id.chatmessageitem_iv_imagecontent);
		progressImageView = (ImageView) findViewById(R.id.chatmessageitem_iv_imageloading);
		mContentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onImageContentClicked();
			}
		});
	}
	
	@Override
	public void refreshViews() {
		super.refreshViews();
		checkDownloadingStatus();
		mContentView.setCallback(this);
//		log.i(""+getMessage().getImageContent());
		mContentView.setLoadImageObject(getMessage().getImageContent());
		mContentView.load();
	}
	
	private void onImageContentClicked() {
		// load all images from database. find currentmessage index in all image messages.
		Message message = getMessage();
		int chatSessionType = message.getChatSessionType();
		String communityOrUserId = chatSessionType == Message.CHATTYPE_PEOPLE 
				? message.getRemoteUserId() : message.getCommunityId();
		List<Message> allImageMessages = MessageServiceWrapper.getInstance()
			.findMessageByContentType(getMessage().getChatSessionType(), getMessage().getContentType(), communityOrUserId);
		
		int currentIndex = 0;
		
		String[] largeArray = new String[allImageMessages.size()];
		String[] smallArray = new String[allImageMessages.size()];
		for (int i = 0; i < allImageMessages.size(); i++) {
			Message imageMessage = allImageMessages.get(i);
			if(message.getMsgId().equals(imageMessage.getMsgId())) {
				currentIndex = i;
			}
			String bigUri = imageMessage.getImageContent().getBigImageUri();
			String smallUri = imageMessage.getImageContent().getSmallImageUri();
			largeArray[i] = bigUri;
			smallArray[i] = smallUri;
		}
		
		Intent intent = new Intent(getContext(), ImageBrowserActivity.class);
		intent.putExtra(ImageBrowserActivity.KEY_IMAGEARRAY_SMALL_URL, smallArray);
		intent.putExtra(ImageBrowserActivity.KEY_IMAGEARRAY_LARGE_URL, largeArray);
		intent.putExtra(ImageBrowserActivity.KEY_INDEX, currentIndex);
		getContext().startActivity(intent);
	}
	
	private void checkDownloadingStatus() {
		Image image = getMessage().getImageContent();
		if(image == null) {
			hideDownloadingProgressAnimation();
		} else {
			if(sDownloadingImages.contains(image)) {
				showDownloadingProgressAnimation();
			} else {
				if(!StorageConfigs.isImageCached(image)) {
					sDownloadingImages.add(image);
					showDownloadingProgressAnimation();
				} else {
					hideDownloadingProgressAnimation();
				}
			}
		}
	}

	private void showDownloadingProgressAnimation() {
		progressImageView.setVisibility(VISIBLE);
		((AnimationDrawable)progressImageView.getDrawable()).start();
	}
	
	private void hideDownloadingProgressAnimation() {
		((AnimationDrawable)progressImageView.getDrawable()).stop();
		progressImageView.setVisibility(GONE);
	}
	
	@Override
	public void onSuccess() {
		hideDownloadingProgressAnimation();
		if(getMessage().getImageContent() != null) {
			sDownloadingImages.remove(getMessage().getImageContent());
		}
	}

	@Override
	public void onError() {
		hideDownloadingProgressAnimation();
		if(getMessage().getImageContent() != null) {
			sDownloadingImages.remove(getMessage().getImageContent());
		}
	}
	@Override
	public void onProgress(LoadedFrom from, long progress, long total) {
	}

}
