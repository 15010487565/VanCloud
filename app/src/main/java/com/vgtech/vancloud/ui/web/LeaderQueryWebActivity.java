package com.vgtech.vancloud.ui.web;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
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

import com.google.inject.Inject;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.controllers.Controller;

/**
 * 领导查询
 */
public class LeaderQueryWebActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    protected WebView mWebView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View mBtnClose;
    private TextView mTitleTv;
    private VancloudLoadingLayout mLoadingLayout;
    @Inject
    Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.iv_back).setOnClickListener(this);
        mBtnClose = findViewById(R.id.iv_close);
        mBtnClose.setVisibility(View.GONE);
        mBtnClose.setOnClickListener(this);
        mTitleTv = (TextView) findViewById(R.id.tv_title);
        mTitleTv.setText(getString(R.string.lable_leaderquery));
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
        settings.setDomStorageEnabled(true);
        settings.setBlockNetworkImage(false); // 解决图片不显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
//        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mLoadingLayout = (VancloudLoadingLayout) findViewById(R.id.ll_loadingview);
        mLoadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                mWebView.loadUrl(ApiUtils.getHost(LeaderQueryWebActivity.this) + "appstatic/vantop/leadersearch/index.html");
            }
        });
        mLoadingLayout.setVisibility(View.GONE);
        mWebView.loadUrl(ApiUtils.getHost(LeaderQueryWebActivity.this) + "appstatic/vantop/leadersearch/index.html");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Constants.DEBUG) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            String url = mWebView.getUrl();
            if (!TextUtils.isEmpty(url) && url.endsWith("leadersearch/index.html")) {
                mWebView.clearCache(true);
                finish();
            } else {
                if (mWebView.canGoBack()) {
                    mBtnClose.setVisibility(View.VISIBLE);
                    mWebView.goBack();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                String url = mWebView.getUrl();
                if (!TextUtils.isEmpty(url) && url.endsWith("leadersearch/index.html")) {
                    mWebView.clearCache(true);
                    finish();
                } else {
                    if (mWebView.canGoBack()) {
                        mBtnClose.setVisibility(View.VISIBLE);
                        mWebView.goBack();
                    } else {
                        mWebView.clearCache(true);
                        finish();
                    }
                }
                break;
            case R.id.iv_close:
                mWebView.clearCache(true);
                finish();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_neigou_web;
    }

    @Override
    public void onRefresh() {
        mWebView.reload();
    }


    private class MyWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            if (!(url.startsWith("http") || url.startsWith("https"))) {
                return true;
            }

            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
//            view.getSettings().setJavaScriptEnabled(true);
//            mWebView.getSettings().setDomStorageEnabled(true);
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
                mTitleTv.setText(getString(R.string.lable_leaderquery));
            }
            //-------------------
            if (TextUtils.isEmpty(url) || !url.endsWith("leadersearch/index.html")) {
                return;
            }
            SharedPreferences preferences = PrfUtils.getSharePreferences(LeaderQueryWebActivity.this);
            String userId = preferences.getString("uid", "");
            String employee_no = preferences.getString("user_no", "");
            String tenantId = preferences.getString("tenantId", "");
            String token = PrfUtils.getToken(LeaderQueryWebActivity.this);
            String host = ApiUtils.getHost(LeaderQueryWebActivity.this) + "v" + ApiUtils.getAppVersion(LeaderQueryWebActivity.this) + "/";


            String jsCode = "javascript:init(\"" + userId + "\",\""
                    + tenantId + "\",\""
                    + token + "\",\""
                    + host + "\",\""
                    + employee_no + "\",\""
                    + "\")";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.evaluateJavascript(jsCode, null);
            } else {
                mWebView.loadUrl(jsCode);
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
            Toast.makeText(LeaderQueryWebActivity.this, getString(R.string.plans_web_toast), Toast.LENGTH_SHORT).show();
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
                mTitleTv.setText(getString(R.string.lable_leaderquery));
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
