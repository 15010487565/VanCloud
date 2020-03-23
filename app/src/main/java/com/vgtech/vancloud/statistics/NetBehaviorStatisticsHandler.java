package com.vgtech.vancloud.statistics;

import android.util.Log;

import com.android.volley.VolleyError;
import com.chenzhanyang.behaviorstatisticslibrary.handler.BehaviorHandler;
import com.vgtech.common.BaseApp;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.VanCloudApplication;

import java.util.HashMap;

/**
 * Data:  2017/7/10
 * Auther: 陈占洋
 * Description:
 */

public class NetBehaviorStatisticsHandler implements BehaviorHandler {

    @Override
    public void behaviorHandle(Object params) {
        if (params instanceof HashMap) {
            HashMap paramsMap = (HashMap) params;

            NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(BaseApp.getAppContext(), URLAddr.URL_BEHAVIOR_STATISTIC), paramsMap, BaseApp.getAppContext());
            ((VanCloudApplication) BaseApp.getAppContext()).getNetworkManager().load(Integer.MAX_VALUE, path, new HttpListener<String>() {
                @Override
                public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                }

                @Override
                public void onResponse(String response) {
                }
            }, false);
        }
    }
}
