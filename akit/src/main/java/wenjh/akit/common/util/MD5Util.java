package wenjh.akit.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

	public static String getMd5HexString(String s) {
		byte messageDigest[] = getMd5Bytes(s.getBytes());

		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < messageDigest.length; i++) {
			String h = Integer.toHexString(0xFF & messageDigest[i]);
			while (h.length() < 2)
				h = "0" + h;
			hexString.append(h);
		}
		return hexString.toString();
	}
	
	public static byte[] getMd5Bytes(byte[] sourceBytes) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(sourceBytes);
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
}
