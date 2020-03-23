package com.vgtech.vancloud.ui.common.publish.module;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by zhangshaofang on 2015/9/18.
 */
public class Pannouncement extends AbsApiData {
    public String title;
    public String content;
    public String receiverids;
    public String attachment;
    public boolean isTop;
    public boolean isSend;
}
