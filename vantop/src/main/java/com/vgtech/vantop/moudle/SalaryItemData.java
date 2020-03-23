package com.vgtech.vantop.moudle;

import com.vgtech.common.api.AbsApiData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shilec on 2016/9/13.
 */
public class SalaryItemData extends AbsApiData{
    public String itemId;
    public List<SalaryItemChildData> items;
    public String label;

    public SalaryItemData() {
        items = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "SalaryItemData{" +
                "itemId='" + itemId + '\'' +
                ", items=" + items +
                ", label='" + label + '\'' +
                '}';
    }
}
