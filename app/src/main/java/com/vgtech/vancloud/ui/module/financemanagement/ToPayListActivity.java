package com.vgtech.vancloud.ui.module.financemanagement;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

/**
 * Created by Duke on 2015/12/29.
 */
public class ToPayListActivity extends BaseActivity {

    @Override
    protected int getContentView() {
        return R.layout.topay_list_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.finance_order_pay_list));

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.content_view,  OrderManagementFragment.create("pending", "", 0));
        transaction.commit();

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            default:
                super.onClick(v);
                break;
        }
    }
}
