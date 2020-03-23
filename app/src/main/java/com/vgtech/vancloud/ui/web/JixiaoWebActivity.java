package com.vgtech.vancloud.ui.web;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
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

import com.vgtech.common.PrfUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vantop.utils.VanTopUtils;

/**
 * Created by code on 2016/10/18.
 * 绩效
 */
public class JixiaoWebActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    protected WebView mWebView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View mBtnClose;
    private TextView mTitleTv;
    private TextView mUrlTv;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.iv_back).setOnClickListener(this);
        mBtnClose = findViewById(R.id.iv_close);
        mBtnClose.setVisibility(View.GONE);
        mBtnClose.setOnClickListener(this);
        mTitleTv = (TextView) findViewById(R.id.tv_title);
        mTitleTv.setText(getString(R.string.lable_jixiao));
        mUrlTv = (TextView) findViewById(R.id.tv_url);
//        if (Constants.DEBUG)
//        {
//            mUrlTv.setVisibility(View.VISIBLE);
//        }
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
//        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        SharedPreferences sharedPreferences = PrfUtils.getSharePreferences(this);
        String url = Uri.parse(VanTopUtils.generatorUrl(this, "performance/performance_list.html")).buildUpon()
                .appendQueryParameter("staff_no", sharedPreferences.getString("user_no", null))
                .appendQueryParameter("language", PrfUtils.getAppLanguage(this))
                .appendQueryParameter("token", sharedPreferences.getString("token", null))
                .appendQueryParameter("tenant_id", sharedPreferences.getString("tenantId", null)).build().toString();
        Log.i("chen_zhanyang", "onCreate: url = "+url);
        mWebView.addJavascriptInterface(new DownloadJSInterface(this),"download");
        mWebView.loadUrl(url, getAppliction().getApiUtils().getSignParams());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            String url = mWebView.getUrl();
            if (!TextUtil.isEmpty(url) && url.contains("performance_list.html")) {
                clearWebViewCache();
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
                String url = mWebView.getUrl();
                if (!TextUtil.isEmpty(url) && url.contains("performance_list.html")) {
                    clearWebViewCache();
                } else if (mWebView.canGoBack()) {
                    mBtnClose.setVisibility(View.VISIBLE);
                    mWebView.goBack();
                } else {
                    clearWebViewCache();
                }
                break;
            case R.id.iv_close:
                clearWebViewCache();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void clearWebViewCache() {
        this.deleteDatabase("WebView.db");
        this.deleteDatabase("WebViewCache.db");
        mWebView.clearCache(true);
        finish();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_process_web;
    }

    @Override
    public void onRefresh() {
        mWebView.reload();
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            mWebView.loadUrl(url);
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
            mUrlTv.setText(view.getUrl());
            if (!TextUtils.isEmpty(title)) {
                mTitleTv.setText(title);
            } else {
                mTitleTv.setText(getString(R.string.flow));
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
            Toast.makeText(JixiaoWebActivity.this, getString(R.string.plans_web_toast), Toast.LENGTH_SHORT).show();
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

            mUrlTv.setText(view.getUrl());
            if (!TextUtils.isEmpty(title)) {
                mTitleTv.setText(title);
            } else {
                mTitleTv.setText(getString(R.string.flow));
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
