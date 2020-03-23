package com.vgtech.vancloud.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import roboguice.util.Strings;

/**
 * @author xuanqiang
 * @date 13-7-3
 */
public class Entity implements Serializable {
  private static final long serialVersionUID = -2403943490664054424L;

  @SerializedName("msg") public String msg;
  @SerializedName("code") public String code;

  public boolean hasError(){
    return Strings.notEmpty(code) && !"200".equals(code);
  }

  @Override
  public String toString(){
    return "server code:" + code + ",server msg:" + msg;
  }
}