package com.vgtech.vantop.moudle;

import com.vgtech.common.api.AbsApiData;

/**
 * 假期
 * Created by Duke on 2016/7/22.
 */
public class Vacations extends AbsApiData {
    public String code;
    public String desc;
    public String unit;
    public String unitCode;
    public String date;
    public String balance;

    @Override
    public String toString() {
        return "{" +
                "code='" + code + '\'' +
                ", desc='" + desc + '\'' +
                ", unit='" + unit + '\'' +
                ", unitCode='" + unitCode + '\'' +
                ", date='" + date + '\'' +
                ", balance='" + balance + '\'' +
                '}';
    }

    public Vacations() {
    }

    public Vacations(VacationApplyDetails details) {
        this.code = details.code;
        this.desc = details.desc;
        this.unit = details.unit;
        this.unitCode = details.unitCode;
    }

}
