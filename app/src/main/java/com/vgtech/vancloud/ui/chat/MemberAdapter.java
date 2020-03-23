package com.vgtech.vancloud.ui.chat;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.adapter.BaseSimpleAdapter;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.models.Staff;

/**
 * Created by vic on 2017/3/14.
 */
public class MemberAdapter extends BaseSimpleAdapter<Staff> {
    public static final int ADAPTER_NORMAL = 0;
    public static final int ADAPTER_DELETE = 1;
    private int mType = ADAPTER_NORMAL;

    public MemberAdapter(Context context) {
        super(context);
    }

    @Override
    public int getItemResource(int viewType) {
        return R.layout.member_user_item;
    }

    public void setType(int type) {
        mType = type;
    }

    public int getType() {
        return mType;
    }

    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {
        SimpleDraweeView icon = holder.getView(R.id.ItemImage);
        View btn_delete_user = holder.getView(R.id.btn_delete_user);
        TextView tv_name = holder.getView(R.id.tv_name);
        Staff staff = getItem(position);
        if (staff.staffType == Staff.ADD) {
            btn_delete_user.setVisibility(View.INVISIBLE);
            tv_name.setVisibility(View.INVISIBLE);
            Uri uri = Uri.parse("res://" +
                    mContext.getPackageName() +
                    "/" + R.drawable.wg_xx_middle_add_btn);
            icon.setImageURI(uri);
        } else if (staff.staffType == Staff.DELETE) {
            btn_delete_user.setVisibility(View.INVISIBLE);
            tv_name.setVisibility(View.INVISIBLE);
            Uri uri = Uri.parse("res://" +
                    mContext.getPackageName() +
                    "/" + R.drawable.wg_xx_middle_reduction_btn);
            icon.setImageURI(uri);
        } else {
            tv_name.setVisibility(View.VISIBLE);
            ImageOptions.setUserImage(icon, staff.avatar);
            tv_name.setText(staff.nick);
            btn_delete_user.setVisibility(mType == ADAPTER_DELETE ? View.VISIBLE : View.INVISIBLE);
        }
        return convertView;
    }
}
