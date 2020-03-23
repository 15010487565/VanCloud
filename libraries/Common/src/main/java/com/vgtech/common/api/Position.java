package com.vgtech.common.api;

/**
 * Created by brook on 2015/10/20.
 */
public class Position extends AbsApiData {
    public String key;  //职位id
    public String value;//职位名
    public boolean isCheck;

    public Position() {
    }

    public Position(String value) {
        this.value = value;
    }
}
