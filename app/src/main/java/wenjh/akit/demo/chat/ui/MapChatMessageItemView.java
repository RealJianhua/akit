package wenjh.akit.demo.chat.ui;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;

import com.wenjh.akit.R;

import wenjh.akit.demo.config.StorageConfigs;
import wenjh.akit.demo.location.model.LatLng;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.view.SmartImageView;

import com.squareup.picasso.Picasso.LoadedFrom;

public class MapChatMessageItemView extends AbsChatMessageItemView implements com.squareup.picasso.Callback {
	private static Set<String> sDownloadingImages = new HashSet<String>();
	SmartImageView mContentView = null;
	LogUtil log = new LogUtil(this);
	ImageView progressImageView = null;
	
	public MapChatMessageItemView(Context context, boolean reveive) {
		super(context, reveive);
		setContentView(reveive ? R.layout.lisitem_chatmessage_map_receive : 
			R.layout.lisitem_chatmessage_map_send);
		mContentView = (SmartImageView) findViewById(R.id.chatmessageitem_iv_imagecontent);
		progressImageView = (ImageView) findViewById(R.id.chatmessageitem_iv_imageloading);
		mContentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LatLng location = getMessage().getMapMessageLocation();
				if(location != null) {
					// open map, TODO
				}
			}
		});
	}
	
	@Override
	public void refreshViews() {
		super.refreshViews();
		checkDownloadingStatus();
		LatLng latLng = getMessage().getMapMessageLocation();
		mContentView.setCallback(this);
		if(latLng != null) {
			mContentView.setLoadImageUri(latLng.getStaticMapUri());
		} else {
			mContentView.setLoadImageObject(null);
		}
		mContentView.load();
	}
	
	private void checkDownloadingStatus() {
		LatLng latLng = getMessage().getMapMessageLocation();
		//check downloading status
		if(latLng == null) {
			hideDownloadingProgressAnimation();
		} else {
			String url = latLng.getStaticMapUri();
			if(sDownloadingImages.contains(url)) {
				showDownloadingProgressAnimation();
			} else {
				if(!StorageConfigs.isImageCachedWithURL(url)) {
					sDownloadingImages.add(getMessage().getMsgId());
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
