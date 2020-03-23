package com.vgtech.vantop.moudle;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by vic on 2017/5/10.
 */
public class CardArea extends AbsApiData {
    public String startTime;
    public String longitude;
    public String latitude;
    public String endTime;


    public long getStartTime() {
        return Long.parseLong(startTime);
    }

    public long getEndTime() {
        return Long.parseLong(endTime);
    }
}
