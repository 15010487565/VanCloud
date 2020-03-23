package com.vgtech.common.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vgtech.common.R;
import com.vgtech.common.utils.CommonUtils;


/**
 * Created by adm01 on 2016/6/3.
 */
public class InputActivity extends BaseActivity implements TextWatcher {
    private TextView mCountTv;
    private int MAX_LENGTH = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mCountTv = (TextView) findViewById(R.id.text_count);
        String title = intent.getStringExtra("title");
        String style = intent.getStringExtra("style");
        boolean edit = intent.getBooleanExtra("edit", true);
        if ("tenant".equals(style)) {
            findViewById(R.id.bg_titlebar).setBackgroundColor(ContextCompat.getColor(this,R.color.comment_blue));
        }
        setTitle(title);
        MAX_LENGTH = intent.getIntExtra("max", MAX_LENGTH);
        String desc = intent.getStringExtra("hint");
        String content = intent.getStringExtra("content");
        EditText descEt = (EditText) findViewById(R.id.et_desc);
        descEt.addTextChangedListener(this);
        descEt.setHint(desc);
        descEt.setText(content);
        TextView rightTv = initRightTv(getString(R.string.personal_save));
        if (!edit) {
            rightTv.setVisibility(View.GONE);
            descEt.setEnabled(false);
            findViewById(R.id.et_input).setVisibility(View.GONE);
            TextView tv = (TextView) findViewById(R.id.tv_text);
            tv.setText(content);
            findViewById(R.id.tv_input).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_right) {
            EditText descEt = (EditText) findViewById(R.id.et_desc);
            String content = descEt.getText().toString();
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(this, getString(R.string.personal_please_msg), Toast.LENGTH_SHORT).show();
                return;
            } else if (CommonUtils.getTextLength(content) > MAX_LENGTH) {
                Toast.makeText(this, getString(R.string.personal_max_msg), Toast.LENGTH_SHORT).show();
                return;
            }
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(descEt.getWindowToken(), 0);
            Intent intent = new Intent();
            intent.putExtra("content", content);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            super.onClick(v);
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_input;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        int length = CommonUtils.getTextLength(s.toString());
//        if (length > MAX_LENGTH) {
//            s = s.subSequence(0, MAX_LENGTH);
//            mCountTv.setText(s);
//            length = Utils.getTextLength(s.toString());
//        }
        mCountTv.setText(length + "/" + MAX_LENGTH);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
