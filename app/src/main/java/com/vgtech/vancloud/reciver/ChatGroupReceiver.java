package com.vgtech.vancloud.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brook on 2016/9/22.
 */
public class ChatGroupReceiver extends BroadcastReceiver implements HttpListener<String> {

    private static final int CALL_BACK_LIST = 1;

    private NetworkManager mNetworkManager;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
//        Log.e("TAG_通知","ChatGroupReceiver");
        if (action.equals(Actions.ACTION_CHATGROUP_REFRESH)) {
            mContext = context;
            VanCloudApplication application = (VanCloudApplication) context
                    .getApplicationContext();
            mNetworkManager = application.getNetworkManager();
            loadContactList(context);
        }
    }


    private void loadContactList(Context context) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(mContext));
        params.put("ownid", PrfUtils.getUserId(mContext));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_CHATGROUP_LIST), params, context);
        mNetworkManager.load(CALL_BACK_LIST, path, this);
    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        boolean safe = ActivityUtils.prehandleNetworkData(mContext, this, callbackId, path, rootData, false);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALL_BACK_LIST:
                parsData(rootData);
                break;
        }
    }

    private void parsData(RootData rootData) {
        try {
            String userId = PrfUtils.getUserId(mContext);
            String tenantId = PrfUtils.getTenantId(mContext);
            List<ChatGroup> groups = new ArrayList<>();
            JSONObject json = rootData.getJson();
            String data = json.optString("data");
            if (!TextUtils.isEmpty(data)){
                JSONArray jsonArrayRoot = json.getJSONArray("data");
                for (int i = 0; i < jsonArrayRoot.length(); i++) {
                    JSONObject rootObject = jsonArrayRoot.getJSONObject(i);
                    ChatGroup group = new ChatGroup();
                    group.name = rootObject.getString("name");
                    group.groupNick = rootObject.getString("roomname");
                    group.isExit = rootObject.getBoolean("isExit");
                    group.setCreator(rootObject.getString("owner"));
                    if (TextUtils.isEmpty(group.owner))
                        group.owner = userId + tenantId;
                    else if (group.owner.length() == 18)
                        group.owner = userId + tenantId;
                    else {
                        if (group.owner.indexOf(tenantId) == -1) {
                            group.owner = userId + tenantId;
                        }
                    }
                    group.tenantId = tenantId;
                    group.type = ChatGroup.GroupTypeGroup;
                    JSONArray jsonArray = rootObject.getJSONArray("members");
                    group.peopleNum = jsonArray.length();
                    StringBuilder photoSb = new StringBuilder();
                    StringBuilder nameSb = new StringBuilder();
                    for (int j = 0; j < jsonArray.length(); j++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(j);
                        photoSb.append(jsonObject.getString("photo"));
                        nameSb.append(jsonObject.getString("name"));
                        if (j != jsonArray.length() - 1) {
                            photoSb.append(",");
                            nameSb.append(",");
                        }
                    }
                    group.nick = nameSb.toString();
                    group.avatar = photoSb.toString();
                    if (group.peopleNum > 1) {
                        groups.add(group);
                    }
                }
                synchdData(groups);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    private void synchdData(List<ChatGroup> groups) {
        ArrayList<ChatGroup> oldGroups = new ArrayList<>();
        List<ChatGroup> list = ChatGroup.findAll(PrfUtils.getUserId(mContext), PrfUtils.getTenantId(mContext));
        for (ChatGroup chatGroup : list) {
            if (ChatGroup.GroupTypeGroup.equals(chatGroup.type))
                oldGroups.add(chatGroup);
        }

        ActiveAndroid.beginTransaction();
        try {
            for (ChatGroup group : groups) {
                if (!oldGroups.contains(group)) {
                    group.save();
                }
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }


    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }
}
