package com.vgtech.vancloud.models;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.vgtech.common.api.UserAccount;
import com.vgtech.vancloud.ui.chat.models.ChatMessage;

import java.io.Serializable;

import roboguice.util.Strings;

/**
 * @author xuanqiang
 */
public class Subject {
    public String uid;
    @SerializedName("face")
    public String avatar;
    public String type;
    @SerializedName("login")
    public String name;

    @SerializedName("name")
    public String nick;
    protected String sendName;
    public String tenantId;
    @SerializedName("naturalname")
    public String groupNick;
    public Room room;

    public String delUser;

    public Gps gps;
    @SerializedName("fil")
    public File file;

    public Subject(String type) {
        this.type = type;
    }

    public Subject(final UserAccount account) {
        this(ChatMessage.MessageTypeNormal);
        uid = account.getUid().toLowerCase()+account.tenant_id;
        nick = account.nickname();
        avatar = account.photo;
        name = uid;
        tenantId = account.tenant_id;
    }

    public Subject(final UserAccount account, final String type, final String groupNick) {
        this(account);
        this.type = type;
        this.groupNick = groupNick;
    }

    public Subject(final UserAccount account, final ChatMessage message, final String groupNick) {
        this(account, message.type, groupNick);
        gps = message.getGps();
        file = message.getFile();
    }

    public String getNick() {
        return Strings.notEmpty(sendName) ? sendName : nick;
    }

    public String getGroupNick() {
        return hasRoom() ? room.groupNick : groupNick;
    }

    public boolean hasRoom() {
        return room != null;
    }

    public static class Room {
        public String name;
        @SerializedName("naturalname")
        public String groupNick;
    }

    public static class Gps implements Serializable {
        private static final long serialVersionUID = 9218984236039299398L;
        @SerializedName("lat")
        public double latitude;
        @SerializedName("log")
        public double longitude;

        public Gps(final double latitude, final double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public static class File implements Serializable {
        private static final long serialVersionUID = 5734693489273518028L;
        public static final String TYPE_PICTURE = "pic";
        public static final String TYPE_AUDIO = "aud";

        @SerializedName("file_url")
        public String url;
        @SerializedName("file_ext")
        public String ext;
        private String duration;
        private String filePath;
        private boolean unread;

        public File(final String filePath, final String ext, final String duration, final String url) {
            this.filePath = filePath;
            this.ext = ext;
            if (Strings.notEmpty(duration)) {
                this.duration = duration;
            }
            if (Strings.notEmpty(url)) {
                this.url = url;
            }
        }

        public String getDuration() {
            if (TextUtils.isEmpty(duration))
                duration = "0";
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public boolean isUnread() {
            return unread;
        }

        public void setUnread(boolean unread) {
            this.unread = unread;
        }

    }

}
