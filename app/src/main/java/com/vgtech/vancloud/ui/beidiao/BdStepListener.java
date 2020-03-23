package com.vgtech.vancloud.ui.beidiao;

import java.util.Map;

/**
 * Created by vic on 2016/10/17.
 */
public interface BdStepListener {
    void reset();
    void stepOne( Map<String, String> params);
    void stepTwo();
}
