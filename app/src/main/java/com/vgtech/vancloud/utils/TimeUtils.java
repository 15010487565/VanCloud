package com.vgtech.vancloud.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Data:  2017/8/4
 * Auther: 陈占洋
 * Description:
 */

public class TimeUtils {

    public static String newGetTimePassedDesc(Long timeStamp) {

        SimpleDateFormat thisYearFormat = new SimpleDateFormat("MM/dd HH:mm");
        SimpleDateFormat oldYearFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String result = "";
        if (timeStamp == null || timeStamp == 0) {
            return result;
        }
        long timeStampBefore = timeStamp;
        long timeStampCurrent = System.currentTimeMillis();
        long secondPassed = timeStampCurrent - timeStampBefore;
        if (secondPassed <= 0) {
            return result;
        }
        Date dateBefore = new Date(timeStampBefore);
        Date dateCurrent = new Date(timeStampCurrent);
        if (dateCurrent.getYear() > dateBefore.getYear()) {
            result = oldYearFormat.format(dateBefore);
        } else if (dateCurrent.getMonth() > dateBefore.getMonth()) {
            result = thisYearFormat.format(dateBefore);
        } else if (dateCurrent.getDate() > dateBefore.getDate()) {
            result = thisYearFormat.format(dateBefore);
        } else if (dateCurrent.getHours() > dateBefore.getHours()) {
            result = "" + (dateCurrent.getHours() - dateBefore.getHours()) + "小时前";
        } else if (dateCurrent.getMinutes() > dateBefore.getMinutes()) {
            result = "" + (dateCurrent.getMinutes() - dateBefore.getMinutes()) + "分钟前";
        } else if (dateCurrent.getSeconds() > dateBefore.getSeconds()) {
            result = "" + (dateCurrent.getSeconds() - dateBefore.getSeconds()) + "秒前";
        } else {
            result = "刚刚";
        }
        return result;
    }

}
