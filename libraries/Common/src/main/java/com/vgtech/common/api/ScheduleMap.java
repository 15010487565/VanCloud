package com.vgtech.common.api;

import java.util.List;

/**
 * Created by zhangshaofang on 2015/9/14.
 */
public class ScheduleMap extends AbsApiData{
    public int selectUserPosition;
    public long startTime;
    public List<ScheduleItem> items;

    public ScheduleMap(long startTime, List<ScheduleItem> items) {
        this.startTime = startTime;
        this.items = items;
    }
}
