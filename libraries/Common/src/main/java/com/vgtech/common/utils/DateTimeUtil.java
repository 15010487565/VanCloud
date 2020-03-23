package com.vgtech.common.utils;

import android.content.Context;

import com.vgtech.common.R;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTimeUtil {

    public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOURE = 60 * MINUTE;
    public static final long DAY = 24 * HOURE;

    public static boolean isFirstTimeThenSecondTime(String FirstTime,
                                                    String SecondTime) {
        Long l1 = DateTimeUtil.stringToLong_YMd(FirstTime);
        Long l2 = DateTimeUtil.stringToLong_YMd(SecondTime);
        if (l1 > l2) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isFirstThenSecond(String FirstTime,
                                            String SecondTime) {
        Long l1 = DateTimeUtil.stringToLong_YMdhm(FirstTime);
        Long l2 = DateTimeUtil.stringToLong_YMdhm(SecondTime);
        if (l1 > l2) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean unbefore30Day(String firstTime, String secondTime) {
        Long l1 = DateTimeUtil.stringToLong_YMd(firstTime);
        Long l2 = DateTimeUtil.stringToLong_YMd(secondTime);
        if ((l2 - l1) > (30 * 24 * 60 * 60 * 1000l)) {
            return true;
        }
        return false;
    }

    public static boolean unbeforefull(String firstTime, String secondTime, int count) {
        Long l1 = DateTimeUtil.stringToLong_YMdhm(firstTime);
        Long l2 = DateTimeUtil.stringToLong_YMdhm(secondTime);
        if ((l2 - l1) > (count * 24 * 60 * 60)) {
            return true;
        }
        return false;
    }

    public static boolean compareFirstSecondTime(String currentTime,
                                                 String startTime) {
        Long l1 = DateTimeUtil.stringToLong_YMd(currentTime);
        Long l2 = DateTimeUtil.stringToLong_YMd(startTime);
        if (l1 > l2 || l1.equals(l2)) {
            return false;
        } else {
            return true;
        }
    }

    public static String longToString_YMdHms(Long longTime) {
        Date date = new Date(longTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        return time;
    }

    public static String longToString_YMdHm(Long longTime) {
        Date date = new Date(longTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String time = sdf.format(date);
        return time;
    }

    public static String longToString_Hm(Long longTime) {
        Date date = new Date(longTime);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String time = sdf.format(date);
        return time;
    }

    public static int longToDay(Long longTime) {
        Date date = new Date(longTime);
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        String day = sdf.format(date);
        return Integer.parseInt(day);
    }

    public static String longToString_YMd(Long longTime) {
        Date date = new Date(longTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String time = sdf.format(date);
        return time;
    }

    public static String formatToMdhm(long longTime) {
        Date date = new Date(longTime);
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        String time = sdf.format(date);
        return time;
    }

    public static Long stringToLong_YMd(String curDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(curDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static Long stringToLong_hm(String curDate) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        Date date = null;
        try {
            date = format.parse(curDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static Long stringToLong_YMdhm(String curDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = null;
        try {
            date = format.parse(curDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static String getCurrentString_YMDHms() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return format.format(date);
    }

    public static String getCurrentString_YMDHm() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date();
        return format.format(date);
    }

    public static String getCurrentString_YM() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Date date = new Date();
        return format.format(date);
    }

    public static String getCurrentString_YMd() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return format.format(date);
    }

    public static String getCurrentString(String dateFormat) {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        Date date = new Date();
        return format.format(date);
    }

    //format 任何字符串格式的日期 返回字符串格式日期
    public static String formatString_YMd(String curDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(curDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return format.format(date.getTime());
    }

    public static String getSpecifiedDayAfter(String specifiedDay) {
        Calendar c = Calendar.getInstance();
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(specifiedDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.setTime(date);
        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day + 1);

        String dayAfter = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
        return dayAfter;
    }

    public static String afterNDay(int n, String selectDate) {
        Calendar c = Calendar.getInstance();
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(selectDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.setTime(date);
        c.add(Calendar.DATE, n);
        Date d2 = c.getTime();
        String dayAfter = new SimpleDateFormat("yyyy-MM-dd").format(d2);
        return dayAfter;
    }

    public static String afterNMonth(int n, String selectDate) {
        Calendar c = Calendar.getInstance();
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(selectDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.setTime(date);
        c.add(Calendar.MONTH, n);
        Date d2 = c.getTime();
        String dayAfter = new SimpleDateFormat("yyyy-MM-dd").format(d2);
        return dayAfter;
    }

    public static String afterNYear(int n, String selectDate) {
//	    	Calendar c = Calendar.getInstance();  
//	        Date date=null;  
//	        try {  
//	            date = new SimpleDateFormat("yyyy-MM-dd").parse(selectDate);  
//	        } catch (ParseException e) {  
//	            e.printStackTrace();  
//	        }  
//	        c.setTime(date);  
//	        c.add(Calendar.YEAR,n);   
//	    	Date d2=c.getTime();   
//	        String dayAfter=new SimpleDateFormat("yyyy-MM-dd").format(d2);  
//	        return dayAfter; 
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(selectDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long afterTime = (date.getTime() / 1000) + n * 60 * 60 * 24 * 365 - 60 * 60 * 24;
        date.setTime(afterTime * 1000);
        String afterDate = formatter.format(date);
        return afterDate;
    }


    /**
     * @return 该毫秒数转换为 * days  天数
     * @author
     */
    public static int formatDuring(long mss) {
        long days = mss / (1000 * 60 * 60 * 24);
//	        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);  
//	        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);  
//	        long seconds = (mss % (1000 * 60)) / 1000;  
//	        return days + " days " + hours + " hours " + minutes + " minutes "  
//	                + seconds + " seconds ";  
        int day = Integer.parseInt(days + "");
        return day;
    }

    public static double formatDuringH(long mss) {
        double hours = div(mss, 1000 * 60 * 60, 1);
        return hours;
    }

    public static double formatDuringH2(long mss) {
        double hours = div(mss, 1000 * 60 * 60, 2);
        return hours;
    }

    /**
     * 除法运算
     *
     * @param d1  被除数
     * @param d2  除数
     * @param len 保留几位有效小数
     * @return
     */
    public static double div(double d1, double d2, int len) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        // BigDecimal.ROUND_HALF_UP 舍入模式
        return b1.divide(b2, len, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static String getHHmm(Long time) {
        StringBuilder sb = new StringBuilder();
        Long m = time / (1000 * 60);
        Long f = m % 60;
        Long h = m / 60;
        if (h < 10) {
            sb.append("0" + String.valueOf(h));
        } else {
            sb.append(String.valueOf(h));
        }
        sb.append(":");
        if (f < 10) {
            sb.append("0" + String.valueOf(f));
        } else {
            sb.append(String.valueOf(f));
        }
        return sb.toString();
    }


    public static String calendarToString_YMd(Calendar calendar) {
        String newdate = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        Date date = calendar.getTime();
        try {
            newdate = format.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newdate;
    }


    public static String nextMonth(long currentTime, int count) {
        Date date = new Date();
        date.setTime(currentTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, count);
        Date date2 = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date2);
    }

    public static String currentYYMMDD(long currentTime) {
        Date date = new Date();
        date.setTime(currentTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }

    public static String calendarToString_YMdHm(Calendar calendar) {
        String newdate = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        Date date = calendar.getTime();
        try {
            newdate = format.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newdate;
    }

    public static boolean isExceedDay(String FirstTime, String SecondTime) {
        Long l1 = stringToLong_YMdhm(FirstTime) + 86400000;
        Long l2 = stringToLong_YMdhm(SecondTime);
        if (l1 > l2) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取指定日期是星期几
     * 参数为null时表示获取当前日期是星期几
     *
     * @param dateStr
     * @return
     */
    public static String getWeekOfDate(Context context, String dateStr) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        String[] weeks = context.getResources().getStringArray(R.array.week);
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
        int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weeks[w];
    }

    public static boolean isAm(String time) {
        int m = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            Date date = sdf.parse(time);
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(date);
            m = gregorianCalendar.get(GregorianCalendar.AM_PM);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return m == 0;
    }
}
