package com.vgtech.vancloud.models;

/**
 * @author xuanqiang
 */
public class Workgroup extends Entity {
  private static final long serialVersionUID = -3971441162333679306L;

  public String id;
  public String name;

  public Workgroup(String id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || ((Object)this).getClass() != o.getClass()) return false;
    Workgroup g = (Workgroup) o;
    return !(id != null ? !id.equals(g.id) : g.id != null);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

}
