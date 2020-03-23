package com.vgtech.vancloud.ui.chat.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.vgtech.vancloud.models.Staff;

import java.io.Serializable;

import roboguice.util.Strings;

/**
 * @author xuanqiang
 */
@Table(name = "users")
public class ChatUser extends Model implements Serializable {
  private static final long serialVersionUID = 6622316126635513536L;

  @Column(name = "name", index = true, length = 200) public String name;
  @Column(name = "uid", length = 100)  public String uid;
  @Column(name = "tenantId", length = 100)  public String tenantId;
  @Column(name = "nick", length = 400) public String nick;
  @Column(name = "avatar") public String avatar;

  @SuppressWarnings("UnusedDeclaration")
  public ChatUser() {}

  public ChatUser(final String name, final String uid, final String nick, final String avatar,String tenantId) {
    this.name = name;
    this.uid = uid;
    this.nick = nick;
    this.avatar = avatar;
    this.tenantId = tenantId;
  }

  public ChatUser(final Staff staff) {

    this(staff.name(),staff.id,staff.nick,staff.avatar,staff.tenantId);

  }

  public static ChatUser find(final String name,String tenantId){
    return new Select().from(ChatUser.class).where("name = ? and tenantId = ?", name,tenantId).executeSingle();
  }

  public static ChatUser update(final Staff staff){

    ChatUser user = ChatUser.find(staff.name(),staff.tenantId);
    if(user == null) {
      user = new ChatUser(staff.name(), staff.id, staff.nick, staff.avatar,staff.tenantId);
    }else {
      if(Strings.isEmpty(user.uid)) {  //解决前版本bug
        user.uid = staff.id;
      }
      user.avatar = staff.avatar;
      user.nick = staff.nick;
    }
    user.save();
    return user;
  }

}
