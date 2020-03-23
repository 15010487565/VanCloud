package com.vgtech.common.utils;

import android.content.Context;
import android.net.Uri;

import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vic on 2016/10/28.
 */
public class ImageCacheManager {
    public static Map<String, String> staffToPhoto = new HashMap<>();
    public static void getImage(final Context context, final SimpleDraweeView imageView, final String staff_no) {

        if (staffToPhoto.containsKey(staff_no)) {
            String photo = staffToPhoto.get(staff_no);
            ImageOptions.setUserImage(imageView, photo);
            return;
        }
        ImageOptions.setUserImage(imageView, "");
        ApplicationProxy applicationProxy = (ApplicationProxy) context.getApplicationContext();
        Uri uri =
                Uri.parse(ApiUtils.generatorUrl(context, URLAddr.URL_USER_HEAD_PHOTO)).buildUpon()
                        .appendQueryParameter("staffno", staff_no)
                        .appendQueryParameter("tenant_id", PrfUtils.getTenantId(context)).build();
        NetworkPath path = new NetworkPath(uri.toString());
        applicationProxy.getNetworkManager().load(1, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
                boolean safe = ActivityUtils.prehandleNetworkData(context, this, callbackId, path, rootData, false);
                if (!safe || context == null || imageView == null) {
                    return;
                }
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    String staffno = jsonObject.getString("staffno");
                    String photo = jsonObject.getString("logo");
                    staffToPhoto.put(staffno, photo);
                    ImageOptions.setUserImage(imageView, photo);
                } catch (JSONException e) {
                    e.printStackTrace();
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
}
