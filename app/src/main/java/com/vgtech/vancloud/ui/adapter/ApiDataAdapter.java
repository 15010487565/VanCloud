package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.common.PrfUtils;
import com.vgtech.common.adapter.BasicArrayAdapter;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.Comment;
import com.vgtech.common.api.Draft;
import com.vgtech.common.api.Group;
import com.vgtech.common.api.IdName;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.Property;
import com.vgtech.common.api.RecruitmentInfoBean;
import com.vgtech.common.api.ScheduleItem;
import com.vgtech.common.api.ScheduleMap;
import com.vgtech.common.api.SharedListItem;
import com.vgtech.common.api.Tenant;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.JobModule;
import com.vgtech.vancloud.api.PoiItem;
import com.vgtech.vancloud.service.SubmitService;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.module.schedule.ScheduleDetailActivity;
import com.vgtech.vancloud.ui.module.schedule.ScheduleHomeActivity;
import com.vgtech.vancloud.ui.module.share.ShareActivity;
import com.vgtech.vancloud.ui.view.MyRecyclerView;
import com.vgtech.vancloud.utils.EditUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * 列表适配器
 *
 * @param <AbsApiData>
 * @author zhangshaofang
 */
public class ApiDataAdapter<AbsApiData> extends BasicArrayAdapter<AbsApiData> implements View.OnClickListener, ViewListener {
    private static final int VIEWTYPE_GROUP_ITEM = 1;
    private static final int VIEWTYPE_DRAFT = 2;
    private static final int VIEWTYPE_SCHEDULE = 3;
    private static final int VIEWTYPE_PROPERTY = 4;
    private static final int VIEWTYPE_TENANT = 5;
    private static final int VIEWTYPE_IDNAME = 6;
    private static final int VIEWTYPE_COMMENT = 7;
    private static final int VIEWTYPE_RECRUIT = 8;
    private static final int VIEWTYPE_JOBMODULE = 9;
    private static final int VIEWTYPE_POIITEM = 10;

    private boolean isMySchedule = false;

    public ApiDataAdapter(Context context) {
        super(context);
        mSelectData = new ArrayList<AbsApiData>();
    }

    private SharedListItem sharedItem;
    private int mPosition;

    public void setPosition(int position) {
        mPosition = position;
    }

    public void setSharedItem(SharedListItem item) {
        sharedItem = item;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount();
    }

    private List<AbsApiData> selectedData;

    public void addSelected(AbsApiData data) {
        if (selectedData == null)
            selectedData = new ArrayList<>();
        selectedData.add(data);
    }

    public List<AbsApiData> getSelectedData() {
        if (selectedData == null)
            selectedData = new ArrayList<>();
        return selectedData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        AbsApiData data = getItem(position);
        int viewType = getItemViewType(data);
        if (convertView == null) {
            view = getItemView(parent, viewType, true);
        } else {
            view = convertView;
        }
        fillItemView(view, viewType, data, position);
        return view;
    }

    public View getItemView(AbsApiData data) {
        int viewType = getItemViewType(data);
        View view = getItemView(null, viewType, true);
        fillItemView(view, viewType, data, 0);
        return view;
    }

    private void fillItemView(View view, int type, AbsApiData data, int position) {
        @SuppressWarnings("unchecked") final
        SparseArray<View> viewMap = (SparseArray<View>) view.getTag();


        int id = getViewResId(type);
        switch (id) {
            case R.layout.group_item: {
                Group group = (Group) data;
                TextView nameTv = (TextView) viewMap.get(R.id.tv_name);
                nameTv.setText(group.name);
                TextView countTv = (TextView) viewMap.get(R.id.tv_count);
                countTv.setText(mContext.getString(R.string.group_count, group.count));
                CheckBox checkBox = (CheckBox) viewMap.get(android.R.id.checkbox);
                checkBox.setChecked(mSelectData.contains(group));
            }
            break;
            case R.layout.property_item: {
                Property property = (Property) data;
                TextView nameTv = (TextView) viewMap.get(R.id.tv_name);
                TextView valueTv = (TextView) viewMap.get(R.id.tv_value);
                nameTv.setText(property.name);
                valueTv.setText("");
//                if (property.type == 3) {
//                    if (!TextUtils.isEmpty(property.value)) {
//                        List<Option> options = property.getArrayData(Option.class);
//                        for (Option option : options) {
//                            if (property.value.equals(option.id)) {
//                                valueTv.setDettailText(option.name);
//                                break;
//                            }
//                        }
//                    }
//                } else {
                valueTv.setText(property.value);
//                }
                viewMap.get(R.id.ic_arrow).setVisibility(property.edit ? View.VISIBLE : View.INVISIBLE);
            }
            break;
            case R.layout.item_choose_company: {
                Tenant tenant = (Tenant) data;
                TextView contentTv = (TextView) viewMap.get(R.id.tv_menu);
                contentTv.setText(tenant.tenant_name);
            }
            break;
            case R.layout.draft: {
                Draft draft = (Draft) data;
                TextView typeTv = (TextView) viewMap.get(R.id.tv_type);
                TextView contentTv = (TextView) viewMap.get(R.id.tv_content);
                TextView timeTv = (TextView) viewMap.get(R.id.tv_time);
                TextView sendTv = (TextView) viewMap.get(R.id.btn_send);
                if (draft.obj instanceof PublishTask) {
                    PublishTask task = (PublishTask) draft.obj;
                    sendTv.setEnabled(!task.sending);
                    int resId = Utils.getPublishTypeResId(task.type);
                    if (resId != 0)
                        typeTv.setText(resId);
//                    contentTv.setDettailText(task.content);
                    timeTv.setText(Utils.getInstance(mContext).dateFormat(task.timestamp));
                }
                sendTv.setTag(draft.obj);
            }
            break;
            case R.layout.schedule_item_new: {
                viewMap.get(R.id.content_layout).setTag(R.id.schedule_item, position);

                ScheduleMap scheduleItemMap = (ScheduleMap) data;
                TextView dateTv = (TextView) viewMap.get(R.id.tv_schedule_date);
                TextView timeTv = (TextView) viewMap.get(R.id.tv_start_time);
                timeTv.setText(Utils.dateFormatHour(scheduleItemMap.startTime));
                if (mDateVisible) {//日程搜索显示日期
                    String date = Utils.dateFormatToDate(scheduleItemMap.startTime);
                    dateTv.setText(date);
                    if (position == 0) {
                        dateTv.setVisibility(View.VISIBLE);
                    } else {
                        ScheduleMap lastScheduleMap = (ScheduleMap) getItem(position - 1);
                        String lastDate = Utils.dateFormatToDate(lastScheduleMap.startTime);
                        //日期相同时不显示
                        dateTv.setVisibility(lastDate.equals(date) ? View.GONE : View.VISIBLE);
                    }
                }


//                viewMap.get(R.id.top_line).setVisibility(position == 0 && isMySchedule ? View.INVISIBLE : View.VISIBLE);
                View parentView = viewMap.get(R.id.content_layout);
                MyRecyclerView mRecyclerView = (MyRecyclerView) viewMap.get(R.id.id_recyclerview_horizontal);
                GalleryAdapter mAdapter = (GalleryAdapter) mRecyclerView.getAdapter();
                mAdapter.setParentView(parentView);
                mAdapter.setData(scheduleItemMap);
                mRecyclerView.scrollToPosition(scheduleItemMap.selectUserPosition);
                ScheduleItem scheduleItem = mAdapter.getSelectItem();
                parentView.setTag(R.string.app_name, scheduleItem);
                TextView mTestTv = (TextView) viewMap.get(R.id.tv_test);
                EditUtils.SetTextViewMaxLines(mTestTv, 5);
                mTestTv.setText(EmojiFragment.getEmojiContent(mContext, mTestTv.getTextSize(), Html.fromHtml(scheduleItem.getContent())));
                List<ImageInfo> imageInfos = scheduleItem.getArrayData(ImageInfo.class);
                GridView imageGrid = (GridView) viewMap.get(R.id.imagegridview);
                if (imageInfos != null && !imageInfos.isEmpty()) {
                    imageGrid.setVisibility(View.VISIBLE);
                    ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(imageGrid, mContext, imageInfos, true);
                    imageGrid.setAdapter(imageGridviewAdapter);
                } else {
                    imageGrid.setVisibility(View.GONE);
                }


                TextView tv_schedule_time = (TextView) viewMap.get(R.id.tv_schedule_time);
                TextView tv_schedule_duration = (TextView) viewMap.get(R.id.tv_schedule_duration);
                tv_schedule_time.setText(Utils.dateFormatNoYesterday(scheduleItem.starttime) + " -- " + Utils.dateFormatNoYesterday(scheduleItem.endtime));
                tv_schedule_duration.setText(Utils.getDuration(mContext, scheduleItem.starttime, scheduleItem.endtime));
                List<AudioInfo> audios = scheduleItem.getArrayData(AudioInfo.class);
                NoScrollListview voiceListview = (NoScrollListview) viewMap.get(R.id.voice_listview);
                AudioListAdapter audioListAdapter = (AudioListAdapter) voiceListview.getAdapter();
                if (audios != null && !audios.isEmpty()) {
                    audioListAdapter.dataSource.clear();
                    audioListAdapter.dataSource.addAll(audios);
                    audioListAdapter.notifyDataSetChanged();
                    voiceListview.setVisibility(View.VISIBLE);

                } else {
                    voiceListview.setVisibility(View.GONE);

                }

                ImageView status = (ImageView) viewMap.get(R.id.operation);
                TextView status_tv = (TextView) viewMap.get(R.id.operation_tv);
                View bottom_time_line = viewMap.get(R.id.bottom_time_line);
                //
                ImageView timeOutLogo = (ImageView) viewMap.get(R.id.cancel_view);
                if (PrfUtils.isChineseForAppLanguage(mContext))
                    timeOutLogo.setImageResource(R.mipmap.cancel_logo_ch);
                else
                    timeOutLogo.setImageResource(R.mipmap.cancel_logo_en);

                if ("2".equals(scheduleItem.getRepealstate())) {
                    timeOutLogo.setVisibility(View.VISIBLE);
                } else if ("2".equals(scheduleItem.signs)) {
                    //已过期
                    timeOutLogo.setVisibility(View.VISIBLE);
                    if (PrfUtils.isChineseForAppLanguage(mContext))
                        timeOutLogo.setImageResource(R.mipmap.expired_logo_ch);
                    else
                        timeOutLogo.setImageResource(R.mipmap.expired_logo_en);
                } else {
                    timeOutLogo.setVisibility(View.GONE);
                }
                if (scheduleItem.replyflag != null) {
                    if ("3".equals(scheduleItem.type)) {
                        status.setVisibility(View.GONE);
                        status_tv.setVisibility(View.GONE);
                        bottom_time_line.setVisibility(View.GONE);
                    } else if ("1".equals(scheduleItem.replyflag)) {
                        status.setVisibility(View.VISIBLE);
                        status_tv.setVisibility(View.VISIBLE);
                        bottom_time_line.setVisibility(View.VISIBLE);
                        status.setImageResource(R.mipmap.schedule_indeterminate_bg);
                        status_tv.setText(mContext.getString(R.string.schedule_detail_watting) + "  ");
                        status_tv.setTextColor(mContext.getResources().getColor(R.color.schedule_indeterminate_color));
                    } else if ("2".equals(scheduleItem.replyflag)) {
                        status.setVisibility(View.VISIBLE);
                        status_tv.setVisibility(View.VISIBLE);
                        bottom_time_line.setVisibility(View.VISIBLE);
                        status.setImageResource(R.mipmap.schedule_refuse_bg);
                        status_tv.setText(mContext.getString(R.string.schedule_detail_refuse) + "  ");
                        status_tv.setTextColor(mContext.getResources().getColor(R.color.schedule_refuse_color));
                    } else if ("3".equals(scheduleItem.replyflag)) {
                        status.setVisibility(View.VISIBLE);
                        status_tv.setVisibility(View.VISIBLE);
                        bottom_time_line.setVisibility(View.VISIBLE);
                        status.setImageResource(R.mipmap.schedule_agree_bg);
                        status_tv.setText(mContext.getString(R.string.schedule_detail_agree) + "  ");
                        status_tv.setTextColor(mContext.getResources().getColor(R.color.schedule_agree_color));
                    } else if ("4".equals(scheduleItem.replyflag)) {
                        status.setVisibility(View.VISIBLE);
                        status_tv.setVisibility(View.VISIBLE);
                        bottom_time_line.setVisibility(View.VISIBLE);
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
                    bottom_time_line.setVisibility(View.GONE);
                }
            }
            break;
            case R.layout.item_job_module:
                JobModule jobModule = (JobModule) data;
                TextView name = (TextView) viewMap.get(R.id.tv_name);
                name.setText(jobModule.job_template_name);
                break;
            case R.layout.item_recruitment: {
                RecruitmentInfoBean item = (RecruitmentInfoBean) data;
                TextView nameTv = (TextView) viewMap.get(R.id.tv_name);
                TextView channelTv = (TextView) viewMap.get(R.id.tv_channel);
                TextView statusTv = (TextView) viewMap.get(R.id.tv_status);
                TextView numTv = (TextView) viewMap.get(R.id.tv_num);
                nameTv.setText(item.job_name);
                if ("vancloud".equals(item.job_source)) {
                    channelTv.setText(mContext.getString(R.string.job_source_title) + mContext.getString(R.string.source_vancloud));
                } else if ("51job".equals(item.job_source)) {
                    channelTv.setText(mContext.getString(R.string.job_source_title) + mContext.getString(R.string.source_51job));
                } else if ("zhaopin".equals(item.job_source)) {
                    channelTv.setText(mContext.getString(R.string.job_source_title) + mContext.getString(R.string.source_zhaopin));
                } else {
                    channelTv.setVisibility(View.GONE);
                }
                if ("publish".equals(item.status)) {
                    statusTv.setText(mContext.getString(R.string.job_status_title) + mContext.getString(R.string.tv_relase));
                } else if ("pause".equals(item.status)) {
                    statusTv.setText(mContext.getString(R.string.job_status_title) + mContext.getString(R.string.tv_stop));
                } else if ("finish".equals(item.status)) {
                    statusTv.setText(mContext.getString(R.string.job_status_title) + mContext.getString(R.string.tv_relase_finish));
                } else if ("remove".equals(item.status)) {
                    statusTv.setText(mContext.getString(R.string.job_status_title) + mContext.getString(R.string.resume_has_del));
                } else {
                    statusTv.setVisibility(View.GONE);
                }
                numTv.setText("(" + item.send_num + ")");
            }
            break;
            case R.layout.poi_item: {
                PoiItem poiItem = (PoiItem) data;
                TextView tv_name = (TextView) viewMap.get(R.id.tv_name);
                TextView tv_address = (TextView) viewMap.get(R.id.tv_address);
                if (position == 0 && TextUtils.isEmpty(poiItem.name)) {
                    tv_name.setText("[" + mContext.getString(R.string.location) + "]");
                } else {
                    tv_name.setText(poiItem.name);
                }
                tv_address.setText(poiItem.address);
                ImageView iv_selected = (ImageView) viewMap.get(R.id.iv_selected);
                if (selectedData != null && selectedData.contains(data)) {
                    iv_selected.setVisibility(View.VISIBLE);
                } else {
                    iv_selected.setVisibility(View.GONE);
                }
            }
            break;
            case R.layout.item_option: {
                IdName idName = (IdName) data;
                TextView nameTv = (TextView) viewMap.get(R.id.tv_name);
                TextView valueTv = (TextView) viewMap.get(R.id.tv_value);
                nameTv.setText(idName.lable);
                valueTv.setText(idName.sub_data);
            }
            break;
            case R.layout.common: {
                Comment comment = (Comment) data;
                View commView = viewMap.get(R.id.comment_view);
                commView.setTag(R.string.app_name, comment);
                JSONObject jsonObject = comment.getJson();
                String replyuser = "";
                if (jsonObject.has("replyuser")) {
                    Object obj = null;
                    try {
                        obj = jsonObject.get("replyuser");
                        if (obj instanceof JSONObject) {
                            JSONObject replayObject = (JSONObject) obj;
                            replyuser = replayObject.getString("name");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                NewUser user = comment.getData(NewUser.class);
                TextView nameTv = (TextView) viewMap.get(R.id.tv_name);
                StringBuffer nameBuffer = new StringBuffer(user.name);
                if (!TextUtils.isEmpty(replyuser)) {
                    nameBuffer.append("<font color=\"#444444\">").append(mContext.getString(R.string.comment)).append("</font>").append(replyuser);
                }
                nameTv.setText(Html.fromHtml(nameBuffer.toString() + ": "));
                TextView commentTv = (TextView) viewMap.get(R.id.tv_comment);
                List<ImageInfo> images = comment.getArrayData(ImageInfo.class);
                List<AudioInfo> audios = comment.getArrayData(AudioInfo.class);
                if (TextUtils.isEmpty(comment.content)) {
                    commentTv.setVisibility(View.GONE);
                    nameTv.setVisibility(View.VISIBLE);
                } else {
                    if (images.isEmpty() && audios.isEmpty()) {
                        nameTv.setVisibility(View.GONE);
                    } else {
                        nameTv.setText(user.name);
                        nameTv.setVisibility(View.INVISIBLE);
                    }
                    commentTv.setVisibility(View.VISIBLE);
                    commentTv.setText(EmojiFragment.getEmojiContent(mContext, commentTv.getTextSize(), Html.fromHtml(user.name + comment.content)));
                }

                StringBuffer namebuf = new StringBuffer();
                if (!TextUtils.isEmpty(replyuser)) {
                    namebuf.append("<font color=\"#5577a7\">").append(user.name).append("</font>")
                            .append(mContext.getString(R.string.comment))
                            .append("<font color=\"#5577a7\">").append(replyuser).append("</font>")
                            .append("<font color=\"#5577a7\">").append(": ").append("</font>");
                    namebuf.append(comment.content);
                    commentTv.setText(EmojiFragment.getEmojiContent(mContext, commentTv.getTextSize(), Html.fromHtml(namebuf.toString())));
                } else {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append(user.name + ": " + comment.content);

                    int color = Color.parseColor("#5577a7");
                    Spannable span = EmojiFragment.getEmojiContent(mContext, commentTv.getTextSize(), Html.fromHtml(stringBuffer.toString()));
                    span.setSpan(new ForegroundColorSpan(color), 0,
                            user.name.length() + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    commentTv.setText(span);
                }


                NoScrollGridview imageGridView = (NoScrollGridview) viewMap.get(R.id.forward_image);
                NoScrollListview audioListView = (NoScrollListview) viewMap.get(R.id.forward_audio);
                if (!images.isEmpty()) {
                    ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(imageGridView, mContext, images, Utils.convertDipOrPx(mContext, 40), 0);
                    imageGridView.setAdapter(imageGridviewAdapter);
                    imageGridView.setVisibility(View.VISIBLE);
                } else {
                    imageGridView.setVisibility(View.GONE);
                }

                if (!audios.isEmpty()) {
                    AudioListAdapter audioListAdapter = new AudioListAdapter(mContext, this);
                    audioListAdapter.small = true;
                    audioListAdapter.dataSource.addAll(audios);
                    audioListView.setAdapter(audioListAdapter);
                    audioListView.setVisibility(View.VISIBLE);
                } else {
                    audioListView.setVisibility(View.GONE);
                }

            }
            break;
            default:
                /**
                 * ignore
                 */
                break;

        }
    }

    /**
     * 选中数据数组
     */
    private List<AbsApiData> mSelectData;

    public List<AbsApiData> getSelectData() {
        return mSelectData;
    }

    /**
     * 初始化页面
     *
     * @param parent
     * @param type
     * @param visible
     * @return
     */
    private View getItemView(ViewGroup parent, int type, boolean visible) {
        int id = getViewResId(type);
        View view = mInflater.inflate(id, parent, false);
        SparseArray<View> viewMap = new SparseArray<View>();
        switch (id) {
            case R.layout.group_item:
                putViewMap(viewMap, view, android.R.id.checkbox);
                putViewMap(viewMap, view, R.id.tv_name);
                putViewMap(viewMap, view, R.id.tv_count);
                break;
            case R.layout.item_choose_company: {
                putViewMap(viewMap, view, R.id.tv_menu);
            }
            break;
            case R.layout.item_option:
                putViewMap(viewMap, view, R.id.tv_name);
                putViewMap(viewMap, view, R.id.ic_arrow).setVisibility(View.INVISIBLE);
                putViewMap(viewMap, view, R.id.tv_value);
                putViewMap(viewMap, view, R.id.driver).setVisibility(View.GONE);
                break;
            case R.layout.property_item:
                putViewMap(viewMap, view, R.id.tv_name);
                putViewMap(viewMap, view, R.id.tv_value);
                putViewMap(viewMap, view, R.id.ic_arrow);
                break;
            case R.layout.item_job_module:
                putViewMap(viewMap, view, R.id.tv_name);
                break;
            case R.layout.item_recruitment:
                putViewMap(viewMap, view, R.id.tv_name);
                putViewMap(viewMap, view, R.id.tv_channel);
                putViewMap(viewMap, view, R.id.tv_status);
                putViewMap(viewMap, view, R.id.tv_num);
                break;
            case R.layout.poi_item:
                putViewMap(viewMap, view, R.id.tv_name);
                putViewMap(viewMap, view, R.id.tv_address);
                putViewMap(viewMap, view, R.id.iv_selected);
                break;
            case R.layout.common:
                putViewMap(viewMap, view, R.id.tv_name);
                putViewMap(viewMap, view, R.id.comment_view).setOnClickListener(this);
                putViewMap(viewMap, view, R.id.tv_comment);
                putViewMap(viewMap, view, R.id.forward_image);
                putViewMap(viewMap, view, R.id.forward_audio);
                break;
            case R.layout.draft:
                putViewMap(viewMap, view, R.id.tv_type);
                putViewMap(viewMap, view, R.id.tv_content);
                putViewMap(viewMap, view, R.id.tv_time);
                putViewMap(viewMap, view, R.id.btn_send).setOnClickListener(this);
                break;
            case R.layout.schedule_item_new: {
                putViewMap(viewMap, view, R.id.content_layout).setOnClickListener(this);
                MyRecyclerView recyclerView = (MyRecyclerView) putViewMap(viewMap, view, R.id.id_recyclerview_horizontal);
                GalleryAdapter mAdapter = new GalleryAdapter(mContext, this);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(mAdapter);

                putViewMap(viewMap, view, R.id.tv_schedule_date);
                putViewMap(viewMap, view, R.id.tv_schedule_time);
                putViewMap(viewMap, view, R.id.tv_schedule_duration);
                putViewMap(viewMap, view, R.id.top_line);
                putViewMap(viewMap, view, R.id.tv_start_time);
                putViewMap(viewMap, view, R.id.tv_test);
                putViewMap(viewMap, view, R.id.imagegridview);
                putViewMap(viewMap, view, R.id.bottom_time_line);
                putViewMap(viewMap, view, R.id.operation);
                putViewMap(viewMap, view, R.id.operation_tv);
                putViewMap(viewMap, view, R.id.cancel_view);
                NoScrollListview voiceListview = (NoScrollListview) putViewMap(viewMap, view, R.id.voice_listview);
                AudioListAdapter audioListAdapter = new AudioListAdapter(mContext, this);
                voiceListview.setAdapter(audioListAdapter);
            }
            break;
            default:
                /**
                 * ignore
                 */
                break;

        }
        view.setTag(viewMap);
        return view;
    }

    private View putViewMap(SparseArray<View> viewMap, View view, int id) {
        View v = view.findViewById(id);
        viewMap.put(id, v);
        return v;
    }

    /**
     * 配置数据页面类型
     *
     * @param data
     * @return
     */
    private int getItemViewType(AbsApiData data) {
        int result = -1;
        if (data instanceof Group) {
            result = VIEWTYPE_GROUP_ITEM;
        } else if (data instanceof Draft) {
            result = VIEWTYPE_DRAFT;
        } else if (data instanceof ScheduleMap) {
            result = VIEWTYPE_SCHEDULE;
        } else if (data instanceof Property) {
            result = VIEWTYPE_PROPERTY;
        } else if (data instanceof Tenant) {
            result = VIEWTYPE_TENANT;
        } else if (data instanceof IdName) {
            result = VIEWTYPE_IDNAME;
        } else if (data instanceof Comment) {
            result = VIEWTYPE_COMMENT;
        } else if (data instanceof RecruitmentInfoBean) {
            result = VIEWTYPE_RECRUIT;
        } else if (data instanceof JobModule) {
            result = VIEWTYPE_JOBMODULE;
        } else if (data instanceof PoiItem) {
            result = VIEWTYPE_POIITEM;
        }
        return result;
    }

    /**
     * 根据类型配置页面
     *
     * @param type
     * @return
     */
    private int getViewResId(int type) {
        int result = 1;
        switch (type) {
            case VIEWTYPE_POIITEM:
                result = R.layout.poi_item;
                break;
            case VIEWTYPE_COMMENT:
                result = R.layout.common;
                break;
            case VIEWTYPE_IDNAME:
                result = R.layout.item_option;
                break;
            case VIEWTYPE_PROPERTY:
                result = R.layout.property_item;
                break;
            case VIEWTYPE_GROUP_ITEM:
                result = R.layout.group_item;
                break;
            case VIEWTYPE_DRAFT:
                result = R.layout.draft;
                break;
            case VIEWTYPE_SCHEDULE:
                result = R.layout.schedule_item_new;
                break;
            case VIEWTYPE_TENANT:
                result = R.layout.item_choose_company;
                break;
            case VIEWTYPE_RECRUIT:
                result = R.layout.item_recruitment;
                break;
            case VIEWTYPE_JOBMODULE:
                result = R.layout.item_job_module;
                break;
            default:
                /**
                 * ignore
                 */
                break;
        }
        return result;
    }

    private ScheduleItem mScheduleItem;


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.comment_view:
                Comment comment = (Comment) v.getTag(R.string.app_name);
                NewUser user = comment.getData(NewUser.class);
                ShareActivity shareActivity = (ShareActivity) mContext;
                shareActivity.commentUser(mPosition, sharedItem, user);
                break;
            case R.id.btn_send: {
                Object obj = v.getTag();
                if (obj instanceof PublishTask) {
                    v.setEnabled(false);
                    PublishTask task = (PublishTask) obj;
                    task.sending = true;
                    Intent intent = new Intent(mContext, SubmitService.class);
                    intent.putExtra("publishTask", task);
                    mContext.startService(intent);
                }
            }
            break;
            case R.id.content_layout: {
                int position = (Integer) v.getTag(R.id.schedule_item);
                ScheduleItem scheduleItem = (ScheduleItem) v.getTag(R.string.app_name);
                mScheduleItem = scheduleItem;
                if (!"2".equals(scheduleItem.getRepealstate())) {
//                    Intent intent = new Intent(mContext, ScheduleDetailActivity.class);
//                    intent.putExtra("data", scheduleItem.getJson().toString());
//                    mContext.startActivity(intent);
                    Intent intent = new Intent(mContext, ScheduleDetailActivity.class);
                    intent.putExtra("data", scheduleItem.getJson().toString());
                    intent.putExtra("position", position);

                    ((Activity) mContext).startActivityForResult(intent, 1);
                }
//                Intent intent = new Intent(activity, ScheduleDetailActivity.class);
//                intent.putExtra("data", s.getJson().toString());
//                activity.startActivity(intent);
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

    public void setIsMySchedule(boolean isMySchedule) {
        this.isMySchedule = isMySchedule;
    }

    private boolean mDateVisible;

    public void setDateVisible(boolean visible) {
        mDateVisible = visible;
    }

    public void chaneScheduleState(int position, int type) {
        if (mScheduleItem == null) {
            return;
        }
        if (position == -1 || type == -1)
            return;
        if (type != -2) {
            //  ScheduleItem item = (ScheduleItem)mSelectData.get(position);
            mScheduleItem.status = type + "";
            try {
                mScheduleItem.getJson().put("status", type + "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            //  ScheduleItem item = (ScheduleItem)mSelectData.get(position);
            mScheduleItem.repealstate = "2";
            try {
                mScheduleItem.getJson().put("repealstate", "2");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        notifyDataSetChanged();
    }
}
