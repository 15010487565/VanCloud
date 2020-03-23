package com.vgtech.common.api;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jackson on 2015/12/25.
 * Version : 1
 * Details :
 */
public class WorkExperience extends AbsApiData implements Parcelable {
    public String name;
    public String id;
    public String sub_data;
public WorkExperience(){}
    protected WorkExperience(Parcel in) {
        name = in.readString();
        id = in.readString();
        sub_data = in.readString();
    }

    public static final Creator<WorkExperience> CREATOR = new Creator<WorkExperience>() {
        @Override
        public WorkExperience createFromParcel(Parcel in) {
            return new WorkExperience(in);
        }

        @Override
        public WorkExperience[] newArray(int size) {
            return new WorkExperience[size];
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
