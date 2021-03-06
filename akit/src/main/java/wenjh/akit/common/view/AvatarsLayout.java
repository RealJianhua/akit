package wenjh.akit.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import wenjh.akit.R;

import java.util.Arrays;
import java.util.List;

import wenjh.akit.common.util.AvatarAndName;
import wenjh.akit.common.util.StringUtil;

public class AvatarsLayout extends FlowLayout {
	private OnAvatarItemClickedLsitener mAvatarItemClickedLsitener;
	
	public AvatarsLayout(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);
	}
	
	public AvatarsLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}
	
	public AvatarsLayout(Context context) {
		super(context);
	}
	
	public void setOnAvatarItemClickedLsitener(OnAvatarItemClickedLsitener mAvatarItemClickedLsitener) {
		this.mAvatarItemClickedLsitener = mAvatarItemClickedLsitener;
	}
	
	public void setAvatarURIS(String[] avatarGuids) {
		setAvatarURIS(Arrays.asList(avatarGuids));
	}
	
	public void setAvatarURIS(List<String> avatarGuids) {
		int interestCount = avatarGuids == null ? 0 : avatarGuids.size();
		
		// remove
		while(getChildCount() > interestCount) {
			removeViewAt(getChildCount()-1);
		}
		
		for (int i = 0; i < interestCount; i++) {
			SmartImageView imageView = null;
			TextView textView = null;
			View itemView = null;
			
			if(i >= getChildCount()) {
				itemView = LayoutInflater.from(getContext()).inflate(R.layout.include_profile_avatarsitem, this, false);
				addView(itemView, i); // append
				itemView.setOnClickListener(clickListener);
			} else {
				itemView = getChildAt(i);
			}
			
			itemView.setTag(R.id.tag_item, avatarGuids.get(i));
			itemView.setTag(R.id.tag_position, i);
			
			textView = (TextView) itemView.findViewById(R.id.textview);
			textView.setVisibility(GONE);
			
			imageView = (SmartImageView) itemView.findViewById(R.id.imageview);
			imageView.load(avatarGuids.get(i));
		}
	}
	
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mAvatarItemClickedLsitener != null) {
				Object tag = v.getTag(R.id.tag_item);
				int position = (Integer) v.getTag(R.id.tag_position);
				mAvatarItemClickedLsitener.onAvatarItemClicked(tag, position);
			}
		}
	};
	
	public void setAvatarArray(AvatarAndName[] avatarArray) {
		setAvatars(Arrays.asList(avatarArray));
	}
	
	public void setAvatars(List<AvatarAndName> avatarList) {
		int interestCount = avatarList == null ? 0 : avatarList.size();
		
		// remove
		while(getChildCount() > interestCount) {
			removeViewAt(getChildCount()-1);
		}
		
		for (int i = 0; i < interestCount; i++) {
			SmartImageView imageView = null;
			TextView textView = null;
			View itemView = null;
			
			if(i >= getChildCount()) {
				itemView = LayoutInflater.from(getContext()).inflate(R.layout.include_profile_avatarsitem, this, false);
				addView(itemView, i); // append
				itemView.setOnClickListener(clickListener);
			} else {
				itemView = getChildAt(i);
			}
			
			itemView.setTag(R.id.tag_item, avatarList.get(i));
			itemView.setTag(R.id.tag_position, i);
			
			imageView = (SmartImageView) itemView.findViewById(R.id.imageview);
			textView = (TextView) itemView.findViewById(R.id.textview);
			
			imageView.load(avatarList.get(i).getAvatarImage());
			String name = avatarList.get(i).getDisplayName();
			
			if(StringUtil.isEmpty(name)) {
				textView.setVisibility(GONE);
			} else {
				textView.setVisibility(VISIBLE);
				textView.setText(name);
			}
		}
	}

	
	public static interface OnAvatarItemClickedLsitener {
		void onAvatarItemClicked(Object object, int position);
	}
	
}
