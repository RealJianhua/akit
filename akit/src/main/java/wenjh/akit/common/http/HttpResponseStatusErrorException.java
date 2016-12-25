package wenjh.akit.common.http;

import wenjh.akit.R;
import wenjh.akit.common.util.ContextUtil;


public class HttpResponseStatusErrorException extends NetworkBaseException {
	private static final long serialVersionUID = 1L;
	public int statusCode = -1;

	public HttpResponseStatusErrorException(int responseCode) {
		super(ContextUtil.getStringFromResource(R.string.network_error_other) + "("+responseCode+")");
		this.statusCode = responseCode;
	}

}
