package com.vgtech.vancloud.ui.search;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.inject.Inject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.Organization;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.SearchItem;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.MessageDB;
import com.vgtech.common.provider.db.User;
import com.vgtech.common.utils.StringUtils;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.adapter.SearchAdapter;
import com.vgtech.vancloud.ui.chat.controllers.AvatarController;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.chat.models.ChatMessage;
import com.vgtech.vancloud.ui.fragment.BaseSwipeBackFragment;
import com.vgtech.vancloud.utils.VgTextUtils;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.fragment.RoboFragment;

/**
 * Created by vic on 2016/9/7.
 */
public class SearchFragment extends BaseSwipeBackFragment implements HttpListener, View.OnTouchListener, AbsListView.OnScrollListener {
    private static final int CALLBACK_TYPE_USER = 1;
    private static final int CALLBACK_TYPE_NNT = 2;
    private SearchAdapter<SearchItem> mSearchAdapter;
    @Inject
    public Controller controller;
    @Inject
    AvatarController avatarController;
    private TextView mSearchTv;
    private SearchItem.Type mSearchType;
    private String mKeyword;
    private LinearLayout searchLayout;

    private View mWaitView;
    private boolean mIsVantop;
    private View mSearchTip;
    private View mSearchEmpty;
    private TextView mSearchEmptyTv;
    private ListView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_search, null);
        mSearchTip = view.findViewById(R.id.search_tip);
        mSearchEmpty = view.findViewById(R.id.tip_search_empty);
        mSearchEmptyTv = (TextView) view.findViewById(R.id.tip_search_empty_tv);
        String[] tips = getResources().getStringArray(R.array.search_tip);
        ((TextView) view.findViewById(R.id.st1)).setText(tips[0]);
        ((TextView) view.findViewById(R.id.st2)).setText(tips[1]);
        ((TextView) view.findViewById(R.id.st3)).setText(tips[2]);
        ((TextView) view.findViewById(R.id.st4)).setText(tips[3]);
        ((TextView) view.findViewById(R.id.st5)).setText(tips[4]);
        ((TextView) view.findViewById(R.id.st6)).setText(tips[5]);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        view.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                mInputMethodManager.hideSoftInputFromWindow(
                        mSearchTv.getWindowToken(), 0);
                getActivity().onBackPressed();
            }
        });
        mIsVantop = TenantPresenter.isVanTop(getActivity());
        mSearchAdapter = new SearchAdapter<>(this, controller, avatarController);
        mListView = (ListView) view.findViewById(android.R.id.list);
        searchLayout = (LinearLayout) view.findViewById(R.id.search_layout);
        searchLayout.setOnClickListener(new View.OnClickListener() {//拦截QuickMenuActionDialog弹窗
            @Override
            public void onClick(View v) {
            }
        });
        if (mIsVantop) {
            mWaitView = inflater.inflate(R.layout.progress_black, null);
            mWaitView.setVisibility(View.GONE);
            mListView.addFooterView(mWaitView);
        }
        mListView.setAdapter(mSearchAdapter);
        mListView.setOnTouchListener(this);
        final SearchView searchView = (SearchView) view.findViewById(R.id.searchview);
        int id = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        int closeId = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        final ImageView closeView = (ImageView) searchView.findViewById(closeId);
        closeView.setBackgroundResource(R.drawable.btn_actionbar);
        final TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.WHITE);
        textView.setHintTextColor(Color.parseColor("#CCFFFFFF"));
        searchView.setIconified(false);
        mSearchTv = textView;
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                InputMethodManager mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                mInputMethodManager.hideSoftInputFromWindow(
                        mSearchTv.getWindowToken(), 0);
                getActivity().onBackPressed();
                return true;
            }
        });
        Bundle bundle = getArguments();
        if (bundle != null) {
            mSearchTip.setVisibility(View.GONE);
            String searchType = bundle.getString("searchType");
            mKeyword = bundle.getString("keyword");
            mSearchType = SearchItem.Type.valueOf(searchType);
            if (mSearchType == SearchItem.Type.mynotice) {
                mListView.setOnScrollListener(this);
            }
            searchView.setQueryHint(SearchItem.getTypeTitle(getActivity(), mSearchType));
            searchView.clearFocus();
        }
        if (!TextUtils.isEmpty(mKeyword)) {
            limit = -1;
            mSearchTv.setText(mKeyword);
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    search(mKeyword);
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String s) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String s) {
                            search(s);
                            return false;
                        }
                    });
                }
            }, 300);

        } else {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    search(s);
                    return false;
                }
            });
        }
        return attachToSwipeBack(view);
    }

    private void search(String s) {
        mSearchAdapter.clear();
        if (!TextUtils.isEmpty(s)) {
            s = StringUtils.sqlValidate(s);
            mSearchTip.setVisibility(View.GONE);
            mSearchEmpty.setVisibility(View.GONE);
            if (mSearchType != null) {
                switch (mSearchType) {
                    case notice:
                        mNntS = "0";
                        mNntN = "12";
                        searchNNTMessage(s, 2);
                        break;
                    case mynotice:
                        mNntS = "0";
                        mNntN = "12";
                        searchNNTMessage(s, 1);
                        break;
                    case todo:
                        mNntS = "0";
                        mNntN = "12";
                        searchNNTMessage(s, 3);
                        break;
                    case user:
                        searchUser(s);//搜索联系人
                        break;
                    case chatgroup:
                        searchChatGroup(s);//搜索聊天群组
                        break;
                    case chatmessage:
                        searchChatMessage(s);//搜索聊天记录
                        break;
                    case pushmessage:
                        searchPushMessage(s);//搜索消息提醒
                        break;
                }
            } else {
                mNntS = "0";
                mNntN = "4";
                searchUser(s);//搜索联系人

            }

        } else {
            if (mSearchType == null)
                mSearchTip.setVisibility(View.VISIBLE);
            mSearchEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(
                mSearchTv.getWindowToken(), 0);
    }

    private int limit = 3;
    private List<User> mAllUser;

    private void searchUser(String s) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("loginUserCode", PrfUtils.getStaff_no(getActivity()));
        params.put("q", s);
        mWaitView.setVisibility(View.VISIBLE);
        String url = VanTopUtils.generatorUrl(getActivity(), UrlAddr.URL_ORGS_SEARCH);
        final NetworkPath path = new NetworkPath(url, params, getActivity(), true);
        final VanCloudApplication vanCloudApplication = (VanCloudApplication) getActivity().getApplication();
        vanCloudApplication.getNetworkManager().cancle(this);
        vanCloudApplication.getNetworkManager().load(CALLBACK_TYPE_USER, path, SearchFragment.this);
    }

    private static final String START_INDEX = "startIndex";
    private static final String KEYWORD = "keyword";
    private String mNntS = "0";
    private String mNntN = "3";
    private Uri mLastUri;

    /**
     * @param s
     * @param type 0全部，1通知，2公告，3待办
     */
    private void searchNNTMessage(String s, int type) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(getActivity()));
        params.put("tenant_id", PrfUtils.getTenantId(getActivity()));
        params.put("key", s);
        params.put("type", "" + type);
        params.put("s", mNntS);
        params.put("n", mNntN);
        mWaitView.setVisibility(View.VISIBLE);
        String url = ApiUtils.generatorUrl(getActivity(), URLAddr.URL_INDEX_SEARCH);
        mLastUri = Uri.parse(url).buildUpon().appendQueryParameter(START_INDEX, mNntS).appendQueryParameter(KEYWORD, s).build();
        final NetworkPath path = new NetworkPath(mLastUri.toString(), params, getActivity());
        final VanCloudApplication vanCloudApplication = (VanCloudApplication) getActivity().getApplication();
        vanCloudApplication.getNetworkManager().cancle(mNntCallback);
        vanCloudApplication.getNetworkManager().load(CALLBACK_TYPE_NNT, path, mNntCallback);
    }

    private List<ChatGroup> mAllChatGroup;

    private void searchChatGroup(String s) {
        if (mAllChatGroup == null) {
            mAllChatGroup = ChatGroup.findAllGroup(PrfUtils.getUserId(getActivity()), PrfUtils.getTenantId(getActivity()));
        }
        List<SearchItem> tmpList = new ArrayList<SearchItem>();
        for (ChatGroup chatGroup : mAllChatGroup) {
            if (limit != -1 && tmpList.size() > limit)
                break;
            String name = chatGroup.getDisplayNick();
            if (!TextUtils.isEmpty(name) && name.contains(s)) {
                name = name.replace(s, "<font color='#3ab5ff'>" + s + "</font>");
                SearchItem searchItem = new SearchItem();
                searchItem.id = s;
                searchItem.type = SearchItem.Type.chatgroup;
                searchItem.title = name;
                searchItem.subtitle = "";
                searchItem.icon = chatGroup.avatar;
                searchItem.itemObj = chatGroup;
                tmpList.add(searchItem);
            } else {
                String nick = chatGroup.nick;
                if (!TextUtils.isEmpty(nick)) {
                    List<String> nicks = new ArrayList<>();
                    String[] nickArray = nick.split(",");
                    for (int i = 0; i < nickArray.length; i++) {
                        String nickName = nickArray[i];
                        if (nickName.contains(s)) {
                            nickName = nickName.replace(s, "<font color='#3ab5ff'>" + s + "</font>");
                            nicks.add(nickName);
                        }
                    }
                    if (!nicks.isEmpty()) {
                        StringBuilder stringBuilder = new StringBuilder(getString(R.string.lable_contains));
                        for (int i = 0; i < nicks.size(); i++) {
                            stringBuilder.append(nicks.get(i)).append(",");
                        }
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                        SearchItem searchItem = new SearchItem();
                        searchItem.id = s;
                        searchItem.type = SearchItem.Type.chatgroup;
                        searchItem.title = name;
                        searchItem.subtitle = stringBuilder.toString();
                        searchItem.icon = chatGroup.avatar;
                        searchItem.itemObj = chatGroup;
                        tmpList.add(searchItem);
                    }
                }
            }
        }
        List<SearchItem> items = checkItemList(tmpList);
        mSearchAdapter.add(items);
    }

    private void searchChatMessage(String s) {
        List<ChatMessage> chatMessages = ChatGroup.searchAllMessageByContent(PrfUtils.getUserId(getActivity()), PrfUtils.getTenantId(getActivity()), s);
        Map<String, List<ChatMessage>> chatGroupMap = new HashMap<String, List<ChatMessage>>();
        for (int i = 0; i < chatMessages.size(); i++) {
            ChatMessage cm = chatMessages.get(i);
            ChatGroup chatGroup = cm.group;
            List<ChatMessage> contentList = chatGroupMap.get(chatGroup.name);
            if (contentList == null) {
                contentList = new ArrayList<>();
            }
            contentList.add(cm);
            chatGroupMap.put(chatGroup.name, contentList);
        }
        List<SearchItem> tmpList = new ArrayList<SearchItem>();
        for (Map.Entry<String, List<ChatMessage>> entry : chatGroupMap.entrySet()) {
            if (limit != -1 && tmpList.size() > limit)
                break;
            //  String cg = entry.getKey();
            List<ChatMessage> cmlist = entry.getValue();
            SearchItem searchItem = new SearchItem();
            searchItem.id = s;
            searchItem.type = SearchItem.Type.chatmessage;
            searchItem.title = cmlist.get(cmlist.size() - 1).group.getDisplayNick();
            String subtitle = "";
            if (cmlist.size() > 1) {
                subtitle = getString(R.string.search_chatmessage_count, cmlist.size());
            } else {
                String content = cmlist.get(0).content;
                content = searchSubString(s, content);
                subtitle = content.replace(s, "<font color='#3ab5ff'>" + s + "</font>");
            }
            searchItem.subtitle = subtitle;
            searchItem.icon = cmlist.get(cmlist.size() - 1).group.avatar;
            if (TextUtils.isEmpty(searchItem.icon))
                searchItem.icon = "";
            searchItem.itemObj = cmlist;
            tmpList.add(searchItem);
        }
        List<SearchItem> items = checkItemList(tmpList);
        mSearchAdapter.add(items);
    }

    @Override
    public void onDestroyView() {
        VanCloudApplication vanCloudApplication = (VanCloudApplication) getActivity().getApplication();
        vanCloudApplication.getNetworkManager().cancle(this);
        super.onDestroyView();
    }

    private void searchPushMessage(String s) {
        List<SearchItem> tmpList = new ArrayList<SearchItem>();
        List<MessageDB> messageDBs = MessageDB.queryMessageByKeyword(getActivity(), s);
        for (MessageDB msg : messageDBs) {
            if (limit != -1 && tmpList.size() > limit)
                break;
            String name = msg.title;
            if (!TextUtils.isEmpty(name) && name.contains(s)) {
                name = name.replace(s, "<font color='#3ab5ff'>" + s + "</font>");
                SearchItem searchItem = new SearchItem();
                searchItem.id = s;
                searchItem.type = SearchItem.Type.pushmessage;
                searchItem.title = name;
                searchItem.subtitle = "";
                searchItem.icon = "";
                searchItem.itemObj = msg;
                tmpList.add(searchItem);
            }
        }
        List<SearchItem> items = checkItemList(tmpList);
        mSearchAdapter.add(items);
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

    public List<SearchItem> checkItemList(List<SearchItem> items) {
        if (!items.isEmpty()) {
            items.get(0).isFirst = true;
            if (limit != -1 && items.size() > limit) {
                SearchItem item = items.get(limit - 1);
                item.hasMore = true;
                item.isLast = true;
                items = items.subList(0, limit);
            } else {
                SearchItem item = items.get(items.size() - 1);
                item.isLast = true;
            }
        }
        return items;
    }

    private HttpListener<String> mNntCallback = new HttpListener<String>() {
        @Override
        public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
            mWaitView.setVisibility(View.GONE);
            boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
            if (!safe) {
                return;
            }
            switch (callbackId) {
                case CALLBACK_TYPE_NNT: {
                    JSONObject jsonObject = rootData.getJson();
                    try {
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < dataArray.length(); i++) {
                            List<SearchItem> tmpList = new ArrayList<SearchItem>();
                            JSONObject itemObject = dataArray.getJSONObject(i);
                            String type = itemObject.getString("type");
                            JSONArray rowArray = itemObject.getJSONArray("rows");
                            if ("notice".equals(type)) {
                                for (int r = 0; r < rowArray.length(); r++) {
                                    if (limit != -1 && tmpList.size() > limit)
                                        break;
                                    JSONObject rowObject = rowArray.getJSONObject(r);
                                    String title = rowObject.getString("title");
                                    String publishTime = rowObject.getString("publishTime");
                                    String s = path.getPostValues().get("key");
                                    SearchItem searchItem = new SearchItem();
                                    searchItem.id = s;
                                    searchItem.timestamp = publishTime;
                                    searchItem.subtitle = "";
                                    searchItem.type = SearchItem.Type.notice;
                                    searchItem.title = title;
                                    searchItem.itemObj = rowObject;
                                    tmpList.add(searchItem);
                                }
                            } else if ("mynotice".equals(type)) {
                                mNntS = itemObject.getString("nextid");
                                for (int r = 0; r < rowArray.length(); r++) {
                                    if (limit != -1 && tmpList.size() > limit)
                                        break;
                                    JSONObject rowObject = rowArray.getJSONObject(r);
                                    String title = rowObject.getString("mynotice_title");
                                    String publishTime = rowObject.getString("create_time");
                                    String s = path.getPostValues().get("key");
                                    SearchItem searchItem = new SearchItem();
                                    searchItem.id = s;
                                    searchItem.timestamp = publishTime;
                                    searchItem.subtitle = "";
                                    searchItem.type = SearchItem.Type.mynotice;
                                    searchItem.title = title;
                                    searchItem.itemObj = rowObject;
                                    tmpList.add(searchItem);
                                }
                            } else if ("todo".equals(type)) {
                                for (int r = 0; r < rowArray.length(); r++) {
                                    if (limit != -1 && tmpList.size() > limit)
                                        break;
                                    JSONObject rowObject = rowArray.getJSONObject(r);
                                    String title = rowObject.getString("todoContent");
                                    String publishTime = rowObject.getString("createDate");
                                    String s = path.getPostValues().get("key");
                                    SearchItem searchItem = new SearchItem();
                                    searchItem.id = s;
                                    searchItem.timestamp = publishTime;
                                    searchItem.subtitle = "";
                                    searchItem.type = SearchItem.Type.todo;
                                    searchItem.title = title;
                                    searchItem.itemObj = rowObject;
                                    tmpList.add(searchItem);
                                }
                            }
                            List<SearchItem> items = checkItemList(tmpList);
                            mSearchAdapter.add(items);
                            if (mSearchAdapter.isEmpty()) {
                                String s = path.getPostValues().get("key");
                                String tip = getString(R.string.tip_search_empty, s);
                                tip = tip.replace(s, "<font color='#3ab5ff'>" + s + "</font>");
                                mSearchEmptyTv.setText(Html.fromHtml(tip));
                                mSearchEmpty.setVisibility(View.VISIBLE);
                            } else {
                                mSearchEmpty.setVisibility(View.GONE);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {

        }

        @Override
        public void onResponse(String response) {

        }
    };

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        boolean safe = VanTopActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            mWaitView.setVisibility(View.GONE);
            return;
        }
        switch (callbackId) {
            case CALLBACK_TYPE_USER: {
                String s = path.getPostValues().get("q");
                JSONObject jsonObject = rootData.getJson();
                try {
                    if (jsonObject.has("staffs") && jsonObject.get("staffs") instanceof JSONArray) {
                        JSONArray nodeArray = jsonObject.getJSONArray("staffs");
                        List<Organization> list = JsonDataFactory.getDataArray(Organization.class, nodeArray);
                        List<SearchItem> tmpList = new ArrayList<SearchItem>();
                        for (Organization organization : list) {
                            if (limit != -1 && tmpList.size() > limit)
                                break;
                            String name = organization.staff_name;
                            if (!TextUtils.isEmpty(name) && name.contains(s)) {
                                name = name.replace(s, "<font color='#3ab5ff'>" + s + "</font>");
                                SearchItem searchItem = new SearchItem();
                                searchItem.id = s;
                                searchItem.type = SearchItem.Type.user;
                                searchItem.title = name;
                                searchItem.subtitle = organization.pos;
                                searchItem.icon = organization.photo;
                                searchItem.itemObj = organization;
                                tmpList.add(searchItem);
                            }
                        }
                        List<SearchItem> items = checkItemList(tmpList);
                        mSearchAdapter.add(items);
                    }
                    searchChatGroup(s);//搜索聊天群组
                    searchChatMessage(s);//搜索聊天记录
                    searchPushMessage(s);//搜索消息提醒
                    searchNNTMessage(s, 0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(Object response) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(mSearchTv.getWindowToken(), 0);
        }
        return false;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (totalItemCount - 2 > view.getLastVisiblePosition() && mLastUri != null && !mLastUri.getQueryParameter(START_INDEX).equals(mNntS) && !"0".equals(mNntS)) {
            String keyword = mLastUri.getQueryParameter(KEYWORD);
            searchNNTMessage(keyword, 0);
        }
    }
}
