package com.vgtech.vancloud.ui.module.announcement;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.NoticeInfo;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.fragment.NoticeNewFragment;
import com.vgtech.vancloud.ui.web.HelpOpenFileUtils;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vic on 2017/3/29.
 */
public class NoticeInfoActivity extends BaseActivity implements HttpListener<String> {
    private VancloudLoadingLayout mLoadingLayout;
    private View mContentView;
    private String mPlanSeq;
    private String mStaffNo;
    private String mCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.lable_notificationinfo));
        mContentView = findViewById(R.id.content_view);
        mLoadingLayout = (VancloudLoadingLayout) findViewById(R.id.ll_loadingview);
        mLoadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                loadInfo(mPlanSeq, mStaffNo, mCode);
            }
        });
        Intent intent = getIntent();
//        mPlanSeq = "1";
//        mStaffNo = "D0001";
//        mCode = "D0001133269702353321";
        mPlanSeq = intent.getStringExtra("Seq");
        mStaffNo = intent.getStringExtra("StaffNo");
        mCode = intent.getStringExtra("Code");
        loadInfo(mPlanSeq, mStaffNo, mCode);
    }

    private void loadInfo(String plan_seq, String staff_no, String code) {
        mLoadingLayout.showLoadingView(mContentView, "", true);
        Map<String, String> params = new HashMap<>();
        params.put("plan_seq", plan_seq);
        params.put("staff_no", staff_no);
        params.put("code", code);
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_MYNOTICE_DETAILS), params, this, true);
        getAppliction().getNetworkManager().load(1, path, this, true);
    }

    private void confirm(String plan_seq, String staff_no, String code) {
        showLoadingDialog(this, "");
        Map<String, String> params = new HashMap<>();
        params.put("plan_seq", plan_seq);
        params.put("staff_no", staff_no);
        params.put("code", code);
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_MYNOTICE_CONFIRM), params, this, true);
        getAppliction().getNetworkManager().load(2, path, this);
    }

    private void confirm(String mynotice_id) {
        showLoadingDialog(this, "");
        Map<String, String> params = new HashMap<>();
        params.put("mynotice_id", mynotice_id);
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_MYNOTICE_CONFIRM), params, this, true);
        getAppliction().getNetworkManager().load(3, path, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                new AlertDialog(this).builder().setTitle(getString(R.string.tip_notice_confirm))
                        .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                confirm(mPlanSeq, mStaffNo, mCode);
                            }
                        }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
                break;
            case R.id.notice_attachment:
                String url = (String) v.getTag();
                HelpOpenFileUtils.downloadFile(this,url);
//                final String forwardUrl = url;//VanTopUtils.generatorImageUrl(this, url);
//                String cachePath = FileCacheUtils.getCacheDir(this) + "/" + MD5.getMD5(forwardUrl) + "_" + mNoticeInfo.attachment_ext;
//                File f = new File(cachePath);
//                if (f.exists()) {
//                    Log.e("TAG_通知","exists");
//                    AttachUtils.openFile(this, f);
//                } else {
//                    new NetSilentAsyncTask<String>(this) {
//                        @Override
//                        protected void onSuccess(String filePath) throws Exception {
//
//                            Log.e("TAG_通知","成功="+filePath);
//
//                            if (Strings.isEmpty(filePath))
//                                return;
//                            AttachUtils.openFile(NoticeInfoActivity.this, new File(filePath));
//                        }
//
//                        @Override
//                        protected String doInBackground() throws Exception {
//                            Log.e("TAG_通知","doInBackground");
//
//                            return net().downloadAttach(forwardUrl, mNoticeInfo.attachment_ext, (Activity) context);
//                        }
//
//                        @Override
//                        protected void onThrowable(Throwable t) throws RuntimeException {
//                            Log.e("TAG_通知",""+t.toString());
//                            Toast.makeText(NoticeInfoActivity.this, getString(R.string.vancloud_resources_unavailable_prompt), Toast.LENGTH_SHORT).show();
//                            super.onThrowable(t);
//                            t.printStackTrace();
//                        }
//                    }.execute();
//                }

                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_noticeinfo;
    }

    private NetworkPath mLoadPath;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mLoadingLayout.dismiss(mContentView);
        dismisLoadingDialog();
        if (callbackId == 3)
            return;
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            if (callbackId == 1)
                mLoadingLayout.showErrorView(mContentView);
            return;
        }
        switch (callbackId) {
            case 1:
                try {
                    mLoadPath = path;
                    NoticeInfo noticeInfo = JsonDataFactory.getData(NoticeInfo.class, rootData.getJson().getJSONObject("data"));
                    initView(noticeInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    initRightTv(getString(R.string.confirm)).setVisibility(View.GONE);
                    if (mLoadPath != null)
                        getAppliction().getNetworkManager().getAcache().remove(mLoadPath.getPath());
                    String data = rootData.getJson().getString("data");
                    Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
                    int position = getIntent().getIntExtra("position", -1);
                    Intent intent = new Intent(NoticeNewFragment.NOTICE_CONFIRM);
                    intent.putExtra("mynotice_code", mCode);
                    intent.putExtra("position", position);
                    sendBroadcast(intent);
                    confirm(getIntent().getStringExtra("mynotice_id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private NoticeInfo mNoticeInfo;

    private void initView(NoticeInfo noticeInfo) {
        mNoticeInfo = noticeInfo;
        TextView notice_title = (TextView) findViewById(R.id.notice_title);
        TextView notice_timestamp = (TextView) findViewById(R.id.notice_timestamp);
        TextView notice_content = (TextView) findViewById(R.id.notice_content);
        TextView notice_attachment = (TextView) findViewById(R.id.notice_attachment);
        TextView notice_suretimestamp = (TextView) findViewById(R.id.notice_suretimestamp);
        notice_title.setText(Html.fromHtml(noticeInfo.subject));
        notice_timestamp.setText(getString(R.string.create) + "：" + Html.fromHtml(noticeInfo.mod_dates));
        notice_content.setText(Html.fromHtml(noticeInfo.content));
        if (!TextUtils.isEmpty(noticeInfo.attachment_ext)) {
            notice_attachment.setText(noticeInfo.attachment_ext);
            notice_attachment.setTag(noticeInfo.attachment);
            notice_attachment.setOnClickListener(this);
        } else {
            notice_attachment.setText("");
        }
        if (!TextUtils.isEmpty(noticeInfo.confirm_dates)) {
            notice_suretimestamp.setText(getString(R.string.confirm) + "：" + noticeInfo.confirm_dates);
        } else {
            notice_suretimestamp.setText("");
        }
        TextView rightTv = initRightTv(getString(R.string.confirm));
        if (noticeInfo.is_confim) {
            rightTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }
}
