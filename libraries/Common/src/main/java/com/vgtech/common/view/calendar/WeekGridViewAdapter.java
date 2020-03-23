package com.vgtech.common.view.calendar;

import android.app.Activity;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vgtech.common.R;
import com.vgtech.common.api.ScheduleisExist;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.utils.CalendarUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Duke on 2015/9/17.
 */
public class WeekGridViewAdapter extends BaseAdapter {

    private Date myDate;
    private Activity activity;
    private Resources resources;
    private String selectDate;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
    private List<ScheduleisExist> scheduleisExists = new ArrayList<>();

    // construct
    public WeekGridViewAdapter(Activity a, Date date) {
        myDate = date;
        activity = a;
        resources = activity.getResources();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                new AsyncTask<Void, Void, ArrayList<Date>>() {
                    @Override
                    protected ArrayList<Date> doInBackground(Void... params) {
                        return getDates();
                    }

                    @Override
                    protected void onPostExecute(ArrayList<Date> dates) {
                        titles = dates;
                        notifyDataSetChanged();
                    }
                }.execute();
            }
        });
//        this.selectDate = format.format(new Date());
    }

    public WeekGridViewAdapter(Activity a) {
        activity = a;
        resources = activity.getResources();
//        this.selectDate = format.format(new Date());
    }


    ArrayList<Date> titles;

    private ArrayList<Date> getDates() {
        ArrayList<Date> alArrayList = new ArrayList<Date>();
        Date sunday = CalendarUtils.getNowWeekMonday(myDate, Calendar.SUNDAY);
        alArrayList.add(sunday);
        Date monday = CalendarUtils.getNowWeekMonday(myDate, Calendar.MONDAY);
        alArrayList.add(monday);
        Date tuesday = CalendarUtils.getNowWeekMonday(myDate, Calendar.TUESDAY);
        alArrayList.add(tuesday);
        Date wednesday = CalendarUtils.getNowWeekMonday(myDate, Calendar.WEDNESDAY);
        alArrayList.add(wednesday);
        Date thursday = CalendarUtils.getNowWeekMonday(myDate, Calendar.THURSDAY);
        alArrayList.add(thursday);
        Date friday = CalendarUtils.getNowWeekMonday(myDate, Calendar.FRIDAY);
        alArrayList.add(friday);
        Date saturday = CalendarUtils.getNowWeekMonday(myDate, Calendar.SATURDAY);
        alArrayList.add(saturday);
        return alArrayList;
    }

    @Override
    public int getCount() {
        return titles == null ? 0 : titles.size();
    }

    @Override
    public Object getItem(int position) {
        return titles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.calendar_item_layout,
                    null);
            holder = new ViewHolder();
            holder.dayTextView = (TextView) convertView.findViewById(R.id.day_text);
            holder.reddotView = convertView.findViewById(R.id.red_dot);
            holder.clickViewBg = convertView.findViewById(R.id.click_view);
            convertView.setTag(R.id.day_text, holder);
        } else {
            holder = (ViewHolder) convertView.getTag(R.id.day_text);
        }
        Date myDate = (Date) getItem(position);
        String str = format.format(myDate);
        Calendar calCalendar = Calendar.getInstance();
        calCalendar.setTime(myDate);

        final int iMonth = calCalendar.get(Calendar.MONTH);
        final int iDay = calCalendar.get(Calendar.DAY_OF_WEEK);


        // 判断周六周日
//		iv.setBackgroundColor(resources.getColor(R.color.white));
//		if (iDay == 7) {
//			// 周六
//			iv.setBackgroundColor(resources.getColor(R.color.text_6));
//		} else if (iDay == 1) {
//			// 周日
//			iv.setBackgroundColor(resources.getColor(R.color.text_7));
//		}
        if (!TextUtils.isEmpty(selectDate) && str.equals(selectDate)) {
            if (!equalsDate(new Date(), myDate)) {
                holder.clickViewBg.setVisibility(View.VISIBLE);
                holder.clickViewBg.setBackgroundResource(R.drawable.ground_bg);
                holder.dayTextView.setTextColor(resources.getColor(R.color.white));
            }
        } else {
            if (equalsDate(new Date(), myDate)) {
                // 当前日期
                holder.clickViewBg.setVisibility(View.VISIBLE);
                holder.clickViewBg.setBackgroundResource(R.drawable.today_ground);
                holder.dayTextView.setTextColor(resources.getColor(R.color.white));
            } else {
                holder.clickViewBg.setVisibility(View.GONE);
                holder.dayTextView.setTextColor(resources.getColor(R.color.text_black));
            }
        }

        if (isExistData(myDate)) {
            holder.reddotView.setVisibility(View.VISIBLE);
        } else {
            holder.reddotView.setVisibility(View.GONE);
        }

//        // 判断是否是当前月
//        if (iMonth == iMonthViewCurrentMonth) {
//            holder.dayTextView.setTextColor(resources.getColor(R.color.text_color));
//        } else {
//            holder.dayTextView.setTextColor(resources.getColor(R.color.comment_grey));
//        }

        final int day = myDate.getDate(); // 日期
        holder.dayTextView.setText(String.valueOf(day));
        convertView.setTag(R.id.date, str);
        return convertView;
    }

    class ViewHolder {

        private TextView dayTextView;
        private View reddotView;
        private View clickViewBg;

    }

    private Boolean equalsDate(Date date1, Date date2) {
        if (date1.getYear() == date2.getYear()
                && date1.getMonth() == date2.getMonth()
                && date1.getDate() == date2.getDate()) {
            return true;
        } else {
            return false;
        }
    }

    private Boolean isExistData(Date myDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String str = format.format(myDate);
        if (scheduleisExists != null && scheduleisExists.size() > 0) {
            for (ScheduleisExist scheduleisExist : scheduleisExists) {
                if (String.valueOf(DateTimeUtil.stringToLong_YMd(str)).equals(scheduleisExist.day)) {
                    Log.i("chen_zhanyamg", "isExistData: ");
                    return true;
                } else {
                    int i = 0;
                    Log.i("chen_zhanyang", "isExistData: ");
                }
            }
        }
        return false;
    }

    public void clickSelect(String selectDate) {
        this.selectDate = selectDate;
        notifyDataSetChanged();
    }

    public void refreshAction(List<ScheduleisExist> sch) {
        this.scheduleisExists = sch;
        notifyDataSetChanged();
    }
}
