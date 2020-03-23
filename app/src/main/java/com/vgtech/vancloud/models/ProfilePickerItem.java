package com.vgtech.vancloud.models;

import java.io.Serializable;

/**
 * @author xuanqiang
 */
public class ProfilePickerItem implements IItemName,Serializable{
  private static final long serialVersionUID = -3579160476932497545L;

  public String id;
  public String content;

  public ProfilePickerItem(String id, String content){
    this.id = id;
    this.content = content;
  }

  @Override
  public String getItemName(){
    return content;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || ((Object)this).getClass() != o.getClass()) return false;
    ProfilePickerItem that = (ProfilePickerItem) o;
    return !(id != null ? !id.equals(that.id) : that.id != null);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
