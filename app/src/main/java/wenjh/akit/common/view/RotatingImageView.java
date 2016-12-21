package wenjh.akit.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public class RotatingImageView extends ImageView {
	private int mDegrees;
	private Scroller scroller = null;

	public RotatingImageView(Context paramContext) {
		super(paramContext);
		initRotatingImageView();
	}

	public RotatingImageView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		initRotatingImageView();
	}

	public RotatingImageView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		initRotatingImageView();
	}

	private void initRotatingImageView() {
		this.mDegrees = 0;
		scroller = new Scroller(getContext(), new LinearInterpolator(), true);
	}

	public void animateToDegress(int fromDegress, int toDegress, int duration) {
		if(!scroller.isFinished()) {
			scroller.abortAnimation();
		}
		scroller.startScroll(fromDegress, 0, toDegress, 0, duration);
		invalidate();
	}
	
	protected void onDraw(Canvas paramCanvas) {
		int i = getTop();
		int j = getLeft();
		int k = getBottom();
		int m = getRight() - j;
		int n = k - i;
		int i1 = paramCanvas.save();
		float f1 = m / 2.0F;
		float f2 = n / 2.0F;
		float f3 = this.mDegrees;
		paramCanvas.rotate(f3, f1, f2);
		super.onDraw(paramCanvas);
		paramCanvas.restoreToCount(i1);
	}

	public void setDegress(int degrees) {
		this.mDegrees = degrees;
		invalidate();
	}
	
	@Override
	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			this.mDegrees = scroller.getCurrX();
			invalidate();
		}
	}
}