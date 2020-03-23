package com.vgtech.common.api;

/**
 * Created by John on 2015/9/6.
 */
public class NewUser extends AbsApiData {

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

    public NewUser() {
    }

    public NewUser(String userid, String name, String photo) {
        this.userid = userid;
        this.name = name;
        this.photo = photo;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)                                      //先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof NewUser))
            return false;
        if (!userid.equals(((NewUser) other).userid))
            return false;
        return true;
    }

}
