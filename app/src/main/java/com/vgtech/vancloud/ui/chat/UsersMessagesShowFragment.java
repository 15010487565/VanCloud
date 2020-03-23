package com.vgtech.vancloud.ui.chat;

import android.os.Bundle;

import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.chat.models.ChatMessage;


/**
 * @author xuanqiang
 */
public class UsersMessagesShowFragment extends AbstractUsersMessagesFragment {

  public static UsersMessagesShowFragment create(final ChatGroup group, final ChatMessage message) {
    Bundle bundle = new Bundle();
    bundle.putSerializable("group", group);
    bundle.putSerializable("message", message);
    UsersMessagesShowFragment fragment = new UsersMessagesShowFragment();
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle bundle = getArguments();
    group = (ChatGroup)bundle.getSerializable("group");
    message = (ChatMessage)bundle.getSerializable("message");
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    adapter = new UsersMessagesAdapter(this);
    adapter.dataSource.addAll(group.findMessagesByMinId(message.getId()));
    listView.setAdapter(adapter);
  }

  private ChatMessage message;

}
