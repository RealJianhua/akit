package wenjh.akit.common.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import wenjh.akit.R;
import wenjh.akit.common.util.ContextUtil;

public class MAlertDialog extends AlertDialog {

	public MAlertDialog(Context paramContext) {
		super(paramContext);
		setTitle(paramContext.getString(R.string.dialog_title));
	}

	public static MAlertDialog makeConfirm(Context context, int msgResid, int btn1TextResid, int btn2TextResid,
			OnClickListener btn1ClickListener, OnClickListener btn2ClickListener) {
		return makeConfirm(context, context.getString(msgResid), ContextUtil.getString(btn1TextResid), ContextUtil.getString(btn2TextResid),
				btn1ClickListener, btn2ClickListener);
	}

	public static MAlertDialog makeConfirm(Context context, CharSequence message, CharSequence btn1Text, CharSequence btn2Text,
			OnClickListener btn1ClickListener, OnClickListener btn2ClickListener) {
		MAlertDialog dialog = new MAlertDialog(context);
		dialog.setMessage(message);
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, btn1Text, btn1ClickListener);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, btn2Text, btn2ClickListener);
		return dialog;
	}

	public static MAlertDialog makeConfirm(Context context, CharSequence message, OnClickListener confirmButtonClickListener,
			OnClickListener cancelButtonClickListener) {
		return makeConfirm(context, message, context.getString(R.string.dialog_btn_cancel), context.getString(R.string.dialog_btn_confirm),
				cancelButtonClickListener, confirmButtonClickListener);
	}

	public static MAlertDialog makeConfirm(Context context, int id, OnClickListener confirmButtonClickListener,
			OnClickListener cancelButtonClickListener) {
		return makeConfirm(context, ContextUtil.getString(id), ContextUtil.getString(R.string.dialog_btn_cancel),
				context.getString(R.string.dialog_btn_confirm), cancelButtonClickListener, confirmButtonClickListener);
	}

	public static MAlertDialog makeConfirm(Context context, CharSequence message, OnClickListener onClickListener) {
		return makeConfirm(context, message, context.getString(R.string.dialog_btn_cancel), context.getString(R.string.dialog_btn_confirm),
				null, onClickListener);
	}

	public static MAlertDialog makeConfirm(Context context, int msgResid, OnClickListener onClickListener) {
		return makeConfirm(context, msgResid, R.string.dialog_btn_cancel, R.string.dialog_btn_confirm, null, onClickListener);
	}

	public static MAlertDialog makeSingleButtonDialog(Context context, CharSequence message, OnClickListener onClickListener) {
		MAlertDialog dialog = new MAlertDialog(context);
		dialog.setMessage(message);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.dialog_btn_confirm), onClickListener);

		return dialog;
	}

	public static MAlertDialog makeSingleButtonDialog(Context context, CharSequence message, String btnText,
			OnClickListener onClickListener) {
		MAlertDialog dialog = new MAlertDialog(context);
		dialog.setMessage(message);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, btnText, onClickListener);
		return dialog;
	}

	public static MAlertDialog makeSingleButtonDialog(Context context, int msgTextId, int btnTextId, OnClickListener onClickListener) {
		return makeSingleButtonDialog(context, ContextUtil.getString(msgTextId), ContextUtil.getString(btnTextId), onClickListener);
	}


}
