/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vgtech.common.view.calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.vgtech.common.R;
import com.vgtech.common.api.ScheduleisExist;
import com.vgtech.common.utils.CalendarUtils;
import com.vgtech.common.view.NoScrollGridview;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 日程-日历
 * by duke
 */
public class CalendarFragment extends Fragment implements RefreshFragment {
    public static final String ARG_PAGE = "page";

    private int mPageNumber;

    private Calendar mCalendar;

    private CalendarGridViewAdapter calendarGridViewAdapter;

    private OnDateSelectListener mDateSelectListener;

    private NoScrollGridview calendarView;

    public void setDateSelectListener(OnDateSelectListener listener) {
        mDateSelectListener = listener;
    }

    public static CalendarFragment create(int pageNumber) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("ceshi", "onCreate---------");
        mPageNumber = getArguments().getInt(ARG_PAGE);
        mCalendar = CalendarUtils.getSelectCalendar(mPageNumber);
//        calendarGridViewAdapter = new CalendarGridViewAdapter(getActivity(), mCalendar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        Log.e("ceshi", "onCreateView---------");

        calendarGridViewAdapter = new CalendarGridViewAdapter(getActivity(), mCalendar);
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.calendar_view, container, false);
        calendarView = (NoScrollGridview) rootView
                .findViewById(R.id.calendarView);
//        initGridView(calendarView, calendarGridViewAdapter);
        calendarView.setItemClick(true);
        calendarView.setAdapter(calendarGridViewAdapter);

        calendarView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Date date = (Date) view.getTag(R.id.date);
                calendarGridViewAdapter.clickSelect(date);
                mDateSelectListener.onSelected(date);
            }
        });
        return rootView;
    }

    private void initGridView(GridView gridView, BaseAdapter adapter) {
        gridView = setGirdView(gridView);
        gridView.setAdapter(adapter);// 设置菜单Adapter
    }

    @SuppressWarnings("deprecation")
    private GridView setGirdView(GridView gridView) {
        gridView.setNumColumns(7);// 设置每行列数
        gridView.setGravity(Gravity.CENTER_VERTICAL);// 位置居中
        gridView.setVerticalSpacing(1);// 垂直间隔
        gridView.setHorizontalSpacing(1);// 水平间隔
        gridView.setBackgroundColor(getResources().getColor(
                R.color.calendar_background));

        WindowManager windowManager = getActivity().getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int i = display.getWidth() / 7;
        int j = display.getWidth() - (i * 7);
        int x = j / 2;
        gridView.setPadding(x, 0, 0, 0);// 居中

        return gridView;
    }

    @Override
    public void refresh(List<ScheduleisExist> scheduleisExists) {
        if (calendarGridViewAdapter != null) {
            calendarGridViewAdapter.refreshAction(scheduleisExists);
        }
    }
}
