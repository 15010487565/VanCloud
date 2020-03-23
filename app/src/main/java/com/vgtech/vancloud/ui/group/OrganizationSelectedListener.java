package com.vgtech.vancloud.ui.group;

import com.vgtech.common.api.Organization;

import java.util.List;

/**
 * Created by vic on 2016/10/10.
 */
public interface OrganizationSelectedListener {
    String SELECT_MODE = "SELECT_MODE";
    int SELECT_SINGLE = 1;
    int SELECT_MULTI = 2;

    int getSelectMode();
    List<Organization> getSeleced();

    List<Organization> getUnSeleced();

    void addUnSelected(List<Organization> list);

    void add(Organization organization);

    void remove(Organization organization);

    boolean contains(Organization organization);

    void add(List<Organization> organizations);

    void remove(List<Organization> organizations);
}
