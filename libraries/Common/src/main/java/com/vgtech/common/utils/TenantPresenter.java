package com.vgtech.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.Tenant;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vic on 2016/9/21.
 */
public class TenantPresenter {
    public static boolean isVanTop(Context context) {
        return PrfUtils.getPrfparams(context,"tenant_type").equals("vantop");
//        return true;
    }

    public static List<Tenant> getTenant(Context context) {
        String jsonTenant = PrfUtils.getPrfparams(context, "tenants");
        List<Tenant> tenants = null;
        try {
            tenants = JsonDataFactory.getDataArray(Tenant.class, new JSONArray(jsonTenant));
        } catch (JSONException e) {
            e.printStackTrace();
            tenants = new ArrayList<>();
        }
        return tenants;
    }

    public static Tenant getCurrentTenant(Context context) {
        Tenant tenant = new Tenant();
        SharedPreferences preferences = PrfUtils.getSharePreferences(context);
        String tenantId = preferences.getString("tenantId", null);
        //中英文切换
        String language = PrfUtils.getPrfparams(context , "is_language");
//        Log.e("TAG_语言","language="+language);
        String tenantName = null;
        if ("en".equals(language)) {
            tenantName = preferences.getString("tenantNameEn", null);
        } else if ("zh".equals(language)) {
            tenantName = preferences.getString("tenantName", null);
        }
        tenant.tenant_id = tenantId;
        tenant.tenant_name = tenantName;
        return tenant;
    }
}
