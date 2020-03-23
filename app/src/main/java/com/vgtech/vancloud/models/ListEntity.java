package com.vgtech.vancloud.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author xuanqiang
 */
public class ListEntity<T> extends Entity {
  private static final long serialVersionUID = 6355519580323781819L;

  @SerializedName("data") public List<T> datas;
  public boolean hasMore;

  public static class StaffListEntity extends ListEntity<Staff>{
    private static final long serialVersionUID = 7462694706535677471L;
  }

  public static class VacationListEntity extends ListEntity<Vacation> {
    private static final long serialVersionUID = -8668291371855686193L;
  }


}
