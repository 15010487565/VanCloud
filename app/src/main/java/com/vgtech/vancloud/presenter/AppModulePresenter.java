package com.vgtech.vancloud.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.AppModule;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.utils.XMLResParser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vic on 2016/9/21.
 */
public class AppModulePresenter {
    public enum Type {
        notice, work_flow, shenqing, calendar, task, work_reportting,
        topic, help, order, meeting, clock_out, kaoqin, sign,
        beidiao,bgdiaocha,entryapprove,zhaopin, salary, flow, vote, unknow,
        see_more, neigou, xinlitijian, luntan, jixiao, leadersearch,
        gongzuorizhi, secdefreport, integral, tax;

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
     * 查看更多--获取所有模块
     *
     * @param context
     * @return
     */
    public static List<AppModule> getAppAllModules(Context context) {
        List<AppModule> modules = getOriModules(context, "modules");
        List<AppModule> appModules = new ArrayList<>();
        XMLResParser.AppMenu[] appMenus = getAppMenu(context, R.xml.app_module);
        for (AppModule appModule : modules) {
            AppModule am = applyModule(appModule, appMenus);
            if (am != null)
                appModules.add(am);
        }
        return appModules;
    }

    /**
     * 应用--仅获取已开通模块
     *
     * @param context
     * @return
     */
    public static List<AppModule> getAppOpenModules(Context context) {
        List<AppModule> modules = getOriModules(context, "moudle_permissions");
//        AppModule neigou = new AppModule();
//        neigou.tag = "neigou";
//        modules.add(neigou);
        XMLResParser.AppMenu[] appMenus = getAppMenu(context, R.xml.app_module);
        List<AppModule> openModules = new ArrayList<>();
        for (AppModule appModule : modules) {
            appModule = applyModule(appModule, appMenus);
            if (appModule != null) {
                openModules.add(appModule);
            }
        }
        return openModules;
    }
    /**
     * 待办--筛选已开通模块
     *
     * @param context
     * @return
     */
    public static XMLResParser.MenuItem[] getTodoModules(Context context,List<XMLResParser.MenuItem> list) {
        List<AppModule> modules = getOriModules(context, "moudle_permissions");

        XMLResParser parser = new XMLResParser(context);
            XMLResParser.RootData rootData = parser
                    .parser(R.xml.todo_menu);
            XMLResParser.MenuItem[] items = rootData
                    .getChildren(
                            XMLResParser.MenuItem.class,
                            true);

        for (AppModule appModule : modules) {
            for ( XMLResParser.MenuItem item : items) {
                if (!TextUtils.isEmpty(appModule.tag) && appModule.tag.equals(item.getId())) {
                    list.add(item);
                }
            }

        }
        XMLResParser.MenuItem[] array = list.toArray(new XMLResParser.MenuItem[list.size()]);
        return array;
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

    /**
     * 审批--仅获取已开通模块
     *
     * @param context
     * @return
     */
    public static List<AppModule> getApproveModules(Context context) {
        List<AppModule> modules = getOriModules(context, "moudle_permissions");
        AppModule approveModule = null;
        for (AppModule appModule : modules) {
            if (Type.work_flow.toString().equals(appModule.tag)) {
                approveModule = appModule;
                break;
            }
        }
        List<AppModule> openModules = new ArrayList<>();
        if (approveModule != null) {
            try {
                List<AppModule> appModules = JsonDataFactory.getDataArray(AppModule.class, approveModule.getJson().getJSONArray("permissions"));
                XMLResParser.AppMenu[] appMenus = getAppMenu(context, R.xml.approve_module);
                for (AppModule appModule : appModules) {
                    appModule = applyModule(appModule, appMenus);
                    if (appModule != null)
                        openModules.add(appModule);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return openModules;
    }

    /**
     * 申请--仅获取已开通模块
     *
     * @param context
     * @return
     */
    public static List<AppModule> getApplyModules(Context context) {
        List<AppModule> modules = getOriModules(context, "moudle_permissions");
       if (Constants.DEBUG){
           Log.e("TAG_申请模块",modules.toString());
       }
        AppModule approveModule = null;
        for (AppModule appModule : modules) {
            if (Type.shenqing.toString().equals(appModule.tag)) {
                approveModule = appModule;
                break;
            }
        }
        List<AppModule> openModules = new ArrayList<>();
        if (approveModule != null) {
            try {
                List<AppModule> appModules = JsonDataFactory.getDataArray(AppModule.class, approveModule.getJson().getJSONArray("permissions"));
                XMLResParser.AppMenu[] appMenus = getAppMenu(context, R.xml.approve_module);
                for (AppModule appModule : appModules) {
                    appModule = applyModule(appModule, appMenus);
                    if (appModule != null)
                        openModules.add(appModule);
                }
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


    public static List<XMLResParser.AppMenu> getAppQuickMenu(Context context) {
        List<XMLResParser.AppMenu> appMenus = new ArrayList<>();
        XMLResParser parser = new XMLResParser(context);
        XMLResParser.RootData rootData = parser
                .parser(R.xml.app_quickoption);
        XMLResParser.AppMenu[] items = rootData
                .getChildren(
                        XMLResParser.AppMenu.class,
                        true);
        List<XMLResParser.AppMenu> appMenuList = Arrays.asList(items);
        appMenus.addAll(appMenuList);
        List<XMLResParser.AppMenu> menuList = new ArrayList<>();
        List<AppModule> applyModules = getApplyModules(context);
        List<AppModule> appModules = getAppOpenModules(context);
        for (int i = 0; i < appMenuList.size(); i++) {
            XMLResParser.AppMenu appMenu = appMenuList.get(i);
            if (i < 6) {
                for (AppModule appModule : applyModules) {
                    if (appMenu.getTag().equals(appModule.tag)) {
                        menuList.add(appMenu);
                        break;
                    }
                }
            } else if (appMenu.getTag().equals("meeting:appointment")) {
                if (AppPermissionPresenter.hasPermission(context, AppPermission.Type.meeting, AppPermission.Meeting.appointment.toString())) {
                    menuList.add(appMenu);
                }
            } else if (appMenu.getTag().equals("meeting:create")) {
                if (AppPermissionPresenter.hasPermission(context, AppPermission.Type.meeting, AppPermission.Meeting.call.toString())) {
                    menuList.add(appMenu);
                }
            } else if (appMenu.getTag().equals("investigate:start")) {
                if (AppPermissionPresenter.hasPermission(context, AppPermission.Type.beidiao, AppPermission.Beidiao.start.toString())) {
                    menuList.add(appMenu);
                }
            } else {
                for (AppModule appModule : appModules) {
                    if (appMenu.getTag().equals(appModule.tag)) {
                        menuList.add(appMenu);
                        break;
                    }
                }
            }
        }
        return menuList;
    }

    public static XMLResParser.AppMenu[] getAppMenu(Context context, int resId) {
        XMLResParser parser = new XMLResParser(context);
        XMLResParser.RootData rootData = parser
                .parser(resId);
        XMLResParser.AppMenu[] items = rootData
                .getChildren(
                        XMLResParser.AppMenu.class,
                        true);
        return items;
    }

    public static AppModule applyModule(AppModule appModule, XMLResParser.AppMenu[] appMenus) {
        for (XMLResParser.AppMenu appMenu : appMenus) {
            if (!TextUtils.isEmpty(appModule.tag) && appModule.tag.equals(appMenu.getTag())) {
                appModule.resName = appMenu.getName();
                appModule.resIcon = appMenu.getIcon();
                appModule.resColor = appMenu.getColor();
                return appModule;
            }
        }
        return null;
    }

    public static List<AppModule> getOriModules(Context context, String moduleKey) {
        String jsonTenant = PrfUtils.getPrfparams(context, moduleKey);
//        Log.e("TAG_评论","jsonTenant="+jsonTenant);
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
