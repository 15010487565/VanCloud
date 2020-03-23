package com.vgtech.vancloud.ui.module.me;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.Update;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.web.WebActivity;
import com.vgtech.vancloud.utils.UpdateManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 关于万客
 * Created by Nick on 2016/1/19.
 */
public class AboutVanCloudActivity extends BaseActivity implements HttpListener<String> {

    TextView currentVersionCode;

    RelativeLayout btnNewVersion;
    TextView newVersionSign;
    TextView tvVersionCode;
    TextView newVersionLable;

    RelativeLayout btnfeatureIntroduce;
    LinearLayout btnSystemNotify;
    LinearLayout btnPrivacyClause;

    private Update mUpdate;
    private NetworkManager mNetworkManager;
    private static final int CALLBACK_UPDATE = 10;


    //personal  company
    private String style;

    @Override
    protected int getContentView() {
        return R.layout.about_vancloud;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.about_vancloud));
        style = getIntent().getStringExtra("style");

        initView();
        initData();
        setListener();
        checkUpdate();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_new_version:
//                isFirstCheckUpdate = false;
                checkUpdate();
                break;
            case R.id.btn_feature_introduce:
                try {
                    PackageManager manager = getPackageManager();
                    PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
                    Intent intent = new Intent(AboutVanCloudActivity.this, WebActivity.class);
                    intent.putExtra("title", getString(R.string.feature_introduce));
                    intent.putExtra("style",style);
                    String url = ApiUtils.generatorUrl(AboutVanCloudActivity.this, String.format(URLAddr.URL_NOTIFICATIONS,""+info.versionCode));
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                }

                break;
            case R.id.btn_system_notify:
                Intent systemNotify = new Intent(this, SystemNotifyListActivity.class);
                systemNotify.putExtra("style",style);
                startActivity(systemNotify);
                break;
            case R.id.btn_privacy_clause:
                Intent intent = new Intent(AboutVanCloudActivity.this, WebActivity.class);
                intent.putExtra("title", getString(R.string.privacy_clause));
//                String path = String.format(URLAddr.URL_NOTIFICATIONS_PRIVACY,PrfUtils.getAppLanguage(this));//"notifications/privacy/" + PrfUtils.getAppLanguage(this) + ".html";
//                String url = ApiUtils.generatorUrl(this, path);

                String url = ApiUtils.generatorUrl(AboutVanCloudActivity.this,URLAddr.URL_PRIVACY_POLICY);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    public void initView() {
        currentVersionCode = (TextView) findViewById(R.id.current_version_code);
        btnNewVersion = (RelativeLayout) findViewById(R.id.btn_new_version);
        newVersionSign = (TextView) findViewById(R.id.new_version_sign);
        tvVersionCode = (TextView) findViewById(R.id.version_code);
        newVersionLable = (TextView) findViewById(R.id.new_version_lable);

        btnfeatureIntroduce = (RelativeLayout) findViewById(R.id.btn_feature_introduce);
        btnSystemNotify = (LinearLayout) findViewById(R.id.btn_system_notify);
        btnPrivacyClause = (LinearLayout) findViewById(R.id.btn_privacy_clause);
    }

    private void initData() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            currentVersionCode.setText(String.format(getString(R.string.current_version), info.versionName));
            tvVersionCode.setText(info.versionName);
        } catch (Exception e) {
            final String defaultVersionCode = "0.0.0";
            currentVersionCode.setText(String.format(getString(R.string.current_version), defaultVersionCode));
            tvVersionCode.setText(defaultVersionCode);
        }
    }

    private void setListener() {
        btnNewVersion.setOnClickListener(this);
        btnfeatureIntroduce.setOnClickListener(this);
        btnSystemNotify.setOnClickListener(this);
        btnPrivacyClause.setOnClickListener(this);
    }

    private void checkUpdate() {

        newVersionLable.setText(getString(R.string.check_new_version));
        newVersionSign.setVisibility(View.GONE);
        tvVersionCode.setVisibility(View.GONE);

        mNetworkManager = getAppliction().getNetworkManager();
        showLoadingDialog(this, "");
        Map<String, String> params = new HashMap<String, String>();
        String tenantId = PrfUtils.getTenantId(this);
        if(TextUtils.isEmpty(tenantId))
            tenantId = "0";
        params.put("tenantid", tenantId);
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("devicetype", "android");
        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(),
                    0);
            int versionCode = info.versionCode;
            params.put("version", String.valueOf(versionCode));
        } catch (Exception e) {

        }
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_COMMOM_VERSION), params, this);
        mNetworkManager.load(CALLBACK_UPDATE, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_UPDATE:
                JSONObject jsonObject = rootData.getJson();
                try {
                    mUpdate = JsonDataFactory.getData(Update.class, jsonObject.getJSONObject("data").getJSONObject("version"));
                    PackageManager manager = this.getPackageManager();
                    PackageInfo info = manager.getPackageInfo(getPackageName(),
                            0);
                    int versionCode = info.versionCode;
                    if (versionCode < Integer.parseInt(mUpdate.vercode)) {

//                        if (!isFirstCheckUpdate) {
                            TextView tipTv = (TextView) findViewById(R.id.tv_version);
                            final String txt = getString(R.string.update_vercode) + mUpdate.vername;
                            tipTv.setText(txt);
                            newVersionLable.setText(getString(R.string.new_version));
                            newVersionSign.setVisibility(View.VISIBLE);
                            showUpdateTip();
//                        } else {
                            tvVersionCode.setVisibility(View.VISIBLE);
                            tvVersionCode.setText(info.versionName);
//                        }

                    } else {
                        newVersionLable.setText(getString(R.string.check_new_version));
                        newVersionSign.setVisibility(View.GONE);
                        tvVersionCode.setVisibility(View.GONE);
                        Toast.makeText(this, R.string.soft_update_no, Toast.LENGTH_SHORT).show();
                    }
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

    private void showUpdateTip() {
        AlertDialog alertDialog = new AlertDialog(this).builder().setTitle(getString(R.string.checked_new_version))
                .setMsg(mUpdate.des);
        alertDialog.setLeft();
        alertDialog.setPositiveButton(getString(R.string.update_now), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateManager manager = new UpdateManager(AboutVanCloudActivity.this);
                // 检查软件更新
                manager.checkUpdate(mUpdate.downloadpath);
            }
        });
        if (TextUtils.isEmpty(mUpdate.isforceUpdate) || mUpdate.isforceUpdate.equals("N")) {
            alertDialog.setNegativeButton(getString(R.string.update_later), new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
        alertDialog.show();
    }

    @Override
    public void finish() {
        super.finish();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }

}
