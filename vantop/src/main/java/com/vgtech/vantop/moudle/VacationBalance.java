package com.vgtech.vantop.moudle;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by Duke on 2016/9/28.
 */

public class VacationBalance extends AbsApiData {

    public String approvedNum;//申请已批准

    public String useNum;//本年度已用

    public String balance;//当前节余

    public String approvingNum;//审批中

    public String refuseNum;//被拒绝申请

    public String adjNum;//调整数

    public String curAssign;//当前分配

    public String yearAssign;//本年度分配

    public String date;//截止日期

    public String lastBal;//上年度节余
}
