package com.vgtech.vantop.moudle;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by vic on 2017/2/15.
 */
public class Coord extends AbsApiData {
    public int circle;//0代表圆

    public double ltlongitufe;//圆心
    public double ltlatitude;

    public double rtlongitude;
    public double rtlatitude;

    public double rblongitude;//半径
    public double rblatitude;

    public double lblongitude;
    public double lblatitude;
    public int areaId;

    public int getRadius()
    {
        return (int) rblongitude;
    }


}
