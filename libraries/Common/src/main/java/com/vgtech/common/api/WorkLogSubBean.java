package com.vgtech.common.api;

/**
 * Data:  2018/7/27
 * Auther: 陈占洋
 * Description:
 */

public class WorkLogSubBean extends AbsApiData {

    /**
     * staffNo : A0001
     * staffName : 赵某某
     * num : 8
     * duration : 27.0
     * isDone : false
     * doneDate :
     * doneTime :
     */

    private String staffNo;
    private String staffName;
    private int num;
    private String duration;
    private String isDone;
    private String doneDate;
    private String doneTime;

    public String getStaffNo() {
        return staffNo;
    }

    public void setStaffNo(String staffNo) {
        this.staffNo = staffNo;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean getIsDone() {
        return Boolean.parseBoolean(isDone);
    }

    public void setIsDone(String isDone) {
        this.isDone = isDone;
    }

    public String getDoneDate() {
        return doneDate;
    }

    public void setDoneDate(String doneDate) {
        this.doneDate = doneDate;
    }

    public String getDoneTime() {
        return doneTime;
    }

    public void setDoneTime(String doneTime) {
        this.doneTime = doneTime;
    }
}
