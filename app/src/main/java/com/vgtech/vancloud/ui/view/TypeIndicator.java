package com.vgtech.vancloud.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.vgtech.common.api.OrderType;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.utils.Utils;

import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class TypeIndicator extends RadioGroup implements RadioGroup.OnCheckedChangeListener{

    private final static int DEFAULT_COUNT = 5;
    public TypeIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnCheckedChangeListener(this);
    }
    private List<OrderType> mItems;
    private TypeSelectListener mStateSelectListener;
    public void init(List<OrderType> tabs, TypeSelectListener listener)
    {
        mItems = tabs;
        mStateSelectListener = listener;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        int width = getResources().getDisplayMetrics().widthPixels/DEFAULT_COUNT;
        for(int i=0;i<tabs.size();i++)
        {
            OrderType os = tabs.get(i);
            RadioButton itemView = (RadioButton) inflater.inflate(R.layout.tab_item,null);
            String name = os.order_type_name;
//            if(!TextUtils.isEmpty(name))
//            {
//                name  = name.replace("订单","");
//            }
            itemView.setText(name);
            itemView.setId(i);
            LayoutParams layoutParams = new LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            layoutParams.setMargins(Utils.dp2px(Utils.getContext(),15),0,Utils.dp2px(Utils.getContext(),15),0);
            addView(itemView, layoutParams);
        }
        if(getChildCount()>0)
            check(0);
    }
    public interface TypeSelectListener {
        void onSelect(OrderType os);
    }
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        OrderType orderStatus =   mItems.get(checkedId);
        mStateSelectListener.onSelect(orderStatus);
    }
}
