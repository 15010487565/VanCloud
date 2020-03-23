package com.vgtech.vancloud.ui.module.contact;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.Node;
import com.vgtech.common.swipeback.SwipeBackFragment;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.models.Staff;
import com.vgtech.vancloud.ui.adapter.DataAdapter;
import com.vgtech.vancloud.ui.chat.OnEvent;
import com.vgtech.vancloud.ui.chat.UsersMessagesFragment;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.chat.controllers.XmppController;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.fragment.BaseSwipeBackFragment;
import com.vgtech.vancloud.ui.group.OrganizationSelectedActivity;
import com.vgtech.vancloud.ui.group.OrganizationSelectedListener;
import com.vgtech.vancloud.ui.view.groupimageview.NineGridImageView;
import com.vgtech.vancloud.ui.view.groupimageview.NineGridImageViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import roboguice.event.Observes;
import roboguice.fragment.RoboFragment;

import static com.vgtech.vancloud.ui.chat.models.ChatGroup.GroupTypeChat;

/**
 * 群聊列表
 * Created by brook on 16/9/9.
 */
public class ChatGroupFragment extends BaseSwipeBackFragment implements View.OnClickListener {

    private View mView;
    private ListView listView;
    private Adapter adapter;
    private VancloudLoadingLayout loadingLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_chat_list, null);
        initView();
        return attachToSwipeBack(mView);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    private void initView() {
        mView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        TextView titleTv = (TextView) mView.findViewById(android.R.id.title);
        titleTv.setText(R.string.chat_group);
        listView = (ListView) mView.findViewById(R.id.listView);
        View createView = mView.findViewById(R.id.btn_right);
        createView.setVisibility(View.VISIBLE);
        createView.setOnClickListener(this);
        mView.findViewById(R.id.btn_back).setOnClickListener(this);
        loadingLayout = (VancloudLoadingLayout) mView.findViewById(R.id.loading);
    }


    private void initData() {
        adapter = new Adapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (controller.isFastDoubleClick()) {
                    return;
                }
                controller.pushFragment(UsersMessagesFragment
                        .newInstance(adapter.dataSource.get(position), null));
            }
        });
        reloadData();
    }


    @SuppressWarnings("UnusedDeclaration")
    void handleEvent(@Observes OnEvent event) {
        eventHandler.sendMessage(eventHandler.obtainMessage(0, event));
    }

    private Handler eventHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (getActivity() != null)
                reloadData();
        }
    };


    private static final int REQUEST_CODE_USERSELECT = 1001;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_right:
                Intent intent = new Intent(getActivity(), OrganizationSelectedActivity.class);
                intent.putExtra(OrganizationSelectedListener.SELECT_MODE, OrganizationSelectedListener.SELECT_MULTI);
                startActivityForResult(intent, REQUEST_CODE_USERSELECT);
                break;
            case R.id.btn_back:
                getActivity().onBackPressed();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_USERSELECT:
                if (resultCode == Activity.RESULT_OK) {
                    ArrayList<Node> userSelectList = data.getParcelableArrayListExtra("select");
                    if (userSelectList != null && !userSelectList.isEmpty()) {
                        List<Staff> contactses = new ArrayList<Staff>();
                        for (Node node : userSelectList) {
                            if (node.isUser()) {
                                Staff staff = new Staff(String.valueOf(node.getId()), String.valueOf(node.getId()), node.getName(), node.getPhoto(), PrfUtils.getTenantId(getActivity()));
                                contactses.add(staff);
                            }
                        }
                        if (contactses.isEmpty()) {
                            Toast.makeText(getActivity(), R.string.toast_chatgroup_empty, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        xmpp.chat(contactses, null);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private boolean first;

    private synchronized void reloadData() {
        if (first)
            loadingLayout.showLoadingView(listView, "", true);
        new AsyncTask<Void, Void, List<ChatGroup>>() {
            @Override
            protected List<ChatGroup> doInBackground(Void... params) {
                ArrayList<ChatGroup> groups = new ArrayList<>();
                List<ChatGroup> list = ChatGroup.findAll(PrfUtils.getUserId(getActivity()), PrfUtils.getTenantId(getActivity()));
                for (ChatGroup chatGroup : list) {
                    if (ChatGroup.GroupTypeGroup.equals(chatGroup.type)&&chatGroup.isExit)
                        groups.add(chatGroup);
                }
                return groups;
            }

            @Override
            protected void onPostExecute(List<ChatGroup> groups) {
                if (first) {
                    loadingLayout.dismiss(listView);
                    first = false;
                }
                adapter.dataSource.clear();
                adapter.dataSource.addAll(groups);
                adapter.notifyDataSetChanged();
                if (adapter.dataSource.size() == 0) {
                    loadingLayout.showEmptyView(listView, getString(R.string.no_list_data), true, true);
                    listView.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    private class Adapter extends DataAdapter<ChatGroup> {
        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getLayoutInflater(null).inflate(R.layout.chatgroup_item, null);
                viewHolder.nameLabel = (TextView) convertView.findViewById(R.id.messages_item_name);
                viewHolder.imageView = (NineGridImageView) convertView.findViewById(R.id.avatar);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ChatGroup group = dataSource.get(position);
            setView(viewHolder, group);
            return convertView;
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        class ViewHolder {
            TextView nameLabel;
            NineGridImageView imageView;
        }

        void setView(ViewHolder viewHolder, ChatGroup group) {
            String name = group.peopleNum > 0 ? group.getDisplayNick() + "(" + group.peopleNum + getString(R.string.people) + ")" : group.getDisplayNick();
            viewHolder.nameLabel.setText(name);
            List<String> avatars = null;
            if (GroupTypeChat.equals(group.type)) {
                avatars = new ArrayList<String>(1);
                avatars.add(group.avatar);
            } else {
                try {
                    avatars = new Gson().fromJson(group.avatar, new TypeToken<List<String>>() {
                    }.getType());
                } catch (JsonSyntaxException ignored) {
                    if (!TextUtils.isEmpty(group.avatar)) {
                        String[] avatarArray = group.avatar.split(",");
                        avatars = new ArrayList(Arrays.asList(avatarArray));
                    }
                }

                if (avatars == null) {
                    avatars = new ArrayList<String>(1);
                    avatars.add("");
                }
            }
            viewHolder.imageView.setAdapter(mAdapter);
            viewHolder.imageView.setImagesData(avatars);
        }
    }

    NineGridImageViewAdapter<String> mAdapter = new NineGridImageViewAdapter<String>() {

        @Override
        protected void onDisplayImage(Context context, SimpleDraweeView avatarView, String s) {
            GenericDraweeHierarchy hierarchy = avatarView.getHierarchy();
            hierarchy.setPlaceholderImage(R.mipmap.user_photo_default_small);
            hierarchy.setFailureImage(R.mipmap.user_photo_default_small);
            avatarView.setImageURI(s);
        }
    };
    @Inject
    XmppController xmpp;
    @Inject
    public Controller controller;

}
