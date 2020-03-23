package com.vgtech.vantop.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vgtech.vantop.moudle.StaffInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Duke on 2016/9/26.
 */

public class GsonUtils {


    public static Map<String, String> parseDataToMap(String data) {
        Gson g = new Gson();
        Map<String, String> map = g.fromJson(data, new TypeToken<Map<String, String>>() {
        }.getType());
        return map;
    }


    public static ArrayList<Map<String, String>> parseDataToArrayList(String data) {
        Gson gson = new Gson();
        ArrayList<Map<String, String>> list = gson.fromJson(data, new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType());
        return list;
    }

    public static List<String> parseDataToList(String data) {
        Gson g = new Gson();
        List<String> list = g.fromJson(data, new TypeToken<List<String>>() {
        }.getType());
        return list;
    }

    public static String parseListToJson(List<StaffInfo> list) {
        Gson g = new Gson();
        String s = g.toJson(list);
        return s;
    }
}
