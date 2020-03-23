package com.vgtech.vantop.moudle;

import com.vgtech.common.api.AbsApiData;

import java.io.Serializable;

/**
 * Created by shilec on 2016/9/7.
 */

/***
 * "date": "2016-06-01",
 "inTime": "",
 "inTimeMid": "",
 "isDetail": true,
 "isException": true,
 "outTime": "",
 "outTimeMid": "",
 "shiftCode": "N",
 "shiftName": "正常班(4次卡)",
 "staffNo": "D0029",
 "timeNum": "4",
 "week": "(三)"
 */
public class ClockInListData extends AbsApiData implements Serializable{
    private String date;
    private String inTime;
    private String inTimeMid;
    private String isDetail;
    private String isException;
    private String outTime;
    private String outTimeMid;
    private String shiftCode;
    private String shiftName;
    private String staffNo;
    private String timeNum;
    private String week;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getInTimeMid() {
        return inTimeMid;
    }

    public void setInTimeMid(String inTimeMid) {
        this.inTimeMid = inTimeMid;
    }

    public String getIsDetail() {
        return isDetail;
    }

    public void setIsDetail(String isDetail) {
        this.isDetail = isDetail;
    }

    public String getIsException() {
        return isException;
    }

    public void setIsException(String isException) {
        this.isException = isException;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public String getOutTimeMid() {
        return outTimeMid;
    }

    public void setOutTimeMid(String outTimeMid) {
        this.outTimeMid = outTimeMid;
    }

    public String getShiftCode() {
        return shiftCode;
    }

    public void setShiftCode(String shiftCode) {
        this.shiftCode = shiftCode;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public String getStaffNo() {
        return staffNo;
    }

    public void setStaffNo(String staffNo) {
        this.staffNo = staffNo;
    }

    public String getTimeNum() {
        return timeNum;
    }

    public void setTimeNum(String timeNum) {
        this.timeNum = timeNum;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    @Override
    public String toString() {
        return "ClockInListData{" +
                "date='" + date + '\'' +
                ", inTime='" + inTime + '\'' +
                ", inTimeMid='" + inTimeMid + '\'' +
                ", isDetail='" + isDetail + '\'' +
                ", isException='" + isException + '\'' +
                ", outTime='" + outTime + '\'' +
                ", outTimeMid='" + outTimeMid + '\'' +
                ", shiftCode='" + shiftCode + '\'' +
                ", shiftName='" + shiftName + '\'' +
                ", staffNo='" + staffNo + '\'' +
                ", timeNum='" + timeNum + '\'' +
                ", week='" + week + '\'' +
                '}';
    }
}
