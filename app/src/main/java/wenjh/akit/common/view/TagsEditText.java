package wenjh.akit.common.view;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ReplacementSpan;
import android.text.style.UpdateLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.wenjh.akit.R;

import wenjh.akit.common.util.ContextUtil;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.util.TextWatcherAdapter;

public class TagsEditText extends EmoteEditeText {
	final static String sTagsRegexPattern = "(\\{.*?\\})";
	int tagPaddingRight = 0;
	final static String sTagStart = "\\{";
	final static String sTagEnd = "\\}";
	int tagBackgroundColor = 0;
	LogUtil log = new LogUtil(this);

	public TagsEditText(Context context) {
		super(context);
		init();
	}

	public TagsEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TagsEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}


	public void setTags(List<String> tags) {
		getText().clear();
		if(tags != null ) {
			for (String tag : tags) {
				append("{"+tag+"}");
			}
		}
	}
	
	public void setTagBackgroundColor(int tagBackgroundColor) {
		this.tagBackgroundColor = tagBackgroundColor;
	}

	@Override
	public void setTextSize(int unit, float size) {
		super.setTextSize(unit, size);
		tagPaddingRight = (int) getTextSize() / 2;
	}

	private void init() {
		tagPaddingRight = (int) getTextSize() / 2;

		addTextChangedListener(new TextWatcherAdapter() {
			@Override
			public void afterTextChanged(Editable s) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (count == 0 || count > 1) {
					return;
				}

				CharSequence changedText = s.toString().substring(start, start + count);
				if (" ".equals(changedText)) {
					StringBuilder builder = new StringBuilder(getText().toString());
					builder.deleteCharAt(builder.length()-1); // delete space code
					changeText(builder);
				}
			}
		});

		setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						changeText(new StringBuilder(getText().toString()));
						return true;
					}
				}
				return false;
			}
		});
	}
	
	private void changeText(StringBuilder builder) {
		if(getTagsText(builder)) {
			setText(builder);
			setSelection(builder.length());
		}
	}
	
	private boolean getTagsText(StringBuilder builder) {
		int index = builder.lastIndexOf("}");
		if(index < builder.length()-1) {
			index = (index <= 0) ? 0 : index + 1;
			builder.insert(index, "{");
			builder.append("}");
			return true;
		}
		return false;
	}

	public List<String> getTags() {
		Pattern pattern = Pattern.compile(sTagsRegexPattern);
		List<String> tags = new ArrayList<String>();
		
		StringBuilder text = new StringBuilder(getText().toString().trim());
		getTagsText(text);
		
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String tag = matcher.group();
			tag = tag.replaceAll(sTagEnd, "");
			tag = tag.replaceAll(sTagStart, "");
			tags.add(tag);
		}
		
		return tags;
	}

	@Override
	protected CharSequence replaceInputText(CharSequence text) {
		boolean found = false;
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		Pattern pattern = Pattern.compile(sTagsRegexPattern);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			found = true;
			builder.setSpan(new TextEmoteSpan(getContext(), matcher.group(), tagPaddingRight, tagBackgroundColor), matcher.start(), matcher.end(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return found ? builder : text;
	}

	public static class TextEmoteSpan2 extends BackgroundColorSpan implements UpdateLayout {

		public TextEmoteSpan2(Context context, String name, int padding, int backgroundColor) {
			super(Color.BLACK);
		}

		@Override
		public int describeContents() {
			return 1;
		}

	}

	public static class TextEmoteSpan3 extends ImageSpan {
		String mTagName;
		int paddingRight;
		int backgroundColor;
		RectF rectF = null;
		LogUtil log = new LogUtil(this);
		Bitmap bitmap = null;

		public TextEmoteSpan3(Context context, String name, int padding, int backgroundColor) {
			super(ALIGN_CENTERVERTICAL);
			setTagName(name);
			this.paddingRight = padding;
			this.backgroundColor = backgroundColor;
		}

		public void setTagName(String tagName) {
			tagName = tagName.replaceAll(sTagStart, "");
			tagName = tagName.replaceAll(sTagEnd, "");
			mTagName = tagName;
		}

		public int getTextWidth(Paint paint, CharSequence text) {
			return (int) paint.measureText(mTagName) + paddingRight;
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			// TODO Auto-generated method stub
			super.updateDrawState(ds);
		}

		/*
		 * @Override public void draw(Canvas canvas, CharSequence text, int
		 * start, int end, float x, int top, int y, int bottom, Paint paint) {
		 * Paint backPaint = new Paint(); // canvas.drawRect(new Rect(0, 0, 50,
		 * 50), backPaint); // canvas.drawText(mTagName, x, y, paint);
		 * 
		 * log.i("end=" + end + ", start=" + start + ", top=" + top +
		 * ", bottom=" + bottom + ", x=" + x + ", y=" + y);
		 * 
		 * Bitmap bitmap = Bitmap.createBitmap(200, 200,
		 * Bitmap.Config.ARGB_8888); Canvas canvas2 = new Canvas(bitmap); //
		 * backPaint.setColor(Color.LTGRAY); //
		 * backPaint.setStyle(Paint.Style.FILL); // canvas2.drawRect(new Rect(0,
		 * 0, bitmap.getWidth(), bitmap.getHeight()), backPaint);
		 * backPaint.setColor(Color.BLACK);
		 * backPaint.setStyle(Paint.Style.FILL);
		 * backPaint.setTextSize(paint.getTextSize());
		 * canvas2.drawText(mTagName, 0, 0, backPaint);
		 * 
		 * canvas.save();
		 * 
		 * int transY = bottom - bitmap.getHeight();
		 * 
		 * canvas.drawBitmap(bitmap, null, new Rect(0, top, 0, bottom),
		 * backPaint); //.drawBitmap(bitmap, x, y, paint);
		 * 
		 * canvas.restore(); }
		 */

		@Override
		public Drawable getDrawable() {
			int textSize = ContextUtil.sp2pix(15);
			int textColor = ContextUtil.getResources().getColor(R.color.text_title);
			Paint paint = new Paint();
			paint.setTextSize(textSize);
			paint.setAntiAlias(true);

			int textWidth = getTextWidth(paint, mTagName);
			Bitmap bitmap = Bitmap.createBitmap(textWidth, textSize, Bitmap.Config.ARGB_8888);

			Canvas canvas2 = new Canvas(bitmap);
			paint.setColor(Color.WHITE);
			paint.setStyle(Paint.Style.FILL);
			canvas2.drawPaint(paint);

			paint.setColor(textColor);
			canvas2.drawText(mTagName, bitmap.getWidth() / 2, bitmap.getHeight() / 2, paint);
			Drawable drawable = new BitmapDrawable(ContextUtil.getResources(), bitmap);
			drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
			return drawable;
		}

	}

	public static class TextEmoteSpan extends ReplacementSpan {
		String mTagName;
		int paddingRight;
		int backgroundColor;
		RectF rectF = null;

		public TextEmoteSpan(Context context, String name, int padding, int backgroundColor) {
			setTagName(name);
			this.paddingRight = padding;
			this.backgroundColor = backgroundColor;
		}

		public void setTagName(String tagName) {
			tagName = tagName.replaceAll(sTagStart, "");
			tagName = tagName.replaceAll(sTagEnd, "");
			mTagName = tagName;
		}

		@Override
		public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
			return (int) paint.measureText(mTagName) + paddingRight;
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.bgColor = Color.BLACK;
		}

		@Override
		public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
			// Paint backPaint = new Paint();
			// backPaint.setColor(Color.LTGRAY);
			// canvas.drawRect(new Rect(0, 0, 50, 50), backPaint);
			canvas.drawText(mTagName, x, y, paint);
		}
	}
}
