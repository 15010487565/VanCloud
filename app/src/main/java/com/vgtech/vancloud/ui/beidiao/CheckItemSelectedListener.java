package com.vgtech.vancloud.ui.beidiao;

import java.util.List;

/**
 * Created by vic on 2016/10/10.
 */
public interface CheckItemSelectedListener {
    String SELECT_MODE = "SELECT_MODE";
    int SELECT_SINGLE = 1;
    int SELECT_MULTI = 2;

    int getSelectMode();
    List<CheckItem> getSeleced();

    List<CheckItem> getUnSeleced();

    void addUnSelected(List<CheckItem> list);
    void addUnSelected(CheckItem checkItem);

    void add(CheckItem organization);

    void remove(CheckItem organization);

    boolean contains(CheckItem organization);

    void add(List<CheckItem> organizations);

    void remove(List<CheckItem> organizations);
}
