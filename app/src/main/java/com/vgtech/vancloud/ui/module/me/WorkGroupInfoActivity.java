package com.vgtech.vancloud.ui.module.me;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.Organization;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.User;
import com.vgtech.common.provider.db.WorkGroup;
import com.vgtech.common.provider.db.WorkRelation;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.group.CreateWorkGroupActivity;
import com.vgtech.vancloud.ui.view.RcDecoration;
import com.vgtech.vancloud.ui.workgroup.WorkGroupRcInfoAdapter;
import com.vgtech.vantop.utils.AppModulePresenterVantop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.vgtech.vancloud.ui.module.me.WorkGroupActivity.RECEIVER_GROUP_REFRESH;

/**
 * Created by vic on 2016/10/29.
 */
public class WorkGroupInfoActivity extends BaseActivity implements
        HttpListener<String>,
        WorkGroupRcInfoAdapter.OnItemClickListener {

    private String mWgId;
    private VancloudLoadingLayout loadingLayout;
    private View mActionView;
    private String mWgName;
    private RecyclerView rcWorkGroup;
    private WorkGroupRcInfoAdapter rcAdapter;

    @Override
    protected int getContentView() {
        return R.layout.workgroup_info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading_layout);
        mActionView = findViewById(R.id.rl_bottom);
        findViewById(R.id.btn_edit_name).setOnClickListener(this);
        findViewById(R.id.btn_update_user).setOnClickListener(this);
        Intent intent = getIntent();
        mWgId = intent.getStringExtra("workgroupId");
        mWgName = intent.getStringExtra("workgroupName");

        rcWorkGroup = (RecyclerView) findViewById(R.id.rc_WorkGroupInfo);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcWorkGroup.setLayoutManager(layoutManager);
        rcWorkGroup.setItemAnimator(null);
        rcAdapter = new WorkGroupRcInfoAdapter();
        rcAdapter.setItemClickListener(this);
        rcWorkGroup.setAdapter(rcAdapter);
        rcWorkGroup.addItemDecoration(new RcDecoration());

        setTitle(mWgName);
        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                loadData(mWgId);
            }
        });
        loadData(mWgId);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER_GROUP_REFRESH);
        registerReceiver(mReceiver, intentFilter);
    }

    private void deleteWorkGroupUser(String userId) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("groupid", mWgId);
        params.put("userids", userId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_WORKGROUP_DELETEUSER), params, this);
        getAppliction().getNetworkManager().load(2, path, this);
    }

    private void updateWorkGroupName(String wgName) {
        showLoadingDialog(this, "");
        Map<String, String> params = new HashMap<String, String>();
        params.put("groupid", mWgId);
        params.put("name", wgName);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_WORKGROUP_UPDATE), params, this);
        getAppliction().getNetworkManager().load(1, path, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            sendBroadcast(new Intent(RECEIVER_GROUP_REFRESH));
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private EditText mNameEt;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit_name:
                AlertDialog dialog = new AlertDialog(this).builder().setTitle(getString(R.string.title_work_group_name));
                mNameEt = dialog.setEditer();
                mNameEt.setText(mWgName);
                mNameEt.setSelection(mNameEt.getText().length());
                dialog.setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String cn = mNameEt.getText().toString();
                        if (!TextUtils.isEmpty(cn)) {
                            updateWorkGroupName(cn);
                        } else {
                            Toast.makeText(WorkGroupInfoActivity.this, R.string.title_work_group_nameedit, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
                break;
            case R.id.btn_update_user:
                if (!AppModulePresenterVantop.isOpenZuzhijiagou(this)) {
                    Toast.makeText(this, getString(R.string.permission_denied_organization), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, CreateWorkGroupActivity.class);
                intent.putExtra("workgroupId", mWgId);
                intent.putExtra("workgroupName", mWgName);
                startActivityForResult(intent, 101);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean refuse = intent.getBooleanExtra("refuse", false);
            if (RECEIVER_GROUP_REFRESH.equals(action) && !refuse) {
                loadData(mWgId);
            }
        }
    };

    private void loadData(final String workgroupId) {
        Log.e("TAG_请求数据", "loadData");
        mActionView.setVisibility(View.GONE);
        loadingLayout.showLoadingView(rcWorkGroup, "", true);
//        WorkGroup wg = WorkGroup.query(this, workgroupId);
//        if (wg == null) {
//            sendBroadcast(new Intent(RECEIVER_GROUP_REFRESH));
//            finish();
//            return;
//        }
//        setTitle(wg.name);
        new AsyncTask<Void, Void, List<Organization>>() {
            @Override
            protected List<Organization> doInBackground(Void... params) {
                List<User> userList = WorkRelation.queryVantopWorkGroupByWgId(WorkGroupInfoActivity.this, workgroupId);
                List<Organization> departs = new ArrayList<Organization>();
                for (User node : userList) {
                    Organization organization = new Organization(node.job, node.name, node.userId, node.photo);
                    departs.add(organization);
                }

                return departs;
            }

            @Override
            protected void onPostExecute(List<Organization> nodeList) {
                loadingLayout.dismiss(rcWorkGroup);
                mActionView.setVisibility(View.VISIBLE);
                if (!isFinishing()) {
                    rcAdapter.setData(nodeList);
                    if (nodeList !=null && nodeList.size() == 0) {
                        loadingLayout.showEmptyView(rcWorkGroup, getString(R.string.no_list_data), true, true);
                        rcWorkGroup.setVisibility(View.VISIBLE);
                    }
                }
            }
        }.execute();
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        Log.e("TAG_请求数据", "callbackId=" + callbackId);
        switch (callbackId) {
            case 1: {
                mWgName = path.getPostValues().get("name");
                setTitle(mWgName);
                WorkGroup workGroup = new WorkGroup();
                workGroup.wgtoupId = mWgId;
                workGroup.name = mWgName;
                workGroup.updateByWgId(WorkGroupInfoActivity.this);
                Intent intent = new Intent(RECEIVER_GROUP_REFRESH);
                intent.putExtra("refuse", true);
                sendBroadcast(intent);
            }
            break;
            case 2:
                break;
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    //点击事件
    @Override
    public void onClick(int position) {
        List<Organization> data = rcAdapter.getData();
        Organization organization = data.get(position);
        UserUtils.enterUserInfo(this, organization.user_id, organization.staff_name, organization.photo);
    }

    @Override
    public void onDelClick(int position) {
        try {
            Log.e("TAG_删除","发送广播");
            List<Organization> data = rcAdapter.getData();
            Organization organization = data.get(position);
            WorkRelation workRelation = WorkRelation.query(WorkGroupInfoActivity.this, mWgId, organization.user_id);
            workRelation.delete(WorkGroupInfoActivity.this);
            //通知上级刷新数量
            Intent intent = new Intent(RECEIVER_GROUP_REFRESH);
            intent.putExtra("refuse", true);
            sendBroadcast(intent);
            //刷新列表
            data.remove(position);
           rcAdapter.notifyItemRemoved(position);
            rcAdapter.notifyItemRangeChanged(0,data.size());

//            loadData(mWgId);
            deleteWorkGroupUser(organization.user_id);
            if (data.size() == 0){
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
