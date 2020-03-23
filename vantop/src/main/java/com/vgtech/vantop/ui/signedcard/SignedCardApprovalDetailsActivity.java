package com.vgtech.vantop.ui.signedcard;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.utils.CommonUtils;
import com.vgtech.common.utils.ImageCacheManager;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.ApprovalAdapter;
import com.vgtech.vantop.moudle.Approval;
import com.vgtech.vantop.moudle.SignedCardItemData;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.ui.userinfo.VantopUserInfoActivity;
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
public class SignedCardApprovalDetailsActivity extends BaseActivity implements HttpListener {

    ImageView statusImg;
    SimpleDraweeView staffImg;
    TextView staffNoTxt;
    TextView staffNameTxt;
    TextView cardNumberTxt;
    TextView dataTxt;
    TextView terminalTxt;
    TextView reasonTxt;
    TextView remarkTxt;
    ListView approvalList;
    LinearLayout bottomLayout;
    RelativeLayout refuseClick;
    RelativeLayout agreeClick;
    private VancloudLoadingLayout loadingLayout;
    private ScrollView scrollview;
    private String taskId;
    private String staffNo;
    private final int CALLBACK_LOADDATA = 0X001;
    private final int CALLBACK_OPERATION = 0X002;

    private int position;
    private LinearLayout mLLAttachmentPicContainer;
    private SimpleDraweeView mSdvAttachementPic;

    @Override
    protected int getContentView() {
        return R.layout.signedcard_approal_details_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intiView();
        initData();
        initEvent();
    }

    private void intiView() {
        setTitle(getString(R.string.signcard_approval));
        statusImg = (ImageView) findViewById(R.id.status_img);
        staffImg = (SimpleDraweeView) findViewById(R.id.staff_img);
        staffNoTxt = (TextView) findViewById(R.id.staff_no_txt);
        staffNameTxt = (TextView) findViewById(R.id.staff_name_txt);
        cardNumberTxt = (TextView) findViewById(R.id.card_number_txt);
        dataTxt = (TextView) findViewById(R.id.data_txt);
        terminalTxt = (TextView) findViewById(R.id.terminal_txt);
        reasonTxt = (TextView) findViewById(R.id.reason_txt);
        remarkTxt = (TextView) findViewById(R.id.remark_txt);
        approvalList = (ListView) findViewById(R.id.approval_list);
        approvalList.setFocusable(false);
        bottomLayout = (LinearLayout) findViewById(R.id.bottom_ll);
        refuseClick = (RelativeLayout) findViewById(R.id.refuse_rl);
        agreeClick = (RelativeLayout) findViewById(R.id.agree_rl);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading_layout);
        scrollview = (ScrollView) findViewById(R.id.scrollview);

        mLLAttachmentPicContainer = (LinearLayout) findViewById(R.id.signedcard_approval_details_ll_pic_container);
        mSdvAttachementPic = (SimpleDraweeView) findViewById(R.id.signedcard__approval_details_sdv_pic);

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                initData();
            }
        });

    }

    private void initEvent() {
        refuseClick.setOnClickListener(this);
        agreeClick.setOnClickListener(this);
        staffImg.setOnClickListener(this);
        mSdvAttachementPic.setOnClickListener(this);
    }

    private void initData() {
        taskId = getIntent().getStringExtra("taskId");
        staffNo = getIntent().getStringExtra("staffNo");
        position = getIntent().getIntExtra("position", -1);
        loadData();
    }

    private void loadData() {
        loadingLayout.showLoadingView(scrollview, "", true);
        String path = VanTopUtils.generatorUrl(this, UrlAddr.URL_SIGNEDCARD_APPROVAL_DETAILS);
        Uri uri = Uri.parse(path);
        Map<String, String> params = new HashMap<>();
        //POST请求params不必须不能为空
        params.put("taskId", taskId);
        params.put("staffNo", staffNo);
        NetworkPath np = new NetworkPath(uri.toString(), params, this, true);
        getApplicationProxy().getNetworkManager().load(CALLBACK_LOADDATA, np, this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (view == refuseClick) {      //拒绝
            showDialog("N");
        } else if (view == agreeClick) { //同意
            showDialog("Y");
        } else if (view == staffImg) {
            Intent intent = new Intent(this, VantopUserInfoActivity.class);
            intent.putExtra(VantopUserInfoActivity.BUNDLE_STAFFNO, staffNo);
            startActivity(intent);
        }else if (view == mSdvAttachementPic){
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

    private EditText editTextInput;

    private void showDialog(final String operation) {
        AlertDialog dialog = new AlertDialog(this).builder()
//                .setTitle(getString(R.string.please_input_remark))
                ;
        editTextInput = dialog.setEditer();
        editTextInput.setHint("原因（非必填项）");
        dialog.setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputString = editTextInput.getText().toString();
                approvalOperation(inputString, operation);
            }
        }).setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    private void approvalOperation(String remark, String operation) {
        showLoadingDialog(this, "");
        String path = VanTopUtils.generatorUrl(this, UrlAddr.URL_SIGNEDCARD_APPROVAL_OPERATION);
        Uri uri = Uri.parse(path);
        Map<String, String> params = new HashMap<>();
        params.put("taskId", taskId);
        params.put("staffNo", staffNo);
        params.put("remark", remark);
        params.put("status", operation);
        NetworkPath np = new NetworkPath(uri.toString(), params, this, true);
        getApplicationProxy().getNetworkManager().load(CALLBACK_OPERATION, np, this);
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
            case CALLBACK_LOADDATA:
                try {
                    JSONObject jo = rootData.getJson().getJSONObject("data");
                    SignedCardItemData signedInfo = JsonDataFactory.getData(SignedCardItemData.class, jo);
                    List<Approval> approvals = JsonDataFactory.getDataArray(Approval.class, jo.getJSONArray("approval"));
                    inflateData(signedInfo, approvals);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CALLBACK_OPERATION:
                sendBroadcast(new Intent("RECEIVER_PUSH"));
                Intent broadcIntent = new Intent(CommonUtils.ACTION_APPROVAL_PROCESS);
                broadcIntent.putExtra("position", position);
                sendBroadcast(broadcIntent);
                finish();
                break;
        }
    }

    private void inflateData(SignedCardItemData signedInfo, List<Approval> approvals) {
        staffNoTxt.setText(signedInfo.staffNo);
        staffNameTxt.setText(signedInfo.staffName);
        cardNumberTxt.setText(signedInfo.cardNo);
        dataTxt.setText(signedInfo.date + " " + signedInfo.time);
        terminalTxt.setText(signedInfo.termNo);
        reasonTxt.setText(signedInfo.reason);
        remarkTxt.setText(signedInfo.remark);
        ImageCacheManager.getImage(this, staffImg, signedInfo.staffNo);
        Approval al = approvals.get(approvals.size() - 1);
        if (al != null)
            setBottomLayout(al.status);
        if (approvals != null && approvals.size() > 0)
            approvalList.setAdapter(new ApprovalAdapter(this, approvals));

        if (TextUtils.isEmpty(signedInfo.picurl)) {
            if (mLLAttachmentPicContainer.getVisibility() != View.GONE)
                mLLAttachmentPicContainer.setVisibility(View.GONE);
        } else {
            if (mLLAttachmentPicContainer.getVisibility() != View.VISIBLE)
                mLLAttachmentPicContainer.setVisibility(View.VISIBLE);
            ImageOptions.setImage(mSdvAttachementPic, signedInfo.picurl);
            mSdvAttachementPic.setTag(signedInfo.picurl);
        }
    }


    private void setBottomLayout(String status) {
        if ("0".equals(status)) {                 //待审批
            bottomLayout.setVisibility(View.VISIBLE);
        } else {
            bottomLayout.setVisibility(View.GONE);//已审批
        }
    }


    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(Object response) {

    }
}
