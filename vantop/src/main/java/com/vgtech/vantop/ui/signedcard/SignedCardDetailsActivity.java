package com.vgtech.vantop.ui.signedcard;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.ApprovalAdapter;
import com.vgtech.vantop.moudle.Approval;
import com.vgtech.vantop.moudle.SignedCardItemData;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.ui.userinfo.VantopUserInfoActivity;
import com.vgtech.vantop.utils.PreferencesController;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by shilec on 2016/7/19.
 */
public class SignedCardDetailsActivity extends BaseActivity implements HttpListener {

    TextView rightTxt;
    ImageView statusImg;
    SimpleDraweeView staffImg;
    TextView staffNoTxt;
    TextView staffNameTxt;
    TextView cardNumberTxt;
    TextView dataTxt;
    TextView terminalTxt;
    TextView reasonTxt;
    TextView remarkTxt;
    TextView tvStatus;
    ListView approvalList;
    private VancloudLoadingLayout loadingLayout;
    private ScrollView scrollview;
    private String taskId;
    private final int CALLBACK_LOADDATA = 0X001;
    private final int CALLBACK_OPERATION = 0X002;
    private int position;
    private SimpleDraweeView mSdvAttachmentPic;
    private LinearLayout mLLAttachmentPicContain;

    @Override
    protected int getContentView() {
        return R.layout.signedcard_details_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intiView();
        initData();
    }

    private void intiView() {
        setTitle(getString(R.string.signcard_detail));
        statusImg = (ImageView) findViewById(R.id.status_img);

        tvStatus = (TextView) findViewById(R.id.tv_status);

        staffImg = (SimpleDraweeView) findViewById(R.id.staff_img);
        staffNoTxt = (TextView) findViewById(R.id.staff_no_txt);
        staffNameTxt = (TextView) findViewById(R.id.staff_name_txt);
        cardNumberTxt = (TextView) findViewById(R.id.card_number_txt);
        dataTxt = (TextView) findViewById(R.id.data_txt);
        terminalTxt = (TextView) findViewById(R.id.terminal_txt);
        reasonTxt = (TextView) findViewById(R.id.reason_txt);
        mSdvAttachmentPic = (SimpleDraweeView) findViewById(R.id.signedcard_details_iv_pic);
        mLLAttachmentPicContain = (LinearLayout) findViewById(R.id.signedcard_details_iv_pic_container);
        remarkTxt = (TextView) findViewById(R.id.remark_txt);
        approvalList = (ListView) findViewById(R.id.approval_list);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading_layout);
        scrollview = (ScrollView) findViewById(R.id.scrollview);
        approvalList.setFocusable(false);
        staffImg.setOnClickListener(this);
        mSdvAttachmentPic.setOnClickListener(this);

        PreferencesController prf = new PreferencesController();
        prf.context = this;
        UserAccount account = prf.getAccount();
        ImageOptions.setUserImage(staffImg, account.photo);

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initData();
            }
        });

    }

    private void initData() {
        taskId = getIntent().getStringExtra("taskId");
        position = getIntent().getIntExtra("position", -1);
        loadData();
    }

    private void loadData() {
        loadingLayout.showLoadingView(scrollview, "", true);
        String path = VanTopUtils.generatorUrl(this, UrlAddr.URL_SIGNEDCARD_DETAILS);
        Uri uri = Uri.parse(path);
        Map<String, String> params = new HashMap<>();
        //POST请求params不必须不能为空
        params.put("taskId", taskId);
        NetworkPath np = new NetworkPath(uri.toString(), params, this, true);
        getApplicationProxy().getNetworkManager().load(CALLBACK_LOADDATA, np, this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (view == rightTxt) {
            showLoadingDialog(this, "");
            String path = VanTopUtils.generatorUrl(this, UrlAddr.URL_SIGNEDCARD_DESTROY);
            Uri uri = Uri.parse(path);
            Map<String, String> params = new HashMap<>();
            params.put("taskId", taskId);
            NetworkPath np = new NetworkPath(uri.toString(), params, this, true);
            getApplicationProxy().getNetworkManager().load(CALLBACK_OPERATION, np, this);
        } else if (view == staffImg) {
            Intent intent = new Intent(this, VantopUserInfoActivity.class);
            intent.putExtra(VantopUserInfoActivity.BUNDLE_STAFFNO, PrfUtils.getStaff_no(this));
            startActivity(intent);
        } else if (view == mSdvAttachmentPic) {
            try {
                Intent intent = new Intent(this, Class.forName("com.vgtech.vancloud.ui.common.image.ImageCheckActivity"));
                String picUrl = (String) view.getTag();
                List<ImageInfo> imgInfos = new ArrayList<>();
                imgInfos.add(new ImageInfo(picUrl, picUrl));
                String json = new Gson().toJson(imgInfos);
                intent.putExtra("listjson", json);
                intent.putExtra("position", 0);
                intent.putExtra("numVisible", false);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        loadingLayout.dismiss(scrollview);
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            if (callbackId == CALLBACK_LOADDATA) {
                try {
                    if (rootData.getJson() == null)
                        loadingLayout.showErrorView(scrollview);
                    else {
                        String code = rootData.getJson().getString("_code");
                        if ("-1".equals(code)) {
                            loadingLayout.showEmptyView(scrollview, getString(R.string.vantop_cancel_prompt), true, true);
                        } else {
                            loadingLayout.showErrorView(scrollview);
                        }
                    }
                } catch (Exception e) {
                    loadingLayout.showErrorView(scrollview);
                    e.printStackTrace();
                }
            }
            return;
        }
        switch (callbackId) {
            case CALLBACK_LOADDATA: {
                try {
                    JSONObject jo = rootData.getJson().getJSONObject("data");
                    SignedCardItemData signedInfo = JsonDataFactory.getData(SignedCardItemData.class, jo);
                    inflateData(signedInfo);
                    List<Approval> approvals = JsonDataFactory.getDataArray(Approval.class, jo.getJSONArray("approval"));
                    if (approvals != null && approvals.size() > 0)
                        approvalList.setAdapter(new ApprovalAdapter(this, approvals));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case CALLBACK_OPERATION: {
                Intent intent = new Intent();
                intent.putExtra("position", position);
                setResult(RESULT_OK, intent);
                finish();
            }
            break;
        }
    }

    private void inflateData(SignedCardItemData signedInfo) {
        setStatusImg(signedInfo);
        staffNoTxt.setText(signedInfo.staffNo);
        staffNameTxt.setText(signedInfo.staffName);
        cardNumberTxt.setText(signedInfo.cardNo);
        dataTxt.setText(signedInfo.date + " " + signedInfo.time);
        terminalTxt.setText(signedInfo.termNo);
        reasonTxt.setText(signedInfo.reason);
        remarkTxt.setText(signedInfo.remark);
        if (TextUtils.isEmpty(signedInfo.picurl)) {
            if (mLLAttachmentPicContain.getVisibility() != View.GONE)
                mLLAttachmentPicContain.setVisibility(View.GONE);
        } else {
            if (mLLAttachmentPicContain.getVisibility() != View.VISIBLE)
                mLLAttachmentPicContain.setVisibility(View.VISIBLE);
            ImageOptions.setImage(mSdvAttachmentPic, signedInfo.picurl);
            mSdvAttachmentPic.setTag(signedInfo.picurl);
        }
    }


    private void setStatusImg(SignedCardItemData signedInfo) {
        String status = "";
        if (signedInfo.status != null)
            status = signedInfo.status;

        String language = PrfUtils.getAppLanguage(this);

        if ("zh".equals(language)) {
            if ("0".equals(status)) {        //审批中
                rightTxt = initRightTv(getResources().getString(R.string.vantop_undo));
                statusImg.setImageResource(R.mipmap.approvaling_img);
                tvStatus.setText(getResources().getString(R.string.vantop_approving));
                tvStatus.setTextColor(getResources().getColor(R.color.txt_explain));
            } else if ("1".equals(status)) {  //同意
                statusImg.setImageResource(R.mipmap.approval_adopted);
                tvStatus.setText(getResources().getString(R.string.vantop_adopt));
                tvStatus.setTextColor(getResources().getColor(R.color.agree));
            } else if ("2".equals(status)) {  //拒绝
                rightTxt = initRightTv(getResources().getString(R.string.delete));
                statusImg.setImageResource(R.mipmap.approval_refused);
                tvStatus.setText(getResources().getString(R.string.vantop_refuse));
                tvStatus.setTextColor(getResources().getColor(R.color.refuse));
            }
        } else {
            if ("0".equals(status)) {        //审批中
                rightTxt = initRightTv(getResources().getString(R.string.vantop_undo));
                statusImg.setImageResource(R.mipmap.approvaling_img_en);
                tvStatus.setText(getResources().getString(R.string.vantop_approving));
                tvStatus.setTextColor(getResources().getColor(R.color.txt_explain));
            } else if ("1".equals(status)) {  //同意
                statusImg.setImageResource(R.mipmap.approval_adopted_en);
                tvStatus.setText(getResources().getString(R.string.vantop_adopt));
                tvStatus.setTextColor(getResources().getColor(R.color.agree));
            } else if ("2".equals(status)) {  //拒绝
                rightTxt = initRightTv(getResources().getString(R.string.delete));
                statusImg.setImageResource(R.mipmap.approval_refused_en);
                tvStatus.setText(getResources().getString(R.string.vantop_refuse));
                tvStatus.setTextColor(getResources().getColor(R.color.refuse));
            }
        }
    }


    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(Object response) {

    }
}
