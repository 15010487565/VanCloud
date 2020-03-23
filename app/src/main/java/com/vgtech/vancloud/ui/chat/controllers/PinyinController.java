package com.vgtech.vancloud.ui.chat.controllers;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import roboguice.util.Ln;
import roboguice.util.Strings;

/**
 * @author xuanqiang
 * @date 13-7-26
 */
@SuppressWarnings("ALL")
public class PinyinController{
  /**
   * 返回中文拼音
   */
  public String getPinyin(final String chinese){
    if(Strings.isEmpty(chinese)){
      return "";
    }
    StringBuilder pinyin = new StringBuilder();
    char[] chars = chinese.toCharArray();
    HanyuPinyinOutputFormat pinyinOutputFormat = new HanyuPinyinOutputFormat();
    pinyinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);// 小写
    pinyinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);// 不标声调
    pinyinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);// u:的声母替换为v
    try{
      for(char c : chars){
        if(Character.toString(c).matches("[\\u4E00-\\u9FA5]+")){
          String[] tempArr = PinyinHelper.toHanyuPinyinStringArray(c,pinyinOutputFormat);
          pinyin.append(tempArr[0]);
        }else{
          pinyin.append(Character.toString(c));
        }
      }
    }catch(BadHanyuPinyinOutputFormatCombination e){
      Ln.e(e);
    }
    return pinyin.toString();
  }

}
