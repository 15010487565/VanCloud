package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.ImageCacheManager;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.Approval;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vantop.moudle.VacationChange;
import com.vgtech.vantop.ui.vacation.VacationApplyDetailsActivity;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.vgtech.vancloud.utils.Utils.getResources;

/**
 * Created by Duke on 2016/9/23.
 */

public class VanTopApprovalAdapter extends BaseAdapter implements View.OnClickListener {

    private Context mContext;
    private List<Approval> dataList;
    private BaseActivity baseActivity;
    private static final int CALLBACK_REMINDERS_APPROVAL = 1;
    private NetworkManager mNetworkManager;

    private String type;// 1我发起的，2我审批的，3抄送我的
    private int isShowBatch = View.GONE;//是否显示批量图标
    public void isShowBatch(int isShowBatch){
       this.isShowBatch = isShowBatch;
       notifyDataSetChanged();
    }
    public VanTopApprovalAdapter(Context context, List<Approval> approvals, String type) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {


        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.approval_item_layout, null);
            mViewHolder.btSelect = convertView.findViewById(R.id.bt_Select);

            mViewHolder.imgLogoView = (SimpleDraweeView) convertView.findViewById(R.id.img_logo);
            mViewHolder.titleView = (TextView) convertView.findViewById(R.id.title_tv);
            mViewHolder.stateView = (TextView) convertView.findViewById(R.id.state_tv);
            mViewHolder.timeView = (TextView) convertView.findViewById(R.id.time_tv);
            mViewHolder.startEndTime = (TextView) convertView.findViewById(R.id.approval_start_end_time);
            mViewHolder.leaveType = (TextView) convertView.findViewById(R.id.approval_leave_type);
            mViewHolder.timeLength = (TextView) convertView.findViewById(R.id.approval_time_length);
            mViewHolder.canHastenView = (TextView) convertView.findViewById(R.id.can_reminders);
            mViewHolder.processStateView = (TextView) convertView.findViewById(R.id.process_state_tv);
            mViewHolder.topView = convertView.findViewById(R.id.top_view);
            mViewHolder.lineView = convertView.findViewById(R.id.line_view);
            mViewHolder.chaneLayout = (RelativeLayout) convertView.findViewById(R.id.chane_layout);
            mViewHolder.changeImg = (ImageView) convertView.findViewById(R.id.img001);
            mViewHolder.changeTitleView = (TextView) convertView.findViewById(R.id.change_title_tv);
            mViewHolder.changeStateView = (TextView) convertView.findViewById(R.id.change_state_tv);
            mViewHolder.changeTimeView = (TextView) convertView.findViewById(R.id.change_time_tv);
            mViewHolder.changeStateTv = (TextView) convertView.findViewById(R.id.change_state_view);
            mViewHolder.timeLayout = convertView.findViewById(R.id.time_layout);
            //顶部状态
            mViewHolder.tvChangeHint = convertView.findViewById(R.id.tv_changeHint);

            //小列表布局
            mViewHolder.tvChangeStateHint = convertView.findViewById(R.id.tv_changeStateHint);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        final Approval approval = dataList.get(position);
        if (TextUtils.isEmpty(approval.lea_type)) {
            if (mViewHolder.leaveType.getVisibility() != View.INVISIBLE) {
                mViewHolder.leaveType.setVisibility(View.INVISIBLE);
            }
            if (mViewHolder.startEndTime.getVisibility() != View.VISIBLE) {
                mViewHolder.startEndTime.setVisibility(View.VISIBLE);
            }
            if (mViewHolder.timeLength.getVisibility() != View.VISIBLE) {
                mViewHolder.timeLength.setVisibility(View.VISIBLE);
            }
            if (mViewHolder.imgLogoView.getVisibility() != View.VISIBLE) {
                mViewHolder.imgLogoView.setVisibility(View.VISIBLE);
            }
            if (mViewHolder.changeImg.getVisibility() != View.VISIBLE) {
                mViewHolder.changeImg.setVisibility(View.VISIBLE);
            }
        } else {
            if (mViewHolder.leaveType.getVisibility() != View.VISIBLE) {
                mViewHolder.leaveType.setVisibility(View.VISIBLE);
            }
            if (mViewHolder.startEndTime.getVisibility() != View.VISIBLE) {
                mViewHolder.startEndTime.setVisibility(View.VISIBLE);
            }
            if (mViewHolder.timeLength.getVisibility() != View.VISIBLE) {
                mViewHolder.timeLength.setVisibility(View.VISIBLE);
            }
            if (mViewHolder.imgLogoView.getVisibility() != View.GONE) {
                mViewHolder.imgLogoView.setVisibility(View.GONE);
            }
            if (mViewHolder.changeImg.getVisibility() != View.GONE) {
                mViewHolder.changeImg.setVisibility(View.GONE);
            }

        }

        mViewHolder.titleView.setText(approval.title);
        mViewHolder.timeView.setText(Utils.vantopDateFormat1(mContext,approval.timestamp));
//        mViewHolder.timeView.setText(DateTimeUtil.longToString_YMd(approval.timestamp));
        mViewHolder.timeLayout.setTag(R.id.position, position);
        mViewHolder.timeLayout.setOnClickListener(this);
        mViewHolder.chaneLayout.setVisibility(View.GONE);
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

        mViewHolder.stateView.setText(approval.status);
        if ("0".equals(approval.processState))
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
            if ("0".equals(approval.processState))
                mViewHolder.stateView.setTextColor(mContext.getResources().getColor(R.color.process_undetermined));
            else
                mViewHolder.stateView.setTextColor(mContext.getResources().getColor(R.color.comment_grey));
            ImageCacheManager.getImage(mContext, mViewHolder.imgLogoView, approval.staffNo);
            VanTopUtils.enterVantopUserInfoBystaffNo(mContext, approval.staffNo, mViewHolder.imgLogoView);
            if ("LEA".equals(approval.classType)) {
                if ("1".equals(approval.changeStatusType)){//1表示撤销，2表示变更
                    mViewHolder.tvChangeHint.setText(mContext.getResources().getString(R.string.vantop_overtime_undone));
                    mViewHolder.tvChangeHint.setVisibility(View.VISIBLE);
                }else if ("2".equals(approval.changeStatusType)){
                    mViewHolder.tvChangeHint.setText(mContext.getResources().getString(R.string.vantop_overtime_changed));
                    mViewHolder.tvChangeHint.setVisibility(View.VISIBLE);
                }else {
                    mViewHolder.tvChangeHint.setVisibility(View.GONE);
                }
            }else {
                mViewHolder.tvChangeHint.setVisibility(View.GONE);
            }

        } else {
            mViewHolder.topView.setVisibility(View.VISIBLE);
            mViewHolder.lineView.setVisibility(View.GONE);
            mViewHolder.stateView.setTextColor(mContext.getResources().getColor(R.color.comment_grey));
            mViewHolder.imgLogoView.setBackgroundResource(R.drawable.approval_round);
            GradientDrawable myGrad = (GradientDrawable) mViewHolder.imgLogoView.getBackground();
            Resources resources = mContext.getResources();
            int color = resources.getColor(R.color.app_approval_shenpi);

//            classType;//LEA休假。OT加班。CAR签卡
            if ("LEA".equals(approval.classType)) {
                color = resources.getColor(R.color.app_leave);
                mViewHolder.imgLogoView.setImageResource(R.mipmap.approval_vacation_logo);
                if ("1".equals(type)) {
                    if ("1".equals(approval.changeStatusType)){//1表示撤销，2表示变更
                        mViewHolder.tvChangeHint.setText(mContext.getResources().getString(R.string.vantop_overtime_undone));
                        mViewHolder.tvChangeHint.setVisibility(View.VISIBLE);
                    }else if ("2".equals(approval.changeStatusType)){
                        mViewHolder.tvChangeHint.setText(mContext.getResources().getString(R.string.vantop_overtime_changed));
                        mViewHolder.tvChangeHint.setVisibility(View.VISIBLE);
                    }else {
                        mViewHolder.tvChangeHint.setVisibility(View.GONE);
                    }
                    try {
                        String changeJson = approval.getJson().getString("vacationChange");
                        if (TextUtils.isEmpty(changeJson) || "{}".equals(changeJson)) {
                            mViewHolder.chaneLayout.setVisibility(View.GONE);
                        } else {
                            mViewHolder.chaneLayout.setVisibility(View.VISIBLE);
                            final VacationChange change = JsonDataFactory.getData(VacationChange.class, new JSONObject(changeJson));
                            mViewHolder.changeTitleView.setText(change.title);
                            mViewHolder.changeTimeView.setText(Utils.vantopDateFormat(change.timestamp));
                            mViewHolder.changeStateTv.setText(change.status);

                            String changeStatusType = approval.getJson().getString("changeStatusType");
                            Log.e("TAG_变更","changeStatusType="+changeStatusType);
                            if ("1".equals(changeStatusType)){//1表示撤销，2表示变更
                                mViewHolder.tvChangeStateHint.setText(mContext.getResources().getString(R.string.vantop_overtime_undo));
                                mViewHolder.tvChangeStateHint.setVisibility(View.VISIBLE);
                            }else if ("2".equals(changeStatusType)){
                                mViewHolder.tvChangeStateHint.setText(mContext.getResources().getString(R.string.vantop_overtime_change_ne));
                                mViewHolder.tvChangeStateHint.setVisibility(View.VISIBLE);
                            }else {
                                mViewHolder.tvChangeStateHint.setVisibility(View.GONE);
                            }
                            if ("0".equals(change.processState))
                                mViewHolder.changeStateView.setVisibility(View.GONE);
                            else if ("1".equals(change.processState)) {
                                mViewHolder.changeStateView.setVisibility(View.VISIBLE);
                                mViewHolder.changeStateView.setTextColor(mContext.getResources().getColor(R.color.process_agree));
                                mViewHolder.changeStateView.setText(mContext.getResources().getString(R.string.approval_agree));
                            } else if ("2".equals(change.processState)) {
                                mViewHolder.changeStateView.setVisibility(View.VISIBLE);
                                mViewHolder.changeStateView.setTextColor(mContext.getResources().getColor(R.color.process_disagree));
                                mViewHolder.changeStateView.setText(mContext.getResources().getString(R.string.approval_refuse));
                            }

                            mViewHolder.chaneLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent intent = new Intent(mContext, VacationApplyDetailsActivity.class);
                                    intent.putExtra("id", change.taskId);
                                    intent.putExtra("isChaneLayout", true);//是否从变更页面跳转
                                    mContext.startActivity(intent);
                                    Log.e("TAG_变更","view=");
                                }
                            });
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    mViewHolder.tvChangeHint.setVisibility(View.GONE);
                }
            } else if ("OT".equals(approval.classType)) {
                color = resources.getColor(R.color.app_overtime);
                mViewHolder.imgLogoView.setImageResource(R.mipmap.approval_overtime_logo);
                mViewHolder.tvChangeHint.setVisibility(View.GONE);
            } else if ("CAR".equals(approval.classType)) {
                color = resources.getColor(R.color.app_sign_card);
                mViewHolder.imgLogoView.setImageResource(R.mipmap.approval_sign_card);
                mViewHolder.tvChangeHint.setVisibility(View.GONE);
            }
            myGrad.setColor(color);
        }
        if ("OT".equals(approval.classType)){
//            mViewHolder.startEndTime.setVisibility(View.VISIBLE);
            mViewHolder.timeLength.setVisibility(View.VISIBLE);
//            Log.e("TAG_审批","ot_time="+approval.ot_time);
            mViewHolder.startEndTime.setText(Utils.getString(mContext,R.string.time) + approval.ot_startDate + "\t" +  approval.ot_time);
            float duration = Float.parseFloat(approval.ot_shichang) - Float.parseFloat(approval.ot_dshichang);
            String d = String.format("%.2f", duration);
            mViewHolder.timeLength.setText(Utils.getString(mContext,R.string.duration) + d + Utils.getString(mContext,R.string.vantop_hour));
        }else if ("CAR".equals(approval.classType)){
//            mViewHolder.startEndTime.setVisibility(View.VISIBLE);
            mViewHolder.timeLength.setVisibility(View.GONE);
            mViewHolder.startEndTime.setText(Utils.getString(mContext,R.string.time) + approval.car_time);
        }else {
            mViewHolder.leaveType.setText("(" + approval.lea_type + ")");
            mViewHolder.startEndTime.setText(Utils.getString(mContext,R.string.time) + approval.lea_startTime + " — " + approval.lea_endTime);
            mViewHolder.timeLength.setText(Utils.getString(mContext,R.string.duration) + approval.lea_shichang + approval.lea_shichangdanwei);
        }
        //批量
        mViewHolder.btSelect.setVisibility(isShowBatch);

        boolean check = approval.isCheck();
//        Log.e("TAG_批量","check="+check+";position="+position);
        if (check){
            mViewHolder.btSelect.setBackgroundResource(R.drawable.selector_batch);
        }else {
            mViewHolder.btSelect.setBackgroundResource(R.drawable.selector_batch_un);

        }

        mViewHolder.btSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Approval approval1 = dataList.get(position);
                boolean check = approval1.isCheck();
                if (check){
                    approval1.setCheck(false);

                }else {
                    approval1.setCheck(true);
                }
                selectListener.onSelecBatchClick();
                notifyDataSetChanged();
            }
        });
        mViewHolder.imgLogoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Approval approval1 = dataList.get(position);
                boolean check = approval1.isCheck();
                if (check){
                    approval1.setCheck(false);

                }else {
                    approval1.setCheck(true);
                }
                notifyDataSetChanged();
            }
        });
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
                    new AlertDialog(mContext).builder() .setTitle(Utils.getString(mContext,R.string.frends_tip))
                            .setMsg(approval.hastenMsg)
                            .setPositiveButton(mContext.getString(R.string.ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //TODO
                                    remindersApproval(approval, position);
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
        ImageView btSelect;
        SimpleDraweeView imgLogoView;
        TextView titleView;
        TextView stateView;
        TextView timeView;
        TextView canHastenView;
        TextView processStateView;
        TextView leaveType;
        TextView startEndTime;
        TextView timeLength;
        View topView;
        View lineView;

        RelativeLayout chaneLayout;
        ImageView changeImg;
        TextView changeTitleView;
        TextView changeStateView;
        TextView changeTimeView;
        TextView changeStateTv,tvChangeHint,tvChangeStateHint;
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
    public void remindersApproval(Approval approval, final int position) {
        baseActivity.showLoadingDialog(mContext, mContext.getString(R.string.prompt_info_02));
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(mContext));
        params.put("tenantid", PrfUtils.getTenantId(mContext));
        params.put("processid", approval.processid);
        params.put("beHastenStaffNo", approval.supervisorStaffNo);
        params.put("classType", approval.classType);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(mContext, URLAddr.URL_PROCESS_HASTEN_VANTOP), params, mContext);
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
        notifyDataSetChanged();
    }

    public void removeIterm(int position) {
        Approval approval = dataList.get(position);
        dataList.remove(approval);
        notifyDataSetChanged();

    }

    public void destroy() {
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }
    public interface SelectListener {
        public void onSelecBatchClick();
    }
    private SelectListener selectListener;
    public void setOnSelectClickListener (SelectListener  selectListener) {
        this.selectListener = selectListener;
    }
}
