package wenjh.akit.common.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import wenjh.akit.R;

public class ImageButton extends LinearLayout {
	private int layout = R.layout.common_imagebutton;
	private CharSequence mLabelText = "";
	private ImageView mLeftIconView = null;
	private ImageView mRightIconView = null;
	private TextView mLabelView = null;
	private LinearLayout mContainerView = null;

	public ImageButton(Context context) {
		super(context);
		inflate();
	}

	public ImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate();
	}

	private void inflate() {
		LayoutInflater inflater = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		inflater.inflate(layout, this, true);
		mLeftIconView = (ImageView) findViewById(R.id.imgbtn_iv_lefticon);
		mRightIconView = (ImageView) findViewById(R.id.imgbtn_iv_righticon);
		mLabelView = (TextView) findViewById(R.id.imgbtn_tv_text);
		mContainerView = (LinearLayout) findViewById(R.id.imgbtn_layout_container);
	}

	@Override
	public void setBackgroundResource(int resid) {
		mContainerView.setBackgroundResource(resid);
	}

	@Override
	public void setBackgroundColor(int color) {
		mContainerView.setBackgroundColor(color);
	}

	@Override
	public void setBackgroundDrawable(Drawable d) {
		mContainerView.setBackgroundDrawable(d);
	}

	public void setTextColor(int color) {
		mLabelView.setTextColor(color);
	}

	public void setLayoutPadding(int left, int top, int right, int bottom) {
		mContainerView.setPadding(left, top, right, bottom);
	}

	public ImageButton setLeftIconResource(int resId) {
		if (resId <= 0) {
			mLeftIconView.setVisibility(View.GONE);
		} else {
			mLeftIconView.setVisibility(View.VISIBLE);
			mLeftIconView.setImageResource(resId);
		}
		return this;
	}

	public ImageButton setLeftIconResource(Drawable d) {
		if (d == null) {
			mLeftIconView.setVisibility(View.GONE);
		} else {
			mLeftIconView.setVisibility(View.VISIBLE);
			mLeftIconView.setImageDrawable(d);
		}
		return this;
	}
	
	protected ImageView getLeftIconView() {
		return mLeftIconView;
	}
	
	protected ImageView getRightIconView() {
		return mRightIconView;
	}

	public ImageButton setLeftIconResource(Bitmap b) {
		if (b == null) {
			mLeftIconView.setVisibility(View.GONE);
		} else {
			mLeftIconView.setVisibility(View.VISIBLE);
			mLeftIconView.setImageBitmap(b);
		}
		return this;
	}

	public ImageButton setRightIconResource(int resId) {
		if (resId <= 0) {
			mRightIconView.setVisibility(View.GONE);
		} else {
			mRightIconView.setVisibility(View.VISIBLE);
			mRightIconView.setImageResource(resId);
		}
		return this;
	}

	public void setLeftIconViewVisibility(int visibility) {
		mLeftIconView.setVisibility(visibility);
	}
	
	public ImageButton setRightIconResource(Drawable d) {
		if (d == null) {
			mRightIconView.setVisibility(View.GONE);
		} else {
			mRightIconView.setVisibility(View.VISIBLE);
			mRightIconView.setImageDrawable(d);
		}
		return this;
	}

	public ImageButton setRightIconResource(Bitmap b) {
		if (b == null) {
			mRightIconView.setVisibility(View.GONE);
		} else {
			mRightIconView.setVisibility(View.VISIBLE);
			mRightIconView.setImageBitmap(b);
		}
		return this;
	}

	public void setRightIconViewVisibility(int visibility) {
		mRightIconView.setVisibility(visibility);
	}

	public ImageButton setText(CharSequence c) {
		if (c == null) {
			mLabelView.setVisibility(View.GONE);
		} else {
			this.mLabelText = c;
			mLabelView.setVisibility(View.VISIBLE);
			mLabelView.setText(this.mLabelText);
		}
		return this;
	}

	public ImageButton setText(int resId) {
		if (resId <= 0) {
			mLabelView.setVisibility(View.GONE);
		} else {
			this.mLabelText = getContext().getString(resId);
			mLabelView.setVisibility(View.VISIBLE);
			mLabelView.setText(this.mLabelText);
		}
		return this;
	}

	public CharSequence getText() {
		return mLabelText;
	}

	@Override
	public void setOnClickListener(OnClickListener click) {
		mContainerView.setOnClickListener(click);
	}

	@Override
	public void setEnabled(boolean enabled) {
		mContainerView.setEnabled(enabled);
	}
}
