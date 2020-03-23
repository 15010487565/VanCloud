package com.vgtech.common.api;

/**
 * Created by zhangshaofang on 2015/9/18.
 */
public class MapItem extends AbsApiData {
    public String key;
    public String value;

    public MapItem() {
    }

    public MapItem(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
