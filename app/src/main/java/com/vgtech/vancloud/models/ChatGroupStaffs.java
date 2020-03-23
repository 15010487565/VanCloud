package com.vgtech.vancloud.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author xuanqiang
 */
public class ChatGroupStaffs extends Entity {
    private static final long serialVersionUID = 4668870025290907561L;

    @SerializedName("owner")
    public String creator;
    @SerializedName("roomname")
    public String groupNick;
    @SerializedName("members")
    public List<Staff> staffs;
    public boolean isExit;

}
