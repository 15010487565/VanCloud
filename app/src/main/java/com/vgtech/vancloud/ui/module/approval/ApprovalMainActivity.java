package com.vgtech.vancloud.ui.module.approval;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.android.volley.VolleyError;
import com.chenzhanyang.behaviorstatisticslibrary.BehaviorStatistics;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.AppModule;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.NoScrollGridviewSpilview;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.AppModulePresenter;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.ApproveModuleAdapter;
import com.vgtech.vancloud.utils.IpUtil;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 审批首页
 * Created by Duke on 2016/9/23.
 */

public class ApprovalMainActivity extends BaseActivity implements AdapterView.OnItemClickListener, HttpListener<String> {
    @Override
    protected int getContentView() {
        return R.layout.approval_layout;
    }

    private ApproveModuleAdapter mAdapter;
    private NetworkManager mNetworkManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(com.vgtech.vantop.R.string.app_approval));
        NoScrollGridviewSpilview gvGridView = (NoScrollGridviewSpilview) findViewById(R.id.grid_view);
        gvGridView.setOnItemClickListener(this);
        gvGridView.setFocusable(false);
        mAdapter = new ApproveModuleAdapter(this, 3);
        gvGridView.setAdapter(mAdapter);
        mAdapter.add(AppModulePresenter.getApproveModules(this));
        mNetworkManager = getAppliction().getNetworkManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUnApprovalCount();
    }
    public void getUnApprovalCount() {

        Map<String, String> params = new HashMap<String, String>();
        String url = VanTopUtils.generatorUrl(this, UrlAddr.URL_APPROVECOMMONNUM);
        params.put("loginUserCode", PrfUtils.getStaff_no(this));
        NetworkPath path = new NetworkPath(url, params, this, true);
        mNetworkManager.load(1, path, this, true);
    }

    @Override
    public void finish() {
        super.finish();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = parent.getItemAtPosition(position);
        if (object instanceof AppModule) {
            AppModule item = (AppModule) object;

            SharedPreferences preferences = PrfUtils.getSharePreferences(this);
            String userId = preferences.getString("uid", "");
            String employee_no = preferences.getString("user_no", "");
            String tenantId = preferences.getString("tenantId", "");

            HashMap<String, String> params = new HashMap<>();
            params.put("user_id", userId);
            params.put("employee_no", employee_no);
            params.put("tenant_id", tenantId);
            params.put("permission_id", item.id);
            params.put("operation_ip", IpUtil.getIpAddressString());
            params.put("operation_url", "");

            switch (AppPermission.WorkFlow.getType(item.tag)) {
                case extra_work: {
                    BehaviorStatistics.getInstance().startBehavior(params);

                    Intent intent = new Intent(this, MyApprovalActivity.class);
                    intent.putExtra("tag", AppPermission.Shenqing.shenqing_extra_work.toString());
                    startActivity(intent);

                }
                break;
                case sign_card: {
                    BehaviorStatistics.getInstance().startBehavior(params);

                    Intent intent = new Intent(this, MyApprovalActivity.class);
                    intent.putExtra("tag", AppPermission.Shenqing.shenqing_sign_card.toString());
                    startActivity(intent);
                }
                break;
                case vantop_holiday: {
                    BehaviorStatistics.getInstance().startBehavior(params);

                    Intent intent = new Intent(this, MyApprovalActivity.class);
                    intent.putExtra("tag", AppPermission.Shenqing.shenqing_vantop_holiday.toString());
                    startActivity(intent);
                }
                break;
            }
        }
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, false);
        if (!safe) {
            return;
        }
        try {
            JSONObject jsonObject = rootData.getJson().getJSONObject("data");
            int un_approve_lea = Integer.parseInt(jsonObject.getString("un_approve_lea"));
            int un_approve_car = Integer.parseInt(jsonObject.getString("un_approve_car"));
            int un_approve_ot = Integer.parseInt(jsonObject.getString("un_approve_ot"));
            if (mAdapter != null)
                mAdapter.updateModuleCount(un_approve_lea, un_approve_car, un_approve_ot);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }
}
