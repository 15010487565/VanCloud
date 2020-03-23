package com.vgtech.vancloud.ui.common.publish.module;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by zhangshaofang on 2015/9/22.
 */
public class Pcomment extends AbsApiData {
    public String content;
    public int commentType;
    public String commentId;
    public String replyuserid;//被回复人用户编号，可为空
    public String replayUser;

}
