package com.vgtech.vancloud.ui.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.utils.ActivityUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhangshaofang on 2016/2/26.
 */
public class BjdcWebActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    protected WebView mWebView;
    public SwipeRefreshLayout swipeRefreshLayout;
    private ApiUtils mApiUtils;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String title = getIntent().getStringExtra("title");
        setTitle(title);
        mWebView = (WebView) findViewById(R.id.webview);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.my_swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setEnabled(false);
        mTvSearch = (TextView) findViewById(R.id.tv_search);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        WebSettings webSettings = mWebView.getSettings();
        mWebView.clearCache(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        mWebView.setWebViewClient(new MyWebViewClient());
        webSettings.setAppCacheEnabled(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.addJavascriptInterface(new JavaScriptObject(this), "jsObj");
        mSearchBtn = (ImageButton) findViewById(R.id.btn_right);
        mSearchBtn.setImageResource(R.mipmap.top_search);
        mSearchBtn.setOnClickListener(this);
        int flag = getIntent().getIntExtra("flag", 0);
        String url = getIntent().getStringExtra("url");
        mApiUtils = new ApiUtils(this);
        if (flag == 1) {//调查结果页
            mCanGoBack = false;
            url = ApiUtils.generatorUrl(this, URLAddr.URL_BG_INVEST_RESULT);
            mWebView.loadUrl(mApiUtils.appendSignInfo(url));
        } else {
            if (TextUtils.isEmpty(url))
                url = ApiUtils.generatorUrl(this, URLAddr.URL_BG_INVEST_INDEX);
            mWebView.loadUrl(mApiUtils.appendSignInfo(url));
        }

    }

    private boolean mCanGoBack = true;

    private void hideInputMethod() {
        if (mSearchTv != null) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(
                    mSearchTv.getWindowToken(), 0);
        }
    }

    private static final int REQUEST_CODE_SCAN = 0x0000;
    private static final String DECODED_CONTENT_KEY = "codedContent";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(DECODED_CONTENT_KEY);
                Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
                mWebView.loadUrl("javascript:setIdcard('"
                        + content + "'"
                        + ")");
            }
        } else if (requestCode == 200) {
            //resultCode -1支付成功，-2支付失败。
            if (resultCode == RESULT_OK) {
                mWebView.loadUrl("javascript:onPayResult('"
                        + resultCode + "'"
                        + ")");
            }
        }
    }

    public class JavaScriptObject {
        Context mContxt;

        public JavaScriptObject(Context mContxt) {
            this.mContxt = mContxt;
        }

        @JavascriptInterface //sdk17版本以上加上注解
        public void scanningIdCard() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Intent intent = new Intent(BjdcWebActivity.this,
//                            CaptureActivity.class);
//                    startActivityForResult(intent, REQUEST_CODE_SCAN);
                }
            });
        }

        @JavascriptInterface //sdk17版本以上加上注解
        public void onClickInfo(String url) {
            Intent intent = new Intent(BjdcWebActivity.this, BjdcWebActivity.class);
            intent.putExtra("title", getString(R.string.lable_jcxq));
            intent.putExtra("info", true);
            intent.putExtra("url", url);
            startActivity(intent);
        }

        @JavascriptInterface //sdk17版本以上加上注解
        public void doPay(String data) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                JSONObject resultObject = jsonObject.getJSONObject("data");
                String orderId = resultObject.getString("order_id");
                String description = resultObject.getString("order_name");
                String amount = resultObject.getString("amount");
                String order_type = resultObject.getString("order_type");
                ActivityUtils.toPay(BjdcWebActivity.this, orderId, description, amount, order_type, -1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface //sdk17版本以上加上注解
        public void showTip(String tip) {
            Toast.makeText(BjdcWebActivity.this, tip, Toast.LENGTH_SHORT).show();
        }
//        @JavascriptInterface //sdk17版本以上加上注解
//        public void overInvert() {
//            new Handler().post(new Runnable() {
//                @Override
//                public void run() {
//                    mWebView.loadUrl(PrfUtils.getServiceHost(BjdcWebActivity.this) + URLAddr.URL_BG_INVEST_RECORD_LIST);
//                }
//            });
//        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mNoNetwork)
                finish();
            String url = mWebView.getUrl();
            if (!TextUtils.isEmpty(url) && url.contains("result.html")) {
                if (mCanGoBack) {
                    Intent intent = new Intent(this, BjdcWebActivity.class);
                    intent.putExtra("title", getString(R.string.lable_investigate));
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else {
                    return super.onKeyDown(keyCode, event);
                }

            } else if (url.contains(URLAddr.URL_BG_INVEST_RECORD_LIST)) {
                if (mCanGoBack) {
                    Intent intent = new Intent(this, BjdcWebActivity.class);
                    intent.putExtra("title", getString(R.string.lable_investigate));
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else {
                    return super.onKeyDown(keyCode, event);
                }

            } else if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private EditText mSearchTv;
    private TextView mTvSearch;
    private ImageButton mSearchBtn;
    private View mSearchCancleIcon;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                if (mNoNetwork)
                    finish();
                String url = mWebView.getUrl();
                if (!TextUtils.isEmpty(url) && url.contains("result.html")) {
                    if (mCanGoBack) {
                        Intent intent = new Intent(this, BjdcWebActivity.class);
                        intent.putExtra("title","背景调查");
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                    finish();
                } else if (url.contains(URLAddr.URL_BG_INVEST_RECORD_LIST)) {
                    if (mCanGoBack) {
                        Intent intent = new Intent(this, BjdcWebActivity.class);
                        intent.putExtra("title", "背景调查");
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                    finish();
                } else if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    finish();
                }
                break;
            case R.id.tv_search:
                int flag = (int) v.getTag();
                if (flag == 1) {
                    String keyword = mSearchTv.getText().toString();
                    mWebView.loadUrl("javascript:search('"
                            + keyword + "'"
                            + ")");
                } else {
                    mWebView.goBack();
                }
                break;
            case R.id.search_cancel:
                mSearchTv.setText("");
                break;
            case R.id.tv_right://调查记录
//                startActivity(new Intent(this, InvestigationRecordsActivity.class));
                if (getIntent().getBooleanExtra("info", false)) {
                    mWebView.loadUrl("javascript:list()");
                } else {
                    mWebView.loadUrl(mApiUtils.appendSignInfo(ApiUtils.generatorUrl(this, URLAddr.URL_BG_INVEST_RECORD_LIST)));
                }
                TextView textView = (TextView) v;
                if ("调查记录".equals(textView.getText()) && textView.getVisibility() == View.VISIBLE) {
                    PrfUtils.saveInvestigationCount(this, 0);
                    numView.setVisibility(View.GONE);

                }
                break;
            case R.id.btn_right:
                mSearchBtn.setVisibility(View.GONE);
                findViewById(R.id.search_layout).setVisibility(View.VISIBLE);
                mSearchTv = (EditText) findViewById(R.id.serch_context);
                mSearchCancleIcon = findViewById(R.id.search_cancel);
                mSearchCancleIcon.setOnClickListener(this);
                mTvSearch.setOnClickListener(this);
                mTvSearch.setVisibility(View.VISIBLE);
                mTvSearch.setText(R.string.cancel);
                mTvSearch.setOnClickListener(this);
                mSearchTv.setText("");
                mTvSearch.setTag(0);
                mSearchTv.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            String keyword = mSearchTv.getText().toString();
                            mWebView.loadUrl("javascript:search('"
                                    + keyword + "'"
                                    + ")");
                            hideInputMethod();
                        }
                        return false;
                    }
                });
                mSearchTv.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (TextUtils.isEmpty(s)) {
                            mSearchCancleIcon.setVisibility(View.GONE);
                            mTvSearch.setText(R.string.cancel);
                            mTvSearch.setTag(0);
                        } else {
                            mSearchCancleIcon.setVisibility(View.VISIBLE);
                            mTvSearch.setText(R.string.search);
                            mTvSearch.setTag(1);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                String searchUrl = mApiUtils.appendSignInfo(ApiUtils.generatorUrl(this, URLAddr.URL_BG_SEARCH));
                mWebView.loadUrl(searchUrl);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void revertSearch() {
        hideInputMethod();
        findViewById(R.id.search_layout).setVisibility(View.GONE);
        mTvSearch.setVisibility(View.GONE);
    }

    private static final String ERROR_URL = "file:///android_asset/404.htm";

    @Override
    protected int getContentView() {
        return R.layout.web_bjdc;
    }

    @Override
    public void onRefresh() {
        mWebView.reload();
    }

    private boolean mNoNetwork;

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            mWebView.loadUrl(mApiUtils.appendSignInfo(url));
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            mNoNetwork = false;
            if (!url.contains(view.getTitle()))
                setTitle(view.getTitle());
            if (!BjdcWebActivity.this.isFinishing()) {
                checkUrl(url);
            }
            swipeRefreshLayout.setRefreshing(false);
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
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
            mNoNetwork = true;
            Toast.makeText(BjdcWebActivity.this, getString(R.string.plans_web_toast), Toast.LENGTH_SHORT).show();
            mWebView.loadUrl(ERROR_URL);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    @SuppressLint("JavascriptInterface")
    private void checkUrl(String url) {
        String path = Uri.parse(url).getLastPathSegment();

        if (mCanGoBack) {
            if (!TextUtils.isEmpty(url) && url.contains("result.html")) {
                String uid = PrfUtils.getUserId(BjdcWebActivity.this);
                String tenantId = PrfUtils.getTenantId(BjdcWebActivity.this);
                String host = ApiUtils.addAppVersion(BjdcWebActivity.this, "");
                mWebView.loadUrl("javascript:init('"
                        + uid + "','"
                        + tenantId + "','"
                        + PrfUtils.getToken(BjdcWebActivity.this) + "','"
                        +  host+ "'"
                        + ")");
            }
        }
        if (getIntent().getBooleanExtra("info", false)) {
            initRightTv("调查清单");
        } else if (path.contains("index.html") && !mWebView.canGoBack()) {
            initRightTv(getString(R.string.investigation_record));
            String uid = PrfUtils.getUserId(BjdcWebActivity.this);
            String tenantId = PrfUtils.getTenantId(BjdcWebActivity.this);
            String host = ApiUtils.addAppVersion(BjdcWebActivity.this, "");
            mWebView.loadUrl("javascript:init('"
                    + uid + "','"
                    + tenantId + "','"
                    + PrfUtils.getToken(BjdcWebActivity.this) + "','"
                    + host + "'"
                    + ")");
//            mWebView.loadUrl("javascript:setIdCardScanning('"
//                    + true + "'"
//                    + ")");

        } else {
            initRightTv("").setVisibility(View.GONE);
        }
        if (!url.contains(URLAddr.URL_BG_SEARCH)) {
            revertSearch();
            mSearchBtn.setVisibility(View.GONE);
        }
        if (url.contains(URLAddr.URL_BG_INVEST_RECORD_LIST)) {
            mSearchBtn.setVisibility(View.VISIBLE);
        }
    }

    private TextView numView;

    @Override
    public TextView initRightTv(String lable) {

        numView = (TextView) findViewById(R.id.num);
        if (getString(R.string.investigation_record).equals(lable)) {
            int num = PrfUtils.getInvestigationCount(this);
            if (num > 0) {
                numView.setText(num + "");
                numView.setVisibility(View.VISIBLE);
            }
        } else {
            numView.setVisibility(View.GONE);
        }
        return super.initRightTv(lable);
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
