package com.vgtech.vancloud.api;

import com.baidu.mapapi.model.LatLng;
import com.vgtech.common.api.AbsApiData;

/**
 * Created by vic on 2017/4/20.
 */
public class PoiItem extends AbsApiData {
    public String name;
    public String address;
    public LatLng latlng;
    public boolean init;
}
