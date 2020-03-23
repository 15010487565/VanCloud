package com.vgtech.common.api;

/**
 * Created by vic on 2016/9/28.
 */
public class UserProperty extends AbsApiData{
    public enum Type{
        sex,birthday,staffno,position,role,depart,leader,mobile,email
    }
    public boolean spit;
    public boolean edit;
    public Type type;
    public String lable;
    public String id;
    public String name;

    /**
     *
     * @param edit 是否可编辑
     * @param spit
     * @param type 类型
     * @param lable 标签
     * @param id
     * @param name 名字
     */
    public UserProperty(boolean edit,boolean spit,Type type, String lable, String id, String name) {
        this.edit = edit;
        this.spit = spit;
        this.type = type;
        this.lable = lable;
        this.id = id;
        this.name = name;
    }
}
