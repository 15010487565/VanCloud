package com.vgtech.vancloud.ui.module.resume;

/**
 * Created by swj on 16/5/31.
 */

import android.content.Intent;
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
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.ui.PasswordFragment;
import com.vgtech.common.utils.ActivityUtils;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.vancloud.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 简历详情
 * Created by Nick on 2016/5/19.
 */
public class ResumeDetail extends BaseActivity implements HttpListener<String>, SwipeRefreshLayout.OnRefreshListener {

    private String resume_id; //简历id
    private String resourseID;
    private String infoID;

    private String is_from;
    private String resource_ids;
    private String is_free;
    private String resume_format;//简历详情第三方-html
    private final int CALL_BACK_PAYINFO = 11;
    private final int CALL_BACK_DEL = 12;

    protected WebView mWebView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.personal_resume_prive));
        setListener();
        Intent intent = getIntent();
        is_from = intent.getStringExtra("is_from");
        resource_ids = intent.getStringExtra("resource_ids");
        is_free = intent.getStringExtra("is_free");
        resume_format = intent.getStringExtra("resume_format");

        resume_id = intent.getExtras().getString("resume_id");

        infoID = intent.getStringExtra("order_info_id");
        resourseID = intent.getStringExtra("resource_id");

        if (!TextUtils.isEmpty(resume_id)) {
            if (!TextUtils.isEmpty(resume_format) && "html".equals(resume_format)) {//第三方简历详情
                String url = ApiUtils.generatorUrl(this, URLAddr.URL_RESUME_DETAIL_HTML) + "?"
                        + "resume_id=" + resume_id;
                initWebviewAction(url);
            } else {//万客简历详情
                if ("resume_list".equals(is_from)) {
                    String url = ApiUtils.generatorUrl(this, URLAddr.URL_ENTERPRISE_RESUME_DETAIL_V2) + "?"
                            + "resume_id=" + resume_id;
                    initWebviewAction(url);
                } else { //万客搜索简历详情
                    String url = ApiUtils.generatorUrl(this, URLAddr.URL_PERSONAL_RESUME_DETAIL_V2) + "?"
                            + "user_id=" + PrfUtils.getUserId(this) + "&" + "resume_id=" + resume_id
                            + "&" + "show_type=true";
                    initWebviewAction(url);
                }
            }
        } else { //购买的简历详情
            String url = ApiUtils.generatorUrl(this, URLAddr.URL_ENTERPRISE_RESUME_PAID_RESUME_DETAIL_V2) + "?"
                    + "order_info_id=" + infoID + "&" + "resource_id=" + resourseID;
            initWebviewAction(url);
        }
    }

    private void changeEnterpriseBottom() {
        //企业版简历预览，删除和购买
        if (!TextUtils.isEmpty(is_from)) {
            findViewById(R.id.enterprise_bottom_layout).setVisibility(View.VISIBLE);
            if ("resume_list".equals(is_from)) {
                findViewById(R.id.enterprise_del).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.enterprise_del).setVisibility(View.GONE);
            }
            if ("N".equals(is_free)) {
                findViewById(R.id.enterprise_buy).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.enterprise_buy).setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.enterprise_bottom_layout).setVisibility(View.GONE);
        }
    }

    private void setListener() {

        findViewById(R.id.enterprise_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //简历删除
                new AlertDialog(ResumeDetail.this).builder().setTitle(getString(R.string.personal_resume_del))
                        .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        delAction(resume_id);
                                    }
                                }
                        ).setNegativeButton(getString(R.string.personal_cancle), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        });
        findViewById(R.id.enterprise_buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //简历购买
                if ("resume_list".equals(is_from)) {
                    getPayinfoList(resume_id);
                } else {
                    getPayinfo(resource_ids);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            if (mWebView != null) {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    finish();
                }
            } else {
                finish();
            }
        } else {
            super.onClick(v);
        }
    }

    //搜索列表获取支付信息
    private void getPayinfo(String ids) {
        showLoadingDialog(this, "");
        ApplicationProxy proxy = (ApplicationProxy) getApplication();
        NetworkManager networkManager = proxy.getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_type", "tenant");
        params.put("resource_ids", ids);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ENTERPRISE_RESUME_RESUME_PAYINFO), params, this);
        networkManager.load(CALL_BACK_PAYINFO, path, this);
    }

    //简历列表获取支付信息
    private void getPayinfoList(String ids) {
        showLoadingDialog(this, "");
        ApplicationProxy proxy = (ApplicationProxy) getApplication();
        NetworkManager networkManager = proxy.getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_type", "tenant");
        params.put("resource_ids", ids);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ENTERPRISE_RESUME_BUY), params, this);
        networkManager.load(CALL_BACK_PAYINFO, path, this);
    }

    //简历删除
    private void delAction(String resume_id) {
        showLoadingDialog(this, "");
        ApplicationProxy proxy = (ApplicationProxy) getApplication();
        NetworkManager networkManager = proxy.getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("resume_ids", resume_id);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ENTERPRISE_RESUME_DELETE), params, this);
        networkManager.load(CALL_BACK_DEL, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = rootData.getJson() != null;
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALL_BACK_DEL:
                setResult(RESULT_OK);
                finish();
                break;
            case CALL_BACK_PAYINFO:
                JSONObject jsonObject = rootData.getJson();
                try {
                    JSONObject resultObject = jsonObject.getJSONObject("data");
                    ActivityUtils.toPay(this, resultObject.getString("order_id"), resultObject.getString("order_name"),
                            resultObject.getString("amount"), resultObject.getString("order_type"), -1, PasswordFragment.COMPANYUSER);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setResult(RESULT_OK);
                finish();
                break;
            default:
                break;

        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    protected int getContentView() {
        return R.layout.resume_detail_layout;
    }


    private void initWebviewAction(String webUrl) {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.my_swiperefreshlayout);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
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
        mWebView.setVisibility(View.GONE);
        mWebView.loadUrl(webUrl);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView != null) {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        mWebView.reload();
        super.onPause();
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
            if (!ResumeDetail.this.isFinishing()) {
                changeEnterpriseBottom();
            }
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mWebView.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 1000);
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
            Toast.makeText(ResumeDetail.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
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

