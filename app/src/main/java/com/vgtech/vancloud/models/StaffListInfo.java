package com.vgtech.vancloud.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author xuanqiang
 */
public class StaffListInfo extends Entity {
  private static final long serialVersionUID = 4400727966516134531L;

  @SerializedName("staffs") public List<Staff> staffs;

}
