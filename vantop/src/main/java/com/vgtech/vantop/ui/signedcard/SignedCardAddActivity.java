package com.vgtech.vantop.ui.signedcard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.Bimp;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.FilePair;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.ui.publish.PicSelectActivity;
import com.vgtech.common.utils.Emiter;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.common.view.ActionSheetDialog;
import com.vgtech.common.view.DateFullDialogView;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.SignedCardAddInitData;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.ui.userinfo.VantopUserInfoActivity;
import com.vgtech.vantop.utils.PreferencesController;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.vgtech.vantop.R.id.reason_rl;
import static com.vgtech.vantop.R.id.terminal_rl;

/**
 * Created by shilec on 2016/7/19.
 */
public class SignedCardAddActivity extends BaseActivity implements HttpListener {

    private TextView mTvName;
    private SimpleDraweeView mIvHead;
    private TextView mTvStaffNo;
    private TextView mTvCardNo;
    private TextView mSelectDate;
    private TextView mSelectZhongDuan;
    private TextView mSelectReason;
    private EditText mEditRemark;
    private TextView mTvRight, tvSubmit;

    private SignedCardAddInitData mData;

    private final int CALLBACK_INITDATA = 0X001;
    private final int CALLBACK_SUBMIT = 0X002;
    private RelativeLayout timeRl;
    private RelativeLayout terminalRl;
    private RelativeLayout reasonRl;
    private ImageView mIvAttachPic;
    private ImageView mIvAttachPicAdd;
    private RelativeLayout mRlAttachContain;
    private ImageView mIvAttachPicDel;
    private static final int INTENT_SELECT_PIC = 0;
    private static final int KEY_PIC_URL = 1;
    private String mDate;
    private RelativeLayout mDateRl;
    private TextView mDateTv;

    @Override
    protected int getContentView() {
        return R.layout.activity_signcard_create;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDate = getIntent().getStringExtra("date");
        initData();
        initView();
    }

    private void initView() {
        setTitle(getString(R.string.change_sign_apply));
        mTvName = (TextView) findViewById(R.id.staff_name_txt);
        mIvHead = (SimpleDraweeView) findViewById(R.id.staff_img);
        mTvCardNo = (TextView) findViewById(R.id.card_number_txt);
        mSelectDate = (TextView) findViewById(R.id.data_txt);
        mSelectReason = (TextView) findViewById(R.id.reason_txt);
        mSelectZhongDuan = (TextView) findViewById(R.id.terminal_txt);
        mEditRemark = (EditText) findViewById(R.id.remark_edt);
        mTvRight = (TextView) findViewById(R.id.tv_right);
        mTvStaffNo = (TextView) findViewById(R.id.staff_no_txt);
        timeRl = (RelativeLayout) findViewById(R.id.time_rl);
        if (!TextUtils.isEmpty(mDate)) {
            mDateRl = (RelativeLayout) findViewById(R.id.date_rl);
            mDateTv = (TextView) findViewById(R.id.date_txt);
            mDateRl.setVisibility(View.VISIBLE);
            mDateTv.setText(mDate);
        }
        terminalRl = (RelativeLayout) findViewById(terminal_rl);
        reasonRl = (RelativeLayout) findViewById(reason_rl);

        mRlAttachContain = (RelativeLayout) findViewById(R.id.signcard_create_iv_attachment_pic_container);
        mIvAttachPic = (ImageView) findViewById(R.id.signcard_create_iv_attachment_pic);
        mIvAttachPicDel = (ImageView) findViewById(R.id.signcard_create_iv_attachment_pic_delete);
        mIvAttachPicAdd = (ImageView) findViewById(R.id.signcard_create_iv_add_attachment_pic);

        //提交
        mTvRight.setVisibility(View.GONE);
//        mTvRight.setText(getString(R.string.vantop_submit));
        tvSubmit = (TextView) findViewById(R.id.tv_submit);
        tvSubmit.setOnClickListener(this);

        mTvRight.setOnClickListener(this);
        timeRl.setOnClickListener(this);
        terminalRl.setOnClickListener(this);
        reasonRl.setOnClickListener(this);
        mIvHead.setOnClickListener(this);
        mIvAttachPicAdd.setOnClickListener(this);
        mIvAttachPicDel.setOnClickListener(this);

        PreferencesController prf = new PreferencesController();
        prf.context = this;
        UserAccount account = prf.getAccount();
        if (account != null && !TextUtils.isEmpty(account.user_name))
            mTvName.setText(account.user_name);
        ImageOptions.setUserImage(mIvHead, account.photo);
    }

    private void initData() {
        String path = VanTopUtils.generatorUrl(this, UrlAddr.URL_SIGNEDCARD_NEW);
        NetworkPath np = new NetworkPath(path, null, this, true);
        getApplicationProxy().getNetworkManager().load(CALLBACK_INITDATA, np, this);
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
                onDataLoadFinished(rootData);
            }
            break;

            case CALLBACK_SUBMIT: {
                Toast.makeText(this, getString(R.string.vantop_submit_success), Toast.LENGTH_SHORT).show();
                dismisLoadingDialog();
                Emiter.getInstance().emit("VanTopApprovalListFragment", Constants.REFRESH);
                finish();
            }
            break;
        }
    }


    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(Object response) {

    }

    private void onDataLoadFinished(RootData rootData) {
        mData = SignedCardAddInitData.fromJson(rootData.getJson().toString());
        if (mData.termNo.values.isEmpty())
            Toast.makeText(this, "服务端未配置终端列表！", Toast.LENGTH_SHORT).show();
        if (mData.reason.values.isEmpty())
            Toast.makeText(this, "服务端未配置原因列表！", Toast.LENGTH_SHORT).show();
        mTvCardNo.setText(mData.cardNo);
        mTvStaffNo.setText(mData.staffNo);
    }


    private String termNo = "";     //终端号
    private String reason = "";     //原因

    @Override
    public void onClick(View v) {
        super.onClick(v);
        hideKeyboard();
        if (v == timeRl) {
            if (!TextUtils.isEmpty(mDate)) {
                showHmTimeDialog();
            } else {
                showTimePicker();
            }
        } else if (v == reasonRl) {
            int select_intex = 0;
            if (mSelectReason.getTag() != null)
                select_intex = (int) mSelectReason.getTag();
            showPositionSelected(convertMapValueToList(mData == null ? null : mData.reason.values), mSelectReason, select_intex);
        } else if (v == terminalRl) {
            int select_intex = 0;
            if (mSelectZhongDuan.getTag() != null)
                select_intex = (int) mSelectZhongDuan.getTag();
            showPositionSelected(convertMapValueToList(mData == null ? null : mData.termNo.values), mSelectZhongDuan, select_intex);
        } else if (v == mTvRight) {//右上提交
            submit();
        }else if (v == tvSubmit){//下方提交
            submit();
        }
        else if (v == mIvHead) {
            Intent intent = new Intent(this, VantopUserInfoActivity.class);
            intent.putExtra(VantopUserInfoActivity.BUNDLE_STAFFNO, PrfUtils.getStaff_no(this));
            startActivity(intent);
        } else if (v == mIvAttachPicAdd) {
            Intent intent = new Intent(this, PicSelectActivity.class);
            intent.putExtra("single", true);
            startActivityForResult(intent, INTENT_SELECT_PIC);
        } else if (v == mIvAttachPicDel) {
            if (mRlAttachContain.getVisibility() != View.GONE) {
                mRlAttachContain.setVisibility(View.GONE);
            }
            mIvAttachPicAdd.setVisibility(View.VISIBLE);
            mIvAttachPic.setTag("");
        }

    }

    private void submit() {
        if ("".equals(mTvCardNo.getText().toString())) {
            Toast.makeText(this, getString(R.string.cardno_not_null), Toast.LENGTH_SHORT).show();
            return;
        }

        if (getString(R.string.please_select).equals(mSelectDate.getText().toString())) {
            Toast.makeText(this, getString(R.string.time_not_null), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mSelectReason.getTag() != null) {
            reason = convertMapKeyToList(mData.reason.values).get((int) mSelectReason.getTag());
        }

        if (mSelectZhongDuan.getTag() != null) {
            termNo = convertMapKeyToList(mData.termNo.values).get((int) mSelectZhongDuan.getTag());
        }
        if (!TextUtils.isEmpty(mDate)) {
            String time = mSelectDate.getText().toString().trim();
            submitSignedCard(mTvCardNo.getText().toString(), mDate + " " + time
                    , termNo, reason, mEditRemark.getText().toString());
        } else {
            submitSignedCard(mTvCardNo.getText().toString(), mSelectDate.getText().toString()
                    , termNo, reason, mEditRemark.getText().toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_SELECT_PIC && resultCode == Activity.RESULT_OK && data != null) {
            String url = data.getStringExtra("path");
            if (mRlAttachContain.getVisibility() != View.VISIBLE) {
                mRlAttachContain.setVisibility(View.VISIBLE);
                mIvAttachPicAdd.setVisibility(View.GONE);
            }
            Bitmap bitmap = Bimp.getimage(url);
            mIvAttachPic.setImageBitmap(bitmap);
            mIvAttachPic.setTag(url);
        }
    }

    private void submitSignedCard(String cardNo, String date, String termNo,
                                  String reason, String remark) {
        String path = VanTopUtils.generatorUrl(this, UrlAddr.URL_SIGNEDCARD_SUBMIT);
        FilePair picPair = null;
        Map<String, String> params = new HashMap<>();
        params.put("cardNo", cardNo);
        params.put("date", date);
        params.put("termNo", termNo);
        params.put("reason", reason);
        params.put("remark", remark);
        String picUrl = (String) mIvAttachPic.getTag();
        if (TextUtils.isEmpty(picUrl)) {
            params.put("picname", "");
            picPair = new FilePair("pic", null);
        } else {
            Bitmap bitmap = Bimp.getimage(picUrl);
            String picName = picUrl.substring(
                    picUrl.lastIndexOf("/") + 1,
                    picUrl.lastIndexOf("."));
            params.put("picname", picName + ".jpg");
            String picPath = FileUtils.saveBitmap(this, bitmap, picName, "jpg");
            picPair = new FilePair("pic", new File(picPath));
            bitmap.recycle();
            bitmap = null;
        }
        NetworkPath np = new NetworkPath(path, params, picPair, this, true);
        getApplicationProxy().getNetworkManager().load(CALLBACK_SUBMIT, np, this);
        mIvAttachPic.setTag("");
        showLoadingDialog(this, getString(R.string.vantop_submitdata), false);
    }

    private List convertMapValueToList(Map<String, String> map) {
        List<String> list = new ArrayList<>();
        if (map != null) {
            Set<String> set = map.keySet();
            for (String str : set) {
                list.add(map.get(str));
            }
        }
        return list;
    }

    public static List<String> convertMapKeyToList(Map<String, String> map) {
        List<String> list = new ArrayList<>();
        if (map != null) {
            for (String value : map.keySet()) {
                list.add(value);
            }
        }
        return list;
    }


    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(mEditRemark.getWindowToken(), 0);
        }
    }

    private void showHmTimeDialog() {
        final DateFullDialogView dialog = new DateFullDialogView(this, mSelectDate, "Hm", "time-hm");
        dialog.setButtonClickListener(new DateFullDialogView.ButtonClickListener() {
            @Override
            public void sureButtonOnClickListener(String time) {
                mSelectDate.setText(time);
                dialog.dismiss();
            }

            @Override
            public void cancelButtonOnClickListener() {

            }
        });
        dialog.show(mSelectDate);
    }

    private void showTimePicker() {
        DateFullDialogView dialog = new DateFullDialogView(this, mSelectDate, "full", "ymdhm");
        dialog.show(mSelectDate);
    }

//    private void showItemPicker(List<String> data, TextView tv, int select_intex) {
//
//        ItemPicker picker = new ItemPicker(this, tv, data, select_intex);
//        picker.showItemPicker();
//    }

    private void showPositionSelected(final List<String> data, final TextView tv, int select_intex) {
        ActionSheetDialog actionSheetDialog = new ActionSheetDialog(this)
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true);

        for (String s : data) {
            actionSheetDialog.addSheetItem(s, ActionSheetDialog.SheetItemColor.Blue,
                    new ActionSheetDialog.OnSheetItemClickListener() {
                        @Override
                        public void onClick(int which) {
                            String title = data.get(which);
                            tv.setText(title);
                            tv.setTag(which);
                        }
                    });
        }
        actionSheetDialog.show();
    }
}
