package com.vgtech.vancloud.ui.register.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.vancloud.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.EditDepartmentBean;
import com.vgtech.common.api.Group;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;
import com.vgtech.vancloud.ui.common.group.GroupAdapter;
import com.vgtech.vancloud.ui.common.group.GroupUtils;
import com.vgtech.common.api.Node;
import com.vgtech.vancloud.ui.common.group.tree.TreeHelper;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.AlertDialog;
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
 * Created by code on 2015/10/19.
 * 编辑设置部门
 */
public class EditDepartmentActivity extends BaseActivity implements HttpListener<String> ,GroupAdapter.OnTreeNodeClickListener{

    @InjectView(R.id.listview)
    ListView listview;
    @InjectView(R.id.company_tv)
    TextView companyTv;
    private final int GET_INDUSDEPARTRINFO = 1;
    private final int DELETEDEPART = 2;
    private final int UPDATEDEPARTNAME = 3;
    private final int SAVEINFOPARTMENT = 4;
    private NetworkManager mNetworkManager;
    private GroupAdapter mAdapter;
    private List<Group> onlyDeparts;
    private List<EditDepartmentBean> departmentInfos;
    private List<Node> treeNode;
    private String department_id,name;
    private String indus_id;

    @Override
    protected int getContentView() {
        return R.layout.activity_edit_department;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        indus_id = getIntent().getExtras().getString("indus_id");
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
        String departVersion = PrfUtils.getPrfparams(this, "departVersion");
        if (!TextUtil.isEmpty(departVersion))
            params.put("version", departVersion);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_GETCOMPANYDEPARTINFO), params, this);
        mNetworkManager.load(GET_INDUSDEPARTRINFO, path, this);
    }

    /**
     * 删除组织结构部门
     */
    public void deletedepartAction(String parent_id, String department_id) {
        showLoadingDialog(this, getString(R.string.dataloading));

        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("parent_id", parent_id);
        params.put("department_id", department_id);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_DELETEDEPART), params, this);
        mNetworkManager.load(DELETEDEPART, path, this);
    }

    /**
     * 新增,修改组织机构部门
     */
    public void updatedepartnameAction(int form, String parent_id, String department_id, String departname, String changed_department_id) {

        showLoadingDialog(this, getString(R.string.dataloading));
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("parent_id", parent_id);
        if (form == 0) { // 修改部门名称
            params.put("type", "0");
            params.put("department_id", department_id);
            params.put("departname", departname);
        }
        if (form == 1) { // 增加部门
            params.put("type", "1");
            params.put("departname", departname);
        }
        if (form == 2) { // 修改部门关系
            params.put("type", "2");
            params.put("department_id", department_id);
            params.put("changed_department_id", changed_department_id);
        }
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_UPDATEDEPARTNAME), params, this);
        mNetworkManager.load(UPDATEDEPARTNAME, path, this);
    }

    /**
     * 企业部门保存
     */
    public void savaInfo() {

        showLoadingDialog(this, getString(R.string.dataloading));

        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_UPDATECOMPANYDEPART), params, this);
        mNetworkManager.load(SAVEINFOPARTMENT, path, this);
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
                departmentInfos = new ArrayList<>();
                onlyDeparts = new ArrayList<>();
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    departmentInfos = JsonDataFactory.getDataArray(EditDepartmentBean.class, resutObject.getJSONArray("departs"));
                    department_id = departmentInfos.get(0).department_id;
                    JSONObject resultObj = jsonObject.getJSONObject("data").getJSONArray("departs").getJSONObject(0);
                    onlyDeparts = GroupUtils.initGroups(resultObj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                updateUIAction();
                break;
            case DELETEDEPART:
                //删除成功
                loadData();
                break;
            case UPDATEDEPARTNAME:
                SharedPreferences preferences = PrfUtils.getSharePreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("step", "" + "1");
                editor.commit();
                //修改成功
                loadData();
                break;
            case SAVEINFOPARTMENT:
                SharedPreferences preference = PrfUtils.getSharePreferences(this);
                SharedPreferences.Editor editors = preference.edit();
                editors.putString("step", "" + "2");
                editors.commit();
                //保存成功,跳转到设置职位界面
                Intent intent = new Intent(this, DefaultPositionActivity.class);
                intent.putExtra("indus_id", indus_id);
                startActivity(intent);
                Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
                sendBroadcast(reveiverIntent);
                break;
        }
    }

    private EditText mNameEt;

    public void updateUIAction() {
        try {
            treeNode = TreeHelper.convetData2Node(onlyDeparts);
            mAdapter = new GroupAdapter(listview, EditDepartmentActivity.this, treeNode, 0, false);
            mAdapter.setOnTreeNodeClickListener(this);
            mAdapter.infoedit = true;
            mAdapter.setEditDepartmentListener(new GroupAdapter.EditDepartmentListener() {
                @Override
                public void EditDepartmentAction(final Node node) {
                    new ActionSheetDialog(EditDepartmentActivity.this)
                            .builder()
                            .setCancelable(true)
                            .setCanceledOnTouchOutside(true)
                            .addSheetItem(getString(R.string.add_partment), ActionSheetDialog.SheetItemColor.Blue,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            AlertDialog dialog = new AlertDialog(EditDepartmentActivity.this).builder().setTitle(getString(R.string.child_partment_name));
                                            mNameEt = dialog.setEditer();
                                            mNameEt.setSelection(mNameEt.getText().length());
                                            dialog.setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    String cn = mNameEt.getText().toString();
                                                    if (TextUtils.isEmpty(cn)){
                                                        showToast(getString(R.string.input_name));
                                                        return;
                                                    }
                                                    updatedepartnameAction(1, String.valueOf(node.getId()), "", cn, "");
                                                }
                                            }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                }
                                            }).show();
                                        }
                                    })

                            .addSheetItem(getString(R.string.edit_partment), ActionSheetDialog.SheetItemColor.Blue,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            AlertDialog dialog = new AlertDialog(EditDepartmentActivity.this).builder().setTitle(getString(R.string.partment_name));
                                            mNameEt = dialog.setEditer();
                                            mNameEt.setSelection(mNameEt.getText().length());
                                            mNameEt.setText(node.getName());
                                            dialog.setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    String cn = mNameEt.getText().toString();
                                                    if (TextUtils.isEmpty(cn)){
                                                        showToast(getString(R.string.input_name));
                                                        return;
                                                    }
                                                    updatedepartnameAction(0, String.valueOf(node.getpId()), String.valueOf(node.getId()), cn, "");
                                                }
                                            }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                }
                                            }).show();
                                        }
                                    })
                            .addSheetItem(getString(R.string.del_partment), ActionSheetDialog.SheetItemColor.Blue,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            if (node.getChildren() != null && node.getChildren().size() > 0) {
                                                Toast.makeText(EditDepartmentActivity.this, getString(R.string.edit_department_toast), Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                            AlertDialog alertDialog = new AlertDialog(EditDepartmentActivity.this).builder().setTitle(getString(R.string.edit_department_title)).setMsg(getString(R.string.edit_department_choose));
                                            alertDialog.setPositiveButton(getString(R.string.yes), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    deletedepartAction(String.valueOf(node.getpId()), String.valueOf(node.getId()));
                                                }
                                            });
                                            alertDialog.setNegativeButton(getString(R.string.no), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                }
                                            });
                                            alertDialog.show();
                                        }
                                    })
                            .show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        listview.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        companyTv.setText(new PreferencesController().getAccount().tenant_name);
    }

    @OnClick(R.id.company_layout)
    public void chooseEditAction() {
        AlertDialog dialog = new AlertDialog(EditDepartmentActivity.this).builder().setTitle(getString(R.string.child_partment_name));
        mNameEt = dialog.setEditer();
        mNameEt.setSelection(mNameEt.getText().length());
        dialog.setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cn = mNameEt.getText().toString();
                updatedepartnameAction(1, department_id, "", cn, "");
            }
        }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    @OnClick(R.id.tv_right_edit)
    public void saveAction() {
        savaInfo();
    }

    @OnClick(R.id.back)
    public void close() {
        finish();
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onClick(Node node, int position) {

    }
}
