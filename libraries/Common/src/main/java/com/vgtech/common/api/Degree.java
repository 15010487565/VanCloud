package com.vgtech.common.api;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jackson on 2015/12/25.
 * Version : 1
 * Details :
 */
public class Degree  extends AbsApiData implements Parcelable {
    public String name;
    public String id;
    public String sub_data;
    public Degree(){}


    protected Degree(Parcel in) {
        name = in.readString();
        id = in.readString();
        sub_data = in.readString();
    }

    public static final Creator<Degree> CREATOR = new Creator<Degree>() {
        @Override
        public Degree createFromParcel(Parcel in) {
            return new Degree(in);
        }

        @Override
        public Degree[] newArray(int size) {
            return new Degree[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(sub_data);
    }
}
