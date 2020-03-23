package com.vgtech.vantop.moudle;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.vgtech.vantop.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shilec on 2016/7/19.
 */
public class PunchCardListData implements Parcelable {
    public JSONObject jsonObject;
    /**
     * "address": "北京市海淀区花园北路37-3号",
     * "cardNo": "28",
     * "date": "2016-07-12",
     * "pictures": "http://192.168.3.154:91/upload/20160712/1468289791479-98951.jpg",
     * "remark": "啦啦啦",
     * "staffNo": "SX28",
     * "time": "10:16"
     */
    private String address;
    private String cardNo;
    private String date;
    private String pictures;
    private String remark;
    private String staffNo;
    private String time;
    private String longitude;
    private String latitude;

    private String flag;//打卡类型


    public int getType() {

        int resId = -1;
        if ("2".equals(flag)) {
            resId = R.string.schedule_outlook_signcard;
        } else if ("1".equals(flag)) {
            resId = R.string.leave_outlook_signcard;
        }
        return resId;
    }

    public void setType(String type) {
        this.flag = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPictures() {
        return pictures;
    }

    public void setPictures(String pictures) {
        this.pictures = pictures;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStaffNo() {
        return staffNo;
    }

    public void setStaffNo(String staffNo) {
        this.staffNo = staffNo;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public static List<PunchCardListData> fromJson(String json) {

        List<PunchCardListData> datas = new ArrayList<>();

        try {
            JSONObject jObj = new JSONObject(json);
            if (jObj == null) {
                return datas;
            }
            JSONArray jArr = jObj.optJSONArray("data");
            if (jArr == null || jArr.length() == 0) {
                return datas;
            }
            for (int i = 0; i < jArr.length(); i++) {
                jObj = jArr.optJSONObject(i);
                PunchCardListData data = new Gson().fromJson(jObj.toString()
                        , PunchCardListData.class);
                data.jsonObject = jObj;
                datas.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return datas;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public PunchCardListData() {
    }

    protected PunchCardListData(Parcel in) {
        address = in.readString();
        cardNo = in.readString();
        date = in.readString();
        pictures = in.readString();
        remark = in.readString();
        staffNo = in.readString();
        time = in.readString();
        flag = in.readString();
        longitude = in.readString();
        latitude = in.readString();
    }

    public static final Creator<PunchCardListData> CREATOR = new Creator<PunchCardListData>() {
        @Override
        public PunchCardListData createFromParcel(Parcel in) {
            return new PunchCardListData(in);
        }

        @Override
        public PunchCardListData[] newArray(int size) {
            return new PunchCardListData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(cardNo);
        dest.writeString(date);
        dest.writeString(pictures);
        dest.writeString(remark);
        dest.writeString(staffNo);
        dest.writeString(time);
        dest.writeString(flag);
        dest.writeString(longitude);
        dest.writeString(latitude);
    }
}
