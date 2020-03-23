package com.vgtech.vantop.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.VantopUserInfoFieldsData;

import java.util.List;

/**
 * 个人信息适配器
 * Created by shilec on 2016/9/18.
 */
public class UserInfoAdapter extends AbsViewAdapter<VantopUserInfoFieldsData> {

    public int fixedSize = 0;
    public boolean isSelf;
    public int bgColor,lineColor;
    public UserInfoAdapter(Context context, List<VantopUserInfoFieldsData> datas) {
        super(context, datas);
        bgColor = mContext.getResources().getColor(R.color.banckground_color);
        lineColor = mContext.getResources().getColor(R.color.line_color);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        Holder h = new Holder(itemView);
        h.tvLabel = (TextView) itemView.findViewById(R.id.tv_label);
        h.tvValue = (TextView) itemView.findViewById(R.id.tv_value);
        h.ivMenu = (ImageView) itemView.findViewById(R.id.iv_editvalue);
        h.vLine = itemView.findViewById(R.id.v_line);
        return h;
    }

    @Override
    protected void onBindData(ViewHolder holder, int posistion) {

        Holder h = (Holder) holder;
        h.tvValue.setText(mDatas.get(posistion).value.trim());
        h.tvLabel.setText(mDatas.get(posistion).label);
        h.ivMenu.setVisibility(mDatas.get(posistion).isEdit && !TextUtils.isEmpty(mDatas.get(posistion).type) ? View.VISIBLE : View.GONE);

        if(h.ivMenu.getVisibility() == View.VISIBLE) {
           ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) h.tvValue.getLayoutParams();
            lp.rightMargin = getDpSize(0);
            h.tvValue.setLayoutParams(lp);
        } else {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) h.tvValue.getLayoutParams();
            lp.rightMargin = getDpSize(15);
            h.tvValue.setLayoutParams(lp);
        }
        float size = 0;
        size = getDpSize(5);
        //分割线和vancloud统一
        if (posistion == 2 || (fixedSize >= 7 && posistion == 7) || posistion == fixedSize) {
            h.vLine.setVisibility(View.VISIBLE);
            h.vLine.setBackgroundColor(bgColor);
            ViewGroup.LayoutParams lp = h.vLine.getLayoutParams();
            lp.height = (int) size;
            h.vLine.setLayoutParams(lp);
        } else {
            h.vLine.setVisibility(View.GONE);
        }
    }
    private int getDpSize(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mContext.getResources().getDisplayMetrics());
    }

    @Override
    protected int onInflateItemView() {
        return R.layout.userinfo_item;
    }

    private final class Holder extends ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }

        TextView tvLabel;
        TextView tvValue;
        ImageView ivMenu;
        View vLine;
    }
}
