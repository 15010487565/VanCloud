package com.vgtech.vancloud.ui.beidiao;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseFragment;
import com.vgtech.vancloud.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vic on 2016/10/14.
 */
public class BdStepOneFragment extends BaseFragment {
    @Override
    protected int initLayoutId() {
        return R.layout.beidiao_step_one;
    }


    private BdStepListener stepListener;

    public void setStepListener(BdStepListener stepListener) {
        this.stepListener = stepListener;
    }

    private View mView;

    @Override
    protected void initView(View view) {
        mView = view;
        mView.findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextStep();
            }
        });
//        TextView tv_name = (TextView) mView.findViewById(R.id.tv_name);
//        TextView tv_cardId = (TextView) mView.findViewById(R.id.tv_cardId);
//        TextView tv_mobile = (TextView) mView.findViewById(R.id.tv_mobile);

    }

    private void hideInputMethod() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null)
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void reset() {
        TextView tv_name = (TextView) mView.findViewById(R.id.tv_name);
        TextView tv_cardId = (TextView) mView.findViewById(R.id.tv_cardId);
        TextView tv_mobile = (TextView) mView.findViewById(R.id.tv_mobile);
        TextView tv_qq = (TextView) mView.findViewById(R.id.tv_qq);
        TextView tv_email = (TextView) mView.findViewById(R.id.tv_email);
        TextView tv_band_card = (TextView) mView.findViewById(R.id.tv_band_card);
        TextView tv_one = (TextView) mView.findViewById(R.id.tv_one);
        TextView tv_two = (TextView) mView.findViewById(R.id.tv_two);
        TextView tv_three = (TextView) mView.findViewById(R.id.tv_three);
        tv_name.setText("");
        tv_cardId.setText("");
        tv_mobile.setText("");
        tv_qq.setText("");
        tv_email.setText("");
        tv_band_card.setText("");
        tv_one.setText("");
        tv_two.setText("");
        tv_three.setText("");
    }

    public void nextStep() {
        TextView tv_name = (TextView) mView.findViewById(R.id.tv_name);
        TextView tv_cardId = (TextView) mView.findViewById(R.id.tv_cardId);
        TextView tv_mobile = (TextView) mView.findViewById(R.id.tv_mobile);
        TextView tv_qq = (TextView) mView.findViewById(R.id.tv_qq);
        TextView tv_email = (TextView) mView.findViewById(R.id.tv_email);
        TextView tv_band_card = (TextView) mView.findViewById(R.id.tv_band_card);
        TextView tv_one = (TextView) mView.findViewById(R.id.tv_one);
        TextView tv_two = (TextView) mView.findViewById(R.id.tv_two);
        TextView tv_three = (TextView) mView.findViewById(R.id.tv_three);
        String name = tv_name.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getActivity(), tv_name.getHint(), Toast.LENGTH_SHORT).show();
            return;
        } else if (name.length() > 20) {
            Toast.makeText(getActivity(), R.string.tip_name_error, Toast.LENGTH_SHORT).show();
            return;
        }
        String card = tv_cardId.getText().toString();
        if (TextUtils.isEmpty(card)) {
            Toast.makeText(getActivity(), tv_cardId.getHint(), Toast.LENGTH_SHORT).show();
            return;
        } else if (!Utils.isCard(card)) {
            Toast.makeText(getActivity(), R.string.tip_card_error, Toast.LENGTH_SHORT).show();
            return;
        }
        String mobile = tv_mobile.getText().toString();
        if (TextUtils.isEmpty(mobile)) {
            Toast.makeText(getActivity(), tv_mobile.getHint(), Toast.LENGTH_SHORT).show();
            return;
        } else if (!Utils.isPhoneNum(mobile)) {
            Toast.makeText(getActivity(), R.string.tip_mobile_error, Toast.LENGTH_SHORT).show();
            return;
        }
        String qq = tv_qq.getText().toString();
        if (!TextUtils.isEmpty(qq) && (qq.length() < 5 || qq.length() > 15)) {
            Toast.makeText(getActivity(), R.string.tip_qq_error, Toast.LENGTH_SHORT).show();
            return;
        }
        String email = tv_email.getText().toString();
        if (!TextUtils.isEmpty(email) && !Utils.isEmail(email)) {
            Toast.makeText(getActivity(), R.string.tip_email_error, Toast.LENGTH_SHORT).show();
            return;
        }
        String band = tv_band_card.getText().toString();
        int bl = band.length();
        if (!TextUtils.isEmpty(band) && !(bl == 16 || bl == 19)) {
            Toast.makeText(getActivity(), R.string.tip_bank_error, Toast.LENGTH_SHORT).show();
            return;
        }
        String one = tv_one.getText().toString();
        String two = tv_two.getText().toString();
        String three = tv_three.getText().toString();
        if (!TextUtils.isEmpty(one) || !TextUtils.isEmpty(two) || !TextUtils.isEmpty(three)) {
            if (TextUtils.isEmpty(one) || TextUtils.isEmpty(two) || TextUtils.isEmpty(three)) {
                Toast.makeText(getActivity(), R.string.tip_education_error, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        hideInputMethod();
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("idCard", card);
        params.put("mobile", mobile);
        params.put("qqNumber", qq);
        params.put("email", email);
        params.put("unionPayCard", band);
        params.put("educationCodeFir", one);
        params.put("educationCodeSec", two);
        params.put("educationCodeThi", three);
        stepListener.stepOne(params);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }
}
