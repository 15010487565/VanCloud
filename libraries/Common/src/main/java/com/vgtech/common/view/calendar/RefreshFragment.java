package com.vgtech.common.view.calendar;

import com.vgtech.common.api.ScheduleisExist;

import java.util.List;

/**
 * Created by Duke on 2015/11/15.
 */
public interface RefreshFragment {

    void refresh(List<ScheduleisExist> scheduleisExists);
}
