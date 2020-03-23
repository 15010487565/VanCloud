package com.vgtech.vancloud.ui.chat.controllers;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TimePicker;

import com.vgtech.common.PrfUtils;
import com.vgtech.vancloud.R;

import java.util.Calendar;
import java.util.Locale;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PickerController {

    public static String[] months = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

    public PickerController(final Context context) {
        this.context = context;
    }


    public void pickerDate(final DialogInterface.OnClickListener listener, final Locale locale) {
        Calendar cal = Calendar.getInstance();
        final DatePickerDialog pickerDialog = new DatePickerDialog(context,
                R.style.PickerDialog, null,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)) {

            @Override
            public void onDateChanged(DatePicker picker, int year, int month, int day) {
                super.onDateChanged(picker, year, month, day);
                String str = String.format("%d-%02d-%02d", picker.getYear(), picker.getMonth() + 1, picker.getDayOfMonth());
                setTitle(context.getString(R.string.pickerDate) + ": " + str);
                if (!PrfUtils.isChineseForAppLanguage(context)) {
                    ((NumberPicker) ((ViewGroup) ((ViewGroup) picker.getChildAt(0)).getChildAt(0)).getChildAt(0)).setDisplayedValues(months);
                } else {
                    ((NumberPicker) ((ViewGroup) ((ViewGroup) picker.getChildAt(0)).getChildAt(0)).getChildAt(1)).setDisplayedValues(months);
                }
            }
        };
        DatePicker picker = pickerDialog.getDatePicker();
        String str = String.format("%d-%02d-%02d", picker.getYear(), picker.getMonth() + 1, picker.getDayOfMonth());
        pickerDialog.setTitle(context.getString(R.string.pickerDate) + ": " + str);
        pickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok), listener);
        pickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        pickerDialog.show();
        if (!PrfUtils.isChineseForAppLanguage(context)) {
            ((NumberPicker) ((ViewGroup) ((ViewGroup) picker.getChildAt(0)).getChildAt(0)).getChildAt(0)).setDisplayedValues(months);
        } else {
            ((NumberPicker) ((ViewGroup) ((ViewGroup) picker.getChildAt(0)).getChildAt(0)).getChildAt(1)).setDisplayedValues(months);
        }
    }

    public void pickerTime(final DialogInterface.OnClickListener listener) {
        Calendar cal = Calendar.getInstance();
        final TimeSelectDialog pickerDialog = new TimeSelectDialog(context,
                R.style.PickerDialog, null, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
        pickerDialog.setTitle(context.getString(R.string.pickerTime));
        pickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.confirm), listener);
        pickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        pickerDialog.show();
    }

    public static class TimeSelectDialog extends TimePickerDialog {
        public int hour, minute;

        public TimeSelectDialog(Context context, int theme, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
            super(context, theme, callBack, hourOfDay, minute, is24HourView);
            this.hour = hourOfDay;
            this.minute = minute;
        }

        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            super.onTimeChanged(view, hourOfDay, minute);
            this.hour = hourOfDay;
            this.minute = minute;
        }

    }

    ;

    Context context;
}
