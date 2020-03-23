package com.vgtech.common.api;

import android.text.TextUtils;

/**
 * Created by vic on 2016/10/8.
 */
public class Organization extends AbsApiData {
    public String num;

    public String code;
    public String level;
    public String label;
    public String pcodes;
    public String user_id;

    public Node node;

    public boolean isBranch;

    public boolean isUser() {
        return !TextUtils.isEmpty(staff_no);
    }

    public boolean hasGroup;
    public boolean isWorkGroup;
    public String pos;
    public String rn;
    public String staff_name;
    public String staff_no;
    public String status;
    public String gid;
    public String photo;
    public String eMail;
    public boolean first;
    public boolean isVantopUser;

    public Organization() {
        isVantopUser = true;
    }

    public Organization(String pos, String staff_name, String staff_no, String photo) {
        this.pos = pos;
        this.staff_name = staff_name;
        this.staff_no = staff_no;
        this.photo = photo;
        this.user_id = staff_no;
    }

    public Organization(String num, String code, String label) {
        this.num = num;
        this.code = code;
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Organization that = (Organization) o;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (user_id != null ? !user_id.equals(that.user_id) : that.user_id != null) return false;
        if (!TextUtils.isEmpty(user_id) && !TextUtils.isEmpty(that.user_id) && user_id.equals(that.user_id))
            return true;

        if (staff_name != null ? !staff_name.equals(that.staff_name) : that.staff_name != null)
            return false;
        return staff_no != null ? staff_no.equals(that.staff_no) : that.staff_no == null;

    }

    @Override
    public String toString() {
        return "{" +
                "num='" + num + '\'' +
                ", code='" + code + '\'' +
                ", level='" + level + '\'' +
                ", label='" + label + '\'' +
                ", pcodes='" + pcodes + '\'' +
                ", user_id='" + user_id + '\'' +
                ", node=" + node +
                ", isBranch=" + isBranch +
                ", hasGroup=" + hasGroup +
                ", isWorkGroup=" + isWorkGroup +
                ", pos='" + pos + '\'' +
                ", rn='" + rn + '\'' +
                ", staff_name='" + staff_name + '\'' +
                ", staff_no='" + staff_no + '\'' +
                ", status='" + status + '\'' +
                ", gid='" + gid + '\'' +
                ", photo='" + photo + '\'' +
                ", eMail='" + eMail + '\'' +
                ", first=" + first +
                ", isVantopUser=" + isVantopUser +
                '}';
    }
}
