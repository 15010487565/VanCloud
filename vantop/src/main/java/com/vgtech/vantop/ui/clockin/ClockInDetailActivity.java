package com.vgtech.vantop.ui.clockin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.ClockInAttStatusData;
import com.vgtech.vantop.moudle.ClockInDetailData;
import com.vgtech.vantop.moudle.ClockInListData;
import com.vgtech.vantop.moudle.VerticalTimeLineMoudle;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.ui.signedcard.SignedCardAddActivity;
import com.vgtech.vantop.ui.userinfo.VantopUserInfoActivity;
import com.vgtech.vantop.ui.view.VerticalTimeLine;
import com.vgtech.vantop.utils.PreferencesController;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考勤详情
 * Created by shilec on 2016/9/7.
 */
public class ClockInDetailActivity extends BaseActivity implements HttpListener<String> {

    public static final String EXTRAS_REQUEST_INFO = "requestInfo";
    private final int CALLBACK_INITDATA = 0X001;
    public static final String NORMAL_INTIME = "09:00";
    public static final String NORMAL_OUTTIME = "18:00";
    public static final String NORMAL_MIDTIMEOUT = "12:00";
    public static final String NORMAL_MIDTIMEBACK = "13:00";

    public static final int REQUEST_CODE_CLOKINAPPEAL = 0X001;
    private final String TAG = "ClockInDetailActivity";
//    private ClockInListData mRequestInfo;
    private ClockInDetailData mData;

    private SimpleDraweeView mIvHead;
    private TextView mTvName;
    private TextView mTvDate;
    private TextView mTvClsValue;
    private TextView mTvException;
    private TextView mTvStatus;
    private TextView mTvClokinAppeal;
    private TextView mTvSignedCardAppeal;
    private TextView mTvPunchTimes;
    private VerticalTimeLine mVtimeLine;

    @Override
    protected int getContentView() {
        return R.layout.activity_clockin_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initData();
    }

    private void initView() {

        mIvHead = (SimpleDraweeView) findViewById(R.id.iv_userphoto);
        mTvName = (TextView) findViewById(R.id.tv_name);
        mTvDate = (TextView) findViewById(R.id.tv_date);
        mTvClsValue = (TextView) findViewById(R.id.tv_calssvalue);
        mTvException = (TextView) findViewById(R.id.tv_exception);
        mTvStatus = (TextView) findViewById(R.id.tv_status);
        mTvClokinAppeal = (TextView) findViewById(R.id.tv_clockin_appeal);
        mTvSignedCardAppeal = (TextView) findViewById(R.id.tv_signedcard_appeal);
        mVtimeLine = (VerticalTimeLine) findViewById(R.id.vt_timeline);
        mTvPunchTimes = (TextView) findViewById(R.id.tv_timesrecord);
        mIvHead.setOnClickListener(this);
        setTitle(getString(R.string.vantop_clockin_detail));
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case CALLBACK_INITDATA: {
                parseInitData(rootData);
                showData();

            }
            break;
        }
    }

    private void showData() {

        if (!TextUtils.isEmpty(mData.date) && !TextUtils.isEmpty(mData.week)) {
            mTvDate.setText(mData.date + mData.week);
        }
        if (!TextUtils.isEmpty(mData.shiftName) && !"null".equals(mData.shiftName)) {
            mTvClsValue.setText(mData.shiftName);
        }
        if (!TextUtils.isEmpty(mData.exceptionExplain)) {
            mTvException.setText(mData.exceptionExplain);
        }
        //根据colorFlag设置字体显示颜色
        if (!mData.attStatus.isEmpty()) {
            String status = new String();
            for (int i = 0; i < mData.attStatus.size(); i++) {
                if (TextUtils.isEmpty(mData.attStatus.get(i).statusValue)) {
                    continue;
                }
                if (mData.attStatus.get(i).colorFlag == -1)
                    status += "<font color=\"red\">";
                status += mData.attStatus.get(i).statusValue;
                if (mData.attStatus.get(i).colorFlag == -1)
                    status += "</font>";
                if (i < mData.attStatus.size() - 1)
                    status += "<br/>";
            }
            mTvStatus.setText(Html.fromHtml(status));
        }

        if (mData.appealVisiable) {
            findViewById(R.id.layout_clockin_appeal).setVisibility(View.VISIBLE);
            mTvClokinAppeal.setOnClickListener(this);
        } else {
            findViewById(R.id.layout_clockin_appeal).setVisibility(View.GONE);
        }

        if (mData.signCardVisiable) {
            findViewById(R.id.layout_signedcard_appeal).setVisibility(View.VISIBLE);
            mTvSignedCardAppeal.setOnClickListener(this);
        } else {
            findViewById(R.id.layout_signedcard_appeal).setVisibility(View.GONE);
        }
        if (mData.attList != null && !mData.attList.isEmpty()) {
            String punchTimes = "";
            for (int i = 0; i < mData.attList.size(); i++) {
                punchTimes += mData.attList.get(i);
                if (i < mData.attList.size() - 1)
                    punchTimes += ",";
            }
            mTvPunchTimes.setText(punchTimes);
        }
        initVertimeLine();

        PreferencesController prf = new PreferencesController();
        prf.context = this;
        UserAccount account = prf.getAccount();
        mTvName.setText(account.user_name);

        String url = account.photo;//VanTopUtils.getImageUrl(this,PrfUtils.getStaff_no(this));
//        if(!TextUtils.isEmpty(url)) {
        ImageOptions.setUserImage(mIvHead, url);
//        }
    }

    /**
     * 初始化时间轴
     */
    private void initVertimeLine() {
        List<VerticalTimeLineMoudle> items = new ArrayList<>();

        VerticalTimeLineMoudle m;
        //上班
        m = new VerticalTimeLineMoudle();
        m.markLabel = getString(R.string.up);
        m.label = getString(R.string.in_time) + " " + mData.inTime;
        m.value = "(" + getString(R.string.in_time) + getString(R.string.vantop_time_) + NORMAL_INTIME + ")";
        m.isDecribeVisiable = true;
        m.decribe = getDescribe(true, NORMAL_INTIME, mData.inTime);
        m.decribeColor = getDecrColor(m.decribe);
        items.add(m);

        //如果为四次班则添加中段离开 和中断返回
        if (mData.timeNum == 4) {

            //中段外出
            m = new VerticalTimeLineMoudle();
            m.markLabel = getString(R.string.out);
            m.label = getString(R.string.out_time_mid) + " " + mData.outTimeMid;
            m.value = "(" + getString(R.string.out_time_mid) + NORMAL_MIDTIMEOUT + ")";
            m.isDecribeVisiable = true;
            m.decribe = getDescribe(false, NORMAL_MIDTIMEOUT, mData.outTimeMid);
            m.decribeColor = getDecrColor(m.decribe);
            items.add(m);

            //中段返回
            m = new VerticalTimeLineMoudle();
            m.markLabel = getString(R.string.back);
            m.label = getString(R.string.in_time_mid) + " " + mData.inTimeMid;
            m.value = "(" + getString(R.string.in_time_mid) + NORMAL_MIDTIMEBACK + ")";
            m.isDecribeVisiable = true;
            m.decribe = getDescribe(true, NORMAL_MIDTIMEBACK, mData.inTimeMid);
            m.decribeColor = getDecrColor(m.decribe);
            items.add(m);
        }
        //下班
        m = new VerticalTimeLineMoudle();
        m.markLabel = getString(R.string.down);
        m.label = getString(R.string.out_time) + " " + mData.outTime;
        m.value = "(" + getString(R.string.out_time) + getString(R.string.vantop_time_) + NORMAL_OUTTIME + ")";
        m.isDecribeVisiable = true;
        m.decribe = getDescribe(false, NORMAL_OUTTIME, mData.outTime);
        m.decribeColor = getDecrColor(m.decribe);
        items.add(m);

        mVtimeLine.addViews(items);
    }

    /***
     * 计算是否正常 迟到 缺勤
     *
     * @param isBack 是否是来公司
     * @param normal 标准上下班时间
     * @param actul  实际打卡时间
     * @return
     */
    private String getDescribe(boolean isBack, String normal, String actul) {

        String[] ns = normal.split(":");
        String[] as;
        //没有打卡记录则为缺勤
        if (TextUtils.isEmpty(actul)) {
            return getString(R.string.vantop_absence);
        } else {
            as = actul.split(":");
        }
        //如果小时没迟到则为 正常
        if (Integer.parseInt(ns[0]) > Integer.parseInt(as[0])) {
            return isBack ? getString(R.string.vantop_normal) : getString(R.string.vantop_leave_early);
            //小时相同
        } else if (Integer.parseInt(ns[0]) == Integer.parseInt(as[0])) {
            //比较分钟
            if (Integer.parseInt(ns[1]) > Integer.parseInt(as[1])) {
                return isBack ? getString(R.string.vantop_normal) : getString(R.string.vantop_leave_early);
            } else {
                return isBack ? getString(R.string.vantop_late) : getString(R.string.vantop_normal);
            }
        } else {
            return isBack ? getString(R.string.vantop_late) : getString(R.string.vantop_normal);
        }
    }

    /***
     * 获取考勤状态的颜色
     *
     * @param decr
     * @return
     */
    private int getDecrColor(String decr) {
        if (TextUtils.equals(decr, getString(R.string.vantop_normal))) {
            return VerticalTimeLine.COLOR_DESCR_BLUE;
        }
        if (TextUtils.equals(decr, getString(R.string.vantop_absence))) {
            return VerticalTimeLine.COLOR_DESCR_YELLOW;
        }
        //迟到早退为红色
        if (TextUtils.equals(decr, getString(R.string.vantop_late)) ||
                TextUtils.equals(decr, getString(R.string.vantop_leave_early))) {
            return VerticalTimeLine.COLOR_DESCR_RED;
        }
        return VerticalTimeLine.COLOR_DESCR_BLUE;
    }

    private void parseInitData(RootData rootData) {

        JSONObject jObj = rootData.getJson().optJSONObject("data");
        try {

            mData = JsonDataFactory.getData(ClockInDetailData.class, jObj);
            JSONArray jAttList = jObj.optJSONArray("attList");
            mData.attList = new ArrayList<>();
            for (int i = 0; i < jAttList.length(); i++) {
                mData.attList.add(jAttList.optString(i));
            }

            //attStatus
            mData.attStatus = new ArrayList<>();
            jAttList = jObj.optJSONArray("attStatus");
            for (int i = 0; i < jAttList.length(); i++) {

                ClockInAttStatusData data = JsonDataFactory.getData(ClockInAttStatusData.class,
                        jAttList.optJSONObject(i));
                mData.attStatus.add(data);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

        Log.i(TAG, "error" + error.getMessage());
    }

    @Override
    public void onResponse(String response) {
        Log.i(TAG, "response:" + response);
    }

    private void initData() {

        ClockInListData mRequestInfo = (ClockInListData) getIntent().getSerializableExtra(EXTRAS_REQUEST_INFO);
        String shiftCode = mRequestInfo.getShiftCode();
        if (TextUtils.isEmpty(shiftCode)) {
            return;
        }
        final Uri uri = Uri.parse(VanTopUtils.generatorUrl(this, UrlAddr.URL_CLOCKIN_DETAIL));
        /* .buildUpon()
                .appendQueryParameter("loginUserCode",mRequestInfo.staffNo)
                .appendQueryParameter("staffNo", mRequestInfo.staffNo)
                .appendQueryParameter("date", mRequestInfo.date)
                .appendQueryParameter("shiftCode", mRequestInfo.shiftCode).build();*/

        Map<String, String> params = new HashMap<>();
        params.put("staffNo", mRequestInfo.getStaffNo());
        params.put("date", mRequestInfo.getDate());
        params.put("shiftCode", shiftCode);
        params.put("loginUserCode", PrfUtils.getStaff_no(this));
        NetworkPath np = new NetworkPath(uri.toString(), params, this, true);
        getApplicationProxy().getNetworkManager().load(CALLBACK_INITDATA, np, this);
        showLoadingDialog(this, "", false);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v == mTvSignedCardAppeal) {
            Intent intent = new Intent(this, SignedCardAddActivity.class);
            intent.putExtra("date", mData.date);
            startActivity(intent);
        }

        if (v == mTvClokinAppeal) {
            showAppealDialog();
        }

        if (v == mIvHead) {
            Intent intent = new Intent(this, VantopUserInfoActivity.class);
            intent.putExtra(VantopUserInfoActivity.BUNDLE_STAFFNO, PrfUtils.getStaff_no(this));
            startActivity(intent);
        }
    }

    /**
     * 考勤申诉
     */
    private void showAppealDialog() {

       /* Uri uri = Uri.parse(VanTopUtils.generatorUrl(ClockInDetailActivity.this,
                UrlAddr.URL_CLOCKIN_APPEAL)).buildUpon()
                //.appendQueryParameter("isFixed", isFixed + "")
                //.appendQueryParameter("explain", explain)
                //.appendQueryParameter("fixedExplainKey", fixedKey)
                .appendQueryParameter("staffNo", mData.staffNo)
                .appendQueryParameter("date", mData.date)
                .appendQueryParameter("shiftCode", mData.shiftCode).build();*/

        //Toast.makeText(this, "dianji", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ClockinExceptionActivity.class);
        HashMap<String, String> map = new HashMap<>();
        map.put("staffNo", mData.staffNo);
        map.put("date", mData.date);
        map.put("shiftCode", mData.shiftCode);
        intent.putExtra(ClockinExceptionActivity.EXTRA_PARAMS, map);
        startActivityForResult(intent, REQUEST_CODE_CLOKINAPPEAL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        //考勤申诉成功后修改异常状态
        if (requestCode == REQUEST_CODE_CLOKINAPPEAL) {
            String exception = data.getStringExtra(ClockinExceptionActivity.EXTRA_RESAULT);
            //mTvException.setText(exception);
        }
        initData();
    }
}
