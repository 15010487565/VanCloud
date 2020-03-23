package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.CommentInfo;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.Processer;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.WorkReport;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.module.todo.ToDoListDig;
import com.vgtech.vancloud.ui.module.workreport.WorkReportTransactActivity;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.ui.view.MoreButtonPopupWindow;
import com.vgtech.vancloud.utils.EditUtils;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Duke on 2015/9/11.
 */
public class WorkReportAdapter extends BaseAdapter implements View.OnClickListener, ViewListener {


    Context context;
    List<WorkReport> list = new ArrayList<WorkReport>();
    MoreButtonPopupWindow mMenuWindow;

    private static final int CALLBACK_WORKREPORTCANCLE = 1;
    private NetworkManager mNetworkManager;
    private BaseActivity baseActivity;
    private Fragment fragment;

    boolean isFromTodo = false;
    private ToDoListDig toDoListDig;

    public WorkReportAdapter(Fragment fragment, Context context, List<WorkReport> list) {
        this.fragment = fragment;
        this.context = context;
        this.list = list;
        baseActivity = (BaseActivity) context;
        mNetworkManager = baseActivity.getAppliction().getNetworkManager();
    }

    public WorkReportAdapter(Context context, List<WorkReport> list) {
        this.context = context;
        this.list = list;
        baseActivity = (BaseActivity) context;
        mNetworkManager = baseActivity.getAppliction().getNetworkManager();
    }

    public WorkReportAdapter(Context context, List<WorkReport> list, ToDoListDig toDoListDig) {
        this.toDoListDig = toDoListDig;
        this.isFromTodo = true;
        this.context = context;
        this.list = list;
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
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.work_report_list_item, null);

            mViewHolder.commentClickLayout = (RelativeLayout) convertView.findViewById(R.id.comment_click);
            mViewHolder.praiseClickLayout = (RelativeLayout) convertView.findViewById(R.id.praise_click);
            mViewHolder.moreClickLayout = (RelativeLayout) convertView.findViewById(R.id.more_click);

            mViewHolder.contentTextView = (TextView) convertView.findViewById(R.id.content_text);

            EditUtils.SetTextViewMaxLines(mViewHolder.contentTextView, 5);

            mViewHolder.imageGridView = (NoScrollGridview) convertView.findViewById(R.id.imagegridview);
            mViewHolder.timestampView = (TextView) convertView.findViewById(R.id.timestamp);
            mViewHolder.userNameView = (TextView) convertView.findViewById(R.id.user_name);
            mViewHolder.userPhotoView = (SimpleDraweeView) convertView.findViewById(R.id.user_photo);

            mViewHolder.workreportTypeTopView = (TextView) convertView.findViewById(R.id.workreport_type_top);
            mViewHolder.timeLayout = convertView.findViewById(R.id.time_layout);

            mViewHolder.commentNumView = (TextView) convertView.findViewById(R.id.comment_num);
            mViewHolder.praiseNumView = (TextView) convertView.findViewById(R.id.praise_num);
            mViewHolder.moreNumView = (TextView) convertView.findViewById(R.id.more_num);
            mViewHolder.workReportTitleView = (TextView) convertView.findViewById(R.id.work_report_title);
            mViewHolder.voiceListview = (NoScrollListview) convertView.findViewById(R.id.voice_listview);

            mViewHolder.commentIconView = (ImageView) convertView.findViewById(R.id.comment_img);
            mViewHolder.praiseIconView = (ImageView) convertView.findViewById(R.id.schedule_list_item_praise_icon);
            mViewHolder.moreIconView = (ImageView) convertView.findViewById(R.id.more_image);
            mViewHolder.finishLogoView = (ImageView) convertView.findViewById(R.id.finish_logo);
            AudioListAdapter audioListAdapter = new AudioListAdapter(context, this);
            mViewHolder.voiceListview.setAdapter(audioListAdapter);
            mViewHolder.commentClickLayout.setOnClickListener(this);
            mViewHolder.praiseClickLayout.setOnClickListener(this);
            mViewHolder.moreClickLayout.setOnClickListener(this);
            convertView.setTag(R.id.day_text, mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag(R.id.day_text);
        }

        mViewHolder.timeLayout.setVisibility(View.GONE);
        mViewHolder.workReportTitleView.setVisibility(View.VISIBLE);

        final WorkReport workReport = list.get(position);
        String json = workReport.getJson().toString();

        NewUser user = workReport.getData(NewUser.class);
        if (workReport.comments > 0)
            mViewHolder.commentNumView.setText(workReport.comments + "");
        else
            mViewHolder.commentNumView.setText(context.getResources().getString(R.string.comment));
        if (workReport.praises > 0)
            mViewHolder.praiseNumView.setText(workReport.praises + "");
        else
            mViewHolder.praiseNumView.setText(context.getResources().getString(R.string.praise));

        mViewHolder.userNameView.setText(Html.fromHtml(user.name));
//        mViewHolder.timestampView.setDettailText(context.getResources().getString(R.string.create) + "：" + Utils.dateFormat(workReport.timestamp));

        mViewHolder.timestampView.setText(Utils.getInstance(context).dateFormat(workReport.timestamp));

        if (TextUtils.isEmpty(workReport.content)) {
            mViewHolder.contentTextView.setVisibility(View.GONE);
        } else {
            mViewHolder.contentTextView.setVisibility(View.VISIBLE);
            final String content = Utils.formatHtmlWorkReportContent(workReport.content);
            mViewHolder.contentTextView.setText(EmojiFragment.getEmojiContent(context, mViewHolder.contentTextView.getTextSize(),Html.fromHtml(content)));
        }

        mViewHolder.workReportTitleView.setText(workReport.title);

        ImageOptions.setUserImage(mViewHolder.userPhotoView, user.photo);
        UserUtils.enterUserInfo(context, user.userid + "", user.name, user.photo, mViewHolder.userPhotoView);

        mViewHolder.workreportTypeTopView.setText(context.getResources().getString(R.string.work_report_type));
        //工作汇报类型
        if ("1".equals(workReport.type)) {
            mViewHolder.workreportTypeTopView.setText(mViewHolder.workreportTypeTopView.getText().toString() + context.getResources().getString(R.string.daily_paper));

        } else if ("2".equals(workReport.type)) {
            mViewHolder.workreportTypeTopView.setText(mViewHolder.workreportTypeTopView.getText().toString() + context.getResources().getString(R.string.weekly_paper));

        } else if ("3".equals(workReport.type)) {
            mViewHolder.workreportTypeTopView.setText(mViewHolder.workreportTypeTopView.getText().toString() + context.getResources().getString(R.string.monthly_paper));
        }
        //点赞
        if (workReport.ispraise) {
            mViewHolder.praiseIconView.setImageResource(R.drawable.item_praise_click_red);
            mViewHolder.praiseNumView.setTextColor(EditUtils.redCreateColorStateList());
        } else {
            mViewHolder.praiseIconView.setImageResource(R.drawable.item_praise_click);
            mViewHolder.praiseNumView.setTextColor(EditUtils.greyCreateColorStateList());
        }
        //"2", 代表撤销  1 代表正常使用
        if ("2".equals(workReport.repealstate)) {
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

            mViewHolder.commentNumView.setSelected(false);
            mViewHolder.praiseNumView.setSelected(false);
            mViewHolder.commentIconView.setSelected(false);
            mViewHolder.praiseIconView.setSelected(false);

            if (workReport.subtype == 2 || workReport.subtype == 3) {
                mViewHolder.moreNumView.setSelected(true);
                mViewHolder.moreIconView.setSelected(true);
            } else {
                Processer leader = workReport.getData(Processer.class);
                CommentInfo commentInfo = leader.getData(CommentInfo.class);
                if ("1".equals(commentInfo.state)) {
                    mViewHolder.moreNumView.setSelected(false);
                    mViewHolder.moreIconView.setSelected(false);
                } else {
                    if (PrfUtils.isChineseForAppLanguage(context))
                        mViewHolder.finishLogoView.setImageResource(R.mipmap.comment_logo_ch);
                    else
                        mViewHolder.finishLogoView.setImageResource(R.mipmap.comment_logo_en);
                    mViewHolder.finishLogoView.setVisibility(View.VISIBLE);
                    mViewHolder.moreNumView.setSelected(true);
                    mViewHolder.moreIconView.setSelected(true);
                }
            }
        }

        List<ImageInfo> images = new ArrayList<>();
        List<AudioInfo> audios = new ArrayList<>();
        try {
            images = JsonDataFactory.getDataArray(ImageInfo.class, workReport.getJson().getJSONArray("image"));
            audios = JsonDataFactory.getDataArray(AudioInfo.class, workReport.getJson().getJSONArray("audio"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

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


        mViewHolder.workreportTypeTopView.setVisibility(View.VISIBLE);
        mViewHolder.commentClickLayout.setTag(position);
        mViewHolder.praiseClickLayout.setTag(position);
        mViewHolder.moreClickLayout.setTag(position);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!"2".equals(workReport.repealstate)) {
                    Intent intent = new Intent(context, WorkReportTransactActivity.class);
                    intent.putExtra(WorkReportTransactActivity.JSON, workReport.getJson().toString());
                    intent.putExtra(WorkReportTransactActivity.WORKREPORTID, workReport.workreportid);
                    intent.putExtra("position", position);
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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.comment_click: {
                final int position = (int) v.getTag();
                WorkReport workreport = list.get(position);
                if (!"2".equals(workreport.repealstate)) {
                    if (workreport.comments > 0) {

                        Intent intent = new Intent(context, WorkReportTransactActivity.class);
                        intent.putExtra(WorkReportTransactActivity.JSON, workreport.getJson().toString());
                        intent.putExtra(WorkReportTransactActivity.WORKREPORTID, workreport.workreportid);
                        intent.putExtra("position", position);
                        intent.putExtra("showcomment", true);

                        if (fragment != null) {
                            fragment.startActivityForResult(intent, 1);
                        } else {
                            baseActivity.startActivityForResult(intent, 1);
                        }

                    } else {
                        PublishUtils.addComment(context, PublishUtils.COMMENTTYPE_WORKREPORT, "" + workreport.workreportid, position);
                    }
                } else
                    showToast(context.getString(R.string.workreport_cancel_prompt));

            }
            break;
            case R.id.praise_click:
                final int position = (int) v.getTag();
                WorkReport workreport = list.get(position);
                if (!"2".equals(workreport.repealstate)) {
                    PublishUtils.toDig(context, workreport.workreportid, PublishUtils.COMMENTTYPE_WORKREPORT, workreport.ispraise, new PublishUtils.DigCallBack() {
                        @Override
                        public void successful(boolean digType) {
                            chanePraiseNum(position, digType);
                            if (position == 0 && isFromTodo) {
                                toDoListDig.listHasDig();
                            }
                        }
                    });

                } else
                    showToast(context.getString(R.string.workreport_cancel_prompt));
                break;
            case R.id.more_click:

                final int positionid = (int) v.getTag();
                final WorkReport workreport1 = list.get(positionid);
                Processer leader = workreport1.getData(Processer.class);
                CommentInfo commentInfo = leader.getData(CommentInfo.class);
                //（0我点评的，1我发出的，2抄送给我的,3和我无关的）
                if (workreport1.subtype != 2 && workreport1.subtype != 3 && "1".equals(commentInfo.state) && !"2".equals(workreport1.repealstate)) {

                    mMenuWindow = new MoreButtonPopupWindow(context, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mMenuWindow != null)
                                mMenuWindow.dismiss();
                            switch (v.getId()) {
                                case R.id.comment_click:
                                    Intent intent = new Intent(context, NewPublishedActivity.class);
                                    intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_WORK_REPORT);
                                    intent.putExtra("workReportId", workreport1.workreportid);
                                    context.startActivity(intent);
                                    break;

                                case R.id.cancel_click:
                                    new AlertDialog(context).builder() .setTitle(context.getString(R.string.frends_tip))
                                            .setMsg(context.getString(R.string.cancel_workreport))
                                            .setPositiveButton(context.getString(R.string.ok), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    cancleWorkReport(workreport1.workreportid, positionid);
                                                }
                                            }).setNegativeButton(context.getString(R.string.cancel), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                        }
                                    }).show();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }, workreport1.subtype + "");

                    //显示窗口
                    mMenuWindow.show();

                } else {
                    if ("2".equals(workreport1.repealstate))
                        showToast(context.getString(R.string.workreport_cancel_prompt));
                    else if (!"1".equals(commentInfo.state))
                        showToast(context.getString(R.string.workreport_finish_prompt));
                    else if (workreport1.subtype == 2)
                        showToast(context.getString(R.string.cc_prompt));
                    else if (workreport1.subtype == 3)
                        showToast(context.getString(R.string.workreport_underling_prompt));
                }
                break;
        }
    }


    private View lastView;

    @Override
    public View getLastView() {
        return lastView;
    }

    @Override
    public void setLastView(View view) {
        lastView = view;
    }


    private class ViewHolder {

        RelativeLayout praiseClickLayout;
        RelativeLayout commentClickLayout;
        RelativeLayout moreClickLayout;
        NoScrollGridview imageGridView;
        TextView contentTextView;
        TextView timestampView;
        TextView userNameView;
        SimpleDraweeView userPhotoView;
        TextView workreportTypeTopView;
        View timeLayout;
        TextView workReportTitleView;
        TextView commentNumView;
        TextView praiseNumView;
        TextView moreNumView;
        NoScrollListview voiceListview;
        ImageView commentIconView;
        ImageView praiseIconView;
        ImageView moreIconView;
        ImageView finishLogoView;
    }

    public void myNotifyDataSetChanged(List<WorkReport> lists) {
        this.list = lists;
        notifyDataSetChanged();
    }

    public List<WorkReport> getlist() {
        return list;
    }

    public void chanePraiseNum(int position, boolean digType) {

        WorkReport workReport = list.get(position);
        int num = workReport.praises;

        if (digType)
            workReport.praises = num - 1;
        else
            workReport.praises = num + 1;
        if (workReport.praises < 0)
            workReport.praises = 0;
        workReport.ispraise = !digType;
        try {
            workReport.getJson().put("praises", workReport.praises);
            workReport.getJson().put("ispraise", !digType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void chaneWorkReportState(int position) {
        WorkReport workReport = list.get(position);
        workReport.repealstate = "2";
        try {
            workReport.getJson().put("repealstate", "2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void chaneCommentNum(int position) {
        WorkReport workReport = list.get(position);
        int num = workReport.comments;
        workReport.comments = num + 1;
        try {
            workReport.getJson().put("comments", workReport.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void cancleWorkReport(String workreportid, final int position) {
        baseActivity.showLoadingDialog(context, context.getString(R.string.prompt_info_02));
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(context));
        params.put("tenantid", PrfUtils.getTenantId(context));
        params.put("workreportid", workreportid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_WORKREPORT_BACKOUT), params, context);
        mNetworkManager.load(CALLBACK_WORKREPORTCANCLE, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

                baseActivity.dismisLoadingDialog();
                boolean safe = ActivityUtils.prehandleNetworkData(context, this, callbackId, path, rootData, true);
                if (!safe) {
//                    Toast.makeText(context, "取消失败！", Toast.LENGTH_SHORT).show();
                    return;
                }
                switch (callbackId) {
                    case CALLBACK_WORKREPORTCANCLE:
                        Toast.makeText(context, context.getString(R.string.cancel_success), Toast.LENGTH_SHORT).show();
                        chaneWorkReportState(position);
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

    public void chaneWorkReport(int position, JSONObject jsonObject) {
        try {
            if (!TextUtil.isEmpty(jsonObject.toString())) {
                WorkReport workReport = JsonDataFactory.getData(WorkReport.class, jsonObject);
                list.remove(position);
                list.add(position, workReport);
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

    public void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
