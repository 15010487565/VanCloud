package com.vgtech.common.api;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jackson on 2015/10/19.
 * Version : 1
 * Details :
 */
public class Role extends AbsApiData implements Serializable{
    public String key;//角色id
    public String value;//角色名字
    public boolean isChecked;//判断是否被选中
    public ArrayList<Auth> auths;//权限
    public String type;
//    public Role(){}
//    protected Role(Parcel in) {
//        key = in.readString();
//        value = in.readString();
//        isChecked = in.readByte() != 0;
//        type = in.readString();
//    }
//
//    public static final Creator<Role> CREATOR = new Creator<Role>() {
//        @Override
//        public Role createFromParcel(Parcel in) {
//            return new Role(in);
//        }
//
//        @Override
//        public Role[] newArray(int size) {
//            return new Role[size];
//        }
//    };
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(key);
//        dest.writeString(value);
//        dest.writeByte((byte) (isChecked ? 1 : 0));
//        dest.writeString(type);
//    }
}
