package com.vgtech.vantop.ui.vacation;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.VacationBalance;
import com.vgtech.vantop.moudle.Vacations;
import com.vgtech.vantop.network.UrlAddr;
import com.vgtech.vantop.utils.VanTopActivityUtils;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 休假节余
 * Created by Duke on 2016/9/13.
 */
public class VacationBalanceActivity extends BaseActivity implements HttpListener<String> {

    private TextView headerTypeView;
    private TextView deadlineView;
    private TextView prevBalancesView;
    private TextView yearAllocView;
    private TextView allocView;
    private TextView adjustmentsView;
    private TextView yearUsedView;
    private TextView applyApprovedView;
    private TextView pendingApprovalView;
    private TextView mayDurationView;
    private TextView deniedApplyView;

    private Vacations vacation;
    private NetworkManager networkManager;


    @Override
    protected int getContentView() {
        return R.layout.vacation_balance_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.vantop_off_balance));
        String jsonTxt = getIntent().getStringExtra("data");
        try {
            if (!TextUtils.isEmpty(jsonTxt)) {
                initView();
                initEvent();
                vacation = JsonDataFactory.getData(Vacations.class, new JSONObject(jsonTxt));
                headerTypeView.setText(vacation.desc);
                initData(vacation.code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        initView();
    }


    public void initView() {
        headerTypeView = (TextView) findViewById(R.id.headerTypeView);
        deadlineView = (TextView) findViewById(R.id.deadlineView);
        prevBalancesView = (TextView) findViewById(R.id.prevBalancesView);
        yearAllocView = (TextView) findViewById(R.id.yearAllocView);
        allocView = (TextView) findViewById(R.id.allocView);
        adjustmentsView = (TextView) findViewById(R.id.adjustmentsView);
        yearUsedView = (TextView) findViewById(R.id.yearUsedView);
        applyApprovedView = (TextView) findViewById(R.id.applyApprovedView);
        pendingApprovalView = (TextView) findViewById(R.id.pendingApprovalView);
        mayDurationView = (TextView) findViewById(R.id.mayDurationView);
        deniedApplyView = (TextView) findViewById(R.id.deniedApplyView);
    }

    public void initEvent() {
        findViewById(R.id.adjustments_click).setOnClickListener(this);
        findViewById(R.id.yearused_click).setOnClickListener(this);
        findViewById(R.id.durationView).setOnClickListener(this);
        findViewById(R.id.apply_approved_click).setOnClickListener(this);
        findViewById(R.id.pending_approval_click).setOnClickListener(this);
        findViewById(R.id.denied_apply_click).setOnClickListener(this);
    }

    public void initData(String code) {
        showLoadingDialog(this, getString(R.string.dataloading));
        networkManager = getApplicationProxy().getNetworkManager();
        String url = VanTopUtils.generatorUrl(VacationBalanceActivity.this, UrlAddr.URL_VACATIONS_BALANCES);
        Map<String, String> params = new HashMap<String, String>();
        params.put("code",code);
        NetworkPath path = new NetworkPath(url, params, this, true);
        networkManager.load(1, path, this, false);
    }

    public void setViewData(VacationBalance vacationBalance) {
        deadlineView.setText(vacationBalance.date);
        prevBalancesView.setText(vacationBalance.lastBal + vacation.unit);
        yearAllocView.setText(vacationBalance.yearAssign + vacation.unit);
        allocView.setText(vacationBalance.curAssign + vacation.unit);
        adjustmentsView.setText(vacationBalance.adjNum + vacation.unit);
        yearUsedView.setText(vacationBalance.useNum + vacation.unit);
        applyApprovedView.setText(vacationBalance.approvedNum + vacation.unit);
        pendingApprovalView.setText(vacationBalance.approvingNum + vacation.unit);
        mayDurationView.setText(vacationBalance.balance+vacation.unit);
        deniedApplyView.setText(vacationBalance.refuseNum + " " + vacation.unit);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.adjustments_click) {
            Intent intent = new Intent(this, BalanceUseActivity.class);
            intent.putExtra("json", vacation.getJson().toString());
            startActivity(intent);
        } else if (v.getId() == R.id.yearused_click) {
            Intent intent = new Intent(this, BalanceUseActivity.class);
            intent.putExtra("type", true);
            intent.putExtra("json", vacation.getJson().toString());
            startActivity(intent);
        } else if (v.getId() == R.id.durationView) {
            Intent intent = new Intent(this, ApplyVacationActivity.class);
            intent.putExtra("json", vacation.getJson().toString());
            startActivity(intent);
        } else if (v.getId() == R.id.apply_approved_click) {
            Intent intent = new Intent(this, VacationApplyListActivity.class);
            intent.putExtra("type", 1);
            intent.putExtra("json", vacation.getJson().toString());
            startActivity(intent);
        } else if (v.getId() == R.id.pending_approval_click) {
            Intent intent = new Intent(this, VacationApplyListActivity.class);
            intent.putExtra("type", 0);
            intent.putExtra("json", vacation.getJson().toString());
            startActivity(intent);
        } else if (v.getId() == R.id.denied_apply_click) {
            Intent intent = new Intent(this, VacationApplyListActivity.class);
            intent.putExtra("type", 2);
            intent.putExtra("json", vacation.getJson().toString());
            startActivity(intent);
        } else {
            super.onClick(v);
        }

    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        boolean safe = VanTopActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            return;
        }
        switch (callbackId) {
            case 1:
                try {
                    VacationBalance vacationBalance = JsonDataFactory.getData(VacationBalance.class, rootData.getJson());
                    setViewData(vacationBalance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
    protected void onDestroy() {
        super.onDestroy();
        if (networkManager != null)
            networkManager.cancle(this);
    }
}
