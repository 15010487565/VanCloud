package com.vgtech.vancloud.ui.module.me;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.vgtech.common.PrfUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.FindPwdStartActivity;

/**
 * Created by Duke on 2016/11/29.
 */

public class ChangePhoneNumberActivity extends BaseActivity {

    TextView textView;

    @Override
    protected int getContentView() {
        return R.layout.change_phone_number_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.change_phone));
        textView = (TextView) findViewById(R.id.phone_number);

        textView.setText(PrfUtils.getUserPhone(this));

        findViewById(R.id.btn_next).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_next:
                Intent intent = new Intent(this, FindPwdStartActivity.class);
                intent.putExtra("from", 1);
                startActivityForResult(intent, 2);

                break;

            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK) {
                    String phoneNum = data.getStringExtra("newphone");
                    if (!TextUtils.isEmpty(phoneNum)) {
                        textView.setText(PrfUtils.getUserPhone(this));
                        PrfUtils.setUserPhone(this, phoneNum);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
}
