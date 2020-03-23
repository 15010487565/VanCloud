package com.vgtech.vancloud.ui.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.vgtech.common.adapter.BasicArrayAdapter;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.Organization;
import com.vgtech.common.api.PushMessage;
import com.vgtech.common.api.SearchItem;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.provider.db.MessageDB;
import com.vgtech.common.provider.db.User;
import com.vgtech.common.utils.TypeUtils;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.NotificationExtension;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.UsersMessagesFragment;
import com.vgtech.vancloud.ui.chat.controllers.AvatarController;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.chat.models.ChatMessage;
import com.vgtech.vancloud.ui.module.announcement.AnnouncementDetailActivity;
import com.vgtech.vancloud.ui.module.announcement.NoticeInfoActivity;
import com.vgtech.vancloud.ui.module.flow.FlowHandleActivity;
import com.vgtech.vancloud.ui.module.todo.CommentMessageListActivity;
import com.vgtech.vancloud.ui.search.SearchFragment;
import com.vgtech.vancloud.ui.search.SearchResultFragment;
import com.vgtech.vancloud.ui.view.groupimageview.NineGridImageView;
import com.vgtech.vancloud.utils.NotificationUtils;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vantop.ui.userinfo.VantopUserInfoActivity;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 列表适配器
 *
 * @param <AbsApiData>
 * @author zhangshaofang
 */
public class SearchAdapter<AbsApiData> extends BasicArrayAdapter<AbsApiData> implements View.OnClickListener {
    private static final int VIEWTYPE_SEARCH_ITEM = 1;
    private Controller mController;
    private AvatarController avatarController;
    private Fragment mFragment;
    public SearchAdapter(Fragment fragment, Controller controller, AvatarController avatarController) {
        super(fragment.getActivity());
        mFragment = fragment;
        mController = controller;
        this.avatarController = avatarController;
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount();
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
            case R.layout.search_item: {
                SearchItem searchItem = (SearchItem) data;
                TextView item_type = (TextView) viewMap.get(R.id.item_type);
                item_type.setText(searchItem.getTypeTitle(mContext));
                item_type.setVisibility(searchItem.isFirst ? View.VISIBLE : View.GONE);
                View item_content = viewMap.get(R.id.item_content);
                item_content.setTag(searchItem);
                TextView item_title = (TextView) viewMap.get(R.id.item_title);
                TextView item_time = (TextView) viewMap.get(R.id.item_time);
                TextView item_subtitle = (TextView) viewMap.get(R.id.item_subtitle);
                item_title.setText(Html.fromHtml(searchItem.title));
                item_subtitle.setText(Html.fromHtml(searchItem.subtitle));
                item_subtitle.setVisibility(TextUtils.isEmpty(searchItem.subtitle) ? View.GONE : View.VISIBLE);
                View line_more = viewMap.get(R.id.line_more);
                View btn_item_more = viewMap.get(R.id.btn_item_more);
                btn_item_more.setTag(searchItem);
                TextView more_item_type = (TextView) viewMap.get(R.id.more_item_type);
                View item_spit = viewMap.get(R.id.item_spit);
                more_item_type.setText(mContext.getString(R.string.search_more_item, searchItem.getTypeTitle(mContext)));
                line_more.setVisibility(searchItem.hasMore ? View.VISIBLE : View.GONE);
                btn_item_more.setVisibility(searchItem.hasMore ? View.VISIBLE : View.GONE);
                item_spit.setVisibility(searchItem.isLast ? View.VISIBLE : View.GONE);
                SimpleDraweeView item_iv = (SimpleDraweeView) viewMap.get(R.id.item_iv);
                NineGridImageView nineIv = (NineGridImageView) viewMap.get(R.id.avatar_container);
                item_time.setVisibility(View.GONE);
                switch (searchItem.type) {
                    case notice: {
                        item_iv.setVisibility(View.VISIBLE);
                        nineIv.setVisibility(View.GONE);
                        item_iv.setBackgroundResource(R.drawable.message_round);
                        item_time.setText(Utils.getInstance(mContext).dateFormat(Long.parseLong(searchItem.timestamp)));
                        item_time.setVisibility(View.VISIBLE);
                        GradientDrawable myGrad = (GradientDrawable) item_iv.getBackground();
                        item_iv.setImageResource(R.mipmap.ic_app_notice);
                        if (myGrad != null)
                            myGrad.setColor(Color.parseColor("#63c5ff"));
                    }
                    break;
                    case mynotice: {
                        item_iv.setVisibility(View.VISIBLE);
                        nineIv.setVisibility(View.GONE);
                        item_iv.setBackgroundResource(R.drawable.message_round);
                        item_time.setText(Utils.getInstance(mContext).dateFormat(Long.parseLong(searchItem.timestamp)));
                        item_time.setVisibility(View.VISIBLE);
                        GradientDrawable myGrad = (GradientDrawable) item_iv.getBackground();
                        item_iv.setImageResource(R.mipmap.ic_app_notification);
                        if (myGrad != null)
                            myGrad.setColor(Color.parseColor("#a1ca92"));
                    }
                    break;
                    case todo:
                        item_iv.setVisibility(View.VISIBLE);
                        nineIv.setVisibility(View.GONE);
                        item_iv.setBackgroundResource(R.drawable.message_round);
                        item_time.setText(Utils.getInstance(mContext).dateFormat(Long.parseLong(searchItem.timestamp)));
                        item_time.setVisibility(View.VISIBLE);
                        GradientDrawable myGrad = (GradientDrawable) item_iv.getBackground();
                        item_iv.setImageResource(R.mipmap.ic_app_todo);
                        if (myGrad != null)
                            myGrad.setColor(Color.parseColor("#23ba9b"));
                        break;
                    case user:
                        item_iv.setVisibility(View.VISIBLE);
                        nineIv.setVisibility(View.GONE);
                        ImageOptions.setUserImage(item_iv, searchItem.icon);
                        break;
                    case chatgroup: {
                        List<String> avatars = new ArrayList<>();
                        try {
                            List<String> tmpList = new Gson().fromJson(searchItem.icon, new TypeToken<List<String>>() {
                            }.getType());
                            avatars.addAll(tmpList);
                        } catch (JsonSyntaxException ignored) {
                            if (!TextUtils.isEmpty(searchItem.icon)) {
                                String[] avatarArray = searchItem.icon.split(",");
                                avatars = new ArrayList(Arrays.asList(avatarArray));
                            }
                        }
                        avatarController.setAvatarContainer(item_iv, nineIv, avatars);
                    }
                    break;
                    case chatmessage:
                        item_iv.setVisibility(View.VISIBLE);
                        nineIv.setVisibility(View.GONE);
                        if (searchItem.itemObj instanceof List) {
                            List<ChatMessage> cmlist = (List<ChatMessage>) searchItem.itemObj;
                            ChatMessage cm = cmlist.get(cmlist.size() - 1);
                            if (Message.Type.groupchat == cm.group.getChatType()) {
                                List<String> avatars = new ArrayList<>();
                                try {
                                    List<String> tmpList = new Gson().fromJson(searchItem.icon, new TypeToken<List<String>>() {
                                    }.getType());
                                    if(tmpList!=null)
                                    avatars.addAll(tmpList);
                                } catch (JsonSyntaxException ignored) {
                                    if (!TextUtils.isEmpty(searchItem.icon)) {
                                        String[] avatarArray = searchItem.icon.split(",");
                                        avatars = new ArrayList(Arrays.asList(avatarArray));
                                    }
                                }
                                avatarController.setAvatarContainer(item_iv, nineIv, avatars);
                            } else {
                                ImageOptions.setUserImage(item_iv, cm.group.avatar);
                            }
                        } else if (searchItem.itemObj instanceof ChatMessage) {
                            ChatMessage cm = (ChatMessage) searchItem.itemObj;
                            if (Message.Type.groupchat == cm.group.getChatType()) {
                                List<String> avatars = new ArrayList<>();
                                try {
                                    List<String> tmpList = new Gson().fromJson(searchItem.icon, new TypeToken<List<String>>() {
                                    }.getType());
                                    avatars.addAll(tmpList);
                                } catch (JsonSyntaxException ignored) {
                                    if (!TextUtils.isEmpty(searchItem.icon)) {
                                        String[] avatarArray = searchItem.icon.split(",");
                                        avatars = new ArrayList(Arrays.asList(avatarArray));
                                    }
                                }
                                avatarController.setAvatarContainer(item_iv, nineIv, avatars);
                            } else {
                                ImageOptions.setUserImage(item_iv, cm.group.avatar);
                            }
                        }

                        break;
                    case pushmessage:
                        item_iv.setVisibility(View.VISIBLE);
                        nineIv.setVisibility(View.GONE);
                        MessageDB messageDB = (MessageDB) searchItem.itemObj;
                        item_iv.setImageResource(R.mipmap.ic_launcher);

                        if (TypeUtils.JOINCOMPANY.equals(messageDB.type)) {
                            PushMessage pushMessage = null;
                            try {
                                pushMessage = JsonDataFactory.getData(PushMessage.class, new JSONObject(messageDB.content));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (pushMessage != null)
                                ImageOptions.setUserImage(item_iv, pushMessage.logo);
                        } else if (TypeUtils.NEWEMPLOYEE.equals(messageDB.type)) {
                            item_iv.setImageResource(R.mipmap.user_photo_default_small);
                        } else {
                            item_iv.setBackgroundResource(R.drawable.message_round);
                            NotificationUtils.setImageView(mContext, item_iv, messageDB.type);
                        }
                        break;
                }
            }
            break;

        }
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
            case R.layout.search_item:
                putViewMap(viewMap, view, R.id.item_type);
                putViewMap(viewMap, view, R.id.line);
                putViewMap(viewMap, view, R.id.item_content).setOnClickListener(this);
                putViewMap(viewMap, view, R.id.item_iv);
                putViewMap(viewMap, view, R.id.avatar_container);
                putViewMap(viewMap, view, R.id.item_title);
                putViewMap(viewMap, view, R.id.item_subtitle);
                putViewMap(viewMap, view, R.id.line_more);
                putViewMap(viewMap, view, R.id.btn_item_more).setOnClickListener(this);
                putViewMap(viewMap, view, R.id.more_item_type);
                putViewMap(viewMap, view, R.id.item_spit);
                putViewMap(viewMap, view, R.id.item_time);
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
        if (data instanceof SearchItem) {
            result = VIEWTYPE_SEARCH_ITEM;
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
            case VIEWTYPE_SEARCH_ITEM:
                result = R.layout.search_item;
                break;
        }
        return result;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_item_more: {
                SearchItem searchItem = (SearchItem) v.getTag();
                Bundle bundle = new Bundle();
                bundle.putString("searchType", searchItem.type.toString());
                bundle.putString("keyword", searchItem.id);
                SearchFragment fragment = new SearchFragment();
                fragment.setArguments(bundle);
                mController.pushFragment(fragment);
            }
            break;
            case R.id.item_content:
                SearchItem searchItem = (SearchItem) v.getTag();
                if (searchItem.type == SearchItem.Type.notice) {
                        JSONObject jsonObject = (JSONObject) searchItem.itemObj;
                    Intent intent = new Intent(mContext, AnnouncementDetailActivity.class);
                    try {
                        intent.putExtra("id", jsonObject.getString("noticeId"));
                        mContext.startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (searchItem.type == SearchItem.Type.mynotice) {
                    JSONObject jsonObject = (JSONObject) searchItem.itemObj;
                    Intent intent = new Intent(mContext, NoticeInfoActivity.class);
                    try {
                        intent.putExtra("Seq",jsonObject.getString("mynotice_seq"));
                        intent.putExtra("StaffNo",jsonObject.getString("staff_no"));
                        intent.putExtra("Code",jsonObject.getString("mynotice_code"));
                        mContext.startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (searchItem.type == SearchItem.Type.todo) {
                    JSONObject jsonObject = (JSONObject) searchItem.itemObj;
                    try {
                        String type =jsonObject.getString("todoType");
                        String resId = jsonObject.getString("todoId");
                        String create_user_no = jsonObject.getString("receiveUserId");
                        NotificationUtils.itemClick(mFragment, type,resId, create_user_no,-1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (searchItem.itemObj instanceof Organization) {
                    Organization organization = (Organization) searchItem.itemObj;
                    Intent intent = new Intent(mContext, VantopUserInfoActivity.class);
                    intent.putExtra(VantopUserInfoActivity.BUNDLE_STAFFNO, organization.staff_no);
                    mContext.startActivity(intent);
                } else if (searchItem.itemObj instanceof User) {
                    User user = (User) searchItem.itemObj;
                    UserUtils.enterUserInfo(mContext, user.userId, user.name, user.photo);
                } else if (searchItem.itemObj instanceof ChatGroup) {
                    ChatGroup chatGroup = (ChatGroup) searchItem.itemObj;
                    UsersMessagesFragment fragment = UsersMessagesFragment.newInstance(chatGroup, null);
                    mController.pushFragment(fragment);
                } else if (searchItem.itemObj instanceof List) {
                    List<ChatMessage> cmlist = (List<ChatMessage>) searchItem.itemObj;
                    if (cmlist.size() == 1) {
                        ChatMessage chatMessage = cmlist.get(0);
                        UsersMessagesFragment fragment = UsersMessagesFragment.newInstanceBySearch(chatMessage.group, chatMessage);
                        mController.pushFragment(fragment);
                    } else {
                        ChatMessage chatMessage = cmlist.get(0);
                        Bundle bundle = new Bundle();
                        bundle.putString("title", searchItem.title);
                        bundle.putString("keyword", searchItem.id);
                        bundle.putLong("gid", chatMessage.group.getId());
                        SearchResultFragment fragment = new SearchResultFragment();
                        fragment.setArguments(bundle);
                        mController.pushFragment(fragment);
                    }
                } else if (searchItem.itemObj instanceof ChatMessage) {
                    ChatMessage chatMessage = (ChatMessage) searchItem.itemObj;
                    UsersMessagesFragment fragment = UsersMessagesFragment.newInstanceBySearch(chatMessage.group, chatMessage);
                    mController.pushFragment(fragment);
                } else if (searchItem.itemObj instanceof MessageDB) {
                    MessageDB messageDB = (MessageDB) searchItem.itemObj;
                    PushMessage pushMessage = null;
                    NotificationExtension notificationExtension = null;
                    try {
                        pushMessage = JsonDataFactory.getData(PushMessage.class, new JSONObject(messageDB.content));
                        String extension = pushMessage.getJson().getString("extension");
                        if (!TextUtils.isEmpty(extension)) {
                            notificationExtension = JsonDataFactory.getData(NotificationExtension.class, new JSONObject(extension));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (mContext.getResources().getString(R.string.message_comment).equals(messageDB.title)) {
                        Intent intent = new Intent(mContext, CommentMessageListActivity.class);
                        mContext.startActivity(intent);
                    } else if (TypeUtils.VANTOPLEAVE.equals(pushMessage.msgTypeId) || TypeUtils.VANTOPSIGNCARD.equals(pushMessage.msgTypeId) || TypeUtils.VANTOPOVERTIME.equals(pushMessage.msgTypeId)) {
                        NotificationUtils.vantopItemClick(mContext, pushMessage, notificationExtension);
                    } else if (TypeUtils.MEETING.equals(pushMessage.msgTypeId)) {
                        long noticeID = messageDB.insert(mContext);
                        Intent intent = new Intent();
                        intent.setAction("com.vgtech.meeting.detail");
                        intent.putExtra("id", pushMessage.resId);
                        intent.putExtra("noticeid", noticeID);
                        mContext.startActivity(intent);
                    } else if (TypeUtils.APPROVAL.equals(pushMessage.msgTypeId)) {

                        Intent intent = new Intent(mContext, FlowHandleActivity.class);
                        intent.putExtra("processid", pushMessage.resId);
                        int type = -1;
                        if ("cc".equals(pushMessage.role)) {
                            type = 3;
                        } else if ("process".equals(pushMessage.role)) {
                            type = 2;
                        } else {
                            type = 1;
                        }
                        intent.putExtra("typer", type);
                        mContext.startActivity(intent);

                    } else if (TypeUtils.NEWEMPLOYEE.equals(pushMessage.msgTypeId)) {
                        if (notificationExtension != null && !TextUtils.isEmpty(notificationExtension.content)) {
                            UserUtils.enterUserInfo(mContext, notificationExtension.content, "", "");
                        }
                    } else
                        NotificationUtils.itemClick(((BaseActivity) mContext), messageDB.type, pushMessage.resId);
                }
                break;
        }
    }

}
