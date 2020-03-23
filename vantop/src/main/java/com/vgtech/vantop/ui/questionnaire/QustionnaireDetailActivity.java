package com.vgtech.vantop.ui.questionnaire;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.EventBusMsg;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.QuestionnaireListData;
import com.vgtech.vantop.utils.VanTopUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * 调查问卷详情
 * create by scott
 */
public class QustionnaireDetailActivity extends BaseActivity {

    private QuestionnaireListData mData;
    private WebView mWebView;
    public static final String BUNDLE_KEY = "key";
    private final String TAG = "QustionDetail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
        if (mWebView != null && mData != null) {
            showLoadingDialog(this, "");
            String url = mData.url311;
            if (TextUtils.isEmpty(url))
                url = mData.url;
            mWebView.clearCache(true);
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            SharedPreferences sharedPreferences = PrfUtils.getSharePreferences(this);
            Uri uri = Uri.parse(VanTopUtils.generatorUrl(this, url)).buildUpon()
                    .appendQueryParameter("staff_no", sharedPreferences.getString("user_no", null))
                    .appendQueryParameter("language", PrfUtils.getAppLanguage(this))
                    .appendQueryParameter("token", sharedPreferences.getString("token", null))
                    .appendQueryParameter("tenant_id", sharedPreferences.getString("tenantId", null)).build();
            mWebView.loadUrl(uri.toString(), getApplicationProxy().getNetworkManager().getApiUtils().getSignParams());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_qustionnaire_detail;
    }

    private void initView() {

        mWebView = (WebView) findViewById(R.id.wv_content);
       /* mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mWebView.loadUrl(url);
                return false;
            }
        });*/
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptCanOpenWindowsAutomatically(true);//设置js可以直接打开窗口，如window.open()，默认为false
        settings.setJavaScriptEnabled(true);//是否允许执行js，默认为false。设置true时，会提醒可能造成XSS漏洞
        settings.setSupportZoom(true);//是否可以缩放，默认true
        settings.setBuiltInZoomControls(true);//是否显示缩放按钮，默认false
        settings.setUseWideViewPort(true);//设置此属性，可任意比例缩放。大视图模式
        settings.setLoadWithOverviewMode(true);//和setUseWideViewPort(true)一起解决网页自适应问题
        settings.setAppCacheEnabled(false);//是否使用缓存
        settings.setDomStorageEnabled(true);//DOM Storage
        settings.setBlockNetworkImage(false); // 解决图片不显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100)
                    dismisLoadingDialog();
                Log.i(TAG, "progress:" + newProgress);
            }

        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Toast.makeText(QustionnaireDetailActivity.this, getString(R.string.loading_error), Toast.LENGTH_SHORT).show();
            }
        });
        mWebView.addJavascriptInterface(this,"download");
        //mWebView.getSettings().setUserAgentString("User-Agent:Android");//设置用户代理，一般不用
        //mWebView.loadUrl(mData.url);
        //showLoadingDialog(this, "");
        //Log.i(TAG, "url == " + mData.url);
    }

    private void initData() {
        mData = (QuestionnaireListData) getIntent().getSerializableExtra(BUNDLE_KEY);
        setTitle(mData.title);
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
    //删除问卷
    @JavascriptInterface
    public void appQuesSurvey(String taskId) {
        Log.e("TAG_删除问卷","taskId="+taskId);
        EventBusMsg messageEvent = new EventBusMsg();
        messageEvent.setActoin("appQuesSurvey");
        messageEvent.setMessage(taskId);
        EventBus.getDefault().post(messageEvent);

    }
}
