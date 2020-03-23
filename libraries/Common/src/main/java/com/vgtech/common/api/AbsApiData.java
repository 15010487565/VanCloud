package com.vgtech.common.api;

import android.text.TextUtils;
import android.util.Log;

import com.vgtech.common.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbsApiData {
    private Map<Class<? extends AbsApiData>, AbsApiData> mDatas;
    private Map<Class<? extends AbsApiData>, List<? extends AbsApiData>> mArrays;
    private JSONObject mJson;

    public AbsApiData() {

    }

    public void parser(JSONObject json) throws JSONException,
            IllegalArgumentException, IllegalAccessException {
        mJson = json;
        Iterator<?> keyIterator = json.keys();
        List<String> keyList = new ArrayList<String>();
        while (keyIterator.hasNext()) {
            String key = (String) keyIterator.next();
            keyList.add(key);
        }

        Field[] fields = getClass().getDeclaredFields();
        Map<String, Field> fieldMap = new HashMap<String, Field>();
        Set<String> fieldNames = new HashSet<String>();
        for (Field field : fields) {
            String name = field.getName();
            name = name.substring(name.lastIndexOf(".") + 1);
            fieldNames.add(name);
            fieldMap.put(name, field);

        }

        for (String key : keyList) {
            Object object = json.get(key);
            if (object instanceof JSONObject) {
                AbsApiData data = JsonDataFactory.getData(key,
                        (JSONObject) object);
                if (data != null) {
                    getDatas().put(data.getClass(), data);
                } else {
                    if (Constants.DEBUG)
                        Log.e(JsonDataFactory.LOG_TAG, "A JSONObject not be parser:\n" + object);
                }
            } else if (object instanceof JSONArray) {

                List<AbsApiData> array = JsonDataFactory.getDataArray(key,
                        (JSONArray) object);
                if (array != null) {
                    getArrayDatas().put(JsonDataFactory.getArrayClass(key),
                            array);
                } else {
                    if (Constants.DEBUG)
                        Log.e(JsonDataFactory.LOG_TAG,
                                "A JSONArray not be parser:\nKey:" + key
                                        + ", JSON:" + object);
                }
            } else {
                Field field = fieldMap.get(key);
                if (field != null) {
                    if ((field.getType() == boolean.class || field.getType() == Boolean.class)
                            && !(object instanceof Boolean)) {
                        object = json.getInt(key) == 0;
                    } else if ((field.getType() == long.class || field
                            .getType() == Long.class)
                            && !(object instanceof Long)) {
                        object = json.getLong(key);
                    } else if (field.getType() == Integer.class
                            || field.getType() == int.class) {
                        Object intObject = json.get(key);
                        if (!TextUtils.isEmpty(intObject.toString()))
                            object = json.getInt(key);
                    } else if (field.getType() == String.class) {
                        object = json.get(key).toString();
                    } else if (field.getType() == Float.class || field.getType() == float.class) {
                        String v = json.get(key).toString();
                        object = Float.parseFloat(v);
                    }
                    if (object == JSONObject.NULL) {
                        object = null;
                    }
                    try {
                        field.setAccessible(true);
                        field.set(this, object);
                    } catch (Exception e) {
                        // System.out.println(field + "---:--" + object);
                    }
                }
            }
        }
    }

    public boolean isValid() {
        return true;
    }

    public Map<Class<? extends AbsApiData>, AbsApiData> getDatas() {
        if (mDatas == null) {
            mDatas = new HashMap<Class<? extends AbsApiData>, AbsApiData>();
        }
        return mDatas;
    }

    public Map<Class<? extends AbsApiData>, List<? extends AbsApiData>> getArrayDatas() {
        if (mArrays == null) {
            mArrays = new HashMap<Class<? extends AbsApiData>, List<? extends AbsApiData>>();
        }
        return mArrays;
    }

    private void typeWarning(Object value, Class<? extends AbsApiData> c,
                             ClassCastException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Try get a ");
        sb.append(c.getName());
        sb.append(", but value was a ");
        sb.append(value.getClass().getName());
        sb.append(".");
        if (Constants.DEBUG) {
            Log.e(JsonDataFactory.LOG_TAG, sb.toString());
            Log.e(JsonDataFactory.LOG_TAG,
                    "Attempt to cast generated internal exception:", e);
        }

    }

    @SuppressWarnings("unchecked")
    public <T extends AbsApiData> T getData(Class<T> c) {
        Map<Class<? extends AbsApiData>, AbsApiData> datas = getDatas();
        AbsApiData data = datas.get(c);
        if (data == null) {
            return null;
        }
        try {
            return (T) data;
        } catch (ClassCastException e) {
            typeWarning(data, c, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends AbsApiData> List<T> getArrayData(Class<T> c) {
        Map<Class<? extends AbsApiData>, List<? extends AbsApiData>> arrays = getArrayDatas();
        List<? extends AbsApiData> list = arrays.get(c);
        if (list == null) {
            return new ArrayList<T>();
        }
        try {
            return (List<T>) list;
        } catch (ClassCastException e) {
            typeWarning(list, c, e);
            return new ArrayList<T>();
        }
    }

    public JSONObject getJson() {
        return mJson;
    }

    public void setJson(JSONObject json) {
        mJson = json;
    }
}
