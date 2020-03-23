package com.vgtech.vancloud.ui.chat.controllers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.vgtech.common.utils.MD5;
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
import com.vgtech.common.network.android.FilePair;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.models.VancloudIQ;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.chat.OnEvent;
import com.vgtech.vancloud.ui.chat.UsersMessagesFragment;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.chat.models.ChatMessage;
import com.vgtech.vancloud.ui.chat.models.ChatUser;
import com.vgtech.vancloud.ui.chat.net.NetAsyncTask;
import com.vgtech.vancloud.ui.common.LogoutActivity;
import com.vgtech.vancloud.utils.NoticeUtils;
import com.vgtech.common.PrfUtils;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import roboguice.event.EventManager;
import roboguice.inject.ContextSingleton;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;
import roboguice.util.Strings;

import static com.vgtech.vancloud.ui.chat.OnEvent.EventType;
import static com.vgtech.vancloud.ui.chat.models.ChatMessage.MessageTypeFile;
import static com.vgtech.vancloud.ui.chat.models.ChatMessage.MessageTypeGroupAddMembers;
import static com.vgtech.vancloud.ui.chat.models.ChatMessage.MessageTypeGroupDelMember;
import static com.vgtech.vancloud.ui.chat.models.ChatMessage.MessageTypeGroupModifyName;
import static org.jivesoftware.smack.SmackException.NotConnectedException;
import static org.jivesoftware.smack.XMPPException.XMPPErrorException;

/**
 * @author xuanqiang
 */
@SuppressWarnings("UnusedDeclaration")
@ContextSingleton
public class XmppController {


    static {
        SmackConfiguration.setDefaultPacketReplyTimeout(1000 * 60);// 5 sec
    }

    private XMPPConnection connection;
    private static String customerId;
    private String logname;
    private String pwd;
    private String resource = "vancloud";
    private String ip;
    private ConnectionConfiguration config;
    private NetworkManager mNetworkManager;
    public static final String TAG = "TAG_liaotian";

    public String logname(final String uid) {

        if (!TextUtils.isEmpty(uid)){
            String tenantId = PrfUtils.getTenantId(context);
            if (uid.indexOf(tenantId) == -1) {
                return  uid.toLowerCase() + tenantId;
            }else {
                return uid.toLowerCase();
            }
        }else {
            return "";
        }
    }


    public void init() {
        VanCloudApplication application = (VanCloudApplication) context.getApplicationContext();
        mNetworkManager = application.getNetworkManager();
        UserAccount account = controller.account();
//        this.logname = logname(account.xmpp_user);
        this.logname = logname(account.getUid());
        mTenantId = PrfUtils.getTenantId(context);
        final SharedPreferences preferences = PrfUtils.getSharePreferences(context);
        String password = preferences.getString("password", "");
        this.pwd = MD5.getMD5(password);
        this.ip = account.xmpp_host;
        if (TextUtils.isEmpty(ip)) {
            ip = URLAddr.IP;
        }
        try {
            Class.forName("org.jivesoftware.smack.ReconnectionManager");
        } catch (Exception e) {
            Ln.e(e);
        }
        config = new ConnectionConfiguration(ip, account.xmpp_port);
        config.setReconnectionAllowed(true);
        config.setSendPresence(true);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setCompressionEnabled(false);
        SASLAuthentication.supportSASLMechanism("DIGEST-MD5", 0);
//    SASLAuthentication.supportSASLMechanism("PLAIN", 0);
        config.setDebuggerEnabled(true);
        config.setRosterLoadedAtLogin(false);

        connection = new XMPPTCPConnection(config);
        PingManager pingManager = PingManager.getInstanceFor(connection);
        pingManager.setPingInterval(10);
        pingManager.registerPingFailedListener(new PingFailedListener() {
            @Override
            public void pingFailed() {
//                Log.d(TAG, "pingFailed");
//                Ln.i("xmpp events: PingManager reported failed ping");
                new RoboAsyncTask<Void>(context) {
                    @Override
                    public Void call() throws Exception {
                        if (!connection.isConnected())
//                        disconnect();
                            connection();
                        return null;
                    }
                }.execute();
            }
        });
        PacketFilter filter = new PacketTypeFilter(Message.class);
        connection.addConnectionListener(new ConnectionListener() {

            @Override
            public void connected(XMPPConnection xmppConnection) {
                Log.e(TAG, "connected: ");
            }

            @Override
            public void authenticated(XMPPConnection xmppConnection) {
                Log.e(TAG, "authenticated");
            }

            @Override
            public void connectionClosed() {
                Log.e(TAG, "connectionClosed: ");
                if (!mStop) {
                    startXmpp();
                }
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                String reason = e.getMessage();
                Log.e(TAG, "reason = " + reason);
                if (!TextUtils.isEmpty(reason)
                        && reason.indexOf("conflict") != -1 && !isConnected()) {
                    stopXmpp();
                    Intent intent = new Intent(context, LogoutActivity.class);
                    context.startActivity(intent);
                }
            }

            @Override
            public void reconnectingIn(int i) {
                Log.e(TAG, "reconnectingIn: ");
            }

            @Override
            public void reconnectionSuccessful() {
                Log.e(TAG, "reconnectionSuccessful: ");
            }

            @Override
            public void reconnectionFailed(Exception e) {
                Log.e(TAG, "reconnectionFailed");
            }
        });
        connection.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                Message message = (Message) packet;
                for (PacketExtension packetExtension : message.getExtensions()) {
                    if (packetExtension instanceof MUCUser) {
                        MultiUserChat muc = new MultiUserChat(connection, message.getFrom());
                        try {
                            muc.join(connection.getUser());
                        } catch (Exception e) {
                            Ln.e(e);
                        }
                        return;
                    } else if (packetExtension instanceof DelayInformation) {
                        if (Message.Type.groupchat.equals(message.getType())) {
                            return;
                        }
                    }
                }
                if (TextUtils.isEmpty(message.getSubject())) {
                    return;
                }
                processMessage(message);
            }
        }, filter);
    }

    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    public String mTenantId;

    private void processMessage(final Message message) {
        if (Message.Type.error.equals(message.getType())) {
            String packetId = message.getPacketID();
            if (Strings.notEmpty(packetId)) {
                ChatMessage chatMessage = ChatMessage.findByPacketId(packetId);
                if (chatMessage != null) {
                    updateSendStatus(chatMessage, true);
                }
            }
            return;
        }
        Subject subject = new Gson().fromJson(message.getSubject(), Subject.class);
        if (logname.equals(logname(subject.name))) {
            return;
        }
        if (ChatMessage.MessageTypeGroupDelMember.equals(subject.type)) {
            String from = message.getFrom().split("@")[0];
            ChatGroup group = ChatGroup.queryFromGroupType(from, logname, getMessageTime(message), subject, subject.tenantId);
            String creator = group.getCreator() + subject.tenantId;
            if (!creator.equals(logname) && !logname.equals(subject.delUser)) {//将你移除群聊
                updateGroupMembers(group);
                return;
            }
        }
        mTenantId = PrfUtils.getTenantId(context);
        subject.tenantId = mTenantId;
//        mTenantId = subject.tenantId;

        if (!Strings.isEmpty(subject.name)) {
            subject.name = subject.name.toLowerCase();
        }
        if (Message.Type.chat.equals(message.getType())) {
            if (subject.hasRoom()) {
                message.setType(Message.Type.groupchat);
                message.setFrom(subject.room.name);
            }
        }
        String from = message.getFrom().split("@")[0];

        if (Message.Type.chat.equals(message.getType())) {
            try {
                connection.sendPacket(new VancloudIQ(message.getTo(), message.getPacketID(), message.getType().name()));
            } catch (NotConnectedException e) {
                e.printStackTrace();
            }
            ChatMessage chatMessage = receiveChatMessage(message, subject);
            if (NoticeUtils.isBackground(context)) {
                ChatGroup group = chatMessage.group;
                CharSequence content = EmojiFragment.getEmojiContent(context, 0, group.getContent(context.getResources()));
                NoticeUtils.showChatNotify(context, chatMessage.user.uid, chatMessage.user.nick, chatMessage.user.avatar, content, "chat");
            }
            eventManager.fire(new OnEvent(EventType.NEW, chatMessage));
        } else if (Message.Type.groupchat.equals(message.getType())) {
            try {
                connection.sendPacket(new VancloudIQ(message.getTo(), message.getPacketID(), message.getType().name()));
            } catch (NotConnectedException e) {
                e.printStackTrace();
            }
            boolean needUpdate = false;
            if (ChatMessage.MessageTypeGroupDelMember.equals(subject.type)) {
                if (logname.equals(subject.delUser)) {
                    Presence leavePresence = new Presence(Presence.Type.unavailable);
                    leavePresence.setTo(getGroupAddress(from));
                    try {
                        connection.sendPacket(leavePresence);
                    } catch (NotConnectedException e) {
                        Ln.e(e);
                    }
                }
                needUpdate = true;
            } else if (ChatMessage.MessageTypeGroupAddMembers.equals(subject.type)) {
                needUpdate = true;
            }
            ChatMessage chatMessage = receiveGroupMessage(message, subject);
            if (NoticeUtils.isBackground(context)) {
                ChatGroup group = chatMessage.group;
                CharSequence content = EmojiFragment.getEmojiContent(context, 0, group.getContent(context.getResources()));
                NoticeUtils.showChatNotify(context, group.name, group.getDisplayNick(), "", content, "group");
            }
            eventManager.fire(new OnEvent(EventType.NEW, chatMessage));

            if (!needUpdate) {
                needUpdate = TextUtils.isEmpty(chatMessage.group.avatar);
            }

            if (needUpdate) {
                updateGroupMembers(chatMessage.group);
            }

        }
    }

    public ChatMessage receiveChatMessage(final Message message, final Subject subject) {
        ChatMessage chatMessage = null;
        ChatUser user = ChatUser.update(new Staff(subject, logname(subject.name)));
        try {
            ActiveAndroid.beginTransaction();
            ChatGroup group = ChatGroup.updateFromChatType(logname, getMessageTime(message), user, subject.tenantId);
            chatMessage = ChatMessage.create(context.getResources(), message.getBody(), group, user, subject, false);
            ActiveAndroid.setTransactionSuccessful();
//      vibrate();
        } catch (Exception e) {
            Ln.e(e);
        } finally {
            ActiveAndroid.endTransaction();
        }
        return chatMessage;
    }

    public ChatMessage receiveGroupMessage(final Message message, final Subject subject) {
        ChatMessage chatMessage = null;
        ChatUser user = ChatUser.update(new Staff(subject, logname(subject.name)));
        try {
            String from = message.getFrom().split("@")[0];
            ActiveAndroid.beginTransaction();
            ChatGroup group = ChatGroup.updateFromGroupType(from, logname, getMessageTime(message), subject, subject.tenantId);
            chatMessage = ChatMessage.create(context.getResources(), message.getBody(), group, user, subject, false);
            ActiveAndroid.setTransactionSuccessful();
//      vibrate();
        } catch (Exception e) {
            Ln.e(e);
        } finally {
            ActiveAndroid.endTransaction();
        }
        return chatMessage;
    }

//  private void vibrate() {
//    vibrator.vibrate(200);
//  }

    public String getAddress(String name) {
        return name + "@" + ip + "/" + resource;
    }

    public String getGroupAddress(String name) {
        return name + "@conference." + ip;
    }

    private long getMessageTime(final Message message) {
        long time = -1;
        for (PacketExtension packetExtension : message.getExtensions()) {
            if (packetExtension instanceof DelayInformation) {
                DelayInformation delayInfo = (DelayInformation) packetExtension;
                time = delayInfo.getStamp().getTime();
                break;
            }
        }
        if (time < 0) {
            time = System.currentTimeMillis();
        }
        return time;
    }

    private void commitGroupOwner(final ChatGroup group) {
        Map<String, String> postValues = new HashMap<>();
        postValues.put("room", group.name);
        postValues.put("ownid", PrfUtils.getUserId(context));
        postValues.put("tenantid", mTenantId);
        postValues.put("token", PrfUtils.getToken(context));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_VCHAT_MUCOWNER), postValues, context);


        mNetworkManager.load(1, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
                String tenantId = mTenantId;
                if (rootData.isSuccess()) {

                } else {
                    // Toast.makeText(getActivity(), getString(R.string.request_failure), Toast.LENGTH_SHORT).show();
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

    public void updateGroupMembers(final ChatGroup group) {
        Map<String, String> postValues = new HashMap<>();
        postValues.put("room", group.name);
        postValues.put("ownid", PrfUtils.getUserId(context));
        postValues.put("tenantid", mTenantId);
        postValues.put("token", PrfUtils.getToken(context));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, URLAddr.URL_VCHAT_GROUPMEMBERS), postValues, context);


        mNetworkManager.load(1, path, new HttpListener<String>() {
            @Override
            public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
                String tenantId = mTenantId;
                if (rootData.isSuccess()) {
                    try {
                        JSONObject rootObject = rootData.getJson().getJSONObject("data");
                        JSONArray jsonArray = rootObject.getJSONArray("members");
                        ChatGroupStaffs groupStaffs = new ChatGroupStaffs();
                        groupStaffs.groupNick = rootObject.getString("roomname");
                        groupStaffs.isExit = rootObject.getBoolean("isExit");
                        groupStaffs.creator = rootObject.getString("owner");
                        groupStaffs.staffs = new ArrayList<Staff>(0);
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
                        group.isExit = groupStaffs.isExit;
                        UserAccount account = controller.account();
                        StringBuilder nickBuilder = new StringBuilder();
                        ArrayList<String> avatars = new ArrayList<>();
                        for (Staff staff : groupStaffs.staffs) {
//                            if (avatars.size() < 9) {
                            nickBuilder.append(staff.nick).append(",");
                            avatars.add(staff.avatar);
//                            }
                        }
                        if (nickBuilder.length() > 0)
                            nickBuilder.deleteCharAt(nickBuilder.length() - 1);

                        if (group.peopleNum == 1) {
                            avatars.add(account.photo);
                        }
                        group.nick = nickBuilder.toString();
                        group.avatar = new Gson().toJson(avatars);
                        group.save();

                        eventManager.fire(new OnEvent(EventType.GROUP_MODIFY, group));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Toast.makeText(getActivity(), getString(R.string.request_failure), Toast.LENGTH_SHORT).show();
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

    private synchronized void connection() {
        if (connection.isConnected())
            return;
        try {
//            Ln.i("xmppConnection");
//            Log.d(TAG, "connection");
            config.setReconnectionAllowed(true);
            if (!connection.isConnected()) {
                connection.connect();
            }
            int i = 0;
            while (i < 3) {
                try {
                    connection.login(logname, pwd, resource);
                    break;
                } catch (Exception e) {
                    if (i >= 3) {
                        throw e;
                    }
                }
                i++;
            }
            stopReconnectHandler();
        } catch (SmackException | XMPPException | IOException ce) {
//      stopReconnectHandler();
            disconnect();
//            Log.d(TAG, "SmackException | XMPPException | IOException ce");
            handler.postDelayed(runnable, 1000);
//      Ln.e(ce);
        } catch (Exception e) {
            Ln.e(e);
//            Log.d(TAG, " } catch (Exception e) {");
        }
    }

    private void disconnect() {
        Ln.i("xmppDisconnect");
        stopReconnectHandler();
        config.setReconnectionAllowed(false);
        try {
            connection.disconnect();
        } catch (NotConnectedException e) {
            Ln.e(e);
        }
    }

    private void stopReconnectHandler() {
        handler.removeCallbacks(runnable);
    }

    public void reStartXmpp() {
        mStop = false;
        startXmpp();
    }

    public void startXmpp() {
        if (mStop)
            return;
        if (connection != null && context != null) {
            if (!connection.isConnected()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connection();
                    }
                }).start();
            }
        }
    }

    private boolean mStop;

    public void stopXmpp() {
        mStop = true;
//        Log.d(TAG, "stopXmpp");
        if (connection != null && context != null) {
            new RoboAsyncTask<Void>(context) {
                @Override
                public Void call() throws Exception {
                    disconnect();
                    return null;
                }
            }.execute();
        }
    }

    public void send(final Message message, final ChatMessage chatMessage) throws Exception {
        try {
            if (chatMessage != null && Message.Type.groupchat.equals(chatMessage.group.type) && !chatMessage.group.isExit) {
                throw new Exception("server is reachable");
            } else if (chatMessage != null && ChatGroup.GroupTypeGroup.equals(chatMessage.group.type) && (!chatMessage.group.create && !chatMessage.group.isExit)) {
                throw new Exception("server is reachable");
            }
            message.setPacketID(chatMessage == null ? ChatMessage.generatePacketId() : chatMessage.packetId);

            PingManager pingManager = PingManager.getInstanceFor(connection);
            pingManager.setPingInterval(30);
            if (pingManager.pingMyServer()) {
//        Ln.e("pong:" + isPing);
                connection.sendPacket(message);
            } else {
                // throw new Exception("server is reachable");
                updateSendStatus(chatMessage, true);
            }
        } catch (Exception e) {
            if (chatMessage != null) {
                updateSendStatus(chatMessage, true);
                Ln.e(e);
            }
            if (e instanceof NotConnectedException) {
                new RoboAsyncTask<Void>(context) {
                    @Override
                    public Void call() throws Exception {
                        disconnect();
                        connection();
                        return null;
                    }
                }.execute();
            }
        }
    }

    public void updateSendStatus(final ChatMessage chatMessage, boolean isFailure) {
        if (chatMessage.isSender && chatMessage.type.equals(ChatMessage.MessageTypeGroupDelMember)) {
            chatMessage.destroy();
            eventManager.fire(new OnEvent(EventType.MESSAGE_DELETE, chatMessage));
        } else {
            chatMessage.isFailure = isFailure;
            chatMessage.save();
            Cache.removeEntity(chatMessage.group);
            eventManager.fire(new OnEvent(EventType.MESSAGE_MODIFY, chatMessage));
        }
    }

    public String getLogname() {
        return logname;
    }

    private String getString(final int resId) {
        return context.getResources().getString(resId);
    }

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public ChatGroup createGroup(final List<Staff> staffs) throws Exception {
        if (CollectionUtils.isEmpty(staffs)) return null;
        UserAccount account = controller.account();
        final ChatGroup group = new ChatGroup(staffs, logname, mTenantId);
        group.create = true;
        //
        if (staffs.size() > 1) {
            try {
                List<String> avatars = new Gson().fromJson(group.avatar, new TypeToken<List<String>>() {
                }.getType());
                avatars.add(account.photo);
                group.avatar = new Gson().toJson(avatars);
            } catch (JsonSyntaxException ignored) {
            }
        }
        group.nick = account.nickname() + "," + group.nick;


        final StringBuilder contentBuilder = new StringBuilder(getString(R.string.invite));
        List<String> roomOwners = new ArrayList<>();

        Iterator<Staff> iter = staffs.iterator();
        while (iter.hasNext()) {
            Staff staff = iter.next();
            if ((account.getUid() + account.tenant_id).equals(staff.id)) {
                iter.remove();
            }
            roomOwners.add(getAddress(staff.name()));
            contentBuilder.append(staff.nick).append("、");
        }
        if (CollectionUtils.isEmpty(staffs)) return null;

        contentBuilder.deleteCharAt(contentBuilder.length() - 1);
        contentBuilder.append(getString(R.string.join_group));

        MultiUserChat muc = new MultiUserChat(connection, getGroupAddress(group.name));
        muc.create(logname);
        Form form = muc.getConfigurationForm();
        Form submitForm = form.createAnswerForm();
        for (FormField field : form.getFields()) {
            if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
                submitForm.setDefaultAnswer(field.getVariable());
            }
        }
        roomOwners.add(connection.getUser());
        submitForm.setTitle(group.nick);
        submitForm.setAnswer("muc#roomconfig_roomname", "");
        submitForm.setAnswer("muc#roomconfig_roomowners", roomOwners);
        submitForm.setAnswer("muc#roomconfig_persistentroom", true);
        submitForm.setAnswer("muc#roomconfig_maxusers", Arrays.asList("0"));
        submitForm.setAnswer("muc#roomconfig_membersonly", false);
        submitForm.setAnswer("muc#roomconfig_allowinvites", true);
        submitForm.setAnswer("muc#roomconfig_enablelogging", false);
        submitForm.setAnswer("x-muc#roomconfig_reservednick", false);
        submitForm.setAnswer("x-muc#roomconfig_canchangenick", true);
        submitForm.setAnswer("x-muc#roomconfig_registration", true);

        muc.sendConfigurationForm(submitForm);
        for (String owner : roomOwners) {
            if (owner.startsWith(connection.getUser())) {
                continue;
            }
            muc.invite(owner, "");
        }
//    Entity entity = netProvider.get(context).roomOwner(group.name);
        String content = contentBuilder.toString();
        send(group, MessageTypeGroupAddMembers, getString(R.string.you) + content, account.nickname() + content);
        return group;
    }

    public void addStaffs(final List<Staff> staffs, final ChatGroup group) throws Exception {
        group.addStaffs(staffs);

        StringBuilder contentBuilder = new StringBuilder(getString(R.string.invite));
        MultiUserChat muc = new MultiUserChat(connection, getGroupAddress(group.name));
        for (Staff staff : staffs) {
            muc.grantOwnership(getAddress(staff.name()));
            muc.invite(getAddress(staff.name()), "");
            contentBuilder.append(staff.nick).append("、");
        }
        contentBuilder.deleteCharAt(contentBuilder.length() - 1);
        contentBuilder.append(getString(R.string.join_group));

        String content = contentBuilder.toString();
        UserAccount account = controller.account();
        send(group, MessageTypeGroupAddMembers, getString(R.string.you) + content, account.nickname() + content);
    }

    public boolean modifyGroupName(final ChatGroup group, final String groupNick) {
        try {
            MultiUserChat muc = new MultiUserChat(connection, getGroupAddress(group.name));
            Form form = muc.getConfigurationForm();
            Form submitForm = form.createAnswerForm();
            for (FormField field : form.getFields()) {
                if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
                    submitForm.setDefaultAnswer(field.getVariable());
                }
            }
            submitForm.setAnswer("muc#roomconfig_roomname", groupNick);
            muc.sendConfigurationForm(submitForm);
            group.groupNick = groupNick;

            String content = getString(R.string.modify_group_name) + "“" + groupNick + "”";
            UserAccount account = controller.account();
            send(group, MessageTypeGroupModifyName, getString(R.string.you) + content, account.nickname() + content);

        } catch (Exception e) {
            Ln.e(e);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public Message newMessage(final String name, final Message.Type type,
                              final String content, final Subject subject) {
        Message message = new Message(Message.Type.groupchat == type ? getGroupAddress(name) : getAddress(name), type);
        message.setBody(content);
        message.setSubject(new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC, Modifier.PRIVATE).create().toJson(subject));
        return message;
    }

    public void removeStaff(final ChatGroup group, final Staff staff) throws Exception {
        group.nick = group.nick.replace("," + staff.nick, "");

        String content = getString(R.string.will) + staff.nick + getString(R.string.remove_group_chat);
        UserAccount account = controller.account();
        Subject subject = new Subject(account, MessageTypeGroupDelMember, group.nick);
        subject.delUser = staff.name();
        send(group, subject, getString(R.string.you) + content, staff.nick);

        MultiUserChat muc = new MultiUserChat(connection, getGroupAddress(group.name));
        muc.revokeMembership(getAddress(staff.name()));

        group.peopleNum--;
        try {
            ArrayList<String> avatars = new Gson().fromJson(group.avatar, new TypeToken<List<String>>() {
            }.getType());
            Iterator<String> iter = avatars.iterator();
            while (iter.hasNext()) {
                String avatar = iter.next();
                if (avatar.equals(staff.avatar)) {
                    iter.remove();
                }
            }
            if (group.peopleNum == 1) {
                avatars.add(account.photo);
            }
            group.avatar = new Gson().toJson(avatars);
        } catch (Exception ignored) {
            Ln.e(ignored);
        }
        group.save();
    }

    public ChatMessage send(final ChatGroup group, final String msgType, final String content) throws Exception {
        return send(group, msgType, content, null);
    }

    public ChatMessage send(final ChatGroup group, final String msgType, final String content, final String body) throws Exception {
        UserAccount account = controller.account();
        Subject subject = new Subject(account, msgType, group.getDisplayNick());
        return send(group, subject, content, body);
    }

    public ChatMessage send(final ChatGroup group, final Subject subject, final String content, final String body) throws Exception {
        Message.Type chatType = group.getChatType();
        ChatMessage chatMessage = sendLocal(group, subject, content);
        if (chatMessage != null) {
            send(newMessage(group.name, chatType, body != null ? body : content, subject), chatMessage);
        }
        if (group.create)
            commitGroupOwner(group);
        return chatMessage;
    }

    public ChatMessage sendLocal(final ChatGroup group, final Subject subject, final String content) {
        Message.Type chatType = group.getChatType();
        if (Message.Type.chat == chatType) {
            ChatUser.update(new Staff(group.user));
            group.nick = group.user.nick;
            group.avatar = group.user.avatar;
        }
        UserAccount account = controller.account();
        group.owner = account.user_id + account.tenant_id;
        group.tenantId = account.tenant_id;
        ChatUser user = ChatUser.update(new Staff(account));
        ChatMessage chatMessage = null;
        group.time = System.currentTimeMillis();
        try {
            ActiveAndroid.beginTransaction();
            group.save();
            chatMessage = ChatMessage.create(context.getResources(), content, group, user, subject, true);
            ActiveAndroid.setTransactionSuccessful();
            eventManager.fire(new OnEvent(EventType.NEW, chatMessage));
        } finally {
            ActiveAndroid.endTransaction();
        }
        return chatMessage;
    }

    public void leaveGroup(final ChatGroup group) throws Exception {
        UserAccount account = controller.account();
        group.nick = group.nick.replace("," + account.nickname(), "");

        Subject subject = new Subject(account, MessageTypeGroupDelMember, group.nick);
        subject.delUser = logname;
        send(newMessage(group.name, Message.Type.groupchat, account.nickname(), subject), null);
        MultiUserChat muc = new MultiUserChat(connection, getGroupAddress(group.name));
        if (group.peopleNum == 1) {
            try {
                muc.destroy("destroy", null);
            } catch (XMPPErrorException e) {
                Ln.e(e);
                if (e.getXMPPError() == null || e.getXMPPError().getType() != XMPPError.Type.AUTH) {
                    throw e;
                }
            }
        } else {
            muc.revokeMembership(connection.getUser());
            Presence leavePresence = new Presence(Presence.Type.unavailable);
            leavePresence.setTo(getGroupAddress(group.name));
            connection.sendPacket(leavePresence);
        }
    }

    public void chat(final List<Staff> contactses, final ChatGroup group) {
        chat(contactses, group, null);
    }

    public void chat(final List<Staff> contactses, final ChatGroup group, final ChatMessage forwardMessage) {
        if (group != null) {
            ChatGroup newGroup = null;
            if (group.getId() == null) {
                newGroup = ChatGroup.find(group.name, logname, ChatGroup.GroupTypeGroup, mTenantId);
            }
            if (newGroup == null) {
                newGroup = group;
            }
            controller.pushUserMessagesFragment(UsersMessagesFragment.newInstance(newGroup, forwardMessage));
        } else if (!CollectionUtils.isEmpty(contactses)) {
            if (contactses.size() == 1) {
                controller.pushUserMessagesFragment(UsersMessagesFragment.newInstance(
                        ChatGroup.fromStaff(contactses.get(0), logname, mTenantId), forwardMessage));
            } else {
                new NetAsyncTask<ChatGroup>(context) {
                    @Override
                    protected void showProgress() {
                        showProgress(getString(R.string.please_wait));
                    }

                    @Override
                    protected void onSuccess(ChatGroup group) throws Exception {
                        if (group != null) {
                            controller.pushUserMessagesFragment(UsersMessagesFragment.newInstance(group, forwardMessage));
                        } else {
                            showErrorText(context.getString(R.string.operation_failure), Toast.LENGTH_SHORT);
                        }
                    }

                    @Override
                    protected ChatGroup doInBackground() throws Exception {
                        return createGroup(contactses);
                    }
                }.execute();
            }
        }
    }

    public void sendFile(final ChatGroup group, final Subject.File subjectFile, final ChatMessage chatMessage) {
        UserAccount account = controller.account();
        final Subject subject = new Subject(account, MessageTypeFile, group.getDisplayNick());
        if (chatMessage != null) {
            subject.file = chatMessage.getFile();
        } else {
            subject.file = subjectFile;
        }
        ChatMessage localMessage = chatMessage;
        if (localMessage == null) {
            localMessage = sendLocal(group, subject, "file");
        }
        final ChatMessage sendMessage = localMessage;
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(context));
        params.put("tenantid", mTenantId);
        params.put("type", "12");
        String url = "";
        FilePair filePair = null;
        if (Subject.File.TYPE_PICTURE.equals(subjectFile.ext)) {
            url = URLAddr.URL_IMAGE;
            filePair = new FilePair("pic", new File(subjectFile.getFilePath()));
        } else if (Subject.File.TYPE_AUDIO.equals(subjectFile.ext)) {
            url = URLAddr.URL_AUDIO;
            filePair = new FilePair("audio", new File(subjectFile.getFilePath()));
            params.put("time", subjectFile.getDuration());
        }
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(context, url), params, filePair);
        if (Strings.isEmpty(subject.file.url)) {
            mNetworkManager.load(1, path, new HttpListener<String>() {

                @Override
                public void onResponse(String response) {

                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }

                @Override
                public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
                    boolean result = rootData.isSuccess();
                    if (result) {
                        try {
                            JSONObject jsonObject = rootData.getJson();
                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            if (dataArray.length() > 0) {
                                String fileUrl = dataArray.getJSONObject(0).getString("url");
                                subject.file.url = fileUrl;
                                sendMessage.setFile(subject.file);
                                sendMessage.save();
                            }
                            if (Strings.isEmpty(subject.file.url)) {
                                throw new Exception();
                            }
                            send(newMessage(group.name, group.getChatType(), "file", subject), sendMessage);

                            if (chatMessage != null) {
                                updateSendStatus(sendMessage, false);
                            }
                        } catch (Exception e) {
                            updateSendStatus(sendMessage, true);
                            e.printStackTrace();
                        }
                    } else {
                        updateSendStatus(sendMessage, true);
                    }
                }
            });
        } else {
            try {
                send(newMessage(group.name, group.getChatType(), "file", subject), sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//
//        new RoboAsyncTask<Void>(context) {
//            @Override
//            protected void onSuccess(Void aVoid) throws Exception {
//                if (chatMessage != null) {
//                    updateSendStatus(sendMessage, false);
//                }
//            }
//
//            @Override
//            public Void call() throws Exception {
//                String url;
//                if (Strings.isEmpty(subject.file.url)) {
//
//                    if () {
//                        subject.file.url = uploadFile(, , params);
//                    } else if () {
//                        subject.file.url = uploadFile(, "", new File(subjectFile.getFilePath()), params);
//                    }
//
//                    sendMessage.setFile(subject.file);
//                    sendMessage.save();
//                }
//                if (Strings.isEmpty(subject.file.url)) {
//                    throw new Exception();
//                }
//                send(newMessage(group.name, group.getChatType(), "file", subject), sendMessage);
//                return null;
//            }
//
//            private String uploadFile(String url, String key, File file, List<StringPart> params) {
//
//                String fileUrl = null;
//                try {
//                    ArrayList<Part> list = new ArrayList<Part>();
//                    list.add(new FilePart(key, file, "image/jpg", HTTP.UTF_8));
//
//                    if (params != null) {
//                        list.addAll(params);
//                    }
//
//                    Part[] parts = new Part[list.size()];
//                    MultipartEntity entity = new MultipartEntity(list.toArray(parts));
//
//                    HttpPost poster = new HttpPost(url);
//                    poster.addHeader(entity.getContentType());
//                    poster.setEntity(entity);
//
//                    int HTTP_CONNECTION_TIMEOUT = 5000;
//                    int SOCKET_TIMEOUT = 3 * 60 * 1000;
//                    HttpParams httpParameters = new BasicHttpParams();
//                    HttpConnectionParams.setConnectionTimeout(httpParameters,
//                            SOCKET_TIMEOUT);
//                    HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);
//                    HttpClient httpClient = new DefaultHttpClient(httpParameters);
//
//                    HttpResponse response = httpClient.execute(poster);
//                    HttpEntity httpResponseEntity = response.getEntity();
//                    InputStream in = httpResponseEntity.getContent();
//                    String result = new String(read(in));
//
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return fileUrl;
//            }
//
//            @Override
//            protected void onThrowable(Throwable t) throws RuntimeException {
//
//                Ln.d(t);
//            }
//        }.execute();

    }

    public static byte[] read(InputStream inStream) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        try {
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            inStream.close();
            return outStream.toByteArray();
        } catch (IOException e) {
        }
        return new byte[0];
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            startXmpp();
        }
    };
    @Inject
    Handler handler;
    @Inject
    Controller controller;
    //  @Inject ContextScopedProvider<NetController> netProvider;
    @Inject
    EventManager eventManager;
    @Inject
    Context context;
//  @Inject Vibrator vibrator;

}

