package com.vgtech.vancloud.ui.chat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.inject.Inject;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.adapter.DataAdapter;
import com.vgtech.vancloud.ui.chat.controllers.AvatarController;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.chat.models.ChatMessage;

import java.util.List;

import roboguice.inject.InjectView;
import roboguice.util.Strings;

/**
 * @author xuanqiang
 * @date 14-3-27
 */
public class SearchUserMessagesFragment extends ActionBarFragment {
  @InjectView(R.id.listView) ListView listView;
  @InjectView(R.id.editText) EditText editView;

  public static SearchUserMessagesFragment create(final ChatGroup group) {
    Bundle bundle = new Bundle();
    bundle.putSerializable("group", group);
    SearchUserMessagesFragment fragment = new SearchUserMessagesFragment();
    fragment.setArguments(bundle);
    return fragment;
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
   // getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    Bundle bundle = getArguments();
    group = (ChatGroup)bundle.getSerializable("group");
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return attachToSwipeBack(createContentView(R.layout.search_user_messages));
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    titleView.setText(R.string.find_chats);
    editView.setOnKeyListener(new View.OnKeyListener(){
      @Override
      public boolean onKey(View v,int keyCode, KeyEvent event){
        if(KeyEvent.KEYCODE_ENTER == keyCode && event.getAction() == KeyEvent.ACTION_UP){
          Editable editable = editView.getText();
          if(editable != null) {
            String q = editable.toString();
            if(Strings.notEmpty(q)) {
              setListData(group.findMessagesByContent(q));
            }
          }
          return true;
        }
        return false;
      }
    });

    //region listView touch
    View.OnTouchListener touchListener = new View.OnTouchListener(){
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent){
        imManager.hideSoftInputFromWindow(getView().getWindowToken(),InputMethodManager.RESULT_UNCHANGED_SHOWN);
        return false;
      }
    };
    listView.setOnTouchListener(touchListener);
    //endregion

    adapter = new Adapter();

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
      @Override
      public void onItemClick(AdapterView<?> adapterView, View convertView, int position, long id){
        ChatMessage message = adapter.dataSource.get(position);
        controller.pushFragment(UsersMessagesShowFragment.create(group, message));
      }
    });
    listView.setAdapter(adapter);

    editView.requestFocus();
    imManager.showSoftInput(editView, InputMethodManager.HIDE_NOT_ALWAYS);
  }

  private void setListData(final List<ChatMessage> dataSource){
    adapter.dataSource.clear();
    adapter.dataSource.addAll(dataSource);
    adapter.notifyDataSetChanged();
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if(!isVisibleToUser && !isDetached()) {
      editView.clearFocus();
      imManager.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
  }

  private class Adapter extends DataAdapter<ChatMessage> {
    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
      ChatMessage message = dataSource.get(position);
      ViewHolder viewHolder;
      if(convertView == null) {
        viewHolder = new ViewHolder();
        convertView = getLayoutInflater(null).inflate(R.layout.search_user_messages_item, null);
        assert convertView != null;
        viewHolder.avatarView = (SimpleDraweeView)convertView.findViewById(R.id.avatar);
        viewHolder.nameLabel = (TextView)convertView.findViewById(R.id.messages_item_name);
        viewHolder.timeLabel = (TextView)convertView.findViewById(R.id.messages_item_time);
        viewHolder.contentLabel = (TextView)convertView.findViewById(R.id.messages_item_content);
        convertView.setTag(viewHolder);
      }else {
        viewHolder = (ViewHolder)convertView.getTag();
      }
      viewHolder.nameLabel.setText(message.user.nick);
      AvatarController.setAvatarView(message.user.avatar, viewHolder.avatarView);
      viewHolder.timeLabel.setText(message.getDisplayTime());
      viewHolder.contentLabel.setText(EmojiFragment.getEmojiContentWithAt(getActivity(), viewHolder.contentLabel.getTextSize(),message.content));
      return convertView;
    }

    class ViewHolder {
      SimpleDraweeView avatarView;
      TextView nameLabel;
      TextView timeLabel;
      TextView contentLabel;
    }
  }

  private ChatGroup group;
  Adapter adapter;
  @Inject InputMethodManager imManager;

}
