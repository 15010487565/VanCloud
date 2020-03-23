package com.vgtech.vancloud.ui.web;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;
import android.widget.TextView;

import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.vgtech.common.Constants;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;


public class WebX5Activity  extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{

    protected WebView mWebView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvTitle;
    private VancloudLoadingLayout mLoadingLayout;
    @Override
    protected int getContentView() {
        return R.layout.activity_web_x5;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.iv_back).setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tv_title);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.my_swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mWebView = (WebView) findViewById(R.id.wv_task);
        WebSettings settings = mWebView.getSettings();
        mWebView.clearCache(true);
        settings.setCacheMode(android.webkit.WebSettings.LOAD_NO_CACHE);
        settings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        settings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        settings.setDisplayZoomControls(true); //隐藏原生的缩放控件
        settings.setBlockNetworkImage(false);//解决图片不显示
        settings.setLoadsImagesAutomatically(true); //支持自动加载图片
        settings.setDefaultTextEncodingName("gb2312");//设置编码格式

        settings.setTextZoom( 100);// textZoom:100表示正常，120表示文字放大1.2倍

        mLoadingLayout = (VancloudLoadingLayout) findViewById(R.id.ll_loadingview);

        mLoadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initLoadUrl();
            }
        });
        mLoadingLayout.setVisibility(View.GONE);
        initLoadUrl();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Constants.DEBUG) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }
        //该界面打开更多链接
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String s) {
                webView.loadUrl(s);
                return true;
            }
        });

    }
    /**
     * 请求加载的url
     */
    private void initLoadUrl() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        tvTitle.setText(TextUtils.isEmpty(title)?"":title);
        String url = intent.getStringExtra("url");
        if (Constants.DEBUG) {
            Log.e("TAG_weiview", "hostUrl=" + url);
        }
        mWebView.loadUrl(url);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;

            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    public void onRefresh() {

            mWebView.reload();
    }
    //销毁 放置内存泄漏
    @Override
    public void onDestroy() {
        if (this.mWebView != null) {
            mWebView.destroy();
        }
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
    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
        mWebView.getSettings().setLightTouchEnabled(false);
    }

}
