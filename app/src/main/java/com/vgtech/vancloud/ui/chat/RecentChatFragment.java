package com.vgtech.vancloud.ui.chat;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.vgtech.common.PrfUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.adapter.DataAdapter;
import com.vgtech.vancloud.ui.chat.controllers.AvatarController;
import com.vgtech.vancloud.ui.chat.controllers.XmppController;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.view.groupimageview.NineGridImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import roboguice.inject.InjectView;
import roboguice.util.Strings;

import static com.vgtech.vancloud.ui.chat.models.ChatGroup.GroupTypeChat;

/**
 * @author xuanqiang
 */
public class RecentChatFragment extends ActionBarFragment{
  @InjectView(R.id.recent_chat_list_view) ListView listView;
  @InjectView(R.id.recent_chat_editText) EditText searchEditView;
  @InjectView(R.id.recent_chat_create) View newChatView;

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    return attachToSwipeBack(createContentView(R.layout.recent_chat));
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState){
    super.onActivityCreated(savedInstanceState);
    titleView.setText(R.string.select);
    newChatView.setOnClickListener(this);
    searchEditView.addTextChangedListener(new TextWatcher(){
      @Override
      public void beforeTextChanged(CharSequence text, int start, int count, int after){}
      @Override
      public void afterTextChanged(Editable editable){
        String input = editable.toString();
        if(Strings.isEmpty(input)){
          setListData(dataSource);
        }else{
          List<ChatGroup> list = new ArrayList<ChatGroup>(0);
          for(ChatGroup group : dataSource) {
            String str = group.getDisplayNick();
            if(str != null && str.contains(input)) {
              list.add(group);
            }
          }
          setListData(list);
        }
      }
      @Override
      public void onTextChanged(CharSequence text, int start, int before, int count){}
    });

    View.OnTouchListener touchListener = new View.OnTouchListener(){
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent){
        imManager.hideSoftInputFromWindow(getView().getWindowToken(),InputMethodManager.RESULT_UNCHANGED_SHOWN);
        return false;
      }
    };
    listView.setOnTouchListener(touchListener);

    adapter = new Adapter();

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
      @Override
      public void onItemClick(AdapterView<?> adapterView, View convertView, int position, long id){
        ChatGroup group = adapter.dataSource.get(position);
        if(listener != null){
          controller.fm().popBackStack();
          listener.selectedContacts(null, group);
        }
      }
    });

    listView.setAdapter(adapter);
    dataSource.addAll(ChatGroup.findAll(PrfUtils.getUserId(getActivity()), PrfUtils.getTenantId(getActivity())));
    setListData(dataSource);
  }

  private void setListData(final List<ChatGroup> dataSource){
    adapter.dataSource.clear();
    adapter.dataSource.addAll(dataSource);
    adapter.notifyDataSetChanged();
  }

  private class Adapter extends DataAdapter<ChatGroup> {
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
      ChatGroup group = dataSource.get(position);
      ViewHolder viewHolder;
      if(convertView == null) {
        viewHolder = new ViewHolder();
        convertView = getLayoutInflater(null).inflate(R.layout.recent_chat_item, null);
        assert convertView != null;
        viewHolder.avatarView = (SimpleDraweeView)convertView.findViewById(R.id.avatar);
        viewHolder.avatarContainer = (NineGridImageView)convertView.findViewById(R.id.avatar_container);
        viewHolder.nameLabel = (TextView)convertView.findViewById(R.id.messages_item_name);
        viewHolder.numLabel = (TextView)convertView.findViewById(R.id.numLabel);
        convertView.setTag(viewHolder);
      }else {
        viewHolder = (ViewHolder)convertView.getTag();
      }

      viewHolder.nameLabel.setText(group.getDisplayNick());
      if(GroupTypeChat.equals(group.type)) {
        viewHolder.numLabel.setText("");
      }else {
        viewHolder.numLabel.setText("(" + group.peopleNum + ")");
      }
      int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
      viewHolder.numLabel.measure(spec, spec);
      viewHolder.nameLabel.setMaxWidth(RecentChatFragment.this.getView().getMeasuredWidth()
        - viewHolder.numLabel.getMeasuredWidth() - controller.getPixels(100));
      List<String> avatars = null;
      if(GroupTypeChat.equals(group.type)) {
        avatars = new ArrayList<String>(1);
        avatars.add(group.avatar);
      }else {
        try {
          avatars = new Gson().fromJson(group.avatar, new TypeToken<List<String>>(){}.getType());
        }catch(JsonSyntaxException ignored) {
          if (!TextUtils.isEmpty(group.avatar)) {
            String[] avatarArray = group.avatar.split(",");
            avatars = new ArrayList(Arrays.asList(avatarArray));
          }
        }
        if(avatars == null) {
          avatars = new ArrayList<String>(1);
          avatars.add("");
        }
      }
      avatarController.setAvatarContainer(viewHolder.avatarView, viewHolder.avatarContainer, avatars);

      return convertView;
    }

    class ViewHolder {
      SimpleDraweeView avatarView;
      NineGridImageView avatarContainer;
      TextView nameLabel;
      TextView numLabel;
    }
  }

  @Override
  public void onClick(View view){
    if(view == newChatView){
//      ContactsFragment fragment = ContactsFragment.newMultiSelectInstance();
//      fragment.setListener(listener);
//      controller.pushFragment(fragment);
    }else{
      super.onClick(view);
    }
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser){
    super.setUserVisibleHint(isVisibleToUser);
    if(!isVisibleToUser && !isDetached()){
      searchEditView.clearFocus();
      imManager.hideSoftInputFromWindow(getView().getWindowToken(),InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
  }

  public void setListener(ContactsListener listener){
    this.listener = listener;
  }

  private ContactsListener listener;
  List<ChatGroup> dataSource = new ArrayList<ChatGroup>(0);
  Adapter adapter;
  @Inject InputMethodManager imManager;
  @Inject
  AvatarController avatarController;
  @Inject private XmppController xmpp;


}
