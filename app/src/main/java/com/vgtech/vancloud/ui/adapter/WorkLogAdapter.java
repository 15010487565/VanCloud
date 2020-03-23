package com.vgtech.vancloud.ui.adapter;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.WorkLogBean;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.vancloud.R;

import java.util.List;

/**
 * Data:  2018/7/4
 * Auther: 陈占洋
 * Description:
 */

public class WorkLogAdapter extends BaseAdapter {

    List<WorkLogBean> mWorkLogData;
    private OnRootClickListener mOnRootClickListener;
    private static final int MINE_TYPE = 0x01;
    private static final int SUB_TYPE = 0x02;
    private static final int SUB_TYPE_SORT_STAFF = 0x03;
    private int mType = MINE_TYPE;

    public WorkLogAdapter(@NonNull List<WorkLogBean> workLogData) {
        mWorkLogData = workLogData;
    }

    @Override
    public int getCount() {
        return mWorkLogData == null ? 0 : mWorkLogData.size();
    }

    @Override
    public Object getItem(int position) {
        return mWorkLogData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.work_log_adapter_item, null);
            viewHolder = new ViewHolder();
            viewHolder.mRoot = (LinearLayout) convertView.findViewById(R.id.work_log_adapter_root);
            viewHolder.mName = (TextView) convertView.findViewById(R.id.work_log_adapter_name);
            viewHolder.mWorkTime = (TextView) convertView.findViewById(R.id.work_log_adapter_tv_work_time);
            viewHolder.mWorkDuration = (TextView) convertView.findViewById(R.id.work_log_adapter_tv_work_duration);
            viewHolder.mWorkDescription = (TextView) convertView.findViewById(R.id.work_log_adapter_tv_work_description);
            viewHolder.mWorkContent = (TextView) convertView.findViewById(R.id.work_log_adapter_tv_work_content);
            viewHolder.mDetailParent = (LinearLayout) convertView.findViewById(R.id.work_log_adapter_ll_detail_parent);
            viewHolder.mWorkLocation = (TextView) convertView.findViewById(R.id.work_log_adapter_tv_location);
            viewHolder.mWorkReleventPerson = (TextView) convertView.findViewById(R.id.work_log_adapter_tv_relevent_person);
            viewHolder.mWorkBranch = (TextView) convertView.findViewById(R.id.work_log_adapter_tv_branch);
            viewHolder.mWorkThoughts = (TextView) convertView.findViewById(R.id.work_log_adapter_tv_thoughts);
            viewHolder.mWorkAttachmentPic = (SimpleDraweeView) convertView.findViewById(R.id.work_log_adapter_iv_attachment_pic);
            viewHolder.mTakeback = (TextView) convertView.findViewById(R.id.work_log_adapter_tv_take_back);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        WorkLogBean workLogBean = mWorkLogData.get(position);

        viewHolder.mRoot.setTag(position);
        viewHolder.mRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                if (mOnRootClickListener != null) {
                    mOnRootClickListener.onRootClick(position);
                }
            }
        });

        if (workLogBean.isOnlyStaff()) {
            viewHolder.mName.setVisibility(View.VISIBLE);
            viewHolder.mName.setText(workLogBean.getStaffName() + "  (" + workLogBean.getStaffNo() + ")");
        } else {
            viewHolder.mName.setVisibility(View.GONE);
        }

        //开始结束时间
        String currentDate = DateTimeUtil.getCurrentString_YMd();

        float minSize = viewHolder.mWorkTime.getPaint().measureText("12:01-12:01");
        viewHolder.mWorkTime.setMinWidth((int) minSize);

        if (currentDate.equals(workLogBean.getDates())) {
            viewHolder.mWorkTime.setText(workLogBean.getFromTime() + "-" + workLogBean.getToTime());
        } else {
            viewHolder.mWorkTime.setText(workLogBean.getDates() + "\n" + workLogBean.getFromTime() + "-" + workLogBean.getToTime());
        }
        //时长
        viewHolder.mWorkDuration.setText(workLogBean.getDuration() + parent.getContext().getString(R.string.working_hours));
        //工作描述
        if (mType == SUB_TYPE) {
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append(workLogBean.getWorkBrief() + "  " + workLogBean.getStaffName() + " (" + workLogBean.getStaffNo() + ")");
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#99999999"));
            ssb.setSpan(colorSpan, workLogBean.getWorkBrief().length() + 2, ssb.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            viewHolder.mWorkDescription.setText(ssb);
        } else {
            viewHolder.mWorkDescription.setText(workLogBean.getWorkBrief());
        }
        //工作详细内容
        if (workLogBean.isShowDetail()) {
            viewHolder.mWorkContent.setText(workLogBean.getWorkContent());
        } else {
            viewHolder.mWorkContent.setTag(position);
            setWorkDetail(viewHolder.mWorkContent, workLogBean.getWorkContent());
        }
        //地点
        viewHolder.mWorkLocation.setText(workLogBean.getWorkLocation());
        //相关人
        viewHolder.mWorkReleventPerson.setText(workLogBean.getRelatedPerson());
        //成本中心
        viewHolder.mWorkBranch.setText(workLogBean.getCostName());
        //感想
        viewHolder.mWorkThoughts.setText(workLogBean.getMyReflections());
        //附件
        if (TextUtils.isEmpty(workLogBean.getImageUrl())) {
            viewHolder.mWorkAttachmentPic.setVisibility(View.GONE);
        } else {
            viewHolder.mWorkAttachmentPic.setVisibility(View.VISIBLE);
            ImageOptions.setImage(viewHolder.mWorkAttachmentPic, workLogBean.getImageUrl());
        }
        //收回
        viewHolder.mTakeback.setTag(position);
        viewHolder.mTakeback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                onTakebackClick(position);
            }
        });
        if (workLogBean.isShowDetail()) {
            viewHolder.mDetailParent.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mDetailParent.setVisibility(View.GONE);
        }
        return convertView;
    }

    public int getDataSize() {
        return mWorkLogData == null ? 0 : mWorkLogData.size();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setWorkDetail(final TextView contentView, final String content) {

        contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                contentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setTextSize(contentView.getTextSize());
                float detailTextSize = paint.measureText("...  【" + contentView.getContext().getString(R.string.btn_info) + "】");
                float contentTextSize = paint.measureText(content);
                float totalTextSize = detailTextSize + contentTextSize;
                int width = (int) ((contentView.getWidth() - contentView.getPaddingLeft() - contentView.getPaddingRight()) * 3 - paint.measureText("工作"));
                if (totalTextSize < width) {
                    String text = content + "  【" + contentView.getContext().getString(R.string.btn_info) + "】";
                    setTextWidgetSpan(text, contentView);
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < content.length(); i++) {
                        sb.append(content.charAt(i));
                        float textSize = paint.measureText(sb.toString());
                        if (textSize + detailTextSize >= width) {
//                            sb.delete(sb.length() - 2, sb.length());
                            break;
                        }
                    }
                    sb.append("...  【" + contentView.getContext().getString(R.string.btn_info) + "】");
                    setTextWidgetSpan(sb.toString(), contentView);
                }
            }
        });


    }

    private void setTextWidgetSpan(String text, TextView contentView) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(text);

        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FF3AB5FF"));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                int position = (int) widget.getTag();
                onDetailClick(position);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
        ssb.setSpan(colorSpan, ssb.length() - 4, ssb.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ssb.setSpan(clickableSpan, ssb.length() - 4, ssb.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        contentView.setText(ssb);
        contentView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void onDetailClick(int position) {
        mWorkLogData.get(position).setShowDetail(true);
        notifyDataSetChanged();
    }

    private void onTakebackClick(int position) {

        mWorkLogData.get(position).setShowDetail(false);
        notifyDataSetChanged();
    }

    public void setData(List<WorkLogBean> workLogData) {
        mWorkLogData = workLogData;
        this.notifyDataSetChanged();
    }

    public List<WorkLogBean> getData() {
        return mWorkLogData;
    }

    public void addData(List<WorkLogBean> workLogData) {
        if (workLogData == null || workLogData.size() <= 0) {
            return;
        }
        mWorkLogData.addAll(workLogData);
        this.notifyDataSetChanged();
    }

    public void setType(int type) {
        mType = type;
    }

    class ViewHolder {
        LinearLayout mRoot;
        TextView mName;
        TextView mWorkTime;
        TextView mWorkDuration;
        TextView mWorkDescription;
        TextView mWorkContent;
        LinearLayout mDetailParent;
        TextView mWorkLocation;
        TextView mWorkReleventPerson;
        TextView mWorkBranch;
        TextView mWorkThoughts;
        SimpleDraweeView mWorkAttachmentPic;
        TextView mTakeback;
    }

    public void setOnRootClickListener(OnRootClickListener l) {
        this.mOnRootClickListener = l;
    }

    public interface OnRootClickListener {
        void onRootClick(int position);
    }
}
