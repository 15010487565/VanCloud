package com.vgtech.common.utils;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.TextView;

import com.vgtech.common.view.DateFullDialogView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by zhangshaofang on 2015/8/28.
 */
public class DataUtils {
    public static final String DATE_TYPE_AFTER_TODAY = "startStrict";
    public static final String DATE_TYPE_AFTER_TODAY_30 = "startStrict30";
    public static final String DATE_TYPE_ALL = "full";
    public static String FORMAT_YYYYMMDD = "yyyy-MM-dd";
    public static SimpleDateFormat YYYYMMDD_FORMAT = new SimpleDateFormat(FORMAT_YYYYMMDD);

    public static void showDateSelect(Activity activity, TextView dateTv, String type, String times) {
        DateFullDialogView dateDialogview = createDateSelect(activity, dateTv, type, times);
        dateDialogview.show(dateTv);
    }

    private static DateFullDialogView createDateSelect(Activity activity, TextView dateTv, String type, String times) {
        String dateS = dateTv.getText().toString();
        if (!TextUtils.isEmpty(times)) {
            dateS = times;
        }
        Calendar calendar = null;
        if (!TextUtils.isEmpty(dateS)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                Date parse = dateFormat.parse(dateS);
                calendar = Calendar.getInstance();
                calendar.setTime(parse);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }

        DateFullDialogView dateDialogview = new DateFullDialogView(activity,
                dateTv, type, "ymdhm", calendar);//年月日时分秒 当前日期之后选择
        return dateDialogview;
    }


    public static long currentDay() {
        try {
            return YYYYMMDD_FORMAT.parse(YYYYMMDD_FORMAT.format(new Date())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

//    public static long currentWeekFirstDay() {
//        try {
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(new Date());
//            calendar.setFirstDayOfWeek(Calendar.MONDAY);
//            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
//            return YYYYMMDD_FORMAT.parse(YYYYMMDD_FORMAT.format(calendar.getTime())).getTime();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }


    public static long currentWeekFirstDay() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            return YYYYMMDD_FORMAT.parse(YYYYMMDD_FORMAT.format(calendar.getTime())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long currentWeekLastDay() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.WEEK_OF_YEAR, 1);// 一周
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            return YYYYMMDD_FORMAT.parse(YYYYMMDD_FORMAT.format(calendar.getTime())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static long currentMonthFirstDay() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
            return YYYYMMDD_FORMAT.parse(YYYYMMDD_FORMAT.format(calendar.getTime())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long currentMonthLastDay() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            return YYYYMMDD_FORMAT.parse(YYYYMMDD_FORMAT.format(calendar.getTime())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 得到某年某周的第一天
     *
     * @param year
     * @param week
     * @return
     */
    public static Date getFirstDayOfWeek(int year, int week) {
        week = week - 1;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DATE, 1);

        Calendar cal = (Calendar) calendar.clone();
        cal.add(Calendar.DATE, week * 7);

        return getFirstDayOfWeek(cal.getTime());
    }

    /**
     * 得到某年某周的最后一天
     *
     * @param year
     * @param week
     * @return
     */
    public static Date getLastDayOfWeek(int year, int week) {
        week = week - 1;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DATE, 1);
        Calendar cal = (Calendar) calendar.clone();
        cal.add(Calendar.DATE, week * 7);

        return getLastDayOfWeek(cal.getTime());
    }

    /**
     * 取得当前日期所在周的第一天
     *
     * @param date
     * @return
     */
    public static Date getFirstDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK,
                calendar.getFirstDayOfWeek()); // Sunday
        return calendar.getTime();
    }

    /**
     * 取得当前日期所在周的最后一天
     *
     * @param date
     * @return
     */
    public static Date getLastDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK,
                calendar.getFirstDayOfWeek() + 6); // Saturday
        return calendar.getTime();
    }

    /**
     * 取得当前日期所在周的前一周最后一天
     *
     * @param date
     * @return
     */
    public static Date getLastDayOfLastWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return getLastDayOfWeek(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.WEEK_OF_YEAR) - 1);
    }

    /**
     * 返回指定日期的月的第一天
     *
     * @return
     */
    public static String getFirstDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), 1);
        return YYYYMMDD_FORMAT.format(calendar.getTime());
    }

    /**
     * 返回指定年月的月的第一天
     *
     * @param year
     * @param month
     * @return
     */
    public static Date getFirstDayOfMonth(Integer year, Integer month) {
        Calendar calendar = Calendar.getInstance();
        if (year == null) {
            year = calendar.get(Calendar.YEAR);
        }
        if (month == null) {
            month = calendar.get(Calendar.MONTH);
        }
        calendar.set(year, month - 1, 1);
        return calendar.getTime();
    }

    /**
     * 返回指定日期的月的最后一天
     *
     * @return
     */
    public static String getLastDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), 1);
        calendar.roll(Calendar.DATE, -1);
        return YYYYMMDD_FORMAT.format(calendar.getTime());
    }

    /**
     * 返回指定年月的月的最后一天
     *
     * @param year
     * @param month
     * @return
     */
    public static Date getLastDayOfMonth(Integer year, Integer month) {
        Calendar calendar = Calendar.getInstance();
        if (year == null) {
            year = calendar.get(Calendar.YEAR);
        }
        if (month == null) {
            month = calendar.get(Calendar.MONTH);
        }
        calendar.set(year, month - 1, 1);
        calendar.roll(Calendar.DATE, -1);
        return calendar.getTime();
    }

    /**
     * 返回指定日期的上个月的最后一天
     *
     * @return
     */
    public static Date getLastDayOfLastMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) - 1, 1);
        calendar.roll(Calendar.DATE, -1);
        return calendar.getTime();
    }

    /**
     * 返回指定日期的季的第一天
     *
     * @return
     */
    public static Date getFirstDayOfQuarter(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return getFirstDayOfQuarter(calendar.get(Calendar.YEAR),
                getQuarterOfYear(date));
    }

    /**
     * 返回指定年季的季的第一天
     *
     * @param year
     * @param quarter
     * @return
     */
    public static Date getFirstDayOfQuarter(Integer year, Integer quarter) {
        Calendar calendar = Calendar.getInstance();
        int month = 0;
        if (quarter == 1) {
            month = 1 - 1;
        } else if (quarter == 2) {
            month = 4 - 1;
        } else if (quarter == 3) {
            month = 7 - 1;
        } else if (quarter == 4) {
            month = 10 - 1;
        } else {
            month = calendar.get(Calendar.MONTH);
        }
        return getFirstDayOfMonth(year, month);
    }

    /**
     * 返回指定日期的季的最后一天
     *
     * @return
     */
    public static Date getLastDayOfQuarter(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return getLastDayOfQuarter(calendar.get(Calendar.YEAR),
                getQuarterOfYear(date));
    }

    /**
     * 返回指定年季的季的最后一天
     *
     * @param year
     * @param quarter
     * @return
     */
    public static Date getLastDayOfQuarter(Integer year, Integer quarter) {
        Calendar calendar = Calendar.getInstance();
        int month = 0;
        if (quarter == 1) {
            month = 3 - 1;
        } else if (quarter == 2) {
            month = 6 - 1;
        } else if (quarter == 3) {
            month = 9 - 1;
        } else if (quarter == 4) {
            month = 12 - 1;
        } else {
            month = calendar.get(Calendar.MONTH);
        }
        return getLastDayOfMonth(year, month);
    }

    /**
     * 返回指定日期的上一季的最后一天
     *
     * @return
     */
    public static Date getLastDayOfLastQuarter(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return getLastDayOfLastQuarter(calendar.get(Calendar.YEAR),
                getQuarterOfYear(date));
    }

    /**
     * 返回指定年季的上一季的最后一天
     *
     * @param year
     * @param quarter
     * @return
     */
    public static Date getLastDayOfLastQuarter(Integer year, Integer quarter) {
        Calendar calendar = Calendar.getInstance();
        int month = 0;
        if (quarter == 1) {
            month = 12 - 1;
        } else if (quarter == 2) {
            month = 3 - 1;
        } else if (quarter == 3) {
            month = 6 - 1;
        } else if (quarter == 4) {
            month = 9 - 1;
        } else {
            month = calendar.get(Calendar.MONTH);
        }
        return getLastDayOfMonth(year, month);
    }

    /**
     * 返回指定日期的季度
     *
     * @param date
     * @return
     */
    public static int getQuarterOfYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) / 3 + 1;
    }


    public static String getFormatYyyymmdd(long date) {
        return YYYYMMDD_FORMAT.format(new Date(date));
    }


    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DATE_TIME_FORMAT_TODAY = "今天 HH:mm";
    public static final String DATE_TIME_FORMAT_YESTERDAY = "昨天 HH:mm";
    public static final String DATE_TIME_FORMAT_TDBY = "前天 HH:mm";

    public static String dateFormat(long times) {
        long current = System.currentTimeMillis();
        Date date = new Date(times);
        Date curr = new Date(current);
        String dateformat = DATE_TIME_FORMAT;
        int y = curr.getYear() - date.getYear();
        int m = curr.getMonth() - date.getMonth();
        int t = curr.getDate() - date.getDate();
        if (y == 0 && m == 0) {
            if (t == 0) {
                dateformat = DATE_TIME_FORMAT_TODAY;
            } else if (t == 1) {
                dateformat = DATE_TIME_FORMAT_YESTERDAY;
            } else if (t == 2) {
                dateformat = DATE_TIME_FORMAT_TDBY;
            }
        }
        SimpleDateFormat dateformat1 = new SimpleDateFormat(dateformat);
        String dateStr = dateformat1.format(date);
        return dateStr;
    }

    public static String dateFormat(String times) {

        if (TextUtils.isEmpty(times)) {
            return "";
        } else {
            return dateFormat(Long.valueOf(times));
        }
    }

    public static String dateFormat(long times, String dateformat) {
        if (times <= 0)
            return "";
        SimpleDateFormat dateformat1 = new SimpleDateFormat(dateformat);
        String dateStr = dateformat1.format(times);
        return dateStr;
    }

}
