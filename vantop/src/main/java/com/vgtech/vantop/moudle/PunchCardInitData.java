package com.vgtech.vantop.moudle;

import com.google.gson.Gson;
import com.vgtech.common.api.AbsApiData;

/**
 * Created by shilec on 2016/7/15.
 */
public class PunchCardInitData extends AbsApiData{

    private String cardNo;
    private String longlat;
    private String termNo;
    private String times;

    public boolean isNew;
    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getLonglat() {
        return longlat;
    }

    public void setLonglat(String longlat) {
        this.longlat = longlat;
    }

    public String getTermNo() {
        return termNo;
    }

    public void setTermNo(String termNo) {
        this.termNo = termNo;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public static PunchCardInitData fromJson(String json) {
        if(json == null) {
            return new PunchCardInitData();
        }
        PunchCardInitData data =  new Gson().fromJson(json,PunchCardInitData.class);
        return data;
    }
}
