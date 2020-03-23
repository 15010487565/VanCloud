package com.vgtech.vancloud.ui.register.ui;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppRole;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.Role;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色选择（单选）
 * 仅提供角色选择，不支持其他业务功能
 * intent 传入id即当前角色id
 * 返回结果 intent  包含 id，name 即角色id，角色名称
 */
public class RoleActivity extends BaseActivity implements HttpListener<String> {

    TextView tv_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv_right = initRightTv(getString(R.string.btn_finish));
        setTitle(getString(R.string.set_role));
        initListView();
        net();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_right:
                Intent data = new Intent();
                data.putExtra("id", getCheckedRoleId());
                data.putExtra("name", getCheckedRoleName());
                setResult(RESULT_OK, data);
                finish();
                break;
        }
    }

    private String getCheckedRoleId() {
        //遍历所有被选中的角色，将其id存起来
        if (mAdapter.getSelected().isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (AppRole role : mAdapter.getSelected()) {
            sb.append(role.id);
            sb.append(",");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();

    }

    private String getCheckedRoleName() {
        //遍历所有被选中的角色，将其id存起来
        if (mAdapter.getSelected().isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (AppRole role : mAdapter.getSelected()) {
            sb.append(role.name);
            sb.append(",");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();

    }

    @Override
    protected int getContentView() {
        return R.layout.activity_role;
    }

    private final int CALL_BACK_GET_ROLE = 0;
    private NetworkManager mNetworkManager;

    public void net() {
        showLoadingDialog(this, getString(R.string.loading));
        VanCloudApplication app = (VanCloudApplication) getApplication();
        mNetworkManager = app.getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this,
                URLAddr.URL_REGISTER_GET_ROLE_INFO), params, this);
        mNetworkManager.load(CALL_BACK_GET_ROLE, path, this, true);
    }

    private AppRole mUserRole;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALL_BACK_GET_ROLE:
                try {
                    if (mAdapter.getCount() > 0) {
                        return;
                    }
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    List<Role> roleList = JsonDataFactory.getDataArray(Role.class, resutObject.getJSONArray("roles"));
                    List<AppRole> appRoleList = new ArrayList<>();
                    mUserRole = null;
                    if (roleList != null && roleList.size() > 0) {
                        for (Role role : roleList) {
                            AppRole appRole = new AppRole(role.key, role.value);
                            if (AppRole.Type.user.toString().equals(role.type)) {
                                mUserRole = appRole;
                            }
                            appRoleList.add(appRole);
                        }
                    } else {
                        tv_right.setEnabled(false);
                    }
                    String selected = getIntent().getStringExtra("id");
                    if (!TextUtils.isEmpty(selected)) {
                        String[] ids = selected.split(",");
                        for (String id : ids) {
                            for (AppRole role : appRoleList) {
                                if (!TextUtils.isEmpty(role.id) && role.id.equals(id))
                                    mAdapter.addSelected(role);
                            }
                        }
                    } else {
                        mAdapter.addSelected(mUserRole);
                    }
                    mAdapter.add(appRoleList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private RoleAdapter mAdapter;
    private boolean enable = false;

    private void initListView() {
        ListView lv_role = (ListView) findViewById(R.id.lv_role);
        mAdapter = new RoleAdapter();
        lv_role.setAdapter(mAdapter);
        lv_role.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!enable) {
                    enable = !enable;
                    tv_right.setEnabled(enable);
                }
                AppRole role = mAdapter.getItem(position);
                mAdapter.getSelected().clear();
                mAdapter.getSelected().add(role);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    class RoleAdapter extends BaseAdapter {
        private List<AppRole> mAppRoleList;
        private List<AppRole> mSelectedList;

        public RoleAdapter() {
            mSelectedList = new ArrayList<>();
            mAppRoleList = new ArrayList<>();
        }

        public void add(List<AppRole> list) {
            mAppRoleList.addAll(list);
            notifyDataSetChanged();
        }

        public void addSelected(List<AppRole> list) {
            mSelectedList.addAll(list);
            notifyDataSetChanged();
        }

        public void addSelected(AppRole appRole) {
            if (!mSelectedList.contains(appRole))
                mSelectedList.add(appRole);
            notifyDataSetChanged();
        }

        public List<AppRole> getSelected() {
            return mSelectedList;
        }

        @Override
        public int getCount() {
            return mAppRoleList.size();
        }

        @Override
        public AppRole getItem(int position) {
            return mAppRoleList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(RoleActivity.this,
                        R.layout.item_role, null);
                viewHolder = new ViewHolder();
                viewHolder.cb = (CheckBox) convertView.findViewById(R.id.cb);
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            AppRole role = mAppRoleList.get(position);
            viewHolder.cb.setChecked(mSelectedList.contains(role));
            viewHolder.tv_name.setText(role.name);
            return convertView;
        }
    }

    static class ViewHolder {
        private TextView tv_name;
        private CheckBox cb;
    }

    @Override
    public void finish() {
        super.finish();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }
}
