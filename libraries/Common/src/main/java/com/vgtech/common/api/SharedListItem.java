package com.vgtech.common.api;

/**
 * Created by Nick on 2015/10/9.
 */
public class SharedListItem extends  AbsApiData{
    public String state;//1 正常，2不存在或已删除
    public String topic;
    public String timestamp;
    public String content;
    public int praises;//点赞数量
    public String topicId;
    public int type;//收藏状态  1收藏 2未收藏
    public int comments;
    public boolean ispraise;//当前用户 是否已赞
    public String address;
    public String latlng;


}
