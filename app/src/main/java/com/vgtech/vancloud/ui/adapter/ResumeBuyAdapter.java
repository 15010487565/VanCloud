package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.ResumeBuyBean;
import com.vgtech.common.api.RootData;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by code on 2015/12/25.
 */
public class ResumeBuyAdapter extends BaseAdapter implements View.OnClickListener, ViewListener {

    Context context;
    List<ResumeBuyBean> mlist = new ArrayList<>();
    Activity activity;
    private BaseActivity baseActivity;
    private NetworkManager mNetworkManager;
    private static final int CALLBACK_STORE = 1;
    int mPosition;
    private OnSelectListener mListener;
    private boolean isCheckbox = false;
    private boolean isStore = false;
    private boolean isDel = false;
    private boolean isfromStore = false;
    private boolean isfromApproval = false;
    private DelClickListener mDelListener;

    public void setIsfromApproval(boolean isfromApproval) {
        this.isfromApproval = isfromApproval;
    }

    public void setIsfromStore(boolean isfromStore) {
        this.isfromStore = isfromStore;
    }

    public void setIsDel(boolean isDel) {
        this.isDel = isDel;
    }

    public void setmDelListener(DelClickListener mDelListener) {
        this.mDelListener = mDelListener;
    }

    public void setIsCheckbox(boolean isCheckbox) {
        this.isCheckbox = isCheckbox;
    }

    public void setIsStore(boolean isStore) {
        this.isStore = isStore;
    }

    public List<ResumeBuyBean> getMlist() {
        return mlist;
    }

    public ResumeBuyAdapter(Context context, List<ResumeBuyBean> list) {
        this.context = context;
        this.mlist = list;
        baseActivity = (BaseActivity) context;
        mNetworkManager = baseActivity.getAppliction().getNetworkManager();
    }

    public void setOnSelectListener(OnSelectListener listener) {
        mListener = listener;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public Object getItem(int position) {
        return mlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        this.mlist.clear();
        try {
            this.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ResumeBuyBean resumeBuyBean = mlist.get(position);
        mPosition = position;
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_resume_buy, null);

            mViewHolder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
            mViewHolder.price_layout = (LinearLayout) convertView.findViewById(R.id.price_layout);
            mViewHolder.price_tv = (TextView) convertView.findViewById(R.id.price_tv);
            mViewHolder.user_photo = (SimpleDraweeView) convertView.findViewById(R.id.user_photo);
            mViewHolder.sex_iv = (ImageView) convertView.findViewById(R.id.sex_iv);
            mViewHolder.record_tv = (TextView) convertView.findViewById(R.id.record_tv);
            mViewHolder.job_type_tv = (TextView) convertView.findViewById(R.id.job_type_tv);
            mViewHolder.wish_job_tv = (TextView) convertView.findViewById(R.id.wish_job_tv);
            mViewHolder.paymoney_tv = (TextView) convertView.findViewById(R.id.paymoney_tv);
            mViewHolder.city_tv = (TextView) convertView.findViewById(R.id.city_tv);
            mViewHolder.praise_icon = (ImageView) convertView.findViewById(R.id.video_icon);
            mViewHolder.praise_icon.setOnClickListener(this);
            mViewHolder.type_view = (ImageView) convertView.findViewById(R.id.type_view);
            mViewHolder.time_tv = (TextView) convertView.findViewById(R.id.time_tv);
            mViewHolder.del_tv = (TextView) convertView.findViewById(R.id.del_tv);
            mViewHolder.del_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDelListener != null) {
                        mDelListener.delAction(resumeBuyBean, position);
                    }
                }
            });
            mViewHolder.select = (CheckBox) convertView.findViewById(R.id.checkbox_list_item);
            mViewHolder.select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ResumeBuyBean item = (ResumeBuyBean) buttonView.getTag();
                    if (isChecked) {
                        if (mListener != null) {
                            mListener.OnSelected(item);
                        }
                    } else {
                        if (mListener != null) {
                            mListener.OnUnSelected(item);
                        }
                    }
                }
            });
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        if (!TextUtils.isEmpty(resumeBuyBean.photo)) {
            ImageOptions.setUserImage(mViewHolder.user_photo,resumeBuyBean.photo);
        }
        mViewHolder.name_tv.setText(resumeBuyBean.name);
        if (!TextUtils.isEmpty(resumeBuyBean.price)) {
            mViewHolder.price_tv.setText(resumeBuyBean.price + context.getResources().getString(R.string.recruit_price));
            mViewHolder.price_layout.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.price_layout.setVisibility(View.GONE);
        }
        mViewHolder.record_tv.setText(resumeBuyBean.degree);
        mViewHolder.wish_job_tv.setText(context.getResources().getString(R.string.recruit_position) + resumeBuyBean.position);
        mViewHolder.paymoney_tv.setText(resumeBuyBean.salary_month);
        mViewHolder.city_tv.setText(resumeBuyBean.work_city);
        mViewHolder.time_tv.setText(baseActivity.getResources().getString(R.string.recruit_time) + Utils.getInstance(context).dateFormat(resumeBuyBean.creator_time));

        //收藏
        if (resumeBuyBean.favorite_status) {
            mViewHolder.praise_icon.setImageResource(R.mipmap.icon_store_press);
        } else {
            mViewHolder.praise_icon.setImageResource(R.mipmap.icon_store_normal);
        }
        if ("male".equals(resumeBuyBean.gender)) {
            mViewHolder.sex_iv.setImageResource(R.mipmap.icon_sex_man);
        } else {
            mViewHolder.sex_iv.setImageResource(R.mipmap.icon_sex_women);
        }
        mViewHolder.praise_icon.setTag(position);

        if (isCheckbox) {
            mViewHolder.select.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.select.setVisibility(View.GONE);
        }

        if (isStore) {
            mViewHolder.praise_icon.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.praise_icon.setVisibility(View.GONE);
        }

        if (isDel) {
            mViewHolder.del_tv.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.del_tv.setVisibility(View.GONE);
        }

        if ("bought".equals(resumeBuyBean.resume_status)) {
            mViewHolder.price_layout.setVisibility(View.GONE);
        } else {
            mViewHolder.price_layout.setVisibility(View.VISIBLE);
        }

        if (isfromApproval) {
            if ("rebutted".equals(resumeBuyBean.apply_status)) {
                mViewHolder.select.setVisibility(View.VISIBLE);
            } else {
                mViewHolder.select.setVisibility(View.GONE);
            }
        }

        if (isfromStore) {
            if (resumeBuyBean.remove_status) {
                mViewHolder.praise_icon.setEnabled(false);
                mViewHolder.type_view.setVisibility(View.VISIBLE);
                mViewHolder.type_view.setImageResource(R.mipmap.resume_del);
            } else {
                mViewHolder.praise_icon.setEnabled(true);
                mViewHolder.type_view.setVisibility(View.GONE);
            }
            if (PrfUtils.getExecutor(baseActivity)) {
                if (resumeBuyBean.remove_status) {
                    mViewHolder.select.setVisibility(View.GONE);
                } else {
                    mViewHolder.select.setVisibility(View.VISIBLE);
                }
                if ("inbox".equals(resumeBuyBean.resume_status) || "bought".equals(resumeBuyBean.resume_status)) {
                    mViewHolder.select.setVisibility(View.VISIBLE);
                } else {
                    mViewHolder.select.setVisibility(View.GONE);
                }
            } else {
                mViewHolder.select.setVisibility(View.GONE);
            }
        }

        mViewHolder.select.setTag(resumeBuyBean);
        boolean isSelect = (mListener != null && mListener.OnIsSelect(resumeBuyBean)) ? true : false;
        mViewHolder.select.setChecked(isSelect);

        return convertView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_icon:
                final int position = (int) v.getTag();
                final ResumeBuyBean resumeBuyBean = mlist.get(position);
                storeAction(resumeBuyBean.resume_id, resumeBuyBean.favorite_status, position);
                break;
        }
    }

    public void storeAction(String resumeId, final boolean isStore, final int position) {
        baseActivity.showLoadingDialog(context, context.getResources().getString(R.string.please_wait_a_min));
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(context));
        params.put("tenant_id", PrfUtils.getTenantId(context));
        params.put("recruit_id", PrfUtils.getResumeId(context));
        params.put("resume_id", resumeId);
        if (isStore) {
            params.put("favorite_status", "N");
        } else {
            params.put("favorite_status", "Y");
        }
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_PLUGIN_RECRUIT_FAVORITES_CREATE), params, context);
        mNetworkManager.load(CALLBACK_STORE, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

                baseActivity.dismisLoadingDialog();
                boolean safe = ActivityUtils.prehandleNetworkData(context, this, callbackId, path, rootData, true);
                if (!safe) {
                    return;
                }
                switch (callbackId) {
                    case CALLBACK_STORE:
                        if (isfromStore) {
                            String status = path.getPostValues().get("favorite_status");
                            if ("N".equals(status)) {
                                removeItemAction(position);
                                LocalBroadcastManager.getInstance(baseActivity).sendBroadcast(new Intent(Actions.ACTION_ISSTORE_UNSTORE));
                            }
                        } else {
                            chaneStore(position, isStore);
                        }
                        break;
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(String response) {

            }
        });
    }

    private View lastView;

    public View getLastView() {
        return lastView;
    }

    public void setLastView(View view) {
        lastView = view;
    }

    private class ViewHolder {
        SimpleDraweeView user_photo;
        ImageView sex_iv;
        ImageView praise_icon;
        ImageView type_view;
        TextView name_tv;
        TextView price_tv;
        LinearLayout price_layout;
        TextView record_tv;
        TextView job_type_tv;
        TextView wish_job_tv;
        TextView paymoney_tv;
        TextView time_tv;
        TextView del_tv;
        TextView city_tv;
        CheckBox select;

    }

    public void removeItemAction(int position) {
        ResumeBuyBean resumeBuyBean = mlist.get(position);
        mlist.remove(resumeBuyBean);
        notifyDataSetChanged();
    }

    public void chaneStore(int position, boolean isStore) {

        ResumeBuyBean resumeBuyBean = mlist.get(position);
        resumeBuyBean.favorite_status = !isStore;
        try {
            resumeBuyBean.getJson().put("isstore", !isStore);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void myNotifyDataSetChanged(List<ResumeBuyBean> lists) {
        this.mlist = lists;
        notifyDataSetChanged();
    }

    public void destroy() {
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }

    public interface OnSelectListener {
        /**
         * 选中
         */
        void OnSelected(ResumeBuyBean item);

        /**
         * 取消选中
         */
        void OnUnSelected(ResumeBuyBean item);

        /**
         * 判断是否选中
         */
        boolean OnIsSelect(ResumeBuyBean item);
    }

    public interface DelClickListener {
        void delAction(ResumeBuyBean item, int position);
    }
}
