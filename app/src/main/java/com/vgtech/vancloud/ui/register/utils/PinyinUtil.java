package com.vgtech.vancloud.ui.register.utils;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;


public class PinyinUtil {

	public static String getPinyin(String string) {
		// 格式化输出
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		// 不带发音数字
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		// 输出大写字母
		format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		
		char[] charArray = string.toCharArray();
		
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			
			if(Character.isWhitespace(c)){
				continue;
			}

			if(c >= -128 && c <= 127){
				sb.append(c);
			}else {
				try {
					String str = PinyinHelper.toHanyuPinyinStringArray(c, format)[0];
					sb.append(str);
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			}
		}
		
		return sb.toString();
	}

}
