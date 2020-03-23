package com.vgtech.vancloud.ui.common.publish.module;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by zhangshaofang on 2015/9/17.
 */
public class PworkReport extends AbsApiData{

    public int type;
    public long startdate;
    public long enddate;
    public String title;
    public String content;
    public String leader;
    public String receiverids;
    public int templateId;

}
