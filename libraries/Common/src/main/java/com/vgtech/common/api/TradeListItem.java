package com.vgtech.common.api;

import java.io.Serializable;

/**
 * Created by swj on 16/4/18.
 */
public class TradeListItem extends AbsApiData implements Serializable {

    public String amount;
    public String order_info_id;
    public String discription;
    public String time;
    public String optioner;
    public String remark;
    public String name;
}
