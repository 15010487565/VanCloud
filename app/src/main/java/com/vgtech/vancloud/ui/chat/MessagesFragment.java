
package com.vgtech.vancloud.ui.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.chenzhanyang.behaviorstatisticslibrary.BehaviorStatistics;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppModule;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.Node;
import com.vgtech.common.api.NotifyNoticePermissions;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.MessageDB;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.view.swipemenu.SwipeMenu;
import com.vgtech.common.view.swipemenu.SwipeMenuListView;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.api.MessageMap;
import com.vgtech.vancloud.models.Staff;
import com.vgtech.vancloud.presenter.AppModulePresenter;
import com.vgtech.vancloud.reciver.GetuiGTIntentService;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.LuntanListActivity;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.NoticeListActivity;
import com.vgtech.vancloud.ui.adapter.DataAdapter;
import com.vgtech.vancloud.ui.chat.controllers.AvatarController;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.chat.controllers.XmppController;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.module.announcement.AnnouncementListActivity;
import com.vgtech.vancloud.ui.module.todo.MessageListNewActivity;
import com.vgtech.vancloud.ui.module.todo.ToDoNotificationActivity;
import com.vgtech.vancloud.ui.view.groupimageview.NineGridImageView;
import com.vgtech.vancloud.utils.IpUtil;
import com.vgtech.vancloud.utils.SwipeMenuFactory;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.event.EventManager;
import roboguice.event.Observes;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

import static com.vgtech.vancloud.ui.chat.models.ChatGroup.GroupTypeChat;

/**
 * @author xuanqiang
 */
public class MessagesFragment extends RoboFragment implements View.OnClickListener, HttpListener<String> {
    private static final int LAST_POST_CALLBACK_ID = 2;
    @InjectView(R.id.messages_empty)
    View emptyView;
    @InjectView(R.id.messages_list)
    SwipeMenuListView listView;
    private NetworkManager mNetworkManager;
    private final int CALLBACK_TODOLIST = 1;

    private TextView todoCountView;
    private TextView todoContentView;
    private TextView todoTimeView;

    private TextView messageContentView;
    private TextView messageTimeView;
    private TextView messageAlertNumView;
//    private View messageCommentNew;
    public static final String RECEIVER_XMPPMESSAGE = "RECEIVER_XMPPMESSAGE";
    private TextView mLuntanContent;
    private TextView mLuntanTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_list, null);
        return view;
    }


    public TextView initRightTv(View v, String lable) {
        TextView rightTv = (TextView) v.findViewById(R.id.tv_right);
        rightTv.setText(lable);
        rightTv.setVisibility(View.VISIBLE);
        return rightTv;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new Adapter();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (controller.isFastDoubleClick()) {
                    return;
                }
                controller.pushFragment(UsersMessagesFragment.newInstance(adapter.dataSource.get(position - listView.getHeaderViewsCount()), null));
            }
        });
        // step 2. listener item click event
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu,
                                           int index) {
                switch (index) {
                    case 0: {
                        final ChatGroup group = adapter.dataSource.get(position);
                        group.deletefromMessage();
                        reloadData();
                    }
                    break;
                }
                return false;
            }
        });
        // set creator
        listView.setMenuCreator(SwipeMenuFactory.getSwipeMenuCreator(getActivity()));
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {
                ChatGroup group = adapter.dataSource.get(position - 1);
                showDeleatDialog(group);
                return true;
            }
        });

        listView.addHeaderView(initHeaderView(), null, false);

        listView.setAdapter(adapter);
//        getTodoList();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GetuiGTIntentService.RECEIVER_PUSH);
        intentFilter.addAction(RECEIVER_XMPPMESSAGE);
        intentFilter.addAction(MainActivity.RECEIVER_DRAFT);
        getActivity().registerReceiver(mReceiver, intentFilter);
        reloadData();
    }

    /**
     * 显示删除聊天提示Dialog
     *
     * @param group
     */
    public void showDeleatDialog(final ChatGroup group) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.DialogItemLargeFont);
        new AlertDialog.Builder(contextThemeWrapper).setTitle(group.getDisplayNick()).setItems(new String[]{getString(R.string.delete_chat)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    dialog.dismiss();
                    group.deletefromMessage();
                    reloadData();
                }
            }
        }).show();
    }

    private TextView mNoticeContentTv;
    private TextView mNoticeTimeTv;
    private TextView mNoticeCountTv;
    private TextView mMyNoticeContentTv;
    private TextView mMyNoticeTimeTv;
    private TextView mMyNoticeCountTv;

    /**
     * @return
     */
    public View initHeaderView() {
        View headView = getActivity().getLayoutInflater().inflate(R.layout.information_layout, null);
        if (AppModulePresenter.hasOpenedModule(getActivity(), AppModulePresenter.Type.notice.toString())) {
            headView.findViewById(R.id.notice_layput).setOnClickListener(this);
        } else {
            headView.findViewById(R.id.notice_layput).setVisibility(View.GONE);
        }
        if (AppModulePresenter.hasOpenedModule(getActivity(), AppModulePresenter.Type.luntan.toString())) {
            headView.findViewById(R.id.message_luntan_layput).setOnClickListener(this);
        } else {
            headView.findViewById(R.id.message_luntan_layput).setVisibility(View.GONE);
        }
        mNoticeContentTv = (TextView) headView.findViewById(R.id.notify_content);
        mNoticeTimeTv = (TextView) headView.findViewById(R.id.notify_time);
        mNoticeCountTv = (TextView) headView.findViewById(R.id.notify_num);

        mMyNoticeContentTv = (TextView) headView.findViewById(R.id.notification_content);
        mMyNoticeTimeTv = (TextView) headView.findViewById(R.id.notification_time);
        mMyNoticeCountTv = (TextView) headView.findViewById(R.id.notification_num);
        headView.findViewById(R.id.todo_layput).setOnClickListener(this);
        headView.findViewById(R.id.notification_layput).setOnClickListener(this);
        headView.findViewById(R.id.message_alert_layput).setOnClickListener(this);
        todoCountView = (TextView) headView.findViewById(R.id.todo_num);
        todoContentView = (TextView) headView.findViewById(R.id.todo_content);
        todoTimeView = (TextView) headView.findViewById(R.id.todo_time);
        messageAlertNumView = (TextView) headView.findViewById(R.id.message_alert_num);

        messageContentView = (TextView) headView.findViewById(R.id.message_alert_content);
        messageTimeView = (TextView) headView.findViewById(R.id.message_alert_time);
//        messageCommentNew = headView.findViewById(R.id.message_comment_new);

        mLuntanContent = (TextView) headView.findViewById(R.id.message_luntan_content);
        mLuntanTime = (TextView) headView.findViewById(R.id.message_luntan_time);
        return headView;
    }


    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private void reloadData() {
        new AsyncTask<Void, Void, List<ChatGroup>>() {
            @Override
            protected List<ChatGroup> doInBackground(Void... params) {
                if (getActivity() == null || getActivity().isFinishing()) {
                    return null;
                }
                List<ChatGroup> chatGroupList = ChatGroup.findAllbyChat(PrfUtils.getUserId(getActivity()), PrfUtils.getTenantId(getActivity()));
                return chatGroupList;
            }

            @Override
            protected void onPostExecute(List<ChatGroup> chatGroupList) {
                if (chatGroupList != null) {
                    adapter.dataSource.clear();
                    adapter.dataSource.addAll(chatGroupList);
                    adapter.notifyDataSetChanged();
                    emptyView.setVisibility(adapter.dataSource.size() == 0 ? View.VISIBLE : View.GONE);
                    controller.updateMessagesBarNum(adapter.dataSource);
                }
            }
        }.execute();
    }
    //公告
    private void updateNotice() {
        new AsyncTask<Void, Void, MessageMap>() {
            @Override
            protected MessageMap doInBackground(Void... params) {
                MessageMap messageMap = new MessageMap();
                if (getActivity() == null || getActivity().isFinishing()){
                    return null;
                }else {
                    List<MessageDB> messageDBList = MessageDB.queryNoticeUnRead(getActivity());
                    int count = PrfUtils.getMessageCount(getActivity(), PrfUtils.MESSAGE_GONGGAO);
                    if (count > 0 && !messageDBList.isEmpty()) {
                        messageMap.lastMessage = messageDBList.get(0);
                        messageMap.newMessageCount = count;
                        return messageMap;
                    }else {
                        return null;
                    }

                }

            }

            @Override
            protected void onPostExecute(MessageMap messageMap) {
                if (getActivity() == null || getActivity().isFinishing())
                    return;
                if (messageMap != null && messageMap.lastMessage != null) {
                    mNoticeContentTv.setText(Html.fromHtml(messageMap.lastMessage.title));
                    mNoticeTimeTv.setText(Utils.getInstance(getActivity()).dateFormat(messageMap.lastMessage.timestamp));
                    if (messageMap.newMessageCount > 0) {
                        mNoticeCountTv.setVisibility(View.VISIBLE);
                        mNoticeCountTv.setText(messageMap.newMessageCount + "");
                    } else {
                        mNoticeCountTv.setVisibility(View.GONE);
                    }
                } else {
                    mNoticeContentTv.setText(getString(R.string.todo_default));
                    mNoticeTimeTv.setText("");
                    mNoticeCountTv.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    private void updateMyNotice() {
        new AsyncTask<Void, Void, MessageMap>() {
            @Override
            protected MessageMap doInBackground(Void... params) {
                if (getActivity() == null || getActivity().isFinishing())
                    return null;
                MessageMap messageMap = new MessageMap();
                List<MessageDB> messageDBList = MessageDB.queryMyNoticeUnRead(getActivity());
                int count = PrfUtils.getMessageCount(getActivity(), PrfUtils.MESSAGE_NOTICE);
                if (count > 0 && !messageDBList.isEmpty()) {
                    messageMap.lastMessage = messageDBList.get(0);
                    messageMap.newMessageCount = count;
                }
                return messageMap;
            }

            @Override
            protected void onPostExecute(MessageMap messageMap) {
                if (getActivity() == null || getActivity().isFinishing()|| messageMap == null)
                    return;
                if (messageMap !=null && messageMap.lastMessage != null) {
                    mMyNoticeContentTv.setText(Html.fromHtml(messageMap.lastMessage.title));
                    mMyNoticeTimeTv.setText(Utils.getInstance(getActivity()).dateFormat(messageMap.lastMessage.timestamp));
                    if (messageMap.newMessageCount > 0) {
                        mMyNoticeCountTv.setVisibility(View.VISIBLE);
                        mMyNoticeCountTv.setText(messageMap.newMessageCount + "");
                    } else {
                        mMyNoticeCountTv.setVisibility(View.GONE);
                    }
                } else {
                    mMyNoticeContentTv.setText(getString(R.string.todo_default));
                    mMyNoticeTimeTv.setText("");
                    mMyNoticeCountTv.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    @Override
    public void onDetach() {
        eventManager.unregisterObserver(this, OnEvent.class);
        super.onDetach();
    }

    private class Adapter extends DataAdapter<ChatGroup> {
        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.messages_item, null);
//                assert convertView != null;
                viewHolder.avatarView = (SimpleDraweeView) convertView.findViewById(R.id.avatar);
                viewHolder.avatarContainer = (NineGridImageView) convertView.findViewById(R.id.avatar_container);
                viewHolder.numButton = (TextView) convertView.findViewById(R.id.messages_item_num);
                viewHolder.nameLabel = (TextView) convertView.findViewById(R.id.messages_item_name);
                viewHolder.timeLabel = (TextView) convertView.findViewById(R.id.messages_item_time);
                viewHolder.contentLabel = (TextView) convertView.findViewById(R.id.messages_item_content);
                viewHolder.failView = convertView.findViewById(R.id.messages_item_fail);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ChatGroup group = dataSource.get(position);
            setView(viewHolder, group);
            return convertView;
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        class ViewHolder {
            SimpleDraweeView avatarView;
            NineGridImageView avatarContainer;
            TextView numButton;
            TextView nameLabel;
            TextView timeLabel;
            TextView contentLabel;
            View failView;
        }


        void setView(ViewHolder viewHolder, ChatGroup group) {
            viewHolder.failView.setVisibility(group.isFailure() ? View.VISIBLE : View.GONE);

            String name = group.peopleNum > 0 ? group.getDisplayNick() + "(" + group.peopleNum + getString(R.string.people) + ")" : group.getDisplayNick();
            viewHolder.nameLabel.setText(name);
            viewHolder.timeLabel.setText(group.getDisplayTime());
            viewHolder.contentLabel.setText(EmojiFragment.getEmojiContentWithAt(getActivity(), viewHolder.contentLabel.getTextSize(), group.getContent(getResources())));
            List<String> avatars = null;
            if (GroupTypeChat.equals(group.type)) {
                avatars = new ArrayList<String>(1);
                avatars.add(group.avatar);
            } else {
                try {
                    avatars = new Gson().fromJson(group.avatar, new TypeToken<List<String>>() {
                    }.getType());
                } catch (JsonSyntaxException ignored) {
                    if (!TextUtils.isEmpty(group.avatar)) {
                        String[] avatarArray = group.avatar.split(",");
                        avatars = new ArrayList(Arrays.asList(avatarArray));
                    }
                }
                if (avatars == null) {
                    avatars = new ArrayList<String>(1);
                    avatars.add("");
                }
            }
            String serviceId = PrfUtils.getPrfparams(getActivity(), Constants.SERVICE_USERID);
            if (!TextUtils.isEmpty(serviceId) && serviceId.equals(group.name)) {
                viewHolder.avatarView.setImageResource(R.drawable.icon_customer);
                viewHolder.avatarView.setVisibility(View.VISIBLE);
                viewHolder.avatarContainer.setVisibility(View.GONE);
            } else
                avatarController.setAvatarContainer(viewHolder.avatarView, viewHolder.avatarContainer, avatars);
            viewHolder.numButton.setText(String.valueOf(group.unreadNum < 100 ? group.unreadNum : "N"));
            viewHolder.numButton.setVisibility(group.unreadNum > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private static final int REQUEST_CODE_USERSELECT = 1001;
    private static final int REQUEST_CODE_GROUPSELECT = 1002;
    private static final int REQUEST_CODE_TODO = 1003;
    private static final int REQUEST_CODE_MESSAGE = 1004;

    @Override
    public void onClick(View view) {
        if (controller.isFastDoubleClick()) {
            return;
        }

        SharedPreferences preferences = PrfUtils.getSharePreferences(this.getActivity());
        String userId = preferences.getString("uid", "");
        String employee_no = preferences.getString("user_no", "");
        String tenantId = preferences.getString("tenantId", "");

        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("employee_no", employee_no);
        params.put("tenant_id", tenantId);
        params.put("operation_ip", IpUtil.getIpAddressString());
        params.put("operation_url", "");

        switch (view.getId()) {
            case R.id.notice_layput: {//公告
                List<AppModule> modules = AppModulePresenter.getOriModules(this.getActivity(), "moudle_permissions");
                //是否显示公告评论和点赞
                boolean isShowNotice = false;
                for (AppModule module : modules) {
                    if ("notice".equals(module.tag)) {
                        params.put("permission_id", module.id);
                        try {
                            //
                            JSONObject json = module.getJson();
                            List<NotifyNoticePermissions> permissions = JsonDataFactory.getDataArray(NotifyNoticePermissions.class, module.getJson().getJSONArray("permissions"));

                            if (permissions == null || permissions.size() <= 0) {
                                isShowNotice = false;//隐藏公告评论和点赞
                            } else {
                                isShowNotice = true;//显示公告评论和点赞
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent announcement = new Intent(getActivity(), AnnouncementListActivity.class);
                announcement.putExtra("isShowNotice", isShowNotice);
                startActivity(announcement);
            }
            break;
            case R.id.message_luntan_layput: {//论坛
                List<AppModule> modules = AppModulePresenter.getOriModules(this.getActivity(), "moudle_permissions");
                for (AppModule module : modules) {
                    if ("notice".equals(module.tag)) {
                        params.put("permission_id", module.id);
                        break;
                    }
                }
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent luntan = new Intent(getActivity(), LuntanListActivity.class);
                startActivity(luntan);
            }
            break;
            case R.id.notification_layput: {//通知
                List<AppModule> modules = AppModulePresenter.getOriModules(this.getActivity(), "moudle_permissions");
                for (AppModule module : modules) {
                    if ("tongzhi".equals(module.tag)) {
                        params.put("permission_id", module.id);
                        break;
                    }
                }
                BehaviorStatistics.getInstance().startBehavior(params);

                Intent announcement = new Intent(getActivity(), NoticeListActivity.class);
                startActivity(announcement);
            }
            break;
            case R.id.todo_layput: {//待办
                Intent intent = new Intent(getActivity(), ToDoNotificationActivity.class);
                startActivityForResult(intent, REQUEST_CODE_TODO);

            }
            break;
            case R.id.message_alert_layput: {//信息
//                Intent intent = new Intent(getActivity(), MessageListActivity.class);
                Intent intent = new Intent(getActivity(), MessageListNewActivity.class);
                startActivityForResult(intent, REQUEST_CODE_MESSAGE);
            }
            break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity.chatUser != null) {
            if (Constants.SERVICE_USERID.equals(mainActivity.chatUser.userid)) {
                List<Staff> contactses = new ArrayList<Staff>();
                Staff staff = new Staff(String.valueOf(mainActivity.chatUser.userid), String.valueOf(mainActivity.chatUser.userid), mainActivity.chatUser.name, mainActivity.chatUser.photo, PrfUtils.getTenantId(getActivity()));
                contactses.add(staff);
                xmpp.chat(contactses, null);
            } else {
                List<Staff> contactses = new ArrayList<Staff>();
                Staff staff = new Staff(String.valueOf(mainActivity.chatUser.userid), String.valueOf(mainActivity.chatUser.userid), mainActivity.chatUser.name, mainActivity.chatUser.photo, PrfUtils.getTenantId(getActivity()));
                contactses.add(staff);
                xmpp.chat(contactses, null);
            }
            mainActivity.chatUser = null;
        } else if (mainActivity.chatGroup != null) {
            controller.pushUserMessagesFragment(UsersMessagesFragment.newInstance(mainActivity.chatGroup, null));
            mainActivity.chatGroup = null;
        }
        getMessageData();
        updateNotice();
        updateMyNotice();
        getTodoList();
        getLastPost();
        if (adapter != null)
            controller.updateMessagesBarNum(adapter.dataSource);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_USERSELECT:
                if (resultCode == Activity.RESULT_OK) {
                    ArrayList<Node> userSelectList = data.getParcelableArrayListExtra("select");
                    if (userSelectList != null && !userSelectList.isEmpty()) {
                        List<Staff> contactses = new ArrayList<Staff>();
                        for (Node node : userSelectList) {
                            if (node.isUser()) {
                                Staff staff = new Staff(String.valueOf(node.getId()), String.valueOf(node.getId()), node.getName(), node.getPhoto(), PrfUtils.getTenantId(getActivity()));
                                contactses.add(staff);
                            }
                        }
                        if (contactses.isEmpty()) {
                            Toast.makeText(getActivity(), R.string.toast_chatgroup_empty, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        xmpp.chat(contactses, null);
                    }
                }
                break;
            case REQUEST_CODE_TODO:
                if (resultCode == Activity.RESULT_OK) {
                    getTodoList();
                }
                break;
            case REQUEST_CODE_MESSAGE:
                if (resultCode == Activity.RESULT_OK) {
                    getMessageData();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

//    @Override
//    public void selectedContacts(final List<Staff> contactses, final ChatGroup group) {
//        xmpp.chat(contactses, group);
//    }

    @SuppressWarnings("UnusedDeclaration")
    void handleEvent(@Observes OnEvent event) {
        eventHandler.sendMessage(eventHandler.obtainMessage(0, event));
    }

    private Handler eventHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (getActivity() != null)
                reloadData();
        }
    };

    private Adapter adapter;
    @Inject
    AvatarController avatarController;
    @Inject
    XmppController xmpp;
    @Inject
    EventManager eventManager;
    @Inject
    public Controller controller;


    public void getTodoList() {
        mNetworkManager = ((VanCloudApplication) getActivity().getApplication()).getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(getActivity()));
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_INDEX_LASTDATA), params, getActivity());
        mNetworkManager.load(CALLBACK_TODOLIST, path, this, false);
    }

    public void getLastPost() {
        HashMap<String, String> params = new HashMap<>();
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        String url = ApiUtils.generatorUrl(getActivity(), URLAddr.LAST_POST);
        NetworkPath path = new NetworkPath(url, params, getActivity());
        mNetworkManager.load(LAST_POST_CALLBACK_ID, path, this);
    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
//        Log.e("TAG_消息顶部","safe="+safe+";callbackId="+callbackId);
        if (!safe) {
            if (callbackId == CALLBACK_TODOLIST) {
                todoContentView.setText(getString(R.string.todo_default));
                todoTimeView.setText("");
            }
            return;
        }
        JSONObject rootDataJson = rootData.getJson();
        switch (callbackId) {
            case CALLBACK_TODOLIST:
                try {
                    JSONArray rootArray = rootDataJson.getJSONArray("data");
                    int totalCount = 0;
                    for (int i = 0; i < rootArray.length(); i++) {
                        JSONObject itemObject = rootArray.getJSONObject(i);
                        String type = itemObject.getString("type");
                        String data = itemObject.optString("data");
                        if ("notice".equals(type))//公告
                        {
                            try {
                                if (itemObject == null || "".equals(itemObject)) {
                                    mNoticeContentTv.setText(getString(R.string.todo_default));
                                    mNoticeTimeTv.setText("");
                                    mNoticeCountTv.setVisibility(View.GONE);
                                } else {
                                    int count = itemObject.getInt("count");
//                                    Log.e("TAG_消息公告","count="+count);
                                    totalCount += count;
                                    if (count > 0) {
                                        mNoticeCountTv.setVisibility(View.VISIBLE);
                                        mNoticeCountTv.setText(count + "");
                                    } else {
                                        mNoticeCountTv.setVisibility(View.GONE);
                                    }
                                    if (!TextUtils.isEmpty(data)) {
                                        JSONObject subObject = itemObject.getJSONObject("data");

                                        if (subObject == null || "".equals(subObject)) {

                                            mNoticeContentTv.setText(getString(R.string.todo_default));
                                            mNoticeTimeTv.setText("");

                                        } else {
                                            String title = subObject.getString("title");
                                            String publishTime = subObject.getString("publishTime");
                                            mNoticeContentTv.setText(Html.fromHtml(title));
                                            mNoticeTimeTv.setText(Utils.getInstance(getActivity()).dateFormat(Long.parseLong(publishTime)));
//                                int count = PrfUtils.getMessageCount(getActivity(), PrfUtils.MESSAGE_GONGGAO);

                                        }
                                    }

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                mNoticeContentTv.setText(getString(R.string.todo_default));
                                mNoticeTimeTv.setText("");
                                mNoticeCountTv.setVisibility(View.GONE);
                            }
                        } else if ("todo".equals(type))//待办
                        {
                            try {


                                if (itemObject == null || "".equals(itemObject)) {
                                    todoContentView.setText(getString(R.string.todo_default));
                                    todoTimeView.setText("");
                                    todoCountView.setVisibility(View.GONE);
                                } else {
                                    int count = itemObject.getInt("count");
//                                    Log.e("TAG_消息待办","count="+count);
                                    if (count > 0) {
                                        todoCountView.setVisibility(View.VISIBLE);
                                        todoCountView.setText(count + "");
                                    } else {
                                        todoCountView.setVisibility(View.GONE);
                                    }
                                    totalCount += count;
                                    PrfUtils.setMessageCountCount(getActivity(), PrfUtils.MESSAGE_TODO, count);

                                    if (!TextUtils.isEmpty(data)) {
                                        JSONObject subObject = itemObject.getJSONObject("data");
                                        if (subObject == null || "".equals(subObject)) {

                                            todoContentView.setText(getString(R.string.todo_default));
                                            todoTimeView.setText("");
                                        } else {
                                            String title = subObject.getString("title");
                                            String publishTime = subObject.getString("createDate");
                                            todoContentView.setText(Html.fromHtml(title));
                                            todoTimeView.setText(Utils.getInstance(getActivity()).dateFormat(Long.parseLong(publishTime)));

                                        }
                                    }

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                todoContentView.setText(getString(R.string.todo_default));
                                todoTimeView.setText("");
                                todoCountView.setVisibility(View.GONE);
                            }

                        } else if ("mynotice".equals(type))//通知
                        {
                            try {

                                if (itemObject == null || "".equals(itemObject)) {
                                    mMyNoticeContentTv.setText(getString(R.string.todo_default));
                                    mMyNoticeTimeTv.setText("");
                                    mMyNoticeCountTv.setVisibility(View.GONE);
                                } else {
                                    int count = itemObject.getInt("count");
                                    totalCount += count;
                                    if (count > 0) {
                                        mMyNoticeCountTv.setVisibility(View.VISIBLE);
                                        mMyNoticeCountTv.setText(count + "");
                                    } else {
                                        mMyNoticeCountTv.setVisibility(View.GONE);
                                    }
                                    if (!TextUtils.isEmpty(data)){
                                        JSONObject subObject = itemObject.getJSONObject("data");
                                        if (subObject == null || "".equals(subObject)) {
                                            mMyNoticeContentTv.setText(getString(R.string.todo_default));
                                            mMyNoticeTimeTv.setText("");
                                        } else {
                                            String title = subObject.getString("mynotice_subject");
                                            String publishTime = subObject.getString("create_time");
                                            mMyNoticeContentTv.setText(Html.fromHtml(title));
                                            mMyNoticeTimeTv.setText(Utils.getInstance(getActivity()).dateFormat(Long.parseLong(publishTime)));

                                        }
                                    }

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                mMyNoticeContentTv.setText(getString(R.string.todo_default));
                                mMyNoticeTimeTv.setText("");
                                mMyNoticeCountTv.setVisibility(View.GONE);
                            }
                        }
                    }

                    ((MainActivity) getActivity()).updateTabNums(0, totalCount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case LAST_POST_CALLBACK_ID:
                if (!safe) {
                    mLuntanContent.setText(getString(R.string.todo_default));
                    mLuntanTime.setText("");
                    return;
                }
                try {

                    String dataOpt = rootDataJson.optString("data");

                    if (!TextUtils.isEmpty(dataOpt)) {
                        JSONObject data = rootDataJson.getJSONObject("data");

                        String title = data.getString("title");
                        String createTime = data.getString("createTime");
//                        Log.e("TAG_消息顶部","mLuntanContent="+title);
                        mLuntanContent.setText(title);
                        mLuntanTime.setText(Utils.getInstance(getActivity()).dateFormat(Long.parseLong(createTime)));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
//                    Log.e("TAG_消息顶部","Exception=");
                    mLuntanContent.setText(getString(R.string.todo_default));
                    mLuntanTime.setText("");
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
    //消息
    public void getMessageData() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                getActivity(). runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MessageMap messageMap = new MessageMap();
                        List<MessageDB> messageDBs = MessageDB.queryLastMessage(getActivity());//获取全部消息

                        if (!messageDBs.isEmpty())
                            messageMap.lastMessage = messageDBs.get(0);
                        int unReadCount = PrfUtils.getMessageCount(getActivity(), PrfUtils.MESSAGE_MSG);
                        if (messageMap.lastMessage != null) {
                            if (Constants.DEBUG)
                            Log.e("TAG_首页消息","messageDBs==="+messageMap.lastMessage.operationType);
                            MessageDB lastMessage = messageMap.lastMessage;
                            messageContentView.setText(Html.fromHtml(lastMessage.title));
                            messageTimeView.setText(Utils.getInstance(getActivity()).dateFormat(lastMessage.timestamp));
//                            if (messageMap.newCommentCount > 0) {
//                                messageCommentNew.setVisibility(View.VISIBLE);
//                            } else {
//                                messageCommentNew.setVisibility(View.GONE);
//                            }
                            if (unReadCount > 0) {
                                messageAlertNumView.setVisibility(View.VISIBLE);
//                                messageAlertNumView.setText(messageMap.newMessageCount + "");
                            } else {
                                messageAlertNumView.setVisibility(View.GONE);
                            }

                        } else {
                            messageContentView.setText(getString(R.string.todo_default));
                            messageTimeView.setText("");
                            messageAlertNumView.setVisibility(View.GONE);
//                            messageCommentNew.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
        if (mReceiver != null)
            getActivity().unregisterReceiver(mReceiver);
        mReceiver = null;
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("TAG_首页推送","action="+action);
            if (RECEIVER_XMPPMESSAGE.equals(action)) {
                reloadData();
            } else if (GetuiGTIntentService.RECEIVER_PUSH.equals(action)) {
                getTodoList();
                getMessageData();
                updateNotice();
                updateMyNotice();
                controller.updateMessagesBarNum(adapter.dataSource);
            } else if (MainActivity.RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_WORK_REPORT://工作汇报点评
                    case PublishTask.PUBLISH_TASK_CONDUCT://任务处理
                    case PublishTask.PUBLISH_SCHEDULE_CONDUCT://日程处理
                    case PublishTask.PUBLISH_FLOW_CONDUCT://流程处理
                    case 39://问卷
                    case 38://入职审批
                        getTodoList();
                        break;
                }
            }
        }
    };
}
