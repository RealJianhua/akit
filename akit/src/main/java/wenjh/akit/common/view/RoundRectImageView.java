package wenjh.akit.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;

import wenjh.akit.R;

public class RoundRectImageView extends CircleImageView {
    private float mTopLeft = 0;
    private float mTopRight = 0;
    private float mBottomRight = 0;
    private float mBottomLeft = 0;
    private RoundRectShape mRoundRectShape;

    public RoundRectImageView(Context context) {
        super(context);
    }

    public RoundRectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttributes(context, attrs);
    }

    public RoundRectImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getAttributes(context, attrs);
    }

    private void getAttributes(Context context, AttributeSet attrs) {
        final TypedArray typedArrayAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.RoundRectImageView);
        int radius = typedArrayAttributes.getDimensionPixelSize(R.styleable.RoundRectImageView_radius, 0);
        mTopLeft = typedArrayAttributes.getDimensionPixelSize(R.styleable.RoundRectImageView_topLeftRadius, radius);
        mTopRight = typedArrayAttributes.getDimensionPixelSize(R.styleable.RoundRectImageView_topRightRadius, radius);
        mBottomLeft = typedArrayAttributes.getDimensionPixelSize(R.styleable.RoundRectImageView_bottomLeftRadius, radius);
        mBottomRight = typedArrayAttributes.getDimensionPixelSize(R.styleable.RoundRectImageView_bottomRightRadius, radius);
        typedArrayAttributes.recycle();
        
        mRoundRectShape = new RoundRectShape(new float[]{
                mTopLeft, mTopLeft,
                mTopRight, mTopRight,
                mBottomRight, mBottomRight,
                mBottomLeft, mBottomLeft
        }, null, null);
    }

    @Override
    protected void drawDrawable(Canvas canvas) {
    	mRoundRectShape.resize(getWidth()-getPaddingLeft()-getPaddingRight(), getHeight()-getPaddingTop()-getPaddingBottom());
    	mRoundRectShape.draw(canvas, getBitmapPaint());
    }

    public float getTopLeftRadius() {
        return mTopLeft;
    }

    public void setTopLeftRadius(float radius) {
        this.mTopLeft = radius;
    }

    public float getTopRightRadius() {
        return mTopRight;
    }

    public void setTopRightRadius(float radius) {
        this.mTopRight = radius;
    }

    public float getBottomRightRadius() {
        return mBottomRight;
    }

    public void setBottomRightRadius(float radius) {
        this.mBottomRight = radius;
    }

    public float getBottomLeftRadius() {
        return mBottomLeft;
    }

    public void setBottomLeftRadius(float radius) {
        this.mBottomLeft = radius;
    }
    
    public void setRadius(float radius) {
        this.mBottomLeft = radius;
        this.mBottomRight = radius;
        this.mTopLeft = radius;
        this.mTopRight = radius;
        invalidate();
    }
    
}