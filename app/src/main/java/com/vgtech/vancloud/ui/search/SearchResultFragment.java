package com.vgtech.vancloud.ui.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.SearchItem;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.adapter.SearchAdapter;
import com.vgtech.vancloud.ui.chat.controllers.AvatarController;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.chat.models.ChatMessage;
import com.vgtech.vancloud.ui.fragment.BaseSwipeBackFragment;

import java.util.ArrayList;
import java.util.List;

import roboguice.fragment.RoboFragment;

/**
 * Created by vic on 2016/9/20.
 */
public class SearchResultFragment extends BaseSwipeBackFragment {
    private SearchAdapter<SearchItem> mSearchAdapter;
    @Inject
    public Controller controller;
    @Inject
    AvatarController avatarController;
    private TextView mSearchTitleTv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_search_result, null);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        view.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        mSearchAdapter = new SearchAdapter<>(this, controller,avatarController);
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        View headerView = inflater.inflate(R.layout.search_title, null);
        mSearchTitleTv = (TextView) headerView.findViewById(R.id.item_type);
        listView.addHeaderView(headerView);
        listView.setAdapter(mSearchAdapter);
        TextView titleTv = (TextView) view.findViewById(android.R.id.title);
        Bundle bundle = getArguments();
        String title = bundle.getString("title");
        String keyword = bundle.getString("keyword");
        long gid = bundle.getLong("gid");
        titleTv.setText(title);
        searchChatMessage(keyword, gid);
        return attachToSwipeBack(view);
    }

    private void searchChatMessage(String s, long gid) {
        List<ChatMessage> chatMessages = ChatGroup.searchMessageByGid(PrfUtils.getUserId(getActivity()), PrfUtils.getTenantId(getActivity()), s, gid);
        mSearchTitleTv.setText(getString(R.string.search_message_acount, chatMessages.size(), s));
        List<SearchItem> tmpList = new ArrayList<SearchItem>();
        for (int i = 0; i < chatMessages.size(); i++) {
            ChatMessage cm = chatMessages.get(i);
            ChatGroup chatGroup = cm.group;
            SearchItem searchItem = new SearchItem();
            searchItem.id = s;
            searchItem.type = SearchItem.Type.chatmessage;
            searchItem.title = chatGroup.getDisplayNick();
            String subtitle = "";
            String content = cm.content;
            content = searchSubString(s, content);
            subtitle = content.replace(s, "<font color='#3ab5ff'>" + s + "</font>");
            searchItem.subtitle = subtitle;
            searchItem.icon = chatGroup.avatar;
            searchItem.itemObj = cm;
            tmpList.add(searchItem);
        }
        mSearchAdapter.add(tmpList);
    }

    private static String searchSubString(String keyword, String content) {
        int maxlength = 20;
        if (keyword.length() < maxlength) {
            int sl = (maxlength - keyword.length()) / 2;
            int first = content.indexOf(keyword);
            int s = first - sl;
            if (s < 0)
                s = 0;
            String sub = content.substring(s);
            if (s > 0) {
                sub = "..." + sub;
            }
            return sub;
        } else {
            return keyword;
        }
    }
//    public List<SearchItem> checkItemList(List<SearchItem> items) {
//        if (!items.isEmpty()) {
//            items.get(0).isFirst = true;
//            items.get(items.size() - 1).isLast = true;
//        }
//        return items;
//    }
}
