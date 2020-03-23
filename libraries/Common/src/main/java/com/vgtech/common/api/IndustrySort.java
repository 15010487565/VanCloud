package com.vgtech.common.api;

/**
 * Created by code on 2016/1/12.
 */
public class IndustrySort extends AbsApiData {
    public String id;
    public String name;

    public IndustrySort() {
    }

    public IndustrySort(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
