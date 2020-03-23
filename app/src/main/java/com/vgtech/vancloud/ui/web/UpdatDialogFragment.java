package com.vgtech.vancloud.ui.web;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.vgtech.common.PrfUtils;
import com.vgtech.vancloud.R;


/**
 * Data:  2019/6/21
 * Auther: xcd
 * Description:
 */
public class UpdatDialogFragment extends DialogFragment implements View.OnClickListener{

    TextView tv_content;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.dialog_fragment_update, container);
            TextView tv_close = view.findViewById(R.id.tv_close);
            tv_close.setOnClickListener(this);

           tv_content = view.findViewById(R.id.tv_content);
            tv_content.setMovementMethod(ScrollingMovementMethod.getInstance());


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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_close:
                PrfUtils.setUpdateTipFlag(getActivity(), true);
                dismiss();
                break;
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowParams = window.getAttributes();
//        windowParams.dimAmount = 0.5f;
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        window.setAttributes(windowParams);
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
//            double v = dm.widthPixels * 0.75* 0.75;
//            tv_content.setMinimumHeight((int) v);
            window.setLayout((int) (dm.widthPixels * 0.75), -2);
        }
    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        BitmapDrawable b= imageView.getDrawable() instanceof BitmapDrawable ? ((BitmapDrawable) imageView.getDrawable()) : null;
//        if (b != null) {
//            b.getBitmap().recycle();
//            imageView.setImageDrawable(null);
//        }
//    }
}
