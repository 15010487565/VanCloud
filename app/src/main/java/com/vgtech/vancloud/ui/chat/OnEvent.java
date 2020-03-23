package com.vgtech.vancloud.ui.chat;

import com.activeandroid.Model;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.chat.models.ChatMessage;

/**
 * @author xuanqiang
 */
public class OnEvent {
  public EventType type;
  public Model model;

  public OnEvent(final EventType type, final Model model) {
    this.type = type;
    this.model = model;
  }

  public ChatGroup getChatGroup() {
    if(model instanceof ChatGroup) {
      return (ChatGroup)model;
    }else if(model instanceof ChatMessage) {
      ChatMessage message = (ChatMessage)model;
      message.group.setMessage(message);
      return message.group;
    }
    return null;
  }

  public static enum EventType {
    NEW,GROUP_MODIFY,MESSAGE_MODIFY,MESSAGE_DELETE,CLEAR_MESSAGE_RECORD,LEAVE_GROUP
  }

}
