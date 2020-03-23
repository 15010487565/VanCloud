package com.vgtech.common.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by Jackson on 2015/12/25.
 * Version : 1
 * Details :职位类型
 */
public class JobType extends AbsApiData implements Parcelable {
    public String name;
    public String id;
    public boolean isChecked;
    public int listPosition = -1;
    public int gridPositioin = -1;
    public JobType(){}


    @Override
    public boolean equals(Object o) {
        if(o instanceof JobType){
            JobType job =   (JobType)o;
            return job.id == this.id && TextUtils.equals(job.name,this.name) && job.listPosition== this.listPosition;
        }
        return false;
    }

    protected JobType(Parcel in) {
        name = in.readString();
        id = in.readString();
        isChecked = in.readByte() != 0;
        listPosition = in.readInt();
        gridPositioin = in.readInt();
    }

    public static final Creator<JobType> CREATOR = new Creator<JobType>() {
        @Override
        public JobType createFromParcel(Parcel in) {
            return new JobType(in);
        }

        @Override
        public JobType[] newArray(int size) {
            return new JobType[size];
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
        dest.writeByte((byte) (isChecked ? 1 : 0));
        dest.writeInt(listPosition);
        dest.writeInt(gridPositioin);
    }
}
