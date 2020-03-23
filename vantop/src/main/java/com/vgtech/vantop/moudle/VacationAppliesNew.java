package com.vgtech.vantop.moudle;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by Duke on 2016/9/30.
 */

public class VacationAppliesNew extends AbsApiData {

    public String leave_remark;

    public boolean ccRequire;

    public boolean picShow;

    public boolean picRequire;

    public boolean enable;

    public float durationUnit;

    public float durationMin;

    public boolean isDurationModify;

    public boolean noteRequire;

    public int picRequireNum;

    public boolean isclockin;//3.1.9 是否需要打卡

    public String applyType;//休假时间类型

    public boolean isclockin()
    {
        return isclockin;
    }

}
