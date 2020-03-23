package com.vgtech.vantop.moudle;

import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by shilec on 2016/9/14.
 */
public class VantopShowUserInfoData {

    private static final String TAG = "ShowData";
    public String avatar_b64;
    public String avatar_big_url;
    public String avatar_url;

    @SerializedName("_fields")
    public List<VantopUserInfoFieldsData> fields;
    public boolean hasXmpp;
    @SerializedName("_fixedFields")
    public Map<String, VantopUserInfoFieldsData> fixedFields;

    public VantopShowUserInfoData() {
        fields = new ArrayList<>();
        fixedFields = new HashMap<>();
    }

    @Override
    public String toString() {
        return "VantopShowUserInfoData{" +
                "avatar_b64='" + avatar_b64 + '\'' +
                ", avatar_big_url='" + avatar_big_url + '\'' +
                ", avatar_url='" + avatar_url + '\'' +
                ", \n fields=" + fields +
                ", \n hasXmpp=" + hasXmpp +
                ",\n fixedFields=" + fixedFields +
                '}';
    }

    public static final List<String> FIXED_KEYS = new ArrayList<>();

    static {
        FIXED_KEYS.add("staff_name");
        FIXED_KEYS.add("english_name");
        FIXED_KEYS.add("sex");
//        FIXED_KEYS.add("age");
        FIXED_KEYS.add("date_of_birth");
        FIXED_KEYS.add("staff_no");
        FIXED_KEYS.add("position");
//        FIXED_KEYS.add("role_name");
        FIXED_KEYS.add("department");
//        FIXED_KEYS.add("section");
        FIXED_KEYS.add("supervisor_name");
        FIXED_KEYS.add("mobile_phone");
        FIXED_KEYS.add("e_mail");
    }

    public static VantopShowUserInfoData fromJson(boolean isChinese,String json) {
        VantopShowUserInfoData data = new VantopShowUserInfoData();
        try {
            JSONObject jobj = new JSONObject(json);
            data.avatar_b64 = jobj.optString("avatar_b64");
            data.avatar_big_url = jobj.optString("avatar_big_url");
            data.avatar_url = jobj.optString("avatar_url");
            data.hasXmpp = jobj.optBoolean("hasXmpp");

            JSONArray jArr = jobj.optJSONArray("fields");
//            if (jArr == null || jArr.length() == 0) {
//                return null;
//            }
            for (int i = 0; i < jArr.length(); i++) {
                data.fields.add(VantopUserInfoFieldsData.
                        fromJson(isChinese,null, jArr.optString(i)));
            }

            jobj = jobj.optJSONObject("fixedFields");
            Iterator<String> kIter = jobj.keys();
            while(kIter.hasNext()) {
                String key = kIter.next();
                String value = jobj.opt(key).toString();
                data.fixedFields.put(key, VantopUserInfoFieldsData.fromJson(isChinese,key, value));
            }
            return data;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return data;
        }
        return null;
    }

    public VantopUserInfoFieldsData getAge() {
        return fixedFields.get("age");
    }

    public VantopUserInfoFieldsData getBirth() {
        return fixedFields.get("date_of_birth");
    }

    public VantopUserInfoFieldsData getEmail() {
        return fixedFields.get("e_mail");
    }

    public VantopUserInfoFieldsData getPhone() {
        return fixedFields.get("mobile_phone");
    }
    public VantopUserInfoFieldsData getPosition() {
        return fixedFields.get("position");
    }

    public VantopUserInfoFieldsData getPosistion() {
        return fixedFields.get("position");
    }

    public VantopUserInfoFieldsData getSex() {
        return fixedFields.get("sex");
    }

    public VantopUserInfoFieldsData getStaffName() {
        return fixedFields.get("staff_name");
    }

    public VantopUserInfoFieldsData getStaffNo() {
        return fixedFields.get("staff_no");
    }

}
