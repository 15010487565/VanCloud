package com.vgtech.common.api;

/**
 * Created by Duke on 2015/9/7.
 */
public class CommentInfo extends AbsApiData {

    public String content;
    public String state;//工作汇报（1-未点评，2-已点评）
    public long timestamp;

}
