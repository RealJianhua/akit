package wenjh.akit.common.http;

import com.wenjh.akit.R;
import wenjh.akit.common.util.ContextUtil;


public class NetworkBaseException extends Exception {

	private static final long serialVersionUID = -3970373080567427194L;
	
	public NetworkBaseException() {
		this(ContextUtil.getStringFromResource(R.string.network_error_other));
	}

	public NetworkBaseException(String string) {
		super(string);
	}

	public NetworkBaseException(String string, Throwable e) {
		super(string, e);
	}

	public NetworkBaseException(Throwable e) {
		super(ContextUtil.getStringFromResource(R.string.network_error_other), e);
	}

}
