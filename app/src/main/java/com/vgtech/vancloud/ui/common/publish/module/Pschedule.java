package com.vgtech.vancloud.ui.common.publish.module;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by zhangshaofang on 2015/9/17.
 */
public class Pschedule extends AbsApiData {
    public long startTime;
    public long endTime;
    public int notifyType;
    public long notifyTime;
    public String content;
    public int permission;
    public String receiverIds;
    public int isrepeat = 1;

    public int isclock;
    public String longitude;
    public String latitude;
    public String address;
    public String poiname;
    public String ccUserIds;
}
