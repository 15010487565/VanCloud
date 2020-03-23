package com.vgtech.vancloud.ui.module.recruit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.JobModule;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.ApiDataAdapter;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by code on 2016/7/25.
 * 职位模板
 */
public class JobModuleActivity extends BaseActivity implements HttpListener<String> {
    private NetworkManager mNetworkManager;
    private static final int CALLBACK_LIST = 1;
    private VancloudLoadingLayout loading;

    private ListView listView;
    private ApiDataAdapter<JobModule> jobModuleAdapter;


    @Override
    protected int getContentView() {
        return R.layout.activity_job_module;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.job_module));
        initView();
        jobModuleAdapter = new ApiDataAdapter(this);
        listView.setAdapter(jobModuleAdapter);
        initDate();
    }


    private void initView() {
        loading = (VancloudLoadingLayout) findViewById(R.id.loading);
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object obj = parent.getItemAtPosition(position);
                if (obj instanceof JobModule) {
                    JobModule jobModule = (JobModule) obj;
                    Intent intent = new Intent();
                    intent.putExtra("jobModule", jobModule);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        loading.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initDate();
            }
        });
    }

    private void initDate() {
        loading.showLoadingView(listView, "", true);
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_VANCLOUD_JOB_JOB_TEMPLATES), params, this);
        mNetworkManager.load(CALLBACK_LIST, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        boolean mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        loading.dismiss(listView);
        if (!mSafe) {
            loading.showErrorView(listView);
            return;
        }
        switch (callbackId) {
            case CALLBACK_LIST:
                List<JobModule> jobModules = new ArrayList<JobModule>();
                try {
                    String data = rootData.getJson().getString("data");
                    jobModules = JsonDataFactory.getDataArray(JobModule.class, new JSONArray(data));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                jobModuleAdapter.add(jobModules);
                if (jobModuleAdapter.getCount() > 0) {

                } else {
                    loading.showEmptyView(listView, getString(R.string.no_job_module), true, true);
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
}
