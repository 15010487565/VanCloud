package com.vgtech.vancloud.ui.web;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

/**
 * 内购
 */
public class NeigouWebActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, HttpListener<String> {
    protected WebView mWebView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View mBtnClose;
    private TextView mTitleTv;
    private VancloudLoadingLayout mLoadingLayout;
    private View mBtnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBtnBack = findViewById(R.id.iv_back);
        mBtnBack.setVisibility(View.GONE);
        mBtnBack.setOnClickListener(this);

        mBtnClose = findViewById(R.id.iv_close);
        mBtnClose.setOnClickListener(this);
        mTitleTv = (TextView) findViewById(R.id.tv_title);
        mTitleTv.setText(getString(R.string.lable_neigou));
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.my_swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings settings = mWebView.getSettings();
        mWebView.clearCache(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setBlockNetworkImage(false); // 解决图片不显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mLoadingLayout = (VancloudLoadingLayout) findViewById(R.id.ll_loadingview);
        mLoadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                loadNeiGouUrl();
            }
        });
        loadNeiGouUrl();
    }

    private void loadNeiGouUrl() {
        mLoadingLayout.showLoadingView(mWebView, "", true);
        Map<String, String> params = new HashMap<>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_NEIGOU_NEIGOU_LOGIN), params, this);
        getAppliction().getNetworkManager().load(1, path, this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            String url = mWebView.getUrl();
            if (!TextUtil.isEmpty(url) && url.contains("apply_list.html")) {
                finish();
            } else if (mWebView.canGoBack()) {
                mBtnClose.setVisibility(View.VISIBLE);
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                }
//                String url = mWebView.getUrl();
//                if (!TextUtil.isEmpty(url) && url.contains("apply_list.html")) {
//                    finish();
//                } else if (mWebView.canGoBack()) {
//                    mBtnClose.setVisibility(View.VISIBLE);
//                    mWebView.goBack();
//                } else {
//                    finish();
//                }
                break;
            case R.id.iv_close:
                finish();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_neigou_web_new;
    }

    @Override
    public void onRefresh() {
        mWebView.reload();
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mLoadingLayout.dismiss(mWebView);
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            if (callbackId == 1)
                mLoadingLayout.showErrorView(mWebView);
            return;
        }
        try {
            String login_url = rootData.getJson().getJSONObject("data").getString("login_url");
            mWebView.loadUrl(login_url);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    private class MyWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            // 获取上下文, H5PayDemoActivity为当前页面
            final Activity context = NeigouWebActivity.this;

            // ------  对alipays:相关的scheme处理 -------
            if (url.startsWith("alipays:") || url.startsWith("alipay")) {
                try {
                    context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
                } catch (Exception e) {
                    new AlertDialog.Builder(context)
                            .setMessage("未检测到支付宝客户端，请安装后重试。")
                            .setPositiveButton("立即安装", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Uri alipayUrl = Uri.parse("https://d.alipay.com");
                                    context.startActivity(new Intent("android.intent.action.VIEW", alipayUrl));
                                }
                            }).setNegativeButton("取消", null).show();
                }
                return true;
            }
            // ------- 处理结束 -------

            if (!(url.startsWith("http") || url.startsWith("https"))) {
                return true;
            }

            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
//            view.getSettings().setJavaScriptEnabled(true);
            swipeRefreshLayout.setRefreshing(false);
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 1000);
            String title = view.getTitle();
            if (!TextUtils.isEmpty(title)) {
                mTitleTv.setText(title);
            } else {
                mTitleTv.setText(getString(R.string.lable_neigou));
            }
            //-----------------------------------------
            if (mWebView.canGoBack()) {
                if (mBtnBack.getVisibility() != View.VISIBLE) {
                    mBtnBack.setVisibility(View.VISIBLE);
                }
            } else {
                if (mBtnBack.getVisibility() != View.GONE) {
                    mBtnBack.setVisibility(View.GONE);
                }
            }
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(NeigouWebActivity.this, getString(R.string.plans_web_toast), Toast.LENGTH_SHORT).show();
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    public class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (!TextUtils.isEmpty(title)) {
                mTitleTv.setText(title);
            } else {
                mTitleTv.setText(getString(R.string.lable_neigou));
            }
            super.onReceivedTitle(view, title);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CookieSyncManager.createInstance(this.getApplicationContext());
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeSessionCookies(null);
            cookieManager.removeAllCookie();
            cookieManager.flush();
        } else {
            cookieManager.removeAllCookie();
            CookieSyncManager.getInstance().sync();
        }
        WebStorage.getInstance().deleteAllData(); //清空WebView的localStorage
    }
}
