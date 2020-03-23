package com.vgtech.common.api;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonDataFactory {
    final static String LOG_TAG = "TAG_JsonDataFactory";
    private static final Map<String, Class<? extends AbsApiData>> MAP_OBJECTS;
    private static final Map<String, Class<? extends AbsApiData>> ARRAY_OBJECTS;

    static {
        MAP_OBJECTS = new HashMap<String, Class<? extends AbsApiData>>();
        MAP_OBJECTS.put("err", Error.class);
        MAP_OBJECTS.put("depart", Group.class);
        MAP_OBJECTS.put("user", NewUser.class);
        MAP_OBJECTS.put("processer", Processer.class);
        MAP_OBJECTS.put("comment", CommentInfo.class);
        MAP_OBJECTS.put("leader", Processer.class);
        MAP_OBJECTS.put("topic", SharedListItem.class);
        MAP_OBJECTS.put("help", HelpListItem.class);


        ARRAY_OBJECTS = new HashMap<String, Class<? extends AbsApiData>>();
        ARRAY_OBJECTS.put("departs", Group.class);
        ARRAY_OBJECTS.put("comment_list", Comment.class);
        ARRAY_OBJECTS.put("users", User.class);
        ARRAY_OBJECTS.put("receiver", NewUser.class);
        ARRAY_OBJECTS.put("cc_user", NewUser.class);
        ARRAY_OBJECTS.put("industry", IndustrySort.class);
        ARRAY_OBJECTS.put("image", ImageInfo.class);
        ARRAY_OBJECTS.put("audio", AudioInfo.class);
        ARRAY_OBJECTS.put("files", AttachFile.class);
        ARRAY_OBJECTS.put("content", TemplateItem.class);
        ARRAY_OBJECTS.put("options", Option.class);
        ARRAY_OBJECTS.put("properties", Property.class);
        ARRAY_OBJECTS.put("tenants", Tenant.class);
        ARRAY_OBJECTS.put("roles", Role.class);
        ARRAY_OBJECTS.put("records", TradeInfoItem.class);
        ARRAY_OBJECTS.put("resume_list", ResumeBuyBean.class);
        ARRAY_OBJECTS.put("sub_data", Dict.class);
    }

    public static <T extends AbsApiData> T getData(Class<T> c, JSONObject json)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, JSONException {
        T data = c.newInstance();
        data.parser(json);
//        Log.e(JsonDataFactory.LOG_TAG, "datac=" +  (data.toString()));
        if (!data.isValid()) {
            data = null;
        }

        return data;
    }

    public static AbsApiData getData(String key, JSONObject json) {
//        Log.e(JsonDataFactory.LOG_TAG, "key=" + key);
//        Log.e(JsonDataFactory.LOG_TAG, "MAP_OBJECTS=" + MAP_OBJECTS.toString());
        final Class<? extends AbsApiData> c = MAP_OBJECTS.get(key);
//        Log.e(JsonDataFactory.LOG_TAG, "c=" +  (c == null));
        if (c == null) {
            return null;
        }

        AbsApiData data = null;
        try {
            data = getData(c, json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    static Class<? extends AbsApiData> getArrayClass(String key) {
        return ARRAY_OBJECTS.get(key);
    }

    static List<AbsApiData> getDataArray(String key, JSONArray jsonArray) {
        final Class<? extends AbsApiData> c = ARRAY_OBJECTS.get(key);
        if (c == null) {
            return new ArrayList<AbsApiData>();
        }

        final List<AbsApiData> list = new ArrayList<AbsApiData>();
        final int count = jsonArray.length();
        for (int i = 0; i < count; i++) {
            try {
                JSONObject json = jsonArray.getJSONObject(i);
                AbsApiData data = getData(c, json);
                if (data != null) {
                    list.add(data);
                }
            } catch (Exception e) {
            }
        }

        return list;
    }

    public static <T extends AbsApiData> List<T> getDataArray(Class<T> c,
                                                              JSONArray jsonArray) {
        if (c == null||jsonArray == null) {
            return new ArrayList<T>();
        }

        final List<T> list = new ArrayList<T>();
        final int count = jsonArray.length();
        for (int i = 0; i < count; i++) {
            try {
                JSONObject json = jsonArray.getJSONObject(i);
                T data = getData(c, json);
                if (data != null) {
                    list.add(data);
                }
            } catch (Exception e) {
            }
        }

        return list;
    }

    public static RootData getData(JSONObject json) {
        RootData data = null;
        try {
            data = getData(RootData.class, json);
        } catch (Exception e) {
        }
        return data;
    }

    public static RootData getDataOrThrow(JSONObject json)
            throws IllegalArgumentException, InstantiationException,
            IllegalAccessException, JSONException {
        return getData(RootData.class, json);
    }
}
