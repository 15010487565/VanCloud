package com.vgtech.vantop.ui.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.vgtech.common.utils.wheel.WheelAdapter;
import com.vgtech.common.utils.wheel.WheelView;
import com.vgtech.vantop.R;

import java.util.List;


/**
 * 条目选择底部弹出框
 * Created by shilec on 2016/9/12.
 */
public class SelectWindow implements View.OnClickListener {

    private final int DEFAULT_VISIABLE_COUNT = 5;
    private final int DEFAULT_TEXTSIZE_SP = 16;
    private PopupWindow mWindow;
    private Context mContext;
    private List<String> mDatas1;
    private List<String> mDatas2;
    private WheelView mWheel1;
    private WheelView mWheel2;
    private TextView mTvConfirm;
    private TextView mTvCancel;

    private OnConfirmListenner mConfirmLisnner;

    public void setOnConfirmLisenner(OnConfirmListenner l) {
        mConfirmLisnner = l;
    }


    public SelectWindow(Context context, List<String> datas1, List<String> datas2) {
        mContext = context;
        mDatas1 = datas1;
        mDatas2 = datas2;
        mWindow = new PopupWindow(mContext);
        initWindow();
    }

    public SelectWindow(Context context, List<String> mDatas1) {
        this(context, mDatas1, null);
    }

    private void initWindow() {

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        View v = LayoutInflater.from(mContext).inflate(R.layout.popupwindow_doubleselectpicker, null);
        v.setLayoutParams(lp);
        mWindow.setContentView(v);
        mWindow.setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(android.R.color.transparent)));
        mWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mWindow.setAnimationStyle(R.style.popwin_anim_style);
        mWindow.setOutsideTouchable(true);
        mWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                showShadow(false);
            }
        });
        mTvConfirm = (TextView) v.findViewById(R.id.tv_confirm);
        mTvCancel = (TextView) v.findViewById(R.id.tv_cancel);
        mTvCancel.setOnClickListener(this);
        mTvConfirm.setOnClickListener(this);

        mWheel1 = (WheelView) v.findViewById(R.id.wheel_1);
        mWheel2 = (WheelView) v.findViewById(R.id.wheel_2);

        int textSize = sp2px(mContext, 16);
        if (mDatas1 != null) {
            mWheel1.setAdapter(new MyWheelAdapter(mDatas1));
            mWheel1.setVisibleItems(DEFAULT_VISIABLE_COUNT);
            mWheel1.TEXT_SIZE = textSize;
            mWheel1.setCurrentItem(0);
        } else {
            mWheel1.setVisibility(View.GONE);
        }
        if (mDatas2 != null) {
            mWheel2.setAdapter(new MyWheelAdapter(mDatas2));
            mWheel2.setVisibleItems(DEFAULT_VISIABLE_COUNT);
            mWheel2.TEXT_SIZE = textSize;
            mWheel2.setCurrentItem(0);
        } else {
            mWheel2.setVisibility(View.GONE);
        }
    }

    public PopupWindow getWindow() {
        return mWindow;
    }

    private void showShadow(boolean isShow) {
        ((Activity)mContext).getWindow().getDecorView().setAlpha(isShow ? 0.6f : 1f);
    }


    public void show(View parent) {
        //showShadow(true);
        mWindow.showAtLocation(parent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        //mWindow.showAsDropDown(parent);
        if (mWheel1 != null)
            mWheel1.invalidate();
    }

    public void setCurrentItem(int index) {
        mWheel1.setCurrentItem(index);
        mWheel2.setCurrentItem(index);
    }

    public void setVisisbleItems(int index) {
        mWheel1.setVisibleItems(index);
        mWheel2.setVisibleItems(index);
    }

    public void setCurrentItem1(int index) {
        mWheel1.setCurrentItem(index);
    }

    public void setCurrentItem2(int index) {
        mWheel2.setCurrentItem(index);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public int getSelectIndex1() {
        if (mWheel1 != null)
            return mWheel1.getCurrentItem();
        else
            return 0;
    }

    public int getSelectIndex2() {
        if(mWheel2 != null)
            return mWheel2.getCurrentItem();
        else
            return 0;
    }

    @Override
    public void onClick(View view) {
        mWindow.dismiss();
        if (view == mTvCancel) {

        }

        if (view == mTvConfirm) {
            if (mConfirmLisnner != null) {
                String item1 = null;
                String item2 = null;
                if (mDatas1 != null) {
                    item1 = mDatas1.get(mWheel1.getCurrentItem());
                }
                if (mDatas2 != null) {
                    item2 = mDatas2.get(mWheel2.getCurrentItem());
                }
                mConfirmLisnner.onConfirm(item1, item2,mWheel1.getCurrentItem(),mWheel2.getCurrentItem());
            }
        }
    }

    public  interface OnConfirmListenner {
         void onConfirm(String item, String item1,int index1,int index2);
    }

    public void dismiss() {
        mWindow.dismiss();
    }
}

final class MyWheelAdapter implements WheelAdapter {
    private List<String> mDatas;

    public MyWheelAdapter(List<String> datas) {
        mDatas = datas;
    }

    @Override
    public int getItemsCount() {
        return mDatas.size();
    }

    @Override
    public String getItem(int index) {
        return mDatas.get(index);
    }

    @Override
    public int getMaximumLength() {
        return 20;
    }
}
