package com.vgtech.vancloud.ui.register.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.DepartmentInfo;
import com.vgtech.common.api.Group;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.api.UserAccount;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.DepartmentInfoDialogAdapter;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;
import com.vgtech.vancloud.ui.common.group.GroupAdapter;
import com.vgtech.vancloud.ui.common.group.GroupUtils;
import com.vgtech.common.api.Node;
import com.vgtech.vancloud.ui.common.group.tree.TreeHelper;
import com.vgtech.vancloud.ui.common.group.tree.TreeListViewAdapter;
import com.vgtech.common.PrfUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by code on 2015/10/19.
 * 设置部门
 */
public class SetupDepartmentActivity extends BaseActivity implements HttpListener<String>{

    @InjectView(R.id.industry_tv)
    TextView industryTv;
    @InjectView(R.id.arrow)
    ImageView arrow;
    @InjectView(R.id.listview)
    ListView listview;
    @InjectView(R.id.company_tv)
    TextView companyTv;
    private final int GET_INDUSDEPARTRINFO = 1;
    private final int GET_SAVEINDUSDEPARTRINFO = 2;
    private NetworkManager mNetworkManager;
    private Dialog allMsg;
    private View allMsgView;
    private GridView gridview;
    private boolean isShow = false;
    private DepartmentInfoDialogAdapter adapter;
    private String indus_id;
    private int position;
    private List<DepartmentInfo> departmentInfos;//各行业list
    private List<Group> onlyDeparts;//单个行业对应的部门信息
    private List<Node> treeNode;

    @Override
    protected int getContentView() {
        return R.layout.activity_setup_department;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        initData();
        loadData();
    }

    private void initData() {
        allMsgView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.view_department_dialog, null);
        allMsg = new AlertDialog.Builder(this).create();
        allMsg.setCanceledOnTouchOutside(false);
        gridview = (GridView) allMsgView.findViewById(R.id.grid_view);
        adapter = new DepartmentInfoDialogAdapter(this, new ArrayList<DepartmentInfo>());
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        mReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Actions.ACTION_DEPARTMENT));
        SharedPreferences preferences = PrfUtils.getSharePreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("step", "" + "0");
        editor.commit();
    }

    //获取行业及行业部门信息
    public void loadData() {
        showLoadingDialog(this, getString(R.string.dataloading));
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_INDUSDEPARTRINFO), params, this);
        mNetworkManager.load(GET_INDUSDEPARTRINFO, path, this);
    }

    //企业部门保存
    public void savaInfo() {
        showLoadingDialog(this, getString(R.string.dataloading));
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("indus_id", indus_id);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_UPDATECOMPANYDEPART), params, this);
        mNetworkManager.load(GET_SAVEINDUSDEPARTRINFO, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case GET_INDUSDEPARTRINFO:
                departmentInfos = new ArrayList<DepartmentInfo>(); //行业list
                onlyDeparts = new ArrayList<>(); //单个对应行业的部门信息

                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    departmentInfos = JsonDataFactory.getDataArray(DepartmentInfo.class, resutObject.getJSONArray("info"));
                    industryTv.setText(getString(R.string.choose_indus_mode) + departmentInfos.get(0).indus_name);
                    indus_id = departmentInfos.get(0).indus_id;

                    JSONObject resultObj = jsonObject.getJSONObject("data").getJSONArray("info").getJSONObject(0).getJSONArray("departs").getJSONObject(0);
                    onlyDeparts = GroupUtils.initGroups(resultObj);

                    Intent action = new Intent();
                    action.setAction(Actions.ACTION_DEPARTMENT);
                    action.putExtra("indus_id", indus_id);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(action);

                    updateUIAction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (departmentInfos != null && departmentInfos.size() > 0) {
                    adapter = new DepartmentInfoDialogAdapter(this, departmentInfos);
                    gridview.setAdapter(adapter);
                }
                PreferencesController preferencesController = new PreferencesController();
                preferencesController.context = this;
                UserAccount userAccount = preferencesController.getAccount();
                companyTv.setText(userAccount.tenant_name);
                break;
            case GET_SAVEINDUSDEPARTRINFO:
                SharedPreferences preferences = PrfUtils.getSharePreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("step", "" + "2");
                editor.commit();
                //跳转到设置职位界面
                Intent intent = new Intent(this, DefaultPositionActivity.class);
                intent.putExtra("indus_id", indus_id);
                intent.putExtra("position", position);
                startActivity(intent);
                finish();
                break;
        }
    }
    private  GroupAdapter mAdapter;
    /**设置listview数据*/
    public void updateUIAction(){

        try {
            treeNode = TreeHelper.convetData2Node(onlyDeparts);
             mAdapter = new GroupAdapter(listview, SetupDepartmentActivity.this, treeNode, 0, false);

            mAdapter.setOnTreeNodeClickListener(new TreeListViewAdapter.OnTreeNodeClickListener() {
                @Override
                public void onClick(Node node, int position) {

                }
            });
            listview.setAdapter(mAdapter);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 选择行业
     */
    @OnClick(R.id.industry_layout)
    public void chooseindustry() {
        showDialog();
        if (isShow) {
            arrow.setBackgroundResource(R.mipmap.up_icon);
        }
    }

    /**
     * 直接使用预设部门
     */
    @OnClick(R.id.btn_default)
    public void savaAction() {
        savaInfo();
    }

    /**
     * 自己设置
     */
    @OnClick(R.id.btn_div)
    public void divAction() {
        Intent intent = new Intent(this, EditDepartmentActivity.class);
        intent.putExtra("indus_id", indus_id);
        startActivity(intent);
    }

    @OnClick(R.id.back)
    public void close() {
        finish();
    }

    public void showDialog() {
        allMsg.show();
        allMsg.getWindow().setContentView((RelativeLayout) allMsgView);
        isShow = true;
    }

    public void closeDialog() {
        allMsg.dismiss();
        isShow = false;
        arrow.setBackgroundResource(R.mipmap.down_icon);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        mReceiver = null;
        super.onDestroy();
    }

    private Receiver mReceiver;
    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Actions.ACTION_DEPARTMENT.equals(intent.getAction())) {
                indus_id = intent.getExtras().getString("indus_id");
                position = intent.getExtras().getInt("position");
                for (int i = 0; i < departmentInfos.size(); i++) {
                    String tmpindus_id = departmentInfos.get(i).indus_id;
                    if (tmpindus_id.equals(indus_id)) {
                        industryTv.setText(getString(R.string.choose_indus_mode) + departmentInfos.get(i).indus_name);
                        indus_id = departmentInfos.get(i).indus_id;
                        try {
                            JSONObject temp = departmentInfos.get(i).getJson().getJSONArray("departs").getJSONObject(0);
                            onlyDeparts = GroupUtils.initGroups(temp);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                updateUIAction();
                closeDialog();
            }
        }
    }
}
