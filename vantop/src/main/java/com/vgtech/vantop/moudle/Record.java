package com.vgtech.vantop.moudle;

import java.util.ArrayList;

/**
 * Created by vic on 2017/2/27.
 */
public class Record {
    public String date;

    public ArrayList<PunchCardListData> cards;

    @Override
    public String toString() {
        return "{" +
                "date='" + date + '\'' +
                ", cards=" + cards +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Record record = (Record) o;

        return date.equals(record.date);

    }

    @Override
    public int hashCode() {
        return date.hashCode();
    }

}
