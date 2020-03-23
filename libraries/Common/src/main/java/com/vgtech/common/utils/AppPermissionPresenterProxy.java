package com.vgtech.common.utils;

import android.content.Context;

import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.AppModule;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.JsonDataFactory;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vic on 2017/1/12.
 */
public class AppPermissionPresenterProxy {
    /**
     * 检查指定模块是否具有该权限
     *
     * @param context
     * @param type    模块名称
     * @param tag     权限名称
     * @return
     */
    public static boolean hasPermission(Context context, AppPermission.Type type, String tag) {
        boolean hasTag = false;
        List<AppPermission> appPermissions = getAppPermission(context, type);
        for (AppPermission appPermission : appPermissions) {
            if (tag.equals(appPermission.tag)) {
                hasTag = true;
                break;
            }
        }
        return hasTag;
    }

    /**
     * 返回指定模块全部权限
     *
     * @param context
     * @param type    模块名称
     * @return 权限列表
     */
    public static List<AppPermission> getAppPermission(Context context, AppPermission.Type type) {
        List<AppPermission> appPermissions = new ArrayList<>();
        try {
            List<AppModule> appModules = getOriModules(context, "moudle_permissions");
            for (AppModule appModule : appModules) {
                if (type.toString().equals(appModule.tag)) {
                    List<AppPermission> permissions = JsonDataFactory.getDataArray(AppPermission.class, appModule.getJson().getJSONArray("permissions"));
                    appPermissions.addAll(permissions);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return appPermissions;
    }
    public static List<AppModule> getOriModules(Context context, String moduleKey) {
        String jsonTenant = PrfUtils.getPrfparams(context, moduleKey);
        List<AppModule> modules = null;
        try {
            modules = JsonDataFactory.getDataArray(AppModule.class, new JSONArray(jsonTenant));
        } catch (JSONException e) {
            e.printStackTrace();
            modules = new ArrayList<>();
        }
        return modules;
    }
}
