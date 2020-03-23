package com.vgtech.vancloud.ui.module.approval;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.chenzhanyang.behaviorstatisticslibrary.BehaviorStatistics;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.AppModule;
import com.vgtech.common.view.NoScrollGridviewSpilview;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.AppModulePresenter;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.ApproveModuleAdapter;
import com.vgtech.vancloud.utils.IpUtil;

import java.util.HashMap;

/**
 * 申请首页
 * Created by vic on 2017/02/27.
 */

public class ApplyMainActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    @Override
    protected int getContentView() {
        return R.layout.apply_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.lable_apply));
        NoScrollGridviewSpilview gvGridView = (NoScrollGridviewSpilview) findViewById(R.id.grid_view);
        gvGridView.setOnItemClickListener(this);
        gvGridView.setFocusable(false);
        ApproveModuleAdapter mAdapter = new ApproveModuleAdapter(this, 3);
        gvGridView.setAdapter(mAdapter);
        mAdapter.add(AppModulePresenter.getApplyModules(this));
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

            BehaviorStatistics.getInstance().startBehavior(params);

//            if ("shenqing_vantop_holiday".equals(item.tag)) {
//                //申请-休假
//                Intent intent = new Intent(this, NormalWebActivity.class);
//
//                String url = "%sappstatic/vantop/jump.html?code=10057&area_code=%s&mobile=%s&tid=%s";
//                String host = ApiUtils.getHost(this);
//                String areaCode = PrfUtils.getPrfparams(this, "areaCode", "86");
//                String userPhone = PrfUtils.getUserPhone(this);
//                url = String.format(url, host, areaCode, userPhone, tenantId);
//
//                intent.putExtra("url", url);
//                intent.putExtra("title", getString(R.string.leave));
//                startActivity(intent);
//            } else {
            Intent intent = new Intent(this, ApprovalListActivity.class);
            intent.putExtra("title", getString(item.resName));
            intent.putExtra("tag", item.tag);
            intent.putExtra("type", 1);
            startActivity(intent);
//            }
        }
    }
}
