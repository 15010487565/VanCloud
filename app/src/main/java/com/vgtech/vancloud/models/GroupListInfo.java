package com.vgtech.vancloud.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author xuanqiang
 */
public class GroupListInfo extends Entity {
  private static final long serialVersionUID = 2107604576784726949L;

  @SerializedName("rooms") public List<GroupInfo> groupInfos;

}
