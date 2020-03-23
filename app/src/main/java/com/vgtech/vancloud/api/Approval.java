package com.vgtech.vancloud.api;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by Duke on 2016/9/23.
 */

public class Approval extends AbsApiData {

    public String processid;
    public String title;
    public String status;
    //-------------新加字段--------------
    public String lea_type;
    public String lea_startTime;
    public String lea_endTime;
    public String lea_shichang;
    public String lea_shichangdanwei;
    //-------------------------------------
    public String ot_startDate;//加班开始时间
    public String ot_endDate;//加班结束时间
    public String ot_dshichang;//加班时长（天）
    public String ot_shichang;//加班时长
    public String ot_time;
    //-------------------------------------
    public String car_time;//签卡时间
    //-------------------------------------
    public String type;//1普通审批。2请假。5招聘计划
    public String photo;
    public String processState;
    public long timestamp;
    public boolean canHasten;
    public String hastenMsg;

    public String classType;//LEA休假。OT加班。CAR签卡
    public String staffNo;

    public String supervisorStaffNo;
    public String sendUserId;

    public boolean hasTenState = true;//false 催办按钮置灰
    public boolean isCheck;//批量选中
    public String changeStatusType;//1表示撤销，2表示变更

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    @Override
    public String toString() {
        return "{" +
                "title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", lea_type='" + lea_type + '\'' +
                ", lea_startTime='" + lea_startTime + '\'' +
                ", lea_endTime='" + lea_endTime + '\'' +
                ", type='" + type + '\'' +
                ", isCheck='" + isCheck + '\'' +
                '}';
    }
}
