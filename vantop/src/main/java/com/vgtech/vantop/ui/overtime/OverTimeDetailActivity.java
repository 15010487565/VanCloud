package com.vgtech.vantop.ui.overtime;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.EventBusMsg;
import com.android.volley.VolleyError;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.utils.CommonUtils;
import com.vgtech.common.utils.DeviceUtils;
import com.vgtech.common.utils.ImageCacheManager;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.ApprovalAdapter;
import com.vgtech.vantop.moudle.Approval;
import com.vgtech.vantop.moudle.OvertimeApplyDetail;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Duke on 2016/7/20.
 * 加班详情
 */
public class OverTimeDetailActivity extends BaseActivity implements HttpListener<String>, View.OnClickListener {

    private ImageView statusImg;
    private SimpleDraweeView staffImg;
    private TextView staffNameTxt,overtimeName;//加班员工姓名
    private TextView stateExplainTxt;
    private TextView overtimeTypeTxt;
    private TextView shiftTxt;
    private TextView overtimeTimeTxt;
    private TextView overtimeNum;
    private TextView deducEatNumTxt;
    private TextView isoutotTxt;
    private TextView overtimeReasonTxt;
    private LinearLayout llStatus;
    private TextView tvStatus;//审核状态
    private TextView convertLeavecodeTxt; //转假假种
    private TextView convertHoursTxt;     //转假时长
    private TextView convertReasonTxt;    //转假原因
    private LinearLayout picsView;
    private LinearLayout convertLeaveLl;
    private LinearLayout shiftLl;
    private View picsScrollView;
    private ListView approvalList;
    private ImageView shift_line;
    private ImageView explain_line;

    private TextView rightView;

    private OvertimeApplyDetail overtimeApplyDetail;
    private String taskId;

    boolean type;
    private RelativeLayout topLayout;
    private LinearLayout bottomLayout;
    String staffNo;
    private EditText editTextInput;
    private String level;
    private NetworkManager networkManager;

    private int position;

    private VancloudLoadingLayout loadingLayout;
    private ScrollView scrollview;
    private TextView mApplyTimeTxt;


    @Override
    protected int getContentView() {
        return R.layout.activity_overtime_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rightView = initRightTv(getString(R.string.vantop_overtime_change));

        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        taskId = intent.getStringExtra("taskId");
        type = intent.getBooleanExtra("type", true);
        initView();

        rightView.setVisibility(View.GONE);

        if (type) {
            setTitle(getString(R.string.vantop_overtime_detail));
            initData(taskId);

        } else {
            setTitle(getString(R.string.vantop_overtime_approval));
            staffNo = intent.getStringExtra("staffno");
            getApprovalData(taskId, staffNo);

        }
    }

    private void initView() {
        statusImg = (ImageView) findViewById(R.id.status_img);
        //头像
        staffImg = (SimpleDraweeView) findViewById(R.id.staff_img);
        //审核状态
        llStatus = (LinearLayout) findViewById(R.id.ll_status);
        tvStatus = (TextView) findViewById(R.id.tv_status);

        staffNameTxt = (TextView) findViewById(R.id.staff_name_txt);
        overtimeName = (TextView) findViewById(R.id.overtime_name);
        stateExplainTxt = (TextView) findViewById(R.id.state_explain_txt);
        overtimeTypeTxt = (TextView) findViewById(R.id.overtime_type_txt);
        shiftTxt = (TextView) findViewById(R.id.shift_txt);
        mApplyTimeTxt = (TextView) findViewById(R.id.apply_time_txt);
        overtimeTimeTxt = (TextView) findViewById(R.id.overtime_time_txt);
        overtimeNum = (TextView) findViewById(R.id.overtime_num_txt);
        deducEatNumTxt = (TextView) findViewById(R.id.deduceat_num_txt);
        isoutotTxt = (TextView) findViewById(R.id.isoutot_txt);
        overtimeReasonTxt = (TextView) findViewById(R.id.overtime_reason_txt);
        convertLeavecodeTxt = (TextView) findViewById(R.id.convert_leavecode_txt);
        convertHoursTxt = (TextView) findViewById(R.id.convert_hours_txt);
        convertReasonTxt = (TextView) findViewById(R.id.convert_reason_txt);
        picsView = (LinearLayout) findViewById(R.id.picsView);
        convertLeaveLl = (LinearLayout) findViewById(R.id.convertLeave_ll);
        shiftLl = (LinearLayout) findViewById(R.id.shift_ll);
        picsScrollView = (View) findViewById(R.id.picsLl);
        approvalList = (ListView) findViewById(R.id.approval_list);
        approvalList.setFocusable(false);
        shift_line = (ImageView) findViewById(R.id.shift_line);
        explain_line = (ImageView) findViewById(R.id.explain_line);
        topLayout = (RelativeLayout) findViewById(R.id.top_layout);
        bottomLayout = (LinearLayout) findViewById(R.id.bottom_layout);

        findViewById(R.id.refuse_click).setOnClickListener(this);
        findViewById(R.id.agree_click).setOnClickListener(this);


        if (!type) {
            LinearLayout.LayoutParams l = (LinearLayout.LayoutParams) topLayout.getLayoutParams();
            l.gravity = Gravity.CENTER;
            topLayout.setLayoutParams(l);
            bottomLayout.setVisibility(View.VISIBLE);
            statusImg.setVisibility(View.GONE);

        } else {
            bottomLayout.setVisibility(View.GONE);

//            statusImg.setVisibility(View.VISIBLE);
            statusImg.setVisibility(View.GONE);

        }

        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading_layout);
        scrollview = (ScrollView) findViewById(R.id.scrollview);

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                if (type) {
                    rightView.setVisibility(View.VISIBLE);
                    initData(taskId);
                } else {
                    rightView.setVisibility(View.GONE);
                    getApprovalData(taskId, staffNo);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_right) {

            if ("0".equals(overtimeApplyDetail.status)) {
                doBackout(taskId);
            } else if ("2".equals(overtimeApplyDetail.status)) {
                doDestroy(taskId);
            } else {
                //TODO 变更
                Intent intent = new Intent(this, CreatedOverTimeActivity.class);
                intent.putExtra("json", overtimeApplyDetail.getJson().toString());
                intent.putExtra("taskid", taskId);
                startActivity(intent);
            }
        } else if (v.getId() == R.id.refuse_click) {
            showDialog("N");
        } else if (v.getId() == R.id.agree_click) {
            showDialog("Y");
        } else {
            super.onClick(v);
        }
    }

    public void initData(String taskId) {

        loadingLayout.showLoadingView(scrollview, "", true);
        networkManager = getApplicationProxy().getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("taskId", taskId);
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_OVERTIMEAPPLY_DETAIL), params, this, true);
        networkManager.load(1, path, this);

    }

    public void getApprovalData(String taskId, String staffNo) {

        loadingLayout.showLoadingView(scrollview, "", true);
        networkManager = getApplicationProxy().getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("taskId", taskId);
        params.put("staffNo", PrfUtils.getStaff_no(this));
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_OVERTIMEAPPROVAL_DETAIL), params, this, true);
        networkManager.load(1, path, this);

    }

    /**
     * 撤销
     *
     * @param taskId
     */
    public void doBackout(String taskId) {

        showLoadingDialog(this, getString(R.string.dataloading));
        networkManager = getApplicationProxy().getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("taskId", taskId);
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_OVERTIMEAPPLY_BACKOUT), params, this, true);
        networkManager.load(2, path, this);

    }

    /**
     * 删除
     *
     * @param taskId
     */
    public void doDestroy(String taskId) {

        showLoadingDialog(this, getString(R.string.dataloading));
        networkManager = getApplicationProxy().getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("taskId", taskId);
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_OVERTIMEAPPLY_DESTROY), params, this, true);
        networkManager.load(2, path, this);

    }

    public void approvalOperation(String inputString, String operation) {

        showLoadingDialog(this, getString(R.string.dataloading));
        networkManager = getApplicationProxy().getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("applyTaskId", taskId);
        params.put("applyStaffNo", staffNo);
        params.put("approvalStaffNo", PrfUtils.getStaff_no(this));
        params.put("approvalStatus", operation);
        params.put("approvalLevel", level);
        params.put("approvalRemark", inputString);
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_OVERTIMEAPPROVAL_OPERATION), params, this, true);
        networkManager.load(3, path, this);
    }


    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        dismisLoadingDialog();
        loadingLayout.dismiss(scrollview);
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            if (callbackId == 1) {
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
            case 1:
                try {
                    if (type) {
                        rightView.setVisibility(View.VISIBLE);
                    } else {
                        rightView.setVisibility(View.GONE);
                    }
                    JSONObject jsonObject = rootData.getJson();
                    overtimeApplyDetail = JsonDataFactory.getData(OvertimeApplyDetail.class, jsonObject.getJSONObject("data"));
                    List<Approval> approvers = JsonDataFactory.getDataArray(Approval.class, overtimeApplyDetail.getJson().getJSONArray("approval"));
                    setData(overtimeApplyDetail, approvers);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    String code = rootData.getJson().getString("_code");
                    String msg = rootData.getJson().getString("_msg");
                    if (!TextUtils.isEmpty(msg))
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//                    if ("0".equals(code)) {
//                        Intent intent = new Intent();
//                        intent.putExtra("position", position);
//                        setResult(RESULT_OK, intent);
//
//                    }
                    EventBusMsg messageEvent = new EventBusMsg();
                    messageEvent.setCode(Constants.REFRESH);
                    EventBus.getDefault().post(messageEvent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                try {
                    String code = rootData.getJson().getString("_code");
                    String msg = rootData.getJson().getString("_msg");
                    if (!TextUtils.isEmpty(msg))
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    if ("0".equals(code)) {
                        //TODO 审批成功
                        Intent broadcIntent = new Intent(CommonUtils.ACTION_APPROVAL_PROCESS);
                        broadcIntent.putExtra("position", position);
                        sendBroadcast(broadcIntent);
                        sendBroadcast(new Intent("RECEIVER_PUSH"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                finish();
                break;
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    public void showDialog(final String operation) {
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

    private void setBottomLayout(String status) {
        if ("0".equals(status)) {                               //待审批
            bottomLayout.setVisibility(View.VISIBLE);

        } else if ("1".equals(status) || "2".equals(status)) {  //已审批
            bottomLayout.setVisibility(View.GONE);

            if ("1".equals(status)) {
                stateExplainTxt.setText(getResources().getString(R.string.vantop_adopt));
            } else {
                stateExplainTxt.setText(getResources().getString(R.string.refuse));
            }
        }
    }

    private void setStatusImg(String status) {
        Log.e("TAG_加班审批",";status="+status);
        if (TextUtils.isEmpty(status))
            return;

        String language = PrfUtils.getAppLanguage(this);
        rightView.setVisibility(View.VISIBLE);
        Log.e("TAG_加班审批","language="+language+";status="+status);
        if ("zh".equals(language)) {
            if ("0".equals(status)) {           //审批中
                rightView.setText(getResources().getString(R.string.vantop_undo));
                statusImg.setImageResource(R.mipmap.approvaling_img);
                tvStatus.setText(getResources().getString(R.string.vantop_approving));
                tvStatus.setTextColor(getResources().getColor(R.color.txt_explain));
            } else if ("1".equals(status)) {    //同意
                rightView.setText(getResources().getString(R.string.vantop_overtime_change));
                stateExplainTxt.setText(getResources().getString(R.string.vantop_adopt));
                statusImg.setImageResource(R.mipmap.approval_adopted);
                tvStatus.setText(getResources().getString(R.string.vantop_adopt));
                tvStatus.setTextColor(getResources().getColor(R.color.agree));
            } else if ("2".equals(status)) {   //拒绝
                rightView.setText(getResources().getString(R.string.delete));
                stateExplainTxt.setText(getResources().getString(R.string.refuse));
                statusImg.setImageResource(R.mipmap.approval_refused);
                tvStatus.setText(getResources().getString(R.string.vantop_refuse));
                tvStatus.setTextColor(getResources().getColor(R.color.refuse));
            }
        } else if ("en".equals(language)) {
            if ("0".equals(status)) {           //审批中
                rightView.setText(getResources().getString(R.string.vantop_undo));
                statusImg.setImageResource(R.mipmap.approvaling_img_en);
                tvStatus.setText(getResources().getString(R.string.vantop_approving));
                tvStatus.setTextColor(getResources().getColor(R.color.txt_explain));
            } else if ("1".equals(status)) {    //同意
                rightView.setText(getResources().getString(R.string.vantop_overtime_change));
                stateExplainTxt.setText(getResources().getString(R.string.vantop_adopt));
                statusImg.setImageResource(R.mipmap.approval_adopted_en);
                tvStatus.setText(getResources().getString(R.string.vantop_adopt));
                tvStatus.setTextColor(getResources().getColor(R.color.agree));
            } else if ("2".equals(status)) {   //拒绝
                rightView.setText(getResources().getString(R.string.delete));
                stateExplainTxt.setText(getResources().getString(R.string.refuse));
                statusImg.setImageResource(R.mipmap.approval_refused_en);
                tvStatus.setText(getResources().getString(R.string.vantop_refuse));
                tvStatus.setTextColor(getResources().getColor(R.color.refuse));
            }
        }
    }

    private void setData(OvertimeApplyDetail detail, List<Approval> approvallist) {
        ImageCacheManager.getImage(this, staffImg, detail.staffNo);
        VanTopUtils.enterVantopUserInfoBystaffNo(this, detail.staffNo, staffImg);
        staffNameTxt.setText(detail.staffName);
        overtimeName.setText(detail.staffName);
        overtimeTypeTxt.setText(detail.typeValue);
        if (detail.shiftVisiable) {
            shiftLl.setVisibility(View.VISIBLE);
            shift_line.setVisibility(View.VISIBLE);
            shiftTxt.setText(detail.shiftValue);
        }
        mApplyTimeTxt.setText(detail.createDate);
        overtimeTimeTxt.setText(detail.date + " " + detail.time);
        overtimeNum.setText(detail.overtimeNum + getString(R.string.vantop_hour));
        deducEatNumTxt.setText(detail.deductHours + getString(R.string.vantop_hour));
        if (detail.isOutOt) {
            isoutotTxt.setText(getString(R.string.vantop_yes));
        } else {
            isoutotTxt.setText(getString(R.string.vantop_no));
        }
        overtimeReasonTxt.setText(detail.remark);
        if (detail.convertLeaveVisiable) {
            convertLeaveLl.setVisibility(View.VISIBLE);
            convertLeavecodeTxt.setText(detail.convertLeaveCodeValue);
            convertHoursTxt.setText(detail.convertHours);
            convertReasonTxt.setText(detail.convertRemark);
        }
        if (detail.picVisiable) {
            if (!TextUtils.isEmpty(detail.pic)) {
                picsScrollView.setVisibility(View.VISIBLE);
                explain_line.setVisibility(View.VISIBLE);
                SimpleDraweeView imageView = new SimpleDraweeView(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(CommonUtils.dpToPx(getResources(), 90), CommonUtils.dpToPx(getResources(), 90));
                lp.setMargins(0, 0, CommonUtils.dpToPx(getResources(), 10), 0);
                imageView.setLayoutParams(lp);
                picsView.addView(imageView);
                String imgUrl = "";
                if (detail.pic.contains("?loginUserCode="))
                    imgUrl = detail.pic;
                else
                    imgUrl = detail.pic + "?loginUserCode=" + PrfUtils.getStaff_no(this);
                imgUrl = VanTopUtils.generatorImageUrl(this, imgUrl);
                int wh = DeviceUtils.convertDipOrPx(this, 90);
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgUrl))
                        .setResizeOptions(new ResizeOptions(wh, wh))
                        .setAutoRotateEnabled(true)
                        .build();
                PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                        .setOldController(imageView.getController())
                        .setImageRequest(request)
                        .build();
                imageView.setController(controller);
                imageView.setTag(imgUrl);
                imageView.setOnClickListener(photoListener);
            }
        }
        Log.e("TAG_加班审批",";type="+type);
        if (type){
            //
            llStatus.setVisibility(View.VISIBLE);
            setStatusImg(detail.status);
        } else{

            llStatus.setVisibility(View.GONE);
            setBottomLayout(detail.status);
        }

        if ("0".equals(detail.status)) {

            for (Approval approval : approvallist) {
                if (PrfUtils.getStaff_no(this).equals(approval.staffNo)) {
                    setBottomLayout(approval.status);
                }
                if ("0".equals(approval.status)) {
                    if (!TextUtils.isEmpty(approval.staffName) && !"null".equals(approval.staffName)) {
                        stateExplainTxt.setText(getString(R.string.vantop_wait)
                                + approval.staffName + getString(R.string.vantop_approval_new));
                    } else {
                        stateExplainTxt.setText(getString(R.string.vantop_wait) + getString(R.string.vantop_approval_new));
                    }
                    break;
                }
            }
        }

        if (approvallist.size() > 0) {
            ApprovalAdapter approvalAdapter = new ApprovalAdapter(this, approvallist);
            approvalList.setAdapter(approvalAdapter);
        }

        if (!type) {
            for (Approval approval : approvallist) {
                if (PrfUtils.getStaff_no(this).equals(approval.staffNo)) {
                    level = approval.level;
                    break;
                }
            }
        }

    }

    View.OnClickListener photoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String url = (String) v.getTag();
            List<ImageInfo> imgInfo = new ArrayList<>();
            imgInfo.add(new ImageInfo(url, url));
            String json = new Gson().toJson(imgInfo);
            Intent intent = new Intent("com.vgtech.imagecheck");
            intent.putExtra("listjson", json);
            intent.putExtra("numVisible", false);
            startActivity(intent);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkManager != null)
            networkManager.cancle(this);
    }
}
