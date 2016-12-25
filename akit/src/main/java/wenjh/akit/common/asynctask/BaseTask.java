package wenjh.akit.common.asynctask;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import wenjh.akit.R;

import org.json.JSONException;

import wenjh.akit.activity.base.BaseActivity;
import wenjh.akit.common.http.NetworkBaseException;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.util.StringUtil;
import wenjh.akit.common.view.Toaster;

public abstract class BaseTask<Params, Progress, Result> extends AsyncTask<Params,  Progress, AsyncResult<Result>> {
	private LogUtil log = new LogUtil(getClass().getSimpleName());
	
	private Context context = null;
	private long threadId;
	
	public BaseTask(Context context) {
		this.context = context;
	}
	
	@Override
	protected final AsyncResult<Result> doInBackground(Params... params) {
		AsyncResult<Result> result = new AsyncResult<Result>();
			try {
				if(!isCancelled()) {
					threadId = Thread.currentThread().getId();
					result.result = executeTask(params);
				} else {
					result.exception = new Exception("task already canceled");
				}
			} catch (Throwable e) {
				result.exception = e;
			}
		return result;
	}

	@Override
	protected final void onPostExecute(AsyncResult<Result> result) {
		onTaskFinish();
		if(context != null && context instanceof BaseActivity) {
			((BaseActivity)context).removeAsyncTask(this);
		}
		if(result.exception == null) {
			onTaskSuccess(result.result);
		} else {
			if(result.exception instanceof Exception) {
				onTaskError((Exception)result.exception);
			} else {
				onTaskError(new Exception(result.exception));
			}
		}
		context = null;
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();
		context = null;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCancelled(AsyncResult<Result> result) {
		super.onCancelled(result);
		context = null;
	}
	
	@Override
	protected final void onPreExecute() {
		// error
		if (context == null) {
			cancel(true);
			AsyncResult<Result> result = new AsyncResult<Result>();
			result.exception = new Exception();
			onPostExecute(result);
			return;
		}
				
		onPreTask();
	}
	
	public boolean isRunning(){
		return getStatus() == Status.RUNNING;
	}
	
	protected abstract Result executeTask(Params... params) throws Exception;
	
	protected void onPreTask() {
	}
	
	protected void onTaskSuccess(Result result) {
		
	}
	
	protected void onTaskError(Exception e) {
		if(e == null) return;
		
		log.e(e);
		
		
		if(e instanceof NetworkBaseException) {
			if(!StringUtil.isEmpty(e.getMessage())) {
				toast(e.getMessage());
			} else {
				toast(R.string.network_error_other);
			}
		} else if(e instanceof JSONException) {
			toast(R.string.network_error_data);
		} else {
			toast(R.string.network_error_client);
		}
	}
	
	public void toast(int resId) {
		if(context != null && context instanceof BaseActivity) {
			((BaseActivity)context).toast(resId);
		} else {
			Toaster.show(resId);
		}
	}
	
	public void toast(String message) {
		toast(message, Toaster.LENGTH_SHORT);
	}
	
	public void toast(String message, int duration) {
		if(context != null && context instanceof BaseActivity) {
			((BaseActivity)context).toast(message, duration);
		} else {
			Toaster.show(message, duration);
		}
	}
	
	public Context getContext() {
		return context;
	}
	
	/**
	 * 在任务执行结束时调用（无论是否发生异常）。并且在 {@link #onTaskError(Exception)} 和 {@link #onTaskSuccess(Object)} 之前调用。
	 */
	protected void onTaskFinish() {
		
	}
}

class AsyncResult<Result> {
	Result result;
	Throwable exception;
	public AsyncResult() {
	}
}
