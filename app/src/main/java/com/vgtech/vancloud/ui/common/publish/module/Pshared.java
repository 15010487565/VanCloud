package com.vgtech.vancloud.ui.common.publish.module;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by Nick on 2015/10/12.
 */
public class Pshared extends AbsApiData {
    public String content;
    public String receiverids;
    public String address;
    public String latlng;
    public int subType;//1 help 2 share
}
