package com.vgtech.common.api;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jackson on 2015/12/25.
 * Version : 1
 * Details :
 */
public class Department extends AbsApiData implements Parcelable {
    public String name;
    public String id;

    public Department(){}

    protected Department(Parcel in) {
        name = in.readString();
        id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Department> CREATOR = new Creator<Department>() {
        @Override
        public Department createFromParcel(Parcel in) {
            return new Department(in);
        }

        @Override
        public Department[] newArray(int size) {
            return new Department[size];
        }
    };
}
