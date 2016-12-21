package wenjh.akit.common.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.wenjh.akit.R;

public class LoadingButton extends ImageButton implements View.OnClickListener {
	private String mNormalText = "";
	private String mLoadingText = "";
	private Animation mLoadingAnimation = null;
	private int mNormalIconResId = 0;
	private int mProcessDrawable = R.drawable.ic_loading;
	private boolean mLoading = false;
	private OnClickListener mOnButtonClickedListener = null;
	
	public LoadingButton(Context context) {
		super(context);
		init();
	}
	
	public LoadingButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public void setNormalText(int resId) {
		setNormalText(getContext().getString(resId));
	}
	
	
	public void setLoadingText(int resId) {
		setLoadingText(getContext().getString(resId));
	}
	
	public void setNormalText(String text) {
		this.mNormalText = text;
		if(!mLoading) {
			setText(mNormalText);
		}
	}
	
	public void setOnClickListener(OnClickListener click) {
		mOnButtonClickedListener = click;
	}
	
	public void setLoadingText(String loadingText) {
		this.mLoadingText = loadingText;
		if(mLoading) {
			setText(loadingText);
		}
	}
	
	public void setNormalIconResId(int normalIconResId) {
		this.mNormalIconResId = normalIconResId;
		
		if(normalIconResId < 0) normalIconResId = 0;
		
		if(!mLoading) {
			setLeftIconResource(normalIconResId);
		}
	}
	
	public String getLoadingText() {
		return mLoadingText;
	}
	
	public String getNormalText() {
		return mNormalText;
	}
	
	private void init() {
		mNormalText = getContext().getString(R.string.loadmore);
		mLoadingText = getContext().getString(R.string.loading);
		setText(mNormalText);
		setTextColor(getResources().getColor(R.color.text_btn_loadmore));
		setLeftIconResource(mNormalIconResId);
		super.setOnClickListener(this);
	}

	public boolean isLoading() {
		return mLoading;
	}
	
	@Override
	public void onClick(View v) {
		onButtonClicked();
	}

	private void onButtonClicked() {
		if(!mLoading && mOnButtonClickedListener != null) {
			startLoding();
			if(mOnButtonClickedListener != null) {
				mOnButtonClickedListener.onClick(this);
			}
		}
	}
	
	public void stopLoading() {
		mLoading = false;
		stopAnimation(getLeftIconView());
		setLeftIconResource(mNormalIconResId);
		setText(mNormalText);
		setEnabled(true);
	}
	
	public void startLoding() {
		mLoading = true;
		setLeftIconResource(mProcessDrawable);
		startAnimation(getLeftIconView());
		setText(mLoadingText);
		setEnabled(false);
	}
	
	public void startLoadingWithButtonText(int textResId) {
		startLoding();
		setText(textResId);
	}
	
	private void stopAnimation(ImageView view) {
		if(view != null) {
			view.clearAnimation();
		}
	}
	
	private void startAnimation(final ImageView view) {
		final Drawable d = view.getDrawable();
		if(d == null) {
			return;
		}
		
		if(mLoadingAnimation == null) {
			mLoadingAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.loading);
		}
		
		view.startAnimation(mLoadingAnimation);
	}
}
