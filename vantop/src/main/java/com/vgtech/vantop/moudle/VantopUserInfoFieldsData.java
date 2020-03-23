package com.vgtech.vantop.moudle;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by shilec on 2016/9/14.
 */
public class VantopUserInfoFieldsData {
    public String label;
    public String name;
    public boolean status;
    public String value;
    public String type;
    public boolean isEdit;
    private static final String TAG = "FieldData";

    @SerializedName("edit_values")
    public Map<String, String> values;
    public String supervisor_name;

    @Override
    public String toString() {
        return "VantopUserInfoFieldsData{" +
                "label='" + label + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", value='" + value + '\'' +
                ", type='" + type + '\'' +
                ", isEdit=" + isEdit +
                ", values=" + values +
                '}';
    }

    public VantopUserInfoFieldsData() {
        values = new HashMap<>();
    }

    public static VantopUserInfoFieldsData fromJson(boolean isChinese, String key1, String json) {

        VantopUserInfoFieldsData data = new VantopUserInfoFieldsData();
        //Gson gson = new Gson();
        //data = gson.fromJson(json, VantopUserInfoFieldsData.class);
        try {
            JSONObject jObj = new JSONObject(json);
            data.label = jObj.optString("label");
            if (isChinese && !TextUtils.isEmpty(data.label)) {
            /*    if ("staff_name".equals(key1)) {
                    data.label = data.label.replace("员工姓名", "姓名");
                } else*/ if ("supervisor_name".equals(key1)) {
                    data.label = data.label.replace("上级主管", "上级领导");
                } else if ("e_mail".equals(key1)) {
                    data.label = data.label.replace("电子邮件", "电子邮箱");
                }
            }
            data.name = jObj.optString("name");
            data.status = jObj.optBoolean("status");
            data.type = jObj.optString("type");
            data.value = jObj.optString("value");
            if (TextUtils.equals("supervisor_name", key1)) {
                data.supervisor_name = jObj.optString("supervisor_name");
                data.value = jObj.optString("supervisor");
            }
           /* if (false*//*TextUtils.equals("role_name", key1)*//*) {
                JSONArray jArr = jObj.optJSONArray("value");
                data.value = "";
                for(int i = 0; i < jArr.length(); i++) {
                    data.value += jArr.optString(i);
                    if(i != jArr.length() - 1) {
                        data.value += ",";
                    }
                }
            } else {*/
            //}

            jObj = jObj.optJSONObject("values");
            if (jObj != null) {
                Iterator<String> keys = jObj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = "";
                    value = jObj.opt(key).toString();
                    data.values.put(key, value);
                }
            }
            return data;
        } catch (JSONException e) {
            e.printStackTrace();
            return data;
        }
    }
}
