package com.vgtech.vancloud.ui.common.publish.module;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by zhangshaofang on 2015/9/17.
 */
public class Ptask extends AbsApiData {

    public String content;
    public long planFinishTime;
    public int notifyType;
    public long notifyTime;
    public String processerId;
    public String receiverIds;
}
