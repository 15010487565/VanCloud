package com.vgtech.vancloud.ui.module.contact;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.reciver.GroupReceiver;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.register.ui.ChooseSubPartmentActivity;
import com.vgtech.vancloud.ui.register.ui.RoleActivity;
import com.vgtech.vancloud.ui.register.ui.SetPositionActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brook on 2016/9/08.
 * Version : 1
 * Details :
 */
public class StaffApprovalActivity extends BaseActivity implements View.OnClickListener, HttpListener<String> {

    private TextView tv_phone;
    private TextView tv_name;
    private TextView tv_role;
    private TextView tv_position;
    private TextView tv_department;

    private String topicId;
    private final String REFUSED = "refused";
    private static final int CALL_BACK_AGREE = 1;
    private static final int CALL_BACK_REFUSE = 2;
    private NetworkManager mNetworkManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
        initData();
        setTitle(R.string.detail_info);
    }

    private void initData() {

    }

    private void initListener() {
        findViewById(R.id.rl_role).setOnClickListener(this);
        findViewById(R.id.rl_position).setOnClickListener(this);
        findViewById(R.id.rl_department).setOnClickListener(this);
        findViewById(R.id.tv_agree).setOnClickListener(this);
        findViewById(R.id.tv_refuse).setOnClickListener(this);
    }

    private void initView() {
        LinearLayout ll_processed = (LinearLayout) findViewById(R.id.ll_processed);
        tv_phone = (TextView) findViewById(R.id.tv_phone);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_role = (TextView) findViewById(R.id.tv_role);
        tv_position = (TextView) findViewById(R.id.tv_position);
        tv_department = (TextView) findViewById(R.id.tv_department);

        Intent intent = getIntent();
        topicId = intent.getStringExtra("topicId");
        String number = intent.getStringExtra("number");
        String user_name = intent.getStringExtra("user_name");
        String state = intent.getStringExtra("state");

        if (state != null) {
            if ("refused".equals(state)) {
                ll_processed.setVisibility(View.GONE);
            } else if ("pending".equals(state)) {
                ll_processed.setVisibility(View.VISIBLE);
            }
        }
        tv_name.setText(user_name);
        tv_phone.setText(number);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_staff_approval;
    }

    private static final int ROLE = 1000;
    private static final int POSITION = 1001;
    private static final int DEPART = 1002;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_role:
                Intent data = new Intent(this, RoleActivity.class);
                data.putExtra("selected", mMap.get("roleId"));
                startActivityForResult(data, ROLE);
                break;
            case R.id.rl_position:
                String positionId = mMap.get("positionId");
                Intent intent = new Intent(this, SetPositionActivity.class);
                intent.putExtra("positionId", positionId);
                intent.putExtra("get", true);
                startActivityForResult(intent, POSITION);
                break;
            case R.id.rl_department:
                startActivityForResult(ChooseSubPartmentActivity.class, DEPART);
                break;
            case R.id.tv_agree:
                approvalAgree();
                break;
            case R.id.tv_refuse:
                approvalRefuse();
                break;
        }
        super.onClick(v);
    }

    private void approvalRefuse() {
        showLoadingDialog(this, getString(R.string.dataloading));
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("invite_id", topicId);
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("option", REFUSED);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_INVITE_OPTION), params, this);
        mNetworkManager.load(CALL_BACK_REFUSE, path, this);
    }

    public void approvalAgree() {

        String phone = tv_phone.getText().toString();
        String name = tv_name.getText().toString().trim();

        String roleId = mMap.get("roleId");
        if (TextUtils.isEmpty(roleId)) {
            showToast(R.string.please_set_role);
            return;
        }

        String positionId = mMap.get("positionId");
        if (positionId == null)
            positionId = "-1";

        String departid = mMap.get("departid");
        if (TextUtils.isEmpty(departid)) {
            showToast(R.string.please_set_depart);
            return;
        }

        showLoadingDialog(this, getString(R.string.dataloading));
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("invite_id", topicId);
        params.put("mobile", phone);
        params.put("user_name", name);
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("area_code", "86");
        params.put("role_id", roleId);
        params.put("position_id", positionId);
        params.put("depart_id", departid);
        params.put("user_id", PrfUtils.getUserId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_AGREED_OPTIO), params, this);
        mNetworkManager.load(CALL_BACK_AGREE, path, this);
    }


    private void startActivityForResult(Class<?> clzz, int requestCode) {
        Intent data = new Intent(this, clzz);
        data.putExtra("get", true);
        startActivityForResult(data, requestCode);
    }

    private Map<String, String> mMap = new HashMap<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ROLE:
                if (resultCode != RESULT_OK) return;
                String roleId = data.getStringExtra("id");
                String roleName = data.getStringExtra("name");
                mMap.put("roleId", roleId);
                tv_role.setText(roleName);
                break;
            case POSITION:
                if (resultCode != RESULT_OK) return;
                String positionId = data.getStringExtra("id");
                String positionName = data.getStringExtra("name");
                mMap.put("positionId", positionId);
                tv_position.setText(positionName);
                break;
            case DEPART:
                if (resultCode != RESULT_OK) return;
                String departid = data.getStringExtra("id");
                String departName = data.getStringExtra("name");
                mMap.put("departid", departid);
                tv_department.setText(departName);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALL_BACK_AGREE:
                finish();
                sendBroadcast(new Intent(GroupReceiver.REFRESH));
                break;
            case CALL_BACK_REFUSE:
                finish();
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