package wenjh.akit.common.http;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpFormFile {
	private byte[] data;
	private InputStream inStream;
	private File file;
	private String filname;
	private String parameterName;
	private String contentType = "application/octet-stream";
	
	/**
	 * @param filname 文件名称
	 * @param data    上传文件的二进制数据
	 * @param parameterName 参数名称，服务器将通过此参数名获取到上传文件
	 * @param contentType 上传文件类型
	 */
	public HttpFormFile(String filname, byte[] data, String parameterName, String contentType) {
		this.data = data;
		this.filname = filname;
		this.parameterName = parameterName;
		if(contentType!=null) this.contentType = contentType;
	}
	
	public HttpFormFile(String filname, File file, String parameterName, String contentType) {
		this.filname = filname;
		this.parameterName = parameterName;
		this.file = file;
		if(contentType!=null) {
			this.contentType = contentType;
		} else {
			this.contentType = getContentType(file);
		}
	}

	public HttpFormFile(String filname, File file, String parameterName) {
		this(filname, file, parameterName, null);
	}
	
	private static String getContentType(File file) {
		if(file != null && file.getName().indexOf('.') >= 0) {
			String suffix = file.getName().substring(file.getName().lastIndexOf('.'));
			String contentType = "";
			if(".html".equalsIgnoreCase(suffix)) {
				contentType = "text/html";
			} else if(".jpg".equalsIgnoreCase(suffix)) {
				contentType = "image/jpeg";
			} else if(".jpeg".equalsIgnoreCase(suffix)) {
				contentType = "image/jpeg";
			} else if(".mp3".equalsIgnoreCase(suffix)) {
				contentType = "audio/mpeg";
			} else if(".mp4".equalsIgnoreCase(suffix)) {
				contentType = "video/mp4";
			} else if(".gif".equalsIgnoreCase(suffix)) {
				contentType = "image/gif";
			} else if(".txt".equalsIgnoreCase(suffix)) {
				contentType = "text/plain";
			} else if(".png".equalsIgnoreCase(suffix)) {
				contentType = "image/png";
			} else {
				contentType = "application/octet-stream";
			}
			
			return contentType;
		}
		
		return null;
	}
	
	public File getFile() {
		return file;
	}

	public InputStream getInputStream() throws IOException {
		if(file != null) {
			return new BufferedInputStream(new FileInputStream(file), 4096);
		}
		return inStream;
	}

	public byte[] getInputData() {
		return data;
	}

	public String getFilname() {
		return filname;
	}

	public void setFilname(String filname) {
		this.filname = filname;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}


	@Override
	public String toString() {
		return "FormFile [data=" + data + ", file=" + file
				+ ", filname=" + filname + ", parameterName=" + parameterName
				+ ", contentType=" + contentType + "]";
	}
}