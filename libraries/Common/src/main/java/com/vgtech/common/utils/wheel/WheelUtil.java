package com.vgtech.common.utils.wheel;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.vgtech.common.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class WheelUtil {
    private WheelView wv_yeas;
    private WheelView wv_months;
    private WheelView wv_day;
    private WheelView wv_count;
    private WheelView wv_hours;
    private WheelView wv_mins;
    private int MaxCount;
    private View view_date;

    private Calendar mCalendar;

    private ArrayList<Integer> yearList;
    private ArrayList<Integer> monthList;
    private ArrayList<Integer> hourList;
    private ArrayList<Integer> minuteList;
    private Activity context;
    private String[] dayList;

    public WheelUtil(View view_date, int MaxCount, Activity context) {
        this.view_date = view_date;
        this.MaxCount = MaxCount;
        this.context = context;
        yearList = new ArrayList<Integer>();
        monthList = new ArrayList<Integer>();
        hourList = new ArrayList<Integer>();
        minuteList = new ArrayList<Integer>();
    }

    public WheelUtil(View view_date, int MaxCount, Calendar calendar, Activity context) {
        this.view_date = view_date;
        this.MaxCount = MaxCount;
        this.mCalendar = calendar;
        this.context = context;
        yearList = new ArrayList<Integer>();
        monthList = new ArrayList<Integer>();
        hourList = new ArrayList<Integer>();
        minuteList = new ArrayList<Integer>();
    }

    // public void showCountPicker() {
    // wv_count = (WheelView) com.vgtech.personaledition.view.findViewById(R.id.count);
    // wv_count.setAdapter(new NumericWheelAdapter(1, MaxCount));
    // wv_count.setCyclic(true);
    // wv_count.setLabel("份");
    // wv_count.setCurrentItem(0);
    //
    // // 根据屏幕密度来指定选择器字体的大小
    // int textSize = 0;
    // textSize = 25;
    // wv_count.TEXT_SIZE = textSize;
    // }

    /**
     * @author:
     * @date:
     * @Title: showHourMinPicker
     * @Description:
     */
    public void showHourMinPicker() {
        wv_hours = (WheelView) view_date.findViewById(R.id.hour);
        wv_mins = (WheelView) view_date.findViewById(R.id.minute);

        Calendar calendar = mCalendar;
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }

        int cHour = calendar.get(Calendar.HOUR_OF_DAY);
        int cMinute = calendar.get(Calendar.MINUTE);
        // 时
        hourList = getList(0, 23);
        wv_hours.setAdapter(new NumericWheelAdapter(0, 23, "%02d"));
        wv_hours.setCyclic(true);
        wv_hours.setLabel(context.getResources().getString(R.string.date_hour));
        wv_hours.setCurrentItem(cHour);
        // 分
        minuteList = getList(0, 59);
        List<Integer> minuList = new ArrayList<>();
        for (int i = 0; i < minuteList.size(); i++) {
            Integer minute = minuteList.get(i);
            if (minute % 5 == 0) {
                minuList.add(minute);
            }
        }
        wv_mins.setAdapter(new MinuteWheelAdapter(minuList, "%02d"));
        wv_mins.setCyclic(true);
        wv_mins.setLabel(context.getResources().getString(R.string.date_minute));
        wv_mins.setCurrentItem(cMinute);
        // 根据屏幕密度来指定选择器字体的大小
        int textSize = 0;
        textSize = sp2px(view_date.getContext(), 13);
        wv_hours.TEXT_SIZE = textSize;
        wv_mins.TEXT_SIZE = textSize;
    }

//    public void showSelect(TextView tv, String[] arrays) {
//        String dateS = tv.getText().toString();
//        int position = 0;
//        if (!TextUtils.isEmpty(dateS)) {
//            for (int i = 0; i < arrays.length; i++) {
//                String s = arrays[i];
//                if (s.equals(dateS)) {
//                    position = i;
//                    break;
//                }
//            }
//        }
//        wv_yeas = (WheelView) view_date.findViewById(R.id.select);
//        wv_yeas.setAdapter(new ArrayWheelAdapter<String>(arrays, arrays.length));
//        wv_yeas.setVisibleItems(5);
//        wv_yeas.setCyclic(false);
//        wv_yeas.setCurrentItem(position);
//        int textSize = sp2px(view_date.getContext(), 13);
//        wv_yeas.TEXT_SIZE = textSize;
//    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获得数据 不带天数的
     *
     * @return
     */
    public String getSelect() {
        String year = wv_yeas.getAdapter().getItem(wv_yeas.getCurrentItem());
        return year;
    }

    /**
     * @Description: TODO 弹出日期时间选择器 年月日 随便选
     */
    public void showDateTimePicker() {
        wv_yeas = (WheelView) view_date.findViewById(R.id.year);
        wv_months = (WheelView) view_date.findViewById(R.id.months);
        wv_day = (WheelView) view_date.findViewById(R.id.day);
        wv_hours = (WheelView) view_date.findViewById(R.id.hour);
        wv_mins = (WheelView) view_date.findViewById(R.id.minute);


        Calendar calendar = mCalendar;
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }


        int cYeas = calendar.get(Calendar.YEAR);
        int cMonths = calendar.get(Calendar.MONTH);
        int cDay = calendar.get(Calendar.DATE);
        int cHour = calendar.get(Calendar.HOUR_OF_DAY);
        int cMinute = calendar.get(Calendar.MINUTE);

        // 年
        int maxYears = cYeas + 50;
        yearList = getList(cYeas - 50, maxYears);
        wv_yeas.setAdapter(new NumericWheelAdapter(cYeas - 50, maxYears));
        wv_yeas.setCyclic(false);
        wv_yeas.setLabel(context.getResources().getString(R.string.date_year));
        wv_yeas.setCurrentItem(50);
        // 月
        monthList = getList(1, 12);
        wv_months.setAdapter(new NumericWheelAdapter(1, 12));
        wv_months.setCyclic(true);
        wv_months.setLabel(context.getResources().getString(R.string.date_month));
        wv_months.setCurrentItem(cMonths);
        // 日
        dayList = getWeekList(cYeas, cMonths, getCurrentMonthAllDay(cYeas, cMonths + 1));
        wv_day.setCyclic(true);
        wv_day.setLabel(context.getResources().getString(R.string.date_day));
        wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
//		wv_day.setBackgroundColor(Color.GREEN);
        wv_day.setCurrentItem(cDay - 1);
        // 时
        hourList = getList(0, 24);
        wv_hours.setAdapter(new NumericWheelAdapter(0, 24, "%02d"));
        wv_hours.setCyclic(true);
        wv_hours.setLabel(context.getResources().getString(R.string.date_hour));
        wv_hours.setCurrentItem(cHour);
        // 分
        minuteList = getList(0, 59);
        wv_mins.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
        wv_mins.setCyclic(true);
        wv_mins.setLabel(context.getResources().getString(R.string.date_minute));
        wv_mins.setCurrentItem(cMinute);
        // 根据屏幕密度来指定选择器字体的大小
        int textSize = 0;
        textSize = sp2px(view_date.getContext(), 13);
        wv_yeas.TEXT_SIZE = textSize;
        wv_months.TEXT_SIZE = textSize;
        wv_day.TEXT_SIZE = textSize;
        wv_hours.TEXT_SIZE = textSize;
        wv_mins.TEXT_SIZE = textSize;

        // 监听
        wv_yeas.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int c_year = yearList.get(newValue);
                int c_month = monthList.get(wv_months.getCurrentItem());
                dayList = getWeekList(c_year, c_month - 1, getCurrentMonthAllDay(c_year, c_month));
                wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
                if (wv_day.getCurrentItem() >= dayList.length) {
                    wv_day.setCurrentItem(dayList.length - 1);
                }
            }
        });
        wv_months.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int c_year = yearList.get(wv_yeas.getCurrentItem());
                int c_month = monthList.get(newValue);
                dayList = getWeekList(c_year, c_month - 1, getCurrentMonthAllDay(c_year, c_month));
                wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
                if (wv_day.getCurrentItem() >= dayList.length) {
                    wv_day.setCurrentItem(dayList.length - 1);
                }
            }
        });
    }

    private boolean mSpitFive;

    public void showDateTimePickerSpitFive() {
        mSpitFive = true;
        wv_yeas = (WheelView) view_date.findViewById(R.id.year);
        wv_months = (WheelView) view_date.findViewById(R.id.months);
        wv_day = (WheelView) view_date.findViewById(R.id.day);
        wv_hours = (WheelView) view_date.findViewById(R.id.hour);
        wv_mins = (WheelView) view_date.findViewById(R.id.minute);


        Calendar calendar = mCalendar;
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }


        int cYeas = calendar.get(Calendar.YEAR);
        int cMonths = calendar.get(Calendar.MONTH);
        int cDay = calendar.get(Calendar.DATE);
        int cHour = calendar.get(Calendar.HOUR_OF_DAY);
        int cMinute = calendar.get(Calendar.MINUTE);
        if (cMinute > 55) {
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, cHour + 1);
            cYeas = calendar.get(Calendar.YEAR);
            cMonths = calendar.get(Calendar.MONTH);
            cDay = calendar.get(Calendar.DATE);
            cHour = calendar.get(Calendar.HOUR_OF_DAY);
            cMinute = calendar.get(Calendar.MINUTE);
        }
        // 年
        int maxYears = cYeas + 50;
        yearList = getList(cYeas - 50, maxYears);
        wv_yeas.setAdapter(new NumericWheelAdapter(cYeas - 50, maxYears));
        wv_yeas.setCyclic(false);
        wv_yeas.setLabel(context.getResources().getString(R.string.date_year));
        wv_yeas.setCurrentItem(50);
        // 月
        monthList = getList(1, 12);
        wv_months.setAdapter(new NumericWheelAdapter(1, 12));
        wv_months.setCyclic(true);
        wv_months.setLabel(context.getResources().getString(R.string.date_month));
        wv_months.setCurrentItem(cMonths);
        // 日
        dayList = getWeekList(cYeas, cMonths, getCurrentMonthAllDay(cYeas, cMonths + 1));
        wv_day.setCyclic(true);
        wv_day.setLabel(context.getResources().getString(R.string.date_day));
        wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
//		wv_day.setBackgroundColor(Color.GREEN);
        wv_day.setCurrentItem(cDay - 1);
        // 时
        hourList = getList(0, 24);
        wv_hours.setAdapter(new NumericWheelAdapter(0, 24, "%02d"));
        wv_hours.setCyclic(true);
        wv_hours.setLabel(context.getResources().getString(R.string.date_hour));
        wv_hours.setCurrentItem(cHour);
        // 分
        minuteList = getMinuteList(0, 59);
        wv_mins.setAdapter(new MinuteWheelAdapter(minuteList, "%02d"));
        wv_mins.setCyclic(true);
        wv_mins.setLabel(context.getResources().getString(R.string.date_minute));
        int index = (cMinute / 5);
        int y = cMinute % 5;
        if (y != 0)
            index += 1;
        wv_mins.setCurrentItem(index);
        // 根据屏幕密度来指定选择器字体的大小
        int textSize = 0;
        textSize = sp2px(view_date.getContext(), 13);
        wv_yeas.TEXT_SIZE = textSize;
        wv_months.TEXT_SIZE = textSize;
        wv_day.TEXT_SIZE = textSize;
        wv_hours.TEXT_SIZE = textSize;
        wv_mins.TEXT_SIZE = textSize;

        // 监听
        wv_yeas.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int c_year = yearList.get(newValue);
                int c_month = monthList.get(wv_months.getCurrentItem());
                dayList = getWeekList(c_year, c_month - 1, getCurrentMonthAllDay(c_year, c_month));
                wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
                if (wv_day.getCurrentItem() >= dayList.length) {
                    wv_day.setCurrentItem(dayList.length - 1);
                }
            }
        });
        wv_months.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int c_year = yearList.get(wv_yeas.getCurrentItem());
                int c_month = monthList.get(newValue);
                dayList = getWeekList(c_year, c_month - 1, getCurrentMonthAllDay(c_year, c_month));
                wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
                if (wv_day.getCurrentItem() >= dayList.length) {
                    wv_day.setCurrentItem(dayList.length - 1);
                }
            }
        });
    }

    /**
     * @Description: TODO 弹出日期时间选择器 带年月日 只能从今天开始往后选
     */
    public void showDateTimePicker2() {
        wv_yeas = (WheelView) view_date.findViewById(R.id.year);
        wv_months = (WheelView) view_date.findViewById(R.id.months);
        wv_day = (WheelView) view_date.findViewById(R.id.day);

        Calendar calendar = mCalendar;
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }

        int cYeas = calendar.get(Calendar.YEAR);
        int cMonths = calendar.get(Calendar.MONTH);
        int cDay = calendar.get(Calendar.DATE);
        // 年
        int maxYears = cYeas + 50;
        yearList = getList(cYeas, maxYears);
        wv_yeas.setAdapter(new NumericWheelAdapter(cYeas, maxYears));
        wv_yeas.setCyclic(false);
        wv_yeas.setLabel(context.getResources().getString(R.string.date_year));
        wv_yeas.setCurrentItem(0);
        // 月
        monthList = getList(1, 12);
        wv_months.setAdapter(new NumericWheelAdapter(1, 12));
        wv_months.setCyclic(true);
        wv_months.setLabel(context.getResources().getString(R.string.date_month));
        wv_months.setCurrentItem(cMonths);
        // 日
        dayList = getWeekList(cYeas, cMonths, getCurrentMonthAllDay(cYeas, cMonths + 1));
        wv_day.setCyclic(true);
        wv_day.setLabel(context.getResources().getString(R.string.date_day));
        wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
//		wv_day.setBackgroundColor(Color.GREEN);
        wv_day.setCurrentItem(cDay - 1);
        // 根据屏幕密度来指定选择器字体的大小
        int textSize = sp2px(view_date.getContext(), 13);
        wv_yeas.TEXT_SIZE = textSize;
        wv_months.TEXT_SIZE = textSize;
        wv_day.TEXT_SIZE = textSize;

        // 监听
        wv_yeas.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int c_year = yearList.get(newValue);
                int c_month = monthList.get(wv_months.getCurrentItem());
                dayList = getWeekList(c_year, c_month - 1, getCurrentMonthAllDay(c_year, c_month));
                wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
                if (wv_day.getCurrentItem() >= dayList.length) {
                    wv_day.setCurrentItem(dayList.length - 1);
                }
            }
        });
        wv_months.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int c_year = yearList.get(wv_yeas.getCurrentItem());
                int c_month = monthList.get(newValue);
                dayList = getWeekList(c_year, c_month - 1, getCurrentMonthAllDay(c_year, c_month));
                wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
                if (wv_day.getCurrentItem() >= dayList.length) {
                    wv_day.setCurrentItem(dayList.length - 1);
                }
            }
        });
    }

    /**
     * @Description: TODO 弹出日期时间选择器 只带年月 不带日 可以从前面选
     */
    public void showDateTimePicker3() {
        wv_yeas = (WheelView) view_date.findViewById(R.id.year);
        wv_months = (WheelView) view_date.findViewById(R.id.months);
        wv_day = (WheelView) view_date.findViewById(R.id.day);
        wv_day.setVisibility(View.GONE);


        Calendar calendar = mCalendar;
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }

        int cYeas = calendar.get(Calendar.YEAR);
        int cMonths = calendar.get(Calendar.MONTH);
        // 年
        int maxYears = cYeas + 50;
        yearList = getList(cYeas - 50, maxYears);
        wv_yeas.setAdapter(new NumericWheelAdapter(cYeas - 50, maxYears));
        wv_yeas.setCyclic(false);
        wv_yeas.setLabel(context.getResources().getString(R.string.date_year));
        wv_yeas.setCurrentItem(50);
        // 月
        monthList = getList(1, 12);
        wv_months.setAdapter(new NumericWheelAdapter(1, 12));
        wv_months.setCyclic(true);
        wv_months.setLabel(context.getResources().getString(R.string.date_month));
        wv_months.setCurrentItem(cMonths);

        // 根据屏幕密度来指定选择器字体的大小
        int textSize = 0;
        textSize = sp2px(view_date.getContext(), 13);
        ;
        wv_yeas.TEXT_SIZE = textSize;
        wv_months.TEXT_SIZE = textSize;
    }

    /**
     * @Description: TODO 弹出日期时间选择器 只带年 只可以从后面选
     */
    public void showYearPicker() {
        wv_yeas = (WheelView) view_date.findViewById(R.id.year);
        wv_months = (WheelView) view_date.findViewById(R.id.months);
        wv_day = (WheelView) view_date.findViewById(R.id.day);
        wv_months.setVisibility(View.VISIBLE);
        wv_day.setVisibility(View.GONE);


        Calendar calendar = Calendar.getInstance();

        int cYeas = calendar.get(Calendar.YEAR);
        int year = mCalendar.get(Calendar.YEAR);
        int cMonths = calendar.get(Calendar.MONTH);
        int index = 120 - (cYeas - year);
        // 年
        int maxYears = cYeas;
        yearList = getList(cYeas - 120, maxYears);
        wv_yeas.setAdapter(new NumericWheelAdapter(cYeas - 120, maxYears));
        wv_yeas.setCyclic(false);
        wv_yeas.setLabel(context.getResources().getString(R.string.date_year));
        wv_yeas.setCurrentItem(index);
        // 月
        monthList = getList(1, 12);
        wv_months.setAdapter(new NumericWheelAdapter(1, 12));
        wv_months.setCyclic(true);
        wv_months.setLabel(context.getResources().getString(R.string.date_month));
        wv_months.setCurrentItem(cMonths);
        // 月
//        monthList = getList(1, 12);
//        wv_months.setAdapter(new NumericWheelAdapter(1, 12));
//        wv_months.setCyclic(true);
//        wv_months.setLabel("月");
//        wv_months.setCurrentItem(cMonths);

        // 根据屏幕密度来指定选择器字体的大小
        int textSize = 0;
        textSize = sp2px(view_date.getContext(), 13);
        ;
        wv_yeas.TEXT_SIZE = textSize;
        wv_months.TEXT_SIZE = textSize;
    }

    /**
     * @Description: TODO 弹出日期时间选择器 带年月日 只能从明天开始往后选
     */
    public void showDateTimePicker4() {
        wv_yeas = (WheelView) view_date.findViewById(R.id.year);
        wv_months = (WheelView) view_date.findViewById(R.id.months);
        wv_day = (WheelView) view_date.findViewById(R.id.day);

        Calendar calendar = mCalendar;
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }

        int nYeas = Calendar.getInstance().get(Calendar.YEAR);
        int cYeas = calendar.get(Calendar.YEAR);
        int cMonths = calendar.get(Calendar.MONTH);
        int cDay = calendar.get(Calendar.DATE);
        // 年
        int minYears = nYeas - 10;
        int maxYears = nYeas + 10;
        yearList = getList(minYears, maxYears);
        wv_yeas.setAdapter(new NumericWheelAdapter(minYears, maxYears));
        wv_yeas.setCyclic(false);
        wv_yeas.setLabel(context.getResources().getString(R.string.date_year));
        wv_yeas.setCurrentItem(cYeas - minYears);
        // 月
        monthList = getList(1, 12);
        wv_months.setAdapter(new NumericWheelAdapter(1, 12));
        wv_months.setCyclic(true);
        wv_months.setLabel(context.getResources().getString(R.string.date_month));
        wv_months.setCurrentItem(cMonths);
        // 日
        dayList = getWeekList(cYeas, cMonths, getCurrentMonthAllDay(cYeas, cMonths + 1));
        wv_day.setCyclic(true);
        wv_day.setLabel(context.getResources().getString(R.string.date_day));
        wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
//		wv_day.setBackgroundColor(Color.GREEN);
        wv_day.setCurrentItem(cDay - 1);
        // 根据屏幕密度来指定选择器字体的大小
        int textSize = 0;
        textSize = sp2px(view_date.getContext(), 13);
        ;
        wv_yeas.TEXT_SIZE = textSize;
        wv_months.TEXT_SIZE = textSize;
        wv_day.TEXT_SIZE = textSize;

        // 监听
        wv_yeas.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int c_year = yearList.get(newValue);
                int c_month = monthList.get(wv_months.getCurrentItem());
                dayList = getWeekList(c_year, c_month - 1, getCurrentMonthAllDay(c_year, c_month));
                wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
                if (wv_day.getCurrentItem() >= dayList.length) {
                    wv_day.setCurrentItem(dayList.length - 1);
                }
            }
        });
        wv_months.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int c_year = yearList.get(wv_yeas.getCurrentItem());
                int c_month = monthList.get(newValue);
                dayList = getWeekList(c_year, c_month - 1, getCurrentMonthAllDay(c_year, c_month));
                wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
                if (wv_day.getCurrentItem() >= dayList.length) {
                    wv_day.setCurrentItem(dayList.length - 1);
                }
            }
        });
    }

    /**
     * @Description: TODO 弹出日期时间选择器 带年月日 只能从明天开始往后选
     */
    public void showDateTimePicker5() {
        wv_yeas = (WheelView) view_date.findViewById(R.id.year);
        wv_months = (WheelView) view_date.findViewById(R.id.months);
        wv_day = (WheelView) view_date.findViewById(R.id.day);

        Calendar calendar = mCalendar;
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }

        int nYeas = Calendar.getInstance().get(Calendar.YEAR);
        int cYeas = calendar.get(Calendar.YEAR);
        int cMonths = calendar.get(Calendar.MONTH);
        int cDay = calendar.get(Calendar.DATE);
        // 年
        int minYears = nYeas - 100;
        int maxYears = nYeas + 10;
        yearList = getList(minYears, maxYears);
        wv_yeas.setAdapter(new NumericWheelAdapter(minYears, maxYears));
        wv_yeas.setCyclic(false);
        wv_yeas.setLabel(context.getResources().getString(R.string.date_year));
        wv_yeas.setCurrentItem(cYeas - minYears);
        // 月
        monthList = getList(1, 12);
        wv_months.setAdapter(new NumericWheelAdapter(1, 12));
        wv_months.setCyclic(true);
        wv_months.setLabel(context.getResources().getString(R.string.date_month));
        wv_months.setCurrentItem(cMonths);
        // 日
        dayList = getWeekList(cYeas, cMonths, getCurrentMonthAllDay(cYeas, cMonths + 1));
        wv_day.setCyclic(true);
        wv_day.setLabel(context.getResources().getString(R.string.date_day));
        wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
//		wv_day.setBackgroundColor(Color.GREEN);
        wv_day.setCurrentItem(cDay - 1);
        // 根据屏幕密度来指定选择器字体的大小
        int textSize = 0;
        textSize = sp2px(view_date.getContext(), 13);

        wv_yeas.TEXT_SIZE = textSize;
        wv_months.TEXT_SIZE = textSize;
        wv_day.TEXT_SIZE = textSize;

        // 监听
        wv_yeas.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int c_year = yearList.get(newValue);
                int c_month = monthList.get(wv_months.getCurrentItem());
                dayList = getWeekList(c_year, c_month - 1, getCurrentMonthAllDay(c_year, c_month));
                wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
                if (wv_day.getCurrentItem() >= dayList.length) {
                    wv_day.setCurrentItem(dayList.length - 1);
                }
            }
        });
        wv_months.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int c_year = yearList.get(wv_yeas.getCurrentItem());
                int c_month = monthList.get(newValue);
                dayList = getWeekList(c_year, c_month - 1, getCurrentMonthAllDay(c_year, c_month));
                wv_day.setAdapter(new ArrayWheelAdapter<String>(dayList));
                if (wv_day.getCurrentItem() >= dayList.length) {
                    wv_day.setCurrentItem(dayList.length - 1);
                }
            }
        });
    }

    public String[] getWeekList(int y, int m, int length) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(y, m, 1);
        String[] sts = new String[length];
        for (int i = 0; i < length; i++) {
            sts[i] = calendar.get(Calendar.DATE) + " ";
            calendar.add(Calendar.DATE, 1);
        }
        return sts;
    }

    public ArrayList<Integer> getList(int min, int max) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        int length = max - min + 1;
        for (int i = 0; i < length; i++) {
//            if (!mSpitFive || mSpitFive && i % 5 == 0)
            list.add(min);
            min++;
        }
        return list;
    }

    public ArrayList<Integer> getMinuteList(int min, int max) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        int length = max - min + 1;
        for (int i = 0; i < length; i++) {
            if (!mSpitFive || mSpitFive && i % 5 == 0)
                list.add(min);
            min++;
        }
        return list;
    }

    public int getCurrentMonthAllDay(int year, int month) {
        int day = 0;
        if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
            day = 31;
        } else if (isLeapYear(year) && month == 2) {
            day = 29;
        } else if (!isLeapYear(year) && month == 2) {
            day = 28;
        } else {
            day = 30;
        }
        return day;
    }

    public boolean isLeapYear(int year) {
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
            return true;
        }
        return false;
    }

    public String getCount() {
        String count = wv_count.getAdapter().getItem(wv_count.getCurrentItem());
        return count;
    }

    /**
     * 获得数据 年月日
     *
     * @return
     */
    public String getDateTime() {
        StringBuffer sb = new StringBuffer();
        String year = wv_yeas.getAdapter().getItem(wv_yeas.getCurrentItem());
        String month = wv_months.getAdapter().getItem(wv_months.getCurrentItem());
        String day = getDay(wv_day.getAdapter().getItem(wv_day.getCurrentItem()));
        if (month.length() == 1) {
            month = 0 + month;
        }
        if (day.length() == 1) {
            day = 0 + day;
        }

        sb.append(year).append("-").append(month).append("-").append(day).append(" ");
        return sb.toString();
    }

    /**
     * 获得数据 年月日 时 分
     *
     * @return
     */
    public String getTime() {
        StringBuffer sb = new StringBuffer();
        String year = wv_yeas.getAdapter().getItem(wv_yeas.getCurrentItem());
        String month = wv_months.getAdapter().getItem(wv_months.getCurrentItem());
        String day = getDay(wv_day.getAdapter().getItem(wv_day.getCurrentItem()));
        String hour = wv_hours.getAdapter().getItem(wv_hours.getCurrentItem());
        String minute = wv_mins.getAdapter().getItem(wv_mins.getCurrentItem());
        if (month.length() == 1) {
            month = 0 + month;
        }
        if (day.length() == 1) {
            day = 0 + day;
        }
        sb.append(year).append("-").append(month).append("-").append(day).append(" ").append(hour).append(":").append(minute);
        return sb.toString();
    }

    /**
     * 获得毫秒数
     *
     * @return
     */
    public Long getMillisecond() {
        StringBuffer sb = new StringBuffer();
        String year = wv_yeas.getAdapter().getItem(wv_yeas.getCurrentItem());
        String month = wv_months.getAdapter().getItem(wv_months.getCurrentItem());
        if (month.length() == 1) {
            month = 0 + month;
        }
        sb.append(year).append(month);
        long result = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            result = sdf.parse(sb.toString()).getTime();
        } catch (Exception e) {

        }

        return result;
    }

    /**
     * 获得毫秒数
     *
     * @return
     */
    public Long getYearMillisecond() {
        StringBuffer sb = new StringBuffer();
        String year = wv_yeas.getAdapter().getItem(wv_yeas.getCurrentItem());
        sb.append(year);
        long result = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            result = sdf.parse(sb.toString()).getTime();
        } catch (Exception e) {

        }

        return result;
    }

    /**
     * @return
     */
    public String getYear() {
        StringBuffer sb = new StringBuffer();
        String year = wv_yeas.getAdapter().getItem(wv_yeas.getCurrentItem());
        return year;
    }

    /**
     * 获得数据 小时和分
     *
     * @return
     */
    public String getHourMinTime() {
        StringBuffer sb = new StringBuffer();
        String hour = wv_hours.getAdapter().getItem(wv_hours.getCurrentItem());
        String minute = wv_mins.getAdapter().getItem(wv_mins.getCurrentItem());

        sb.append(hour).append(":").append(minute);
        return sb.toString();
    }

    /**
     * 获得数据 不带天数的
     *
     * @return
     */
    public String getTimeNoDay() {
        StringBuffer sb = new StringBuffer();
        String year = wv_yeas.getAdapter().getItem(wv_yeas.getCurrentItem());
        String month = wv_months.getAdapter().getItem(wv_months.getCurrentItem());
        if (month.length() == 1) {
            month = 0 + month;
        }

        sb.append(year).append(context.getResources().getString(R.string.date_year))
                .append(month).append(context.getResources().getString(R.string.date_month));
        return sb.toString();
    }

    public String getDay(String str) {
        return str.split(" ")[0];
    }

    /**
     * 获得数据中的日历、时、分
     */
    public static final int DATE = 1;
    public static final int HOUR = 2;
    public static final int MINUTE = 3;

    public String getDateString(String wheelDate, int mark) {
        String date = "";
        String[] strArray = wheelDate.split("\\+");
        switch (mark) {
            case DATE:
                date = strArray[0];
                break;
            case HOUR:
                date = strArray[1];
                break;
            case MINUTE:
                date = strArray[2];
                break;
        }
        return date;
    }
}
