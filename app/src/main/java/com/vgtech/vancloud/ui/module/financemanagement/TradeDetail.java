package com.vgtech.vancloud.ui.module.financemanagement;

import android.os.Bundle;
import android.widget.TextView;

import com.vgtech.common.api.TradeListItem;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by swj on 16/4/8.
 */
public class TradeDetail extends BaseActivity {

    private TextView tradeTime;
    private TextView tradeInstruction;
    private TextView operatingPersonnel;
    private TextView amountOfMoney;
    private TextView relevanceCode;
    private TextView remarks;

    private TradeListItem data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        setListener();
        setTitle(getString(R.string.trade_detail_title));
    }

    private void initView() {
        tradeTime = (TextView) findViewById(R.id.trade_time);
        tradeInstruction = (TextView) findViewById(R.id.trade_instruction);
        operatingPersonnel = (TextView) findViewById(R.id.operating_personnel);
        amountOfMoney = (TextView) findViewById(R.id.amount_of_money);
        relevanceCode = (TextView) findViewById(R.id.relevance_code);
        remarks = (TextView) findViewById(R.id.remarks);
    }

    private void initData() {
        data = (TradeListItem) getIntent().getSerializableExtra("data");

        if (data == null) {
            finish();
            return;
        }
        tradeInstruction.setText(String.format(getString(R.string.trade_instruction), data.discription));
        amountOfMoney.setText("￥" + data.amount);
        operatingPersonnel.setText(data.optioner);
        String time = "";

        try {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(Long.parseLong(data.time));
            time = new SimpleDateFormat(getString(R.string.trade_list_time_format)).format(c.getTime());
        } catch (Exception e) {

        }


        tradeTime.setText(time);
        relevanceCode.setText(data.order_info_id);
        remarks.setText(data.remark);
//        data.name;
    }

    private void setListener() {

    }

    @Override
    protected int getContentView() {
        return R.layout.trade_detail_layout;
    }
}
