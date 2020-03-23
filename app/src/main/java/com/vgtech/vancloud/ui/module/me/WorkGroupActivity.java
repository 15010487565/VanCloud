package com.vgtech.vancloud.ui.module.me;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.Node;
import com.vgtech.common.api.Organization;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.User;
import com.vgtech.common.provider.db.WorkGroup;
import com.vgtech.common.provider.db.WorkRelation;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.common.group.tree.TreeHelper;
import com.vgtech.vancloud.ui.group.CreateWorkGroupActivity;
import com.vgtech.vancloud.ui.view.RcDecoration;
import com.vgtech.vancloud.ui.workgroup.WorkGroupRcAdapter;
import com.vgtech.vantop.utils.AppModulePresenterVantop;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作组列表
 * Created by zhangshaofang on 2015/9/22.
 */
public class WorkGroupActivity extends BaseActivity implements WorkGroupRcAdapter.OnItemClickListener, HttpListener<String> {

//    private SwipeMenuListView listView;
    private VancloudLoadingLayout loadingLayout;
//    private WorkGroupAdapter mWorkGroupAdapter;

    private RecyclerView rcWorkGroup;
    private WorkGroupRcAdapter rcAdapter;
    @Override
    protected int getContentView() {
        return R.layout.workgroup_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.tab_work));
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);

        rcWorkGroup = (RecyclerView) findViewById(R.id.rc_WorkGroup);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        rcWorkGroup.setLayoutManager(layoutManager);
        rcWorkGroup.setItemAnimator(null);
        rcAdapter = new WorkGroupRcAdapter();
        rcAdapter.setItemClickListener(this);
        rcWorkGroup.setAdapter(rcAdapter);
        rcWorkGroup.addItemDecoration(new RcDecoration());

        View createView = findViewById(R.id.btn_right);
        createView.setOnClickListener(this);
        if (AppModulePresenterVantop.isOpenZuzhijiagou(this)) {
            createView.setVisibility(View.VISIBLE);
        }else {
            createView.setVisibility(View.GONE);
        }

        initListData();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER_GROUP_REFRESH);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void finish() {
        super.finish();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private Organization mDelOrg;

    private void deleteGroup(Organization organization) {
        mDelOrg = organization;
        String groupId = organization.code;
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("groupid", groupId);
        showLoadingDialog(this, "");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_WORKGROUP_DELETE), params, this);
        getAppliction().getNetworkManager().load(1, path, this);
    }

    public static final String RECEIVER_GROUP_REFRESH = "RECEIVER_GROUP_REFRESH";

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RECEIVER_GROUP_REFRESH.equals(action)) {
                initListData();
            }
        }
    };

    private static final int REQUEST_CREATE = 5001;
    private EditText mNameEt;

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_right:
                AlertDialog dialog = new AlertDialog(this).builder().setTitle(getString(R.string.title_work_group_name));
                mNameEt = dialog.setEditer();
//                mNameEt.setDettailText("");
                mNameEt.setSelection(mNameEt.getText().length());
                dialog.setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String cn = mNameEt.getText().toString();
                        if (!TextUtils.isEmpty(cn)) {
                            createWorkGroup(cn);
                        } else {
                            Toast.makeText(WorkGroupActivity.this, R.string.title_work_group_nameedit, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void createWorkGroup(String wgName) {
        showLoadingDialog(this, "");
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("name", wgName);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_WORKGROUP_CREATE), params, this);
        getAppliction().getNetworkManager().load(2, path, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 101:
            case REQUEST_CREATE:
                if (resultCode == Activity.RESULT_OK) {
                    initListData();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void initListData() {
        loadingLayout.showLoadingView(rcWorkGroup, "", true);
        new AsyncTask<Void, Void, List<Organization>>() {
            @Override
            protected List<Organization> doInBackground(Void... params) {
                List<User> userList = TenantPresenter.isVanTop(WorkGroupActivity.this) ? WorkRelation.queryVantopWorkGroup(WorkGroupActivity.this) : WorkRelation.queryWorkGroup(WorkGroupActivity.this);
                ArrayList<WorkGroup> workGroups = WorkGroup.queryWorkGroup(WorkGroupActivity.this);
//                Log.e("TAG_讨论组","workGroups="+workGroups.toString());
                List datas = new ArrayList();
                datas.addAll(userList);
                datas.addAll(workGroups);
                List<Node> nodes = new ArrayList<Node>();
                try {
                    nodes = TreeHelper.convetData2Node(datas);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                List<Organization> departs = new ArrayList<Organization>();
                for (Node node : nodes) {
                    if (node.getLevel() == 0) {
                        Organization organization = new Organization("" + TreeHelper.getChildCount(node), node.getId(), node.getName());
                        organization.isWorkGroup = true;
                        organization.pcodes = node.getpId();
                        departs.add(organization);
                    }
                }
                return departs;
            }

            @Override
            protected void onPostExecute(List<Organization> nodeList) {
                loadingLayout.dismiss(rcWorkGroup);
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
        switch (callbackId) {
            case 1:
                String groupid = path.getPostValues().get("groupid");
                new WorkGroup().deleteByWgId(this, groupid);
                WorkRelation.deleteFromWorkGroupId(this, groupid);
                if (mDelOrg != null && mDelOrg.code.equals(groupid)) {

                } else {
                    List<Organization> data = rcAdapter.getData();
                    for (Organization org : data) {
                        if (groupid.equals(org.code)) {
                            data.remove(org);
                            break;
                        }
                    }
                }
                break;
            case 2:
                try {
                    String wgName = path.getPostValues().get("name");
                    String groupId = rootData.getJson().getJSONObject("data").getJSONObject("workGroup").getString("id");
                    WorkGroup workGroup = new WorkGroup();
                    workGroup.wgtoupId = groupId;
                    workGroup.name = wgName;
                    workGroup.insert(this);
                    initListData();
                    Intent intent = new Intent(this, CreateWorkGroupActivity.class);
                    intent.putExtra("workgroupId", groupId);
                    intent.putExtra("workgroupName", wgName);
                    startActivityForResult(intent, REQUEST_CREATE);
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

    @Override
    public void onClick(int position) {
        List<Organization> data = rcAdapter.getData();
        Organization organization = data.get(position);
        Intent intent = new Intent(this, WorkGroupInfoActivity.class);
        intent.putExtra("workgroupId", organization.code);
        intent.putExtra("workgroupName", organization.label);
        startActivity(intent);
    }

    @Override
    public void onDelClick(int position) {
        List<Organization> data = rcAdapter.getData();
        Organization organization = data.get(position);
        deleteGroup(organization);
        //刷新列表
        data.remove(position);
        rcAdapter.notifyItemRemoved(position);
        rcAdapter.notifyItemRangeChanged(0,data.size());
    }
}
