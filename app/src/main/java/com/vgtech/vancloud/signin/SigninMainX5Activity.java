package com.vgtech.vancloud.signin;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.TextView;

import com.huantansheng.easyphotos.EasyPhotos;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.common.ui.publish.PicSelectActivity;
import com.vgtech.common.ui.zxing.zxing.ToastUtil;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.IphoneDialog;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.common.image.ClipActivity;
import com.vgtech.vancloud.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

public class SigninMainX5Activity extends FragmentActivity implements View.OnClickListener {

    protected WebView mWebView;

    private VancloudLoadingLayout mLoadingLayout;
    View topTitle;
    private View mBtnClose;

    /**
     * 相机权限
     */
    protected static final int CAMERA_REQUESTCODE = 20001;
    protected static final String[] CAMERAPERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.READ_EXTERNAL_STORAGE
            , Manifest.permission.CAMERA
    };
    private static final int TAKE_PICTURE = 0x000000;
    private static final int FROM_PHOTO = 0x000001;
    private String path = "";
    private static final int PHOTO_CLIP = 0x000002;
    private String initUrl = "appstatic/individual/jump.html";
    private String payBackUrl = "appstatic/individual/recharge.html?";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            boolean result = fixOrientation();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sigin_mainx5);
        init();

        mWebView = (WebView) findViewById(R.id.wv_task);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);//允许js调用
        settings.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过JS打开新窗口
        settings.setAllowFileAccess(true);//在File域下，能够执行任意的JavaScript代码，同源策略跨域访问能够对私有目录文件进行访问等
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);//控制页面的布局(使所有列的宽度不超过屏幕宽度)
        mWebView.addJavascriptInterface(this, "appJs");//与js进行交互
        // 设置WebViewClient
        mWebView.setWebChromeClient(new WebChromeClient());

        settings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        settings.setBuiltInZoomControls(false); //设置内置的缩放控件。若为false，则该WebView不可缩放
        settings.setDisplayZoomControls(true); //隐藏原生的缩放控件
        settings.setBlockNetworkImage(false);//解决图片不显示
        settings.setLoadsImagesAutomatically(true); //支持自动加载图片
        settings.setDefaultTextEncodingName("gb2312");//设置编码格式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        settings.setTextZoom(100);// textZoom:100表示正常，120表示文字放大1.2倍
        //
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowContentAccess(true); // 是否可访问Content Provider的资源，默认值 true
        // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
        settings.setAllowFileAccessFromFileURLs(false);
        // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        settings.setAllowUniversalAccessFromFileURLs(false);


        mLoadingLayout = (VancloudLoadingLayout) findViewById(R.id.ll_loadingview);

        mLoadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initLoadUrl(initUrl);
            }
        });
        mLoadingLayout.setVisibility(View.GONE);
        initLoadUrl(initUrl);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Constants.DEBUG) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }
        //该界面打开更多链接
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                try {
                    WebView.HitTestResult hit = webView.getHitTestResult();
                    //hit.getExtra()为null或者hit.getType() == 0都表示即将加载的URL会发生重定向，需要做拦截处理
                    if (TextUtils.isEmpty(hit.getExtra()) || hit.getType() == 0) {
                        //通过判断开头协议就可解决大部分重定向问题了，有另外的需求可以在此判断下操作
                        Log.e("TAG_重定向", "重定向: " + hit.getType() + " && EXTRA（）" + hit.getExtra() + "------");
                        Log.e("TAG_重定向", "GetURL: " + webView.getUrl() + "\n" + "getOriginalUrl()" + webView.getOriginalUrl());
                        Log.d("TAG_重定向", "URL: " + url);
                    }
                    Log.d("TAG_encode", "URL: " + url);
                    if (url.startsWith("weixin://wap/pay?") // 微信
                            || url.startsWith("alipays://") // 支付宝
                            || url.startsWith("mailto://") // 邮件
                            || url.startsWith("tel:")// 电话
                            || url.startsWith("dianping://")// 大众点评
                        // 其他自定义的scheme
                    ) {
//                        String encode = URLEncoder.encode(indexUrl, "utf-8");
//                        if (!url.contains(encode)) {
//                            url = url + "&url=" + encode;
//                        }
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);

                        return true;
                    } else if (url.startsWith("https://wx.tenpay.com")) {
                        boolean weixinAvilible = isWeixinAvilible(SigninMainX5Activity.this);
                        if (weixinAvilible) {
                            //H5微信支付要用，不然说"商家参数格式有误"
                            Map<String, String> extraHeaders = new HashMap<String, String>();
                            extraHeaders.put("Referer", "https://app.vgsaas.com");
                            webView.loadUrl(url, extraHeaders);
                        } else {
                            ToastUtil.toast(SigninMainX5Activity.this, "未找到该应用！");
                        }
                        return true;
                    }
                } catch (Exception e) { // 防止crash
                    // (如果手机上没有安装处理某个scheme开头的url的APP,会导致crash)
                    e.printStackTrace();
                    return true;// 没有安装该app时，返回true，表示拦截自定义链接，但不跳转，避免弹出上面的错误页面
                }
                webView.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView webView, String url) {
                super.onPageFinished(webView, url);
                Log.e("TAG_个人派币====", "url=" + url);
                String localUrl = ApiUtils.generatorLocalUrl(SigninMainX5Activity.this, "appstatic/individual/jump.html");
                if (localUrl.equals(url)) {
                    String userId = PrfUtils.getPrfparams(SigninMainX5Activity.this, "sigin_userId");
                    String token = PrfUtils.getPrfparams(SigninMainX5Activity.this, "sigin_token");
                    String host = ApiUtils.generatorLocalUrl(SigninMainX5Activity.this, "v%1$d/");

                    mWebView.loadUrl("javascript:init('" + userId + "','" + token + "','" + host + "')");
                }
                if (url.contains("test.neigou.com") || url.contains("neigou.com")
                        || url.contains("openapi.test.neigou.coml")
                        || url.contains("openapi.neigou.com")) {
                    topTitle.setVisibility(View.VISIBLE);
                } else {
                    topTitle.setVisibility(View.GONE);
                }

            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String
                    failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e("TAG_加载失败", "errorCode=" + errorCode + ";description=" + description + ";failingUrl=" + failingUrl);

            }

            //回调该方法，处理HTTP认证错误
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest
                    request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                Log.e("TAG_加载失败Http", "error=" + errorResponse.getStatusCode());
            }

            //回调该方法，处理SSL认证错误
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError
                    error) {
                Log.e("TAG_加载失败Ssl", "error=" + error.getPrimaryError());
                super.onReceivedSslError(view, handler, error);

            }

            //回调该方法，请求已授权用户自动登录
            @Override
            public void onReceivedLoginRequest(WebView view, String realm, String
                    account, String args) {
                super.onReceivedLoginRequest(view, realm, account, args);
                Log.e("TAG_加载失败Ssl", "realm=" + realm + ";account=" + account + ";args=" + args);
            }
        });

    }

    /**
     * 检测是否安装微信
     *
     * @param context
     * @return
     */
    public static boolean isWeixinAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void init() {
        String title = getTitle().toString();
        setTitle(title);
        View backView = findViewById(R.id.btn_back);
        if (backView != null)
            backView.setOnClickListener(this);
        topTitle = findViewById(R.id.bg_titlebar);
        topTitle.setVisibility(View.GONE);
        findViewById(R.id.iv_back).setOnClickListener(this);
        mBtnClose = findViewById(R.id.iv_close);
//        mBtnClose.setVisibility(View.GONE);
        mBtnClose.setOnClickListener(this);
        TextView mTitleTv = (TextView) findViewById(R.id.tv_title);
        mTitleTv.setText("派商城");

    }

    /**
     * 请求加载的url
     */
    String indexUrl;

    private void initLoadUrl(String url) {

        indexUrl = ApiUtils.generatorLocalUrl(this, url);
        if (Constants.DEBUG) {
            Log.e("TAG_个人派币", "indexUrl=" + this.indexUrl);
        }
        mWebView.loadUrl(this.indexUrl);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.iv_back:

                if (mWebView.canGoBack()) {
                    mBtnClose.setVisibility(View.VISIBLE);
                    mWebView.goBack();
                }
                break;
            case R.id.iv_close:
                initLoadUrl(initUrl);
                break;

        }
    }

    //销毁 放置内存泄漏
    @Override
    public void onDestroy() {
        if (this.mWebView != null) {
            mWebView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
        mWebView.getSettings().setLightTouchEnabled(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        mWebView.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.e("TAG_返回","keyCode="+keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            String url = mWebView.getUrl();
            Log.e("TAG_返回","url="+url);
            if (TextUtils.isEmpty(url)) {
                mWebView.clearCache(true);
                finish();
                return true;
            }

            if (indexUrl.contains(url)) {//首页
                mWebView.clearCache(true);
                finish();
                return true;
            }
            if (url.startsWith("https://mclient.alipay.com/cashier/mobilepay.htm")){
                String userId = PrfUtils.getPrfparams(SigninMainX5Activity.this, "sigin_userId");
                initLoadUrl(payBackUrl+"ret=Y&uid="+userId);
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

    @JavascriptInterface
    public void loginOut() {

        PrfUtils.savePrfparams(this, "indexUrl", "");
        PrfUtils.savePrfparams(this, "sigin_userId", "");
        PrfUtils.savePrfparams(this, "sigin_token", "");
        ShortcutBadger.with(SigninMainX5Activity.this).count(0);
//                                FileUtils.writeString("SettingActivity -> 点击了退出登录,退出到登录界面！\r\n");
        Intent intent = new Intent(SigninMainX5Activity.this, SignInLoginActivity.class);
        Utils.clearUserInfo(SigninMainX5Activity.this);
        ApplicationProxy applicationProxy = (ApplicationProxy) SigninMainX5Activity.this.getApplication();
        applicationProxy.clear();
        startActivityForResult(intent, 11);
        Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
        SigninMainX5Activity.this.sendBroadcast(reveiverIntent);
        finish();
    }

    /**
     * 微信登录相关
     */
    private IWXAPI api;

    @JavascriptInterface
    public void weixinLogin() {

        //通过WXAPIFactory工厂获取IWXApI的示例
        api = WXAPIFactory.createWXAPI(this, com.vgtech.common.wxapi.Constants.APP_ID, true);
        //将应用的appid注册到微信
        api.registerApp(com.vgtech.common.wxapi.Constants.APP_ID);

        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";//
//                req.scope = "snsapi_login";//提示 scope参数错误，或者没有scope权限
        req.state = "wechat_sdk_微信登录";
        api.sendReq(req);
    }

    private boolean isTranslucentOrFloating() {
        boolean isTranslucentOrFloating = false;
        try {
            int[] styleableRes = (int[]) Class.forName("com.android.internal.R$styleable").getField("Window").get(null);
            final TypedArray ta = obtainStyledAttributes(styleableRes);
            Method m = ActivityInfo.class.getMethod("isTranslucentOrFloating", TypedArray.class);
            m.setAccessible(true);
            isTranslucentOrFloating = (boolean) m.invoke(null, ta);
            m.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isTranslucentOrFloating;

    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {

            return;
        }
        super.setRequestedOrientation(requestedOrientation);

    }

    private boolean fixOrientation() {
        try {
            Field field = Activity.class.getDeclaredField("mActivityInfo");
            field.setAccessible(true);
            ActivityInfo o = (ActivityInfo) field.get(this);
            o.screenOrientation = -1;
            field.setAccessible(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void uploadImageAndroid(final String imageUri) {

        if (imageUri != null) {
            showLoadingDialog(SigninMainX5Activity.this, "");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        //将图片转化为字符串
                        String imagebase64 = ReadImgToBinary.imgToBase64(imageUri);
                        Log.e("TAG_上传图片", "uploadImageAndroid=" + imagebase64);
                        mWebView.loadUrl("javascript:getImg('data:image/jpeg;base64," + imagebase64 + "')");

                    }
                });
            } else {
                // 19级以后用evaluateJavascript
                mWebView.post(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        //将图片转化为字符串
                        String imagebase64 = ReadImgToBinary.imgToBase64(imageUri);
                        Log.e("TAG_上传图片", "uploadImageAndroid=" + imagebase64);
                        mWebView.loadUrl("javascript:getImg('data:image/jpeg;base64," + imagebase64 + "')");
                        mWebView.evaluateJavascript("javascript:getImg('data:image/jpeg;base64," + imagebase64 + "')", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                // 如果不需要JS返回数据，该回调方法参数可以写成null
                            }
                        });
                        // 这么写
                        /// mWebView.evaluateJavascript("javascript:jsFunc('" + msg + "')", null);
                    }
                });
            }
        }
    }

    /**
     * @param mContext
     * @param contentStr
     */
    protected IphoneDialog iphoneDialog;

    public void showLoadingDialog(Context mContext, String contentStr) {
        if (iphoneDialog == null) {
            iphoneDialog = new IphoneDialog(mContext);
        }
        iphoneDialog.setMessage(contentStr);
        iphoneDialog.show(true);
    }

    /**
     *
     */
    public void dismisLoadingDialog() {
        if (iphoneDialog != null && iphoneDialog.isShowing()) {
            iphoneDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case TAKE_PICTURE: {
                //返回图片地址集合：如果你只需要获取图片的地址，可以用这个
                ArrayList<String> resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS);
                //返回图片地址集合时如果你需要知道用户选择图片时是否选择了原图选项，用如下方法获取
                boolean selectedOriginal = data.getBooleanExtra(EasyPhotos.RESULT_SELECTED_ORIGINAL, false);
                if (resultPaths != null && resultPaths.size() > 0) {
                    path = resultPaths.get(0);
                    Intent intent = new Intent(this, ClipActivity.class);
                    intent.putExtra("path", path);
                    startActivityForResult(intent, PHOTO_CLIP);
                }
            }
            break;
            case FROM_PHOTO: {
                String path = data.getStringExtra("path");
                Intent intent = new Intent(this, ClipActivity.class);
                intent.putExtra("path", path);
                startActivityForResult(intent, PHOTO_CLIP);
            }
            break;
            case PHOTO_CLIP: {
                String path = data.getStringExtra("path");
                uploadImageAndroid(path);

            }
            break;
        }

    }

    //关闭弹窗
    @JavascriptInterface
    public void uploadImageSucceed() {
        dismisLoadingDialog();
        startActivity(new Intent(this, SigninMainX5Activity.class));
    }

    /**
     * 选择图片
     */
    @JavascriptInterface
    public void uploadImage() {
        take();
    }

    public void take() {

        new ActionSheetDialog(this)
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .addSheetItem(getString(R.string.take), ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                PermissionsChecker mChecker = new PermissionsChecker(SigninMainX5Activity.this);
                                if (mChecker.lacksPermissions(CAMERAPERMISSION)) {
                                    // 请求权限
                                    PermissionsActivity.startActivityForResult(SigninMainX5Activity.this, CAMERA_REQUESTCODE, CAMERAPERMISSION);
                                } else {
                                    // 全部权限都已获取
                                    EasyPhotos.createCamera(SigninMainX5Activity.this)
                                            .setFileProviderAuthority("com.vgtech.vantop.ui.userinfo.fileprovider")
                                            .start(TAKE_PICTURE);
                                }
                            }
                        })

                .addSheetItem(getString(R.string.select_from_photo), ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                Intent intent = new Intent(getApplicationContext(),
                                        PicSelectActivity.class);
                                intent.putExtra("single", true);
                                startActivityForResult(intent, FROM_PHOTO);
                            }
                        }).show();
    }

}
