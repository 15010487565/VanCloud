package com.vgtech.common.utils;

import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkPath;

/**
 * Created by gs on 2020-02-19.
 */
public interface HttpView {

    void dataLoaded(int callbackId, NetworkPath path, RootData rootData);
    void onFailure(int callbackId,  String data);
}
