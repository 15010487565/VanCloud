package com.vgtech.common;

import com.vgtech.common.provider.db.MessageDB;

import java.util.Comparator;

/**
 * Created by Duke on 2016/9/22.
 */

public class MessageComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        MessageDB messageDB1 = (MessageDB) o1;
        MessageDB messageDB2 = (MessageDB) o2;

        String timestamp1 = messageDB1.timestamp + "";
        String timestamp2 = messageDB2.timestamp + "";

        //首先比较时间戳，时间戳相同再比较名字
        int flag1 = timestamp2.compareTo(timestamp1);
        if (flag1 == 0) {
            return messageDB2.title.compareTo(messageDB1.title);
        } else {
            return flag1;
        }
    }
}
