package com.vgtech.vancloud.ui.module.contact;

import android.os.Bundle;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

/**
 * Created by brook on 2016/10/11.
 */

public class WebAddActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.pc_manage));
    }

    @Override
    protected int getContentView() {
        return R.layout.web_add_activity;
    }
}
