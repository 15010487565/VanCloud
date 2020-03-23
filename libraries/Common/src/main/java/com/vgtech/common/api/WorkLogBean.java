package com.vgtech.common.api;

import java.io.Serializable;

/**
 * Data:  2018/7/4
 * Auther: 陈占洋
 * Description:
 */

public class WorkLogBean extends AbsApiData implements Serializable{


    /**
     * staffNo : A0001
     * staffName :
     * dates : 2018-08-01
     * logId : 1
     * fromTime : 11:51
     * toTime : 13:51
     * duration : 2.00
     * workLocation : 测试地点1
     * relatedPerson : 测试相关人1
     * workBrief : 测试工作描述1
     * workContent : 测试详细内容1
     * modUser : A0001
     * modDate : 2018-08-01
     * modTime : 115324
     * costCode : 10
     * costName : 管理中心
     * myReflections : 测试感想1
     * imageUrl : http://192.168.3.173:91/work_log/image/A0001/2018-08-01/1
     * fileExt : jpg
     */

    private String staffNo;
    private String staffName;
    private String dates;
    private String logId;
    private String fromTime;
    private String toTime;
    private String duration;
    private String workLocation;
    private String relatedPerson;
    private String workBrief;
    private String workContent;
    private String modUser;
    private String modDate;
    private String modTime;
    private String costCode;
    private String costName;
    private String myReflections;
    private String imageUrl;
    private String fileExt;
    private boolean showDetail;
    private boolean onlyStaff;

    public boolean isOnlyStaff() {
        return onlyStaff;
    }

    public void setOnlyStaff(boolean onlyStaff) {
        this.onlyStaff = onlyStaff;
    }

    public boolean isShowDetail() {
        return showDetail;
    }

    public void setShowDetail(boolean showDetail) {
        this.showDetail = showDetail;
    }

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

    public String getDates() {
        return dates;
    }

    public void setDates(String dates) {
        this.dates = dates;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(String workLocation) {
        this.workLocation = workLocation;
    }

    public String getRelatedPerson() {
        return relatedPerson;
    }

    public void setRelatedPerson(String relatedPerson) {
        this.relatedPerson = relatedPerson;
    }

    public String getWorkBrief() {
        return workBrief;
    }

    public void setWorkBrief(String workBrief) {
        this.workBrief = workBrief;
    }

    public String getWorkContent() {
        return workContent;
    }

    public void setWorkContent(String workContent) {
        this.workContent = workContent;
    }

    public String getModUser() {
        return modUser;
    }

    public void setModUser(String modUser) {
        this.modUser = modUser;
    }

    public String getModDate() {
        return modDate;
    }

    public void setModDate(String modDate) {
        this.modDate = modDate;
    }

    public String getModTime() {
        return modTime;
    }

    public void setModTime(String modTime) {
        this.modTime = modTime;
    }

    public String getCostCode() {
        return costCode;
    }

    public void setCostCode(String costCode) {
        this.costCode = costCode;
    }

    public String getCostName() {
        return costName;
    }

    public void setCostName(String costName) {
        this.costName = costName;
    }

    public String getMyReflections() {
        return myReflections;
    }

    public void setMyReflections(String myReflections) {
        this.myReflections = myReflections;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }
}
