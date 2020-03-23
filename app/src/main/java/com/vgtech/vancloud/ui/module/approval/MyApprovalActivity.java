package com.vgtech.vancloud.ui.module.approval;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vgtech.common.api.AppPermission;
import com.vgtech.common.utils.TenantPresenter;
import com.vgtech.common.view.NoScrollViewPager;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duke on 2016/9/23.
 * 审批休假、加班、签卡
 */

public class MyApprovalActivity extends BaseActivity {

    NoScrollViewPager viewPager;
    TabLayout tabLayout;
    private String mTag;

    private boolean isSelectAll = false;//默认未选择批量
    TextView rightTv;
    private Button btSelectAllRefuse,btSelectAllAgree;
    @Override
    protected int getContentView() {
        return R.layout.todo_notification_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = getIntent().getStringExtra("tag");
        rightTv = initRightTv(getString(R.string.batch));

        if (mTag.equals(AppPermission.Shenqing.shenqing_extra_work.toString())) {//加班
            setTitle(getString(R.string.vantop_overtime_apply));
        } else if (mTag.equals(AppPermission.Shenqing.shenqing_sign_card.toString())) {//签卡
            setTitle(getString(R.string.change_sign));
        } else {//休假
            setTitle(getString(R.string.leave));
        }
        initView();
    }

    public void initView() {

        viewPager = (NoScrollViewPager) findViewById(R.id.vp);
        viewPager.setNoScroll(false);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
//                固定标签宽度
        Class<?> tablayout = tabLayout.getClass();
        Field tabStrip = null;
        try {
            tabStrip = tablayout.getDeclaredField("mTabStrip");
            tabStrip.setAccessible(true);
            LinearLayout ll_tab = (LinearLayout) tabStrip.get(tabLayout);
            for (int i = 0; i < ll_tab.getChildCount(); i++) {
                View child = ll_tab.getChildAt(i);
                child.setPadding(0, 0, 0, 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                params.setMargins(dpToPx(30), 0, dpToPx(30), 0);
                child.setLayoutParams(params);
                child.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    VanTopApprovalListFragment vanTopApprovalListFragment;
    VanTopApprovalOkListFragment vanTopApprovalOkListFragment;
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        if (TenantPresenter.isVanTop(this)) {
            //待审批
            vanTopApprovalListFragment = VanTopApprovalListFragment.create("3", "2", 0, mTag);
            adapter.addFrag(vanTopApprovalListFragment, getString(R.string.waiting_approval));
            //已经审批
            vanTopApprovalOkListFragment = VanTopApprovalOkListFragment.create("4", "2", 1, mTag);
            adapter.addFrag(vanTopApprovalOkListFragment, getString(R.string.already_approval));
        } else {
            adapter.addFrag(VanCloudApprovalListFragment.create("3", "2", 0), getString(R.string.waiting_approval));
            adapter.addFrag(VanCloudApprovalListFragment.create("4", "2", 1), getString(R.string.already_approval));
        }
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // 把当前显示的position传递出去
//                Log.e("TAG_批量====position",""+position);
                if (position == 0){
//                    vanTopApprovalListFragment.onFragmentBottomVisibility(View.VISIBLE);
                    rightTv.setVisibility(View.VISIBLE);
                }else {
                  vanTopApprovalOkListFragment.onFragmentBottomVisibility(View.GONE);
                    rightTv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {

            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);

            mFragmentTitleList.add(title);
            switch (title) {
                case "wode":
                    break;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return mFragmentTitleList.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }

    private int dpToPx(int dps) {
        return Math.round(this.getResources().getDisplayMetrics().density * (float) dps);
    }

    public void chaneTitle(int position, int num) {

        if (0 == position) {
            if (num > 0)
                tabLayout.getTabAt(position).setText(getString(R.string.waiting_approval_num, num + ""));
            else
                tabLayout.getTabAt(position).setText(getString(R.string.waiting_approval));

        } else {
            if (num > 0)
                tabLayout.getTabAt(position).setText(getString(R.string.already_approval_num, num + ""));
            else
                tabLayout.getTabAt(position).setText(getString(R.string.already_approval));

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                if (isSelectAll) {//选择取消时，字体变为批量
                    vanTopApprovalListFragment.onFragmentBottomVisibility(View.GONE);
                    rightTv.setText(R.string.batch);

                    isSelectAll = false;
                } else {//点击批量时，字体变为取消
                    vanTopApprovalListFragment.onFragmentBottomVisibility(View.VISIBLE);
                    rightTv.setText(R.string.cancel);
                    isSelectAll = true;
                }
                break;

            default:
                super.onClick(v);
                break;
        }
    }
}
