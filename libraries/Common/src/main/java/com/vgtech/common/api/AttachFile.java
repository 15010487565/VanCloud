package com.vgtech.common.api;

/**
 * Created by John on 2015/9/6.
 */
public class AttachFile extends AbsApiData  {

    //文件ID
    public String fid;
    //文件URL
    public String url;
    //文件名
    public String name;

    public AttachFile() {
    }

    public AttachFile(String fid, String url, String name) {
        this.fid = fid;
        this.url = url;
        this.name = name;
    }
}
