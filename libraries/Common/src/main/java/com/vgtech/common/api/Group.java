package com.vgtech.common.api;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhangshaofang on 2015/8/20.
 */
public class Group extends AbsApiData implements Parcelable {


    @TreeNodeId
    public String department_id;
    @TreeNodePid
    public String pid;
    @TreeNodeLabel
    public String name;
    @TreeNodeBranch
    public String isbranch;
    public String version;
    public int count;

    public Group() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(department_id);
        dest.writeString(name);
    }

    public static final Parcelable.Creator<Group> CREATOR = new Parcelable.Creator<Group>() {
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    private Group(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        department_id = in.readString();
        name = in.readString();
    }

    public Group(String groupId, String name, int count) {

        this.name = name;
        this.count = count;
    }

    public String getGid() {
        return department_id;
    }

    public void setGid(String gid) {
        this.department_id = gid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return isbranch;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Group{" +
                "gid=" + department_id +
                ", pid=" + pid +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
