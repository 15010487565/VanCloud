package com.vgtech.common.api;

import java.io.Serializable;

/**
 * Created by swj on 16/4/18.
 */
public class TradeTypeListItem extends AbsApiData implements Serializable {

    public String dictId;
    public int sort;
    public String remark;
    public String status;
    public String name;
    public String value;
    public String creatorTime;
    public String type;
    public String modifyTime;
}
