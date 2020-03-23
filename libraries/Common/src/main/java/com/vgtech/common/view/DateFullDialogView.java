package com.vgtech.common.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vgtech.common.R;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.utils.wheel.WheelUtil;

import java.util.Calendar;

public class DateFullDialogView implements
        View.OnClickListener {
    public static final String DATE_TYPE_MINUTE_SPIT_FIVE = "DATE_TYPE_MINUTE_SPIT_FIVE";
    private Activity context;
    private TextView dateText;
    private Button btn_sure;
    private Button btn_cancel;
    private ButtonClickListener buttonClickListener;
    private WheelUtil mWheel;
    private String type;
    private EditText endDateText;
    private String deadline;
    private Calendar otherCalendar;
    private int color;
    private Calendar calendar;
    private OnSelectedListener onSelectedListener;
    private Dialog dialog;

    public interface ButtonClickListener {
        void sureButtonOnClickListener(String time);

        void cancelButtonOnClickListener();
    }

    public interface OnSelectedListener {
        void onSelectedListener(long time);
    }

    public DateFullDialogView(Activity context, TextView dateText, String type,
                              String style_type) {
        this.dateText = dateText;
        this.context = context;
        this.type = type;
        init(style_type);
    }

    public DateFullDialogView(Activity context, TextView dateText, String type,
                              String style_type, Calendar calendar) {
        this.dateText = dateText;
        this.context = context;
        this.type = type;
        this.calendar = calendar;
        init(style_type);
    }

    public DateFullDialogView(Activity context, TextView dateText, String type,
                              String style_type, Calendar calendar, int color) {
        this.dateText = dateText;
        this.context = context;
        this.type = type;
        this.calendar = calendar;
        this.color = color;
        init(style_type);
    }

    public DateFullDialogView(Activity context, TextView dateText, String type,
                              String style_type, Calendar calendar, int color, Calendar otherCalendar) {
        this.dateText = dateText;
        this.context = context;
        this.type = type;
        this.calendar = calendar;
        this.color = color;
        this.otherCalendar = otherCalendar;
        init(style_type);
    }

    public DateFullDialogView(Activity context, OnSelectedListener listener, String type,
                              String style_type, Calendar calendar, int color, Calendar otherCalendar) {
        this.onSelectedListener = listener;
        this.context = context;
        this.type = type;
        this.calendar = calendar;
        this.color = color;
        this.otherCalendar = otherCalendar;
        init(style_type);
    }

    public DateFullDialogView(Activity context, EditText dateText, String type,
                              EditText endDateText, String deadline, String style_type) {
        this.dateText = dateText;
        this.context = context;
        this.endDateText = endDateText;
        this.deadline = deadline;
        this.type = type;
        init(style_type);
    }

    public DateFullDialogView(Activity context, int theme, String style_type) {
        this.context = context;
        init(style_type);
    }

    private Display display;

    private void init(String style_type) {

        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();

        dialog = new Dialog(context, R.style.ActionSheetDialogStyle);

        if (style_type.equals("date")) {
            View viewdate = LayoutInflater.from(context).inflate(
                    R.layout.date_wheel_view, null);
            viewdate.setMinimumWidth(display.getWidth());
            dialog.setContentView(viewdate);
            btn_sure = (Button) viewdate.findViewById(R.id.btn_sure);
            btn_cancel = (Button) viewdate.findViewById(R.id.btn_cancel);
            btn_cancel.setOnClickListener(this);
            btn_sure.setOnClickListener(this);
            if (calendar != null) {
                mWheel = new WheelUtil(viewdate, 0, calendar, context);
            } else {
                mWheel = new WheelUtil(viewdate, 0, context);
            }

        } else if (style_type.equals("time")) {
            View viewtime = LayoutInflater.from(context).inflate(
                    R.layout.time_wheel_view, null);
            viewtime.setMinimumWidth(display.getWidth());
            dialog.setContentView(viewtime);
            btn_sure = (Button) viewtime.findViewById(R.id.btn_sure);
            btn_cancel = (Button) viewtime.findViewById(R.id.btn_cancel);
            btn_cancel.setOnClickListener(this);
            btn_sure.setOnClickListener(this);
            mWheel = new WheelUtil(viewtime, 0, context);

        } else if (style_type.equals("time-hm")) {
            View viewtimehm = LayoutInflater.from(context).inflate(
                    R.layout.time_hm_wheel_view, null);
            viewtimehm.setMinimumWidth(display.getWidth());
            dialog.setContentView(viewtimehm);
            btn_sure = (Button) viewtimehm.findViewById(R.id.btn_sure);
            btn_cancel = (Button) viewtimehm.findViewById(R.id.btn_cancel);
            btn_cancel.setOnClickListener(this);
            btn_sure.setOnClickListener(this);
            mWheel = new WheelUtil(viewtimehm, 0, context);
        } else {
            View viewtime = LayoutInflater.from(context).inflate(
                    R.layout.fulldate_wheel_view, null);
            viewtime.setMinimumWidth(display.getWidth());
            dialog.setContentView(viewtime);
            btn_sure = (Button) viewtime.findViewById(R.id.btn_sure);
            btn_cancel = (Button) viewtime.findViewById(R.id.btn_cancel);
            btn_cancel.setOnClickListener(this);
            btn_sure.setOnClickListener(this);
            if (calendar != null) {
                mWheel = new WheelUtil(viewtime, 0, calendar, context);
            } else {
                mWheel = new WheelUtil(viewtime, 0, context);
            }
        }

        if (type.equals("full")) {// 最全日期格式 年月日时分
            mWheel.showDateTimePicker();
        } else if (type.equals(DATE_TYPE_MINUTE_SPIT_FIVE)) {
            mWheel.showDateTimePickerSpitFive();
        } else if (type.equals("strict")) {// 日期大于等于今天啊
            mWheel.showDateTimePicker2();
        } else if (type.equals("yearmonth")) {// 只带年月
            mWheel.showDateTimePicker3();
        } else if (type.equals("hourmin")) {
            mWheel.showHourMinPicker();
        } else if (type.equals("else")) {// 只带年月
            mWheel.showDateTimePicker3();
        } else if (type.equals("year")) {// 只带年月
            mWheel.showYearPicker();
        } else if (type.equals("startStrict") || type.equals("startStrict30") || type.equals("StartTime_full") || type.equals("EndTime_full")) {//年月日时分 (专用当前日期之后选起)
            mWheel.showDateTimePicker();
        } else if (type.equals("YMD")) {//年月日
            mWheel.showDateTimePicker5();
        } else if (type.equals("StartTime_YMD")) {//年月日
            mWheel.showDateTimePicker5();
        } else if (type.equals("EndTime_YMD")) {//年月日
            mWheel.showDateTimePicker5();
        } else if ((type.equals("Hm"))) {
            mWheel.showHourMinPicker();
        }
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            String starttime = DateTimeUtil.calendarToString_YMd(otherCalendar);
            String othetTime_full = DateTimeUtil.calendarToString_YMdHm(otherCalendar);
            String current = DateTimeUtil.getCurrentString_YMd();// 当前时间 年月日
            String tomorrow = DateTimeUtil.getSpecifiedDayAfter(current);// 当前时间的第二天
            if (type.equals("full") || type.equals(DATE_TYPE_MINUTE_SPIT_FIVE)) {// 最全日期格式
//                dateText.setText(mWheel.getTime());
                setText(dateText, mWheel.getTime());
                dismiss();
            } else if (type.equals("YMD")) {// 年月日
                if (dateText != null)
                    setText(dateText, mWheel.getDateTime());
                if (onSelectedListener != null)
                    onSelectedListener.onSelectedListener(DateTimeUtil.stringToLong_YMd(mWheel.getDateTime()));
                dismiss();
            } else if (type.equals("StartTime_YMD")) {// 有检验的年月日
                if (DateTimeUtil.isFirstTimeThenSecondTime(mWheel.getDateTime(),
                        starttime)) {
                    Toast.makeText(context, context.getResources().getString(R.string.datefulldialog_info_starttime), Toast.LENGTH_SHORT).show();
                } else {
//                    dateText.setText(mWheel.getDateTime());
                    setText(dateText, mWheel.getDateTime());
                    dismiss();
                }
            } else if (type.equals("EndTime_YMD")) {// 有检验的年月日
                if (DateTimeUtil.isFirstTimeThenSecondTime(starttime,
                        mWheel.getDateTime())) {
                    setText(dateText, mWheel.getDateTime());
                    Toast.makeText(context, context.getResources().getString(R.string.datefulldialog_info_endtime), Toast.LENGTH_SHORT).show();
                } else {
//                    dateText.setText(mWheel.getDateTime());
                    setText(dateText, mWheel.getDateTime());
                    dismiss();
                }
            } else if (type.equals("strict")) {// 日期大于等于今天啊
                // 如果当前时间大于选择时间，说明错误
                if (DateTimeUtil.isFirstTimeThenSecondTime(current,
                        mWheel.getDateTime())) {
                    Toast.makeText(context, "选择日期不能小于当前日期", Toast.LENGTH_SHORT).show();
                } else {
//                    dateText.setText(mWheel.getDateTime());
                    setText(dateText, mWheel.getDateTime());
                    dismiss();
                }
            } else if (type.equals("else")) {// 只带年月
//                dateText.setText(mWheel.getTimeNoDay());
//                setText(dateText, mWheel.getTimeNoDay());
                onSelectedListener.onSelectedListener(mWheel.getMillisecond());
                dismiss();
            } else if (type.equals("year")) {
                setText(dateText, mWheel.getYear());
                onSelectedListener.onSelectedListener(mWheel.getYearMillisecond());
                dismiss();
            } else if (type.equals("startStrict")) {// 年月日时分
                // 如果当前时间大于选择时间，说明错误
                current = DateTimeUtil.getCurrentString_YMDHm();//当前日期 年月日时分
                if (DateTimeUtil.isFirstThenSecond(current, mWheel.getTime())) {
                    Toast.makeText(context, context.getResources().getString(R.string.select_not_less_current), Toast.LENGTH_SHORT).show();
                } else {
//                    dateText.setText(mWheel.getTime());
                    setText(dateText, mWheel.getTime());
                    dismiss();
                }
            } else if (type.equals("startStrict30")) {
                // 如果当前时间大于选择时间，说明错误
                current = DateTimeUtil.getCurrentString_YMDHm();//当前日期 年月日时分
                if (DateTimeUtil.isFirstThenSecond(current, mWheel.getTime())) {
                    Toast.makeText(context, context.getResources().getString(R.string.select_not_less_current), Toast.LENGTH_SHORT).show();
                } else if (DateTimeUtil.unbefore30Day(current, mWheel.getTime())) {
                    Toast.makeText(context, context.getResources().getString(R.string.select_not_great_current), Toast.LENGTH_SHORT).show();
                } else {
                    setText(dateText, mWheel.getTime());
                    dismiss();
                }
            } else if (type.equals("hourmin")) {
//                dateText.setText(mWheel.getHourMinTime());
                setText(dateText, mWheel.getHourMinTime());
                dismiss();
            } else if (type.equals("StartTime_full")) {
                if (DateTimeUtil.isFirstThenSecond(mWheel.getTime(), othetTime_full)) {
                    setText(dateText, mWheel.getTime());
                    Toast.makeText(context, context.getResources().getString(R.string.datefulldialog_info_starttime), Toast.LENGTH_SHORT).show();
                } else {
                    if (!DateTimeUtil.isExceedDay(mWheel.getTime(), othetTime_full)) {
                        Toast.makeText(context, context.getResources().getString(R.string.datefulldialog_starttime), Toast.LENGTH_SHORT).show();
                    } else {
                        setText(dateText, mWheel.getTime());
                        dismiss();
                    }
                }
            } else if (type.equals("EndTime_full")) {
                if (DateTimeUtil.isFirstThenSecond(othetTime_full, mWheel.getTime())) {
                    setText(dateText, mWheel.getTime());
                    Toast.makeText(context, context.getResources().getString(R.string.datefulldialog_info_endtime), Toast.LENGTH_SHORT).show();
                } else {
                    if (!DateTimeUtil.isExceedDay(othetTime_full, mWheel.getTime())) {
                        Toast.makeText(context, context.getResources().getString(R.string.datefulldialog_endtime), Toast.LENGTH_SHORT).show();
                    } else {
                        setText(dateText, mWheel.getTime());
                        dismiss();
                    }
                }
            }

        }
    };

    public void setText(TextView textView, String s) {
        textView.setText(s);
        if (color != 0) {
            textView.setTextColor(color);
        }
    }

    public void show(View v) {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    @Override
    public void onClick(View v) {
        if (v == btn_sure) {
            if (buttonClickListener != null) {
                if ("Hm".equals(type)) {
                    String time = mWheel.getHourMinTime();
                    buttonClickListener.sureButtonOnClickListener(time);
                } else {
                    buttonClickListener.sureButtonOnClickListener("");
                }
            }
            handler.sendEmptyMessage(0);
        } else if (v == btn_cancel) {
            if (buttonClickListener != null) {
                buttonClickListener.cancelButtonOnClickListener();
            }
            dismiss();
        }

    }

    public ButtonClickListener getButtonClickListener() {
        return buttonClickListener;
    }

    public void setButtonClickListener(ButtonClickListener buttonClickListener) {
        this.buttonClickListener = buttonClickListener;
    }


    public void setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
    }


}
