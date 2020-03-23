package com.vgtech.vancloud.api;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by Duke on 2016/3/18.
 */
public class OrderStatus extends AbsApiData {
    public String key;
    public String value;

    public OrderStatus() {
    }
    public OrderStatus(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
