package com.vgtech.vantop.ui.clockin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.ItemSelectedAdapter;
import com.vgtech.vantop.moudle.ClockInAppealReasonItem;
import com.vgtech.vantop.moudle.ItemSelectMoudle;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.ui.ItemSelectActivity;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 考勤异常申诉
 * create by scott
 */
public class ClockinExceptionActivity extends BaseActivity implements HttpListener<String> {

    private final int CALLBACK_LOADATA = 0X001;
    private final int CALLBACK_SUBMIT = 0X002;
    public static final String EXTRA_PARAMS = "params";
    private HashMap<String, String> mParams;

    private EditText mEtOther;

    private RadioButton mRadioFiexd;
    private RadioButton mRadiOther;
    private TextView mTvRight;
    private TextView mTvValue;

    private ItemSelectMoudle mSelected;
    private List<ClockInAppealReasonItem> mSelectDatas;
    public static final String EXTRA_RESAULT = "resault";
    private String mException;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //申请人的信息
        mParams = (HashMap<String, String>) getIntent().getSerializableExtra(EXTRA_PARAMS);
        if (mParams == null) {
            finish();
            return;
        }
        initDatas();
        initViews();
    }

    private void initDatas() {
        NetworkPath np = new NetworkPath(VanTopUtils.generatorUrl(this, UrlAddr.URL_CLOCKIN_APPEAL_REASON),null,this,true);
        NetworkManager nm = getApplicationProxy().getNetworkManager();
        nm.load(CALLBACK_LOADATA, np, this);
        showLoadingDialog(this, "", false);
    }

    private void initViews() {

        mEtOther = (EditText) findViewById(R.id.et_other);

        mTvRight = (TextView) findViewById(R.id.tv_submit);
       // mTvRight.setVisibility(View.VISIBLE);
        mTvRight.setText(getString(R.string.vantop_submit));
        mTvRight.setOnClickListener(this);

        //findViewById(R.id.iv_back).setVisibility(View.VISIBLE);

        mRadioFiexd = (RadioButton) findViewById(R.id.rb_fixed);
        mRadioFiexd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setRadioStaus(b);
                mRadiOther.setChecked(!b);
            }
        });
        // mRadioFiexd.setText(getString(R.string.vantop_fixed_inst));
        mRadiOther = (RadioButton) findViewById(R.id.rb_other);
        mRadiOther.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setRadioStaus(!b);
                mRadioFiexd.setChecked(!b);
            }
        });
        // mRadiOther.setText(getString(R.string.vantop_other));
        mTvValue = (TextView) findViewById(R.id.tv_value);
        mTvValue.setOnClickListener(this);
        setRadioStaus(true);
        mRadioFiexd.setChecked(true);
        setTitle(getString(R.string.vantop_exception1));
    }

    private void setRadioStaus(boolean isFixed) {
        if (isFixed) {
            mEtOther.setEnabled(false);
            mTvValue.setEnabled(true);
        } else {
            mEtOther.setEnabled(true);
            mTvValue.setEnabled(false);
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_clockin_exception;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v == mTvRight) {
            submitResault();
        }
        if (v == mTvValue) {
            if (mSelectDatas != null)
                showItemSelectActivity(mSelectDatas);
        }
    }

    private void submitResault() {

        String exception = "";
        boolean isFixed = true;
        String fixedKey = "";
        int id = mRadioFiexd.isChecked() ? mRadioFiexd.getId() : mEtOther.getId();
        //当为固定说明时
        if (id == R.id.rb_fixed) {
            isFixed = true;
            if(mSelected == null) {
                Toast.makeText(this,getString(R.string.vantop_select_null),Toast.LENGTH_SHORT).show();
                return;
            }
            //提交的为选中条目的代号而不是值
            fixedKey = mSelected.code;
            mException = mSelected.value;
            //当为其他异常时
        } else {
            isFixed = false;
            exception = mEtOther.getText().toString();
            if(TextUtils.isEmpty(exception)) {
                Toast.makeText(this,getString(R.string.vantop_select_null),Toast.LENGTH_SHORT).show();
                return;
            }
            mException = exception;
        }

        String path = VanTopUtils.generatorUrl(this,
                UrlAddr.URL_CLOCKIN_APPEAL);//mUrl.toString() + "&isFixed=" + isFixed + "&explain=" + exception + "&fixedExplainKey=" + fixedKey;
        mParams.put("isFixed", isFixed + "");
        mParams.put("explain", exception);
        mParams.put("fixedExplainKey", fixedKey);
        NetworkManager nm = getApplicationProxy().getNetworkManager();
        NetworkPath np = new NetworkPath(path, mParams, this,true);
        nm.load(CALLBACK_SUBMIT, np, this);
        showLoadingDialog(this,"",false);
    }

    /**
     * 显示item选择activity
     * @param list
     */
    private void showItemSelectActivity(List<ClockInAppealReasonItem> list) {

        ArrayList<ItemSelectMoudle> datas = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ItemSelectMoudle model = new ItemSelectMoudle();
            model.code = list.get(i).fixedKey;
            model.value = list.get(i).fixedValue;
            if (i != 0)
                model.isSelected = false;
            else
                model.isSelected = true;
            datas.add(model);
        }

        Intent intent = new Intent(this, ItemSelectActivity.class);
        intent.putExtra(ItemSelectActivity.EXTRA_DATA, datas);
        intent.putExtra(ItemSelectActivity.EXTRA_MODE,ItemSelectedAdapter.SELECTED_MODE_RADIOBTN);
        intent.putExtra(ItemSelectActivity.EXTRA_CHECK_MODE,ItemSelectedAdapter.CHECK_MODE_SINGLE);
        startActivityForResult(intent, 0);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        switch (callbackId) {
            case CALLBACK_LOADATA: {
                boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
                if (!safe) {
                    return;
                }
                JSONArray jArr = rootData.getJson().optJSONArray("data");
                mSelectDatas = JsonDataFactory.getDataArray(ClockInAppealReasonItem.class, jArr);
                dismisLoadingDialog();
            }
            break;

            case CALLBACK_SUBMIT: {
                boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
                if (safe) {
                    Intent data = new Intent();
                    data.putExtra(EXTRA_RESAULT,mException);
                    setResult(RESULT_OK,data);
                    Toast.makeText(this, getString(R.string.vantop_submit_success), Toast.LENGTH_SHORT).show();
                }
                dismisLoadingDialog();
                this.finish();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            ArrayList<ItemSelectMoudle> datas = (ArrayList<ItemSelectMoudle>) data.getSerializableExtra(ItemSelectActivity.EXTRA_RESAULT);
            mSelected = datas.get(0);
            if (mSelected != null && !TextUtils.isEmpty(mSelected.value))
                mTvValue.setText(mSelected.value);
        }
    }
}
