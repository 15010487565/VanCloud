package com.vgtech.common.api;

/**
 * 用户信息
 * Created by Duke on 2015/8/17.
 */
public class User extends AbsApiData {


    /**
     * 用户id
     */
    public String userid;
    /**
     * 姓名
     */
    public String name;
    /**
     * 用户头像
     */
    public String photo;
    /**
     * 职位
     */
    public String job;
    /**
     * 电话
     */
    public String phone;

    public String email;
    public String getEmail()
    {
        return email;
    }
    public String departid;
    public String passwd;
    public String newpasswd;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return userid != null ? userid.equals(user.userid) : user.userid == null;

    }
}
