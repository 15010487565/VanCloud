package com.vgtech.common.view.calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.vgtech.common.R;
import com.vgtech.common.api.ScheduleisExist;
import com.vgtech.common.utils.CalendarUtils;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.view.NoScrollGridview;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Duke on 2015/9/17.
 */
public class WeekFragment extends Fragment implements RefreshFragment {

    public static final String ARG_PAGE = "page";

    private int mPageNumber;
    Date myDate;
    public WeekGridViewAdapter weekGridViewAdapter;
    public NoScrollGridview calendarView;
    private OnDateSelectListener mDateSelectListener;

    public void setDateSelectListener(OnDateSelectListener listener) {
        mDateSelectListener = listener;
    }

    public static WeekFragment create(int pageNumber) {
        WeekFragment fragment = new WeekFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public WeekFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
        myDate = CalendarUtils.getSelectWeek(mPageNumber);
        weekGridViewAdapter = new WeekGridViewAdapter(getActivity(),
                myDate);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.calendar_view, container, false);
        calendarView = (NoScrollGridview) rootView
                .findViewById(R.id.calendarView);

        calendarView.setItemClick(true);
        calendarView.setAdapter(weekGridViewAdapter);
        calendarView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
//
                weekGridViewAdapter.clickSelect(view.getTag(R.id.date) + "");
                if (mDateSelectListener != null) {
                    mDateSelectListener.onSelected(view.getTag(R.id.date) + "");
                    Date date = null;
                    try {
                        date = new SimpleDateFormat("yyyy年MM月dd日").parse(view.getTag(R.id.date) + "");
                    } catch (Exception e) {
                    }

                    mDateSelectListener.onSelected(date);
                }
            }
        });
        return rootView;
    }

    /**
     * 选中指定日期
     *
     * @param specifiedDate
     */
    public void selectSpecifiedDate(long specifiedDate) {
        Date date = new Date(specifiedDate);
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
        String d = format.format(date);
        if (weekGridViewAdapter != null) {
            weekGridViewAdapter.clickSelect(d);
        }
    }

    @Override
    public void refresh(List<ScheduleisExist> scheduleisExists) {
        if (weekGridViewAdapter != null) {
            weekGridViewAdapter.refreshAction(scheduleisExists);
        }
    }

    public String getMonth() {
        String month;
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM");
        Date firstDate = CalendarUtils.getNowWeekMonday(myDate, Calendar.SUNDAY);
        Date lastDate = CalendarUtils.getNowWeekMonday(myDate, Calendar.SATURDAY);
        Date nowDate = new Date(System.currentTimeMillis());//获取当前时间

        if (firstDate.getTime() < nowDate.getTime() && nowDate.getTime() < lastDate.getTime()) {
            month = format.format(nowDate);
        } else {
            month = format.format(firstDate);
        }
        return month;
    }

}
