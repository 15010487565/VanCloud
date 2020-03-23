package com.vgtech.vancloud.ui.module.recruit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.Dict;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.WelfareTreatmentAdapter;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by code on 2016/5/30.
 * 福利待遇
 */
public class WelfareTreatmentActivity extends BaseActivity implements View.OnClickListener, HttpListener<String>, WelfareTreatmentAdapter.OnSelectListener {

    private ListView listView;
    //private ImageButton btn_right;
    private TextView tv_right;
    private EditText editText;
    private WelfareTreatmentAdapter welfareTreatmentAdapter;
    private HashMap<String, Dict> mIndexerIds = new HashMap<String, Dict>();
    private HashMap<String, Dict> mIndexerNames = new HashMap<String, Dict>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.recruit_detail_fuli));
        initRightTv(getString(R.string.ok));
        listView = (ListView) findViewById(R.id.lv_content);
        //btn_right = (ImageButton) findViewById(R.id.btn_right);
        tv_right = (TextView) findViewById(R.id.tv_right);
        //btn_right.setVisibility(View.GONE);
        //btn_right.setOnClickListener(this);
        tv_right.setOnClickListener(this);
        welfareTreatmentAdapter = new WelfareTreatmentAdapter(this, new ArrayList<Dict>());
        listView.setAdapter(welfareTreatmentAdapter);
        welfareTreatmentAdapter.setmListener(this);
        net();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_welfare_treatment;
    }

    private void showAlertDialog(String title, CharSequence content, View.OnClickListener positiveListener, View.OnClickListener negativeListener) {
        AlertDialog dialog = new AlertDialog(this).builder().setTitle(title);
        editText = dialog.setEditer();
        editText.setText(content);
        editText.setSelection(content.length());
        dialog.setPositiveButton(getString(R.string.ok), positiveListener)
                .setNegativeButton(getString(R.string.cancel), negativeListener).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
//                if (mIndexerIds.size() == 0) {
//                    showToast(getString(R.string.vancloud_welfare_treatment_prompt));
//                    return;
//                } else {
                Intent data = new Intent();
                data.putExtra("welfare_ids", getIds(mIndexerIds));
                data.putExtra("welfare_names", getNames(mIndexerNames));
                setResult(RESULT_OK, data);
                finish();
//                }
                break;
//            case R.id.btn_right:
//                showAlertDialog(getString(R.string.recruit_add_hint_fuli), "",
//                        new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                String str = editText.getText().toString();
//                                if (TextUtils.isEmpty(str)) {
//                                    showToast(getString(R.string.vancloud_input_welfare_name));
//                                    return;
//                                } else {
//                                    addWelfare(str);
//                                }
//                            }
//                        }, new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//
//                            }
//                        });
//                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private static final int CALL_BACK_OK = 1;
    private static final int CALL_BACK_ADD = 2;

    private NetworkManager mNetworkManager;

    private void net() {
        showLoadingDialog(this, "");
        VanCloudApplication app = (VanCloudApplication) getApplication();
        mNetworkManager = app.getNetworkManager();
        Map<String, String> params = new HashMap<>();
//        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(URLAddr.URL_DICT_VANCLOUD_WELFARE, params, this);
        mNetworkManager.load(CALL_BACK_OK, path, this);
    }

    private void addWelfare(String name) {
        showLoadingDialog(this, "");
        VanCloudApplication app = (VanCloudApplication) getApplication();
        mNetworkManager = app.getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("welfare_name", name);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ENTERPRISE_JOB_ADD_JOB_WELFARES), params, this);
        mNetworkManager.load(CALL_BACK_ADD, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
//        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
//        if (!safe) {
//            return;
//        }
        switch (callbackId) {
            case CALL_BACK_OK:
                List<Dict> dicts = new ArrayList<>();
                try {
                    if (!TextUtils.isEmpty(rootData.responce)) {
//                        String data = rootData.getJson().getString("data");
                        JSONArray jsonArray = new JSONArray(rootData.responce);
                        dicts = JsonDataFactory.getDataArray(Dict.class, jsonArray);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String ids = getIntent().getStringExtra("ids");
                if (!TextUtils.isEmpty(ids)) {
                    String[] idArray = ids.split(",");
                    for (Dict item : dicts) {
                        for (String id : idArray) {
                            if (!TextUtils.isEmpty(item.id) && item.id.equals(id)) {
                                mIndexerIds.put(item.id, item);
                                mIndexerNames.put(item.name, item);
                            }
                        }
                    }
                }
//                else {
//                    for (WelfareTreatment item : welfareTreatments) {
//                        if ("五险一金".equals(item.welfare_name)) {
//                            mIndexerIds.put(item.welfare_id, item);
//                            mIndexerNames.put(item.welfare_name, item);
//                        }
//                    }
//                }
                if (welfareTreatmentAdapter == null) {
                    welfareTreatmentAdapter = new WelfareTreatmentAdapter(this, dicts);
                    listView.setAdapter(welfareTreatmentAdapter);
                } else {
                    List<Dict> list = welfareTreatmentAdapter.getMlist();
                    list.clear();
                    list.addAll(dicts);
                    welfareTreatmentAdapter.myNotifyDataSetChanged(list);
                }

                break;
            case CALL_BACK_ADD:
                net();
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    public static String getIds(HashMap<String, Dict> map) {
        StringBuffer buffer = new StringBuffer();

        Set<String> keySet = map.keySet();
        for (String keyName : keySet) {
            Dict item = map.get(keyName);
            buffer.append(item.id).append(",");
        }
        if (buffer.toString().length() > 1) {
            return buffer.toString().substring(0, buffer.toString().length() - 1);
        } else {
            return buffer.toString().substring(0, buffer.toString().length());
        }
    }

    public static String getNames(HashMap<String, Dict> map) {
        StringBuffer buffer = new StringBuffer();

        Set<String> keySet = map.keySet();
        for (String keyName : keySet) {
            Dict item = map.get(keyName);
            buffer.append(item.name).append(",");
        }
        if (buffer.toString().length() > 1) {
            return buffer.toString().substring(0, buffer.toString().length() - 1);
        } else {
            return buffer.toString().substring(0, buffer.toString().length());
        }
    }

    @Override
    public void OnSelected(Dict item) {

        mIndexerIds.put(item.id, item);
        mIndexerNames.put(item.name, item);
    }

    @Override
    public void OnUnSelected(Dict item) {
        mIndexerIds.remove(item.id);
        mIndexerNames.remove(item.name);
    }

    @Override
    public boolean OnIsSelect(Dict item) {
        return mIndexerIds.containsKey(item.id);
    }
}
