package com.vgtech.vancloud.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.chenzhanyang.behaviorstatisticslibrary.BehaviorStatistics;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.Department;
import com.vgtech.common.provider.db.WorkGroup;
import com.vgtech.common.provider.db.WorkRelation;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.common.utils.MD5;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.presenter.LoginPresenter;
import com.vgtech.vancloud.reciver.GroupReceiver;
import com.vgtech.vancloud.signin.SigninMainX5Activity;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.module.ad.SplashAD;
import com.vgtech.vancloud.ui.register.ui.DefaultPositionActivity;
import com.vgtech.vancloud.ui.register.ui.EditDepartmentActivity;
import com.vgtech.vancloud.ui.register.ui.SetupDepartmentActivity;
import com.vgtech.vancloud.ui.register.ui.UpdatePositionActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.utils.IpUtil;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

import static com.vgtech.vancloud.presenter.LoginPresenter.CALL_BACK_LOGIN;


/**
 * 实时开屏，广告实时请求并且立即展现
 */
public class RSplashActivity extends Activity implements HttpListener<String>, View.OnClickListener {
    private TextView text_view_jump;
    private static final int SPLASH_AD_CALLBACK_ID = 1000001;
    private SimpleDraweeView mAdsImgView;
    private RSplashHandler mRSplashHandler;
    private boolean mCanClick;
    private static final int ISCHECK_LOGIN_STATE = 1000002;//校验登录状态是否离职
    private boolean isActive = true;
    @Inject
    Controller controller;

    public static class RSplashHandler extends Handler {
        private WeakReference<RSplashActivity> actRef;

        public RSplashHandler(RSplashActivity act) {
            actRef = new WeakReference<>(act);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (actRef == null || actRef.get() == null || actRef.get().isFinishing()) {
                return;
            }
            actRef.get().enterVgSystem();
        }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            boolean result = fixOrientation();
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash);

        text_view_jump = (TextView) findViewById(R.id.text_view_jump);

        text_view_jump.setVisibility(View.GONE);
        text_view_jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterVgSystem();
            }
        });

        // 默认请求http广告，若需要请求https广告，请设置AdSettings.setSupportHttps为true
        // AdSettings.setSupportHttps(true);

        // adUnitContainer
//        final RelativeLayout adsParent = (RelativeLayout) this.findViewById(R.id.adsRl);
        mAdsImgView = (SimpleDraweeView) findViewById(R.id.ads_img_view);
        mAdsImgView.setOnClickListener(this);

        String size_flag = "";
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        int height = outMetrics.heightPixels;
        if (height <= 1280) {
            size_flag = "small";
        } else {
            size_flag = "big";
        }

        //获取广告图片
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", "");
        params.put("tenant_id", "");
        params.put("size_flag", size_flag);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_SPLASH_AD), params, this);
        VanCloudApplication app = (VanCloudApplication) getApplication();
        app.getNetworkManager().load(SPLASH_AD_CALLBACK_ID, path, this, false);

        mRSplashHandler = new RSplashHandler(this);
        mRSplashHandler.sendEmptyMessageDelayed(1, 1500);
    }

    private void enterVgSystem() {
        boolean loginSignInType = LoginPresenter.loginSignInType(this);
//        Log.e("TAG_登录Rsp","loginSignInType="+loginSignInType);
        if (loginSignInType) {//跳转个人派币
            Intent intent = new Intent(this, SigninMainX5Activity.class);
            startActivity(intent);
            finish();
        }else {//跳转万客

            if (LoginPresenter.isLogin(this)) {
                String language = PrfUtils.getPrfparams(this, "is_language");

                if ("en".equals(language)) {
                    PrfUtils.getInstance(this).saveLanguage( 3);
                } else if ("zh".equals(language)) {
                    PrfUtils.getInstance(this).saveLanguage( 1);
                }
                enterSystem();
            } else {
                FileUtils.writeString(this,"RSplashActivity -> 判断用户未登录，退出到登录界面！\r\n");
                Intent intent = new Intent(this, LoginActivity.class);
                Uri uri = getIntent().getData();
                if (uri != null) {
                    intent.setData(uri);
                }
                startActivityForResult(intent, 11);
                finish();
            }
            sendBroadcast(new Intent(LoadingActivity.LOADING_FINISH));
        }

    }

    private void enterSystem() {
        String step = PrfUtils.getPrfparams(this, "step");
        nextStep(step);
    }

    /**
     * 通过setType状态指定跳转初始化页面
     */
    private void nextStep(String step) {
        int set_type = 4;
        if (!TextUtil.isEmpty(step)) {
            set_type = Integer.parseInt(step);
        }
        switch (set_type) {
            case 0:
                startActivityForResult(new Intent(this, SetupDepartmentActivity.class), 11);
                finish();
                break;
            case 1:
                Intent intent = new Intent(this, EditDepartmentActivity.class);
                intent.putExtra("indus_id", "");
                startActivityForResult(intent, 11);
                finish();
                break;
            case 2:
                startActivityForResult(new Intent(this, DefaultPositionActivity.class), 11);
                finish();
                break;
            case 3:
                startActivityForResult(new Intent(this, UpdatePositionActivity.class), 11);
                finish();
                break;
            default:
                SharedPreferences preferences = PrfUtils.getSharePreferences(this);
                String username = preferences.getString("username", "");
                String password = preferences.getString("password", "");
                String area_code = preferences.getString("areaCode", "");
//                Log.e("TAG_登录", "username=" + username + ";password=" + password + ";areaCode=" + area_code);
                Map<String, String> params = new HashMap<>();
                params.put("area_code", area_code);
                params.put("mobile", username);
                params.put("password", MD5.getMD5(password));
//                if (!TextUtils.isEmpty(tenantId))
//                    params.put("tenant_id", tenantId);
                VanCloudApplication app = (VanCloudApplication) getApplication();
                NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(this, URLAddr.URL_LOGIN), params, this);
                app.getNetworkManager().load(ISCHECK_LOGIN_STATE, path, this);

                break;
        }
    }

    @Override
    protected void onDestroy() {
        controller = null;
        super.onDestroy();

    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        switch (callbackId) {
            case SPLASH_AD_CALLBACK_ID:
                if (rootData.getCode() == 200) {
                    Gson gson = new Gson();
                    SplashAD splashAD = gson.fromJson(rootData.getJson().toString(), SplashAD.class);
                    if (splashAD.getData().isIs_show()) {
                        SplashAD.DataBeanX.DataBean data = splashAD.getData().getData();
                        if (data.isShow()) {
                            mCanClick = true;
                            String img_url = data.getImg_url();
                            mAdsImgView.setImageURI(img_url);
                            mAdsImgView.setTag(data.getImg_href());
                            mAdsImgView.getHierarchy().setFailureImage(R.mipmap.splash_nomal);
                            text_view_jump.setVisibility(View.VISIBLE);

                        } else {
                            enterVgSystem();
                        }
                    } else {
                        enterVgSystem();
                    }
                } else {
                    String localUrl = "res://" + this.getPackageName() + "/" + R.mipmap.splash_nomal;
                    mAdsImgView.setImageURI(Uri.parse(localUrl));
                    text_view_jump.setVisibility(View.VISIBLE);
                }
                break;
            case ISCHECK_LOGIN_STATE:
                if (rootData.getCode() == 200) {
                    processLoginResult(path, rootData);
                    init();

                } else {
                    if (dialogIsActivity()) {
                        new AlertDialog(this).builder().setTitle(getString(R.string.account_exception))
                                .setMsg(getString(R.string.account_exception_reasion))
                                .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ShortcutBadger.with(RSplashActivity.this).count(0);
                                        Intent intent = new Intent(RSplashActivity.this, LoginActivity.class);
                                        Utils.clearUserInfo(RSplashActivity.this);
                                        ApplicationProxy applicationProxy = (ApplicationProxy) RSplashActivity.this.getApplication();
                                        applicationProxy.clear();
                                        startActivityForResult(intent, 11);
                                        Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
                                        RSplashActivity.this.sendBroadcast(reveiverIntent);
                                        finish();
                                    }
                                }).show();
                    }
                }

                break;

        }

    }

    @Override
    public void onResponse(String response) {
        Log.e("TAG_自动登录","response="+response);
        if (String.valueOf(CALL_BACK_LOGIN).equals(response)||String.valueOf(ISCHECK_LOGIN_STATE).equals(response)){
            ShortcutBadger.with(RSplashActivity.this).count(0);
            Intent intent = new Intent(RSplashActivity.this, LoginActivity.class);
            Utils.clearUserInfo(RSplashActivity.this);
            ApplicationProxy applicationProxy = (ApplicationProxy) RSplashActivity.this.getApplication();
            applicationProxy.clear();
            startActivityForResult(intent, 11);
            Intent reveiverIntent = new Intent(BaseActivity.RECEIVER_EXIT);
            RSplashActivity.this.sendBroadcast(reveiverIntent);
            finish();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        String localUrl = "res://" + this.getPackageName() + "/" + R.mipmap.splash_nomal;
        mAdsImgView.setImageURI(Uri.parse(localUrl));
        text_view_jump.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ads_img_view:
                if (mCanClick) {
                    String img_herf = "";
                    if (v.getTag() != null && v.getTag() instanceof String) {
                        img_herf = (String) v.getTag();
                    } /*else {
                        img_herf = "https://www.vgsaas.com/";
                    }*/
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse(img_herf);
                    intent.setData(uri);
                    startActivity(intent);
                    mRSplashHandler.removeCallbacksAndMessages(null);
                }
                break;
        }
    }

    public void processLoginResult(NetworkPath path, RootData rootData) {
        Map<String, String> params = path.getPostValues();
        JSONObject jsonObject = rootData.getJson();
        try {
            JSONObject resultObject = jsonObject.getJSONObject("data");
            String user_type = resultObject.getString("user_type");
            String username = params.get("mobile");
            String area_code = params.get("area_code");
            UserAccount userAccount = JsonDataFactory.getData(UserAccount.class, resultObject);
            SharedPreferences preferences = PrfUtils.getSharePreferences(this);
            {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", username);
                PrfUtils.setUserPhone(this, username);
                editor.putString("user_no", resultObject.getString("user_no"));
                editor.putString("tenant_no", resultObject.getString("tenant_no"));
                if (resultObject.has("vantop_service_host"))
                    editor.putString("vantop_service_host", resultObject.getString("vantop_service_host"));
                editor.putString("user_type", user_type);
                editor.putString("tenant_type", resultObject.getString("tenant_type"));

                String luid = preferences.getString("uid", null);
                if (!userAccount.getUid().equals(luid)) {
                    editor.remove("departVersion");
                    editor.remove("user_version");
                    editor.remove("workGroup_version");
                    editor.commit();
//                    new User().deleteAll(mBaseActivity);
                    new Department().deleteAll(this);
                    new WorkGroup().deleteAll(this);
                    new WorkRelation().deleteAll(this);
                }
                editor.putString("uid", userAccount.getUid());
                VanCloudApplication app = (VanCloudApplication) getApplication();
                app.getApiUtils().getSignParams().put(URLAddr.URL_PARAM_OID, userAccount.getUid());
                editor.putString("token", userAccount.token);
                editor.putString("step", userAccount.step);
                editor.putString("areaCode", area_code);

                editor.putString("service_host", userAccount.service_host);

                editor.putString("tenantId", userAccount.tenant_id);
                editor.putString("tenantNameEn", userAccount.tenant_name_en);
                editor.putString("tenantName", userAccount.tenant_name);
                editor.putString("tenants", resultObject.getJSONArray("tenants").toString());
                editor.putString("moudle_permissions", resultObject.getJSONArray("moudle_permissions").toString());
//                String moudle_permissions = resultObject.getJSONArray("moudle_permissions").toString();

                String language = PrfUtils.getPrfparams(this , "is_language");
//                Log.e("TAG_自动登陆","language="+language);
                if (TextUtils.isEmpty(language)){
                    PrfUtils.savePrfparams(this, "is_language", "zh");
                }
                editor.commit();
            }

            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.putExtra("login", true);
            Uri uri = this.getIntent().getData();
            if (uri != null) {
                mainIntent.setData(uri);
            }
            this.startActivityForResult(mainIntent, 11);
            this.finish();

            SharedPreferences preferences1 = PrfUtils.getSharePreferences(this);
            String userId = preferences1.getString("uid", "");
            String employee_no = preferences1.getString("user_no", "");
            String tenantId = preferences1.getString("tenantId", "");

            HashMap<String, String> params1 = new HashMap<>();
            params1.put("user_id", userId);
            params1.put("employee_no", employee_no);
            params1.put("tenant_id", tenantId);
            params1.put("permission_id", "2000");
            params1.put("operation_ip", IpUtil.getIpAddressString());
            params1.put("operation_url", "");

            BehaviorStatistics.getInstance().startBehavior(params1);
//            } else {
//               sendBroadcast(new Intent(MainActivity.RECEIVER_XMPP));
//               finish();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        sendBroadcast(new Intent(Actions.ACTION_CHATGROUP_REFRESH));
        sendBroadcast(new Intent(GroupReceiver.REFRESH));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isActive = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    public boolean dialogIsActivity() {
        return isActive;
    }

}
