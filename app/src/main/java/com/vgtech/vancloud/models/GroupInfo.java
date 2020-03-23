package com.vgtech.vancloud.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import roboguice.util.Strings;

/**
 * @author xuanqiang
 */
public class GroupInfo extends Entity {
  private static final long serialVersionUID = -2449924032629295490L;

  public String name;
  @SerializedName("naturalname") public String groupNick;
  @SerializedName("members") public List<Staff> staffs;

  private String nick;

  public String getNick() {
    if(Strings.isEmpty(nick)) {
      StringBuilder nickBuilder = new StringBuilder();
      int i = 0;
      for(Staff staff : staffs) {
        if(i < 9) {
          nickBuilder.append(staff.nick).append(",");
        }else {
          break;
        }
        i++;
      }
      nickBuilder.deleteCharAt(nickBuilder.length() - 1);
      nick = nickBuilder.toString();
    }
    return nick;
  }

  public String getDisplayNick() {
    if(Strings.isEmpty(groupNick)) {
      return getNick();
    }
    return groupNick;
  }

}
