package wenjh.akit.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
	public static String readString(File file) throws IOException {
		InputStream is = null;
		try {
			if(!file.exists())
				return null;
			is = new FileInputStream(file);
			int buffersize = is.available();
			byte buffer[] = new byte[buffersize];
			is.read(buffer);
			String result = new String(buffer, "UTF-8");
			return result;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public static void writeString(File file, String text) throws IOException {
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			outputStream.write(text.getBytes());
			outputStream.flush();
		} finally {
			IOUtils.closeQuietly(outputStream);
		}
	}
	
	public static void copyFile(File oldFile, File newFile) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new BufferedInputStream(new FileInputStream(oldFile), 4096);
			os = new BufferedOutputStream(new FileOutputStream(newFile), 4096);
			int len = -1;
			byte[] buffer = new byte[2048]; 
			while((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
			os.flush();
		} catch(IOException e) {
			if(newFile.exists()) {
				newFile.delete();
			}
			throw e;
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
	}
}
