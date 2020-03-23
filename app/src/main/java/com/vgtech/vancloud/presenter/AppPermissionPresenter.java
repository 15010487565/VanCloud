package com.vgtech.vancloud.presenter;

import android.content.Context;

import com.vgtech.common.api.AppModule;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.JsonDataFactory;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vic on 2016/9/30.
 * 权限相关
 */
public class AppPermissionPresenter {

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
            List<AppModule> appModules = AppModulePresenter.getOriModules(context, "moudle_permissions");
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
}
