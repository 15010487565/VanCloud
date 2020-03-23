package com.vgtech.vancloud.ui.beidiao;

/**
 * Created by vic on 2016/10/17.
 */
public class CheckItem {

    public int id;
    public String name;
    public int price;

    public CheckItem(int id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CheckItem checkItem = (CheckItem) o;

        return id == checkItem.id;

    }
}
