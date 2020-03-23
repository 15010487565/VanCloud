package com.vgtech.common.api;

import android.text.TextUtils;

/**
 * Created by zhangshaofang on 2015/9/9.
 */
public class ScheduleItem extends AbsApiData {
    public String scheduleid;
    public long starttime;
    public long endtime;
    public String content;

    public String getContent() {
        String str = content;
        if (!TextUtils.isEmpty(str))
            str = str.replaceAll("\n", "<br/>");
        return str;
    }

    public long timestamp;

    public long updatetime;

    public long getUpdateTime() {
        if (updatetime == timestamp) {
            updatetime = 0;
        }
        return updatetime;
    }

    public int permission;
    public String repealstate; //1代表日程状态有效，2代表日程状态撤消
    public String iscanviewdetail;  // 是否可以点击详情 1可以，2不可以

    public String getRepealstate() {
        if ("2".equals(repealstate) || "2".equals(iscanviewdetail))
            return "2";
        else {
            return repealstate;
        }
    }

    public String notifytype;
    public int comments;
    public int praises;
    public boolean ispraise;

    public String replyflag; //日程表新增参与人返回状态字段，-1代表是自己发布的日程没有状态，1待定，2谢绝，3同意，4未处理（默认）
    //新加的一个字段，默认为空
    public String status; //1待定，2谢绝，3同意，4未处理（默认）
    public String signs; //1代表日程状态未过期  2代表日程状态过期
    public String type; //1 我发布的 2 抄送我的 3 其他（查询下属时可能出现）

    public static final int NO_COMPARE = 0;
    public static final int NO_PERMISSION = 1;
    public static final int HAS_PERMISSION = 2;
    //是否有权限去处理此日程
    public int deepPermission;


    public String isclock;
    public String clockaddress;
    public String poiaddress;
    public String longitude;
    public String latitude;

    public String getAddress() {
        String adrName = null;
        if (!TextUtils.isEmpty(poiaddress)) {
            adrName = poiaddress;
        } else if (!TextUtils.isEmpty(clockaddress)) {
            adrName = clockaddress;
        }
        return adrName;
    }
}
