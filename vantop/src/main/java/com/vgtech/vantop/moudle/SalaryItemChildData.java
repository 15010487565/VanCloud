package com.vgtech.vantop.moudle;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by shilec on 2016/9/13.
 */
public class SalaryItemChildData extends AbsApiData{
    public String month;
    public String value;

    @Override
    public String toString() {
        return "SalaryItemChildData{" +
                "month='" + month + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
