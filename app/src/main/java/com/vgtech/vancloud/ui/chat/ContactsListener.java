package com.vgtech.vancloud.ui.chat;


import com.vgtech.vancloud.models.Staff;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;

import java.util.List;

/**
 * @author xuanqiang
 * @date 13-7-29
 */
public interface ContactsListener {
  void selectedContacts(List<Staff> contactses, ChatGroup group);
}
