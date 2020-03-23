package com.vgtech.vantop.moudle;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by Duke on 2016/10/13.
 */

public class VacationChange  extends AbsApiData {

    public String taskId;

    public long timestamp;

    public String title;

    public String status;

    public boolean canHasten;

    public String fromTaskId;

    public String processState;

    public String hastenMsg;

    public String changeStatusType;
}
