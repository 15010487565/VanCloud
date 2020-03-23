package com.vgtech.vancloud.ui.register.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.register.country.CountryActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.common.PrfUtils;

/**
 * Created by Jackson on 2015/12/8.
 * Version : 1
 * Details : 有选择国家的按钮的Activity时，
 * 继承这个类可以直接实现选择后获取返回结果更新UI功能，
 * 切提供获取区号国家名方法
 */
public class ChooseCountryActivity extends BaseActivity {
    private static final int REQUEST_CHOOSE = 2;
    private static boolean hasChooseCountry = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
        initData();
    }

    /**
     * 将上次登陆账号的区号和国家名称取出来并显示
     */
    private void initData() {
        if (!hasChooseCountry) return;
        String areaCode = getAreaCodeFromSp();
        String countryName = getCountryNameFromSp();
        if (!TextUtils.isEmpty(areaCode))
            editText_areaCode.setText(areaCode);
        if (!TextUtils.isEmpty(countryName))
            tv_countryName.setText(countryName);
    }

    private TextView tv_countryName;
    private TextView editText_areaCode;

    private void initView() {
        tv_countryName = (TextView) findViewById(R.id.tv_chosed_country);
        editText_areaCode = (EditText) findViewById(R.id.et_country_num);
        hasChooseCountry = (tv_countryName != null || editText_areaCode != null);
    }

    private void initListener() {
        if (!hasChooseCountry) return;
        findViewById(R.id.rala_chose_country).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChooseCountryActivity.this, CountryActivity.class);
                        intent.putExtra("style", "personal");
                        startActivityForResult(intent, REQUEST_CHOOSE);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (hasChooseCountry) {
            switch (requestCode) {
                case REQUEST_CHOOSE:
                    if (resultCode == RESULT_OK) {
                        Bundle bundle = data.getExtras();
                        String countryName = bundle.getString("countryName");
                        String countryNum = bundle.getString("countryNumber");
                        editText_areaCode.setText(countryNum);
                        tv_countryName.setText(countryName + "(" + countryNum + ")");
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean isChina() {
        String areaCode = getAreaCode();
        return TextUtils.equals(areaCode, "86") || TextUtils.equals(areaCode, "+86");
    }


    public String getAreaCode() {
        String areaCode = editText_areaCode.getText().toString();
        return TextUtil.formatAreaCode(areaCode);
    }

    private String getAreaCodeFromSp() {
        SharedPreferences preferences = PrfUtils.getSharePreferences(this);
        String areaCode = preferences.getString("areaCode", "");
        return TextUtil.formatAreaCode(areaCode);
    }
    public void saveCountryNameAndAreaCode() {
        SharedPreferences preferences = PrfUtils.getSharePreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("countryName", getCountryName()).
                putString("areaCode", getAreaCode()).commit();
    }

    private String getCountryNameFromSp() {
        SharedPreferences preferences = PrfUtils.getSharePreferences(this);
        String countryName = preferences.getString("countryName", "");
        return countryName;
    }

    public String getCountryName() {
        return tv_countryName.getText().toString();
    }

    public void setAreaCode(String areaCode) {
        editText_areaCode.setText(areaCode);
    }

    public void setCountryName(String countryName) {
        tv_countryName.setText(countryName);
    }
}
