package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.ScheduleItem;
import com.vgtech.common.api.ScheduleReciver;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.module.schedule.ScheduleDetailActivity;
import com.vgtech.vancloud.ui.module.todo.ToDoListDig;
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
 * 日程列表适配器
 * Created by Duke on 2015/8/20.
 */

public class ScheduleAdapter extends BaseAdapter implements
        ViewListener, MoreButtonPopupWindow.CancelSchedule, MoreButtonPopupWindow.EditSchedule, HttpListener<String> {

    private final int GET_SCHEDULE_CANCEL = 1;
    private final int GET_SCHEDULE_DETAIL = 2;

    private List<ScheduleItem> list = new ArrayList<ScheduleItem>();
    private BaseActivity activity;
    private Fragment fragment;
    private int mPosition;
    private ScheduleItem scheduleItem;
    private MoreButtonPopupWindow menuWindow;

    private NetworkManager mNetworkManager;

    private List<ScheduleReciver> recivers;

    boolean isFromTodo = false;
    private ToDoListDig toDoListDig;

    public ScheduleAdapter(Fragment fragment, List<ScheduleItem> list) {

        this.fragment = fragment;
        this.activity = (BaseActivity) fragment.getActivity();
        this.list = list;
    }

    public ScheduleAdapter(Activity activity, List<ScheduleItem> list) {

//        this.fragment = fragment;
        this.activity = (BaseActivity) activity;
        this.list = list;
    }

    public ScheduleAdapter(Activity activity, List<ScheduleItem> list, ToDoListDig toDoListDig) {

        this.isFromTodo = true;
        this.toDoListDig = toDoListDig;
        this.activity = (BaseActivity) activity;
        this.list = list;
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

    public void clear() {
        this.list.clear();
        try {
            this.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        mPosition = position;
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(activity).inflate(R.layout.schedule_list_item, null);

            mViewHolder.imageGridView = (NoScrollGridview) convertView.findViewById(R.id.imagegridview);
            mViewHolder.voiceListview = (NoScrollListview) convertView.findViewById(R.id.voice_listview);
            mViewHolder.commentClickLayout = (RelativeLayout) convertView.findViewById(R.id.comment_click);
            mViewHolder.praiseClickLayout = (RelativeLayout) convertView.findViewById(R.id.praise_click);
            mViewHolder.moreClickLayout = (RelativeLayout) convertView.findViewById(R.id.more_click);
            mViewHolder.userPhotoView = (SimpleDraweeView) convertView.findViewById(R.id.user_photo);
            mViewHolder.userNameView = (TextView) convertView.findViewById(R.id.user_name);
            mViewHolder.timestampView = (TextView) convertView.findViewById(R.id.timestamp);
            mViewHolder.contentTextView = (TextView) convertView.findViewById(R.id.content_text);

            EditUtils.SetTextViewMaxLines(mViewHolder.contentTextView, 5);

            convertView.findViewById(R.id.time_layout).setVisibility(View.VISIBLE);
//            mViewHolder.leftTimeLayout = (LinearLayout) convertView.findViewById(R.id.left_time_layout);
            mViewHolder.leftTimeView = (TextView) convertView.findViewById(R.id.left_time_text);
//            mViewHolder.rightTimeLayout = (LinearLayout) convertView.findViewById(R.id.right_time_layout);
            mViewHolder.rightTimeView = (TextView) convertView.findViewById(R.id.right_time_text);
            mViewHolder.commentNumView = (TextView) convertView.findViewById(R.id.comment_num);
            mViewHolder.praiseNumView = (TextView) convertView.findViewById(R.id.praise_num);
            mViewHolder.moreNumView = (TextView) convertView.findViewById(R.id.more_num);
            mViewHolder.tv_address_name = (TextView) convertView.findViewById(R.id.tv_address_name);
            mViewHolder.status = (ImageView) convertView.findViewById(R.id.operation);
            mViewHolder.status_tv = (TextView) convertView.findViewById(R.id.operation_tv);

            mViewHolder.commentNumButton = (LinearLayout) convertView.findViewById(R.id.comment_num_button);
            mViewHolder.praiseNumButton = (LinearLayout) convertView.findViewById(R.id.praise_num_button);
            mViewHolder.moreButton = (LinearLayout) convertView.findViewById(R.id.more_button);

            mViewHolder.moreImage = (ImageView) convertView.findViewById(R.id.more_image);
            mViewHolder.moreNum = (TextView) convertView.findViewById(R.id.more_num);

            mViewHolder.scheduleListItemPraiseIcon = (ImageView) convertView.findViewById(R.id.schedule_list_item_praise_icon);
            mViewHolder.cancelView = (ImageView) convertView.findViewById(R.id.cancel_view);
            mViewHolder.tv_schedule_duration = (TextView) convertView.findViewById(R.id.tv_schedule_duration);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        final ScheduleItem s = (ScheduleItem) getItem(position);
        final List<ImageInfo> imags = s.getArrayData(ImageInfo.class);
        final List<AudioInfo> audios = s.getArrayData(AudioInfo.class);
        final NewUser user = s.getData(NewUser.class);
        ImageOptions.setUserImage(mViewHolder.userPhotoView, user.photo);
        mViewHolder.tv_schedule_duration.setText(Utils.getDuration(activity, s.starttime, s.endtime));
        if (0 == s.comments) {
            mViewHolder.commentNumView.setText(activity.getResources().getString(R.string.comment));
        } else {
            mViewHolder.commentNumView.setText(s.comments + "");
        }
        if (0 == s.praises) {
            mViewHolder.praiseNumView.setText(activity.getResources().getString(R.string.praise));
        } else {
            mViewHolder.praiseNumView.setText(s.praises + "");
        }
//        mViewHolder.moreNumView.setDettailText(activity.getResources().getString(R.string.more));


        mViewHolder.userNameView.setText(Html.fromHtml(user.name));

        mViewHolder.contentTextView.setText(EmojiFragment.getEmojiContent(activity,mViewHolder.contentTextView.getTextSize(), Html.fromHtml(s.getContent())));
        mViewHolder.timestampView.setText(Utils.dateFormatStr(s.starttime) + " -- " + Utils.dateFormatStr(s.endtime));
//        mViewHolder.timestampView.setDettailText(activity.getResources().getString(R.string.create) + "：" + Utils.dateFormat(s.timestamp));
        String adrName = s.getAddress();
        mViewHolder.tv_address_name.setText(adrName);
        mViewHolder.tv_address_name.setVisibility(TextUtils.isEmpty(adrName) ? View.GONE : View.VISIBLE);
        mViewHolder.leftTimeView.setText(activity.getString(R.string.schedule_create_time, Utils.getInstance(activity).dateFormat(s.timestamp)));
        mViewHolder.rightTimeView.setText(activity.getString(R.string.schedule_update_time, Utils.getInstance(activity).dateFormat(s.getUpdateTime())));
        mViewHolder.rightTimeView.setVisibility(s.getUpdateTime() == 0 ? View.GONE : View.VISIBLE);
        UserUtils.enterUserInfo(activity, user.userid + "", user.name, user.photo, mViewHolder.userPhotoView);

        if (imags != null && imags.size() > 0) {
            mViewHolder.imageGridView.setVisibility(View.VISIBLE);
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(mViewHolder.imageGridView, activity, imags);
            mViewHolder.imageGridView.setAdapter(imageGridviewAdapter);
        } else {
            mViewHolder.imageGridView.setVisibility(View.GONE);
        }


        if (audios != null && !audios.isEmpty()) {
            mViewHolder.voiceListview.setVisibility(View.VISIBLE);
            AudioListAdapter audioListAdapter = new AudioListAdapter(activity, this);
            audioListAdapter.dataSource.clear();
            audioListAdapter.dataSource.addAll(audios);
            audioListAdapter.notifyDataSetChanged();
            mViewHolder.voiceListview.setAdapter(audioListAdapter);
        } else {
            mViewHolder.voiceListview.setVisibility(View.GONE);

        }
        List<ScheduleReciver> recivers = null;
        try {
            recivers = JsonDataFactory.getDataArray(ScheduleReciver.class, new JSONObject(s.getJson().toString()).getJSONArray("receiver"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (user.userid.equals(PrfUtils.getUserId(activity)))
            s.deepPermission = ScheduleItem.HAS_PERMISSION;

        if (recivers != null && s.deepPermission == ScheduleItem.NO_COMPARE)
            for (ScheduleReciver sr : recivers) {
                if (sr.userid != null && sr.userid.equals(PrfUtils.getUserId(activity))) {
                    s.deepPermission = ScheduleItem.HAS_PERMISSION;
                    break;
                } else
                    s.deepPermission = ScheduleItem.NO_PERMISSION;
            }

//        if(s.deepPermission!=ScheduleItem.HAS_PERMISSION){
//            mViewHolder.moreButton.setSelected(true);
//        }

        if (PrfUtils.isChineseForAppLanguage(activity))
            mViewHolder.cancelView.setImageResource(R.mipmap.cancel_logo_ch);
        else
            mViewHolder.cancelView.setImageResource(R.mipmap.cancel_logo_en);

        if ("2".equals(s.getRepealstate())) {
            mViewHolder.cancelView.setVisibility(View.VISIBLE);
        } else if ("2".equals(s.signs)) {
            //已过期
            mViewHolder.cancelView.setVisibility(View.VISIBLE);
            if (PrfUtils.isChineseForAppLanguage(activity))
                mViewHolder.cancelView.setImageResource(R.mipmap.expired_logo_ch);
            else
                mViewHolder.cancelView.setImageResource(R.mipmap.expired_logo_en);
        } else {
            mViewHolder.cancelView.setVisibility(View.GONE);
        }
        ImageView status = mViewHolder.status;
        TextView status_tv = mViewHolder.status_tv;
        Context mContext = fragment.getContext();
        if (!"2".equals(s.getRepealstate()) && s.replyflag != null) {
//            if ("3".equals(scheduleItem.type)) {
            if ("3".equals(s.type)) {
                status.setVisibility(View.GONE);
                status_tv.setVisibility(View.GONE);
            } else if ("1".equals(s.replyflag)) {
                status.setVisibility(View.VISIBLE);
                status_tv.setVisibility(View.VISIBLE);
                status.setImageResource(R.mipmap.schedule_indeterminate_bg);
                status_tv.setText(mContext.getString(R.string.schedule_detail_watting) + "  ");
                status_tv.setTextColor(mContext.getResources().getColor(R.color.schedule_indeterminate_color));
            } else if ("2".equals(s.replyflag)) {
                status.setVisibility(View.VISIBLE);
                status_tv.setVisibility(View.VISIBLE);
                status.setImageResource(R.mipmap.schedule_refuse_bg);
                status_tv.setText(mContext.getString(R.string.schedule_detail_refuse) + "  ");
                status_tv.setTextColor(mContext.getResources().getColor(R.color.schedule_refuse_color));
            } else if ("3".equals(s.replyflag)) {
                status.setVisibility(View.VISIBLE);
                status_tv.setVisibility(View.VISIBLE);
                status.setImageResource(R.mipmap.schedule_agree_bg);
                status_tv.setText(mContext.getString(R.string.schedule_detail_agree) + "  ");
                status_tv.setTextColor(mContext.getResources().getColor(R.color.schedule_agree_color));
            } else if ("4".equals(s.replyflag)) {
                status.setVisibility(View.VISIBLE);
                status_tv.setVisibility(View.VISIBLE);
                status.setImageResource(R.mipmap.schedule_undispose_bg);
                status_tv.setText(mContext.getString(R.string.schedule_detail_not_deep));
                status_tv.setTextColor(mContext.getResources().getColor(R.color.schedule_undispose_color));
            } else {
                status.setVisibility(View.GONE);
                status_tv.setVisibility(View.GONE);
            }
        } else {
            status.setVisibility(View.GONE);
            status_tv.setVisibility(View.GONE);
        }

        if (s.ispraise) {
            mViewHolder.scheduleListItemPraiseIcon.setImageResource(R.drawable.item_praise_click_red);
            mViewHolder.praiseNumView.setTextColor(EditUtils.redCreateColorStateList());
        } else {
            mViewHolder.scheduleListItemPraiseIcon.setImageResource(R.drawable.item_praise_click);
            mViewHolder.praiseNumView.setTextColor(EditUtils.greyCreateColorStateList());
        }
        mViewHolder.moreClickLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("2".equals(s.replyflag) || "3".equals(s.replyflag)) {
                    showToast(activity.getResources().getString(R.string.tip_schedule_sure));
                    return;
                }
                if (!"2".equals(s.getRepealstate())) {

                    String userId = PrfUtils.getUserId(activity);
                    if (!user.userid.equals(userId))
                        menuWindow = new MoreButtonPopupWindow(activity, ScheduleAdapter.this, ScheduleAdapter.this, s, MoreButtonPopupWindow.OTHERS_SCHEDULE_DEEP);
                    else
                        menuWindow = new MoreButtonPopupWindow(activity, ScheduleAdapter.this, ScheduleAdapter.this, s, MoreButtonPopupWindow.MY_SCHEDULE_DEEP);
                    //显示窗口
                    menuWindow.show(); //设置layout在PopupWindow中显示的位置

                } else if ("3".equals(s.type)) {
                    showToast(activity.getResources().getString(R.string.schedule_underling_prompt));
                } else if ("2".equals(s.getRepealstate()))
                    showToast(activity.getResources().getString(R.string.schedule_cancel_prompt));
            }
        });

        mViewHolder.commentClickLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"2".equals(s.getRepealstate())) {
                    if (s.comments > 0) {
                        Intent intent = new Intent(activity, ScheduleDetailActivity.class);
                        intent.putExtra("data", s.getJson().toString());
                        intent.putExtra("position", position);
                        intent.putExtra("showcomment", true);
                        if (fragment != null)
                            fragment.startActivityForResult(intent, 1);
                        else
                            activity.startActivityForResult(intent, 1);
                    } else {
                        PublishUtils.addComment(activity, PublishUtils.COMMENTTYPE_SCHEDULE, s.scheduleid + "", position);
                    }
                } else if ("2".equals(s.getRepealstate()))
                    showToast(activity.getResources().getString(R.string.schedule_cancel_prompt));
            }
        });

        mViewHolder.praiseClickLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"2".equals(s.getRepealstate())) {
                    PublishUtils.toDig(activity, s.scheduleid + "", PublishUtils.COMMENTTYPE_SCHEDULE, s.ispraise, new PublishUtils.DigCallBack() {
                        @Override
                        public void successful(boolean digType) {
                            chanePraiseNum(position, digType);
                            if (position == 0 && isFromTodo) {
                                toDoListDig.listHasDig();
                            }
                        }
                    });
                } else if ("2".equals(s.getRepealstate()))
                    showToast(activity.getResources().getString(R.string.schedule_cancel_prompt));
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"2".equals(s.getRepealstate())) {
                    Intent intent = new Intent(activity, ScheduleDetailActivity.class);
                    intent.putExtra("data", s.getJson().toString());
                    intent.putExtra("position", position);

                    if (fragment != null)
                        fragment.startActivityForResult(intent, 1);
                    else
                        activity.startActivityForResult(intent, 1);
                }
            }
        });

        return convertView;
    }

    @Override
    public void editSchedule(ScheduleItem schedule) {
        loadScheduleInfo(schedule.scheduleid);
    }

    private class ViewHolder {

        RelativeLayout praiseClickLayout;
        RelativeLayout commentClickLayout;
        RelativeLayout moreClickLayout;
        SimpleDraweeView userPhotoView;
        TextView userNameView;
        TextView timestampView;
        TextView contentTextView;
        NoScrollGridview imageGridView;
        NoScrollListview voiceListview;
        TextView leftTimeView;
        TextView rightTimeView;
        TextView commentNumView;
        TextView praiseNumView;
        TextView moreNumView;
        TextView tv_address_name;

        LinearLayout commentNumButton;
        LinearLayout praiseNumButton;
        LinearLayout moreButton;

        ImageView moreImage;
        TextView moreNum;

        ImageView scheduleListItemPraiseIcon;

        ImageView status;
        TextView status_tv;
        ImageView cancelView;
        TextView tv_schedule_duration;
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

    @Override
    public void cancelScheule(final ScheduleItem schedule) {
        new AlertDialog(activity).builder().setTitle(activity.getString(R.string.frends_tip))
                .setMsg(activity.getString(R.string.cancle_schedule))
                .setPositiveButton(activity.getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadContactInfo(schedule.scheduleid);
                    }
                }).setNegativeButton(activity.getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    public List<ScheduleItem> getList() {
        return list;
    }

    public void myNotifyDataSetChanged(List<ScheduleItem> list) {

        this.list = list;
        notifyDataSetChanged();

    }

    //加载日程详情网络请求
    private void loadScheduleInfo(String id) {
        mNetworkManager = activity.getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(activity));
        params.put("tenantid", PrfUtils.getTenantId(activity));
        params.put("calendarid", id);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(activity, URLAddr.URL_SCHEDULE_DETAIL), params, activity);
        mNetworkManager.load(GET_SCHEDULE_DETAIL, path, this);
    }


    //网络请求
    private void loadContactInfo(String id) {
        mNetworkManager = activity.getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(activity));
        params.put("tenantid", PrfUtils.getTenantId(activity));
        params.put("calendarid", id);

        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(activity, URLAddr.URL_SCHEDULE_CANCEL), params, activity);
        mNetworkManager.load(GET_SCHEDULE_CANCEL, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        boolean mSafe = ActivityUtils.prehandleNetworkData(activity, this, callbackId, path, rootData, true);
        if (!mSafe) {
//            showProgress(mWaitView, false);
            return;
        }
        switch (callbackId) {
            case GET_SCHEDULE_CANCEL:
                try {
                    if (rootData.result)
                        Toast.makeText(activity, activity.getString(R.string.this_schedule_is_cancel), Toast.LENGTH_SHORT).show();
//                    else
//                        Toast.makeText(activity, activity.getString(R.string.this_schedule_is_cancel_fail), Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(Actions.ACTION_CANREFRESH));
                break;
            case GET_SCHEDULE_DETAIL:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    scheduleItem = JsonDataFactory.getData(ScheduleItem.class, resutObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(activity, NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_SCHEDULE_UPDATE);
                intent.putExtra("scheduleInfo", scheduleItem.getJson().toString());
                activity.startActivity(intent);
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    public void chanePraiseNum(int position, boolean type) {

        try {
            ScheduleItem s = list.get(position);
            int num = s.praises;
            if (type)
                s.praises = num - 1;
            else
                s.praises = num + 1;
            if (s.praises < 0)
                s.praises = 0;
            s.ispraise = !type;
            list.get(position).getJson().put("praises", list.get(position).praises);
            list.get(position).getJson().put("ispraise", !type);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chaneCommentNum(int position) {
        if (position >= list.size())
            return;
        ScheduleItem item = list.get(position);
        int num = item.comments;
        item.comments = num + 1;
        try {
            item.getJson().put("comments", item.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void chaneScheduleState(int position, int type) {
        if (position == -1 || type == -1)
            return;
        if (type != -2) {
            ScheduleItem item = list.get(position);
            item.status = type + "";
            try {
                item.getJson().put("status", type + "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            ScheduleItem item = list.get(position);
            item.repealstate = "2";
            try {
                item.getJson().put("repealstate", "2");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        notifyDataSetChanged();
    }

    public void chaneTask(int position, JSONObject jsonObject) {
        try {
            if (!TextUtil.isEmpty(jsonObject.toString())) {
                ScheduleItem task = JsonDataFactory.getData(ScheduleItem.class, jsonObject);
                list.remove(position);
                list.add(position, task);
                notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showToast(String msg) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }
}
