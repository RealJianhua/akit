package wenjh.akit.common.view;

import java.util.Arrays;
import java.util.List;

import com.wenjh.akit.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

public class TagsLayout extends FlowLayout {

	public TagsLayout(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);
	}
	
	public TagsLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}
	
	public TagsLayout(Context context) {
		super(context);
	}
	
	public void setTags(String[] tags) {
		setTags(Arrays.asList(tags));
	}
	
	public void setTags(List<String> tags) {
		int interestCount = tags == null ? 0 : tags.size();
		
		// remove
		while(getChildCount() > interestCount) {
			removeViewAt(getChildCount()-1);
		}
		
		for (int i = 0; i < interestCount; i++) {
			TextView tagView = null;
			if(i >= getChildCount()) {
				tagView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.include_profile_tagitem, this, false);
				addView(tagView, getChildCount()-1); // append
			} else {
				tagView = (TextView) getChildAt(i);
			}
			tagView.setText(tags.get(i));
		}
	}

}
