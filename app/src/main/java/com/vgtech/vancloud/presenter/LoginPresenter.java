package com.vgtech.vancloud.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.VolleyError;
import com.chenzhanyang.behaviorstatisticslibrary.BehaviorStatistics;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.Department;
import com.vgtech.common.provider.db.WorkGroup;
import com.vgtech.common.provider.db.WorkRelation;
import com.vgtech.common.utils.MD5;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.reciver.GroupReceiver;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.SplashActivity;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.register.ui.DefaultPositionActivity;
import com.vgtech.vancloud.ui.register.ui.EditDepartmentActivity;
import com.vgtech.vancloud.ui.register.ui.SetupDepartmentActivity;
import com.vgtech.vancloud.ui.register.ui.UpdatePositionActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.utils.IpUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vic on 2016/9/21.
 */
public class LoginPresenter implements HttpListener<String> {

    /**
     *  是否登录个人派币
     * @param context
     * @return
     */
    public static boolean loginSignInType(Context context) {
        String loginSignInType = PrfUtils.getPrfparams(context, "sigin_token");
        return !TextUtil.isEmpty(loginSignInType);
    }

    public static boolean isLogin(Context context) {
        boolean isLogin = !TextUtils.isEmpty(PrfUtils.getToken(context)) && !TextUtils.isEmpty(PrfUtils.getPrfparams(context, "moudle_permissions"));
        return isLogin;
    }

    public enum LoginType {
        login, logout;
    }

    public static final int CALL_BACK_LOGIN = 1;
    public static final int CALL_BACK_CONFIRM = 2;

    private BaseActivity mBaseActivity;
    private Controller mController;
    public LoginType mLoginType;

    public LoginPresenter(BaseActivity baseActivity, Controller controller) {
        this(baseActivity, controller, LoginType.login);
    }

    public LoginPresenter(BaseActivity baseActivity, Controller controller, LoginType loginType) {
        mBaseActivity = baseActivity;
        mController = controller;
        mLoginType = loginType;
    }

    private String mOriPwd;

    public void login() {
        String tenantId = PrfUtils.getTenantId(mBaseActivity);
        login(tenantId);
    }

    private boolean mNeedNotify = true;

    public void login(boolean needNotify) {
        mNeedNotify = needNotify;
        String tenantId = PrfUtils.getTenantId(mBaseActivity);
        login(tenantId);
    }

    public void login(String tenantId) {
        SharedPreferences preferences = PrfUtils.getSharePreferences(mBaseActivity);
        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);
        String area_code = preferences.getString("areaCode", null);
        login(area_code, username, password, tenantId);

    }

    /**
     * 登录
     */
    public void login(String areaCode, String username, String password, String tenantId) {
        if (mNeedNotify)
            mBaseActivity.showLoadingDialog(mBaseActivity, mBaseActivity.getString(R.string.loading_login));
        VanCloudApplication app = (VanCloudApplication) mBaseActivity.getApplication();
        mOriPwd = password;
        Map<String, String> params = new HashMap<>();
        params.put("area_code", areaCode);
        params.put("mobile", username);
        params.put("password", MD5.getMD5(password));
        if (!TextUtils.isEmpty(tenantId))
            params.put("tenant_id", tenantId);

        NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(mBaseActivity, URLAddr.URL_LOGIN), params, mBaseActivity);
        app.getNetworkManager().load(CALL_BACK_LOGIN, path, this);

    }
    /**
     * 个人信息保护指引确认
     */
    public void userauthConfirm(String deviceId, String phone_model) {

        VanCloudApplication app = (VanCloudApplication) mBaseActivity.getApplication();
        Map<String, String> params = new HashMap<>();
//        params.put("device_id", deviceId);
        params.put("phone_model", phone_model);
        NetworkPath path = new NetworkPath(ApiUtils.generatorLocalUrl(mBaseActivity, URLAddr.URL_USERAUTH_CONFIRM), params, mBaseActivity);
        app.getNetworkManager().load(CALL_BACK_CONFIRM, path, this);
    }

    public void processLoginResult(NetworkPath path, RootData rootData) {
        Map<String, String> params = path.getPostValues();
        JSONObject jsonObject = rootData.getJson();
        try {
            JSONObject resultObject = jsonObject.getJSONObject("data");
            String user_type = resultObject.optString("user_type");
            String username = params.get("mobile");
            String area_code = params.get("area_code");
            UserAccount userAccount = JsonDataFactory.getData(UserAccount.class, resultObject);
            SharedPreferences preferences = PrfUtils.getSharePreferences(mBaseActivity);
            {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", username);
                PrfUtils.setUserPhone(mBaseActivity, username);
                editor.putString("user_no", resultObject.optString("user_no"));
                editor.putString("tenant_no", resultObject.optString("tenant_no"));
                if (resultObject.has("vantop_service_host"))
                    editor.putString("vantop_service_host", resultObject.optString("vantop_service_host"));
                editor.putString("user_type", user_type);
                editor.putString("tenant_type", resultObject.optString("tenant_type"));
                if (!TextUtils.isEmpty(mOriPwd))
                    editor.putString("password", mOriPwd);
                String luid = preferences.getString("uid", null);
                if (!userAccount.getUid().equals(luid)) {
                    editor.remove("departVersion");
                    editor.remove("user_version");
                    editor.remove("workGroup_version");
                    editor.commit();
//                    new User().deleteAll(mBaseActivity);
                    new Department().deleteAll(mBaseActivity);
                    new WorkGroup().deleteAll(mBaseActivity);
                    new WorkRelation().deleteAll(mBaseActivity);
                }
                editor.putString("uid", userAccount.getUid());
                VanCloudApplication app = (VanCloudApplication) mBaseActivity.getApplication();
                app.getApiUtils().getSignParams().put(URLAddr.URL_PARAM_OID, userAccount.getUid());
                editor.putString("token", userAccount.token);
                editor.putString("step", userAccount.step);
                editor.putString("areaCode", area_code);
//                if (Constants.DEBUG) {
//                    editor.putString("service_host", userAccount.service_host.replace("http", PrfUtils.getPrfparams(mBaseActivity, "scheme", URLAddr.SCHEME)));//.replace("app.vgsaas.com", URLAddr.IP));
//                } else {
                editor.putString("service_host", userAccount.service_host);
//                editor.putString("service_host", "http://192.168.2.77:8085/");
//                }
                editor.putString("tenantId", userAccount.tenant_id);
                //中英文切换
                editor.putString("tenantNameEn", userAccount.tenant_name_en);
                editor.putString("tenantName", userAccount.tenant_name);

                editor.putString("tenants", resultObject.getJSONArray("tenants").toString());
                editor.putString("moudle_permissions", resultObject.getJSONArray("moudle_permissions").toString());


                String language = PrfUtils.getPrfparams(mBaseActivity , "is_language");
                if (TextUtils.isEmpty(language)){
                    PrfUtils.savePrfparams(mBaseActivity, "is_language", "zh");
                }
                editor.commit();
            }
            mController.pref().storageAccount(userAccount);
            if (!mNeedNotify)
                return;
            mBaseActivity.dismisLoadingDialog();
            if (LoginType.login == mLoginType) {
                enterSystem(userAccount.step);

                SharedPreferences preferences1 = PrfUtils.getSharePreferences(mBaseActivity);
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
            } else {
//                Log.e("TAG_关闭","关闭登录页面");
                mBaseActivity.sendBroadcast(new Intent(MainActivity.RECEIVER_XMPP));
                mBaseActivity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        if (Constants.DEBUG){
            Log.e("TAG_返回数据","rootData="+rootData.toString());
        }
        if (mBaseActivity == null || mBaseActivity.isFinishing())
            return;
        boolean safe = ActivityUtils.prehandleNetworkData(mBaseActivity, this, callbackId, path, rootData, true);
        if (!safe) {
            mBaseActivity.dismisLoadingDialog();
            return;
        }
        switch (callbackId) {
            case CALL_BACK_LOGIN:
                processLoginResult(path, rootData);
                init();
                break;
            case CALL_BACK_CONFIRM:
                break;
        }
    }

    private void init() {
        mBaseActivity.sendBroadcast(new Intent(Actions.ACTION_CHATGROUP_REFRESH));
        mBaseActivity.sendBroadcast(new Intent(GroupReceiver.REFRESH));
    }

    public void enterSystem(String step) {
        nextStep(step);
    }

    public void enterSystem() {
        String step = PrfUtils.getPrfparams(mBaseActivity, "step");
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
                mBaseActivity.startActivityForResult(new Intent(mBaseActivity, SetupDepartmentActivity.class), 11);
                mBaseActivity.finish();
                break;
            case 1:
                Intent intent = new Intent(mBaseActivity, EditDepartmentActivity.class);
                intent.putExtra("indus_id", "");
                mBaseActivity.startActivityForResult(intent, 11);
                mBaseActivity.finish();
                break;
            case 2:
                mBaseActivity.startActivityForResult(new Intent(mBaseActivity, DefaultPositionActivity.class), 11);
                mBaseActivity.finish();
                break;
            case 3:
                mBaseActivity.startActivityForResult(new Intent(mBaseActivity, UpdatePositionActivity.class), 11);
                mBaseActivity.finish();
                break;
            default:
                boolean isMain = mBaseActivity instanceof MainActivity;
                if (isMain) {
                    Intent mainIntent = new Intent(mBaseActivity, SplashActivity.class);
                    mBaseActivity.startActivity(mainIntent);
                    mBaseActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    mBaseActivity.finish();
                } else {
                    Intent mainIntent = new Intent(mBaseActivity, MainActivity.class);
                    mainIntent.putExtra("login", true);
                    Uri uri = mBaseActivity.getIntent().getData();
                    if (uri != null) {
                        mainIntent.setData(uri);
                    }
                    mBaseActivity.startActivityForResult(mainIntent, 11);
                    mBaseActivity.finish();
                }

                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }
}
