package com.vgtech.vancloud.ui.common.group;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.android.volley.VolleyError;
import com.vgtech.vancloud.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.common.PrfUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangshaofang on 2015/11/13.
 */
public class TenantInfoActivity extends BaseActivity implements HttpListener<String> {
    private NetworkManager mNetworkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNetworkManager = getAppliction().getNetworkManager();
        Intent intent = getIntent();
        String companyId = intent.getStringExtra("companyId");
        loadCompanyInfo(companyId);
    }

    private void loadCompanyInfo(String companyId) {
        showLoadingDialog(this, getString(R.string.loading));
        Map<String, String> params = new HashMap<>();
        SharedPreferences preferences = PrfUtils.getSharePreferences(this);
        params.put("user_id", preferences.getString("uid", ""));
        params.put("tenant_id", preferences.getString("tenantId", ""));
        if (!TextUtil.isEmpty(companyId)) {
            params.put("subCompany", companyId);
        }
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_GET_COMPANY_INFO), params, this);
        mNetworkManager.load(1, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case 1:
               
                break;
        }


    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    protected int getContentView() {
        return R.layout.tenantinfo;
    }
}
