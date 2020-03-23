package com.vgtech.vancloud.api;

import com.vgtech.common.api.AbsApiData;
import com.vgtech.vancloud.models.Staff;

import java.util.List;

/**
 * Created by zhangshaofang on 2015/11/26.
 */
public class ChatGroup extends AbsApiData {
    public String creator;
    public String groupNick;
    public List<Staff> staffs;

    public ChatGroup(String creator, String groupNick, List<Staff> staffs) {
        this.creator = creator;
        this.groupNick = groupNick;
        this.staffs = staffs;
    }
}
