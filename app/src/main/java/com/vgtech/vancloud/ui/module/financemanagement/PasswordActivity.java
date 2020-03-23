package com.vgtech.vancloud.ui.module.financemanagement;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.vgtech.common.PrfUtils;
import com.vgtech.common.ui.PasswordFragment;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

/**
 * 修改支付密码
 * Created by Nick on 2015/12/25.
 */
public class PasswordActivity extends BaseActivity {

    private int type;
    private int userType;

    @Override
    protected int getContentView() {
        return R.layout.edit_password_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        type = getIntent().getIntExtra("type", -1);
        userType = getIntent().getIntExtra("userType", 1);

        initPasswordView();
    }

    private void initPasswordView() {
        setTitle(getString(R.string.modify_paypassword));
        if (userType == PasswordFragment.INDIVIDUALUSER) {
            findViewById(R.id.title).setBackgroundColor(Color.parseColor("#faa41d"));
            if (type == 76) {
                setTitle(getString(R.string.init_new_password));
                TextView nameView = (TextView) findViewById(R.id.username);
                nameView.setText(String.format(getString(R.string.user_name), PrfUtils.getUserName(this)));
                nameView.setVisibility(View.VISIBLE);
            }
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_layout, PasswordFragment.create(type, userType, null));
        transaction.commit();
    }
}
