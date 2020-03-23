package com.vgtech.vancloud.ui.register.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.vancloud.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.Group;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.reciver.GroupReceiver;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;
import com.vgtech.vancloud.ui.common.group.GroupAdapter;
import com.vgtech.vancloud.ui.common.group.GroupUtils;
import com.vgtech.common.api.Node;
import com.vgtech.vancloud.ui.common.group.tree.TreeHelper;
import com.vgtech.vancloud.ui.common.group.tree.TreeListViewAdapter;
import com.vgtech.common.PrfUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by code on 2015/10/22.
 * 选择所属部门
 */
public class ChooseSubPartmentActivity extends BaseActivity implements HttpListener<String>{

    @InjectView(R.id.company_tv)
    TextView companyTv;
    @InjectView(R.id.listview)
    ListView listview;
    private final int GET_INDUSDEPARTRINFO = 1;
    private final int SETNEWSTAFFSDEPARTMENT = 2;
    private static final int CALLBACK_CONTACT_LIST_INFO = 3;
    private NetworkManager mNetworkManager;
    private GroupAdapter mAdapter;
    private String userids; //选择设置人的id
    private List<Group> onlyDeparts;
    private List<Node> treeNode;

    @Override
    protected int getContentView() {
        return R.layout.activity_choose_subpartment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        setTitle(getString(R.string.choose_depart));
        userids = getIntent().getExtras().getString("userIds");
        loadData();
    }

    /**
     * 获取企业部门信息
     */
    public void loadData() {
        showLoadingDialog(this, getString(R.string.dataloading));
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_GETCOMPANYDEPARTINFO), params, this);
        mNetworkManager.load(GET_INDUSDEPARTRINFO, path, this);
    }

    /**
     * 设置新员工所属部门
     */
    public void setsubpartmentAction(String department_id) {
        showLoadingDialog(this, getString(R.string.dataloading));
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("userids", userids);
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("department_id", department_id);
        params.put("n", "12");
        params.put("s", "0");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SETNEWSTAFFSDEPARTMENT), params, this);
        mNetworkManager.load(SETNEWSTAFFSDEPARTMENT, path, this);
    }

    /**
     * 更新本地信息
     */
    private void loadContactInfo() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(this));
        String user_version = PrfUtils.getPrfparams(this, "user_version");
        if (!TextUtil.isEmpty(user_version))
            params.put("version", user_version);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_GROUP_INFO), params, this);
        mNetworkManager.load(CALLBACK_CONTACT_LIST_INFO, path, this);
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
                onlyDeparts = new ArrayList<>();
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resultObj = jsonObject.getJSONObject("data").getJSONArray("departs").getJSONObject(0);
                    onlyDeparts = GroupUtils.initGroups(resultObj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                companyTv.setText(new PreferencesController().getAccount().tenant_name);
                updateUIAction();
                break;
            case SETNEWSTAFFSDEPARTMENT:
                loadContactInfo();
                break;
            case CALLBACK_CONTACT_LIST_INFO:
                sendBroadcast(new Intent(GroupReceiver.REFRESH));
                finish();
                break;
        }
    }

    public void updateUIAction() {
        try {
            treeNode = TreeHelper.convetData2Node(onlyDeparts);
            mAdapter = new GroupAdapter(listview, ChooseSubPartmentActivity.this, treeNode, 0, false);
            mAdapter.join = true;
            mAdapter.setJoinDepartmentListener(new GroupAdapter.JoinDepartmentListener() {
                @Override
                public void JoinDepartmentAction(Node node) {
                    if(getIntent().getBooleanExtra("get",false)){
                        Intent data = new Intent();
                        data.putExtra("id",node.getId());
                        data.putExtra("name",node.getName());
                        setResult(RESULT_OK,data);
                        finish();
                    }else {
                        setsubpartmentAction(String.valueOf(node.getId()));
                    }
                }
            });
            mAdapter.setOnTreeNodeClickListener(new TreeListViewAdapter.OnTreeNodeClickListener() {
                @Override
                public void onClick(final Node node, int position) {
                    if (node.getChildren().size() < 0) {
                        setsubpartmentAction(String.valueOf(node.getId()));
                    }
                }
            });
            listview.setAdapter(mAdapter);
            companyTv.setText(new PreferencesController().getAccount().tenant_name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.company_layout)
    public void setCompany() {
        setsubpartmentAction(String.valueOf(0));
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }
}
