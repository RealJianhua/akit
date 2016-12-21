package wenjh.akit.common.view;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.wenjh.akit.R;

import wenjh.akit.common.util.ContextUtil;

public class InputTextDialog extends MAlertDialog {
	EditText textView = null;
	View contentview = null;
	
	public InputTextDialog(Context paramContext, OnClickListener confimListener) {
		super(paramContext);
		contentview = ContextUtil.getLayoutInflater().inflate(R.layout.dialog_singleedittext, null);
		setView(contentview);
		textView = (EditText) contentview;
		OnClickListener cancelListener = null;
		setButton(DialogInterface.BUTTON_NEGATIVE, 
				paramContext.getString(R.string.dialog_btn_cancel), cancelListener);
		setButton(DialogInterface.BUTTON_POSITIVE, 
				paramContext.getString(R.string.dialog_btn_confirm), confimListener);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}

	@Override
	public void show() {
		super.show();
	}
	
	public void setLines(int lines) {
		textView.setLines(lines);
	}
	
	public void setMaxLength(int max) {
		textView.setFilters(new InputFilter[] { new InputFilter.LengthFilter(max) });
	}
	
	public Editable getInputContent() {
		return textView.getText();
	}
	
	public void setHint(String h) {
		textView.setHint(h);
	}
	
	public void setText(CharSequence str){
		textView.setText(str);
		textView.setSelection(str.length());
	}
}
