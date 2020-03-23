package com.vgtech.vancloud.ui.chat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.vgtech.common.Constants;
import com.vgtech.vancloud.R;
import com.vgtech.common.URLAddr;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.common.api.RootData;
import com.vgtech.vancloud.models.ChatGroupStaffs;
import com.vgtech.vancloud.models.Staff;
import com.vgtech.vancloud.models.Subject;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.chat.controllers.MediaController;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.chat.models.ChatMessage;
import com.vgtech.common.view.IphoneDialog;
import com.vgtech.common.FileCacheUtils;
import com.vgtech.common.PrfUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import roboguice.event.Observes;
import roboguice.inject.InjectView;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;
import roboguice.util.Strings;

import static com.vgtech.vancloud.models.Subject.File.TYPE_AUDIO;
import static com.vgtech.vancloud.models.Subject.File.TYPE_PICTURE;
import static com.vgtech.vancloud.ui.chat.OnEvent.EventType;
import static com.vgtech.vancloud.ui.chat.models.ChatMessage.MessageTypeFile;
import static com.vgtech.vancloud.ui.chat.models.ChatMessage.MessageTypeGps;
import static com.vgtech.vancloud.ui.chat.models.ChatMessage.MessageTypeGroupAddMembers;
import static com.vgtech.vancloud.ui.chat.models.ChatMessage.MessageTypeGroupDelMember;
import static com.vgtech.vancloud.ui.chat.models.ChatMessage.MessageTypeGroupModifyName;
import static com.vgtech.vancloud.ui.chat.models.ChatMessage.MessageTypeNormal;

/**
 * @author xuanqiang
 */
public class UsersMessagesFragment extends AbstractUsersMessagesFragment implements MessageInputListener, BackPressedListener, HttpListener<String> {
    @InjectView(R.id.actionbar_right)
    ImageView staffButton;

    public static UsersMessagesFragment newInstance(final ChatGroup group, final ChatMessage forwardMessage) {

        Bundle bundle = new Bundle();
        bundle.putSerializable("group", group);
        bundle.putSerializable("forwardMessage", forwardMessage);
        UsersMessagesFragment fragment = new UsersMessagesFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static UsersMessagesFragment newInstanceForward(final ChatGroup group, String firstMessage) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("group", group);
        bundle.putSerializable("firstMessage", firstMessage);
        UsersMessagesFragment fragment = new UsersMessagesFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static UsersMessagesFragment newInstanceBySearch(final ChatGroup group, final ChatMessage searchMessage) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("group", group);
        bundle.putSerializable("searchMessage", searchMessage);
        UsersMessagesFragment fragment = new UsersMessagesFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private boolean mService;
    private String mFirstMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        group = (ChatGroup) bundle.getSerializable("group");
        String serviceId = PrfUtils.getPrfparams(getActivity(), Constants.SERVICE_USERID);
        if (Constants.SERVICE_USERID.equals(group.name)) {
            mService = true;
            String serviceName = PrfUtils.getPrfparams(getActivity(), Constants.SERVICE_NAME);
            if (!TextUtils.isEmpty(serviceId) && !TextUtils.isEmpty(serviceName)) {
                Staff staff = new Staff(serviceId, serviceId, serviceName, "", PrfUtils.getTenantId(getActivity()));
                group = ChatGroup.fromStaff(staff, PrfUtils.getUserId(getActivity()), PrfUtils.getTenantId(getActivity()));
            }
        } else if (!TextUtils.isEmpty(serviceId) && serviceId.equals(group.name)) {
            mService = true;
        }
        if (mService) {
            loadServiceUserId();
        }
        forwardMessage = (ChatMessage) bundle.getSerializable("forwardMessage");
        mFirstMessage = bundle.getString("firstMessage");
        if (!TextUtils.isEmpty(mFirstMessage))
            onMessageSend(mFirstMessage, null);
    }

    private static final int CALLBACK_SERVICEID = 1;
    private IphoneDialog iphoneDialog;

    private void loadServiceUserId() {
        iphoneDialog = new IphoneDialog(getActivity());
        iphoneDialog.setMessage(getString(R.string.dataloading));
        iphoneDialog.show();
        NetworkManager mNetWorkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_CUSTOMERSERVICE), params, getActivity());
        mNetWorkManager.load(CALLBACK_SERVICEID, path, this);
    }

    private int MAX_Y = 10;
    private int MAX_X = 10;
    private float mDownX;
    private float mDownY;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//    staffButton.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.wg_xx_top_xx), null);
        staffButton.setImageResource(R.drawable.btn_add_user);
        if (group != null && ChatGroup.GroupTypeGroup.equals(group.type) && !group.isExit || mService)
            staffButton.setVisibility(View.GONE);
        else {
            staffButton.setVisibility(View.VISIBLE);
        }
        staffButton.setOnClickListener(this);
        messageInput = new MessageInputFragment();
        messageInput.setListener(this);
        messageInput.setChatGroup(group);
        messageInput.bindToContentView(listView);
        adapter = new UsersMessagesAdapter(this);
        messageInput.setAdapter(adapter);
        controller.replaceFragment(R.id.message_input, messageInput);
    }

    @Override
    public void chageState() {
        adapter.scrollToLast();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        adapter.setInputView(messageInput.getInputView());
        ChatMessage searchMessage = (ChatMessage) getArguments().getSerializable("searchMessage");
        if (searchMessage != null) {
            adapter.dataSource.addAll(group.findMessagesByMinId(searchMessage.getId()));
        } else {
            adapter.dataSource.addAll(group.getMessages(-1));
        }
        if (group.unreadNum > 0) {
            group.clearUnreadNum();
            eventManager.fire(new OnEvent(OnEvent.EventType.GROUP_MODIFY, group));
        }
        loadGroupView();
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                messageInput.hideMoreView();
                int action = MotionEventCompat.getActionMasked(ev);
                action = ev.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = ev.getX();
                        mDownY = ev.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dy = Math.abs((ev.getY() - mDownY));
                        float dx = Math.abs((ev.getX() - mDownX));
                        if (dy < MAX_Y && dx < MAX_X) {
                            return true;
                        }
                        break;
                }
                return false;
            }
        };
        listView.getRefreshableView().setOnTouchListener(touchListener);
        TextView textView = new TextView(getActivity());
        textView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.convertDipOrPx(getActivity(), 5)));
        listView.getRefreshableView().addFooterView(textView);
        listView.setAdapter(adapter);
        if (searchMessage != null) {
            adapter.scrollToPosition(0);
        } else {
            adapter.scrollToLast();
        }

        if (forwardMessage != null) {
            new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.confirm_forward_chat)).setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (MessageTypeFile.equals(forwardMessage.type)) {
                        Subject.File subjectFile = forwardMessage.getFile();
                        File file = null;
                        String cachePath = null;
                        String type = null;
                        if (Strings.notEmpty(subjectFile.getFilePath())) {
                            file = new File(subjectFile.getFilePath());
                        }
                        if (Subject.File.TYPE_PICTURE.equals(subjectFile.ext)) {
                            if (file == null) {
                                ImageRequest imageRequest = ImageRequest.fromUri(subjectFile.url);
                                CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(imageRequest, null);
                                BinaryResource resource = ImagePipelineFactory.getInstance().getMainDiskStorageCache().getResource(cacheKey);
                                file = ((FileBinaryResource) resource).getFile();
                            }
                            cachePath = FileCacheUtils.getImageDir(getActivity()) + "/" + System.currentTimeMillis() + ".jpg";
                            type = TYPE_PICTURE;
                        } else if (Subject.File.TYPE_AUDIO.equals(subjectFile.ext)) {
                            cachePath = FileCacheUtils.getAudioDir(getActivity()) + "/" + System.currentTimeMillis() + ".amr";
                            type = TYPE_AUDIO;
                        }
                        if (cachePath != null) {
                            try {
                                if (file != null && file.exists()) {
                                    FileCopyUtils.copy(file, new File(cachePath));
                                }
                                onMessageSend(null, new Subject.File(cachePath, type, subjectFile.getDuration(), subjectFile.url));
                            } catch (IOException e) {
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                Ln.e(e);
                            }
                        }
                    } else if (MessageTypeGps.equals(forwardMessage.type)) {
                        Subject.Gps gps = forwardMessage.getGps();
                        onMessageSend(forwardMessage.content, new Subject.Gps(gps.latitude, gps.longitude));
                    } else {
                        onMessageSend(forwardMessage.content, null);
                    }
                }
            }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    }

    protected void exitGroup() {
        if (!group.isExit) {
            if (staffButton != null)
                staffButton.setVisibility(View.GONE);
            //TODO
//      staffButton.setVisibility(View.INVISIBLE);
//      messageInput.hideMoreView();
//      getFragmentManager().beginTransaction().hide(messageInput).commitAllowingStateLoss();
        }
    }

    private void enterGroup() {
        if (group.isExit) {
            staffButton.setVisibility(View.VISIBLE);


//      getFragmentManager().beginTransaction().show(messageInput).commitAllowingStateLoss();
        }
    }

    @Override
    public void onMessageSend(final String content, final Object model) {
        Log.e("TAG_发送","content="+content+";model="+(model == null));
        if (model != null) {
            if (model instanceof Subject.File) {
                xmpp.sendFile(group, (Subject.File) model, null);
            } else if (model instanceof Subject.Gps) {
                UserAccount account = controller.account();
                final Subject subject = new Subject(account, MessageTypeGps, group.nick);
                subject.gps = (Subject.Gps) model;
                new RoboAsyncTask<Void>(getActivity()) {
                    @Override
                    protected void onSuccess(Void aVoid) throws Exception {
                    }

                    @Override
                    public Void call() throws Exception {
                        xmpp.send(group, subject, content, null);
                        return null;
                    }
                }.execute();
            }
        } else {
            new RoboAsyncTask<Void>(getActivity()) {
                @Override
                protected void onSuccess(Void aVoid) throws Exception {
                    messageInput.getEditText().setText("");
                }

                @Override
                public Void call() throws Exception {
                    Log.e("TAG_发送","group="+group.toString()+";MessageTypeNormal="+MessageTypeNormal);
                    xmpp.send(group, MessageTypeNormal, content);
                    return null;
                }
            }.execute();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        VanCloudApplication vanCloudApplication = getApplication();
        NetworkManager networkManager = vanCloudApplication.getNetworkManager();
        networkManager.cancle(this);
    }

    @Override
    public void onDetach() {
        MediaController.stopAudio();
        if (controller != null)
            controller.removeFragmentByHandler(R.id.message_input);
        if (eventManager != null)
            eventManager.unregisterObserver(this, OnEvent.class);
        super.onDetach();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            controller.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        } else if (!isDetached()) {
            if (getView() != null) {
                imManager.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }

        }
    }

    @Override
    public void onClick(View view) {
        if (view == staffButton) {
            controller.pushFragment(MessageStaffFragment.newInstance(group));
        } else {
            super.onClick(view);
        }
    }

    private void loadGroupView() {

        if (group == null || TextUtils.isEmpty(group.name) || !(ChatGroup.GroupTypeGroup.equals(group.type))) {
            return;
        }
        VanCloudApplication vanCloudApplication = getApplication();
        NetworkManager networkManager = vanCloudApplication.getNetworkManager();
        Map<String, String> postValues = new HashMap<>();
        postValues.put("room", group.name);
        postValues.put("ownid", PrfUtils.getUserId(getActivity()));
        postValues.put("tenantid", PrfUtils.getTenantId(getActivity()));
        postValues.put("token", PrfUtils.getToken(getActivity()));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_VCHAT_GROUPMEMBERS), postValues, getActivity());
        networkManager.load(1, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
                if (getActivity() == null || getActivity().isFinishing())
                    return;
                String tenantId = PrfUtils.getTenantId(getActivity());
                if (rootData.isSuccess()) {
                    try {
                        JSONObject rootObject = rootData.getJson().getJSONObject("data");
                        JSONArray jsonArray = rootObject.getJSONArray("members");
                        ChatGroupStaffs groupStaffs = new ChatGroupStaffs();
                        groupStaffs.staffs = new ArrayList<Staff>(0);
                        groupStaffs.groupNick = rootObject.getString("roomname");
                        groupStaffs.isExit = rootObject.getBoolean("isExit");
                        groupStaffs.creator = rootObject.getString("owner");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String userid = jsonObject.getString("userid");
                            String photo = jsonObject.getString("photo");
                            String name = jsonObject.getString("name");
                            Staff staff = new Staff(userid, userid, name, photo, tenantId);
                            groupStaffs.staffs.add(staff);
                        }
                        group.isExit = groupStaffs.isExit;
                        group.peopleNum = groupStaffs.staffs.size();
                        group.groupNick = groupStaffs.groupNick;
                        group.setCreator(groupStaffs.creator);
                        if (titleView != null)
                            titleView.setText(getTitle());
                        if (!group.isExit) {
                            staffButton.setVisibility(View.GONE);
                        } else {
                            staffButton.setVisibility(View.VISIBLE);
                        }
                        group.save();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
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
    public void onBackPressed() {
        if (this instanceof UsersMessagesFragment)
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        else
            controller.navigationToMessageFragment();
    }

    @SuppressWarnings("UnusedDeclaration")
    void handleEvent(@Observes OnEvent event) {
        eventHandler.sendMessage(eventHandler.obtainMessage(0, event));
    }

    //region event handler
    private Handler eventHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (getActivity() == null)
                return;
            OnEvent event = (OnEvent) msg.obj;
            assert event != null;
            ChatGroup g = event.getChatGroup();
            if (g == null || !g.name.equals(group.name)) {
                return;
            }
            group = g;
            if (ChatGroup.GroupTypeGroup.equals(group.type) && !group.isExit || mService)
                staffButton.setVisibility(View.GONE);
            else {
                staffButton.setVisibility(View.VISIBLE);
            }
            if (event.type == EventType.GROUP_MODIFY) {
                titleView.setText(getTitle());
                return;
            }
            if (event.type == EventType.CLEAR_MESSAGE_RECORD) {
                adapter.dataSource.clear();
                adapter.notifyDataSetChanged();
                return;
            }
            if (event.type == EventType.LEAVE_GROUP) {
                exitGroup();
                return;
            }
            ChatMessage chatMessage = g.getMessage();
            if (chatMessage != null) {
                if (MessageTypeGroupModifyName.equals(chatMessage.type)) {
                    titleView.setText(getTitle());
                } else if (MessageTypeGroupDelMember.equals(chatMessage.type)) {
                    exitGroup();
                } else if (MessageTypeGroupAddMembers.equals(chatMessage.type)) {
                    enterGroup();
                }
                if (event.type == EventType.NEW) {
                    adapter.dataSource.add(chatMessage);
                    adapter.notifyDataSetChanged();
                    group.clearUnreadNum();
                    eventManager.fire(new OnEvent(EventType.GROUP_MODIFY, group));
                    adapter.scrollToLast();
                } else if (event.type == EventType.MESSAGE_MODIFY) {
                    int idx = adapter.dataSource.indexOf(chatMessage);
                    if (idx != -1) {
                        adapter.dataSource.set(idx, chatMessage);
                        adapter.notifyDataSetChanged();
                    }
                } else if (event.type == EventType.MESSAGE_DELETE) {
                    adapter.dataSource.remove(chatMessage);
                    adapter.notifyDataSetInvalidated();
                }
            }
        }
    };
    //endregion


    private MessageInputFragment messageInput;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        if (getActivity() == null || getActivity().isFinishing())
            return;
        if (iphoneDialog != null && iphoneDialog.isShowing())
            iphoneDialog.dismiss();
        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_SERVICEID:
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    String serviceName = jsonObject.getString("service_name");
                    String serviceId = jsonObject.getString("service_id");
                    String lastServiceId = PrfUtils.getPrfparams(getActivity(), Constants.SERVICE_USERID);
                    if (!TextUtils.isEmpty(lastServiceId) && !serviceId.equals(lastServiceId)) {
                        ChatGroup.updateServiceId(PrfUtils.getUserId(getActivity()), PrfUtils.getTenantId(getActivity()), serviceId, lastServiceId);
                    }
                    PrfUtils.savePrfparams(getActivity(), Constants.SERVICE_NAME, serviceName);
                    PrfUtils.savePrfparams(getActivity(), Constants.SERVICE_USERID, serviceId);
                    Staff staff = new Staff(serviceId, serviceId, serviceName, "", PrfUtils.getTenantId(getActivity()));
                    group = ChatGroup.fromStaff(staff, PrfUtils.getUserId(getActivity()), PrfUtils.getTenantId(getActivity()));
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
}
