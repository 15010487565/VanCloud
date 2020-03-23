package com.vgtech.common.api;

import java.io.Serializable;

/**
 * Created by app02 on 2015/9/6.
 */
public class CalendarItem extends AbsApiData implements Serializable{
    public long calendarId;
    public String tenantId;
    public String userId;
    public long calendarContentId;
    public long startTime;
    public long endTime;
    public String content;
    public String isReminder;
    public String reminderMethod;
    public long reminderId;
    public int isVisible;
    public String participatorListId;
    public long supervisorId;
    public String createTime;
    public String state;
    public int isRepeat;
    public String type;
    public int isSync;
    public String imageListId;
    public String videoListId;
    public String audioListId;
    public int digCount;
    public long replyCount;

}
