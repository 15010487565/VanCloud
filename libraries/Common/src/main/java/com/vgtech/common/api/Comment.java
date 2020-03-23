package com.vgtech.common.api;

/**
 * 回复（评论）信息
 * Created by Duke on 2015/8/17.
 */
public class Comment extends AbsApiData {
    /**
     * 评论id
     */
    public String commentid;
    /**
     * 内容
     */
    public String content;
    /**
     * 创建时间戳
     */
    public long timestamp;


}
