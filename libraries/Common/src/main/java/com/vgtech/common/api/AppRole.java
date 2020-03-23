package com.vgtech.common.api;

/**
 * Created by vic on 2016/9/28.
 */
public class AppRole extends AbsApiData {
    public enum Type{
        user,system
    }
    public String role_id;
    public String role_name;
    //个人信息 userinfo 返回
    public String id;
    public String name;
    public AppRole() {
    }

    public AppRole(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppRole appRole = (AppRole) o;

        return id.equals(appRole.id);

    }
}
