package wenjh.akit.common.http;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import wenjh.akit.common.util.ImageUtil;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.util.StringUtil;
import wenjh.akit.common.util.UniqueIDentity;
import wenjh.akit.demo.people.UserApi;

public class RangeUploadHandler {
	public final static int LENGTH = 10 * 1024;

	private static LogUtil log = new LogUtil("RangeUploadHandler");
	public interface UploadCallBack {
		void onProgressUpdate(long progress);
	}

	public interface DoUploadHandler {
		String doUpload(ByteArrayOutputStream bos, String uuid, long offset, long fileLength) throws Exception;
	}

	private final static String defaultUpload(final ResumableUploadData data, final int source) throws Exception {
		DoUploadHandler doUploadHandler = new DoUploadHandler() {
			@Override
			public String doUpload(ByteArrayOutputStream bos, String uuid, long offset, long fileLength) throws Exception {
				fillToResumableData(data, bos, uuid, offset, fileLength);
				return UserApi.getInstance().uploadRangeBytesData(data, source);
			}
		};

		
		if(StringUtil.isEmpty(data.uuid)) {
			data.uuid = UniqueIDentity.randomString(8);
			data.resetProgress();
		}
		
		String guid = upload(data.file, data.offset, data.uuid, null, doUploadHandler);
		if(!StringUtil.isEmpty(guid)) {
			ImageUtil.renameImageFileWithGUID(data.file, guid);
		}
		return guid;
	}
	
	public final static String uploadUserAvatarImage(final ResumableUploadData data) throws Exception {
		return defaultUpload(data, 10);
	}
	
	public final static String uploadUserCoverImage(final ResumableUploadData data) throws Exception {
		return defaultUpload(data, 11);
	}
	
	public final static String upload(File file, long offset, String uuid, UploadCallBack callback, DoUploadHandler doUploadHandler) throws Exception {
		InputStream is = null;
		ByteArrayOutputStream bos = null;
		String filename = null;

		try {
			long fileLength = file.length(); // 文件总大小
			log.i("准备上传，文件总大小：" + fileLength + ", offset:" + offset);

			is = new BufferedInputStream(new FileInputStream(file));

			// 跳过已上传的字节
			if (offset > 0 && offset <= fileLength) {
				long skipBytes = is.skip(offset);
				if (skipBytes < 0 || skipBytes != offset) {
					offset = 0;
					is.skip(0);
				}
			} else {
				offset = 0;
			}

			log.i("开始位置：" + offset);
			if(callback != null) {
				callback.onProgressUpdate(offset);
			}

			bos = new ByteArrayOutputStream(LENGTH); // 一次要上传的数据缓冲区 10240
			byte[] data = new byte[2048];
			int len = -1;
			while ((len = is.read(data)) > 0) {
				bos.write(data, 0, len);

				int bufferSize = bos.size();

				// 当缓冲区的数据大于等于 10KB 时，上传数据到服务器
				if (bufferSize >= LENGTH) {
					doUploadHandler.doUpload(bos, uuid, offset, fileLength);
					offset += bufferSize;
					log.i("上传成功,  已上传大小：" + offset + " " + uuid);
					if(callback != null) {
						callback.onProgressUpdate(offset);
					}

					bos.close();
					bos = new ByteArrayOutputStream(LENGTH);
				}
			}

			// 如果缓冲区还有残余数据
			int bufferSize = bos.size();
			if (bufferSize > 0) {
				filename = doUploadHandler.doUpload(bos, uuid, offset, fileLength);
				offset += bufferSize;
				if(callback != null) {
					callback.onProgressUpdate(offset);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (bos != null) {
				bos.close();
				bos = null;
			}

			if (is != null) {
				is.close();
				is = null;
			}
		}

		return filename;
	}
	
	public static class ResumableUploadData {
		public byte[] buffer;
		public long offset;
		public long totalLength;
		public String uuid;
		public int index;
		public File file;
		
		public String toJson() {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("total", totalLength);
				jsonObject.put("uuid", uuid);
				jsonObject.put("offset", offset);
				jsonObject.put("index", index);
				if(file != null) {
					jsonObject.put("file", file.getPath());
				}
				return jsonObject.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public void resetProgress() {
			totalLength = file.length();
			offset = 0;
			index = 0;
		}

		public static ResumableUploadData parseJson(String json) {
			if(StringUtil.isEmpty(json)) {
				return null;
			}
			
			try {
				JSONObject jsonObject = new JSONObject(json);
				ResumableUploadData data = new ResumableUploadData();
				String file = jsonObject.optString("file");
				if(!StringUtil.isEmpty(file)) {
					data.file = new File(file);
				}
				data.index = jsonObject.getInt("index");
				data.offset = jsonObject.getLong("offset");
				data.totalLength = jsonObject.getLong("total");
				data.uuid = jsonObject.getString("uuid");
				return data;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	private static void fillToResumableData(ResumableUploadData data, ByteArrayOutputStream bos, String uuid, long offset, long fileLength) {
		int bufferSize = bos.size();
		final byte[] bytes = bos.toByteArray();
		int index = (int)(offset / LENGTH);
		log.i("uploading,  buffersize：" + bufferSize + ", uuid=" + uuid + ", index=" + index);
		
		data.buffer = bytes;
		data.index = index;
		data.offset = offset;
		data.totalLength = fileLength;
		data.uuid = uuid;
	}
}
