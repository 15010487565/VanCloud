package com.vgtech.vancloud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.vgtech.common.view.widget.VanTextWatcher;
import com.vgtech.vancloud.R;

/**
 * Created by adm01 on 2016/6/15.
 */
public class SingleEditActivity extends BaseActivity {
    private EditText mResumeNameEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRightTv(getString(R.string.save));
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        setTitle(title);
        String content = intent.getStringExtra("content");
        String hint = intent.getStringExtra("hint");
        boolean single = intent.getBooleanExtra("single", true);
        mResumeNameEt = (EditText) findViewById(R.id.et_name);
        if (single) {
            mResumeNameEt.setSingleLine();
        } else {
            mResumeNameEt.setMinLines(5);
        }
        mResumeNameEt.setHint(hint);
        int inputType = intent.getIntExtra("inputType",-1);
        if(inputType!=-1)
        mResumeNameEt.setInputType(inputType);
        mResumeNameEt.addTextChangedListener(new VanTextWatcher(mResumeNameEt, findViewById(R.id.et_remove)));
        mResumeNameEt.setText(content);
        mResumeNameEt.setSelection(mResumeNameEt.getText().length());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                String content = mResumeNameEt.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(this, mResumeNameEt.getHint(), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("content", content);
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_single_edit;
    }
}
