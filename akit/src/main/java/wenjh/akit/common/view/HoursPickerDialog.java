package wenjh.akit.common.view;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;

import wenjh.akit.R;
import wenjh.akit.common.util.ContextUtil;

public class HoursPickerDialog extends MAlertDialog {
	NumberPicker startNumberPicker = null;
	NumberPicker endNumberPicker = null;
	OnClickListener mOuterConfimlListener = null;
	
	public HoursPickerDialog(Context paramContext) {
		super(paramContext);
		View contentview = ContextUtil.getLayoutInflater().inflate(R.layout.dialog_hourpicker, null);
		setView(contentview);
		startNumberPicker = (NumberPicker) contentview.findViewById(R.id.start_time);
		endNumberPicker = (NumberPicker) contentview.findViewById(R.id.end_time);

		startNumberPicker.setMaxValue(23);
		startNumberPicker.setMinValue(0);
		endNumberPicker.setMaxValue(23);
		endNumberPicker.setMinValue(0);

		OnClickListener cancelListener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cancel();
			}
		};
		setButton(DialogInterface.BUTTON_NEGATIVE, paramContext.getString(R.string.dialog_btn_cancel), cancelListener);
		setButton(DialogInterface.BUTTON_POSITIVE, paramContext.getString(R.string.dialog_btn_confirm), confimlListener);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}
	
	OnClickListener confimlListener = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(mOuterConfimlListener != null) {
				mOuterConfimlListener.onClick(dialog, which);
			}
		}
	};
	
	public void setConfimlListener(OnClickListener mConfimlListener) {
		this.mOuterConfimlListener = mConfimlListener;
	}

	public void setInitValue(int start, int end) {
		startNumberPicker.setValue(start);
		endNumberPicker.setValue(end);
	}
	
	@Override
	public void show() {
		super.show();
	}
	
	public int getStartHour() {
		return startNumberPicker.getValue();
	}
	
	public int getEndHour() {
		return endNumberPicker.getValue();
	}
	
}
