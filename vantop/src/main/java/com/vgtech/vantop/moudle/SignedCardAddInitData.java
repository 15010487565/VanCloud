package com.vgtech.vantop.moudle;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shilec on 2016/7/20.
 */
public class SignedCardAddInitData {

    public String cardNo;
    public String staffNo;

    public CommonValue reason;
    public CommonValue termNo;

    private SignedCardAddInitData() {

        reason = new CommonValue();
        termNo = new CommonValue();
    }

    public static SignedCardAddInitData fromJson(String json) {

        SignedCardAddInitData data = new SignedCardAddInitData();
        try {
            JSONObject jObj = new JSONObject(json);
            data.cardNo = jObj.optString("cardNo");
            data.staffNo = jObj.optString("staffNo");

            data.reason = CommonValue.fromJson(jObj.optString("reason"));
            data.termNo = CommonValue.fromJson(jObj.optString("termNo"));

            return data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static class CommonValue {

        public String defaultValue;
        public Map<String,String> values;


        private CommonValue() {

            values = new HashMap<>();
        }

        public static CommonValue fromJson(String json) {

            CommonValue comValue = new CommonValue();
            try {
                JSONObject jObj = new JSONObject(json);
                comValue.defaultValue = jObj.optString("defaultValue");

                jObj = jObj.optJSONObject("values");
                String str = jObj.toString();
                str = str.substring(1,str.length()-1);
                String[] keyAndValues = str.split(",");
                for(String kv : keyAndValues) {
                    if (TextUtils.isEmpty(kv)){
                        continue;
                    }
                    String key = kv.split(":")[0];
                    String value = kv.split(":")[1];
                    key = key.replace("\"","");
//                    key = key.replace("{","");
                    value = value.replace("\"","");
//                    value = value.replace("}","");

                    comValue.values.put(key,value);
                }

                return comValue;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return comValue;
        }
    }

}
