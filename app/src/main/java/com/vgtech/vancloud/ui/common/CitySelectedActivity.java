package com.vgtech.vancloud.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.vancloud.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.Department;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.common.group.DepartSelectedAdapter;
import com.vgtech.vancloud.ui.common.group.GroupAdapter;
import com.vgtech.common.api.Node;
import com.vgtech.vancloud.ui.common.group.tree.TreeHelper;
import com.vgtech.vancloud.ui.common.group.tree.TreeListViewAdapter;
import com.vgtech.common.PrfUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangshaofang on 2015/9/22.
 */
public class CitySelectedActivity extends BaseActivity implements GroupAdapter.OnTreeNodeClickListener, HttpListener<String> {
    private ListView mTree;
    private DepartSelectedAdapter mAdapter;
    private static List<Node> mAllNodes;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dismisLoadingDialog();
            mAllNodes = (List<Node>) msg.obj;
            initCityAdapter();
        }
    };
    private TextView mRightTv;
    private NetworkManager mNetworkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.lable_city_select));
        mTree = (ListView) findViewById(android.R.id.list);
        mRightTv = initRightTv(getString(R.string.ok));
        mRightTv.setEnabled(false);
        mNetworkManager = getAppliction().getNetworkManager();
        if (mAllNodes != null) {
            initCityAdapter();
        } else
            updataUIAction();

    }

    private void initCityAdapter() {
        try {
            mAdapter = new DepartSelectedAdapter(mTree, CitySelectedActivity.this, mAllNodes, 0, false, mRightTv);
            mAdapter.setOnTreeNodeClickListener(CitySelectedActivity.this);
            Node node = getIntent().getParcelableExtra("node");
            if (node != null) {
                mAdapter.setSelect(node);
                mRightTv.setEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTree.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void finish() {
        super.finish();
        mNetworkManager.cancle(this);
    }

    @Override
    protected int getContentView() {
        return R.layout.depart_selectedlist;
    }

    public void updataUIAction() {
        showLoadingDialog(this, "");
        Map<String, String> params = new HashMap<>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        String cityVersion = PrfUtils.getPrfparams(this, "cityVersion");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this,URLAddr.URL_POSITIONS_PLACES), params, this);
        if (!TextUtils.isEmpty(cityVersion)) {

            final JSONObject jsonObject = mNetworkManager.getAcache().getAsJSONObject(cityVersion);
            if (jsonObject != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        initCityData(jsonObject);
                    }
                }).start();
            } else {
                mNetworkManager.load(1, path, this,true);
            }
        } else {
            mNetworkManager.load(1, path, this,true);
        }
    }

    private void initCityData(JSONObject rootObject) {
        List<Department> groups = new ArrayList<Department>();
        try {
            JSONArray jsonArray = rootObject.getJSONObject("data").getJSONArray("rows");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String firstId = jsonObject.getString("id");
                groups.add(new Department(firstId, jsonObject.getString("name"), ""));
                JSONArray cityArray = jsonObject.getJSONArray("sub_data");
                for (int j = 0; j < cityArray.length(); j++) {
                    JSONObject cityObject = cityArray.getJSONObject(j);
                    String secondId = cityObject.getString("id");
                    groups.add(new Department(secondId, cityObject.getString("name"), firstId));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List mDatas = new ArrayList();
        mDatas.addAll(groups);
        try {
            List<Node> nodes = TreeHelper.convetData2Node(mDatas);
            Message msg = new Message();
            msg.what = 1;
            msg.obj = nodes;
            mHandler.sendMessage(msg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    private boolean mInit;
    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            dismisLoadingDialog();
            return;
        }
        switch (callbackId) {
            case 1:
                try {
                    if(mInit)
                        return;
                    mInit = true;
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    String version = resutObject.getString("version");
                    PrfUtils.savePrfparams(this, "cityVersion", version);
                    mNetworkManager.getAcache().put(version, jsonObject);
                    initCityData(jsonObject);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    dismisLoadingDialog();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                Node node = mAdapter.getSelectedNode();
                Intent intent = new Intent();
                intent.putExtra("node", node);
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    public void onClick(Node node, int position) {
        try {
            mAdapter.setOnTreeNodeClickListener(new TreeListViewAdapter.OnTreeNodeClickListener() {
                @Override
                public void onClick(Node node, int position) {
                    if (node.isLeaf()) {
                        {
                            mRightTv.setEnabled(true);
                            mAdapter.setSelect(node);
                        }
                    }
                }
            });
        } catch (Exception e) {
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
