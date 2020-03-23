package com.vgtech.common.api;

/**
 * Created by app02 on 2015/9/9.
 */
public class AnnounceNotify extends AbsApiData {

    public String notifyid;
    public String title;
    public String content;
    public int comments;
    public int praises;
    public long timestamp;
    public String from;

    //已读未读
    public String isread;
    //是否点过赞
    public boolean ispraise;
    //公告类型，草稿/已发布
    public String type;
    public boolean ishigh;
}
