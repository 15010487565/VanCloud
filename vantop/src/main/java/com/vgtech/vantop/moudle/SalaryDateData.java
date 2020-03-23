package com.vgtech.vantop.moudle;

import com.vgtech.common.api.AbsApiData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shilec on 2016/9/12.
 */
public class SalaryDateData extends AbsApiData{
    public String month;
    public String remark;
    public List<SalaryProjectItemData> items;

    public SalaryDateData() {
        items = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "SalaryDateData{" +
                "month='" + month + '\'' +
                ", items=" + items +
                '}';
    }
}
