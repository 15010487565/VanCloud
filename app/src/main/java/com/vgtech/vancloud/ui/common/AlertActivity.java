package com.vgtech.vancloud.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vgtech.vancloud.R;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.vancloud.service.SubmitService;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.common.publish.module.Pschedule;

import org.json.JSONObject;

/**
 * Created by zhangshaofang on 2015/10/19.
 */
public class AlertActivity extends BaseActivity {
    @Override
    protected int getContentView() {
        return R.layout.tip_alertdialog;
    }

    public PublishTask publishTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setFinishOnTouchOutside(false);
        super.onCreate(savedInstanceState);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        String publishId = getIntent().getStringExtra("publishId");
        String msg = getIntent().getStringExtra("msg");
        publishTask = PublishTask.query(this, publishId);


        String tipTip = "";
        String ok = "";
        tipTip = getString(R.string.edit_department_title);
        ok = getString(R.string.yes);
        try {
            findViewById(R.id.et_msg).setVisibility(View.GONE);
            TextView titleTv = (TextView) findViewById(R.id.txt_title);
            titleTv.setText(tipTip);
            TextView msgTv = (TextView) findViewById(R.id.txt_msg);
            msgTv.setText(EmojiFragment.getEmojiContent(this,msgTv.getTextSize(),msg));
            TextView btn_neg = (TextView) findViewById(R.id.btn_neg);
            btn_neg.setText(R.string.no);
            TextView btn_pos = (TextView) findViewById(R.id.btn_pos);
            btn_pos.setText(ok);
            btn_neg.setOnClickListener(this);
            btn_pos.setOnClickListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_neg:
                finish();
                break;
            case R.id.btn_pos:
                try {
                    Pschedule mTask = JsonDataFactory.getData(Pschedule.class, new JSONObject(publishTask.content));
                    mTask.isrepeat = 2;
                    publishTask.content = new Gson().toJson(mTask);
                    Intent intent = new Intent(this, SubmitService.class);
                    intent.putExtra("publishTask", publishTask);
                    startService(intent);
                    finish();
                } catch (Exception e) {

                }

                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

}
