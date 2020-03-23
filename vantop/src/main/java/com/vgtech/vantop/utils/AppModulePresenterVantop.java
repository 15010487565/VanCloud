package com.vgtech.vantop.utils;

import android.content.Context;

import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.AppModule;
import com.vgtech.common.api.JsonDataFactory;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vic on 2016/9/21.
 */
public class AppModulePresenterVantop {
    public enum Type {
        notice, work_flow, shenqing, calendar, task, work_reportting, topic, help, order, meeting, clock_out, kaoqin, sign, beidiao, zhaopin,
        salary, flow, vote, unknow, see_more, neigou, xinlitijian, luntan, guanggao,zuzhijiagou,qita;

        public static Type getType(String tag) {
            Type[] types = Type.values();
            for (Type type : types) {
                if (type.toString().equals(tag)) {
                    return type;
                }
            }
            return Type.unknow;
        }
    }

    /**
     * 应用--仅获取已开通模块
     *
     * @param context
     * @return
     */
    public static boolean hasOpenedModule(Context context, String moduleTag) {
        List<AppModule> modules = getOriModules(context, "moudle_permissions");
        boolean opened = false;
        for (AppModule appModule : modules) {
            if (moduleTag.equals(appModule.tag)) {
                opened = true;
                break;
            }
        }

        return opened;
    }

    public static boolean isOpenAdXinxiliu(Context ctx) {
        List<AppModule> childModules = getChildModules(ctx, Type.guanggao);
        for (AppModule module : childModules) {
            if ("guanggao:xinxiliu".equals(module.tag)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOpenZuzhijiagou(Context ctx) {
        List<AppModule> childModules = getChildModules(ctx, Type.zuzhijiagou);
        for (AppModule module : childModules) {
            if ("zuzhijiagou:showzuzhijiagou".equals(module.tag)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOpenPermission(Context ctx,Type type,String permissionTag){
        List<AppModule> childModules = getChildModules(ctx, type);
        for (int i = 0; i < childModules.size(); i++) {
            if (permissionTag.equals(childModules.get(i).tag)){
                return true;
            }
        }
        return false;
    }

    public static List<AppModule> getChildModules(Context ctx, Type parentTag) {
        List<AppModule> modules = getOriModules(ctx, "moudle_permissions");
        AppModule parentModule = null;
        for (AppModule appModule : modules) {
            if (parentTag.toString().equals(appModule.tag)) {
                parentModule = appModule;
                break;
            }
        }
        List<AppModule> openModules = new ArrayList<>();
        if (parentModule != null) {
            try {
                List<AppModule> appModules = JsonDataFactory.getDataArray(AppModule.class, parentModule.getJson().getJSONArray("permissions"));
                return appModules;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return openModules;
    }

    /**
     * 考勤打卡--仅获取已开通模块
     *
     * @param context
     * @return
     */
    public static List<AppModule> getClockOutModules(Context context) {
        List<AppModule> modules = getOriModules(context, "moudle_permissions");
        AppModule approveModule = null;
        for (AppModule appModule : modules) {
            if (Type.clock_out.toString().equals(appModule.tag)) {
                approveModule = appModule;
                break;
            }
        }
        List<AppModule> openModules = new ArrayList<>();
        if (approveModule != null) {
            try {
                List<AppModule> appModules = JsonDataFactory.getDataArray(AppModule.class, approveModule.getJson().getJSONArray("permissions"));
                return appModules;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return openModules;
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
