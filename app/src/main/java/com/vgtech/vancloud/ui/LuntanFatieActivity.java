package com.vgtech.vancloud.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.module.luntan.EventMsg;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * Data:  2017/8/2
 * Auther: 陈占洋
 * Description: 发帖界面
 */

public class LuntanFatieActivity extends BaseActivity implements HttpListener<String> {

    private static final int POST_CALLBACK_ID = 10001;
    private NetworkManager mNetworkManager;
    private EditText mEtPostTitle;
    private EditText mEtPostContent;
    private Button mBtnPostSubmit;
    private boolean mEtPostTitleIsEmpty = true;
    private boolean mEtPostContentIsEmpty = true;

    @Override
    protected int getContentView() {
        return R.layout.activity_luntan_fatie;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.fatie));

        mNetworkManager = getAppliction().getNetworkManager();
        initView();
        initListener();
    }

    private void initView() {
        mEtPostTitle = (EditText) findViewById(R.id.luntan_fatie_title);
        mEtPostContent = (EditText) findViewById(R.id.luntan_fatie_content);
        mBtnPostSubmit = (Button) findViewById(R.id.luntan_fatie_submit);
    }

    private void initListener() {
        mEtPostTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (TextUtils.isEmpty(text)){
                    mEtPostTitleIsEmpty = true;
                }else {
                    mEtPostTitleIsEmpty = false;
                }
                if (!mEtPostTitleIsEmpty && !mEtPostContentIsEmpty){
                    mBtnPostSubmit.setBackgroundResource(R.drawable.fatie_btn_bg_blue);
                    mBtnPostSubmit.setClickable(true);
                }else {
                    mBtnPostSubmit.setBackgroundResource(R.drawable.fatie_btn_bg_gray);
                    mBtnPostSubmit.setClickable(false);
                }
            }
        });

        mEtPostContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (TextUtils.isEmpty(text)){
                    mEtPostContentIsEmpty = true;
                }else {
                    mEtPostContentIsEmpty = false;
                }
                if (!mEtPostTitleIsEmpty && !mEtPostContentIsEmpty){
                    mBtnPostSubmit.setBackgroundResource(R.drawable.fatie_btn_bg_blue);
                    mBtnPostSubmit.setClickable(true);
                }else {
                    mBtnPostSubmit.setBackgroundResource(R.drawable.fatie_btn_bg_gray);
                    mBtnPostSubmit.setClickable(false);
                }
            }
        });
        mBtnPostSubmit.setOnClickListener(this);
        mBtnPostSubmit.setClickable(false);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.luntan_fatie_submit:
                submitPost();
                break;
        }
    }

    private void submitPost() {
        String title = mEtPostTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, getString(R.string.toast_title_not_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        String content = mEtPostContent.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, getString(R.string.toast_content_not_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("title", title);
        params.put("content", content);

        String url = ApiUtils.generatorUrl(this, URLAddr.POST);
        NetworkPath path = new NetworkPath(url, params, this);
        mNetworkManager.load(POST_CALLBACK_ID, path, this);

    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        switch (callbackId) {
            case POST_CALLBACK_ID:
                boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
                if (safe) {
                    Toast.makeText(this, getString(R.string.toast_post_success), Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(new EventMsg(EventMsg.LUNTAN_REFRESH_LIST));
                    this.finish();
                }else {
                    Toast.makeText(this, getString(R.string.toast_post_failed), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
