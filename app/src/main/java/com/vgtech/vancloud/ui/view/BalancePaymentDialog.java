package com.vgtech.vancloud.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vgtech.vancloud.R;

/**
 * Created by Duke on 2016/10/17.
 */

public class BalancePaymentDialog {

    private Display display;
    private Context context;
    private Dialog dialog;
    private TextView titleView;
    private Button cancelButton;
    private Button confirmButton;
    private EditText passwordView;
    private TextView balanceView;
    private TextView amountView;

    private RelativeLayout relativeLayout;
    private TextView msgView;
    private Button oneButton;


    public BalancePaymentDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
    }

    public BalancePaymentDialog builder() {
        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(
                R.layout.balance_payment_dialog_layout, null);

        titleView = (TextView) view.findViewById(R.id.txt_title);
        cancelButton = (Button) view.findViewById(R.id.btn_neg);
        confirmButton = (Button) view.findViewById(R.id.btn_pos);
        passwordView = (EditText) view.findViewById(R.id.payment_password);
        balanceView = (TextView) view.findViewById(R.id.company_balance);
        amountView = (TextView) view.findViewById(R.id.payment_amount);
        relativeLayout = (RelativeLayout) view.findViewById(R.id.order_info);
        relativeLayout.setVisibility(View.GONE);
        msgView = (TextView) view.findViewById(R.id.msg_text);
        msgView.setVisibility(View.GONE);
        oneButton = (Button) view.findViewById(R.id.one_btn);
        oneButton.setVisibility(View.GONE);


        // 定义Dialog布局和参数
        dialog = new Dialog(context, R.style.AlertDialogStyle);
//        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);

        // 调整dialog背景大小
        view.findViewById(R.id.dialog_layout).setLayoutParams(new FrameLayout.LayoutParams((int) (display
                .getWidth() * 0.85), LinearLayout.LayoutParams.WRAP_CONTENT));

        return this;
    }

    public BalancePaymentDialog setTitle(String title) {
        if ("".equals(title)) {
            titleView.setText(context.getString(R.string.lable_title));
        } else {
            titleView.setText(title);
        }
        return this;
    }

    public EditText getEditer() {

        return passwordView;
    }

    public BalancePaymentDialog setPositiveButton(String text,
                                                  final View.OnClickListener listener) {
        if ("".equals(text)) {
            confirmButton.setText(context.getString(R.string.confirm));
        } else {
            confirmButton.setText(text);
        }
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {
                    listener.onClick(v);
                } else {
                    dialog.dismiss();
                }
            }
        });
        return this;
    }

    public BalancePaymentDialog setNegativeButton(String text,
                                                  final View.OnClickListener listener) {
        if ("".equals(text)) {
            cancelButton.setText(context.getString(R.string.cancel));
        } else {
            cancelButton.setText(text);
        }
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
        return this;
    }

    public BalancePaymentDialog setOneButton(String text,
                                             final View.OnClickListener listener) {

        confirmButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        oneButton.setVisibility(View.VISIBLE);
        if ("".equals(text)) {
            oneButton.setText(context.getString(R.string.confirm));
        } else {
            oneButton.setText(text);
        }
        oneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
        return this;
    }


    public BalancePaymentDialog setInfo(String company_balance, String payment_amount) {

        relativeLayout.setVisibility(View.VISIBLE);
        balanceView.setText(context.getString(R.string.dialog_company_balance, company_balance));
        payment_amount = "<font color='#ff0000'>" + payment_amount + "</font>";
        amountView.setText(Html.fromHtml(context.getString(R.string.dialog_payment_amount, payment_amount)));

        return this;
    }

    public BalancePaymentDialog setMsg(String msg) {

        msgView.setVisibility(View.VISIBLE);
        msgView.setText(msg);
        return this;
    }


    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
