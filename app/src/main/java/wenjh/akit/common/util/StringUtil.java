package wenjh.akit.common.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 字符串处理类
 * 
 */
public class StringUtil {
	/**
	 * 判断字符串是否为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(CharSequence str) {
		if (str == null || str.length() == 0)
			return true;
		return false;
	}

	/**
	 * 将字符串转换成字符串组数,按照指定的标记进行转换
	 */
	public static String[] str2Arr(String value, String tag) {
		if (!isEmpty(value)) {
			return value.split(tag);
		}
		return null;
	}

	/**
	 * 将一个字符串数组组合成一个以指定分割符分割的字符串
	 */
	public static String join(Object[] array, String separator) {
		if (array == null) {
			return null;
		}
		return join(array, separator, "", 0, array.length);
	}

	/**
	 * 将一个字符串数组组合成一个以指定分割符分割的字符串
	 */
	public static String join(Object[] array, String ch, String separator) {
		if (array == null) {
			return null;
		}
		return join(array, separator, ch, 0, array.length);
	}

	/**
	 * 将一个字符串数组的某一部分组合成一个以指定分割符分割的字符串
	 */
	public static String join(Object[] array, String separator, String ch, int startIndex, int endIndex) {
		if (array == null) {
			return null;
		}
		if (separator == null) {
			separator = "";
		}

		// 开始位置大于结束位置
		int bufSize = (endIndex - startIndex);
		if (bufSize <= 0) {
			return "";
		}

		bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length()) + separator.length());

		StringBuffer buf = new StringBuffer(bufSize);

		for (int i = startIndex; i < endIndex; i++) {
			if (i > startIndex) {
				buf.append(separator);
			}
			if (array[i] != null) {
				buf.append(ch + array[i] + ch);
			}
		}
		return buf.toString();
	}

	/**
	 * 将一个集合组合成以指定分割符分割的字符串
	 */
	public static String join(Collection<?> collection, String separator) {
		if (collection == null) {
			return null;
		}
		return join(collection.iterator(), separator);
	}

	/**
	 * 根据迭代器，迭代的元素将组合成以指定分割符分割的字符串
	 */
	public static String join(Iterator<?> iterator, String separator) {

		// 空的迭代器，返回 null
		if (iterator == null) {
			return null;
		}
		// 空元素，返回 null
		if (!iterator.hasNext()) {
			return "";
		}

		Object first = iterator.next();
		// 只有一个元素
		if (!iterator.hasNext()) {
			if (first != null) {
				return first.toString();
			} else {
				return "";
			}
		}

		StringBuffer buf = new StringBuffer(256);
		if (first != null) {
			buf.append(first);
		}

		while (iterator.hasNext()) {
			if (separator != null) {
				buf.append(separator);
			}
			Object obj = iterator.next();
			if (obj != null) {
				buf.append(obj);
			}
		}

		return buf.toString();
	}

	/**
	 * 将集合元素转换成字符串："'qq','aa','cc'"
	 */
	public static String Conll2StringWithSingleGuotes(@SuppressWarnings("rawtypes") Collection collection, String separator) {
		if (collection == null) {
			return null;
		}
		Iterator<?> iterator = collection.iterator();

		// 空的迭代器，返回 null
		if (iterator == null) {
			return null;
		}
		// 空元素，返回 null
		if (!iterator.hasNext()) {
			return "";
		}

		Object first = iterator.next();
		// 只有一个元素
		if (!iterator.hasNext()) {
			if (first != null) {
				return "'" + first.toString() + "'";
			} else {
				return "";
			}
		}

		StringBuffer buf = new StringBuffer(256);
		if (first != null) {
			buf.append("'" + first + "'");
		}

		while (iterator.hasNext()) {
			if (separator != null) {
				buf.append(separator);
			}
			Object obj = iterator.next();
			if (obj != null) {
				buf.append("'" + obj + "'");
			}
		}

		return buf.toString();
	}

	/**
	 * 一个字符串是否包含另一字符串
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean contains(String str1, String str2) {
		return str1.contains(str2) || str2.contains(str1);
	}
	
	/**
	 * check if the string match the certain length
	 * @return
	 */
	public static boolean matchLength(String word,int length){
		if(word.length()<=length){
			return true;
		}else{
			return false;
		}
	}
	/**
	 * check if the words in array list match certain length
	 * @param stringList
	 * @param length
	 * @return
	 */
	public static boolean matchLengthStringArray(List<String> stringList,int length){
		for(String word:stringList){
			if(matchLength(word, length)){
				continue;
			}else{
				return false;
			}
		}
		return true;
	}

	/**
	 * 一个字符串是以另一字符串开头
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean startsWith(String str1, String str2) {
		return str1.startsWith(str2) || str2.startsWith(str1);
	}

	private static final char[] QUOTE_ENCODE = "&quot;".toCharArray();
	private static final char[] APOS_ENCODE = "&apos;".toCharArray();
	private static final char[] AMP_ENCODE = "&amp;".toCharArray();
	private static final char[] LT_ENCODE = "&lt;".toCharArray();
	private static final char[] GT_ENCODE = "&gt;".toCharArray();

	public static String escapeForXML(String string) {
		if (string == null) {
			return null;
		}
		char ch;
		int i = 0;
		int last = 0;
		char[] input = string.toCharArray();
		int len = input.length;
		StringBuilder out = new StringBuilder((int) (len * 1.3));
		for (; i < len; i++) {
			ch = input[i];
			if (ch > '>') {
			} else if (ch == '<') {
				if (i > last) {
					out.append(input, last, i - last);
				}
				last = i + 1;
				out.append(LT_ENCODE);
			} else if (ch == '>') {
				if (i > last) {
					out.append(input, last, i - last);
				}
				last = i + 1;
				out.append(GT_ENCODE);
			}

			else if (ch == '&') {
				if (i > last) {
					out.append(input, last, i - last);
				}
				// Do nothing if the string is of the form &#235; (unicode
				// value)
				if (!(len > i + 5 && input[i + 1] == '#' && Character.isDigit(input[i + 2]) && Character.isDigit(input[i + 3])
						&& Character.isDigit(input[i + 4]) && input[i + 5] == ';')) {
					last = i + 1;
					out.append(AMP_ENCODE);
				}
			} else if (ch == '"') {
				if (i > last) {
					out.append(input, last, i - last);
				}
				last = i + 1;
				out.append(QUOTE_ENCODE);
			} else if (ch == '\'') {
				if (i > last) {
					out.append(input, last, i - last);
				}
				last = i + 1;
				out.append(APOS_ENCODE);
			}
		}
		if (last == 0) {
			return string;
		}
		if (i > last) {
			out.append(input, last, i - last);
		}
		return out.toString();
	}
}
