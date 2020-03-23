package com.vgtech.vantop.moudle;


import com.vgtech.common.api.AbsApiData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by app02 on 2016/7/14.
 */
public class SalaryProjectData extends AbsApiData{
    private static final String TAG = "SalaryProjectData";

    public List<String> years;
    public List<SalaryProjectItemData> items;

    public SalaryProjectData() {
        items = new ArrayList<>();
        years = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "SalaryProjectData{" +
                "month='" + years + '\'' +
                ", items=" + items +
                '}';
    }
}
