package com.vgtech.common.api;

/**
 * Created by zhangshaofang on 2016/5/23.
 */
public class Dict extends AbsApiData {
    public String id;
    public String pid;
    public String name;
    public String title;
    public String jobTitle;
    public String referCode;
    public int subSelect;
    public Dict pDict;
    public boolean isAll;

    public String getId() {
        return id == null ? "" : id;
    }

    public String getName() {
        return name == null ? "" : name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dict dict = (Dict) o;
        if (!id.equals(dict.id))
            return false;
        return true;

    }
}
