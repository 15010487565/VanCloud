package com.vgtech.vancloud.ui.chat;

/**
 * @author xuanqiang
 */
public interface MessageInputListener{
  public void onMessageSend(final String content, final Object model);
  void chageState();
}
