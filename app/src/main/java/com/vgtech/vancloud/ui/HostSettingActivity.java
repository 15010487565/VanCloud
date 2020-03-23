package com.vgtech.vancloud.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.vgtech.vancloud.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.Option;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.PrfUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangshaofang on 2015/11/6.
 */
public class HostSettingActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRightTv(getString(R.string.ok));
        findViewById(R.id.tv_host).setOnClickListener(this);
        TextView hostTv = (TextView) findViewById(R.id.et_host);
        TextView portTv = (TextView) findViewById(R.id.et_port);
        RadioGroup rgScheme = (RadioGroup) findViewById(R.id.rg_scheme);
        rgScheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_http) {
                    PrfUtils.savePrfparams(HostSettingActivity.this, "scheme", "http");
                } else if (checkedId == R.id.rb_https) {
                    PrfUtils.savePrfparams(HostSettingActivity.this, "scheme", "https");
                }
            }
        });
        RadioButton rb_http = (RadioButton) findViewById(R.id.rb_http);
        RadioButton rb_https = (RadioButton) findViewById(R.id.rb_https);
        String host = PrfUtils.getPrfparams(this, "host");
        String port = PrfUtils.getPrfparams(this, "port");
        String scheme = PrfUtils.getPrfparams(this, "scheme",URLAddr.SCHEME);
        if ("https".equals(scheme)) {
            rb_https.setChecked(true);
        } else {
            rb_http.setChecked(true);
        }
        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(port)) {
            host = URLAddr.IP;
            port = URLAddr.PORT;
        }
        hostTv.setText(host);
        portTv.setText(port);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_host:
                ActionSheetDialog actionSheetDialog = new ActionSheetDialog(this)
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true);

                final List<Option> options = new ArrayList<>();
                options.add(new Option("app.vancloud.com", "80","测试服"));
                options.add(new Option("uat.vancloud.com", "80","uat"));
                options.add(new Option("app.vgsaas.com", "80","正式服"));
                for (Option option : options) {
                    actionSheetDialog.addSheetItem(option.id + ":" + option.name+"("+option.type+")", ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    Option o = options.get(which);
                                    TextView hostTv = (TextView) findViewById(R.id.et_host);
                                    TextView portTv = (TextView) findViewById(R.id.et_port);
                                    hostTv.setText(o.id);
                                    portTv.setText(o.name);
                                }
                            });
                }
                actionSheetDialog.show();
                break;
            case R.id.tv_right:
                TextView hostTv = (TextView) findViewById(R.id.et_host);
                TextView portTv = (TextView) findViewById(R.id.et_port);
                String host = hostTv.getText().toString();
                String port = portTv.getText().toString();
                PrfUtils.savePrfparams(this, "host", host);
                PrfUtils.savePrfparams(this, "port", port);
                finish();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.setting_host;
    }
}
