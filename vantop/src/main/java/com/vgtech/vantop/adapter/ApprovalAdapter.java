package com.vgtech.vantop.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.utils.ImageCacheManager;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.Approval;
import com.vgtech.vantop.ui.userinfo.VantopUserInfoActivity;

import java.util.List;

/**
 * Created by Brook on 2016/3/7.
 */
public class ApprovalAdapter extends DataAdapter<Approval> {


    Context context;

    public ApprovalAdapter(Context context, List dataSource) {
        this.context = context;
        this.dataSource = dataSource;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.approval_item, null);
            assert convertView != null;
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final Approval al = dataSource.get(position);
        if (!TextUtils.isEmpty(al.staffName) && !"null".equals(al.staffName))
            viewHolder.approvalNameTxt.setText(al.staffName);

        String status = al.status;

        if ("0".equals(status)) {
            viewHolder.statusRemark.setText(context.getResources().getString(R.string.vantop_approving));
            viewHolder.statusRemark.setTextColor(context.getResources().getColor(R.color.txt_explain));
            viewHolder.pointImg.setImageResource(R.mipmap.approval_doing_logo);
        } else if ("1".equals(status)) {
            viewHolder.statusRemark.setText(context.getResources().getString(R.string.vantop_adopt));
            viewHolder.statusRemark.setTextColor(context.getResources().getColor(R.color.adopted_txt));
            viewHolder.pointImg.setImageResource(R.mipmap.approval_end_logo);
            viewHolder.approvalDate.setText(al.date + "  " + al.time);
        } else if ("2".equals(status)) {
            viewHolder.statusRemark.setText(context.getResources().getString(R.string.vantop_refuse));
            viewHolder.statusRemark.setTextColor(context.getResources().getColor(R.color.refused_txt));
            viewHolder.pointImg.setImageResource(R.mipmap.approval_end_logo_un);
            viewHolder.approvalDate.setText(al.date + "  " + al.time);
        }

        if ("".equals(al.remark)) {
            viewHolder.statusExplain.setText("");
        } else {
            viewHolder.statusExplain.setText("(" + al.remark + ")");
        }
        viewHolder.approvalStaffImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VantopUserInfoActivity.class);
                intent.putExtra(VantopUserInfoActivity.BUNDLE_STAFFNO, al.staffNo);
                context.startActivity(intent);
            }
        });
        ImageCacheManager.getImage(context, viewHolder.approvalStaffImg, al.staffNo);
        if (position == dataSource.size() - 1) {
            viewHolder.bottomLine.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.bottomLine.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public class ViewHolder {
        SimpleDraweeView approvalStaffImg;
        TextView approvalNameTxt;
        TextView approvalDate;
        TextView statusRemark;
        View bottomLine;
        ImageView pointImg;
        TextView statusExplain;

        public ViewHolder(final View view) {
            approvalStaffImg = (SimpleDraweeView) view.findViewById(R.id.approval_staff_img);
            approvalNameTxt = (TextView) view.findViewById(R.id.approval_name_txt);
            approvalDate = (TextView) view.findViewById(R.id.approval_date);
            statusRemark = (TextView) view.findViewById(R.id.status_remark);
            bottomLine = view.findViewById(R.id.bottom_line);
            pointImg = (ImageView) view.findViewById(R.id.iv_point);
            statusExplain = (TextView) view.findViewById(R.id.status_explain);
        }
    }
}
