package com.vgtech.common.view.widget;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

/**
 * Created by zhangshaofang on 2015/11/19.
 */
public class VanTextWatcher implements TextWatcher {

    public View mDeleteView;

    public VanTextWatcher(final EditText editText, View deleteView) {

        mDeleteView = deleteView;
        mDeleteView.setVisibility(View.GONE);
        mDeleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mDeleteView.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
    }
}