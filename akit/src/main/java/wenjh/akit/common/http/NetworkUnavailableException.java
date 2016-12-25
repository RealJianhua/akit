package wenjh.akit.common.http;

import wenjh.akit.R;
import wenjh.akit.common.util.ContextUtil;

public class NetworkUnavailableException extends NetworkBaseException {
	private static final long serialVersionUID = 1L;

	
	public NetworkUnavailableException() {
		super(ContextUtil.getStringFromResource(R.string.network_error_unavailable));
	}
	
}
