package com.vgtech.vancloud.ui.web;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.EventBusMsg;
import com.android.volley.VolleyError;
import com.google.inject.Inject;
import com.huantansheng.easyphotos.EasyPhotos;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.module.pi.PiModule;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 报表、领导查询、积分、PI币、个税申报
 */
public class HttpWebActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, HttpListener<String> {
    protected WebView mWebView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View mBtnClose;
    private TextView mTitleTv;
    private VancloudLoadingLayout mLoadingLayout;
    private String hostUrl;
    private final int PINEIGOU = 0x000000;
    @Inject
    Controller controller;
    private int mCode;
    private ImageView ivPi;
    private boolean isPi = false;//true:pi币页面；false:pi币下级页面
    private boolean isStartPi = false;//true:pi币页面；false:pi币下级页面
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final int CAPTURE_TAG = 0x000001;
    private final int CHOOSE_TAG = 0x000003;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        findViewById(R.id.iv_back).setOnClickListener(this);

        //顶部切换按钮
        ivPi = (ImageView) findViewById(R.id.iv_Pi);
        //右侧π是否显示，默认不显示
        isStartPi = getIntent().getBooleanExtra("isSratrPi", false);
        if (isStartPi) {
            ivPi.setVisibility(View.VISIBLE);
        } else {
            ivPi.setVisibility(View.GONE);
        }
        ivPi.setOnClickListener(this);

        mBtnClose = findViewById(R.id.iv_close);
        mBtnClose.setVisibility(View.GONE);
        mBtnClose.setOnClickListener(this);
        mTitleTv = (TextView) findViewById(R.id.tv_title);
        mCode = getIntent().getIntExtra("code", -1);
        if (mCode == Constants.REPORT_FORM_CODE) {//报表
            mTitleTv.setText(getString(R.string.lable_report_form));
        } else if (mCode == Constants.LEADER_SEARCH_CODE) {//领导查询
            mTitleTv.setText(getString(R.string.lable_leaderquery));
        } else if (mCode == Constants.INTEGRAL_CODE) {//积分
            mTitleTv.setText(getString(R.string.lable_integral));
        } else if (mCode == Constants.PI_COIN_CODE) {//
            if (isPi) {
                mTitleTv.setText(getString(R.string.lable_pi_coin));
            } else {
                isPi = false;
                mTitleTv.setText("派商城");
//                mTitleTv.setTextSize(0,14);
                ivPi.setVisibility(View.VISIBLE);
            }
        } else if (mCode == Constants.TAX_CODE) {//附加扣除
            mTitleTv.setText(getString(R.string.lable_deduction));
        } else if (mCode == Constants.ENTRYAPPROVE_CODE) {//入职审批
            mTitleTv.setText(getString(R.string.lable_entryapprove));
        }else if (mCode == Constants.BGDIAOCHA_CODE) {//极速背调
            mTitleTv.setText(getString(R.string.lable_background_check));
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.my_swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mWebView = (WebView) findViewById(R.id.webview);
        MyWebChromeClient myWebChromeClient = new MyWebChromeClient();
        mWebView.addJavascriptInterface(new DownloadJSInterface(this), "appJs");
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setBlockNetworkImage(false); // 解决图片不显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mWebView.clearCache(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(myWebChromeClient);
        mLoadingLayout = (VancloudLoadingLayout) findViewById(R.id.ll_loadingview);
        //http://app.vancloud.com/appencash_weixin.htmlstatic/vantop/jump.html?from=app&type_code=41&mobile=13311210625&tid=735893790648176640
        hostUrl = ApiUtils.getHost(HttpWebActivity.this);

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
    }

    /**
     * 请求加载的url
     */
    private void initLoadUrl() {

        if (mCode == Constants.PI_COIN_CODE) {//π币
            if (isPi) {//跳转π币
                String url = hostUrl + "appstatic/paicoin/jump.html";
                mWebView.loadUrl(url);
                Log.e("TAG_loadUrl", url);
            } else {//跳转π币二级页面
                SharedPreferences preferences = PrfUtils.getSharePreferences(HttpWebActivity.this);
                String userId = preferences.getString("uid", "");
                String tenantId = preferences.getString("tenantId", "");
                String token = PrfUtils.getToken(HttpWebActivity.this);

                NetworkManager mNetworkManager = ((ApplicationProxy) getApplication()).getNetworkManager();
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", userId);
                params.put("tenant_id", tenantId);
                params.put("token_id", token);
                NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(HttpWebActivity.this, URLAddr.URL_PI), params, HttpWebActivity.this);
                mNetworkManager.load(PINEIGOU, path, this);
            }
        } else {
            String url = hostUrl + "appstatic/vantop/jump.html?from=app" +
                    "&type_code=" + mCode +
                    "&mobile=" + PrfUtils.getUserPhone(HttpWebActivity.this) +
                    "&tid=" + PrfUtils.getTenantId(HttpWebActivity.this);
            mWebView.loadUrl(url);
            Log.e("TAG_loadUrl", url);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            String url = mWebView.getUrl();
            Log.e("TAG_onKeyDown","url="+url);
            if (TextUtils.isEmpty(url)) {
                mWebView.clearCache(true);
                finish();
                return true;
            }
            if (url.contains("leadersearch/index.html") || url.contains("report/index.html")
                    || url.contains("integral/index.html") || url.contains("tax/index.html")
                    || url.contains("paicoin/index.html")|| url.contains("entryapprove/index.html")
                    || url.contains("bgcheck/index.html")) {
                mWebView.clearCache(true);
                finish();
                return true;
            }
            if (mWebView.canGoBack()) {
                mBtnClose.setVisibility(View.VISIBLE);
                mWebView.goBack();
                return true;
            } else {
                mWebView.clearCache(true);
                finish();
                return true;
            }

        }
        return super.onKeyDown(keyCode, event);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String url = mWebView.getUrl();
        Log.e("TAG_onBackPressed","url="+url);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                String url = mWebView.getUrl();
                Log.e("TAG_返回键","url="+url);
                if (url == null) {
                    finish();
                    return;
                }
                if (url.contains("leadersearch/index.html") || url.contains("report/index.html")
                        || url.contains("integral/index.html") || url.contains("tax/index.html")
                        || url.contains("paicoin/index.html")|| url.contains("entryapprove/index.html")
                        || url.contains("bgcheck/index.html")) {
                    finish();
                    return;
                }
                if (mWebView.canGoBack()) {
                    mBtnClose.setVisibility(View.VISIBLE);
                    mWebView.goBack();
                } else {
                    finish();
                }
                break;
            case R.id.iv_close:
                finish();
                break;
            case R.id.iv_Pi://pi币二级页面右上切换按钮
                isPi = true;
                ivPi.setVisibility(View.GONE);
                initLoadUrl();
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

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case PINEIGOU:
                try {
                    JSONObject data = rootData.getJson().getJSONObject("data");
                    PiModule piModule = JsonDataFactory.getData(PiModule.class, data);
                    String login_url = piModule.getLogin_url();
                    if (!TextUtils.isEmpty(login_url)) {
                        mWebView.loadUrl(login_url);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
                if (title.contains("点滴关怀")) {
                    isPi = false;
                    mTitleTv.setText("派商城");
                } else {
                    mTitleTv.setText(title);
                }
            } else {
                if (mCode == Constants.REPORT_FORM_CODE) {
                    mTitleTv.setText(getString(R.string.lable_report_form));
                } else if (mCode == Constants.LEADER_SEARCH_CODE) {
                    mTitleTv.setText(getString(R.string.lable_leaderquery));
                } else if (mCode == Constants.INTEGRAL_CODE) {
                    mTitleTv.setText(getString(R.string.lable_integral));
                } else if (mCode == Constants.PI_COIN_CODE) {
                    if (isPi) {
                        mTitleTv.setText(getString(R.string.lable_pi_coin));
                    } else {
                        isPi = false;
                        mTitleTv.setText("派商城");
                    }
                } else if (mCode == Constants.TAX_CODE) {
                    mTitleTv.setText(getString(R.string.lable_deduction));
                } else if (mCode == Constants.ENTRYAPPROVE_CODE) {//入职审批
                    mTitleTv.setText(getString(R.string.lable_entryapprove));
                }else if (mCode == Constants.BGDIAOCHA_CODE) {//极速背调
                    mTitleTv.setText(getString(R.string.lable_background_check));
                }
            }
            //调用js的init方法
            SharedPreferences preferences = PrfUtils.getSharePreferences(HttpWebActivity.this);
            String userId = preferences.getString("uid", "");
            String employee_no = preferences.getString("user_no", "");
            String tenantId = preferences.getString("tenantId", "");
            String token = PrfUtils.getToken(HttpWebActivity.this);
            String host = hostUrl + "v" + ApiUtils.getAppVersion(HttpWebActivity.this) + "/";

            if (mCode == Constants.PI_COIN_CODE && url.contains("paicoin/jump.html")) {

                String jsCode = "javascript:init(\"" + userId + "\",\""
                        + tenantId + "\",\""
                        + token + "\",\""
                        + host + "\",\""
                        + "\")";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mWebView.evaluateJavascript(jsCode, null);
                } else {
                    mWebView.loadUrl(jsCode);
                }
                Log.e("TAG_init", "调用js的init方法");
            } else if (mCode == Constants.PI_COIN_CODE && url.contains("encash_weixin.html")) {
                Log.e("TAG_微信登录", "openid=" + openid + ";nickname=" + nickname);
                mWebView.loadUrl("javascript:init('" + openid + "','" + nickname + "')");
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
            Toast.makeText(HttpWebActivity.this, getString(R.string.plans_web_toast), Toast.LENGTH_SHORT).show();
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
                if (title.contains("点滴关怀")) {
                    isPi = false;
                    mTitleTv.setText("派商城");
                    ivPi.setVisibility(View.VISIBLE);
                } else {
                    mTitleTv.setText(title);
                }
            } else {
                if (mCode == Constants.REPORT_FORM_CODE) {
                    mTitleTv.setText(getString(R.string.lable_report_form));
                } else if (mCode == Constants.LEADER_SEARCH_CODE) {
                    mTitleTv.setText(getString(R.string.lable_leaderquery));
                } else if (mCode == Constants.INTEGRAL_CODE) {
                    mTitleTv.setText(getString(R.string.lable_integral));
                } else if (mCode == Constants.PI_COIN_CODE) {
                    if (isPi) {
                        mTitleTv.setText(getString(R.string.lable_pi_coin));
                    } else {
                        isPi = false;
                        mTitleTv.setText("派商城");
                        ivPi.setVisibility(View.VISIBLE);
                    }
                } else if (mCode == Constants.TAX_CODE) {
                    mTitleTv.setText(getString(R.string.lable_deduction));
                } else if (mCode == Constants.ENTRYAPPROVE_CODE) {//入职审批
                    mTitleTv.setText(getString(R.string.lable_entryapprove));
                }else if (mCode == Constants.BGDIAOCHA_CODE) {//极速背调
                    mTitleTv.setText(getString(R.string.lable_background_check));
                }
            }
            super.onReceivedTitle(view, title);
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        //For Android  >= 4.1
        public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
            uploadMessage = valueCallback;
            showFileChooser();
        }

        //For Android  >= 5.0
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            uploadMessageAboveL = filePathCallback;
            showFileChooser();
            return true;
        }

    }

    private void showFileChooser() {
        String[] selectPicTypeStr = {"拍照", "从相册中选"};
//        IMG_PATH = Environment.getExternalStorageDirectory() + File.separator + "vancloud" + File.separator + System.currentTimeMillis() + ".jpg";
        AlertDialog mAlertDialog = new AlertDialog.Builder(this)
                .setItems(selectPicTypeStr,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                switch (which) {
                                    // 拍照
                                    case 0:

                                        PermissionsChecker mChecker = new PermissionsChecker(HttpWebActivity.this);
                                        if (mChecker.lacksPermissions(CAMERAPERMISSION)) {
                                            // 请求权限
                                            PermissionsActivity.startActivityForResult(HttpWebActivity.this, CAMERA_REQUESTCODE, CAMERAPERMISSION);
                                        } else {
                                            // 全部权限都已获取
                                            EasyPhotos.createCamera(HttpWebActivity.this)
                                                    .setFileProviderAuthority("com.vgtech.vantop.ui.userinfo.fileprovider")
                                                    .start(CAPTURE_TAG);
                                        }
                                        break;
                                    // 手机相册
                                    case 1:
                                        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                                        i.addCategory(Intent.CATEGORY_OPENABLE);
                                        i.setType("image/*");
                                        startActivityForResult(Intent.createChooser(i, "选择您要添加的照片"), CHOOSE_TAG);
                                        break;
                                    default:
                                        break;
                                }

                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        //DO NOTHING
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_TAG || requestCode == CHOOSE_TAG) {

            if (uploadMessage == null && uploadMessageAboveL == null) {
                return;
            }
            if (resultCode != RESULT_OK) {
                //一定要返回null,否则<input file> 就是没有反应
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }
                if (uploadMessageAboveL != null) {
                    uploadMessageAboveL.onReceiveValue(null);
                    uploadMessageAboveL = null;
                }
                return;
            }
            Uri imageUri = null;
            switch (requestCode) {
                case CAPTURE_TAG:
//                    if (data == null) {//指定了uri则data 为空
                    //返回图片地址集合：如果你只需要获取图片的地址，可以用这个
                    ArrayList<String> resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS);
                    //返回图片地址集合时如果你需要知道用户选择图片时是否选择了原图选项，用如下方法获取
                    boolean selectedOriginal = data.getBooleanExtra(EasyPhotos.RESULT_SELECTED_ORIGINAL, false);
                    if (resultPaths != null && resultPaths.size() > 0) {
                        String path = resultPaths.get(0);
                        File file = new File(path);
                        if (file.exists()) {
                            Uri localUri = Uri.fromFile(file);
                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
                            sendBroadcast(intent);
                            imageUri = Uri.fromFile(file);
                        }
                    }

//                    }
                    break;
                case CHOOSE_TAG:
                    if (data != null) {
                        Uri uri = data.getData();
//                        String path = FileUtils.getFilePathByUri(this, uri);
                        if (!TextUtils.isEmpty(uri.toString())) {
                            imageUri = uri;
                        }
                    }
                    break;
            }
            if (imageUri != null) {
                //上传文件
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(imageUri);
                    uploadMessage = null;
                }
                if (uploadMessageAboveL != null) {
                    uploadMessageAboveL.onReceiveValue(new Uri[]{imageUri});
                    uploadMessageAboveL = null;

                }
            } else {
                Log.e("TAG_图片上传", "imageUri为空");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
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

    String openid;
    String nickname;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventBusMsg event) {
        boolean author = event.isAuthor();
        if (author) {//微信是否授权
            openid = event.getOppnId();
            nickname = event.getWeName();
            mWebView.loadUrl("javascript:weixinCallback('" + true + "')");
        } else {
            mWebView.loadUrl("javascript:weixinCallback('" + false + "')");
        }

    }
}
