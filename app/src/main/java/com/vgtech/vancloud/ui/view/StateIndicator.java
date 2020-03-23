package com.vgtech.vancloud.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.vgtech.common.api.OrderType;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.OrderStatus;
import com.vgtech.vancloud.utils.Utils;

import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class StateIndicator extends RadioGroup implements RadioGroup.OnCheckedChangeListener {

    private final static int DEFAULT_COUNT = 5;

    public StateIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnCheckedChangeListener(this);
    }

    private List<OrderStatus> mItems;
    private StateSelectListener mStateSelectListener;
    private OrderType orderType;

    public void init(OrderType orderType, List<OrderStatus> tabs, StateSelectListener listener) {
        mItems = tabs;
        this.orderType = orderType;
        if (getChildCount() > 0) {
            removeAllViews();
            clearCheck();
        }
        mStateSelectListener = listener;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        int width = getResources().getDisplayMetrics().widthPixels / DEFAULT_COUNT;
        for (int i = 0; i < tabs.size(); i++) {
            OrderStatus os = tabs.get(i);
            RadioButton itemView = (RadioButton) inflater.inflate(R.layout.tab_item, null);
            itemView.setText(os.value);
            itemView.setId(i);
            LayoutParams layoutParams = new LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            layoutParams.setMargins(Utils.dp2px(Utils.getContext(), 15), 0, Utils.dp2px(Utils.getContext(), 15), 0);
            addView(itemView, layoutParams);
        }
        if (getChildCount() > 0)
            setSelect(getChildAt(0).getId());
//          check(getChildAt(0).getId());
    }

    public void setSelect(int index) {
        RadioButton itemView = (RadioButton) getChildAt(index);
        if (!itemView.isChecked())
            itemView.setChecked(true);
    }

    public interface StateSelectListener {
        void onSelect(OrderType orderType, OrderStatus os, int index);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == -1)
            return;
        OrderStatus orderStatus = mItems.get(checkedId);
        mStateSelectListener.onSelect(orderType, orderStatus, checkedId);
    }
}
