package wenjh.akit.common.http;

public class HttpServerReturnedException extends NetworkBaseException {
	private static final long serialVersionUID = 1L;
	
	private String serverResult = null;
	private int errorCode;
	
	public HttpServerReturnedException(String errmsg, int errcode, String result) {
		super(errmsg);
		this.serverResult = result;
		this.errorCode = errcode;
	}
	
	public int getErrorCode() {
		return errorCode;
	}

	public String getServerResult() {
		return serverResult;
	}

}
