package com.vgtech.vantop.moudle;

import android.text.TextUtils;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by Duke on 2016/9/30.
 */

public class StaffInfo extends AbsApiData {
    public String staff_no;
    public String staff_name;
    public String e_mail;
    public boolean checked;

    public String getStaff_no() {
        String staffNo = staff_no;
        if (!TextUtils.isEmpty(staffNo)) {
            staffNo = staffNo.trim();
        }
        return staffNo;
    }
}
