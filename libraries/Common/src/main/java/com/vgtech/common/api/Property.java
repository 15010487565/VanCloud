package com.vgtech.common.api;

/**
 * Created by zhangshaofang on 2015/9/30.
 */
public class Property extends AbsApiData{
    public int id;
    public String name;
    public String value;
    public int type;//0文本，1数字，2日期，3选择
    public boolean edit;//是否可编辑（0否，1可编辑）默认0

    public int subType;//1,部门，2职位，3角色，4上级

    public Property(){}

    public Property(String name, String value, int subType, boolean edit) {
        this.name = name;
        this.value = value;
        this.subType = subType;
        this.edit = edit;
    }
}