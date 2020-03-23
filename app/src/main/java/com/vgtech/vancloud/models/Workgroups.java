package com.vgtech.vancloud.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author xuanqiang
 */
public class Workgroups extends Entity {
  private static final long serialVersionUID = -3219970285096798696L;

  @SerializedName("groups") public List<Workgroup> groups;

}
