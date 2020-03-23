package com.vgtech.vancloud.ui.module;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.inject.Inject;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppModule;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.presenter.AppModulePresenter;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.adapter.AppListAdapter;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.fragment.BaseSwipeBackFragment;
import com.vgtech.vancloud.ui.web.AppInfoWebFragment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import roboguice.fragment.RoboFragment;

/**
 * Created by code on 2016/9/5.
 * 应用查看更多
 */
public class AppListFragment extends BaseSwipeBackFragment implements AdapterView.OnItemClickListener, HttpListener<String> {
    private static final int CALLBACK_MODULE = 1;
    private ListView listView;
    private AppListAdapter mAppListAdapter;
    @Inject
    public Controller controller;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_see_more, null);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        TextView titleTv = (TextView) view.findViewById(android.R.id.title);
        titleTv.setText(R.string.app_recommend);
        view.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        mWaitView = LayoutInflater.from(getActivity()).inflate(R.layout.progress_black, null);
        listView.addFooterView(mWaitView);
        mWaitView.findViewById(R.id.btn_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadModule();
            }
        });
        mAppListAdapter = new AppListAdapter(getActivity());
        listView.setAdapter(mAppListAdapter);
        loadModule();
        return attachToSwipeBack(view);
    }

    private View mWaitView;

    private void loadModule() {
        showProgress(mWaitView, true);
        Map<String, String> params = new HashMap<>();
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_USER_MODELS), params, getActivity());
        VanCloudApplication vanCloudApplication = (VanCloudApplication) getActivity().getApplication();
        vanCloudApplication.getNetworkManager().load(CALLBACK_MODULE, path, this, true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        VanCloudApplication vanCloudApplication = (VanCloudApplication) getActivity().getApplication();
        vanCloudApplication.getNetworkManager().cancle(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = parent.getItemAtPosition(position);
        if (obj instanceof AppModule) {
            AppModule appModule = (AppModule) obj;
            AppInfoWebFragment appInfoWebFragment = new AppInfoWebFragment();
            Bundle bundle = new Bundle();
            bundle.putString("appmodule", appModule.getJson().toString());
            appInfoWebFragment.setArguments(bundle);
            controller.pushFragment(appInfoWebFragment);
        }

    }

    private boolean mInit;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        if (getActivity() == null || getActivity().isFinishing())
            return;
        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            showProgress(mWaitView, false);
            return;
        }
        switch (callbackId) {
            case CALLBACK_MODULE:
                try {
                    if (mInit)
                        return;
                    mInit = true;
                    listView.removeFooterView(mWaitView);
                    mWaitView.setVisibility(View.GONE);
                    JSONArray jsonObject = rootData.getJson().getJSONArray("data");
                    PrfUtils.savePrfparams(getActivity(), "modules", jsonObject.toString());
                    mAppListAdapter.addAllData(AppModulePresenter.getAppAllModules(getActivity()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    protected void showProgress(View progressLayout, boolean show) {

        if (progressLayout == null) {
            return;
        }
        if (progressLayout.getVisibility() != View.VISIBLE) {
            progressLayout.setVisibility(View.VISIBLE);
        }
        if (show) {
            progressLayout.findViewById(R.id.error_footer).setVisibility(
                    View.GONE);
            progressLayout.findViewById(R.id.progressBar).setVisibility(
                    View.VISIBLE);
        } else {
            progressLayout.findViewById(R.id.error_footer).setVisibility(
                    View.VISIBLE);
            progressLayout.findViewById(R.id.progressBar).setVisibility(
                    View.GONE);
        }
    }
}
