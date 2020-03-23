package com.vgtech.vantop.ui.vacation;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.BalanceUseAdapter;
import com.vgtech.vantop.moudle.Vacations;
import com.vgtech.vantop.moudle.VacationsBalanceUses;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 假期使用详情
 * Created by Duke on 2016/9/13.
 */
public class BalanceUseActivity extends BaseActivity implements HttpListener<String> {

    private ListView listView;
    private VancloudLoadingLayout loadingLayout;
    private NetworkManager mNetworkManager;
    private final int CALLBACK_LIST = 1;

    private TextView headerTypeView;

    private TextView footerDurationView;

    private BalanceUseAdapter adapter;
    private Vacations vacation;

    private boolean type;

    @Override
    protected int getContentView() {
        return R.layout.balance_use_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String jsonTxt = intent.getStringExtra("json");
        try {
            if (!TextUtils.isEmpty(jsonTxt)) {
                mNetworkManager = getApplicationProxy().getNetworkManager();
                initView();
                vacation = JsonDataFactory.getData(Vacations.class, new JSONObject(jsonTxt));
                headerTypeView.setText(vacation.desc);
                adapter = new BalanceUseAdapter(this, new ArrayList<VacationsBalanceUses>(), vacation);
                type = intent.getBooleanExtra("type", false);
                if (type) {
                    setTitle(getString(R.string.vantop_year_used));
                    View footerView = LayoutInflater.from(this).inflate(R.layout.balance_use_footer, null);
                    footerDurationView = (TextView) footerView.findViewById(R.id.useFooterDurationView);
                    listView.addFooterView(footerView);
                    listView.setAdapter(adapter);
                    getUsesData(vacation.code);
                } else {
                    setTitle(getString(R.string.vantop_adjustments));
                    listView.setAdapter(adapter);
                    getAdjustsData(vacation.code);
                }

                loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
                    @Override
                    public void loadAgain() {
                        if (type) {
                            getUsesData(vacation.code);
                        } else {
                            getAdjustsData(vacation.code);
                        }
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initView() {
        headerTypeView = (TextView) findViewById(R.id.headerTypeView);
        listView = (ListView) findViewById(R.id.list);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);
    }

    //获取调整数数据
    public void getAdjustsData(String code) {
        loadingLayout.showLoadingView(listView, "", true);
        String url = VanTopUtils.generatorUrl(BalanceUseActivity.this, UrlAddr.URL_VACATIONS_BY_CODE_ADJUSTS);
        Map<String, String> params = new HashMap<String, String>();
        params.put("code", code);
        NetworkPath path = new NetworkPath(url, params, this, true);
        mNetworkManager.load(CALLBACK_LIST, path, this, false);
    }

    //获取已用数数据
    public void getUsesData(String code) {
        loadingLayout.showLoadingView(listView, "", true);
        String url = VanTopUtils.generatorUrl(BalanceUseActivity.this, UrlAddr.URL_VACATIONS_BY_CODE_USES);
        Map<String, String> params = new HashMap<String, String>();
        params.put("code", code);
        NetworkPath path = new NetworkPath(url, params, this, true);
        mNetworkManager.load(CALLBACK_LIST, path, this, false);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        loadingLayout.dismiss(listView);
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            loadingLayout.showErrorView(listView);
            return;
        }

        switch (callbackId) {
            case CALLBACK_LIST:

                List<VacationsBalanceUses> list = new ArrayList<>();
                try {
                    JSONObject jsonObject = rootData.getJson();
                    list = JsonDataFactory.getDataArray(VacationsBalanceUses.class, jsonObject.getJSONArray("data"));
                    if (getIntent().getBooleanExtra("type", false)) {
                        String sum = rootData.getJson().getString("sum");
                        footerDurationView.setText(sum + vacation.unit);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (adapter == null) {
                    adapter = new BalanceUseAdapter(this, list, vacation);
                    listView.setAdapter(adapter);
                } else {
                    adapter.myNotifyDataSetChanged(list);
                }
                if (adapter.getList().size() <= 0) {
                    loadingLayout.showEmptyView(listView, getString(R.string.vantop_no_list_data), true, true);
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
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }
}
