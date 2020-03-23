package com.vgtech.vancloud.ui.web;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.vgtech.common.URLAddr;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.vancloud.R;

/**
 * Data:  2019/6/21
 * Auther: xcd
 * Description:
 */
public class AgreementDialogFragment extends DialogFragment implements View.OnClickListener {

    private TextView tvLoginAgreement;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AgreementDialog);

        View view = inflater.inflate(R.layout.dialog_fragment_agreement, container);
        tvLoginAgreement = view.findViewById(R.id.tv_LoginAgreement);
        String agreementStr = tvLoginAgreement.getText().toString();
        Log.e("TAG_协议", agreementStr);
        SpannableString spannableString = new SpannableString(agreementStr);

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {

                Intent intent = new Intent(getActivity(), WebActivity.class);
                intent.putExtra("title", getString(R.string.privacy_clause));
                String url = ApiUtils.generatorUrl(getActivity(), URLAddr.URL_PRIVACY_POLICY);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(getActivity(), R.color.comment_blue));//设置颜色
//                ds.setUnderlineText(false);//去掉下划线
            }

        }, 4, agreementStr.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);


        tvLoginAgreement.setText(spannableString);
        tvLoginAgreement.setMovementMethod(LinkMovementMethod.getInstance());//不设置 没有点击事件
        tvLoginAgreement.setHighlightColor(Color.TRANSPARENT);

        TextView tv_ColseAgreement = view.findViewById(R.id.tv_ColseAgreement);
        tv_ColseAgreement.setOnClickListener(this);
        TextView tvExitAgreement = view.findViewById(R.id.tv_ExitAgreement);
        tvExitAgreement.setOnClickListener(this);


        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.5f;
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(windowParams);
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            window.setLayout((int) (dm.widthPixels * 0.75), (int) (dm.heightPixels * 0.63));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_ColseAgreement:
                dismiss();
                closeDialogFragment.close();
                break;
            case R.id.tv_ExitAgreement:
                dismiss();
                getActivity().finish();
                break;
        }
    }
    public CloseDialogFragment closeDialogFragment;
    public void setCloseDialogFragment(CloseDialogFragment closeDialogFragment){
        this.closeDialogFragment = closeDialogFragment;
    }
    public interface CloseDialogFragment {
        void close();
    }
}
