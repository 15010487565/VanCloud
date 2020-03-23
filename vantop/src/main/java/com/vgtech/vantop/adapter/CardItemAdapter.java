package com.vgtech.vantop.adapter;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.PunchCardListData;
import com.vgtech.vantop.utils.AlignedTextUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 打卡记录适配器
 * Created by shilec on 2016/9/6.
 */
public class CardItemAdapter extends AbsViewAdapter<PunchCardListData> {
    public CardItemAdapter(Context context, List<PunchCardListData> datas) {
        super(context, datas);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        Holder holder = new Holder(itemView);
        holder.line_header = holder.itemView.findViewById(R.id.line_header);
        holder.line_footer = holder.itemView.findViewById(R.id.line_footer);
        holder.line_bottom = holder.itemView.findViewById(R.id.line_bottom);
        holder.tv_time = (TextView) holder.itemView.findViewById(R.id.tv_time);
        holder.tv_type = (TextView) holder.itemView.findViewById(R.id.tv_type);
        holder.tv_remark = (TextView) holder.itemView.findViewById(R.id.tv_remark);
        holder.imagegridview = (NoScrollGridview) holder.itemView.findViewById(R.id.imagegridview);
        holder.tvLocation = (TextView) holder.itemView.findViewById(R.id.tv_location);
        holder.tvLocationContent = (TextView) holder.itemView.findViewById(R.id.tv_locationContent);
        holder.tvAddress = (TextView) holder.itemView.findViewById(R.id.tv_address);
        holder.tvRemarkLeft = (TextView) holder.itemView.findViewById(R.id.tv_remarkLeft);

        holder.llLocation = (LinearLayout) holder.itemView.findViewById(R.id.ll_location);
        holder.llAddress = (LinearLayout) holder.itemView.findViewById(R.id.ll_Address);
        holder.ll_remark = (LinearLayout) holder.itemView.findViewById(R.id.ll_remark);

        holder.line1 = (View) holder.itemView.findViewById(R.id.line1);
        holder.line2 = (View) holder.itemView.findViewById(R.id.line2);
        holder.line3 = (View) holder.itemView.findViewById(R.id.line3);
        return holder;
    }

    @Override
    protected void onBindData(ViewHolder holder, int posistion) {
        Holder h = (Holder) holder;
        h.line_header.setVisibility(posistion == 0 ? View.INVISIBLE : View.VISIBLE);
        h.line_footer.setVisibility(posistion == getCount() - 1 ? View.INVISIBLE : View.VISIBLE);
        h.line_bottom.setVisibility(posistion == getCount() - 1 ? View.INVISIBLE : View.VISIBLE);
        PunchCardListData data = mDatas.get(posistion);
        h.tv_time.setText(data.getTime());
//        h.tv_remark.setText(data.getRemark());
//        h.tv_remark.setVisibility(TextUtils.isEmpty(data.getRemark()) ? View.GONE : View.VISIBLE);
        if (data.getType() == -1) {
            h.tv_type.setText("");
        } else {
            h.tv_type.setText(data.getType());
        }

        SpannableStringBuilder retailLocation = AlignedTextUtils.justifyString("经纬度", 4);
        retailLocation.append(":");
        h.tvLocation.setText(retailLocation);

        String longitude = data.getLongitude();
        String latitude = data.getLatitude();
        if (TextUtils.isEmpty(longitude)||TextUtils.isEmpty(latitude)){
            h.llLocation.setVisibility(View.GONE);
            h.line1.setVisibility(View.GONE);
        }else {
            h.llLocation.setVisibility(View.VISIBLE);
            h.tvLocationContent.setText( (TextUtils.isEmpty(longitude) ? "0.00" : longitude)+ "," + (TextUtils.isEmpty(latitude) ? "0.00" : latitude));
            h.line1.setVisibility(View.VISIBLE);
        }

        String address = data.getAddress();
        if (TextUtils.isEmpty(address)){
            h.llAddress.setVisibility(View.GONE);
            h.line2.setVisibility(View.GONE);
        }else {
            h.llAddress.setVisibility(View.VISIBLE);
            h.tvAddress.setText(address);
            h.line2.setVisibility(View.VISIBLE);
        }

        String remark = data.getRemark();
        if (TextUtils.isEmpty(remark)){
            h.ll_remark.setVisibility(View.GONE);
            h.line3.setVisibility(View.GONE);
        }else {
            h.line3.setVisibility(View.VISIBLE);
            h.ll_remark.setVisibility(View.VISIBLE);
            h.tv_remark.setText(remark);
            SpannableStringBuilder retailRemarkLeft = AlignedTextUtils.justifyString("备注", 4);
            retailRemarkLeft.append(":");
            h.tvRemarkLeft.setText(retailRemarkLeft);
        }


        String imgs = data.getPictures();
        NoScrollGridview igView = h.imagegridview;
        if (!TextUtils.isEmpty(imgs)) {
            final List<ImageInfo> imags = new ArrayList<>();
            if (imgs.contains(",")) {
                String[] path = imgs.split(",");
                for (String p : path) {
                    p = VanTopUtils.generatorImageUrl(mContext, p);
                    ImageInfo info = new ImageInfo(p);
                    info.thumb = p;
                    info.url = p;
                    imags.add(info);
                }
            } else {
                imgs = VanTopUtils.generatorImageUrl(mContext, imgs);
                ImageInfo info = new ImageInfo(imgs);
                info.thumb = imgs;
                info.url = imgs;
                imags.add(info);
            }
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(igView, mContext,3, imags);
            igView.setAdapter(imageGridviewAdapter);
            igView.setVisibility(View.VISIBLE);
        } else {
            igView.setVisibility(View.GONE);
            if (TextUtils.isEmpty(remark)){
                h.line2.setVisibility(View.GONE);
            }else {
                h.line3.setVisibility(View.GONE);
            }

        }

    }

    @Override
    protected int onInflateItemView() {
        return R.layout.cardinfo_item;
    }


    private class Holder extends ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }

        View line_header;
        View line_footer;
        View line_bottom;
        TextView tv_time;
        TextView tv_type;
        TextView tv_remark;
        NoScrollGridview imagegridview;
        TextView tvLocation;
        TextView tvLocationContent;
        TextView tvAddress;
        TextView tvRemarkLeft;

        LinearLayout llLocation;
        LinearLayout llAddress;
        LinearLayout ll_remark;
        View line1,line2,line3;
    }
}
