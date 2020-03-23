package com.vgtech.vantop.ui.punchcard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.CardItemAdapter;
import com.vgtech.vantop.moudle.PunchCardListData;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vic on 2017/4/27.
 * 打卡，一天全部记录
 */
public class CardInfoByDayActivity extends BaseActivity implements HttpListener<String> {

    private int mNextId;

    private ListView mListView;
    private VancloudLoadingLayout mLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.vantop_punchcard_record_info));
        Intent intent = getIntent();
        String sdate = intent.getStringExtra("date");
        if (TextUtils.isEmpty(sdate)) {
            long time = System.currentTimeMillis();
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdate = sdf.format(date);
        }
        TextView tv_date = (TextView) findViewById(R.id.tv_date);
        tv_date.setText(sdate + " " + DateTimeUtil.getWeekOfDate(this, sdate));
        mListView = (ListView) findViewById(R.id.list_view);
        mLoadingView = (VancloudLoadingLayout) findViewById(R.id.ll_loadingview);
        List<PunchCardListData> listDatas = intent.getParcelableArrayListExtra("cards");
        if (listDatas != null) {
            CardItemAdapter cardItemAdapter = new CardItemAdapter(this, listDatas);
            mListView.setAdapter(cardItemAdapter);
        } else {
            mNextId = 1;
            mLoadingView.showLoadingView(mListView, "", true);
            long time = System.currentTimeMillis();
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String mStartDate = sdf.format(date);
            String mEndDate = sdf.format(date);
            String path = VanTopUtils.generatorUrl(this, UrlAddr.URL_PUNCHCARD_LOADHISTORY);
            Uri uri = Uri.parse(path);
            Map<String, String> params = new HashMap<>();
            params.put("startDate", mStartDate);
            params.put("endDate", mEndDate);
            params.put("nextId", "" + mNextId);
            params.put("loginUserCode", PrfUtils.getStaff_no(this));
            NetworkPath np = new NetworkPath(uri.toString(), params, this, true);

            getApplicationProxy().getNetworkManager().load(1, np, this, true);
        }

    }

    @Override
    protected int getContentView() {
        return R.layout.activity_cardinfobyday;
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mLoadingView.dismiss(mListView);
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            mLoadingView.showErrorView(mListView);
            return;
        }
        switch (callbackId) {
            case 1: {
                String jsonData = rootData.getJson().toString();
                if (TextUtils.isEmpty(jsonData)) {
                    return;
                }
                List<PunchCardListData> list = PunchCardListData.fromJson(jsonData);
                CardItemAdapter cardItemAdapter = new CardItemAdapter(this, list);
                mListView.setAdapter(cardItemAdapter);
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
}
