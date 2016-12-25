package wenjh.akit.common.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import wenjh.akit.R;

public class MProgressDialog extends ProgressDialog {
	AsyncTask<?,?,?> mTask = null;

	public MProgressDialog(Context context) {
		this(context, context.getString(R.string.progress));
	}
	
	public MProgressDialog(Context context, AsyncTask<?, ?, ?> task) {
		this(context, context.getString(R.string.progress), task);
	}
	
	public MProgressDialog(Context context, String s) {
		this(context, null, s);
	}
	
	public MProgressDialog(Context context, String s, AsyncTask<?, ?, ?> task) {
		this(context, null, s, task);
	}
	
	public MProgressDialog(Context context, String title, String msg) {
		this(context, title, msg, null);
	}
	
	public MProgressDialog(Context context, String title, String msg, AsyncTask<?, ?, ?> task) {
		super(context);
		if(title != null) {
			setTitle(title);
		}
		setMessage(msg);
		setCancelable(true);
		setSyncTask(task);
		setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if(mTask != null) {
					mTask.cancel(true);
				}
			}
		});
	}

	public void setSyncTask(AsyncTask<?,?,?> task) {
		this.mTask = task;
	}
	
	@Override
	public void dismiss() {
		super.dismiss();
		mTask = null;
	}
	
}
