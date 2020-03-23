package com.vgtech.vancloud.ui.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.Node;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.common.view.IphoneDialog;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.models.ChatGroupStaffs;
import com.vgtech.vancloud.models.Staff;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.chat.controllers.XmppController;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.chat.net.NetAsyncTask;
import com.vgtech.vancloud.ui.group.OrganizationSelectedActivity;
import com.vgtech.vancloud.ui.group.OrganizationSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.event.EventManager;
import roboguice.inject.InjectView;

/**
 * @author xuanqiang
 */
public class MessageStaffFragment extends ActionBarFragment implements ContactsListener, MessageGroupNameListener, HttpListener<String>, AdapterView.OnItemClickListener {
    @InjectView(R.id.message_staff_root)
    View rootView;
    @InjectView(R.id.message_staff_groupName)
    TextView groupNameLabel;
    @InjectView(R.id.message_staff_exit_btn)
    View exitView;
    @InjectView(R.id.message_staff_clear)
    View clearView;
    @InjectView(R.id.btn_group_name)
    View btn_group_name;
    @InjectView(R.id.find_chats)
    View findChatsView;
    @Inject
    public Controller controller;
    private static final int REQUEST_CODE_USERSELECT = 1001;

    private ArrayList<Node> mSelectBeans = new ArrayList<Node>();

    public static MessageStaffFragment newInstance(final ChatGroup group) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("group", group);
        MessageStaffFragment fragment = new MessageStaffFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private Staff mAddStaff;
    private Staff mDeleteStaff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mAddStaff = new Staff(Staff.ADD);
        mDeleteStaff = new Staff(Staff.DELETE);
        group = (ChatGroup) bundle.getSerializable("group");
    }

    private MemberAdapter memberAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        memberAdapter = new MemberAdapter(getActivity());
        View view = createContentView(R.layout.message_staff);
        GridView gridView = (GridView) view.findViewById(R.id.member_gird);
        gridView.setOnItemClickListener(this);
        gridView.setAdapter(memberAdapter);
        return attachToSwipeBack(view);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        titleView.setText(R.string.chat_messages);
        rootView.setOnClickListener(this);
        exitView.setOnClickListener(this);
        findChatsView.setOnClickListener(this);
        clearView.setOnClickListener(this);
        btn_group_name.setOnClickListener(this);
        groupNameLabel.setText(group.groupNick);
        fixedViewCount = 2;
        if (ChatGroup.GroupTypeChat.equals(group.type)) {
            ((View) groupNameLabel.getParent()).setVisibility(View.GONE);
            exitView.setVisibility(View.GONE);
            Staff staff = new Staff(group.user());
            mStaff = staff;
            staffIds.add(staff.id);
            UserAccount account = controller.account();
            memberAdapter.getData().add(new Staff(account));
            memberAdapter.getData().add(mAddStaff);
            String userId = staff.id;
            String tenantId = PrfUtils.getTenantId(getActivity());
            if (!TextUtils.isEmpty(tenantId)&&!TextUtils.isEmpty(userId)){
                userId = userId.replaceAll(tenantId, "");
            }
            Node node = new Node(userId, staff.nick, true, staff.avatar);
//            node.setId(Long.valueOf(staff.id));
            mSelectBeans.add(node);
        } else {
            loadGroupView();
        }

    }

    private Staff mStaff;

    private NetworkManager mNetworkManager;
    private IphoneDialog progressHUD;

    public void showLoadingDialog(String contentStr) {
        if (progressHUD == null) {
            progressHUD = new IphoneDialog(getActivity());
        }
        progressHUD.setMessage(contentStr);
        progressHUD.show(true);
    }

    private void loadGroupView() {
        showLoadingDialog(getActivity().getString(R.string.loading));
        VanCloudApplication vanCloudApplication = getApplication();
        mNetworkManager = vanCloudApplication.getNetworkManager();
        Map<String, String> postValues = new HashMap<>();
        postValues.put("room", group.name);
        postValues.put("ownid", PrfUtils.getUserId(getActivity()));
        postValues.put("tenantid", PrfUtils.getTenantId(getActivity()));
        postValues.put("token", PrfUtils.getToken(getActivity()));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_VCHAT_GROUPMEMBERS), postValues, getActivity());
        mNetworkManager.load(1, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        progressHUD.dismiss();
        String tenantId = PrfUtils.getTenantId(getActivity());
        if (rootData.isSuccess()) {
            try {
                memberAdapter.clear();
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
                group.peopleNum = groupStaffs.staffs.size();
                group.groupNick = groupStaffs.groupNick;
                group.setCreator(groupStaffs.creator);

                groupNameLabel.setText(group.groupNick);
                titleView.setText(getString(R.string.chat_messages) + "(" + group.peopleNum + getString(R.string.people) + ")");

                //TODO 只有一人时默认退出该群聊
                if (group.peopleNum == 1)
                    exitGroup();

                UserAccount account = controller.account();
                String xmppLogin = xmpp.getLogname();
                String creator = group.getCreator() + account.tenant_id;
                if (xmppLogin.equals(creator)) {
                    isAdmin = true;
                }

                //TODO 管理员
                memberAdapter.getData().add(new Staff(account));
                StringBuilder nickBuilder = new StringBuilder(account.nickname()).append(",");
                ArrayList<String> avatars = new ArrayList<String>();
                staffIds.clear();
                mSelectBeans.clear();
                for (Staff staff : groupStaffs.staffs) {
                    Node node = new Node(staff.id, staff.nick, true, staff.avatar);
                    String userId = staff.id;
//                    if (!TextUtils.isEmpty(userId) && userId.length() > 18)
//                        userId = userId.substring(0, 18);
                    if (!TextUtils.isEmpty(tenantId)&&!TextUtils.isEmpty(userId)){
                        userId = userId.replaceAll(tenantId, "");
                    }
                    node.setId(userId);
                    mSelectBeans.add(node);
//                    if (avatars.size() < 9) {
                    if (!staff.id.equals(account.getUid() + account.tenant_id)) {
                        nickBuilder.append(staff.nick).append(",");
                    }
                    avatars.add(staff.avatar);
//                    }
                    if (!staff.id.equals(account.getUid() + account.tenant_id)) {
                        memberAdapter.getData().add(staff);
                    }
                    staffIds.add(staff.id);
                }
                memberAdapter.getData().add(mAddStaff);
                if (isAdmin) {
                    memberAdapter.getData().add(mDeleteStaff);
                }
                memberAdapter.notifyDataSetChanged();
                nickBuilder.deleteCharAt(nickBuilder.length() - 1);

                if (group.peopleNum == 1) {
                    avatars.add(account.photo);
                }

                group.nick = nickBuilder.toString();
                group.avatar = new Gson().toJson(avatars);
                group.save();
                if (!group.isExit) {
                    eventManager.fire(new OnEvent(OnEvent.EventType.LEAVE_GROUP, group));
                    getActivity().onBackPressed();
                } else {
                    eventManager.fire(new OnEvent(OnEvent.EventType.GROUP_MODIFY, group));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.request_failure), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onClick(View view) {
        if (view == exitView) {
            exitMessageGroup();
        } else if (view == findChatsView) {
            controller.pushFragment(SearchUserMessagesFragment.create(group));
        } else if (view == clearView) {
            clearRecords();
        } else if (view == btn_group_name) {
            MessageGroupNameFragment fragment = MessageGroupNameFragment.newInstance(group);
            fragment.listener = this;
            controller.pushFragment(fragment);
        } else {
            super.onClick(view);
        }
    }

    @Override
    public void selectedContacts(final List<Staff> contactses, final ChatGroup selGroup) {
        new NetAsyncTask<ChatGroup>(getActivity()) {
            @Override
            protected void showProgress() {
                showProgress(getString(R.string.please_wait));
            }

            @Override
            protected void onSuccess(ChatGroup group) throws Exception {
                if (ChatGroup.GroupTypeGroup.equals(MessageStaffFragment.this.group.type)) {
//                    controller.fm().popBackStack();
                    loadGroupView();
                } else {
                    if (group != null) {
                        controller.pushUserMessagesFragment(UsersMessagesFragment.newInstance(group, null));
                    } else {
                        showErrorText(context.getString(R.string.operation_failure), Toast.LENGTH_SHORT);
                    }
                }
            }

            @Override
            protected ChatGroup doInBackground() throws Exception {
                if (ChatGroup.GroupTypeChat.equals(group.type)) {
                    contactses.add(new Staff(group.user()));
                    return xmpp.createGroup(contactses);
                } else {
                    xmpp.addStaffs(contactses, group);
                }
                return null;
            }
        }.execute();
    }

    private void clearRecords() {
        ActionSheet actionSheet = new ActionSheet(getActivity());
//        actionSheet.setTitle(getString(R.string.delete_chat_record_hint));
        actionSheet.addAction(getString(R.string.clear_chat_record), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                group.destroyMessages();
                Toast.makeText(getActivity(), R.string.operation_success, Toast.LENGTH_SHORT).show();
                eventManager.fire(new OnEvent(OnEvent.EventType.CLEAR_MESSAGE_RECORD, group));
            }
        });
        actionSheet.show();
    }

    private void exitMessageGroup() {
        ActionSheet actionSheet = new ActionSheet(getActivity());
//        actionSheet.setTitle(getString(R.string.exit_chat_group_hint));
        actionSheet.addAction(getString(R.string.delete_and_exit), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitGroup();
            }
        });
        actionSheet.show();
    }

    @Override
    public void onModifyGroupName(String name) {
        groupNameLabel.setText(name);
    }

    private ArrayList<String> staffIds = new ArrayList<String>(0);
    boolean isAdmin;
    private ChatGroup group;
    private int fixedViewCount;
    @Inject
    XmppController xmpp;
    @Inject
    EventManager eventManager;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_USERSELECT:
                if (resultCode == Activity.RESULT_OK) {

                    final ArrayList<Node> userSelectList = data.getParcelableArrayListExtra("select");
                    if (userSelectList != null && !userSelectList.isEmpty()) {
                        new NetAsyncTask<ChatGroup>(getActivity()) {
                            @Override
                            protected void showProgress() {
                                showProgress(getString(R.string.please_wait));
                            }

                            @Override
                            protected void onSuccess(ChatGroup group) throws Exception {
                                if (group == null) {
                                    Toast.makeText(getActivity(), getString(R.string.xmpp_group_user_max, Constants.XMPP_GROUP_USER_MAX), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (ChatGroup.GroupTypeGroup.equals(MessageStaffFragment.this.group.type)) {
                                    //  controller.fm().popBackStack();
                                    loadGroupView();
                                } else {
                                    final List<Staff> contactses = new ArrayList<Staff>();
                                    contactses.add(mStaff);
                                    for (Node node : userSelectList) {
                                        if (node.isUser()) {
                                            UserAccount account = controller.account();
                                            if (!(account.getUid() + account.tenant_id).equals(node.getId())) {
                                                Staff staff = new Staff(String.valueOf(node.getId()), String.valueOf(node.getId()), node.getName(), node.getPhoto(), PrfUtils.getTenantId(getActivity()));
                                                contactses.add(staff);
                                            }
                                        }

                                    }
                                    controller.fm().popBackStack();
                                    xmpp.chat(contactses, null);
                                }
                            }

                            @Override
                            protected ChatGroup doInBackground() throws Exception {

                                final List<Staff> contactses = new ArrayList<Staff>();
                                if (ChatGroup.GroupTypeChat.equals(group.type)) {
                                    return group;
                                } else {
                                    List<String> ids = new ArrayList<String>();
                                    for (Node node : mSelectBeans) {
                                        ids.add(node.getId());
                                    }
                                    for (Node node1 : userSelectList) {
                                        if (node1.isUser()) {
                                            if (!ids.contains(node1.getId())) {
                                                Staff staff = new Staff(String.valueOf(node1.getId()), String.valueOf(node1.getId()), node1.getName(), node1.getPhoto(), PrfUtils.getTenantId(getActivity()));
                                                contactses.add(staff);
                                            }
                                        }
                                    }
                                    int userCount = ids.size() + contactses.size();
                                    if (userCount > Constants.XMPP_GROUP_USER_MAX) {
                                        return null;
                                    } else {
                                        xmpp.addStaffs(contactses, group);
                                    }

                                }
                                return group;
                            }
                        }.execute();
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }


    private void exitGroup() {
        new NetAsyncTask<Void>(getActivity()) {
            @Override
            protected void showProgress() {
                showProgress(getString(R.string.please_wait));
            }

            @Override
            protected void onSuccess(Void aVoid) throws Exception {
                controller.navigationToMessageFragment();
            }

            @Override
            protected Void doInBackground() throws Exception {
                xmpp.leaveGroup(group);
                group.destroy();
                eventManager.fire(new OnEvent(OnEvent.EventType.LEAVE_GROUP, group));
//            try {
//              netController().leaveGroup(group.name);
//            }catch(Exception e) {
//              Ln.e(e);
//            }
                return null;
            }
        }.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Staff staff = (Staff) parent.getItemAtPosition(position);
        if (staff.staffType == Staff.ADD) {
            Intent intent = new Intent(getActivity(), OrganizationSelectedActivity.class);
            intent.putExtra(OrganizationSelectedListener.SELECT_MODE, OrganizationSelectedListener.SELECT_MULTI);
            intent.putParcelableArrayListExtra("unselect", mSelectBeans);
            startActivityForResult(intent, REQUEST_CODE_USERSELECT);
        } else if (staff.staffType == Staff.DELETE) {
            memberAdapter.setType(MemberAdapter.ADAPTER_DELETE);
            memberAdapter.remove(mAddStaff);
            memberAdapter.remove(mDeleteStaff);
        } else {
            if (memberAdapter.getType() == MemberAdapter.ADAPTER_DELETE) {
                if ((controller.account().getUid() + controller.account().tenant_id).equals(staff.id)) {
                    Toast.makeText(getActivity(), R.string.not_allowed_delete_self, Toast.LENGTH_SHORT).show();
                    return;
                }
                new NetAsyncTask<Void>(getActivity()) {
                    @Override
                    protected void showProgress() {
                        showProgress(getString(R.string.deleting));
                    }

                    @Override
                    protected void onSuccess(Void aVoid) throws Exception {
                        staffIds.remove(staff.avatar);
                        eventManager.fire(new OnEvent(OnEvent.EventType.GROUP_MODIFY, group));
                        memberAdapter.setType(MemberAdapter.ADAPTER_NORMAL);
                        memberAdapter.notifyDataSetChanged();
                        loadGroupView();
                    }

                    @Override
                    protected Void doInBackground() throws Exception {
                        xmpp.removeStaff(group, staff);
                        return null;
                    }
                }.execute();
            } else {
                UserUtils.enterUserInfo(getActivity(), staff.id, staff.nick, staff.avatar);
            }
        }
    }
}
