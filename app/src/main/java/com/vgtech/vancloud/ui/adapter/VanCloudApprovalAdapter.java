package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.RootData;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.Approval;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.vancloud.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.vgtech.vancloud.utils.Utils.getResources;

/**
 * Created by Duke on 2016/9/23.
 */

public class VanCloudApprovalAdapter extends BaseAdapter implements View.OnClickListener {

    private Context mContext;
    private List<Approval> dataList;
    private BaseActivity baseActivity;
    private static final int CALLBACK_REMINDERS_APPROVAL = 1;
    private NetworkManager mNetworkManager;

    private String type;// 1我发起的，2我审批的，3抄送我的

    public VanCloudApprovalAdapter(Context context, List<Approval> approvals, String type) {
        mContext = context;
        dataList = approvals;
        baseActivity = (BaseActivity) context;
        mNetworkManager = baseActivity.getAppliction().getNetworkManager();
        this.type = type;

    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.approval_item_layout, null);
            mViewHolder.imgLogoView = (SimpleDraweeView) convertView.findViewById(R.id.img_logo);
            mViewHolder.titleView = (TextView) convertView.findViewById(R.id.title_tv);
            mViewHolder.stateView = (TextView) convertView.findViewById(R.id.state_tv);
            mViewHolder.timeView = (TextView) convertView.findViewById(R.id.time_tv);
            mViewHolder.canHastenView = (TextView) convertView.findViewById(R.id.can_reminders);
            mViewHolder.processStateView = (TextView) convertView.findViewById(R.id.process_state_tv);
            mViewHolder.topView = convertView.findViewById(R.id.top_view);
            mViewHolder.lineView = convertView.findViewById(R.id.line_view);
            mViewHolder.timeLayout = convertView.findViewById(R.id.time_layout);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        Approval approval = dataList.get(position);
        mViewHolder.titleView.setText(Html.fromHtml(approval.title));
        mViewHolder.timeView.setText(Utils.getInstance(mContext).dateFormat(approval.timestamp));
        mViewHolder.timeLayout.setTag(R.id.position, position);
        mViewHolder.timeLayout.setOnClickListener(this);
        if (approval.canHasten) {
            if (approval.hasTenState) {
                mViewHolder.timeLayout.setClickable(true);
                mViewHolder.canHastenView.setTextColor(getResources().getColor(R.color.bg_title));
            } else {
                mViewHolder.timeLayout.setClickable(false);
                mViewHolder.canHastenView.setTextColor(getResources().getColor(R.color.diaphaneity_grey));
            }
            mViewHolder.canHastenView.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.timeLayout.setClickable(false);
            mViewHolder.canHastenView.setVisibility(View.GONE);
        }

//        if (approval.canHasten)
//            mViewHolder.canHastenView.setVisibility(View.VISIBLE);
//        else
//            mViewHolder.canHastenView.setVisibility(View.GONE);

        mViewHolder.stateView.setText(approval.status);
        if ("3".equals(approval.processState))
            mViewHolder.processStateView.setVisibility(View.GONE);
        else if ("1".equals(approval.processState)) {
            mViewHolder.processStateView.setVisibility(View.VISIBLE);
            mViewHolder.processStateView.setTextColor(mContext.getResources().getColor(R.color.process_agree));
            mViewHolder.processStateView.setText(mContext.getResources().getString(R.string.approval_agree));
        } else if ("2".equals(approval.processState)) {
            mViewHolder.processStateView.setVisibility(View.VISIBLE);
            mViewHolder.processStateView.setTextColor(mContext.getResources().getColor(R.color.process_disagree));
            mViewHolder.processStateView.setText(mContext.getResources().getString(R.string.approval_refuse));
        }
        if ("2".equals(type)) {
            mViewHolder.topView.setVisibility(View.GONE);
            mViewHolder.lineView.setVisibility(View.VISIBLE);
            if ("3".equals(approval.processState))
                mViewHolder.stateView.setTextColor(mContext.getResources().getColor(R.color.process_undetermined));
            else
                mViewHolder.stateView.setTextColor(mContext.getResources().getColor(R.color.comment_grey));
            ImageOptions.setUserImage(mViewHolder.imgLogoView, approval.photo);
            UserUtils.enterUserInfo(mContext, approval.sendUserId, "", approval.photo, mViewHolder.imgLogoView);

        } else {
            mViewHolder.topView.setVisibility(View.VISIBLE);
            mViewHolder.lineView.setVisibility(View.GONE);
            mViewHolder.stateView.setTextColor(mContext.getResources().getColor(R.color.comment_grey));
            mViewHolder.imgLogoView.setBackgroundResource(R.drawable.approval_round);
            GradientDrawable myGrad = (GradientDrawable) mViewHolder.imgLogoView.getBackground();
            Resources resources = mContext.getResources();
            int color = resources.getColor(R.color.app_approval_shenpi);
//            approval.type;//1普通审批。2请假。5招聘计划
            if ("1".equals(approval.type)) {
                color = resources.getColor(R.color.app_approval_shenpi);
                mViewHolder.imgLogoView.setImageResource(R.mipmap.ic_app_approve_shenpi);
            } else if ("2".equals(approval.type)) {
                color = resources.getColor(R.color.app_leave);
                mViewHolder.imgLogoView.setImageResource(R.mipmap.approval_vacation_logo);
            } else if ("5".equals(approval.type)) {
                color = resources.getColor(R.color.app_recruit);
                mViewHolder.imgLogoView.setImageResource(R.mipmap.approval_recruit_plan_logo);
            }
            myGrad.setColor(color);
        }
        return convertView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.time_layout:
                //TODO 催办
                final int position = (int) v.getTag(R.id.position);
                final Approval approval = dataList.get(position);

                if (approval.canHasten && approval.hasTenState) {
                    new AlertDialog(mContext).builder() .setTitle(mContext.getString(R.string.frends_tip))
                            .setMsg(approval.hastenMsg)
                            .setPositiveButton(mContext.getString(R.string.ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    remindersApproval(approval.processid, position);
                                }
                            }).setNegativeButton(mContext.getString(R.string.cancel), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                }
                break;

            default:

                break;
        }

    }


    private class ViewHolder {

        SimpleDraweeView imgLogoView;
        TextView titleView;
        TextView stateView;
        TextView timeView;
        TextView canHastenView;
        TextView processStateView;
        View topView;
        View lineView;
        View timeLayout;

    }

    public void myNotifyDataSetChanged(List<Approval> list) {

        dataList = list;
        notifyDataSetChanged();
    }

    public List<Approval> getList() {
        return dataList;
    }

    //催办
    public void remindersApproval(String processid, final int position) {
        baseActivity.showLoadingDialog(mContext, mContext.getString(R.string.prompt_info_02));
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(mContext));
        params.put("tenantid", PrfUtils.getTenantId(mContext));
        params.put("processid", processid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(mContext, URLAddr.URL_PROCESS_HASTEN), params, mContext);
        mNetworkManager.load(CALLBACK_REMINDERS_APPROVAL, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

                baseActivity.dismisLoadingDialog();
                boolean safe = ActivityUtils.prehandleNetworkData(mContext, this, callbackId, path, rootData, true);
                if (!safe) {
                    return;
                }
                switch (callbackId) {
                    case CALLBACK_REMINDERS_APPROVAL:
                        Toast.makeText(mContext, mContext.getString(R.string.send_has_success), Toast.LENGTH_SHORT).show();
                        chaneHastenState(position);
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

    public void chaneHastenState(int position) {
        Approval approval = dataList.get(position);
        approval.hasTenState = false;
//        try {
//            approval.getJson().put("canHasten", false);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        notifyDataSetChanged();

    }

    public void destroy() {
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }


    public void deleteItem(int position) {
        Approval approval = dataList.get(position);
        dataList.remove(approval);
        notifyDataSetChanged();
    }
}
