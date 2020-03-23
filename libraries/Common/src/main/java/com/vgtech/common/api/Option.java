package com.vgtech.common.api;

/**
 * Created by zhangshaofang on 2015/9/30.
 */
public class Option extends AbsApiData {
    public String id;
    public String name;
    public String type;
    public Option() {
    }

    public Option(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public Option(String id, String name,String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
}
