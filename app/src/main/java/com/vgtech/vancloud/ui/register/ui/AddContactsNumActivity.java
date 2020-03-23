package com.vgtech.vancloud.ui.register.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

/**
 * Created by Jackson on 2015/12/9.
 * Version : 1
 * Details :
 */
public class AddContactsNumActivity extends ChooseCountryActivity {

    private TextView mTv_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setTitle(R.string.please_input_phone);
    }

    private void initView() {
        mTv_right = initRightTv(getString(R.string.btn_finish));

        mTv_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_usertel = (EditText) findViewById(R.id.et_usertel);
                String phone = et_usertel.getText().toString();
                if (!TextUtil.isAvailablePhone(AddContactsNumActivity.this, phone, isChina()))return;
                phone =getAreaCode()+"+"+phone;
                Intent data = new Intent();
                data.putExtra("phone", phone);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_add_contacts_num;
    }
}
