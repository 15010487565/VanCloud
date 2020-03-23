package com.vgtech.vantop.utils;

import android.content.Context;

import com.vgtech.vantop.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Brook on 2016/3/16.
 */
public class MapUtils {
    public static List<String> getMapValues(Map<String, String> map) {
        List<String> list = new ArrayList<>();
        if (map != null) {
            for (String value : map.values()) {
                list.add(value);
            }
        }
        return list;
    }

    public static List<String> getMapKeys(Map<String, String> map) {
        List<String> list = new ArrayList<>();
        if (map != null) {
            for (String value : map.keySet()) {
                list.add(value);
            }
        }
        return list;
    }

    public static List<String> getdeductHourValues(Context context) {
        ArrayList<String> values = new ArrayList<>();
        String min = context.getString(R.string.vantop_minute);
        String hour = context.getString(R.string.vantop_hour);
        values.add("0.0");
        values.add("15"+min);
        values.add("20"+min);
        values.add("30"+min);
        values.add("40"+min);
        values.add("45"+min);
        values.add("1.0"+hour);
        values.add("1.5"+hour);
        values.add("1.75"+hour);
        values.add("2.0"+hour);
        values.add("2.5"+hour);
        values.add("3.0"+hour);
        values.add("3.5"+hour);
        values.add("4.0"+hour);
        values.add("4.5"+hour);
        values.add("5.0"+hour);
        return values;
    }


    public static List<String> getdeductHourKeys(Context context) {
        ArrayList<String> Keys = new ArrayList<>();
        //String min = context.getString(R.string.vantop_minute);
        //String hour = context.getString(R.string.vantop_hour);
        Keys.add("0.0");
        Keys.add("0.25");
        Keys.add("0.33");
        Keys.add("0.5");
        Keys.add("0.67");
        Keys.add("0.75");
        Keys.add("1.0");
        Keys.add("1.5");
        Keys.add("1.75");
        Keys.add("2.0");
        Keys.add("2.5");
        Keys.add("3.0");
        Keys.add("3.5");
        Keys.add("4.0");
        Keys.add("4.5");
        Keys.add("5.0");
        return Keys;
    }

}
