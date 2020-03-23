package com.vgtech.vancloud.ui.module.accountmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.ActivityUtils;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.Account;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.AccountListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Duke on 2016/7/20.
 */
public class AccountManagementActivity extends BaseActivity implements HttpListener<String> {

    private static final int CALLBACK_ACCOUNTS = 1;
    private VancloudLoadingLayout loadingLayout;
    private ListView listView;
    private NetworkManager mNetworkManager;
    private AccountListAdapter accountListAdapter;

    @Override
    protected int getContentView() {
        return R.layout.account_management_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.vancloud_account_management));

        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);
        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                getAccounts();
            }
        });
        listView = (ListView) findViewById(R.id.list);
        accountListAdapter = new AccountListAdapter(this, new ArrayList<Account>());
        listView.setAdapter(accountListAdapter);

        getAccounts();
    }

    public void getAccounts() {
        loadingLayout.showLoadingView(listView, "", true);
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_VANCLOUD_JOB_ACCOUNTS), params, this);
        mNetworkManager.load(CALLBACK_ACCOUNTS, path, this);
    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        loadingLayout.dismiss(listView);
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            loadingLayout.showErrorView(listView, "", true, true);
            return;
        }

        switch (callbackId) {
            case CALLBACK_ACCOUNTS:

                List<Account> accounts = new ArrayList<Account>();
                try {
                    accounts = JsonDataFactory.getDataArray(Account.class, rootData.getJson().getJSONArray("data"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (accountListAdapter == null) {
                    accountListAdapter = new AccountListAdapter(this, accounts);
                    listView.setAdapter(accountListAdapter);
                } else {

                    accountListAdapter.myNotifyDataSetChanged(accounts);
                }
                if (accountListAdapter.getList().size() <= 0) {
                    loadingLayout.showEmptyView(listView, getString(R.string.no_list_data), true, true);
                    listView.setVisibility(View.VISIBLE);
                }
                break;

            default:

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 101:
                if (resultCode == RESULT_OK) {
                    getAccounts();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
}