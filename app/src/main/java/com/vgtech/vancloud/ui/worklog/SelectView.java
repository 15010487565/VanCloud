package com.vgtech.vancloud.ui.worklog;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vgtech.vancloud.R;

/**
 * Data:  2018/7/5
 * Auther: 陈占洋
 * Description:
 */

public class SelectView extends FrameLayout {

    private String mSelectName;
    private String mSelectValue;
    private int mSelectNameMarginLeft;
    private int mSelectValueMarginRight;
    private int mSelectTextColor;
    private float mSelectTextSize;
    private View mRootView;
    private TextView mSelectNameView;
    private TextView mSelectValueView;

    public SelectView(Context context) {
        this(context, null);
    }

    public SelectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SelectView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SelectView);
        mSelectName = ta.getString(R.styleable.SelectView_selectName);
        mSelectValue = ta.getString(R.styleable.SelectView_selectValue);
        mSelectNameMarginLeft = ta.getDimensionPixelSize(R.styleable.SelectView_selectNameMarginLeft, 0);
        mSelectValueMarginRight = ta.getDimensionPixelSize(R.styleable.SelectView_selectValueMarginRight, 0);
        mSelectTextColor = ta.getColor(R.styleable.SelectView_selectTextColor, 0x000000);
        mSelectTextSize = ta.getDimension(R.styleable.SelectView_selectTextSize, 0.0f);
        ta.recycle();

        mRootView = inflate(this.getContext(), R.layout.select_item, this);
        mSelectNameView = (TextView) mRootView.findViewById(R.id.select_name);
        mSelectValueView = (TextView) mRootView.findViewById(R.id.select_value);

        if (!TextUtils.isEmpty(mSelectName)) {
            mSelectNameView.setText(mSelectName);
        }
        if (!TextUtils.isEmpty(mSelectValue)) {
            mSelectValueView.setText(mSelectValue);
        }
        RelativeLayout.LayoutParams selectNameLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        selectNameLP.addRule(RelativeLayout.CENTER_VERTICAL);
        selectNameLP.leftMargin = mSelectNameMarginLeft;
        mSelectNameView.setLayoutParams(selectNameLP);

        RelativeLayout.LayoutParams selectValueLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        selectValueLP.addRule(RelativeLayout.CENTER_VERTICAL);
        selectValueLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        selectValueLP.rightMargin = mSelectValueMarginRight;
        mSelectValueView.setLayoutParams(selectValueLP);

        mSelectNameView.setTextColor(mSelectTextColor);
        mSelectValueView.setTextColor(mSelectTextColor);

        mSelectNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSelectTextSize);
        mSelectValueView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSelectTextSize);
    }

//    public void setSelectName(@NonNull String value) {
//        mSelectNameView.setDettailText(value);
//    }
//
//    public void setSelectName(int resId) {
//        String value = this.getContext().getString(resId);
//        setSelectName(value);
//    }

    public void setSelectValue(@NonNull String value) {
        mSelectValueView.setText(value);
    }

    public void setSelectValue(int resId) {
        String value = this.getContext().getString(resId);
        setSelectValue(value);
    }

    public String getSelectValue() {
        return mSelectValueView.getText().toString();
    }

}
