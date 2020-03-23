package com.vgtech.vancloud.ui.chat.models;

import android.content.res.Resources;
import android.text.TextUtils;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.google.gson.Gson;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.models.Subject;

import java.io.File;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Random;

import static com.activeandroid.annotation.Column.ForeignKeyAction;
import static com.vgtech.vancloud.models.Subject.File.TYPE_AUDIO;
import static com.vgtech.vancloud.models.Subject.File.TYPE_PICTURE;
import static com.vgtech.vancloud.models.Subject.Gps;

/**
 * @author xuanqiang
 */
@Table(name = "messages")
public class ChatMessage extends Model implements Serializable {
    private static final long serialVersionUID = -7615763991643780846L;

    public static final String MessageTypeNormal = "nml";//普通消息
    public static final String MessageTypeFile = "fil";//文件消息
    public static final String MessageTypeGps = "gps";//地理位置

    public static final String MessageTypeGroupAddMembers = "groupAdd";//组增加成员
    public static final String MessageTypeGroupDelMember = "groupDel";//组删除成员
    public static final String MessageTypeGroupModifyName = "groupMod";

    @Column(name = "uid")
    public ChatUser user;
    @Column(name = "time")
    public long time;
    @Column(name = "content")
    public String content;
    @Column(name = "affiliated")
    public String affiliated;
    @Column(name = "isFailure", length = 1)
    public boolean isFailure;
    @Column(name = "isSender", length = 1)
    public boolean isSender;
    @Column(name = "type", index = true, length = 20)
    public String type = MessageTypeNormal;
    @Column(name = "packetId", index = true, length = 30)
    public String packetId;
    @Column(name = "gid", index = true, onDelete = ForeignKeyAction.CASCADE)
    public ChatGroup group;

    public ShowType showType = ShowType.unknown;
    private Subject.File file;
    private Gps gps;

    @SuppressWarnings("UnusedDeclaration")
    public ChatMessage() {
    }

    public ShowType getShowType() {
        if (showType == ShowType.unknown) {
            showType = isSender ? ShowType.normal_my : ShowType.normal;
            //noinspection StatementWithEmptyBody
            if (MessageTypeNormal.equals(type)) {
            } else if (MessageTypeGroupAddMembers.equals(type) || MessageTypeGroupDelMember.equals(type)
                    || MessageTypeGroupModifyName.equals(type)) {
                showType = ShowType.text_tip;
            } else if (MessageTypeFile.equals(type)) {
                Subject.File file = getFile();
                if (TYPE_PICTURE.equals(file.ext)) {
                    showType = isSender ? ShowType.file_pic_my : ShowType.file_pic;
                } else if (TYPE_AUDIO.equals(file.ext)) {
                    showType = isSender ? ShowType.file_au_my : ShowType.file_au;
                }
            } else if (MessageTypeGps.equals(type)) {
                showType = isSender ? ShowType.gps_my : ShowType.gps;
            }
        }
        return showType;
    }

    public ChatMessage(final String content, final String type, final ChatUser user,
                       final ChatGroup group, final boolean isSender) {
        this.content = content;
        this.type = type;
        this.user = user;
        this.time = group.time;
        this.isSender = isSender;
        this.group = group;
        this.packetId = generatePacketId();
    }

    public String getDisplayTime() {
        return getDisplayTime(time);
    }

    public Subject.File getFile() {
        if (file == null) {
            if (MessageTypeFile.equals(type)) {
                file = new Gson().fromJson(affiliated, Subject.File.class);
            }
        }
        return file;
    }

    public void setFile(final Subject.File file) {
        if (file != null) {
            this.file = file;
            affiliated = new Gson().toJson(file);
        }
    }

    public Gps getGps() {
        if (gps == null) {
            if (MessageTypeGps.equals(type)) {
                gps = new Gson().fromJson(affiliated, Gps.class);
            }
        }
        return gps;
    }

    public void setGps(final Gps gps) {
        if (gps != null) {
            this.gps = gps;
            affiliated = new Gson().toJson(gps);
        }
    }

    public String displayImageUrl() {
        if (isSender) {
            return "file://" + getFile().getFilePath();
        } else {
            return getFile().url;
        }
    }

    public String imageRealPath() {
        if (isSender) {
            return getFile().getFilePath();
        } else {
            ImageRequest imageRequest = ImageRequest.fromUri(getFile().url);
            CacheKey cacheKey = DefaultCacheKeyFactory.getInstance()
                    .getEncodedCacheKey(imageRequest, null);
            BinaryResource resource = ImagePipelineFactory.getInstance()
                    .getMainDiskStorageCache().getResource(cacheKey);
            File file = ((FileBinaryResource) resource).getFile();
            return file.getAbsolutePath();
        }
    }

    public void destroy() {
        delete();
        if (getId().equals(group.getMessageId())) {
            ChatMessage msg = new Select()
                    .from(ChatMessage.class).
                            where("gid = ?", group.getId())
                    .orderBy("id desc")
                    .executeSingle();
            group.setMessage(msg);
            group.save();
        }
        Cache.removeEntity(group);
        if (MessageTypeFile.equals(type)) {
            Subject.File file = getFile();
            if (!TextUtils.isEmpty(file.getFilePath())) {
                File f = new File(file.getFilePath());
                if (f.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                }
            }
            if (Subject.File.TYPE_PICTURE.equals(file.ext)) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    ImageRequest imageRequest = ImageRequest.fromUri(displayImageUrl());
                    CacheKey cacheKey = DefaultCacheKeyFactory.getInstance()
                            .getEncodedCacheKey(imageRequest, null);
                    ImagePipelineFactory.getInstance()
                            .getMainDiskStorageCache().remove(cacheKey);
                } catch (Exception e) {
//          Ln.d(e);
                }
            }
        }
    }

    public static String generatePacketId() {
        return String.valueOf(System.currentTimeMillis()) + new Random().nextInt(3);
    }

    public static ChatMessage create(final Resources res, final String content, final ChatGroup group,
                                     final ChatUser user, final Subject subject, final boolean isSender) {
        ChatMessage chatMessage = new ChatMessage(content, subject.type, user, group, isSender);
        chatMessage.setGps(subject.gps);
        if (!isSender) {
            if (subject.file != null && TYPE_AUDIO.equals(subject.file.ext)) {
                subject.file.setUnread(true);
            }
            if (ChatMessage.MessageTypeGroupDelMember.equals(subject.type)) {
                if (group.owner.equals(subject.delUser)) {
                    chatMessage.content = subject.getNick() + res.getString(R.string.remove_my_group_chat);
                } else {
                    chatMessage.content = content + res.getString(R.string.exit_group_chat);
                }
            }
        }
        chatMessage.setFile(subject.file);

        chatMessage.save();
        group.setMessage(chatMessage);
        group.save();

        return chatMessage;
    }

    public static String getDisplayTime(long time) {
        if (time <= 0)
            return "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH);
        int d = calendar.get(Calendar.DAY_OF_MONTH);
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);

        calendar.setTimeInMillis(System.currentTimeMillis());
        int cy = calendar.get(Calendar.YEAR);
        int cm = calendar.get(Calendar.MONTH);
        int cd = calendar.get(Calendar.DAY_OF_MONTH);

        if (y == cy && m == cm && d == cd) {
            return "" + (h < 10 ? "0" + h : h) + ":" + (min < 10 ? "0" + min : min);
        } else {
            if (y == cy) {
                m++;
                return "" + (m < 10 ? "0" + m : m) + "-" + (d < 10 ? "0" + d : d) + " " + (h < 10 ? "0" + h : h) + ":" + (min < 10 ? "0" + min : min);
            } else {
                m++;
                return "" + y + "-" + (m < 10 ? "0" + m : m) + "-" + (d < 10 ? "0" + d : d) + " " + (h < 10 ? "0" + h : h) + ":" + (min < 10 ? "0" + min : min);
            }
        }
    }

    public static ChatMessage findByPacketId(final String packetId) {
        return new Select()
                .from(ChatMessage.class)
                .where("packetId = ?", packetId)
                .executeSingle();
    }

    public static enum ShowType {
        unknown, text_tip, normal, normal_my,
        file_pic, file_pic_my, file_au, file_au_my, gps, gps_my
    }

    public boolean isPlaying;

}
