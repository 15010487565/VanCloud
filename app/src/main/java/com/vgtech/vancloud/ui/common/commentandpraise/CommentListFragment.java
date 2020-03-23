package com.vgtech.vancloud.ui.common.commentandpraise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.Comment;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.adapter.CommentAdapter;
import com.vgtech.vancloud.ui.common.CountListener;
import com.vgtech.vancloud.ui.fragment.ScrollAbleFragment;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 回复（评论）列表
 * Created by Duke on 2015/8/17.
 */
public class CommentListFragment extends ScrollAbleFragment implements HttpListener<String> {

    private static final int CALLBACK_COMMENT = 1;
    ListView noScrollListview;
    public static final String TYPE = "type";
    public static final String TYPEID = "typeid";

    private int type;
    private String id;
    private NetworkManager mNetworkManager;
    private int n = 200;
    private String nextId = "0";
    private CommentAdapter adapter;


    public static CommentListFragment create(int type, String id) {
        CommentListFragment fragment = new CommentListFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putString(TYPEID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public CommentListFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getInt(TYPE);
        id = getArguments().getString(TYPEID);
    }

    @Override
    protected int initLayoutId() {
        return R.layout.commentlist_fragment_layout;
    }

    private View mFooterView;

    @Override
    protected void initView(View view) {


//        WindowManager wm = getActivity().getWindowManager();
//        int height = wm.getDefaultDisplay().getHeight();
//        height = height - Utils.convertDipOrPx(getActivity(), 165);

      /*  View myView = view.findViewById(R.id.view_01);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) myView.getLayoutParams();
        layoutParams.height = height;
        myView.setLayoutParams(layoutParams);*/

        noScrollListview = (ListView) view.findViewById(R.id.comment_list);
//        noScrollListview.setFocusable(false);
        mFooterView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_layout, null);
        noScrollListview.addFooterView(mFooterView);
        mNetworkManager = getApplication().getNetworkManager();
        adapter = new CommentAdapter(getActivity(), new ArrayList<Comment>());
        noScrollListview.setAdapter(adapter);
    }

    private CountListener mCountListner;

    public void setCountListener(CountListener countListener) {
        mCountListner = countListener;
    }

    public void refresh() {
        nextId = "0";
        if (getActivity() != null)
            initDate(nextId, false);
    }

    @Override
    public void initData() {

//        new Handler().postDelayed(new Runnable() {
//            public void run() {
//                initDate(nextId);
//            }
//        }, 100);
        initDate(nextId, true);
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }


    private void initDate(String nextId, boolean init) {
        if (init)
            showEmptyProgress(mFooterView, true);
//        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("typeid", id);
        params.put("type", type + "");
        params.put("n", n + "");
        params.put("s", nextId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_SUPPORT_COMMENTLIST), params, getActivity());
        mNetworkManager.load(CALLBACK_COMMENT, path, this,true);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        if (mFooterView != null)
            mFooterView.setVisibility(View.GONE);
        boolean safe = ActivityUtils.prehandleNetworkData(getActivity(), this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_COMMENT:
                List<Comment> comments = new ArrayList<Comment>();
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    int count = resutObject.getInt("count");
                    if (getActivity() instanceof CountListener) {
                        CountListener listener = (CountListener) getActivity();
                        listener.commentsCount(count);
                    }
                    if (mCountListner != null) {
                        mCountListner.commentsCount(count);
                    }
                    comments = JsonDataFactory.getDataArray(Comment.class, resutObject.getJSONArray("comments"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (adapter == null) {
                    adapter = new CommentAdapter(getActivity(), comments);
                    noScrollListview.setAdapter(adapter);
                } else {
                    adapter.myNotifyDataSetChanged(comments);
                }
                if (adapter.getCount() == 0) {
                    showEmptyProgress(mFooterView, false);
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
    public void onDestroy() {
        super.onDestroy();
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
    }

    @Override
    public ListView getScrollableView() {
        return noScrollListview;
    }
}
