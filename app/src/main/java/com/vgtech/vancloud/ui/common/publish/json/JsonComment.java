package com.vgtech.vancloud.ui.common.publish.json;


import java.util.List;

/**
 * Created by zhangshaofang on 2016/1/20.
 */
public class JsonComment {
    public List<JsonImage> image;
    public List<JsonAudio> audio;
    public String commentid;
    public String content;
    public String timestamp;
    public JsonUser user;
    public JsonUser replyuser;
}
