package wenjh.akit.common.asynctask;

import java.net.HttpURLConnection;

import org.json.JSONException;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.wenjh.akit.R;

import wenjh.akit.activity.base.BaseActivity;
import wenjh.akit.common.http.HttpConnectionManager;
import wenjh.akit.common.http.NetworkBaseException;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.util.StringUtil;
import wenjh.akit.common.view.Toaster;

/**
 * 异步任务的抽象类，主要封装了catch异常的模块 <br/>
 * 
 * {@link #executeTask(Object...)} 方法里的事务，将新启线程来执行。除此之外，其它生命周期方法(“on” 打头的方法)都在主线程(启动此Task的线程)执行。<br/>
 * 执行前首先调用：{@link #onPreTask()}方法。执行结束后，调用 {@link #onTaskFinish()} 方法。<br/>
 * 
 * {@link #onTaskFinish()} 方法后，或者会调用 {@link #onTaskSuccess(Result result)} 或者会调用 {@link #onTaskError(Exception e)}。<br/>
 * 
 * {@link #onTaskError(Exception e)} 方法参数是 在 {@link #executeTask(Object...)} catch 到的异常。
 *   默认的处理方式是 {@link NetworkBaseException} 的异常，直接 Toater 提示 HttpBaseException.getMessage 的内容，其它异常 Toater 提示 R.string.errormsg_server
 * 
 * {@link #onTaskSuccess(Result result)} ： 无异常，成功执行。
 * 
 * 模板一：<br/>
 * <pre>
 *  private final class PostHttpTask extends BaseTask<Object, Object, Object> {
 *      protected Object executeTask(Object... params) throws Exception {
 *          // Client.XXX();
 *          return null;
 *      }
 *  }
 *  
 *  </pre>
 *  
 *  模板二：<br/>
 *  <pre>
 *  private final class PostHttpTask extends BaseTask<Object, Object, Object> {
 *      protected Object executeTask(Object... params) throws Exception {
 *          // Client.XXX();
 *          return null;
 *      }
 *      
 *      protected void onTaskError(Exception e) {
 *          if(e instansof HttpException403) {
 *           Toaster.show("没有权限，不能提交，请查看要提交的数据");  // 定制异常处理方式
 *           return;
 *          }
 *          super.onTaskError(e);
 *      }
 *  }
 *  
 *  </pre>
 *  
 *  
 *  模板三：<br/>
 *  <pre>
 *  private final class PostHttpTask extends BaseTask<Object, Object, User> {
 *    protected void onPreTask() {
 *    	showDialog(new MProcessDialog(SettingActivity.this, "请稍候，正在提交..."), this);
 *	  }
 *  
 *    protected Object executeTask(Object... params) throws Exception {
 *          // Client.XXX();
 *          return new User();
 *     }
 *      
 *     protected void onTaskFinish() {
 *       closeDialog();
 *     }
 *      
 *     protected void onTaskError(Exception e) {
 *          super.onTaskError(e);
 *          statusView.setText("刷新错误");
 *     }
 *      
 *     protected void onTaskSuccess(User user) {
 *        user.save();
 *        statusView.setText("刷新成功");
 *     }
 *  }
 *  
 *  </pre>
 * 
 * 
 * 
 * 
 * @author wenjianhua
 *
 * @param <Params> 此类型的参数将传递到 {@link #executeTask(Object...)} 中去
 * @param <Progress> 此类型的参数将传递到 {@link #onProgressUpdate(Object...)} 中去
 * @param <Result> 此类型的参数将传递到 {@link #onTaskSuccess(Object)} 中去
 */
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
	
	
	public void killAsynctask() {
		if(!isCancelled()) {
			cancel(true);
		}
		if(threadId > 0 && isKillRuningOnCancelled()) {
			killRuningTask();
		}
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
	
	protected void killRuningTask() {
		final HttpURLConnection httpURLConnection = HttpConnectionManager.getInstance().removeThreadActiveConnection(threadId);
		if(httpURLConnection != null) {
			HandyThreadPool.getGlobalThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					try {
						log.w(httpURLConnection.getURL().getPath()+"->disconned");
						httpURLConnection.disconnect();
					} catch (Exception e) {
						log.e(e);
					}
				}
			});
		}
	}
	
	protected boolean isKillRuningOnCancelled() {
		return true;
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
	
	/**
	 * 开启异步线程执行此任务
	 * @param params
	 * @return
	 */
	protected abstract Result executeTask(Params... params) throws Exception;
	
	/**
	 * 任务执行前被调用
	 */
	protected void onPreTask() {
	}
	
	/**
	 * 当任务执行成功后调用。<br/>
	 * 执行成功的标识是 {@link #executeTask(Object...)} 过程中没有发生异常。即此方法和 {@link #onTaskError(Exception)} 是互斥的。
	 * @param result
	 */
	protected void onTaskSuccess(Result result) {
		
	}
	
	/**
	 * 任务发生异常时调用。<br/>
	 * 当发生的异常属于 {@link NetworkBaseException} 类型时，默认采用 Toast 打印 Exception.getMessage() 的内容。
	 * 其它类型的异常会打印 R.string.errormsg_server 的字符串。<br/>
	 * 此方法调用后，{@link #onTaskSuccess(Result result)} 不会再调用了。<br/>
	 * 
	 * @see NetworkBaseException
	 * @see NetworkBaseException#getMessage()
	 * @param e
	 */
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
