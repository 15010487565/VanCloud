package com.vgtech.vancloud.ui.common.commentandpraise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.Comment;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseFragment;
import com.vgtech.vancloud.ui.adapter.CommentItemAdapter;
import com.vgtech.vancloud.ui.adapter.UserImageGridAdapter;
import com.vgtech.vancloud.ui.common.CountListener;
import com.vgtech.vancloud.ui.module.ReciverUserActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 回复（评论）和点赞列表
 * Created by Duke on 2015/8/17.
 */
public class ComPraiseFragment extends BaseFragment implements HttpListener<String>, View.OnClickListener {

    private static final int CALLBACK_PRAISE = 1;
    private static final int CALLBACK_COMMENT = 2;

    public static final String TYPE = "type";
    public static final String TYPEID = "typeid";
    public static final String POSITION = "position";

    private int type;
    private String typeid;
    private int position;

    public static ComPraiseFragment create(int type, String id, int position) {
        ComPraiseFragment fragment = new ComPraiseFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putString(TYPEID, id);
        args.putInt(POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }


    public ComPraiseFragment() {
    }

    public void refreshComment() {
        loadCommentsData();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt(TYPE);
            typeid = getArguments().getString(TYPEID);
            position = getArguments().getInt(POSITION);
        }
    }

    @Override
    protected int initLayoutId() {
        return R.layout.compraise;
    }

    private NoScrollListview mCommentListView;

    private NoScrollGridview mGridView;
    private View mPraiseView;
    private View mCommentView;
    private View mSpitLine;

    @Override
    protected void initView(View view) {
        mGridView = (NoScrollGridview) view.findViewById(R.id.praise_image);
        mCommentListView = (NoScrollListview) view.findViewById(R.id.comment_list);
        mCommentListView.setItemClick(true);
        mPraiseView = view.findViewById(R.id.praise_view);
        mSpitLine = view.findViewById(R.id.comment_line);
        mCommentView = view.findViewById(R.id.comment_view);
        mPraiseView.setOnClickListener(this);
    }

    private NetworkManager mNetworkManager;

    @Override
    protected void initData() {
        mNetworkManager = getApplication().getNetworkManager();
        loadPraiseData();
        loadCommentsData();
    }

    private void loadPraiseData() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("typeid", typeid);
        params.put("type", type + "");
        params.put("n", "200");
        params.put("s", "0");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_SUPPORT_PRAISELIST), params, getActivity());
        mNetworkManager.load(CALLBACK_PRAISE, path, this, true);
    }

    private void loadCommentsData() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("typeid", typeid);
        params.put("type", type + "");
        params.put("n", "200");
        params.put("s", "0");
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_SUPPORT_COMMENTLIST), params, getActivity());
        mNetworkManager.load(CALLBACK_COMMENT, path, this, true);
    }

    @Override
    protected void initEvent() {
    }

    private UserImageGridAdapter mUserAdapter;

    public boolean contains(NewUser user) {
        if (mUserAdapter == null)
            return false;
        return mUserAdapter.contains(user);
    }

    public List<NewUser> getPraiseList() {
        if (mUserAdapter == null)
            return null;
        return mUserAdapter.getList();
    }

    public List<Comment> getCommentList() {
        if (mCommentAdapter == null)
            return null;
        return mCommentAdapter.getList();
    }

    public void remove(NewUser user) {
        if (mUserAdapter == null)
            return;
        mUserAdapter.remove(user);
        if (mUserAdapter.isEmpty()) {
            mPraiseView.setVisibility(View.GONE);
            mSpitLine.setVisibility(View.GONE);
        } else {
            mSpitLine.setVisibility(View.VISIBLE);
            mPraiseView.setVisibility(View.VISIBLE);
        }

    }

    public void add(NewUser user) {
        if (mUserAdapter == null)
            return;
        mUserAdapter.add(user);
        if (mUserAdapter.isEmpty()) {
            mPraiseView.setVisibility(View.GONE);
            mSpitLine.setVisibility(View.GONE);
        } else {
            mSpitLine.setVisibility(View.VISIBLE);
            mPraiseView.setVisibility(View.VISIBLE);
        }
    }

    private CommentItemAdapter mCommentAdapter;

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_COMMENT:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    int count = resutObject.getInt("count");
                    if (getActivity() instanceof CountListener) {
                        CountListener listener = (CountListener) getActivity();
                        listener.commentsCount(count);
                    }
                    List<Comment> comments = JsonDataFactory.getDataArray(Comment.class, resutObject.getJSONArray("comments"));
                    mCommentAdapter = new CommentItemAdapter(getActivity(), comments);
                    mCommentAdapter.setPosition(position);
                    mCommentAdapter.setTypeId(typeid);
                    mCommentListView.setAdapter(mCommentAdapter);
                    if (comments.isEmpty()) {
                        mCommentView.setVisibility(View.GONE);
                    } else {
                        mCommentView.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CALLBACK_PRAISE:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    List<NewUser> users = JsonDataFactory.getDataArray(NewUser.class, resutObject.getJSONArray("users"));
                    mUserAdapter = new UserImageGridAdapter(mGridView, getActivity(), users);
                    mGridView.setAdapter(mUserAdapter);
                    if (users.isEmpty()) {
                        mPraiseView.setVisibility(View.GONE);
                        mSpitLine.setVisibility(View.GONE);
                    } else {
                        mSpitLine.setVisibility(View.VISIBLE);
                        mPraiseView.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.praise_view:
                String json = new Gson().toJson(mUserAdapter.getList());
                Intent intent = new Intent(getActivity(), ReciverUserActivity.class);
                intent.putExtra("title", getString(R.string.title_prise_list));
                intent.putExtra("json", json);
                startActivity(intent);
                break;
        }
    }
}
