package com.vgtech.vancloud.ui.view;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupWindow;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.zxing.zxing.MipcaActivityCapture;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.AppPermissionPresenter;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.XmlDataAdapter;
import com.vgtech.vancloud.ui.group.OrganizationSelectedActivity;
import com.vgtech.vancloud.ui.group.OrganizationSelectedListener;
import com.vgtech.vancloud.ui.module.contact.InvitationColleagueActivity;
import com.vgtech.vancloud.ui.module.contact.StaffApplysActivity;
import com.vgtech.vancloud.ui.register.ui.AddNewStaff;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vancloud.utils.XMLResParser;


import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vic on 2016/9/20.
 */
public class QuickMenuActionDialog implements HttpListener<String> {
    public static final int REQUEST_SCANBAR = 11;
    public static final int REQUEST_USERSELECT = 12;
    private BaseActivity mActivity;

    public QuickMenuActionDialog(BaseActivity activity) {
        mActivity = activity;
    }

    private PopupWindow popupWindow;

    public void showPop(View v) {
        if (popupWindow == null) {
            XMLResParser parser = new XMLResParser(mActivity);
            XMLResParser.RootData rootData = parser
                    .parser(R.xml.menu_more);
            XMLResParser.MenuItem[] items = rootData
                    .getChildren(
                            XMLResParser.MenuItem.class,
                            true);
            XmlDataAdapter menuAdapter = new XmlDataAdapter<>(mActivity);
            menuAdapter.add(items);
            //如没有添加员工权限 则移除
            if (!AppPermissionPresenter.hasPermission(mActivity, AppPermission.Type.meeting, AppPermission.Meeting.call.toString())) {
                menuAdapter.remove(2);
            }
            if (TenantPresenter.isVanTop(mActivity)) {//vantop用户没有 邀请同事、添加员工、员工申请管理
                menuAdapter.remove(3);
            }
            if (!AppPermissionPresenter.hasPermission(mActivity, AppPermission.Type.settings, AppPermission.Setting.employeeAdd.toString())
                    || TenantPresenter.isVanTop(mActivity)) {
                menuAdapter.remove(menuAdapter.getCount() - 2);
            }
            if (!AppPermissionPresenter.hasPermission(mActivity, AppPermission.Type.settings, AppPermission.Setting.employeeInvite.toString())
                    || TenantPresenter.isVanTop(mActivity)) {
                menuAdapter.remove(menuAdapter.getCount() - 1);
            }
//            if (Constants.DEBUG) {
//                XMLResParser.MenuItem menuItem = new XMLResParser.MenuItem();
//                menuItem.setLabel(R.string.tab_vantop);
//                menuItem.setIcon(R.mipmap.icon_account);
//                menuItem.setId("vantop_login");
//                menuAdapter.add(menuItem);
//            }
            View popView = LayoutInflater.from(mActivity).inflate(R.layout.action_pop_layout, null);
            NoScrollListview listView = (NoScrollListview) popView.findViewById(R.id.listview);
            listView.setAdapter(menuAdapter);
            listView.setItemClick(true);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    popupWindow.dismiss();
                    XMLResParser.MenuItem menuItem = (XMLResParser.MenuItem) parent.getItemAtPosition(position);
                    switch (menuItem.getId()) {
                        case "action_menu_scanbar":
                            templatesAction();
                            break;
                        case "action_menu_groupchat":
                            Intent intent = new Intent(mActivity, OrganizationSelectedActivity.class);
                            intent.putExtra(OrganizationSelectedListener.SELECT_MODE, OrganizationSelectedListener.SELECT_MULTI);
                            mActivity.startActivityForResult(intent, REQUEST_USERSELECT);
                            break;
                        case "action_menu_create_meeting":
//                            mActivity.startActivity(new Intent(mActivity, ChooseMeetingRoomActivity.class));
                            break;
                        case "action_menu_invite_user":
                            mActivity.startActivity(new Intent(mActivity, InvitationColleagueActivity.class));
                            break;
                        case "action_menu_add_user":
                            mActivity.startActivity(new Intent(mActivity, AddNewStaff.class));
                            //  TODO add user
                            break;
                        case "action_menu_usermanager":
                            mActivity.startActivity(new Intent(mActivity, StaffApplysActivity.class));
                            // TODO user manager
                            break;
                    }
                }
            });
            popupWindow = new PopupWindow(popView, Utils.convertDipOrPx(mActivity, 210),
                    ViewGroup.LayoutParams.WRAP_CONTENT);// 创建一个PopuWidow对象
            popupWindow.setFocusable(true);// 使其聚集
            popupWindow.setOutsideTouchable(true);// 设置允许在外点击消失
            popupWindow.setBackgroundDrawable(mActivity.getResources().getDrawable(
                    R.drawable.abc_popup_background_mtrl_mult));// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
            popupWindow.update();
        }
        popupWindow.showAsDropDown(v, 0 - Utils.convertDipOrPx(mActivity, 10), 0 - Utils.convertDipOrPx(mActivity, 8));

    }

    private void templatesAction() {
        mActivity.showLoadingDialog(mActivity, "");
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(mActivity));
        params.put("tenant_id", PrfUtils.getTenantId(mActivity));
        params.put("template_flag", "tenant_phone_login");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(mActivity, URLAddr.URL_TENANT_PHONE_LOGIN), params, mActivity);
        mActivity.getAppliction().getNetworkManager().load(1, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mActivity.dismisLoadingDialog();
        boolean mSafe = ActivityUtils.prehandleNetworkData(mActivity, this, callbackId, path, rootData, true);
        if (!mSafe) {
            return;
        }
        switch (callbackId) {
            case 1:
                try {
                    String data = rootData.getJson().getString("data");
                    Intent intent_scanning = new Intent(mActivity, MipcaActivityCapture.class);
                    intent_scanning.putExtra("templates_url", data);
                    intent_scanning.putExtra("style", "company");
                    mActivity.startActivityForResult(intent_scanning, REQUEST_SCANBAR);
                } catch (JSONException e) {
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
