package wenjh.akit.common.view;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import com.wenjh.akit.R;

import wenjh.akit.common.util.StringUtil;
import wenjh.akit.common.util.ContextUtil;

public class MomoEmotionUtil {
	private MomoEmotionUtil() {
	}

	/**
	 * 表情的 “占位符” -- "图片名" 的对应表。
	 */
	private final static Map<String, Integer> sEmotionsMap;

	/**
	 * 表情“占位符”集合
	 */
	private final static String[] sEmotionsStringArray = new String[] {
		"[微笑]",        //1
		"[哈哈]",        //2
		"[嘻嘻]",        //3
		"[偷笑]",        //4
		"[得意]",        //5
		"[飞吻]",        //6
		"[挖鼻孔]",       //7
		"[摊手]",        //8
		"[无聊]",        //9
		"[惊讶]",        //10
		"[疑问]",        //11
		"[委屈]",        //12
		"[害羞]",        //13
		"[汗]",         //14
		"[斜眼]",        //15
		"[尴尬]",        //16
		"[鼓掌]",        //17
		"[咆哮]",        //18
		"[不开心]",       //19
		"[泪]",         //20
		"[思考]",        //21
		"[仰慕]",        //22
		"[酷]",         //23
		"[流鼻血]",       //24
		"[吐舌头]",       //25
		"[愤怒]",        //26
		"[No]",        //27
		"[鄙视]",        //28
		"[生病]",        //29
		"[鬼脸]",        //30
		"[悲剧]",        //31
		"[安慰]",        //32
		"[抓狂]",        //33
		"[囧]",         //34
		"[花心]",        //35
		"[拜拜]",        //36
		"[砸死你]",       //37
		"[呕吐]",        //38
		"[闭嘴]",        //39
		"[晕]",         //40
		"[瞌睡]",        //41
		"[衰]",         //42
		"[饿]",         //43
		"[奋斗]",        //44
		"[财迷]",        //45
		"[中指]",        //46
		"[GOOD]",      //47
		"[BAD]",       //48
		"[握手]",        //49
		"[拉勾]",        //50
		"[OK]",        //51
		"[勾引]",        //52
		"[剪刀手]",       //53
		"[抱抱]",        //54
		"[恶魔]",        //55
		"[猪头]",        //56
		"[喵]",         //57
		"[咖啡]",        //58
		"[蛋糕]",        //59
		"[碰杯]",        //60
		"[礼物]",        //61
		"[路过]",        //62
		"[大便]",        //63
		"[心]",         //64
		"[心碎]",        //65
		"[花]",         //66
		"[晚安]",        //67
		"[肥皂]",        //68
	};

	/**
	 * 表情“图片”集合
	 */
	private final static int[] sEmotionIconArray = new int[] { 
		R.drawable.zem1,        //1
		R.drawable.zem2,        //2
		R.drawable.zem3,        //3
		R.drawable.zem4,        //4
		R.drawable.zem5,        //5
		R.drawable.zem6,        //6
		R.drawable.zem7,       //7
		R.drawable.zem8,        //8
		R.drawable.zem9,        //9
		R.drawable.zem10,        //10
		R.drawable.zem11,        //11
		R.drawable.zem12,        //12
		R.drawable.zem13,        //13
		R.drawable.zem14,         //14
		R.drawable.zem15,        //15
		R.drawable.zem16,        //16
		R.drawable.zem17,        //17
		R.drawable.zem18,        //18
		R.drawable.zem19,       //19
		R.drawable.zem20,         //20
		R.drawable.zem21,        //21
		R.drawable.zem22,        //22
		R.drawable.zem23,         //23
		R.drawable.zem24,       //24
		R.drawable.zem25,       //25
		R.drawable.zem26,        //26
		R.drawable.zem27,        //27
		R.drawable.zem28,        //28
		R.drawable.zem29,        //29
		R.drawable.zem30,        //30
		R.drawable.zem31,        //31
		R.drawable.zem32,        //32
		R.drawable.zem33,        //33
		R.drawable.zem34,         //34
		R.drawable.zem35,        //35
		R.drawable.zem36,        //36
		R.drawable.zem37,       //37
		R.drawable.zem38,        //38
		R.drawable.zem39,        //39
		R.drawable.zem40,         //40
		R.drawable.zem41,        //41
		R.drawable.zem42,         //42
		R.drawable.zem43,         //43
		R.drawable.zem44,        //44
		R.drawable.zem45,        //45
		R.drawable.zem46,        //46
		R.drawable.zem47,      //47
		R.drawable.zem48,       //48
		R.drawable.zem49,        //49
		R.drawable.zem50,        //50
		R.drawable.zem51,        //51
		R.drawable.zem52,        //52
		R.drawable.zem53,       //53
		R.drawable.zem54,        //54
		R.drawable.zem55,        //55
		R.drawable.zem56,        //56
		R.drawable.zem57,         //57
		R.drawable.zem58,        //58
		R.drawable.zem59,        //59
		R.drawable.zem60,        //60
		R.drawable.zem61,        //61
		R.drawable.zem62,        //62
		R.drawable.zem63,        //63
		R.drawable.zem64,         //64
		R.drawable.zem65,        //65
		R.drawable.zem66,         //66
		R.drawable.zem67,        //67
		R.drawable.zem68,        //68
	};

	/**
	 * 匹配表情的正则表达式
	 */
	private final static String sEmotionsRegex;

	static {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < sEmotionsStringArray.length; i++) {
			map.put(sEmotionsStringArray[i], sEmotionIconArray[i]);
		}
		sEmotionsMap = map;

		StringBuilder sb = new StringBuilder("(");
		sb.append(StringUtil.join(sEmotionsStringArray, "|").replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]"));
		sb.append(")");
		sEmotionsRegex = sb.toString();
	}

	/**
	 * 根据表情字符获取对应的表情图片名
	 * 
	 * @param chars
	 * @return
	 */
	public static final int getEmoteIcon(String chars) {
		if(sEmotionsMap.containsKey(chars)) {
			return sEmotionsMap.get(chars);
		} else {
			return 0;
		}
	}

	/**
	 * 获取表情的字符匹配集合
	 * 
	 * @return
	 */
	public static final String[] getEmoteChars() {
		return sEmotionsStringArray;
	}

	/**
	 * 获取全部的表情图片名
	 * 
	 * @return
	 */
	public static final int[] getEmoteIcons() {
		return sEmotionIconArray;
	}

	/**
	 * 获取匹配表情字符的正则表达式
	 * 
	 * @return
	 */
	public static final String getEmoteRegex() {
		return sEmotionsRegex;
	}
	
	
	public static final CharSequence getEmoteSpan(CharSequence text, int size) {
		if (text == null) {
			return "";
		}
		SpannableStringBuilder builder;
		if (text instanceof SpannableStringBuilder) {
			builder = (SpannableStringBuilder) text;
		} else {
			builder = new SpannableStringBuilder(text);
		}
		Pattern mPattern = Pattern.compile(sEmotionsRegex);
		Matcher matcher = mPattern.matcher(text);

		Context context = ContextUtil.getContext();
		boolean found = false;
		while (matcher.find()) {
			found = true;
			int resId = getEmoteIcon(matcher.group());
			if (resId > 0) {
				builder.setSpan(new EmojiconDrawableSpan(context, resId, size), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}

		return found ? builder : text;
	}
}
