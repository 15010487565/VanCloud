package com.vgtech.vancloud.ui.module.workreport;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.vgtech.common.view.TabInfo;
import com.vgtech.common.view.TitleIndicator;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseFragment;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.adapter.MyPagerAdapter;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duke on 2015/9/10.
 */
public class WorkReportNewActivity extends SearchBaseActivity implements ViewPager.OnPageChangeListener {

    private int FRAGMENT_ONE = 0;
    private int FRAGMENT_TWO = 1;
    private int FRAGMENT_THREE = 2;
    int myTypeCurrentTab = 0;
    private int myTypeLastTab = -1;
    int subordinateTypeCurrentTab = 0;
    private int subordinateTypeLastTab = -1;

    private ArrayList<TabInfo> myTypeTabs = new ArrayList<TabInfo>();
    private ArrayList<TabInfo> subordinateTypeTabs = new ArrayList<TabInfo>();

    private MyPagerAdapter myTypeViewPagerAdapter = null;
    private MyPagerAdapter subordinateTypeViewPagerAdapter = null;

    private ViewPager myTypeViewPager;
    private ViewPager subordinateTypeViewPager;

    private TitleIndicator myTypeIndicator;
    private TitleIndicator subordinateTypeIndicator;

    private RelativeLayout myTypeLayout;
    private RelativeLayout subordinateTypeLayout;

    //1-自己，2-下属.
    String permission = "1";

    private LinearLayout typeGroup;
    private RelativeLayout myButtonLayout;
    private RelativeLayout subordinateButtonLayout;

//    private NoticeLayout noticeLayout;

    public static final String WORKREPORT_REFRESH = "WORKREPORT_REFRESH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTitleLayout();
        setTitleText(getString(R.string.my_work_report));

        initViews();

        initMyTypeViews();
        initSubordinateTypeViews();


    }

    @Override
    protected int getContentView() {
        return R.layout.work_report_layout;
    }

    @Override
    protected void onDestroy() {

        destroyPager(myTypeTabs, myTypeViewPagerAdapter, myTypeViewPager, myTypeIndicator);
        destroyPager(subordinateTypeTabs, subordinateTypeViewPagerAdapter, subordinateTypeViewPager, subordinateTypeIndicator);
        super.onDestroy();
    }


    private void initViews() {

        arrowView.setVisibility(View.VISIBLE);
        typeGroup = (LinearLayout) findViewById(R.id.type_group);
        typeGroup.setTag(true);
        typeGroup.setOnClickListener(this);

        myButtonLayout = (RelativeLayout) findViewById(R.id.my_button);
        subordinateButtonLayout = (RelativeLayout) findViewById(R.id.subordinate_button);

        myButtonLayout.setOnClickListener(this);
        subordinateButtonLayout.setOnClickListener(this);
        myButtonLayout.setSelected(true);

        myTypeLayout = (RelativeLayout) findViewById(R.id.my_type);
        subordinateTypeLayout = (RelativeLayout) findViewById(R.id.subordinate_type);

        myTypeLayout.setVisibility(View.VISIBLE);

    }


    private void initMyTypeViews() {
        myTypeCurrentTab = supplyMyTypeTabs(myTypeTabs);
        myTypeViewPagerAdapter = new MyPagerAdapter(this, getSupportFragmentManager(), myTypeTabs);
        myTypeViewPager = (ViewPager) findViewById(R.id.my_type_pager);
        myTypeViewPager.setAdapter(myTypeViewPagerAdapter);
        myTypeViewPager.addOnPageChangeListener(this);
        myTypeViewPager.setOffscreenPageLimit(myTypeTabs.size());
        myTypeIndicator = (TitleIndicator) findViewById(R.id.my_type_title);
        myTypeIndicator.init(myTypeCurrentTab, myTypeTabs, myTypeViewPager);
        myTypeViewPager.setCurrentItem(myTypeCurrentTab);
        myTypeLastTab = myTypeCurrentTab;
    }


    private void initSubordinateTypeViews() {
        subordinateTypeCurrentTab = supplysubordinateTypeTabs(subordinateTypeTabs);
        subordinateTypeViewPagerAdapter = new MyPagerAdapter(this, getSupportFragmentManager(), subordinateTypeTabs);
        subordinateTypeViewPager = (ViewPager) findViewById(R.id.subordinate_type_pager);
        subordinateTypeViewPager.setAdapter(subordinateTypeViewPagerAdapter);
        subordinateTypeViewPager.addOnPageChangeListener(this);
        subordinateTypeViewPager.setOffscreenPageLimit(subordinateTypeTabs.size());
        subordinateTypeIndicator = (TitleIndicator) findViewById(R.id.subordinate_type_title);
        subordinateTypeIndicator.init(subordinateTypeCurrentTab, subordinateTypeTabs, subordinateTypeViewPager);
        subordinateTypeViewPager.setCurrentItem(subordinateTypeCurrentTab);
        subordinateTypeLastTab = subordinateTypeCurrentTab;
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        if ("1".equals(permission)) {
            myTypeIndicator.onScrolled((myTypeViewPager.getWidth() + myTypeViewPager.getPageMargin()) * position + positionOffsetPixels);
        } else {
            subordinateTypeIndicator.onScrolled((subordinateTypeViewPager.getWidth() + subordinateTypeViewPager.getPageMargin()) * position + positionOffsetPixels);
        }

    }

    @Override
    public void onPageSelected(int position) {

        if ("1".equals(permission)) {
            myTypeIndicator.onSwitched(position);
            myTypeCurrentTab = position;

        } else {
            subordinateTypeIndicator.onSwitched(position);
            subordinateTypeCurrentTab = position;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

        if ("1".equals(permission)) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                myTypeLastTab = myTypeCurrentTab;
            }
        } else {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                subordinateTypeLastTab = subordinateTypeCurrentTab;
            }
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }


    public int supplyMyTypeTabs(List<TabInfo> tabs) {

        tabs.add(new TabInfo(FRAGMENT_ONE, getString(R.string.i_send_out),
                WorkReportISendOutFragment.class));
        tabs.add(new TabInfo(FRAGMENT_TWO, getString(R.string.i_comment_on),
                WorkReportICommentOnFragment.class));
        tabs.add(new TabInfo(FRAGMENT_THREE, getString(R.string.send_me),
                WorkReportSendMeFragment.class));

        return FRAGMENT_ONE;

    }

    public int supplysubordinateTypeTabs(List<TabInfo> tabs) {

        tabs.add(new TabInfo(FRAGMENT_ONE, getString(R.string.daily_paper),
                WorkReportDailyPaperFragment.class));
        tabs.add(new TabInfo(FRAGMENT_TWO, getString(R.string.weekly_paper),
                WorkReportWeeklyPaperFragment.class));
        tabs.add(new TabInfo(FRAGMENT_THREE, getString(R.string.monthly_paper),
                WorkReportMonthlyPaperFragment.class));

        return FRAGMENT_ONE;

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.top_type_click:

                if (typeGroup.getVisibility() == View.VISIBLE) {
                    hideTopTypeselectView();
                } else {
                    showTopTypeselectView();
                }
                break;

            case R.id.add:
                Intent intent = new Intent(this, NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_WORKREPORT);
                startActivity(intent);

                break;


            case R.id.title_shade:

                if (typeGroup.getVisibility() == View.VISIBLE) {
                    hideTopTypeselectView();
                }

                break;

            case R.id.shade_view:

                if (typeGroup.getVisibility() == View.VISIBLE) {
                    hideTopTypeselectView();
                }

                break;

            case R.id.my_button:

                if (!myButtonLayout.isSelected()) {
                    subordinateButtonLayout.setSelected(false);
                    myButtonLayout.setSelected(true);
                    setTitleText(getString(R.string.my_work_report));
                    hideTopTypeselectView();
                    permission = "1";
                    myTypeLayout.setVisibility(View.VISIBLE);
                    subordinateTypeLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.subordinate_button:

                if (!subordinateButtonLayout.isSelected()) {
                    myButtonLayout.setSelected(false);
                    subordinateButtonLayout.setSelected(true);
                    setTitleText(getString(R.string.subordinate_work_report));
                    hideTopTypeselectView();
                    permission = "2";
                    myTypeLayout.setVisibility(View.GONE);
                    subordinateTypeLayout.setVisibility(View.VISIBLE);
                }
                break;

            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    public void searchRequest() {
        BaseFragment baseFragment;
        if ("1".equals(permission)) {
            baseFragment = (BaseFragment) myTypeTabs.get(myTypeCurrentTab).getFragment();
        } else {
            baseFragment = (BaseFragment) subordinateTypeTabs.get(subordinateTypeCurrentTab).getFragment();
        }
        if (baseFragment != null) {
            baseFragment.searchRequest(serchContextView.getText().toString(), startTimeView.getText().toString(), endTimeView.getText().toString());
        }
    }

    /**
     * 显示顶部分类选择布局
     */
    public void showTopTypeselectView() {

        openAnimation(typeGroup, arrowView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                typeGroup.setTag(false);
                typeGroup.setVisibility(View.VISIBLE);
                titleShadeView.setVisibility(View.VISIBLE);
                shadeView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                typeGroup.setTag(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 隐藏顶部分类选择布局
     */
    public void hideTopTypeselectView() {


        closeAnimation(typeGroup, arrowView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                typeGroup.setTag(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                typeGroup.setTag(true);
                typeGroup.setVisibility(View.INVISIBLE);
                titleShadeView.setVisibility(View.GONE);
                shadeView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    private void destroyPager(ArrayList<TabInfo> tabInfos, MyPagerAdapter myAdapter, ViewPager viewPager, TitleIndicator titleIndicator) {

        tabInfos.clear();
        tabInfos = null;
        myAdapter.notifyDataSetChanged();
        myAdapter = null;
        viewPager.setAdapter(null);
        viewPager = null;
        titleIndicator = null;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {


            case 2000:
                if (resultCode == Activity.RESULT_OK) {
                    boolean refresh = data.getBooleanExtra("backRefresh", false);
                    if (refresh) {
                        Intent broadcIntent = new Intent(WORKREPORT_REFRESH);
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