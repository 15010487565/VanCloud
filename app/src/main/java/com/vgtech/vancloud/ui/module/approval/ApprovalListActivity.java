package com.vgtech.vancloud.ui.module.approval;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;

import com.vgtech.common.api.AppPermission;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vantop.ui.overtime.CreatedOverTimeActivity;
import com.vgtech.vantop.ui.signedcard.SignedCardAddActivity;
import com.vgtech.vantop.ui.vacation.MyVacationActivity;

/**
 * 申请休假页
 */
public class ApprovalListActivity extends BaseActivity {
    @Override
    protected int getContentView() {
        return R.layout.approval_list_activity_layout;
    }

    private String mTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View addBtn = findViewById(R.id.btn_right);
        addBtn.setVisibility(View.VISIBLE);
        addBtn.setOnClickListener(this);
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        mTag = intent.getStringExtra("tag");
        if(TextUtils.isEmpty(title))
        {
            switch (AppPermission.Shenqing.getType(mTag)) {
                case shenqing_extra_work: {
                    title = getString(R.string.overtime);
                }
                break;
                case shenqing_sign_card: {
                    title = getString(R.string.change_sign);
                }
                break;
                case shenqing_vantop_holiday: {
                    title = getString(R.string.leave);
                }
                break;
            }
        }
        setTitle(title);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.content_layout, VanTopApplylListFragment.create("0", "1", 0, mTag));
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_right:
                AppPermission.Shenqing type = AppPermission.Shenqing.getType(mTag);

                switch (type) {
                    case shenqing_extra_work: {//加班
                        Intent intent = new Intent(this, CreatedOverTimeActivity.class);
                        startActivity(intent);
                    }
                    break;
                    case shenqing_sign_card: {//签卡
                        Intent intent = new Intent(this, SignedCardAddActivity.class);
                        startActivity(intent);
                    }
                    break;
                    case shenqing_vantop_holiday: {//休假
                        Intent intent = new Intent(this, MyVacationActivity.class);
                        startActivity(intent);
                    }
                    break;
                }
                break;
            default:
                super.onClick(v);
                break;
        }
    }

}
