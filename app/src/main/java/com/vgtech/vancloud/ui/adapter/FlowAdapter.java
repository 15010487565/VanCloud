package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
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
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.Flow;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.ResourceInfo;
import com.vgtech.common.api.RootData;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.module.flow.FlowHandleActivity;
import com.vgtech.vancloud.ui.module.todo.ToDoListDig;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.ui.view.MoreButtonPopupWindowFlow;
import com.vgtech.vancloud.utils.EditUtils;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程列表适配器
 */

public class FlowAdapter extends BaseAdapter implements View.OnClickListener, ViewListener {
    private static final String TAG = "LlhFragmentActivity";
    Context context;
    List<Flow> list = new ArrayList<Flow>();
    Activity activity;
    int type;//1我发起的，2我审批的，3抄送我的
    private BaseActivity baseActivity;
    private NetworkManager mNetworkManager;
    private static final int CALLBACK_FLOWCANCLE = 1;
    public static String FLOWID = "processid";
    int mPosition;
    boolean isFromTodo = false;
    private ToDoListDig toDoListDig;

    Fragment fragment;

    public FlowAdapter(Context context, List<Flow> list, int type) {

        this.context = context;
        this.list = list;
        this.type = type;
        baseActivity = (BaseActivity) context;
        mNetworkManager = baseActivity.getAppliction().getNetworkManager();
    }

    public FlowAdapter(Context context, List<Flow> list, int type, ToDoListDig toDoListDig) {

        this.toDoListDig = toDoListDig;
        this.context = context;
        this.list = list;
        this.type = type;
        baseActivity = (BaseActivity) context;
        mNetworkManager = baseActivity.getAppliction().getNetworkManager();
        this.isFromTodo = true;
    }

    public FlowAdapter(Fragment fragment, List<Flow> list, int type) {

        this.fragment = fragment;
        this.context = fragment.getActivity();
        this.list = list;
        this.type = type;
        baseActivity = (BaseActivity) context;
        mNetworkManager = baseActivity.getAppliction().getNetworkManager();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        mPosition = position;
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.flow_list_item, null);

            mViewHolder.imageGridView = (NoScrollGridview) convertView.findViewById(R.id.imagegridview);
            mViewHolder.voiceListview = (NoScrollListview) convertView.findViewById(R.id.voice_listview);
            mViewHolder.revocationclickLayout = (RelativeLayout) convertView.findViewById(R.id.revocation_click);
            mViewHolder.agreeClickLayout = (RelativeLayout) convertView.findViewById(R.id.agree_click);
            mViewHolder.disagreeClickLayout = (RelativeLayout) convertView.findViewById(R.id.disagress_click);
            mViewHolder.commentClickLayout = (RelativeLayout) convertView.findViewById(R.id.comment_click);
            mViewHolder.praiseClickLayout = (RelativeLayout) convertView.findViewById(R.id.praise_click);
            mViewHolder.moreClickLayout = (RelativeLayout) convertView.findViewById(R.id.more_click);
            mViewHolder.userPhotoView = (SimpleDraweeView) convertView.findViewById(R.id.user_photo);
            mViewHolder.userNameView = (TextView) convertView.findViewById(R.id.user_name);
            mViewHolder.timestampView = (TextView) convertView.findViewById(R.id.timestamp);
            mViewHolder.contentTextView = (TextView) convertView.findViewById(R.id.content_text);

            EditUtils.SetTextViewMaxLines(mViewHolder.contentTextView, 5);

//            mViewHolder.approvalTextViewName = (TextView) convertView.findViewById(R.id.reciver_names);
            mViewHolder.commentNumView = (TextView) convertView.findViewById(R.id.comment_num);
            mViewHolder.praiseNumView = (TextView) convertView.findViewById(R.id.praise_num);
            mViewHolder.moreNumView = (TextView) convertView.findViewById(R.id.more_num);

            mViewHolder.commentIconView = (ImageView) convertView.findViewById(R.id.comment_img);
            mViewHolder.praiseIconView = (ImageView) convertView.findViewById(R.id.schedule_list_item_praise_icon);
            mViewHolder.moreIconView = (ImageView) convertView.findViewById(R.id.more_image);
            mViewHolder.commentClickLayout.setOnClickListener(this);
            mViewHolder.praiseClickLayout.setOnClickListener(this);
            mViewHolder.moreClickLayout.setOnClickListener(this);
//            mViewHolder.approvalTextViewName.setOnClickListener(this);
            mViewHolder.voiceListview = (NoScrollListview) convertView.findViewById(R.id.voice_listview);
            AudioListAdapter audioListAdapter = new AudioListAdapter(context, this);
            mViewHolder.voiceListview.setAdapter(audioListAdapter);
            mViewHolder.finishLogoView = (ImageView) convertView.findViewById(R.id.finish_logo);
            mViewHolder.timeLayout = convertView.findViewById(R.id.time_layout);
            convertView.setTag(R.id.day_text, mViewHolder);
            mViewHolder.operationTag = (ImageView) convertView.findViewById(R.id.operation);
            mViewHolder.operationTv = (TextView) convertView.findViewById(R.id.operation_tv);

            mViewHolder.resumeInfoLayout = (RelativeLayout) convertView.findViewById(R.id.resumeinfo);
            mViewHolder.amountView = (TextView) convertView.findViewById(R.id.amount);
            mViewHolder.resumeCountView = (TextView) convertView.findViewById(R.id.resume_count);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag(R.id.day_text);
        }

        mViewHolder.timeLayout.setVisibility(View.GONE);
        final Flow flow = list.get(position);
        String json = flow.getJson().toString();
        NewUser user = flow.getData(NewUser.class);
        if (flow.comments > 0)
            mViewHolder.commentNumView.setText(flow.comments + "");
        else
            mViewHolder.commentNumView.setText(context.getResources().getString(R.string.comment));
        if (flow.praises > 0)
            mViewHolder.praiseNumView.setText(flow.praises + "");
        else
            mViewHolder.praiseNumView.setText(context.getResources().getString(R.string.praise));

        mViewHolder.userNameView.setText(Html.fromHtml(user.name));
//        mViewHolder.timestampView.setDettailText(context.getResources().getString(R.string.create) + "：" + Utils.dateFormat(flow.timestamp));
        mViewHolder.timestampView.setText(Utils.getInstance(context).dateFormat(flow.timestamp));
//        mViewHolder.approvalTextView.setDettailText(context.getResources().getString(R.string.task_receiver) + "（" + flow.processerid + "）");

        String leaveinfo = Utils.formatHtmlLeaveInfoContent(context, flow.leaveinfo);
        if (!TextUtils.isEmpty(leaveinfo)) {
            mViewHolder.contentTextView.setText(EmojiFragment.getEmojiContent(context, mViewHolder.contentTextView.getTextSize(),Html.fromHtml(flow.content + "<br>" + leaveinfo)));
        } else {
            mViewHolder.contentTextView.setText(EmojiFragment.getEmojiContent(context, mViewHolder.contentTextView.getTextSize(),Html.fromHtml(flow.content)));
        }

        //点赞
        if (flow.ispraise) {
            mViewHolder.praiseIconView.setImageResource(R.drawable.item_praise_click_red);
            mViewHolder.praiseNumView.setTextColor(EditUtils.redCreateColorStateList());
        } else {
            mViewHolder.praiseIconView.setImageResource(R.drawable.item_praise_click);
            mViewHolder.praiseNumView.setTextColor(EditUtils.greyCreateColorStateList());
        }
        //2:撤销
        if ("2".equals(flow.repealstate)) {
            mViewHolder.operationTag.setVisibility(View.GONE);
            mViewHolder.operationTv.setVisibility(View.GONE);
            if (PrfUtils.isChineseForAppLanguage(context))
                mViewHolder.finishLogoView.setImageResource(R.mipmap.cancel_logo_ch);
            else
                mViewHolder.finishLogoView.setImageResource(R.mipmap.cancel_logo_en);
            mViewHolder.finishLogoView.setVisibility(View.VISIBLE);
            mViewHolder.commentNumView.setSelected(true);
            mViewHolder.praiseNumView.setSelected(true);
            mViewHolder.moreNumView.setSelected(true);
            mViewHolder.commentIconView.setSelected(true);
            mViewHolder.praiseIconView.setSelected(true);
            mViewHolder.moreIconView.setSelected(true);
        } else {
            mViewHolder.finishLogoView.setVisibility(View.GONE);
            mViewHolder.operationTag.setVisibility(View.VISIBLE);
            mViewHolder.operationTv.setVisibility(View.VISIBLE);
            //1同意，2不同意，3待审批，默认0全部
            if ("1".equals(flow.state)) {
                mViewHolder.operationTag.setImageResource(R.mipmap.schedule_agree_bg);
                mViewHolder.operationTv.setText(context.getResources().getString(R.string.agree) + "  ");
                mViewHolder.operationTv.setTextColor(context.getResources().getColor(R.color.schedule_agree_color));

            } else if ("2".equals(flow.state)) {
                mViewHolder.operationTag.setImageResource(R.mipmap.schedule_refuse_bg);
                mViewHolder.operationTv.setText(context.getResources().getString(R.string.disagree));
                mViewHolder.operationTv.setTextColor(context.getResources().getColor(R.color.schedule_refuse_color));

            } else if ("3".equals(flow.state)) {
                mViewHolder.operationTag.setImageResource(R.mipmap.schedule_undispose_bg);
                mViewHolder.operationTv.setText(context.getResources().getString(R.string.approvaling));
                mViewHolder.operationTv.setTextColor(context.getResources().getColor(R.color.schedule_undispose_color));
            }
//            1我发起的，2我审批的，3抄送我的
            switch (type) {
                case 2:
                    break;
                case 3:
                    mViewHolder.moreNumView.setSelected(true);
                    mViewHolder.moreIconView.setSelected(true);
                    break;
                default:
                    if ("3".equals(flow.state)) {
                        mViewHolder.moreNumView.setSelected(false);
                        mViewHolder.moreIconView.setSelected(false);
                    } else {
                        mViewHolder.moreNumView.setSelected(true);
                        mViewHolder.moreIconView.setSelected(true);
                    }
                    break;

            }


            if (type == 3) {
                mViewHolder.moreNumView.setSelected(true);
                mViewHolder.moreIconView.setSelected(true);
            } else {
                if ("3".equals(flow.state) && !(type == 1 && flow.resource != 1)) {
                    mViewHolder.moreNumView.setSelected(false);
                    mViewHolder.moreIconView.setSelected(false);
                } else {
                    mViewHolder.moreNumView.setSelected(true);
                    mViewHolder.moreIconView.setSelected(true);
                }
            }
            mViewHolder.commentNumView.setSelected(false);
            mViewHolder.praiseNumView.setSelected(false);
            mViewHolder.commentIconView.setSelected(false);
            mViewHolder.praiseIconView.setSelected(false);
        }
        List<ImageInfo> images = new ArrayList<>();
        List<AudioInfo> audios = new ArrayList<>();
        List<ResourceInfo> resourceInfos = new ArrayList<>();
        try {
            images = JsonDataFactory.getDataArray(ImageInfo.class, flow.getJson().getJSONArray("image"));
            audios = JsonDataFactory.getDataArray(AudioInfo.class, flow.getJson().getJSONArray("audio"));
            if (!TextUtils.isEmpty(flow.resourceinfo))
                resourceInfos = JsonDataFactory.getDataArray(ResourceInfo.class, new JSONArray(flow.resourceinfo));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (flow.resource == 4 && resourceInfos.size() > 0) {
            mViewHolder.resumeInfoLayout.setVisibility(View.VISIBLE);
            mViewHolder.resumeCountView.setText(Utils.format(context.getString(R.string.resumes_total), resourceInfos.get(0).resume_count));
            mViewHolder.amountView.setText(Utils.format(context.getString(R.string.order_total_01), resourceInfos.get(0).amount));

        } else
            mViewHolder.resumeInfoLayout.setVisibility(View.GONE);
        ImageOptions.setUserImage(mViewHolder.userPhotoView, user.photo);
        UserUtils.enterUserInfo(context, user.userid + "", user.name, user.photo, mViewHolder.userPhotoView);

        if (images.size() > 0) {
            mViewHolder.imageGridView.setVisibility(View.VISIBLE);
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(mViewHolder.imageGridView, context, images);
            mViewHolder.imageGridView.setAdapter(imageGridviewAdapter);
        } else {
            mViewHolder.imageGridView.setVisibility(View.GONE);
        }

        AudioListAdapter audioListAdapter = (AudioListAdapter) mViewHolder.voiceListview.getAdapter();
        if (audios.size() > 0) {

            audioListAdapter.dataSource.clear();
            audioListAdapter.dataSource.addAll(audios);
            audioListAdapter.notifyDataSetChanged();
            mViewHolder.voiceListview.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.voiceListview.setVisibility(View.GONE);
        }
//        List<NewUser> receiver = flow.getArrayData(NewUser.class);
//        if (receiver.size() > 0) {
//            String names = "";
//            for (NewUser nuser : receiver) {
//                if (TextUtils.isEmpty(names)) {
//                    names = names + nuser.name;
//                } else {
//                    names = names + "，" + nuser.name;
//                }
//            }
//            names = names + "（" + receiver.size() + "）";
//            mViewHolder.approvalTextViewName.setDettailText(Html.fromHtml(names));
////            mViewHolder.approvalTextView.setDettailText("（" + receiver.size() + "）");
////            mViewHolder.approvalTextView.setVisibility(View.VISIBLE);
//        } else {
//            mViewHolder.approvalTextViewName.setDettailText(R.string.no_time);
////            mViewHolder.approvalTextView.setVisibility(View.GONE);
//        }

//        mViewHolder.approvalTextViewName.setSingleLine(flow.singleline);
//        mViewHolder.approvalTextViewName.setTag(position);
        mViewHolder.moreClickLayout.setTag(position);
        mViewHolder.commentClickLayout.setTag(position);
        mViewHolder.praiseClickLayout.setTag(position);
//        mViewHolder.approvalTextViewName.setVisibility(View.GONE);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!"2".equals(flow.repealstate)) {
                    Intent intent = new Intent(context, FlowHandleActivity.class);
                    intent.putExtra(FlowHandleActivity.TYPER, type);
                    intent.putExtra(FlowHandleActivity.JSON, flow.getJson().toString());
                    intent.putExtra(FlowHandleActivity.FLOWID, flow.processid);
                    intent.putExtra(FlowHandleActivity.POSITION, position);
                    if (fragment != null) {
                        fragment.startActivityForResult(intent, 1);
                    } else {
                        baseActivity.startActivityForResult(intent, 1);
                    }
                }
            }
        });
        return convertView;
    }

    private View lastView;

    public View getLastView() {
        return lastView;
    }

    public void setLastView(View view) {
        lastView = view;
    }

    private MoreButtonPopupWindowFlow mMenuWindow;


    public void cancleFlow(String processid, final int position) {
        baseActivity.showLoadingDialog(context, context.getString(R.string.prompt_info_02));
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(context));
        params.put("tenantid", PrfUtils.getTenantId(context));
        params.put("processid", processid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_PROCESS_CANCEL), params, context);
        mNetworkManager.load(CALLBACK_FLOWCANCLE, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

                baseActivity.dismisLoadingDialog();
                boolean safe = ActivityUtils.prehandleNetworkData(context, this, callbackId, path, rootData, true);
                if (!safe) {
//                    Toast.makeText(context, "取消失败！", Toast.LENGTH_SHORT).show();
                    return;
                }
                switch (callbackId) {
                    case CALLBACK_FLOWCANCLE:
                        Toast.makeText(context, context.getString(R.string.cancel_success_info), Toast.LENGTH_SHORT).show();
                        chaneFlowState(position);
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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.reciver_names: {
                int position = (int) v.getTag();
                Flow flow = list.get(position);
                if (flow.singleline) {
                    flow.singleline = false;
                } else {
                    flow.singleline = true;
                }
                notifyDataSetChanged();
            }
            break;

            case R.id.comment_click: {
                final int myposition = (int) v.getTag();
                Flow flow = list.get(myposition);
                if (!"2".equals(flow.repealstate)) {
                    if (flow.comments > 0) {
                        Intent intent = new Intent(context, FlowHandleActivity.class);
                        intent.putExtra(FlowHandleActivity.TYPER, type);
                        intent.putExtra(FlowHandleActivity.JSON, flow.getJson().toString());
                        intent.putExtra(FlowHandleActivity.FLOWID, flow.processid);
                        intent.putExtra(FlowHandleActivity.POSITION, myposition);
                        intent.putExtra("showcomment", true);
                        if (fragment != null) {
                            fragment.startActivityForResult(intent, 1);
                        } else {
                            baseActivity.startActivityForResult(intent, 1);
                        }

                    } else {
                        PublishUtils.addComment(context, PublishUtils.COMMENTTYPE_FLOW, flow.processid + "", myposition);
                    }
                }
            }
            break;
            case R.id.praise_click: {
                final int position = (int) v.getTag();
                Flow flow = list.get(position);
                if (!"2".equals(flow.repealstate)) {
                    PublishUtils.toDig(context, flow.processid + "", PublishUtils.COMMENTTYPE_FLOW, flow.ispraise, new PublishUtils.DigCallBack() {
                        @Override
                        public void successful(boolean digType) {
                            chanePraiseNum(position, digType);
                            if (position == 0 && isFromTodo) {
                                toDoListDig.listHasDig();
                            }
                        }
                    });
                }
            }
            break;
            case R.id.more_click: {
                final int position = (int) v.getTag();
                final Flow flow = list.get(position);
                if (!(type == 1 && flow.resource != 1) && type != 3 && !"2".equals(flow.repealstate) && "3".equals(flow.state)) {

                    mMenuWindow = new MoreButtonPopupWindowFlow(context, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mMenuWindow != null)
                                mMenuWindow.dismiss();
                            switch (v.getId()) {
                                case R.id.agree_click: {//同意

//                                    switch (flow.resource) {
//
//                                        case 3:
//                                            PublishUtils.recruitApproveAction(context, 1, flow.resourceid);
//                                            break;
//                                        case 4:
//
//                                            PublishUtils.resumeApproveAction(context, 1, flow.resourceid);
//
//                                            break;
//
//                                        default:

                                    Intent intent = new Intent(context, NewPublishedActivity.class);
                                    intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_FLOW_CONDUCT);
                                    intent.putExtra(Constants.TYPE, Constants.AGREE);
                                    intent.putExtra("flowId", "" + flow.processid);
                                    context.startActivity(intent);

//                                            break;
//
//                                    }
                                }
                                break;
                                case R.id.cancel_click://撤销
                                    if (flow.resource == 1) {
                                        new AlertDialog(context).builder() .setTitle(context.getString(R.string.frends_tip))
                                                .setMsg(context.getString(R.string.cancel_flow))
                                                .setPositiveButton(context.getString(R.string.ok), new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        cancleFlow(flow.processid + "", position);
                                                    }
                                                }).setNegativeButton(context.getString(R.string.cancel), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        }).show();
                                    }
                                    break;
                                case R.id.disagree_click://不同意
//                                    switch (flow.resource) {
//                                        case 3:
//                                            PublishUtils.recruitApproveAction(context, 2, flow.resourceid);
//                                            break;
//                                        case 4:
//                                            PublishUtils.resumeApproveAction(context, 2, flow.resourceid);
//                                            break;
//                                        default:
                                    Intent intent = new Intent(context, NewPublishedActivity.class);
                                    intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_FLOW_CONDUCT);
                                    intent.putExtra(Constants.TYPE, Constants.UNAGREE);
                                    intent.putExtra("flowId", "" + flow.processid);
                                    context.startActivity(intent);
//                                            break;
//                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }, type);

//                //显示窗口
                    mMenuWindow.show();
                }
            }
            break;
        }

    }

    private class ViewHolder {
        RelativeLayout praiseClickLayout;
        RelativeLayout commentClickLayout;
        RelativeLayout agreeClickLayout;
        RelativeLayout revocationclickLayout;
        RelativeLayout disagreeClickLayout;
        RelativeLayout moreClickLayout;
        SimpleDraweeView userPhotoView;
        TextView userNameView;
        TextView timestampView;
        TextView contentTextView;
        //        TextView approvalTextViewName;
        NoScrollGridview imageGridView;
        NoScrollListview voiceListview;
        TextView commentNumView;
        TextView praiseNumView;
        TextView moreNumView;

        ImageView commentIconView;
        ImageView praiseIconView;
        ImageView moreIconView;

        ImageView finishLogoView;
        TextView operationTv;
        ImageView operationTag;
        View timeLayout;

        RelativeLayout resumeInfoLayout;
        TextView resumeCountView;
        TextView amountView;


    }

    private String getDateFormatString(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }

    public void myNotifyDataSetChanged(List<Flow> lists) {

        this.list = lists;
        notifyDataSetChanged();
    }

    public void chanePraiseNum(int position, boolean digType) {

        Flow flow = list.get(position);
        int num = flow.praises;
        if (digType)
            flow.praises = num - 1;
        else
            flow.praises = num + 1;
        flow.ispraise = !digType;
        try {
            flow.getJson().put("praises", flow.praises);
            flow.getJson().put("ispraise", !digType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void chaneFlowState(int position) {
        Flow flow = list.get(position);
        flow.repealstate = "2";
        try {
            flow.getJson().put("repealstate", "2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public List<Flow> getList() {
        return list;
    }

    public void chaneCommentNum(int position) {
        Flow flow = list.get(position);
        int num = flow.comments;
        flow.comments = num + 1;
        try {
            flow.getJson().put("comments", flow.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void chaneFlow(int position, JSONObject jsonObject) {
        try {
            if (!TextUtil.isEmpty(jsonObject.toString())) {
                Flow flow = JsonDataFactory.getData(Flow.class, jsonObject);
                list.remove(position);
                list.add(position, flow);
                notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }
}
