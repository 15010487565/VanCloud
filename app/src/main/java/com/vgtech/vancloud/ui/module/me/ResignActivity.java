package com.vgtech.vancloud.ui.module.me;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.Position;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.DataUtils;
import com.vgtech.common.utils.EditionUtils;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.LoginActivity;
import com.vgtech.vancloud.ui.group.OrganizationActivity;
import com.vgtech.vancloud.ui.group.OrganizationVanCloudActivity;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by zhangshaofang on 2015/11/12.
 */
public class ResignActivity extends BaseActivity implements HttpListener<String> {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.del_user));
        initView();
    }

    private NetworkManager mNetworkManager;
    private String mUserId;
    private List<Position> mPrositionList;

    private void initView() {
        Intent intent = getIntent();
        mUserId = intent.getStringExtra("userId");
        String name = intent.getStringExtra("name");
        TextView nameTv = (TextView) findViewById(R.id.tv_name);
        nameTv.setText(name);
        findViewById(R.id.btn_reasion).setOnClickListener(this);
        findViewById(R.id.btn_time).setOnClickListener(this);
        findViewById(R.id.btn_work_time).setOnClickListener(this);
        findViewById(R.id.btn_del_user).setOnClickListener(this);
        mNetworkManager = getAppliction().getNetworkManager();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reasion:
                if (mPrositionList == null) {
                    loadPositionInfo();
                } else {
                    showPositionSelected();
                }
                break;
            case R.id.btn_time: {
                TextView timeTv = (TextView) findViewById(R.id.tv_resign_time);
                DataUtils.showDateSelect(this, timeTv, DataUtils.DATE_TYPE_ALL, null);
            }
            break;
            case R.id.btn_work_time:
                TextView timeTv = (TextView) findViewById(R.id.tv_work_time);
                DataUtils.showDateSelect(this, timeTv, DataUtils.DATE_TYPE_ALL, null);
                break;
            case R.id.btn_del_user:
                doSubmit("leave");
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void doSubmit(String select_mod) {
        TextView reasionTv = (TextView) findViewById(R.id.tv_resign_reasion);
        TextView tv_resign_time = (TextView) findViewById(R.id.tv_resign_time);
        TextView tv_work_time = (TextView) findViewById(R.id.tv_work_time);
        TextView et_remark = (TextView) findViewById(R.id.et_remark);
        String reasion = reasionTv.getText().toString();
        String resignTime = tv_resign_time.getText().toString();
        String workTime = tv_work_time.getText().toString();
        String remark = et_remark.getText().toString();
        if (TextUtils.isEmpty(reasion)) {
            Toast.makeText(this, R.string.toast_resign, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(resignTime)) {
            Toast.makeText(this, R.string.toast_resign_time, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(workTime)) {
            Toast.makeText(this, R.string.toast_resign_work_time, Toast.LENGTH_SHORT).show();
            return;
        }
        showLoadingDialog(this, "");
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("user_id", mUserId);
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("reason", reasion);
        params.put("leave_time", "" + Utils.dateFormat(resignTime));
        params.put("last_work_time", "" + Utils.dateFormat(workTime));
        params.put("remark", remark);
        params.put("select_mod", select_mod);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_DEALLEAVER), params, this);
        mNetworkManager.load(1001, path, this);
    }


    private void loadPositionInfo() {
        showLoadingDialog(this, "");
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_GET_LEAVE_REASON), params, this);
        mNetworkManager.load(1002, path, this);
    }

    private void showPositionSelected() {
        ActionSheetDialog actionSheetDialog = new ActionSheetDialog(this)
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true);

        for (Position option : mPrositionList) {
            actionSheetDialog.addSheetItem(option.value, ActionSheetDialog.SheetItemColor.Blue,
                    new ActionSheetDialog.OnSheetItemClickListener() {
                        @Override
                        public void onClick(int which) {
                            Position position = mPrositionList.get(which);
                            String value = position.value;
                            TextView reasionTv = (TextView) findViewById(R.id.tv_resign_reasion);
                            reasionTv.setText(value);
                        }
                    });
        }
        actionSheetDialog.show();
    }

    @Override
    protected int getContentView() {
        return R.layout.resign;
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        if (callbackId == 1002) {
            boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
            if (!safe) {
                return;
            }
        }
        switch (callbackId) {
            case 1001:
                if (rootData != null) {
                    boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, false);
                    if (safe) {
                        Toast.makeText(this, R.string.operation_success, Toast.LENGTH_SHORT).show();
                        if (mUserId.equals(PrfUtils.getUserId(this))) {
                            EditionUtils.setCurrentEdition(this, EditionUtils.EDITION_PERSONAL);
                            ShortcutBadger.with(this).count(0);
//                            FileUtils.writeString("ResignActivity -> 重新登录界面，退出到登录界面！\r\n");
                            Intent intent = new Intent(this, LoginActivity.class);
                            Utils.clearUserInfo(this);
                            startActivity(intent);
                            Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
                            sendBroadcast(reveiverIntent);
                            finish();
                        } else {
                            setResult(RESULT_OK);
                            finish();
                        }
                    } else {
                        if (rootData.code == 1500) {
                            new AlertDialog(this).builder().setTitle(getString(R.string.prompt))
                                    .setMsg(rootData.msg)
                                    .setNegativeButton(getString(R.string.vancloud_dissolve_the_company), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            doSubmit("dissolve");
                                        }
                                    }).setPositiveButton(getString(R.string.vancloud_set_administrator), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (TenantPresenter.isVanTop(ResignActivity.this)) {
                                        startActivity(new Intent(ResignActivity.this, OrganizationActivity.class));
                                    } else {
                                        startActivity(new Intent(ResignActivity.this, OrganizationVanCloudActivity.class));
                                    }
                                }
                            }).show();
                        } else {
                            Toast.makeText(this, rootData.msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                break;
            case 1002:
                try {
                    JSONArray jsonArray = rootData.getJson().getJSONArray("data");
                    mPrositionList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String name = jsonArray.getJSONObject(i).getString("name");
                        mPrositionList.add(new Position(name));
                    }
                    showPositionSelected();
                } catch (Exception e) {
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
}
