package com.vgtech.common.utils;

import org.apache.commons.lang.time.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Duke on 2015/9/16.
 */
public class CalendarUtils {

    public static String LeftPad_Tow_Zero(int str) {
        java.text.DecimalFormat format = new java.text.DecimalFormat("00");
        return format.format(str);
    }

    public static Calendar getSelectCalendar(int mPageNumber) {
        Calendar calendar = Calendar.getInstance();
        if (mPageNumber > 500) {
            for (int i = 0; i < mPageNumber - 500; i++) {
                calendar = setNextViewItem(calendar);
            }
        } else if (mPageNumber < 500) {
            for (int i = 0; i < 500 - mPageNumber; i++) {
                calendar = setPrevViewItem(calendar);
            }
        }
        return calendar;
    }

    // 上一个月
    private static Calendar setPrevViewItem(Calendar calendar) {
        int iMonthViewCurrentMonth = calendar.get(Calendar.MONTH);
        int iMonthViewCurrentYear = calendar.get(Calendar.YEAR);
        iMonthViewCurrentMonth--;// 当前选择月--

        // 如果当前月为负数的话显示上一年
        if (iMonthViewCurrentMonth == -1) {
            iMonthViewCurrentMonth = 11;
            iMonthViewCurrentYear--;
        }
        calendar.set(Calendar.DAY_OF_MONTH, 1); // 设置日为当月1日
        calendar.set(Calendar.MONTH, iMonthViewCurrentMonth); // 设置月
        calendar.set(Calendar.YEAR, iMonthViewCurrentYear); // 设置年
        return calendar;
    }

    //下一个月
    private static Calendar setNextViewItem(Calendar calendar) {
        int iMonthViewCurrentMonth = calendar.get(Calendar.MONTH);
        int iMonthViewCurrentYear = calendar.get(Calendar.YEAR);
        iMonthViewCurrentMonth++;
        if (iMonthViewCurrentMonth == 12) {
            iMonthViewCurrentMonth = 0;
            iMonthViewCurrentYear++;
        }
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, iMonthViewCurrentMonth);
        calendar.set(Calendar.YEAR, iMonthViewCurrentYear);
        return calendar;
    }


    public static Date getSelectWeek(int mPageNumber) {
        Date date = new Date();
        if (mPageNumber > 500) {
            for (int i = 0; i < mPageNumber - 500; i++) {
                date = setNextDate(date);
            }
        } else if (mPageNumber < 500) {
            for (int i = 0; i < 500 - mPageNumber; i++) {
                date = setlastDate(date);
            }
        }
        return date;
    }


    // 上一个周
    private static Date setlastDate(Date date) {
        return addWeeks(date, -1);
    }

    //下一个周
    private static Date setNextDate(Date date) {
        return addWeeks(date, 1);
    }


    public static Date addWeeks(Date date, int amount) {
        return DateUtils.addWeeks(date, amount);
    }

    public static Date getNowWeekMonday(Date date, int type) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
//        cal.setFirstDayOfWeek(Calendar.SUNDAY);
//        cal.add(Calendar.DAY_OF_MONTH, -1); //解决周日会出现 并到下一周的情况
        cal.set(Calendar.DAY_OF_WEEK, type);
        return cal.getTime();
    }

    public static String[] getWeek(Date date, int num) {

        List<String> list = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        for (int i = 0; i < num; i++) {
            Date s;
            if (isSunDay(date)) {
                s = addWeeks(date, -i - 1);
            } else {
                s = addWeeks(date, -i);
            }
            Calendar calSunday = Calendar.getInstance();
            calSunday.setTime(addWeeks(s, 1));
            calSunday.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            String sunday = format.format(calSunday.getTime());
            Calendar calMonday = Calendar.getInstance();
            calMonday.setTime(s);
            calMonday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            String monday = format.format(calMonday.getTime());
            String time = monday + "--" + sunday;
            list.add(time);
        }
        return list.toArray(new String[list.size()]);
    }


    public static Date addMonths(Date date, int month) {
        return DateUtils.addMonths(date, month);
    }


    public static String[] getMonths(Date date, int num) {

        List<String> list = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM");
        for (int i = 0; i < num; i++) {
            list.add(format.format(addMonths(date, -i)));
        }
        String[] s = list.toArray(new String[list.size()]);
        return s;

    }

    /**
     * 获取周末
     *
     * @param date
     * @return
     */
    public static Date getWeekSunday(Date date) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.WEEK_OF_YEAR, 1);// 一周
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        String monday = format.format(cal.getTime());
        return cal.getTime();
    }

    /**
     * 获取周一
     *
     * @param date
     * @return
     */
    public static Date getWeekMonday(Date date) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, -1); //解决周日会出现 并到下一周的情况
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String monday = format.format(cal.getTime());
        return cal.getTime();
    }

    /**
     * 获取周一
     *
     * @param date
     * @return
     */
    public static long getWeekMonday(Date date, String format) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);

            Date s;
            if (isSunDay(date)) {
                s = addWeeks(date, -1);
            } else {
                s = addWeeks(date, 0);
            }
            Calendar calMonday = Calendar.getInstance();
            calMonday.setTime(s);
            calMonday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            return simpleDateFormat.parse(simpleDateFormat.format(calMonday.getTime())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * 获取周末
     *
     * @param date
     * @return
     */
    public static long getWeekSunday(Date date, String format) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            Date s;
            if (isSunDay(date)) {
                s = addWeeks(date, -1);
            } else {
                s = addWeeks(date, 0);
            }
            Calendar calSunday = Calendar.getInstance();
            calSunday.setTime(addWeeks(s, 1));
            calSunday.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            return simpleDateFormat.parse(simpleDateFormat.format(calSunday.getTime())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取月第一天
     *
     * @param date
     * @return
     */
    public static Date getMonthFirstDay(Date date) {
        return DateUtils.setDays(date, 1);
    }

    /**
     * 获取月最后一天
     *
     * @param date
     * @return
     */
    public static Date getMonthLastDay(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        return ca.getTime();
    }

    /**
     * 获取月第一天
     *
     * @return
     */
    public static long getMonthFirstDay(String times, String format) {
        DateFormat sdf = new SimpleDateFormat(format);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = null;
        try {
            date = sdf.parse(times);
            return simpleDateFormat.parse(simpleDateFormat.format(getMonthFirstDay(date))).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取月最后一天
     *
     * @return
     */
    public static long getMonthLastDay(String times, String format) {
        DateFormat sdf = new SimpleDateFormat(format);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = null;
        try {
            date = sdf.parse(times);
            return simpleDateFormat.parse(simpleDateFormat.format(getMonthLastDay(date))).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * 判断是否是周末
     *
     * @return
     */
    private static boolean isSunDay(Date date) {

        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        int week = ca.get(Calendar.DAY_OF_WEEK) - 1;
        if (week == 0) {//0代表周日，6代表周六
            return true;
        }
        return false;
    }
}
