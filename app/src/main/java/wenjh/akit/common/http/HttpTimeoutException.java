package wenjh.akit.common.http;

import com.wenjh.akit.R;
import wenjh.akit.common.util.ContextUtil;


public class HttpTimeoutException extends NetworkBaseException {
	private static final long serialVersionUID = 1L;
	public HttpTimeoutException(){
		super(ContextUtil.getStringFromResource(R.string.network_error_timeout));
	}
}
