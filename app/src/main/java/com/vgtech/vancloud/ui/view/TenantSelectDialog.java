package com.vgtech.vancloud.ui.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupWindow;

import com.vgtech.common.api.Tenant;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.ApiDataAdapter;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.vancloud.utils.Utils;

import java.util.List;

/**
 * Created by vic on 2016/9/20.
 */
public class TenantSelectDialog {
    public static final int REQUEST_SCANBAR = 11;
    public static final int REQUEST_USERSELECT = 12;
    private BaseActivity mActivity;
    private TenantSelectListener mTenantSelectListener;
    private List<Tenant> mTenants;

    public TenantSelectDialog(BaseActivity activity, TenantSelectListener listener) {
        mActivity = activity;
        mTenantSelectListener = listener;
        mTenants = TenantPresenter.getTenant(activity);
    }

    private PopupWindow popupWindow;

    public void showPop(View v) {
        if (popupWindow == null) {
            ApiDataAdapter<Tenant> menuAdapter = new ApiDataAdapter<>(mActivity);
            menuAdapter.add(mTenants);
            View popView = LayoutInflater.from(mActivity).inflate(R.layout.action_pop_layout, null);
            NoScrollListview listView = (NoScrollListview) popView.findViewById(R.id.listview);
            listView.setAdapter(menuAdapter);
            listView.setItemClick(true);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    popupWindow.dismiss();
                    Tenant tenant = (Tenant) parent.getItemAtPosition(position);
                    mTenantSelectListener.onTenantSelected(tenant);
                }
            });
            popupWindow = new PopupWindow(popView, Utils.convertDipOrPx(mActivity, 250),
                    ViewGroup.LayoutParams.WRAP_CONTENT);// 创建一个PopuWidow对象
            popupWindow.setFocusable(true);// 使其聚集
            popupWindow.setOutsideTouchable(true);// 设置允许在外点击消失
            popupWindow.setBackgroundDrawable(mActivity.getResources().getDrawable(
                    R.drawable.abc_popup_background_mtrl_mult));// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
            popupWindow.update();
        }
        popupWindow.showAsDropDown(v, 0 - Utils.convertDipOrPx(mActivity, 10), 0 - Utils.convertDipOrPx(mActivity, 8));

    }
}
