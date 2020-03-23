package com.vgtech.vancloud.ui.module.schedule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vgtech.common.view.DateFullDialogView;
import com.vgtech.common.view.TabInfo;
import com.vgtech.common.view.TitleIndicator;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseFragment;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.adapter.MyPagerAdapter;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.module.schedule.fragment.MyselfScheduleFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ScheduleFragmentActivity extends SearchBaseActivity implements View.OnClickListener
        , ViewPager.OnPageChangeListener {

    View shadeView;
    ImageView backView;
    ImageView addView;

    LinearLayout topTypeClickLayout;
    ImageView arrowView;

    LinearLayout titleTextView;
    View titleShadeView;

    ImageView searchView;
    RelativeLayout searchLayout;
    ImageView searchCancelView;
    TextView cancelView;

    TextView advancedView;
    ImageView advancedArrowView;
    EditText serchContextView;

    TextView startTimeView;
    TextView endTimeView;
    private TextView tvDate;
    private View mWaitView;

    private int FRAGMENT_ONE = 0;
    private int FRAGMENT_TWO = 1;
    private int FRAGMENT_THREE = 2;
    private int FRAGMENT_FOUR = 3;
    private int myTypeCurrentTab = 0;

    private MyPagerAdapter myTypeViewPagerAdapter = null;
    private MyPagerAdapter subordinateTypeViewPagerAdapter = null;
    private String permission = "1";

    private ViewPager subordinateTypeViewPager;

    private TitleIndicator myTypeIndicator;
    private TitleIndicator subordinateTypeIndicator;

    private int myTypeLastTab = -1;
    public int subordinateTypeCurrentTab = 0;
    private int subordinateTypeLastTab = -1;
    private ArrayList<TabInfo> myTypeTabs = new ArrayList<TabInfo>();
    private ArrayList<TabInfo> subordinateTypeTabs = new ArrayList<TabInfo>();

    public static final int MY_SELF = 123;
    public static final int SUBORDINATE = MY_SELF << 1;
    public int fragmentLoadType = MY_SELF;

    //    private NoticeLayout noticeLayout;
    private boolean mHasEmployee;

    protected void onCreate(Bundle savedInstanceState) {
        mHasEmployee = getIntent().getBooleanExtra("hasEmployee", false);
        super.onCreate(savedInstanceState);
        initView();
        initData();
        initEvent();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected int getContentView() {
        return mHasEmployee?R.layout.schedule_list_layout:R.layout.schedule_list_layout_noemployee;
    }

    protected void initView() {

        shadeView = this.findViewById(R.id.shade_view);

        backView = (ImageView) this.findViewById(R.id.back);
        addView = (ImageView) this.findViewById(R.id.add);
        tvDate = (TextView) findViewById(R.id.tv_date);


        topTypeClickLayout = (LinearLayout) this.findViewById(R.id.top_type_click);
        arrowView = (ImageView) this.findViewById(R.id.arrow);
        titleTextView = (LinearLayout) this.findViewById(R.id.top_type_click);
        titleShadeView = this.findViewById(R.id.title_shade);

        searchView = (ImageView) this.findViewById(R.id.search);
        cancelView = (TextView) this.findViewById(R.id.cancel);
        searchLayout = (RelativeLayout) this.findViewById(R.id.search_layout);
        searchCancelView = (ImageView) this.findViewById(R.id.search_cancel);

        advancedView = (TextView) this.findViewById(R.id.advanced);
        advancedArrowView = (ImageView) this.findViewById(R.id.advanced_arrow);

        serchContextView = (EditText) this.findViewById(R.id.serch_context);

        serchContextView = (EditText) findViewById(R.id.serch_context);
        startTimeView = (TextView) findViewById(R.id.start_time);
        endTimeView = (TextView) findViewById(R.id.end_time);

        initSubordinateTypeViews();
        initTitleLayout();

//        noticeLayout = (NoticeLayout) findViewById(R.id.layout_notice);
    }


    protected void initData() {
        mWaitView = getLayoutInflater().inflate(R.layout.progress, null);
        mWaitView.findViewById(R.id.btn_retry).setOnClickListener(this);
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format_for_schedule_list_tittle_time));
        tvDate.setText(sdf.format(System.currentTimeMillis()));
    }


    protected void initEvent() {

        backView.setOnClickListener(this);
        addView.setOnClickListener(this);
        searchView.setOnClickListener(this);
        cancelView.setOnClickListener(this);
        searchLayout.setOnClickListener(this);
        advancedView.setOnClickListener(this);
        searchCancelView.setOnClickListener(this);

        serchContextView.setOnClickListener(this);
        startTimeView.setOnClickListener(this);
        endTimeView.setOnClickListener(this);

        findViewById(R.id.cancle_button).setOnClickListener(this);
        findViewById(R.id.confirm_button).setOnClickListener(this);

        serchContextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!serchContextView.getText().toString().equals("")) {
                    searchCancelView.setVisibility(View.VISIBLE);
                } else {
                    searchCancelView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.back:
//                finish();
//                break;
            case R.id.add:

                Intent intent = new Intent(this, NewPublishedActivity.class);
                intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_SCHEDULE);
                startActivity(intent);

                break;

//            case R.id.search:
//                searchLayout.setVisibility(View.VISIBLE);
//                addView.setVisibility(View.INVISIBLE);
//                searchView.setVisibility(View.INVISIBLE);
//                cancelView.setVisibility(View.VISIBLE);
//                titleTextView.setVisibility(View.GONE);
//
//                break;
            case R.id.title_shade:

                break;
//            case R.id.start_time:
//                showDateDialogview(startTimeView);
//                break;
//            case R.id.end_time:
//                showDateDialogview(endTimeView);
//                break;
//            case R.id.confirm_button:
//                hideAdvancedSearchLayout();
//                ((ScheduleListLoadFragment)subordinateTypeTabs.get(subordinateTypeCurrentTab).fragment).load(startTimeView.getText().toString(),endTimeView.getText().toString());
//                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void initSubordinateTypeViews() {
        subordinateTypeCurrentTab = supplysubordinateTypeTabs(subordinateTypeTabs);
        subordinateTypeViewPagerAdapter = new MyPagerAdapter(this, getSupportFragmentManager(), subordinateTypeTabs);
        subordinateTypeViewPager = (ViewPager) findViewById(R.id.subordinate_type_pager);
        subordinateTypeViewPager.setAdapter(subordinateTypeViewPagerAdapter);
        subordinateTypeViewPager.addOnPageChangeListener(this);
        subordinateTypeViewPager.setOffscreenPageLimit(subordinateTypeTabs.size());
        subordinateTypeIndicator = (TitleIndicator) findViewById(R.id.subordinate_type_title);
        if (mHasEmployee)
            subordinateTypeIndicator.init(subordinateTypeCurrentTab, subordinateTypeTabs, subordinateTypeViewPager, 3);
        else
            subordinateTypeIndicator.init(subordinateTypeCurrentTab, subordinateTypeTabs, subordinateTypeViewPager);
        subordinateTypeViewPager.setCurrentItem(subordinateTypeCurrentTab);
        subordinateTypeLastTab = subordinateTypeCurrentTab;
    }

    public int supplysubordinateTypeTabs(List<TabInfo> tabs) {

        tabs.add(new TabInfo(FRAGMENT_ONE, getString(R.string.myself_subtitle),
                MyselfScheduleFragment.class, 5));
        tabs.add(new TabInfo(FRAGMENT_TWO, getString(R.string.mywith_subtitle),
                MyselfScheduleFragment.class, 6));
        if (mHasEmployee)
            tabs.add(new TabInfo(FRAGMENT_THREE, getString(R.string.subordinate_subtitle),
                    MyselfScheduleFragment.class, 7));
        tabs.add(new TabInfo(FRAGMENT_FOUR, getString(R.string.cancle_order),
                MyselfScheduleFragment.class, 8));

        return FRAGMENT_ONE;

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        subordinateTypeIndicator.onScrolled((subordinateTypeViewPager.getWidth() + subordinateTypeViewPager.getPageMargin()) * position + positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        subordinateTypeIndicator.onSwitched(position);
        subordinateTypeCurrentTab = position;
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

    public void showDateDialogview(TextView textView) {
        String dateS = textView.getText().toString();
        Calendar calendar = null;
        if (!TextUtils.isEmpty(dateS)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                Date parse = dateFormat.parse(dateS);
                calendar = Calendar.getInstance();
                calendar.setTime(parse);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }

        DateFullDialogView dateDialogview = new DateFullDialogView(this,
                textView, "full", "ymdhm", calendar, getResources().getColor(R.color.text_black));//年月日时分秒 当前日期之后选择
        dateDialogview.show(textView);


    }

    public void setTime(String time) {
        tvDate.setText(time);
    }

    @Override
    public void searchRequest() {
        BaseFragment baseFragment;
        baseFragment = (BaseFragment) subordinateTypeTabs.get(subordinateTypeCurrentTab).getFragment();
        if (baseFragment != null) {
            baseFragment.searchRequest(serchContextView.getText().toString(), startTimeView.getText().toString(), endTimeView.getText().toString());
        }
    }

    public interface ScheduleListLoadFragment {
        void load(String startTime, String endTime);
    }

    public interface OnTimeSearchListener {
        void timeJump(String time);
    }

    @Override
    public boolean onSearchRequested() {
        return super.onSearchRequested();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2000:
                if (resultCode == Activity.RESULT_OK) {
                    boolean refresh = data.getBooleanExtra("backRefresh", false);

                    if (refresh) {
                        BaseFragment baseFragment;
                        baseFragment = (BaseFragment) subordinateTypeTabs.get(subordinateTypeCurrentTab).getFragment();
                        if (baseFragment != null) {
                            baseFragment.toRefresh();
                        }
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Actions.ACTION_SHOW_MORE_VIEW));
    }

}
