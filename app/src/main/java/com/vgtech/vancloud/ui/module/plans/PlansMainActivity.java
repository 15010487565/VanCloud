package com.vgtech.vancloud.ui.module.plans;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.common.view.ShareActionSheet;
import com.vgtech.common.utils.ShareUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by code on 2015/11/4.
 * 万客计划
 */
public class PlansMainActivity extends BaseActivity implements HttpListener<String> {

    private final int PROMOTIONSSHAREMODULE = 1;
    private NetworkManager mNetworkManager;
    private int type;

    @Override
    protected int getContentView() {
        return R.layout.activity_plans_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        setTitle(getString(R.string.vg_plans));
        initRightTv(getString(R.string.plans_main_indes));
        String qrCodeUri = ApiUtils.generatorUrl(this,URLAddr.URL_EWM);
        SimpleDraweeView qrImage = (SimpleDraweeView)findViewById(R.id.iv_pic);
        ImageOptions.setImage(qrImage,qrCodeUri);
    }

    private void initDate() {
        showLoadingDialog(this, getString(R.string.dataloading));
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("template_flag","recommend_app");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_PROPERTY_TEMPLATES), params, this);
        mNetworkManager.load(PROMOTIONSSHAREMODULE, path, this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_right) {
            Intent intent = new Intent(this, RecommendListActivity.class);
            startActivity(intent);
        } else {
            super.onClick(v);
        }
    }

    @OnClick(R.id.btn_recommend_friends)
    public void sharefriendAction(){
        ShareActionSheet actionSheet = ShareActionSheet.getInstance(this, new ShareActionSheet.IListener() {
            @Override
            public void msmAction() {
                type = 1;
                initDate();
            }

            @Override
            public void wetchAction() {
                type = 2;
                initDate();
            }

            @Override
            public void friendAction() {
                type = 3;
                initDate();
            }

            @Override
            public void sinaAction() {
                type = 4;
                initDate();
            }
        });
        actionSheet.show();
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId){
            case PROMOTIONSSHAREMODULE:
                    try {
                        JSONObject jsonObject = rootData.getJson();
                        final String content = jsonObject.getString("data");
                        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                        if (!TextUtils.isEmpty(content)){
                            switch (type){
                                case 1:
                                    Uri smsToUri = Uri.parse("smsto:");
                                    Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
                                    intent.putExtra("sms_body", content);
                                    startActivity(intent);
                                    /**Intent intent = new Intent(PlansMainActivity.this, SmsInvitationStaffActivity.class);
                                    intent.putExtra("type","1");
                                    startActivity(intent);*/
                                    break;
                                case 2:
                                    new ShareUtils().shareWebPage(PlansMainActivity.this, SendMessageToWX.Req.WXSceneSession, bitmap, URLAddr.URL_APPLIST, getString(R.string.app_name),content);
                                    break;
                                case 3:
                                    new ShareUtils().shareWebPage(PlansMainActivity.this, SendMessageToWX.Req.WXSceneTimeline, bitmap, URLAddr.URL_APPLIST, getString(R.string.app_name),content);
                                    break;
                                case 4:
                                    new ShareUtils().shareWeibo(PlansMainActivity.this, content, bitmap);
                                    break;
                            }
                        }
                    } catch (Exception e){
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
