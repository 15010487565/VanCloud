package com.vgtech.common.api;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhangshaofang on 2016/1/10.
 */
public class IdName extends AbsApiData implements Parcelable {
    public String id;
    public String name;
    public String lable;
    public String sub_data;

    public IdName() {
    }

    public IdName(String lable,String id, String name, String sub_data) {
        this.lable = lable;
        this.id = id;
        this.name = name;
        this.sub_data = sub_data;
    }

    public IdName(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public String getId()
    {
        return id==null?"":id;
    }
    protected IdName(Parcel in) {
        lable = in.readString();
        name = in.readString();
        id = in.readString();
        sub_data = in.readString();
    }

    public static final Parcelable.Creator<IdName> CREATOR = new Parcelable.Creator<IdName>() {
        @Override
        public IdName createFromParcel(Parcel in) {
            return new IdName(in);
        }

        @Override
        public IdName[] newArray(int size) {
            return new IdName[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(lable);
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(sub_data);
    }
}
