package com.vgtech.vancloud.ui.common.commentandpraise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.adapter.PraiseAdapter;
import com.vgtech.vancloud.ui.common.CountListener;
import com.vgtech.vancloud.ui.fragment.ScrollAbleFragment;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 点赞列表
 * Created by Duke on 2015/8/17.
 */
public class PraiseListFragment extends ScrollAbleFragment implements HttpListener<String> {

    public static final String TYPE = "type";
    public static final String TYPEID = "typeid";
    private int type;
    private ListView noScrollListview;
    private static final int CALLBACK_PRAISE = 1;
    private int n = 200;
    private String nextId = "0";
    private NetworkManager mNetworkManager;
    private PraiseAdapter praiseAdapter;
    private String id;


    public static PraiseListFragment create(int type, String id) {
        PraiseListFragment fragment = new PraiseListFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putString(TYPEID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public PraiseListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getInt(TYPE);
        id = getArguments().getString(TYPEID);
    }


    @Override
    protected int initLayoutId() {
        return R.layout.praiselist_fragment_layout;
    }

    @Override
    protected void initView(View view) {

        WindowManager wm = getActivity().getWindowManager();
        int height = wm.getDefaultDisplay().getHeight();
        height = height - Utils.convertDipOrPx(getActivity(), 165);

  /*      View myView = view.findViewById(R.id.view_01);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) myView.getLayoutParams();
        layoutParams.height = height;
        myView.setLayoutParams(layoutParams);*/

        noScrollListview = (ListView) view.findViewById(R.id.praise_list);
        mFooterView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_layout, null);
        TextView tipTv = (TextView) mFooterView.findViewById(R.id.empty_tip);
        tipTv.setText(R.string.tip_no_praise);
        noScrollListview.addFooterView(mFooterView);
        noScrollListview.setFocusable(false);
        praiseAdapter = new PraiseAdapter(getActivity(), new ArrayList<NewUser>());
        noScrollListview.setAdapter(praiseAdapter);

        mNetworkManager = getApplication().getNetworkManager();
    }

    private CountListener mCountListener;

    public void setCountListener(CountListener countListener) {
        mCountListener = countListener;
    }

    public void refresh() {
        if (getActivity() != null)
            initDate(nextId, false);
    }

    @Override
    public void initData() {
        initDate(nextId, true);
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    private View mFooterView;

    private void initDate(String nextId, boolean show) {
        if (show)
            showEmptyProgress(mFooterView, true);
//        mNetworkManager = getApplication().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(getActivity()));
        params.put("tenantid", PrfUtils.getTenantId(getActivity()));
        params.put("typeid", id);
        params.put("type", type + "");
        params.put("n", n + "");
        params.put("s", nextId);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(getActivity(), URLAddr.URL_SUPPORT_PRAISELIST), params, getActivity());
        mNetworkManager.load(CALLBACK_PRAISE, path, this,true);
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
            case CALLBACK_PRAISE:


                List<NewUser> users = new ArrayList<NewUser>();
                try {
                    JSONObject jsonObject = rootData.getJson();
                    JSONObject resutObject = jsonObject.getJSONObject("data");
                    int count = resutObject.getInt("count");
                    if (getActivity() instanceof CountListener) {
                        CountListener listener = (CountListener) getActivity();
                        listener.praiseCount(count);
                    }

                    if (mCountListener != null) {
                        mCountListener.praiseCount(count);
                    }
//                    String id = resutObject.getString("nextid");
//                    if (!TextUtils.isEmpty("id") && !id.equals("0")) {
//                        nextId = id;
//                    }
                    users = JsonDataFactory.getDataArray(NewUser.class, resutObject.getJSONArray("users"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (praiseAdapter == null) {
                    praiseAdapter = new PraiseAdapter(getActivity(), users);
                    noScrollListview.setAdapter(praiseAdapter);
                } else {
                    praiseAdapter.myNotifyDataSetChanged(users);
                }
                if (praiseAdapter.getCount() == 0) {
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
