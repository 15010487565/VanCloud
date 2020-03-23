package com.vgtech.vancloud.models;

import java.util.List;

/**
 * @author sandy
 */
public class OrgData extends Entity {
  private static final long serialVersionUID = 2968941994495262746L;

  public List<Org> parents;
  public List<Org> nodes;
  public List<Staff> staffs;
  public boolean hasLevel;
  public boolean hasStaffs;

}
