package com.vgtech.vantop.ui.vacation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
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
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.ApprovalAdapter;
import com.vgtech.vantop.adapter.ApproverAdapter;
import com.vgtech.vantop.moudle.Approval;
import com.vgtech.vantop.moudle.Approver;
import com.vgtech.vantop.moudle.VacationApplyDetails;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.GsonUtils;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 假期审批详情
 * Created by Duke on 2016/9/18.
 */
public class VacationApplyDetailsActivity extends BaseActivity implements HttpListener<String> {
    private static final int CODE_ACTION_CHANGE = 1;
    private NetworkManager networkManager;

    private NoScrollListview approvalsListView;
    private TextView durationSumView;
    private TextView headerView;
    private TextView statusView;
    private TextView remarkView;
    private LinearLayout picsView;
    private View picsScrollView;
    private TextView timeView;
    private View undoButton;
    private View changeButton;
    private String taskid;
    private int position;

    private TextView startTimeView;
    private TextView endTimeView;

    VacationApplyDetails details;
    private boolean type;//true 我发起的，false我审批的
    private String staffNo;

    private LinearLayout buttonLayout;
    private LinearLayout approvalLayout;
    private EditText notesView;
    private Button agreeButton;
    private Button refuseButton;

    private VancloudLoadingLayout loadingLayout;
    private ScrollView scrollView;
    private Button deleteButton;
    private boolean mPreActivityRefresh;


    @Override
    protected int getContentView() {
        return R.layout.vacation_apply_detail_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.vantop_leave_apply));
        Intent intent = getIntent();
        taskid = intent.getStringExtra("id");

        position = intent.getIntExtra("position", -1);
        type = intent.getBooleanExtra("type", true);
        initView();
        if (type)
            initData(taskid);
        else {
            staffNo = intent.getStringExtra("staffno");
            getApprovalsData(taskid, staffNo);
        }
    }

    public void initView() {

        approvalsListView = (NoScrollListview) findViewById(R.id.approvals_list_view);
        durationSumView = (TextView) findViewById(R.id.durationSumView);
        headerView = (TextView) findViewById(R.id.headerTypeView);
        statusView = (TextView) findViewById(R.id.statusView);
        remarkView = (TextView) findViewById(R.id.remarkView);
        picsView = (LinearLayout) findViewById(R.id.picsView);
        picsScrollView = findViewById(R.id.picsScrollView);
        timeView = (TextView) findViewById(R.id.timeView);
        undoButton = findViewById(R.id.undoButton);
        changeButton = findViewById(R.id.changeButton);
        startTimeView = (TextView) findViewById(R.id.start_time_tv);
        endTimeView = (TextView) findViewById(R.id.end_time_tv);

        buttonLayout = (LinearLayout) findViewById(R.id.button_layout);
        approvalLayout = (LinearLayout) findViewById(R.id.approval_layout);
        notesView = (EditText) findViewById(R.id.notesView);
        agreeButton = (Button) findViewById(R.id.agree_button);
        refuseButton = (Button) findViewById(R.id.refuse_button);
        deleteButton = (Button) findViewById(R.id.delete_button);



        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading_layout);
        scrollView = (ScrollView) findViewById(R.id.scrollview);

        undoButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        changeButton.setOnClickListener(this);
        agreeButton.setOnClickListener(this);
        refuseButton.setOnClickListener(this);

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                if (type)
                    initData(taskid);
                else {
                    getApprovalsData(taskid, staffNo);
                }
            }
        });

    }

    public void initData(String taskId) {
        loadingLayout.showLoadingView(scrollView, "", true);
        networkManager = getApplicationProxy().getNetworkManager();
        String url = VanTopUtils.generatorUrl(VacationApplyDetailsActivity.this, UrlAddr.URL_VACATIONS_APPLY);
        Map<String, String> params = new HashMap<>();
        params.put("taskId", taskId);
        NetworkPath path = new NetworkPath(url, params, this, true);
        networkManager.load(1, path, this, false);
    }

    public void getApprovalsData(String taskId, String staffNo) {
        loadingLayout.showLoadingView(scrollView, "", true);
        networkManager = getApplicationProxy().getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("taskId", taskId);
        params.put("staffNo", staffNo);
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(VacationApplyDetailsActivity.this, UrlAddr.URL_APPROVALS_SHOW), params, this, true);
        networkManager.load(1, path, this, false);
    }
    //撤销
    public void cancelApply(String taskId) {
        showLoadingDialog(this, getString(R.string.prompt_info_common));
        networkManager = getApplicationProxy().getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("taskId", taskId);
        String generatorUrl = VanTopUtils.generatorUrl(VacationApplyDetailsActivity.this, UrlAddr.URL_VACATIONS_ROLLBACK);
        NetworkPath path = new NetworkPath(generatorUrl, params, this, true);
        networkManager.load(2, path, this, false);
    }
    //删除
    public void destroyApply(String taskId) {
        showLoadingDialog(this, getString(R.string.prompt_info_common));
        networkManager = getApplicationProxy().getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("taskId", taskId);
        String generatorUrl = VanTopUtils.generatorUrl(VacationApplyDetailsActivity.this, UrlAddr.URL_VACATIONS_DESTROY);
        NetworkPath path = new NetworkPath(generatorUrl, params, this, true);
        networkManager.load(2, path, this, false);
    }

    public void doApproval(String taskId, boolean status) {
        showLoadingDialog(this, getString(R.string.prompt_info_common));
        networkManager = getApplicationProxy().getNetworkManager();
        Map<String, String> params = new HashMap<>();
        params.put("taskId", taskId);
        params.put("status", status ? "Y" : "N");
        params.put("staffNo", staffNo);
        params.put("remark", notesView.getText().toString());
        NetworkPath path = new NetworkPath(VanTopUtils.generatorUrl(VacationApplyDetailsActivity.this, UrlAddr.URL_APPROVALS), params, this, true);
        networkManager.load(3, path, this, false);
    }

    private void approvalAction(final boolean isAgree) {

        String title = isAgree ? getString(R.string.confirm_agree_leave_apply) : getString(R.string.confirm_refuse_leave_apply);
        new com.vgtech.common.view.AlertDialog(this).builder()
                .setMsg(title)
                .setPositiveButton(getString(R.string.vantop_confirm_01), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doApproval(taskid, isAgree);
                    }
                }).setNegativeButton(getString(R.string.vantop_cancle), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        dismisLoadingDialog();
        loadingLayout.dismiss(scrollView);
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            if (callbackId == 1) {
                try {
                    if (rootData.getJson() == null)
                        loadingLayout.showErrorView(scrollView);
                    else {
                        String code = rootData.getJson().getString("_code");
                        if ("-1".equals(code)) {
                            loadingLayout.showEmptyView(scrollView, getString(R.string.vantop_cancel_prompt), true, true);
                        } else {
                            loadingLayout.showErrorView(scrollView);
                        }
                    }
                } catch (Exception e) {
                    loadingLayout.showErrorView(scrollView);
                    e.printStackTrace();
                }
            }
            return;
        }
        switch (callbackId) {
            case 1:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    details = JsonDataFactory.getData(VacationApplyDetails.class, jsonObject);

                    List<String> piclist = GsonUtils.parseDataToList(jsonObject.getString("pics"));
                    List<Approver> approvers = JsonDataFactory.getDataArray(Approver.class, jsonObject.getJSONArray("details"));

                    if (details.isclockin) {
                        findViewById(R.id.isclockin_view).setVisibility(View.VISIBLE);
                        findViewById(R.id.isclockin_view).setOnClickListener(this);
                    }
                    setDataToView(details, piclist, approvers);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    String msg = rootData.getJson().getString("_msg");
                    if (!TextUtils.isEmpty(msg))
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                Intent intent = new Intent();
//                intent.putExtra("position", position);
//                setResult(RESULT_OK, intent);

                sendBroadcast(new Intent("Refresh_List"));

                finish();
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
            default:
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

        if (v.getId() == R.id.undoButton) {

            new AlertDialog(this).builder()
                    .setMsg(getString(R.string.vantop_confirm_undo))
                    .setPositiveButton(getString(R.string.vantop_confirm_01), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {//撤销
                            cancelApply(taskid);
                        }
                    }).setNegativeButton(getString(R.string.vantop_cancle), new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();

        } else if (v.getId() == R.id.delete_button) {
            new AlertDialog(this).builder()
                    .setMsg(getString(R.string.vantop_confirm_delete))
                    .setPositiveButton(getString(R.string.vantop_confirm_01), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {//删除
                            destroyApply(taskid);
                        }
                    }).setNegativeButton(getString(R.string.vantop_cancle), new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        } else if (v.getId() == R.id.changeButton) {
            //TODO 变更
            Intent intent = new Intent(VacationApplyDetailsActivity.this, ApplyVacationActivity.class);
            intent.putExtra("detailsJson", details.getJson().toString());
//            startActivity(intent);
            startActivityForResult(intent, CODE_ACTION_CHANGE);
        } else if (v.getId() == R.id.agree_button) {
            approvalAction(true);
        } else if (v.getId() == R.id.refuse_button) {
            approvalAction(false);
        } else if (v.getId() == R.id.isclockin_view) {
            Intent intent = new Intent("com.vgtech.vancloud.intent.action.MapLocation");
            String latlng = details.latitude + "," + details.longitude;
            intent.putExtra("latlng", latlng);
            intent.putExtra("address", "");
            intent.putExtra("name", "");
            intent.putExtra("type", 1);
            intent.putExtra("edit", false);
            startActivity(intent);
        } else {
            super.onClick(v);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_ACTION_CHANGE && resultCode == Activity.RESULT_OK) {
            initData(taskid);
            sendBroadcast(new Intent("Refresh_List"));
        }
    }

    public void setDataToView(VacationApplyDetails details, List<String> picList, List<Approver> approvers) {

        headerView.setText(details.desc);
        durationSumView.setText(details.num + " " + details.unit);
        statusView.setText(VanTopUtils.getStatusResId(details.status));
        if ("0".equals(details.status)) {           //审批中
            statusView.setTextColor(getResources().getColor(R.color.txt_explain));
        } else if ("1".equals(details.status)) {    //同意
            statusView.setTextColor(getResources().getColor(R.color.agree));
        } else if ("2".equals(details.status)) {   //拒绝
            statusView.setTextColor(getResources().getColor(R.color.refuse));
        }

        timeView.setText(details.created_at);
        remarkView.setText(details.remark);
        startTimeView.setText(details.from);
        endTimeView.setText(details.to);

        if (picList.size() > 0) {
//            picUrl = pics.get(0);
            picsScrollView.setVisibility(View.VISIBLE);
            for (String pic : picList) {
                View view = LayoutInflater.from(this).inflate(R.layout.image_layout, null);
                SimpleDraweeView imageView = (SimpleDraweeView) view.findViewById(R.id.image);
                picsView.addView(view);
                String imgUrl = "";
                if (pic.contains("?loginUserCode="))
                    imgUrl = pic;
                else
                    imgUrl = pic + "?loginUserCode=" + PrfUtils.getStaff_no(this);
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
        if (approvers.size() > 0) {
            ApproverAdapter approverAdapter = new ApproverAdapter(this, approvers);
            approvalsListView.setAdapter(approverAdapter);
        }

        //true 我发起的，false我审批的
        if (type) {
            if (details.canChange&&details.canUndo){
                buttonLayout.setVisibility(View.VISIBLE);
                //变更
                changeButton.setVisibility(View.VISIBLE);
                //撤销
                undoButton.setVisibility(View.VISIBLE);
            }else if (details.canChange){
                buttonLayout.setVisibility(View.VISIBLE);
                //变更
                changeButton.setVisibility(View.VISIBLE);
            }else if (details.canUndo){
                buttonLayout.setVisibility(View.VISIBLE);
                //撤销
                undoButton.setVisibility(View.VISIBLE);
            }else {
                buttonLayout.setVisibility(View.GONE);
                //变更
                changeButton.setVisibility(View.GONE);
                //撤销
                undoButton.setVisibility(View.GONE);
            }

            if (details.canDelete) {
                //删除
                deleteButton.setVisibility(View.VISIBLE);
            } else {
                if ("0".equals(details.status)) {//审批中
                    boolean isUnApprover = false;//是否正在审批
                    for (int i = 0; i < approvers.size(); i++) {
                        Approver approver = approvers.get(i);
                        if (!"0".equals(approver.status)){
                            isUnApprover = true;
                        }
                    }
                    if (isUnApprover){//正在审批
                        deleteButton.setVisibility(View.GONE);
                    }else {//未审批
                        deleteButton.setVisibility(View.VISIBLE);
                    }
                } else if ("1".equals(details.status)) {    //同意
                    deleteButton.setVisibility(View.GONE);
                } else if ("2".equals(details.status)) {   //拒绝
                    boolean isChaneLayout = getIntent().getBooleanExtra("isChaneLayout", false);//是否从变更页面跳转
                    if (isChaneLayout){
                        deleteButton.setVisibility(View.GONE);
                    }else {
                        deleteButton.setVisibility(View.VISIBLE);
                    }
                }
            }
            approvalLayout.setVisibility(View.GONE);
        } else {
            deleteButton.setVisibility(View.GONE);
            if ("0".equals(details.status)) {
                approvalLayout.setVisibility(View.VISIBLE);
            }
            buttonLayout.setVisibility(View.GONE);
        }
    }


    public static class ApprovalViewHolder {

        public final View root;
        public View bottomLineView;
        public TextView staffTitleView;
        public TextView approvalNameView;
        public TextView approvalRemarkView;
        public TextView approvalStatusView;
        public TextView approvalDateView;


        public ApprovalViewHolder(final View root) {
            bottomLineView = root.findViewById(R.id.bottom_line);
            staffTitleView = (TextView) root.findViewById(R.id.approvalstaff_title);
            approvalNameView = (TextView) root.findViewById(R.id.approval_staff);
            approvalRemarkView = (TextView) root.findViewById(R.id.approval_notes);
            approvalStatusView = (TextView) root.findViewById(R.id.approval_status);
            approvalDateView = (TextView) root.findViewById(R.id.approval_time);

            this.root = root;
        }

        public void setData(final int level, final Approver approver, Context context) {

//            approver.status 0:待审批。1：同意。2：拒绝。

            if ("0".equals(approver.status)) {
                approvalStatusView.setText(context.getString(R.string.vantop_approving));
                approvalStatusView.setTextColor(context.getResources().getColor(R.color.comment_grey));
            } else if ("1".equals(approver.status)) {
                approvalStatusView.setText(context.getString(R.string.agree));
                approvalStatusView.setTextColor(context.getResources().getColor(R.color.vantop_process_agree));
            } else if ("2".equals(approver.status)) {
                approvalStatusView.setText(context.getString(R.string.refuse));
                approvalStatusView.setTextColor(context.getResources().getColor(R.color.vantop_process_disagree));
            }
            staffTitleView.setText(root.getContext().getString(R.string.vantop_approval_leval, VanTopUtils.arabToIndex(level)));
            if (!TextUtils.isEmpty(approver.staff_name) && !"null".equals(approver.staff_name))
                approvalNameView.setText(approver.staff_name);
            approvalDateView.setText(approver.approved_date);
            approvalRemarkView.setText(approver.remark);
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
