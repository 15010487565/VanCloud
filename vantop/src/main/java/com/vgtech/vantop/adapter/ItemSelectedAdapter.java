package com.vgtech.vantop.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.ItemSelectMoudle;

import java.util.ArrayList;
import java.util.List;

/**
 * 条目选择activity 适配器
 * Created by shilec on 2016/9/20.
 */
public class ItemSelectedAdapter extends AbsViewAdapter<ItemSelectMoudle> {

    public static final int SELECTED_MODE_IMG = 0X001;//右侧对勾选中
    public static final int SELECTED_MODE_RADIOBTN = 0X002;//左侧radio选中
    public static final int CHECK_MODE_SINGLE = 0X003;//单选
    public static final int CHECK_MODE_MULTI = 0X004;//多选
    private int mMode = SELECTED_MODE_IMG;
    private int mCheckMode = CHECK_MODE_SINGLE;
    public void setMode(int mode) {
        mMode = mode;
    }
    public void setCheckMode(int checkMode) {
        mCheckMode = checkMode;
    }

    public ItemSelectedAdapter(Context context, List<ItemSelectMoudle> datas) {
        super(context, datas);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        Holder h = new Holder(itemView);
        h.tvLabel = (TextView) itemView.findViewById(R.id.tv_label);
        h.ivSelected = (ImageView) itemView.findViewById(R.id.iv_selected);
        h.ivSeleted1 = (ImageView) itemView.findViewById(R.id.iv_select1);
        return h;
    }

    @Override
    protected void onBindData(ViewHolder holder, final int posistion) {

        Holder h = (Holder) holder;
        h.tvLabel.setText(mDatas.get(posistion).value);

        if (mMode == SELECTED_MODE_IMG) {
            if (mDatas.get(posistion).isSelected) {
                h.ivSelected.setVisibility(View.VISIBLE);
            } else {
                h.ivSelected.setVisibility(View.GONE);
            }
        } else if (mMode == SELECTED_MODE_RADIOBTN) {
            h.ivSeleted1.setVisibility(View.VISIBLE);
            if (mDatas.get(posistion).isSelected) {
                h.ivSeleted1.setSelected(true);
            } else {
                h.ivSeleted1.setSelected(false);
            }
            // h.ivSeleted1.setPressed(false);
            // h.ivSeleted1.setClickable(false);
            //h.ivSeleted1.setEnabled(false);
        }
    }

    public void setSelected(int index) {

        if(mCheckMode == CHECK_MODE_SINGLE) {
            resetSelected();
            mDatas.get(index).isSelected = true;
        } else {
            if(mDatas.get(index).isSelected) {
                mDatas.get(index).isSelected = false;
            } else {
                mDatas.get(index).isSelected = true;
            }
        }
        notifyDataSetChanged();
    }

    public List<Integer> getSelected() {
        List<Integer> sels = new ArrayList<>();
        for(int i = 0; i < mDatas.size(); i++) {
            if(mDatas.get(i).isSelected) {
                sels.add(i);
            }
        }
        return sels;
    }

    private void resetSelected() {
        for(int i = 0 ; i < mDatas.size(); i++) {
            mDatas.get(i).isSelected = false;
        }
    }

    @Override
    protected int onInflateItemView() {
        return R.layout.itemselect_list_item;
    }

    private final class Holder extends ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }

        TextView tvLabel;
        ImageView ivSelected;
        ImageView ivSeleted1;
    }
}
