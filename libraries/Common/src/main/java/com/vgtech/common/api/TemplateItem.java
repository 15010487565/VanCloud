package com.vgtech.common.api;

/**
 * Created by zhangshaofang on 2015/9/17.
 */
public class TemplateItem extends AbsApiData {
    public int id;
    public String title;
    public String content;

    public TemplateItem() {
    }

    public TemplateItem(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
