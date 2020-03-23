package com.vgtech.vancloud.ui.common.publish.module;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by zhangshaofang on 2015/9/18.
 */
public class Pflow extends AbsApiData{
    public String content;
    public String receiverids;
    public String processerid;
    public long startTime;
    public long endTime;
    public int leaveType;
    public int leaveTime;
}
