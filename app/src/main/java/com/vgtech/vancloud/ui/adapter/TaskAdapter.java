package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.Task;
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
import com.vgtech.vancloud.ui.module.task.TaskTransactActivity;
import com.vgtech.vancloud.ui.module.todo.ToDoListDig;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.ui.view.MoreButtonPopupWindow;
import com.vgtech.vancloud.utils.EditUtils;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务列表适配器
 * Created by Duke on 2015/8/20.
 */

public class TaskAdapter extends BaseAdapter implements View.OnClickListener, ViewListener {

    Context context;
    private boolean type;//true 抄送我的

    public List<Task> getMlist() {
        return mlist;
    }

    List<Task> mlist;
    int mPosition;

    private static final int CALLBACK_TASKCANCLE = 1;
    private NetworkManager mNetworkManager;
    private BaseActivity baseActivity;

    boolean isFromTodo = false;
    private ToDoListDig toDoListDig;

    public TaskAdapter(Context context, List<Task> list) {
        this.context = context;
        this.mlist = list;
        baseActivity = (BaseActivity) context;
        mNetworkManager = baseActivity.getAppliction().getNetworkManager();
    }

    public TaskAdapter(Context context, List<Task> list, ToDoListDig toDoListDig) {
        this.context = context;
        this.mlist = list;
        this.toDoListDig = toDoListDig;
        this.isFromTodo = true;
        baseActivity = (BaseActivity) context;
        mNetworkManager = baseActivity.getAppliction().getNetworkManager();
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        mPosition = position;
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.task_list_item, null);

            mViewHolder.imageGridView = (NoScrollGridview) convertView.findViewById(R.id.imagegridview);
            mViewHolder.commentClickLayout = (RelativeLayout) convertView.findViewById(R.id.comment_click);
            mViewHolder.praiseClickLayout = (RelativeLayout) convertView.findViewById(R.id.praise_click);
            mViewHolder.moreClickLayout = (RelativeLayout) convertView.findViewById(R.id.more_click);
            mViewHolder.userPhotoView = (SimpleDraweeView) convertView.findViewById(R.id.user_photo);
            mViewHolder.userNameView = (TextView) convertView.findViewById(R.id.user_name);
            mViewHolder.timestampView = (TextView) convertView.findViewById(R.id.timestamp);
            mViewHolder.contentTextView = (TextView) convertView.findViewById(R.id.content_text);
            convertView.findViewById(R.id.time_layout).setVisibility(View.VISIBLE);
            EditUtils.SetTextViewMaxLines(mViewHolder.contentTextView, 5);

            mViewHolder.leftTimeView = (TextView) convertView.findViewById(R.id.left_time_text);
            mViewHolder.rightTimeView = (TextView) convertView.findViewById(R.id.right_time_text);
            mViewHolder.commentNumView = (TextView) convertView.findViewById(R.id.comment_num);
            mViewHolder.praiseNumView = (TextView) convertView.findViewById(R.id.praise_num);
            mViewHolder.finishLogoView = (ImageView) convertView.findViewById(R.id.finish_logo);
            mViewHolder.moreNumView = (TextView) convertView.findViewById(R.id.more_num);
            mViewHolder.moreNumView.setTag(true);

            mViewHolder.commentClickLayout.setOnClickListener(this);
            mViewHolder.praiseClickLayout.setOnClickListener(this);
            mViewHolder.moreClickLayout.setOnClickListener(this);
            mViewHolder.voiceListview = (NoScrollListview) convertView.findViewById(R.id.voice_listview);
            AudioListAdapter audioListAdapter = new AudioListAdapter(context, this);
            mViewHolder.voiceListview.setAdapter(audioListAdapter);

            mViewHolder.commentIconView = (ImageView) convertView.findViewById(R.id.comment_img);
            mViewHolder.praiseIconView = (ImageView) convertView.findViewById(R.id.schedule_list_item_praise_icon);
            mViewHolder.moreIconView = (ImageView) convertView.findViewById(R.id.more_image);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        final Task task = mlist.get(position);

        String json = task.getJson().toString();
        NewUser user = task.getData(NewUser.class);
        if (task.comments > 0)
            mViewHolder.commentNumView.setText(task.comments + "");
        else
            mViewHolder.commentNumView.setText(context.getResources().getString(R.string.comment));
        if (task.praises > 0)
            mViewHolder.praiseNumView.setText(task.praises + "");
        else
            mViewHolder.praiseNumView.setText(context.getResources().getString(R.string.praise));
        mViewHolder.userNameView.setText(Html.fromHtml(user.name));
//        mViewHolder.timestampView.setDettailText(context.getResources().getString(R.string.create) + "：" + Utils.dateFormat(task.timestamp));
        mViewHolder.timestampView.setText(Utils.getInstance(context).dateFormat(task.timestamp));

        mViewHolder.contentTextView.setText(EmojiFragment.getEmojiContent(context, mViewHolder.contentTextView.getTextSize(),Html.fromHtml(task.content)));

//        Html.escapeHtml(EmojiFragment.getEmojiContent(context, task.content))
        mViewHolder.leftTimeView.setText(context.getResources().getString(R.string.plant_) + "：" + Utils.getInstance(context).dateFormat(task.plantime));
        mViewHolder.rightTimeView.setVisibility(View.GONE);

        if (task.ispraise) {
            mViewHolder.praiseIconView.setImageResource(R.drawable.item_praise_click_red);
            mViewHolder.praiseNumView.setTextColor(EditUtils.redCreateColorStateList());
        } else {
            mViewHolder.praiseIconView.setImageResource(R.drawable.item_praise_click);
            mViewHolder.praiseNumView.setTextColor(EditUtils.greyCreateColorStateList());
        }
        //"2", 代表撤销  1 代表正常使用
        if ("2".equals(task.repealstate) || "2".equals(task.iscanviewdetail)) {
//            if ("2".equals(task.repealstate)) {
            if (PrfUtils.isChineseForAppLanguage(context))
                mViewHolder.finishLogoView.setImageResource(R.mipmap.cancel_logo_ch);
            else
                mViewHolder.finishLogoView.setImageResource(R.mipmap.cancel_logo_en);
            mViewHolder.finishLogoView.setVisibility(View.VISIBLE);
//            }
            mViewHolder.commentNumView.setSelected(true);
            mViewHolder.praiseNumView.setSelected(true);
            mViewHolder.moreNumView.setSelected(true);
            mViewHolder.commentIconView.setSelected(true);
            mViewHolder.praiseIconView.setSelected(true);
            mViewHolder.moreIconView.setSelected(true);

        } else {
            //2代表 未完成  1 代表 已完成
            if ("1".equals(task.state)) {
                if (PrfUtils.isChineseForAppLanguage(context))
                    mViewHolder.finishLogoView.setImageResource(R.mipmap.finish_logo_ch);
                else
                    mViewHolder.finishLogoView.setImageResource(R.mipmap.finish_logo_en);
                mViewHolder.finishLogoView.setVisibility(View.VISIBLE);
                mViewHolder.rightTimeView.setText(context.getResources().getString(R.string.finish_time) + "：" + Utils.getInstance(context).dateFormat(task.finishtime));
                mViewHolder.rightTimeView.setVisibility(View.VISIBLE);
                mViewHolder.commentNumView.setSelected(false);
                mViewHolder.praiseNumView.setSelected(false);
                mViewHolder.moreNumView.setSelected(true);
                mViewHolder.commentIconView.setSelected(false);
                mViewHolder.praiseIconView.setSelected(false);
                mViewHolder.moreIconView.setSelected(true);
            } else {
                mViewHolder.finishLogoView.setVisibility(View.GONE);
                //type = 3代表抄送给我的
                if (task.type != 3 && task.type != 4 && !(task.type == 2 && task.resource == 2)) {
                    mViewHolder.moreNumView.setSelected(false);
                    mViewHolder.moreIconView.setSelected(false);
                } else {
                    mViewHolder.moreNumView.setSelected(true);
                    mViewHolder.moreIconView.setSelected(true);
                }
                mViewHolder.commentNumView.setSelected(false);
                mViewHolder.praiseNumView.setSelected(false);
                mViewHolder.commentIconView.setSelected(false);
                mViewHolder.praiseIconView.setSelected(false);
            }
        }

        List<ImageInfo> images = new ArrayList<>();
        List<AudioInfo> audios = new ArrayList<>();
        try {
//            Log.e("TAG_任务","task="+task.getJson());
            if (!TextUtils.isEmpty(task.getJson().getString("audio"))) {
                audios = JsonDataFactory.getDataArray(AudioInfo.class, task.getJson().getJSONArray("audio"));
            }
            if (!TextUtils.isEmpty(task.getJson().getString("image"))) {
                images = JsonDataFactory.getDataArray(ImageInfo.class, task.getJson().getJSONArray("image"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        GenericDraweeHierarchy hierarchy = mViewHolder.userPhotoView.getHierarchy();
        hierarchy.setPlaceholderImage(R.mipmap.user_photo_default_small);
        hierarchy.setFailureImage(R.mipmap.user_photo_default_small);
        mViewHolder.userPhotoView.setImageURI(user.photo);
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


        mViewHolder.moreClickLayout.setTag(R.id.day, position);
        mViewHolder.commentClickLayout.setTag(position);
        mViewHolder.praiseClickLayout.setTag(position);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ("1".equals(task.iscanviewdetail) && !"2".equals(task.repealstate)) {
                    Intent intent = new Intent(context, TaskTransactActivity.class);
                    intent.putExtra("TaskID", task.taskid);
                    intent.putExtra("Task", task.getJson().toString());
                    intent.putExtra("position", position);
                    baseActivity.startActivityForResult(intent, 1);
                }
            }
        });

        return convertView;
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

    private MoreButtonPopupWindow mMenuWindow;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.more_click: {
                final int position = (int) v.getTag(R.id.day);
                final Task task = mlist.get(position);
                if (!(task.type == 2 && task.resource == 2) && task.type != 3 && task.type != 4 && !"2".equals(task.repealstate) && !"1".equals(task.state) && "1".equals(task.iscanviewdetail)) {

                    mMenuWindow = new MoreButtonPopupWindow(context, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mMenuWindow != null)
                                mMenuWindow.dismiss();
                            switch (v.getId()) {
                                case R.id.finish_click: {//完成

//                                    if (task.resource == 2) {
//                                        PublishUtils.recruitFinish(context, task.resourceid);
//                                    } else {
                                    Intent intent = new Intent(context, NewPublishedActivity.class);
                                    intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_TASK_CONDUCT);
                                    intent.putExtra(Constants.TYPE, Constants.FINISH);
                                    intent.putExtra("taskid", task.taskid);
                                    context.startActivity(intent);
//                                    }
                                }
                                break;
                                case R.id.cancel_click://撤销
                                {
                                    if (task.resource == 1) {
                                        new AlertDialog(context).builder() .setTitle(context.getString(R.string.frends_tip))
                                                .setMsg(context.getString(R.string.cancel_task))
                                                .setPositiveButton(context.getString(R.string.ok), new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        cancleTask(task.taskid, position);
                                                    }
                                                }).setNegativeButton(context.getString(R.string.cancel), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        }).show();
                                    }
                                }
                                break;
                                case R.id.revise_click://修改

                                    if (task.resource == 1) {
                                        baseActivity.showLoadingDialog(context, "正在请求服务器");
                                        Map<String, String> params = new HashMap<String, String>();
                                        params.put("ownid", PrfUtils.getUserId(context));
                                        params.put("tenantid", PrfUtils.getTenantId(context));
                                        params.put("taskid", task.taskid);
                                        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_TASK_INFO), params, context);
                                        mNetworkManager.load(2, path, new HttpListener<String>() {
                                            @Override
                                            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

                                                baseActivity.dismisLoadingDialog();
                                                boolean safe = ActivityUtils.prehandleNetworkData(context, this, callbackId, path, rootData, true);
                                                if (!safe) {
                                                    return;
                                                }
                                                switch (callbackId) {
                                                    case 2:
                                                        try {
                                                            Intent intent = new Intent(context, NewPublishedActivity.class);
                                                            intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_TASK_UPDATE);
                                                            intent.putExtra("taskInfo", rootData.getJson().getJSONObject("data").toString());
                                                            context.startActivity(intent);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
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
                                    break;
                                default:
                                    break;
                            }
                        }
                    }, task.type);

                    //显示窗口
                    mMenuWindow.show();

                } else {
                    if ("2".equals(task.repealstate) || !"1".equals(task.iscanviewdetail))
                        showToast(context.getString(R.string.task_cancel_prompt));
                    else if ("1".equals(task.state))
                        showToast(context.getString(R.string.task_finish_prompt));
                    else if (task.type == 3)
                        showToast(context.getString(R.string.cc_prompt));
                    else if (task.type == 4)
                        showToast(context.getString(R.string.task_underling_prompt));
                }
            }
            break;

            case R.id.comment_click: {
                final int position = (int) v.getTag();
                Task task = mlist.get(position);
                if (!"2".equals(task.repealstate) && "1".equals(task.iscanviewdetail)) {
                    if (task.comments > 0) {

                        Intent intent = new Intent(context, TaskTransactActivity.class);
                        intent.putExtra("TaskID", task.taskid);
                        intent.putExtra("Task", task.getJson().toString());
                        intent.putExtra("position", position);
                        intent.putExtra("showcomment", true);
                        baseActivity.startActivityForResult(intent, 1);

                    } else {
                        PublishUtils.addComment(context, PublishUtils.COMMENTTYPE_TASK, task.taskid, position);
                    }
                } else
                    showToast(context.getString(R.string.task_cancel_prompt));
            }
            break;
            case R.id.praise_click: {

                final int position = (int) v.getTag();
                Task task = mlist.get(position);
                if (!"2".equals(task.repealstate) && "1".equals(task.iscanviewdetail)) {
                    PublishUtils.toDig(context, task.taskid, PublishUtils.COMMENTTYPE_TASK, task.ispraise, new PublishUtils.DigCallBack() {
                        @Override
                        public void successful(boolean digType) {
                            chanePraiseNum(position, digType);
                            if (position == 0 && isFromTodo) {
                                toDoListDig.listHasDig();
                            }
                        }
                    });
                } else
                    showToast(context.getString(R.string.task_cancel_prompt));
            }
            break;
        }
    }

    public void cancleTask(String taskid, final int position) {
        baseActivity.showLoadingDialog(context, context.getString(R.string.prompt_info_02));
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(context));
        params.put("tenantid", PrfUtils.getTenantId(context));
        params.put("taskid", taskid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_TASK_BACKOUT), params, context);
        mNetworkManager.load(CALLBACK_TASKCANCLE, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

                baseActivity.dismisLoadingDialog();
                boolean safe = ActivityUtils.prehandleNetworkData(context, this, callbackId, path, rootData, true);
                if (!safe) {
//                    Toast.makeText(context, "取消失败！", Toast.LENGTH_SHORT).show();
                    return;
                }
                switch (callbackId) {
                    case CALLBACK_TASKCANCLE:
                        Toast.makeText(context, context.getString(R.string.cancel_success), Toast.LENGTH_SHORT).show();
                        chaneTaskState(position);
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

    private class ViewHolder {

        RelativeLayout praiseClickLayout;
        RelativeLayout commentClickLayout;
        RelativeLayout moreClickLayout;
        SimpleDraweeView userPhotoView;
        TextView userNameView;
        TextView timestampView;
        TextView contentTextView;
        NoScrollGridview imageGridView;
        TextView leftTimeView;
        TextView rightTimeView;
        TextView commentNumView;
        TextView praiseNumView;
        TextView moreNumView;
        ImageView finishLogoView;
        NoScrollListview voiceListview;
        ImageView commentIconView;
        ImageView praiseIconView;
        ImageView moreIconView;
    }


    public void myNotifyDataSetChanged(List<Task> lists) {

        this.mlist = lists;
        notifyDataSetChanged();
    }

    public void myNotifyDataSetChanged(List<Task> lists, boolean type) {

        this.mlist = lists;
        notifyDataSetChanged();
    }

    public void chanePraiseNum(int position, boolean type) {

        try {
            Task task = mlist.get(position);
            int num = task.praises;
            if (type)
                task.praises = num - 1;
            else
                task.praises = num + 1;
            if (task.praises < 0)
                task.praises = 0;
            task.ispraise = !type;
            task.getJson().put("praises", task.praises);
            task.getJson().put("ispraise", !type);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void chaneCommentNum(int position) {
        Task task = mlist.get(position);
        int num = task.comments;
        task.comments = num + 1;
        try {
            task.getJson().put("comments", task.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void chaneTaskState(int position) {
        Task task = mlist.get(position);
        task.repealstate = "2";
        try {
            task.getJson().put("repealstate", "2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void chaneTask(int position, JSONObject jsonObject) {
        try {
            if (!TextUtil.isEmpty(jsonObject.toString())) {
                Task task = JsonDataFactory.getData(Task.class, jsonObject);
                mlist.remove(position);
                mlist.add(position, task);
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
