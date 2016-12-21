package wenjh.akit.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** 
 * @author <a href="mailto:wenlin56@sina.com">wjh</a>
 */
public class StreamUtils {
	public static byte[] stream2byteArr(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		int len;
		
		while((len = is.read(buffer)) != -1 ) {
			bos.write(buffer, 0, len);
		}
		
		bos.close();
		
		return bos.toByteArray();
	}
}
