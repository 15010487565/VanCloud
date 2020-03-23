package com.vgtech.vancloud.ui.chat.models;

import android.content.res.Resources;
import android.text.TextUtils;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.models.GroupInfo;
import com.vgtech.vancloud.models.Staff;
import com.vgtech.vancloud.models.Subject;

import org.jivesoftware.smack.packet.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import roboguice.util.Ln;
import roboguice.util.Strings;

import static com.vgtech.vancloud.models.Subject.File.TYPE_AUDIO;
import static com.vgtech.vancloud.models.Subject.File.TYPE_PICTURE;
import static com.vgtech.vancloud.ui.chat.models.ChatMessage.MessageTypeGps;
import static com.vgtech.vancloud.ui.chat.models.ChatMessage.MessageTypeNormal;

/**
 * @author xuanqiang
 */
@Table(name = "groups")
public class ChatGroup extends Model implements Serializable {
    private static final long serialVersionUID = 5297588819782534385L;

    public static final String GroupTypeChat = "chat";
    public static final String GroupTypeGroup = "group";

    @Column(name = "owner", index = true, length = 200)
    public String owner;
    @Column(name = "name", index = true, length = 200)
    public String name;
    @Column(name = "nick", length = 400)
    public String nick;
    @Column(name = "groupNick", length = 400)
    public String groupNick;
    @Column(name = "time")
    public long time;
    @Column(name = "avatar")
    public String avatar;
    @Column(name = "creator", length = 200)
    private String creator;
    @Column(name = "unreadNum")
    public int unreadNum;
    @Column(name = "type", index = true, length = 20)
    public String type = GroupTypeChat;
    @Column(name = "isExit", length = 1)
    public boolean isExit;
    @Column(name = "peopleNum")
    public int peopleNum;
    @Column(name = "tenantId")
    public String tenantId;

    @Column(name = "mId", index = true)
    private Long messageId;
    private ChatMessage message;

    public boolean create;
    public ChatUser user;

    public ChatGroup() {
    }

    public ChatGroup(final String owner, final String name, final String type, String tenantId) {
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.tenantId = tenantId;
    }

    public ChatGroup(final GroupInfo groupInfo, final String creator, String tenantId) {
        this(creator, groupInfo.name, GroupTypeGroup, tenantId);
        groupNick = groupInfo.groupNick;
        addStaffs(groupInfo.staffs);
        peopleNum++;
    }

    public ChatGroup(final Staff staff, final String creator, String tenantId) {
        this(creator, staff.name(), GroupTypeChat, tenantId);
        user = new ChatUser(staff);
    }

    public ChatGroup(final List<Staff> staffs, final String creator, String tenantId) {
        this(creator, null, GroupTypeGroup, tenantId);
        time = System.currentTimeMillis();
        name = "adr" + String.valueOf(time);
        this.creator = creator;
        addStaffs(staffs);
        peopleNum++;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(final String creator) {
        if (Strings.notEmpty(creator)) {
            this.creator = creator;
        }
    }

    public String getDisplayNick() {
        if (TextUtils.isEmpty(groupNick)) {
            return nick;
        }
        return groupNick;
    }

    public boolean isFailure() {
        return getMessage() != null && message.isFailure;
    }

    public String getContent(final Resources res) {
        String content = "";
        if (getMessage() != null) {
            if (ChatMessage.MessageTypeFile.equals(message.type)) {
                Subject.File file = message.getFile();
                if (TYPE_PICTURE.equals(file.ext)) {
                    content = "[" + res.getString(R.string.picture) + "]";
                } else if (TYPE_AUDIO.equals(file.ext)) {
                    content = "[" + res.getString(R.string.voice) + "]";
                }
            } else if (MessageTypeGps.equals(message.type)) {
                content = "[" + res.getString(R.string.location) + "]";
            } else {
                content = message.content;
                if (!TextUtils.isEmpty(content)) {
                    if (content.length() > 100) {
                        content = content.substring(0, 50);
                    }
                }
            }
        }
        return content;
    }

    public String getDisplayTime() {
        return ChatMessage.getDisplayTime(time);
    }

    public ChatUser user() {
        if (user == null && GroupTypeChat.equals(type)) {
            user = new Select()
                    .from(ChatUser.class)
                    .where("uid = ?", name)
//                    .where("uid = ? and tenantId = ?", name,tenantId)
                    .executeSingle();
        }
        return user;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessage(final ChatMessage message) {
        this.message = message;
        if (message != null) {
            messageId = message.getId();
        } else {
            messageId = null;
        }
    }

    public ChatMessage getMessage() {
        if (messageId != null && message == null) {
            message = load(ChatMessage.class, messageId);
        }
        return message;
    }

    public void addStaffs(final List<Staff> staffs) {
        peopleNum += staffs.size();
        ArrayList<String> avatars = null;
        if (Strings.isEmpty(avatar)) {
            avatars = new Gson().fromJson(avatar, new TypeToken<List<String>>() {
            }.getType());
        }
        if (avatars == null) {
            avatars = new ArrayList<>(0);
        }
        StringBuilder nickBuilder = new StringBuilder();
        if (nick != null) {
            nickBuilder.append(nick).append(",");
        }
        for (Staff staff : staffs) {
//            if (avatars.size() < 9) {
            if (!staff.name().equals(owner)) {
                nickBuilder.append(staff.nick).append(",");
            }
            avatars.add(staff.avatar);
//            }
        }
        nickBuilder.deleteCharAt(nickBuilder.length() - 1);
        nick = nickBuilder.toString();
        avatar = new Gson().toJson(avatars);
    }

    public void deletefromMessage() {
        try {
            ActiveAndroid.beginTransaction();
            new Delete()
                    .from(ChatMessage.class)
                    .where("gid = ?", getId())
                    .execute();
            time = 0;
            save();
            ActiveAndroid.clearCache();
            ActiveAndroid.setTransactionSuccessful();
        } catch (Exception e) {
            Ln.e(e);
        } finally {
            ActiveAndroid.endTransaction();
        }
    }
    public void destroy() {
        try {
            ActiveAndroid.beginTransaction();
            new Delete()
                    .from(ChatMessage.class)
                    .where("gid = ?", getId())
                    .execute();
            delete();
            ActiveAndroid.clearCache();
            ActiveAndroid.setTransactionSuccessful();
        } catch (Exception e) {
            Ln.e(e);
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    public void destroyMessages() {
        try {
            ActiveAndroid.beginTransaction();
            new Delete()
                    .from(ChatMessage.class)
                    .where("gid = ?", getId())
                    .execute();
            ActiveAndroid.clearCache();
            ActiveAndroid.setTransactionSuccessful();
        } catch (Exception e) {
            Ln.e(e);
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    public List<ChatMessage> getMessages(long maxId) {
        if (getId() == null) {
            return Collections.emptyList();
        }
//  return getMany(ChatMessage.class, "gid");
        From from = new Select()
                .from(ChatMessage.class);
        if (maxId > 0) {
            from.where("gid = ? and id < ?", getId(), maxId);
        } else {
            from.where("gid = ?", getId());
        }
        List<ChatMessage> messages = from.orderBy("id desc")
                .limit(20)
                .execute();
        Collections.reverse(messages);
        return messages;
    }

    public List<ChatMessage> findMessagesByContent(final String content) {
        return new Select()
                .from(ChatMessage.class)
                .where("gid = ? and type = ? and content like ?", getId(), MessageTypeNormal, "%" + content + "%")
                .execute();
    }

    public static List<ChatMessage> searchAllMessageByContent(String owner, String tenantId,  String content) {
//        if (!TextUtils.isEmpty(owner) && owner.length() == 18)
//            owner += tenantId;
        if (!TextUtils.isEmpty(owner)){
            if (owner.indexOf(tenantId) == -1) {
                owner = owner + tenantId;
            }
        }
        List<ChatMessage> chatMessages = new Select()
                .from(ChatMessage.class)
                .where("type = ? and content like ?", MessageTypeNormal, "%" + content + "%")
                .execute();
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < chatMessages.size(); i++) {
            ChatMessage cmsg = chatMessages.get(i);
            if (cmsg.group.owner.equals(owner)) {
                messages.add(cmsg);
            }
        }
        return messages;
    }

    public static List<ChatMessage> searchMessageByGid(String owner, String tenantId, final String content, long gid) {
//        if (!TextUtils.isEmpty(owner) && owner.length() == 18)
//            owner += tenantId;
        if (!TextUtils.isEmpty(owner)){
            if (owner.indexOf(tenantId) == -1) {
                owner = owner + tenantId;
            }
        }
        return new Select()
                .from(ChatMessage.class)
                .where("gid = ? and type = ? and content like ?", gid, MessageTypeNormal, "%" + content + "%")
                .execute();
    }

    public List<ChatMessage> findMessagesByMinId(final long minId) {
        return new Select()
                .from(ChatMessage.class)
                .where("gid = ? and id >= ?", getId(), minId)
                .execute();
    }

    public void clearUnreadNum() {
        unreadNum = 0;
        if (getId() != null) {
            save();
        }
    }

    public Message.Type getChatType() {
        Message.Type chatType = Message.Type.chat;
        if (ChatGroup.GroupTypeGroup.equals(type)) {
            chatType = Message.Type.groupchat;
        }
        return chatType;
    }

    public static List<ChatGroup> findAll(String owner, String tenantId) {
        try {
//            if (!TextUtils.isEmpty(owner) && owner.length() == 18)
//                owner += tenantId;
            if (!TextUtils.isEmpty(owner)){
                if (owner.indexOf(tenantId) == -1) {
                    owner = owner + tenantId;
                }
            }
            return new Select()
                    .from(ChatGroup.class)
                    .where("owner = ? and tenantId = ?",
                            owner, tenantId)
                    .orderBy("time desc")
                    .execute();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static List<ChatGroup> findAllByGroup(String owner, String tenantId) {
        try {
//            if (!TextUtils.isEmpty(owner) && owner.length() == 18)
//                owner += tenantId;
            if (!TextUtils.isEmpty(owner)){
                if (owner.indexOf(tenantId) == -1) {
                    owner = owner + tenantId;
                }
            }
            return new Select()
                    .from(ChatGroup.class)
                    .where("type = ? and owner = ? and tenantId = ?",
                            GroupTypeGroup, owner, tenantId)
                    .orderBy("time desc")
                    .execute();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static List<ChatGroup> findAllbyChat(String owner, String tenantId) {
        try {
//            if (!TextUtils.isEmpty(owner) && owner.length() == 18)
//                owner += tenantId;
            if (!TextUtils.isEmpty(owner)){
                if (owner.indexOf(tenantId) == -1) {
                    owner = owner + tenantId;
                }
            }
            return new Select()
                    .from(ChatGroup.class)
                    .where("time > 0 and owner = ? and tenantId = ?",
                            owner, tenantId)
                    .orderBy("time desc")
                    .execute();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void updateServiceId(String owner, String tenantId, String uid, String lastUid) {
//        if (!TextUtils.isEmpty(owner) && owner.length() == 18)
//            owner += tenantId;
        if (!TextUtils.isEmpty(owner)){
            if (owner.indexOf(tenantId) == -1) {
                owner = owner + tenantId;
            }
        }
        new Update(ChatGroup.class).set("name = ?", uid).where("name = ? and owner = ? and tenantId = ?", lastUid, owner, tenantId).execute();
    }

    public static List<ChatGroup> findAllGroup(String owner, String tenantId) {
//        if (!TextUtils.isEmpty(owner) && owner.length() == 18)
//            owner += tenantId;
        if (!TextUtils.isEmpty(owner)){
            if (owner.indexOf(tenantId) == -1) {
                owner = owner + tenantId;
            }
        }
        return new Select()
                .from(ChatGroup.class)
                .where("owner = ? and type = ? and tenantId = ?", owner, ChatGroup.GroupTypeGroup, tenantId)
                .orderBy("time desc")
                .execute();
    }

    public static ChatGroup find(long id) {
        return new Select()
                .from(ChatGroup.class)
                .where("Id = ?", id)
                .executeSingle();
    }

    public static ChatGroup find(final String name, String owner, final String type, String tenantId) {
//        if (!TextUtils.isEmpty(owner) && owner.length() == 18)
//            owner += tenantId;
        if (!TextUtils.isEmpty(owner)){
            if (owner.indexOf(tenantId) == -1) {
                owner = owner + tenantId;
            }
        }
        return new Select()
                .from(ChatGroup.class)
                .where("name = ? and owner = ? and type = ? and tenantId = ?", name, owner, type, tenantId)
                .executeSingle();
    }

    public static ChatGroup updateFromChatType(String owner, final long time, final ChatUser user, String tenantId) {
//        if (!TextUtils.isEmpty(owner) && owner.length() == 18)
//            owner += tenantId;
        if (!TextUtils.isEmpty(owner)){
            if (owner.indexOf(tenantId) == -1) {
                owner = owner + tenantId;
            }
        }
        ChatGroup group = ChatGroup.find(user.name, owner, ChatGroup.GroupTypeChat, tenantId);
        if (group == null) {
            group = new ChatGroup(owner, user.name, GroupTypeChat, tenantId);
            group.unreadNum = 1;
        } else {
            group.unreadNum++;
        }
        group.time = time;
        group.nick = user.nick;
        group.avatar = user.avatar;
        group.save();
        return group;
    }

    public static ChatGroup updateFromGroupType(final String name, String owner, final long time, final Subject subject, String tenantId) {
//        if (!TextUtils.isEmpty(owner) && owner.length() == 18)
//            owner += tenantId;
        if (!TextUtils.isEmpty(owner)){
            if (owner.indexOf(tenantId) == -1) {
                owner = owner + tenantId;
            }
        }
        ChatGroup group = ChatGroup.find(name, owner, ChatGroup.GroupTypeGroup, tenantId);
        if (group == null) {
            group = new ChatGroup(owner, name, GroupTypeGroup, tenantId);
            group.unreadNum = 1;
        } else {
            group.unreadNum++;
        }
        group.time = time;
      //  group.isExit = false;
        if (ChatMessage.MessageTypeGroupModifyName.equals(subject.type)) {
            group.groupNick = subject.getGroupNick();
        } else if (ChatMessage.MessageTypeGroupAddMembers.equals(subject.type)) {
            group.nick = subject.getGroupNick();
        } else if (ChatMessage.MessageTypeGroupDelMember.equals(subject.type)) {
            if (owner.equals(subject.delUser)) {
                group.isExit = false;
            }
            group.nick = subject.getGroupNick();
        }

        if (Strings.isEmpty(group.nick)) {
            group.nick = subject.getGroupNick();
        }

        group.save();
        return group;
    }
    public static ChatGroup queryFromGroupType(final String name, String owner, final long time, final Subject subject, String tenantId) {
//        if (!TextUtils.isEmpty(owner) && owner.length() == 18)
//            owner += tenantId;
        if (!TextUtils.isEmpty(owner)){
            if (owner.indexOf(tenantId) == -1) {
                owner = owner + tenantId;
            }
        }
        ChatGroup group = ChatGroup.find(name, owner, ChatGroup.GroupTypeGroup, tenantId);
        if (group == null) {
            group = new ChatGroup(owner, name, GroupTypeGroup, tenantId);
        } else {
        }
      //  group.isExit = false;
        if (ChatMessage.MessageTypeGroupModifyName.equals(subject.type)) {
            group.groupNick = subject.getGroupNick();
        } else if (ChatMessage.MessageTypeGroupAddMembers.equals(subject.type)) {
            group.nick = subject.getGroupNick();
        } else if (ChatMessage.MessageTypeGroupDelMember.equals(subject.type)) {
            if (owner.equals(subject.delUser)) {
                group.isExit = false;
            }
            group.nick = subject.getGroupNick();
        }

        if (Strings.isEmpty(group.nick)) {
            group.nick = subject.getGroupNick();
        }
        return group;
    }

    public static ChatGroup updateFromGroupType(final String name, String owner, final long time, String tenantId, String groupNick) {
//        if (!TextUtils.isEmpty(owner) && owner.length() == 18)
//            owner += tenantId;
        if (!TextUtils.isEmpty(owner)){
            if (owner.indexOf(tenantId) == -1) {
                owner = owner + tenantId;
            }
        }
        ChatGroup group = ChatGroup.find(name, owner, ChatGroup.GroupTypeGroup, tenantId);
        if (group == null) {
            group = new ChatGroup(owner, name, GroupTypeGroup, tenantId);
            group.unreadNum = 1;
        } else {
            group.unreadNum++;
        }
        group.time = time;
      //  group.isExit = false;
        group.groupNick = groupNick;
        return group;
    }

    public static ChatGroup fromStaff(final Staff staff, String owner, String tenantId) {
//        if (!TextUtils.isEmpty(owner) && owner.length() == 18)
//            owner += tenantId;
        if (!TextUtils.isEmpty(owner)){
            if (owner.indexOf(tenantId) == -1) {
                owner = owner + tenantId;
            }
        }
        ChatGroup group = ChatGroup.find(staff.name(), owner, ChatGroup.GroupTypeChat, tenantId);
        if (group == null) {
            group = new ChatGroup(staff, owner, tenantId);
        }
        return group;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other)                                      //先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof ChatGroup))
            return false;
        if (!name.equals(((ChatGroup) other).name))
            return false;
        return true;
    }

}
