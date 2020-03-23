package com.vgtech.vantop.moudle;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by app02 on 2016/7/14.
 */
public class SalaryProjectItemData extends AbsApiData{

    public String label;
    public String value;

    @Override
    public String toString() {
        return "SalaryProjectItemData{" +
                "label='" + label + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
