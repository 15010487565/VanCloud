package com.vgtech.vantop.moudle;

/**
 * Created by shilec on 2016/9/8.
 */

import com.vgtech.common.api.AbsApiData;

import java.util.List;

/***
 * _code": "0",
 "_msg": "",
 "data": {
 "appealVisiable": true,
 "attList": [
 "10:09"
 ],
 "attStatus": [
 {
 "colorFlag": 0,
 "statusValue": "无外出时间 1.0 次"
 },
 {
 "colorFlag": 0,
 "statusValue": "无返回时间 1.0 次"
 },
 {
 "colorFlag": -1,
 "statusValue": "迟到 99.0 分钟"
 },
 {
 "colorFlag": -1,
 "statusValue": "迟到60分钟以上 1.0 次"
 },
 {
 "colorFlag": -1,
 "statusValue": "缺勤 1.0 小时"
 }
 ],
 "date": "2016-06-01",
 "exceptionExplain": "工作原因",
 "inTime": "10:09",
 "inTimeMid": "",
 "outTime": "",
 "outTimeMid": "",
 "shiftCode": "N",
 "shiftName": "正常班(4次卡)",
 "signCardVisiable": true,
 "staffNo": "D0019",
 "timeNum": "4",
 "week": "(三)"
 */
public class ClockInDetailData extends AbsApiData{
    public boolean appealVisiable;
    public List<String> attList;
    public String date;
    public String exceptionExplain;
    public String inTime;
    public String inTimeMid;
    public String outTime;
    public String outTimeMid;
    public String shiftCode;
    public String shiftName;
    public boolean signCardVisiable;
    public String staffNo;
    public int timeNum;
    public String week;
    public List<ClockInAttStatusData> attStatus;

    @Override
    public String toString() {
        return "ClockInDetailData{" +
                "appealVisiable=" + appealVisiable +
                ", attList=" + attList +
                ", date='" + date + '\'' +
                ", exceptionExplain='" + exceptionExplain + '\'' +
                ", inTime='" + inTime + '\'' +
                ", inTimeMid='" + inTimeMid + '\'' +
                ", outTime='" + outTime + '\'' +
                ", outTimeMid='" + outTimeMid + '\'' +
                ", shiftCode='" + shiftCode + '\'' +
                ", shiftName='" + shiftName + '\'' +
                ", signCardVisiable=" + signCardVisiable +
                ", staffNo='" + staffNo + '\'' +
                ", timeNum=" + timeNum +
                ", week='" + week + '\'' +
                ", attStatus=" + attStatus +
                '}';
    }
}
