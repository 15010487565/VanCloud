package com.vgtech.vancloud.service;

import android.app.IntentService;
import android.content.Intent;

import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.ui.common.publish.SubmitTask;

/**
 * Created by zhangshaofang on 2015/9/6.
 */
public class SubmitService extends IntentService {

    public SubmitService() {
        super("com.vgtech.vancloud.SubmitService");
    }

    private NetworkManager mNetworkManager;

    @Override
    protected void onHandleIntent(Intent intent) {
        VanCloudApplication app = (VanCloudApplication) getApplication();
        mNetworkManager = app.getNetworkManager();
        if (intent != null) {
            PublishTask publishTask = intent.getParcelableExtra("publishTask");
            if (publishTask != null) {
                mNetworkManager.getApiUtils().getSignParams().put(URLAddr.URL_PARAM_OID, PrfUtils.getUserId(this));
                Thread thread = new Thread(new SubmitTask(this, mNetworkManager, publishTask));
                thread.start();
            }
        }
    }
}
