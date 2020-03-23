package com.vgtech.vantop.moudle;

import com.vgtech.common.api.AbsApiData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shilec on 2016/9/13.
 */
public class SalaryYearlyReportData extends AbsApiData implements Serializable{

    public String id;
    public String reportName;
    public List<String> value;

    @Override
    public String toString() {
        return "SalaryYearlyReportData{" +
                "id='" + id + '\'' +
                ", reportName='" + reportName + '\'' +
                ", value=" + value +
                '}';
    }

    public SalaryYearlyReportData() {
        value = new ArrayList<>();
    }
}
