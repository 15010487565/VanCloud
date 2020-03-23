package com.vgtech.vancloud.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.User;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.common.group.GroupUtils;
import com.vgtech.vancloud.ui.module.me.WorkGroupActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangshaofang on 2015/11/1.
 */
public class GroupReceiver extends BroadcastReceiver implements HttpListener<String> {

    private static final int CALLBACK_CONTACT_LIST = 1;
    private static final int CALLBACK_CONTACT_LIST_INFO = 2;
    private static final int CALLBACK_CONTACT_WORKGROUP_LIST = 3;
    private static final int CALLBACK_WORKREPORT_TEMPLATE = 4;
    private static final int CALLBACK_URL_USER_CHANGELANGUAGE = 5;
    private static final int CALLBACK_USER_PERMISSIONS = 6;
    public static String REFRESH = "android.group.refresh";
    public static String PERMISSIONS_REFRESH = "android.moudle.permissions.refresh";
    private NetworkManager mNetworkManager;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        mContext = context;
        VanCloudApplication application = (VanCloudApplication) context
                .getApplicationContext();
        mNetworkManager = application.getNetworkManager();
//        Log.e("TAG_讨论组","action="+action);
        if (action.equals(REFRESH)) {
            if (!TenantPresenter.isVanTop(context)) {
//                loadContactList(context);
//                loadContactInfo(context);
            }
            loadWorkGroupInfo(context);
            loadWorkReportTemplate(context);
            if (TenantPresenter.isVanTop(context))
                setVanTopLanguage(context);
        } else if (PERMISSIONS_REFRESH.equals(action)) {
            loadMoudlePermissions(context);
        }
    }

    private void loadMoudlePermissions(Context context) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(context));
        params.put("user_id", PrfUtils.getUserId(context));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_USER_PERMISSIONS), params, context);
        mNetworkManager.load(CALLBACK_USER_PERMISSIONS, path, this);
    }

    private void loadWorkReportTemplate(Context context) {

        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(context));
        params.put("ownid", PrfUtils.getUserId(context));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_WORKREPORT_TEMPLATE), params, context);
        JSONObject jsonObject = mNetworkManager.getCache(path);
        if (jsonObject == null)
            mNetworkManager.load(CALLBACK_WORKREPORT_TEMPLATE, path, this, true);
        else {
            try {
                JSONObject resutObject = jsonObject.getJSONObject("data");
                PrfUtils.setWorkReportTempleate(mContext, resutObject.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setVanTopLanguage(final Context context) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("lang", PrfUtils.isChineseForAppLanguage(context) ? "C" : "E");
        String url = VanTopUtils.generatorUrl(context, UrlAddr.URL_USER_CHANGELANGUAGE);
        NetworkPath path = new NetworkPath(url, params, context, true);
        mNetworkManager.load(CALLBACK_URL_USER_CHANGELANGUAGE, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
                boolean safe = VanTopActivityUtils.prehandleNetworkData(context, this, callbackId, path, rootData, false);
                if (!safe) {
                    return;
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(String response) {

            }
        });
    }

    private void loadContactList(Context context) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(context));
        String departVersion = PrfUtils.getPrfparams(context, "departVersion");
        if (!TextUtil.isEmpty(departVersion))
            params.put("version", departVersion);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_REGISTER_GETCOMPANYDEPARTINFO), params, context);
        mNetworkManager.load(CALLBACK_CONTACT_LIST, path, this);
    }

    private void loadContactInfo(Context context) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(context));
        String user_version = PrfUtils.getPrfparams(context, "user_version");
        if (!TextUtil.isEmpty(user_version))
            params.put("version", user_version);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_GROUP_INFO), params, context);
        mNetworkManager.load(CALLBACK_CONTACT_LIST_INFO, path, this);
    }

    private void loadWorkGroupInfo(Context context) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(context));
        params.put("ownid", PrfUtils.getUserId(context));
        String workGroupVersion = PrfUtils.getPrfparams(context, "workGroup_version");
        if (!TextUtil.isEmpty(workGroupVersion))
            params.put("version", workGroupVersion);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_WORKGROUP_LIST), params, context);
        mNetworkManager.load(CALLBACK_CONTACT_WORKGROUP_LIST, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        boolean safe = ActivityUtils.prehandleNetworkData(mContext, this, callbackId, path, rootData, false);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_CONTACT_LIST:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject dataJson = jsonObject.getJSONObject("data");
                    String version = dataJson.getString("version");
                    String departVersion = PrfUtils.getPrfparams(mContext, "departVersion");
                    if (TextUtil.isEmpty(departVersion) || !departVersion.equals(version)) {
                        JSONObject resutObject = dataJson.getJSONArray("departs").getJSONObject(0);
                        if (GroupUtils.initGroups(mContext, resutObject))
                            PrfUtils.savePrfparams(mContext, "departVersion", version);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CALLBACK_USER_PERMISSIONS: {
                JSONObject jsonObject = rootData.getJson();
                try {
                    JSONArray dataJson = jsonObject.getJSONArray("data");
                    PrfUtils.savePrfparams(mContext, "moudle_permissions", dataJson.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case CALLBACK_CONTACT_LIST_INFO:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    String version = resutObject.getString("version");
                    String user_version = PrfUtils.getPrfparams(mContext, "user_version");
                    if (TextUtil.isEmpty(user_version) || !user_version.equals(version)) {
                        if (GroupUtils.initUsers(mContext, resutObject))
                            PrfUtils.savePrfparams(mContext, "user_version", version);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CALLBACK_CONTACT_WORKGROUP_LIST:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    if (TenantPresenter.isVanTop(mContext)) { // vantop用户只存储工作组里面user
                        if (resutObject.has("workGroups")) {
                            JSONArray jsonArray = resutObject.getJSONArray("workGroups");
                            List<com.vgtech.common.api.User> userList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject wgObject = jsonArray.getJSONObject(i);
                                List<com.vgtech.common.api.User> tmpList = JsonDataFactory.getDataArray(com.vgtech.common.api.User.class, wgObject.getJSONArray("user"));
                                for (com.vgtech.common.api.User user : tmpList)
                                    if (!userList.contains(user))
                                        userList.add(user);
                            }
                            User.updateVantopUserTable(userList, mContext);
                            String version = resutObject.getString("version");
                            String workGroupVersion = PrfUtils.getPrfparams(mContext, "workGroup_version");
                            if (TextUtil.isEmpty(workGroupVersion) || !workGroupVersion.equals(version)) {
                                if (GroupUtils.initWorkGroupInfo(mContext, resutObject))
                                    PrfUtils.savePrfparams(mContext, "workGroup_version", version);
                            }
                            mContext.sendBroadcast(new Intent(WorkGroupActivity.RECEIVER_GROUP_REFRESH));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CALLBACK_WORKREPORT_TEMPLATE:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    PrfUtils.setWorkReportTempleate(mContext, resutObject.toString());
                } catch (Exception e) {
                    e.printStackTrace();
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
