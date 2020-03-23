package com.vgtech.vancloud.ui.module.flow;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.vgtech.common.view.TabInfo;
import com.vgtech.common.view.TitleIndicator;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseFragment;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.module.recruit.JobCreateActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frances on 2015/9/11.
 */
public class FlowReportNewActivity extends SearchBaseActivity implements ViewPager.OnPageChangeListener {
    private static final String TAG = "LlhFragmentActivity";

    public static final String EXTRA_TAB = "tab";
    public static final String EXTRA_QUIT = "extra.quit";

    public static final int FRAGMENT_ONE = 0;
    public static final int FRAGMENT_TWO = 1;
    public static final int FRAGMENT_THREE = 2;

    public int mCurrentTab = 0;
    protected int mLastTab = -1;

    //存放选项卡信息的列表
    protected ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

    //viewpager adapter
    protected MyAdapter myAdapter = null;

    //viewpager
    protected ViewPager mPager;

    //选项卡控件
    protected TitleIndicator mIndicator;

//    private NoticeLayout noticeLayout;

    public TitleIndicator getIndicator() {
        return mIndicator;
    }

    public static final String FLOW_REFRESH = "FLOW_REFRESH";


    public class MyAdapter extends FragmentPagerAdapter {
        ArrayList<TabInfo> tabs = null;
        Context context = null;

        public MyAdapter(Context context, FragmentManager fm, ArrayList<TabInfo> tabs) {
            super(fm);
            this.tabs = tabs;
            this.context = context;
        }

        @Override
        public Fragment getItem(int pos) {
            Fragment fragment = null;
            if (tabs != null && pos < tabs.size()) {
                TabInfo tab = tabs.get(pos);
                if (tab == null)
                    return null;
                fragment = tab.createFragment();
            }
            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            if (tabs != null && tabs.size() > 0)
                return tabs.size();
            return 0;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            TabInfo tab = tabs.get(position);
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            tab.fragment = fragment;
            return fragment;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTitleLayout();
        setTitleText(getString(R.string.flow));

        initpagerViews();

        //设置viewpager内部页面之间的间距
//        mPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.page_margin_width));
        //设置viewpager内部页面间距的drawable
//        mPager.setPageMarginDrawable(R.color.page_viewer_margin_color);
    }

    @Override
    protected int getContentView() {
        return R.layout.flow_report_layout;
    }

    @Override
    protected void onDestroy() {
        mTabs.clear();
        mTabs = null;
        myAdapter.notifyDataSetChanged();
        myAdapter = null;
        mPager.setAdapter(null);
        mPager = null;
        mIndicator = null;

        super.onDestroy();
    }


    private final void initpagerViews() {
        // 这里初始化界面
        mCurrentTab = supplyTabs(mTabs);
        Intent intent = getIntent();
        if (intent != null) {
            mCurrentTab = intent.getIntExtra(EXTRA_TAB, mCurrentTab);
        }
        Log.d(TAG, "mTabs.size() == " + mTabs.size() + ", cur: " + mCurrentTab);
        myAdapter = new MyAdapter(this, getSupportFragmentManager(), mTabs);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(myAdapter);
        mPager.setOnPageChangeListener(this);
        mPager.setOffscreenPageLimit(mTabs.size());

        mIndicator = (TitleIndicator) findViewById(R.id.pagerindicator);
        mIndicator.init(mCurrentTab, mTabs, mPager);
//        mIndicator.setChangeOnClick(true);

        mPager.setCurrentItem(mCurrentTab);
        mLastTab = mCurrentTab;
    }

    /**
     * 添加一个选项卡
     *
     * @param tab
     */
    public void addTabInfo(TabInfo tab) {
        mTabs.add(tab);
        myAdapter.notifyDataSetChanged();
    }

    /**
     * 从列表添加选项卡
     *
     * @param tabs
     */
    public void addTabInfos(ArrayList<TabInfo> tabs) {
        mTabs.addAll(tabs);
        myAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mIndicator.onScrolled((mPager.getWidth() + mPager.getPageMargin()) * position + positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        mIndicator.onSwitched(position);
        mCurrentTab = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            mLastTab = mCurrentTab;
        }
    }

    protected TabInfo getFragmentById(int tabId) {
        if (mTabs == null) return null;
        for (int index = 0, count = mTabs.size(); index < count; index++) {
            TabInfo tab = mTabs.get(index);
            if (tab.getId() == tabId) {
                return tab;
            }
        }
        return null;
    }

    /**
     * 跳转到任意选项卡
     *
     * @param tabId 选项卡下标
     */
    public void navigate(int tabId) {
        for (int index = 0, count = mTabs.size(); index < count; index++) {
            if (mTabs.get(index).getId() == tabId) {
                mPager.setCurrentItem(index);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    /**
     * 在这里提供要显示的选项卡数据
     */
    public int supplyTabs(List<TabInfo> tabs) {

        tabs.add(new TabInfo(FRAGMENT_ONE, getString(R.string.my_self_flow),
                MySelfFlowFragment.class));
        tabs.add(new TabInfo(FRAGMENT_TWO, getString(R.string.myapproval_flow),
                MyApprovalFlowFragment.class));
        tabs.add(new TabInfo(FRAGMENT_THREE, getString(R.string.mentionmy_flow),
                MentionMyFlowFragment.class));

        return FRAGMENT_ONE;

    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {

            case R.id.add:

//                Toast.makeText(this, "流程", Toast.LENGTH_SHORT).show();
                showPopupWindow(v);
                break;
            case R.id.general_approval: {
                Intent intent = new Intent(this, NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_FLOW);
                startActivity(intent);
                if (popupWindow != null && popupWindow.isShowing())
                    popupWindow.dismiss();
            }
            break;
            case R.id.leave_alone: {
                Intent intent = new Intent(this, NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_FLOW_LEAVE);
                startActivity(intent);
                if (popupWindow != null && popupWindow.isShowing())
                    popupWindow.dismiss();
            }
            break;
            case R.id.recruit_plan: {
                Intent intent = new Intent(this, JobCreateActivity.class);
                intent.putExtra("type", "2");
                startActivity(intent);
                if (popupWindow != null && popupWindow.isShowing())
                    popupWindow.dismiss();
            }
            break;
            case R.id.shade_view:

                break;
        }
    }

    private PopupWindow popupWindow;

    /**
     * 弹出pupopwindow
     *
     * @param v
     */
    private void showPopupWindow(View v) {

        if (popupWindow == null) {
            View view = View.inflate(this, R.layout.add_popup_dialog, null);
            view.findViewById(R.id.general_approval).setOnClickListener(this);
            view.findViewById(R.id.leave_alone).setOnClickListener(this);
            view.findViewById(R.id.recruit_plan).setOnClickListener(this);
            popupWindow = new PopupWindow(view, convertDipOrPx(this, 150),
                    ViewGroup.LayoutParams.WRAP_CONTENT);// 创建一个PopuWidow对象
            popupWindow.setFocusable(true);// 使其聚集
            popupWindow.setOutsideTouchable(true);// 设置允许在外点击消失
            popupWindow.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.bg_pop_chat_select));// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
            popupWindow.update();
        }

        popupWindow.showAsDropDown(v, 0 - convertDipOrPx(this, 110), 0);

    }

    // dip--px
    public static int convertDipOrPx(Context context, int dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }

    @Override
    public void searchRequest() {
        BaseFragment baseFragment;
        baseFragment = (BaseFragment) mTabs.get(mCurrentTab).getFragment();
        if (baseFragment != null) {
            baseFragment.searchRequest(serchContextView.getText().toString(), startTimeView.getText().toString(), endTimeView.getText().toString());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case 2000:
                if (resultCode == Activity.RESULT_OK) {
                    boolean refresh = data.getBooleanExtra("backRefresh", false);
                    if (refresh) {
                        Intent broadcIntent = new Intent(FLOW_REFRESH);
                        sendBroadcast(broadcIntent);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

}
