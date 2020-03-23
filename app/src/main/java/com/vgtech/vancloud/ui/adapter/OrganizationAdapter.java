package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.adapter.BaseSimpleAdapter;
import com.vgtech.common.api.Organization;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.chat.controllers.AvatarController;
import com.vgtech.vancloud.ui.group.OrganizationSelectedListener;

/**
 * Created by code on 2016/9/5.
 */
public class OrganizationAdapter extends BaseSimpleAdapter<Organization> implements View.OnClickListener {

    private OrganizationSelectedListener selectedListener;

    public OrganizationAdapter(Context context) {
        super(context);
    }


    public OrganizationAdapter(Context context, OrganizationSelectedListener listener) {
        super(context);
        selectedListener = listener;
    }

    @Override
    public void notifyDataSetChanged() {
        i = 0;
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemResource(int viewType) {
        return R.layout.organization_item;
    }

    private int i = 0;
    private boolean mFirst;

    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {
        Organization organization = getItem(position);
        View depart_view = holder.getView(R.id.depart_view);
        View user_view = holder.getView(R.id.user_view);
        View all_view = holder.getView(R.id.all_view);
        if (organization.isUser()) {
            if (i == 1 && !mFirst) {
                mFirst = true;
                organization.first = true;
            }
            user_view.setVisibility(View.VISIBLE);
            depart_view.setVisibility(View.GONE);
            all_view.setVisibility(View.GONE);
            TextView user_name = holder.getView(R.id.user_name);
            user_name.setText(organization.staff_name);
            TextView user_desc = holder.getView(R.id.user_desc);
            user_desc.setText(organization.pos);
            holder.getView(R.id.user_line).setVisibility(View.VISIBLE);
            holder.getView(R.id.item_line).setVisibility(View.GONE);
            holder.getView(R.id.all_line).setVisibility(View.GONE);
            SimpleDraweeView user_photo = holder.getView(R.id.user_photo);
            AvatarController.setAvatarView(organization.photo,user_photo);
            if (selectedListener != null) {
                ImageView checkBox = holder.getView(R.id.check_user);
                checkBox.setVisibility(selectedListener.getUnSeleced().contains(organization) ? View.INVISIBLE : View.VISIBLE);
                checkBox.setTag(organization);
                if (selectedListener.getSelectMode() == OrganizationSelectedListener.SELECT_MULTI)
                    checkBox.setOnClickListener(this);
                checkBox.setImageResource(selectedListener.contains(organization) ? R.mipmap.chk_on_normal : R.mipmap.chk_off_normal);
            }
        } else if ("all".equals(organization.code)) {
            user_view.setVisibility(View.GONE);
            depart_view.setVisibility(View.GONE);
            all_view.setVisibility(View.VISIBLE);
            holder.getView(R.id.item_line).setVisibility(View.GONE);
            holder.getView(R.id.user_line).setVisibility(View.GONE);
            holder.getView(R.id.all_line).setVisibility(View.VISIBLE);
            if (selectedListener != null) {
                ImageView checkBox = holder.getView(R.id.check_all);
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setTag(organization);
                checkBox.setOnClickListener(this);
                checkBox.setImageResource(selectedListener.contains(organization) ? R.mipmap.chk_on_normal : R.mipmap.chk_off_normal);
            }
        } else {
            i = 1;
            holder.getView(R.id.item_line).setVisibility(View.VISIBLE);
            holder.getView(R.id.user_line).setVisibility(View.GONE);
            holder.getView(R.id.all_line).setVisibility(View.GONE);
            user_view.setVisibility(View.GONE);
            depart_view.setVisibility(View.VISIBLE);
            all_view.setVisibility(View.GONE);
            TextView depart_name = holder.getView(R.id.depart_name);
            depart_name.setText(organization.label);
            TextView depart_count = holder.getView(R.id.depart_count);
            depart_count.setText(organization.num);
            if (selectedListener != null && selectedListener.getSelectMode() == OrganizationSelectedListener.SELECT_MULTI) {
                ImageView checkBox = holder.getView(R.id.check_depart);
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setTag(organization);
                checkBox.setOnClickListener(this);
                checkBox.setImageResource(selectedListener.contains(organization) ? R.mipmap.chk_on_normal : R.mipmap.chk_off_normal);
            }
        }
        holder.getView(R.id.item_spit).setVisibility(organization.first ? View.VISIBLE : View.GONE);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        Organization organization = (Organization) v.getTag();
        if (selectedListener.contains(organization)) {
            selectedListener.remove(organization);
            Organization allOrganization = getItem(0);
            if ("all".equals(allOrganization.code)) {
                selectedListener.remove(allOrganization);
                notifyDataSetChanged();
            }
            ((ImageView) v).setImageResource(selectedListener.contains(organization) ? R.mipmap.chk_on_normal : R.mipmap.chk_off_normal);
            if ("all".equals(organization.code)) {
                selectedListener.remove(getData());
                notifyDataSetChanged();
            }
        } else {
            selectedListener.add(organization);
            ((ImageView) v).setImageResource(selectedListener.contains(organization) ? R.mipmap.chk_on_normal : R.mipmap.chk_off_normal);
            if ("all".equals(organization.code)) {
                selectedListener.add(getData());
                notifyDataSetChanged();
            }

        }
    }
}
