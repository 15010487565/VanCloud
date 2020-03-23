package com.vgtech.vancloud.ui.module.payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

/**
 * Created by Duke on 2016/4/15.
 */
public class PayResultActivity extends BaseActivity {

    private static final String INVESTIGATE = "investigate";
    private static final String RECRUIT = "recruit";
    private static final String MEETING = "meeting";

    private ImageView resultImageView;
    private TextView resultTextView;
    private TextView infoTextView;
    private Button enterInfoButton;
    private Button backToPayButton;

    private String orderType;//investigate背景调查，recruit购买简历，meeting视频会议

    private boolean payResult;//支付是否成功

    private Button closeButton;

    @Override
    protected int getContentView() {
        return R.layout.pay_result_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.pay_result));
        initView();

        Intent intent = getIntent();
        orderType = intent.getStringExtra("ordertype");
        payResult = intent.getBooleanExtra("payresult", false);
        if (payResult) {
            setResultSuccess();
        } else {
            setResultFail();
        }

    }

    public void initView() {

        resultImageView = (ImageView) findViewById(R.id.result_img);
        resultTextView = (TextView) findViewById(R.id.result_text);
        infoTextView = (TextView) findViewById(R.id.info_text);
        enterInfoButton = (Button) findViewById(R.id.enter_info);
        backToPayButton = (Button) findViewById(R.id.back_pay);
        closeButton = (Button) findViewById(R.id.close_btn);

        findViewById(R.id.btn_back).setVisibility(View.GONE);

        enterInfoButton.setOnClickListener(this);
        backToPayButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
    }

    /**
     * 刷新支付成功界面
     */
    public void setResultSuccess() {

        resultImageView.setImageResource(R.mipmap.payment_success);
        resultTextView.setText(getString(R.string.pay_succuss));
        resultTextView.setTextColor(getResources().getColorStateList(R.color.bg_title));

        if (getString(R.string.lable_investigate).equals(orderType)) {
            infoTextView.setText(getString(R.string.pay_investigate_info));
        } else if (getString(R.string.lable_vidio_metting).equals(orderType)) {
            infoTextView.setText(getString(R.string.pay_meeting_info));
        } else {
            infoTextView.setText(getString(R.string.pay_recruit_info));
        }
        enterInfoButton.setText(getString(R.string.open_order));
        closeButton.setText(getString(R.string.vancloud_close));
        closeButton.setVisibility(View.VISIBLE);
        infoTextView.setVisibility(View.VISIBLE);
        backToPayButton.setVisibility(View.GONE);
    }

    /**
     * 刷新支付失败界面
     */
    public void setResultFail() {

        resultImageView.setImageResource(R.mipmap.payment_failure);
        resultTextView.setText(getString(R.string.pay_faill));
        resultTextView.setTextColor(getResources().getColorStateList(R.color.pay_failure_color));
        enterInfoButton.setText(getString(R.string.open_order));
        infoTextView.setText(getString(R.string.pay_fail_info));
        infoTextView.setVisibility(View.VISIBLE);
        backToPayButton.setVisibility(View.VISIBLE);
        closeButton.setVisibility(View.GONE);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.back_pay:
                finish();
                break;
            case R.id.enter_info:
                setResult(10);
                finish();
                break;
            case R.id.close_btn:
                setResult(11);
                finish();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(11);
        finish();
    }
}
