package com.vgtech.vantop.ui;

import android.content.BroadcastReceiver;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.utils.wheel.WheelUtil;
import com.vgtech.common.view.DateFullDialogView;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vantop.R;
import com.vgtech.vantop.ui.punchcard.AttendenceActivity;
import com.vgtech.vantop.ui.punchcard.OperationType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 搜索基类
 * Created by shilec on 2016/9/5.
 */
public abstract class SearchActivity extends BaseActivity {

    public static final String RESTOR_SEARCH = "restor_search";
    private ImageButton mBtnSearch;
    private View mShadeView;

    private LinearLayout mOPtion;
    private TextView mTvOptionTitle;
    private TextView mTvOptionContent;

    private LinearLayout mOPtion1;
    private TextView mTvOptionTitle1;
    private TextView mTvOptionContent1;

    private LinearLayout mSelectTimeView;
    private TextView mTvSelectTimeContent;

    private LinearLayout mTvStartTimeView;
    private TextView mTvStartTimeContent;

    private LinearLayout mTvEndTimeView;
    private TextView mTvEndTimeContent;

    private TextView mBtnCancel;
    private TextView mBtnConfirm;

    private View mSearchView;

    //年月日
    public final String DIALOG_DATEALL_STYLE = "YMD";
    //年月
    public final String DIALOG_DATEMOUNTH_STYLE = "else";
    private String mDateStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        initView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRestorSearch(String tag) {
        if (!TextUtils.isEmpty(tag) && tag.equals(RESTOR_SEARCH)) {
            mTvStartTimeContent.setText(R.string.vantop_nothing);
            mTvEndTimeContent.setText(R.string.vantop_nothing);
        }
    }

    public void setSearchVisiable(boolean flag) {
        if (mBtnSearch != null)
            mBtnSearch.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    public boolean getSearchVisisable() {
        return mBtnSearch.getVisibility() == View.VISIBLE ? true : false;
    }

    private void initView() {

        //搜索按钮
        mBtnSearch = (ImageButton) findViewById(R.id.btn_right);
        mBtnSearch.setImageResource(R.mipmap.top_search);
        mBtnSearch.setOnClickListener(this);
        //
        mShadeView = findViewById(R.id.shade_view);
        mShadeView.setOnClickListener(this);

        //扩展条目1
        mOPtion = (LinearLayout) findViewById(R.id.new_option);
        mTvOptionTitle = (TextView) findViewById(R.id.new_option_title);
        mTvOptionContent = (TextView) findViewById(R.id.new_option_content);
        mTvOptionContent.setOnClickListener(this);
        //扩展条目二
        mOPtion1 = (LinearLayout) findViewById(R.id.new_option1);
        mTvOptionTitle1 = (TextView) findViewById(R.id.new_option_title1);
        mTvOptionContent1 = (TextView) findViewById(R.id.new_option_content1);
        mTvOptionContent1.setOnClickListener(this);
        //单选时间条目
        mSelectTimeView = (LinearLayout) findViewById(R.id.timeYM_ll);
        mTvSelectTimeContent = (TextView) findViewById(R.id.timeYm);
        mTvSelectTimeContent.setOnClickListener(this);
        //开始时间
        mTvStartTimeView = (LinearLayout) findViewById(R.id.start_time_ll);
        mTvStartTimeContent = (TextView) findViewById(R.id.start_time);
        mTvStartTimeContent.setOnClickListener(this);
        //结束时间
        mTvEndTimeView = (LinearLayout) findViewById(R.id.end_time_ll);
        mTvEndTimeContent = (TextView) findViewById(R.id.end_time);
        mTvEndTimeContent.setOnClickListener(this);

        mBtnCancel = (TextView) findViewById(R.id.cancle_button);
        mBtnConfirm = (TextView) findViewById(R.id.confirm_button);
        mBtnCancel.setOnClickListener(this);
        mBtnConfirm.setOnClickListener(this);

        mSearchView = findViewById(R.id.v_search);

        initViewVisisable();
        mSearchView.setBackgroundColor(Color.parseColor("#ffffff"));

        //test
        initContentView();
    }

    protected void setOptionItem(String title, String defValue) {
        mOPtion.setVisibility(View.VISIBLE);
        mTvOptionTitle.setText(title);
        mTvOptionContent.setText(defValue);
    }

    protected void setOptionItemGone() {
        mOPtion.setVisibility(View.GONE);
    }

    protected void initContentView() {

        int resId = initLayout();
        ViewGroup v = (ViewGroup) findViewById(R.id.ll_container);
        LayoutInflater.from(this).inflate(resId, v, true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public boolean isRight = false;

    /**
     * 显示一个带日期校验的日期选择对话框
     *
     * @param contentView 显示日期的TextView
     * @param nowDate     当前默认要显示的日期
     * @param isYmd       是否显示为年月日格式
     * @param isStart     当为开始结束时，改标记为开始时间:true
     */
    private void showDatePicker(final TextView contentView,
                                final String nowDate, final boolean isYmd, final boolean isStart) {
        final SimpleDateFormat sdf = isYmd ? new SimpleDateFormat("yyyy-MM-dd") : new SimpleDateFormat("yyyy-MM");
        Calendar cal = Calendar.getInstance();
        //初始化显示的日期
        if (TextUtils.isEmpty(nowDate)) {
            cal.setTime(new Date(System.currentTimeMillis()));
        } else {
            try {
                cal.setTime(sdf.parse(nowDate));
            } catch (ParseException e) {
                cal.setTime(new Date(System.currentTimeMillis()));
            }
        }
        isRight = false;
        final String default1 = mTvStartTimeContent.getText().toString();
        final String default2 = mTvEndTimeContent.getText().toString();
        final DateFullDialogView dv = new DateFullDialogView(this, contentView, isYmd ? "YMD" : "else", "date", cal) {

            @Override
            public void onClick(View v) {
                super.onClick(v);
                if (v.getId() == R.id.btn_cancel) {
                    /*if (isYmd) {
                        startAndEndtimeSelect();
                    } else {
                        singleSelect();
                    }*/
                    isRight = true;
                    dismiss();
                }
            }

            @Override
            public void dismiss() {
                //当校验验通过时才能消失
                if (isRight)
                    super.dismiss();
            }
        };
        dv.setOnSelectedListener(new DateFullDialogView.OnSelectedListener() {
            @Override
            public void onSelectedListener(long time) {
                if (!isYmd) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                    mTvOptionContent.setText(sdf.format(new Date(time)));
                    isRight = true;
                    Toast.makeText(SearchActivity.this, sdf.format(new Date(time)), Toast.LENGTH_SHORT).show();
                    dv.dismiss();
                    return;
                }
//                if (isStart ? (time - parseDate(compareDate, true)) <= 0 :
//                        (time - parseDate(compareDate, true)) >= 0) {
                if (isStart) {
                    mTvStartTimeContent.setText(getFormatYmdDate(time));
                } else {
                    mTvEndTimeContent.setText(getFormatYmdDate(time));
                }
                isRight = true;
                dv.dismiss();
//                } else {
//                    int resId = isStart ? R.string.datefulldialog_info_starttime :
//                            R.string.datefulldialog_info_endtime;
//                    Toast.makeText(SearchActivity.this, getString(resId), Toast.LENGTH_SHORT).show();
//                    if (isStart) {
//                        mTvStartTimeContent.setText(default1);
//                    } else {
//                        mTvEndTimeContent.setText(default2);
//                    }
//                    isRight = false;
//                    return;
//                }
            }
        });
        dv.show(contentView);
    }

    private String getFormatYmdDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date(time));
    }

    private int compareDate(String d1, String d2, SimpleDateFormat sdf) {
        try {
            Date dd1 = sdf.parse(d1);
            Date dd2 = sdf.parse(d2);

            long ld1 = dd1.getTime();
            long ld2 = dd2.getTime();
            return (int) (ld1 - ld2);
        } catch (ParseException e) {
            return 0;
        }
    }

    private long parseDate(String date, boolean isYmd) {
        SimpleDateFormat sdf = isYmd ? new SimpleDateFormat("yyyy-MM-dd") : new SimpleDateFormat("yyyy-MM");
        return parseDate(date, sdf);
    }

    private long parseDate(String date, SimpleDateFormat sdf) {
        try {
            return sdf.parse(date).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    /**
     * 获取前一个月的日期(不计算天，默认前一个月的天和当前日期的天数相同)
     *
     * @param nowDate
     * @param isYmd
     * @return
     */
    private String getLastMonth(String nowDate, boolean isYmd) {
        SimpleDateFormat sdf = isYmd ? new SimpleDateFormat("yyyy-MM-dd")
                : new SimpleDateFormat("yyyy-MM");
        long time = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000l);
        Date date = new Date(time);
        String str = sdf.format(date);
        try {
            str = str.substring(0, str.lastIndexOf("-") + 1) + getNowDate(true).split("-")[2];
        } catch (Exception e) {
            str = getNowDate(true);
        }
        return str;
    }

    private String getDate(DateFullDialogView dv, boolean isYmd) {
        Class cls = DateFullDialogView.class;
        try {
            //通过WheelUtil的方法getDateTime获取当前选中的日期
            Field mWheel = cls.getDeclaredField("mWheel");
            mWheel.setAccessible(true);
            WheelUtil util = (WheelUtil) mWheel.get(dv);
            //获取WheelUtil对象
            cls = WheelUtil.class;
            //执行getDateTime方法
            Method m = cls.getDeclaredMethod("getDateTime");
            m.setAccessible(true);
            String date = (String) m.invoke(util);
            return date;
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf;
        if (isYmd) {
            sdf = new SimpleDateFormat("yyyy-MM-dd");
        } else {
            sdf = new SimpleDateFormat("yyyy-MM");
        }
        return sdf.format(new Date(System.currentTimeMillis()));
    }


    private void initViewVisisable() {

        mBtnSearch.setVisibility(View.VISIBLE);
        mSearchView.setVisibility(View.INVISIBLE);
        mShadeView.setVisibility(View.GONE);
    }

    @Override
    protected int getContentView() {
        return R.layout.searchactivity_layout;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.btn_right) {
            expandSearchView(mSearchView.getVisibility() == View.VISIBLE ? false : true);
        }

        if (v.getId() == R.id.start_time) {
            showDatePicker(mTvStartTimeContent,
                    mTvStartTimeContent.getText().toString(), true, true);
//            showDatePicker(mTvStartTimeContent, mTvEndTimeContent.getText().toString(),
//                    getLastMonth(getNowDate(true), true), true, true);
        }

        if (v.getId() == R.id.timeYm) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(System.currentTimeMillis()));
            DateFullDialogView dialog = new DateFullDialogView(this,
                    mTvSelectTimeContent, mDateStyle, "date", Calendar.getInstance(),
                    getResources().getColor(R.color.text_black), Calendar.getInstance());//年月
            dialog.setOnSelectedListener(new DateFullDialogView.OnSelectedListener() {
                @Override
                public void onSelectedListener(long time) {

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                    Date date = new Date(time);
                    mTvSelectTimeContent.setText(sdf.format(date));
                }
            });
            dialog.show(mTvSelectTimeContent);
        }

        if (v.getId() == R.id.end_time) {
            showDatePicker(mTvEndTimeContent,
                    mTvEndTimeContent.getText().toString(), true, false);
        }

        if (v.getId() == R.id.cancle_button) {
            if (mTvStartTimeView.getVisibility() == View.VISIBLE) {
                mTvStartTimeContent.setText(R.string.vantop_nothing);
                mTvEndTimeContent.setText(R.string.vantop_nothing);
                startAndEndtimeSelect();
            } else {
                mTvSelectTimeContent.setText(R.string.vantop_nothing);
                singleSelect();
            }
            expandSearchView(false);
            mSearchView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    search(""
                            , "",
                            "",
                            "");
                }
            }, 300);
        }

        if (v.getId() == R.id.confirm_button) {

            if (TextUtils.equals(getResources().getString(R.string.vantop_nothing), mTvOptionContent.getText())) {
                return;
            } else {

//                if (mTvStartTimeView.getVisibility() == View.VISIBLE && TextUtils.isEmpty(mTvStartTimeContent.getText())) {
//                    ToastUtil.toast(this, R.string.choose_start_time);
//                    return;
//                }
//                if (mTvEndTimeView.getVisibility() == View.VISIBLE && TextUtils.isEmpty(mTvEndTimeContent.getText())) {
//                    ToastUtil.toast(this, R.string.choose_end_time);
//                    return;
//                }

                final String time = TextUtils.equals(mDateStyle, DIALOG_DATEALL_STYLE) ?
                        mTvStartTimeContent.getText().toString() : mTvSelectTimeContent
                        .getText().toString();
                if (TextUtils.equals(mDateStyle, DIALOG_DATEALL_STYLE)) {
                    String start = mTvStartTimeContent.getText().toString();
                    String end = mTvEndTimeContent.getText().toString();
                    if (!TextUtils.isEmpty(start) && !start.equals(getResources().getString(R.string.vantop_nothing))
                            && !TextUtils.isEmpty(end) && !end.equals(getResources().getString(R.string.vantop_nothing))) {
                        try {
                            long startDate = parseDate(start, true);
                            long endDate = parseDate(end, true);
                            if (endDate < startDate) {
                                Toast.makeText(SearchActivity.this, getString(R.string.datefulldialog_info_endtime), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (Exception e) {

                        }
                    }
                }
                expandSearchView(false);
                mSearchView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        search(time
                                , mTvEndTimeContent.getText().toString(),
                                mTvOptionContent.getText().toString(),
                                mTvOptionContent1.getText().toString());
                    }
                }, 300);
            }

        }
        if (v == mShadeView) {
            expandSearchView(false);
        }
    }

    protected abstract void search(String startTime, String endTime, String option1, String option2);

    protected abstract int initLayout();

    private void expandSearchView(final boolean isExpand) {

        //展开后无法展开，
        if ((mSearchView.getVisibility() == View.VISIBLE && isExpand)
                || (mSearchView.getVisibility() != View.VISIBLE && !isExpand)) {
            return;
        }
        float fromY = isExpand ? -mSearchView.getHeight() : 0;
        float toY = isExpand ? 0 : -mSearchView.getHeight();
        TranslateAnimation tAnim = new TranslateAnimation(
                mSearchView.getX(),
                mSearchView.getX(),
                fromY,
                toY
        );
        tAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

                if (isExpand) {
                    mSearchView.setVisibility(View.VISIBLE);
                    setShadeViewVisisable(true);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (!isExpand) {
                    mSearchView.setVisibility(View.INVISIBLE);
                    //mTvEndTimeContent.setText("无");
                    //mTvStartTimeContent.setText("无");
                    setShadeViewVisisable(false);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        tAnim.setDuration(300);
        mSearchView.startAnimation(tAnim);
    }

    private void setShadeViewVisisable(boolean isVisiavle) {

        if (isVisiavle && mShadeView.getVisibility() == View.VISIBLE
                || !isVisiavle && mShadeView.getVisibility() != View.VISIBLE) {
            return;
        }
        mShadeView.setVisibility(isVisiavle ? View.VISIBLE : View.GONE);
    }

    /***
     * 日期选择为单一的日期选择
     */
    private void singleSelect() {
        mTvEndTimeView.setVisibility(View.GONE);
        mTvStartTimeView.setVisibility(View.GONE);
        mSelectTimeView.setVisibility(View.VISIBLE);
        mDateStyle = DIALOG_DATEMOUNTH_STYLE;
        if (!TextUtils.isEmpty(mTvSelectTimeContent.getText().toString())) {
//            mTvSelectTimeContent.setText(getNowDate(false));
            setOptionItem(getString(R.string.lable_sign_status), getResources().getStringArray(R.array.sign_status)[0]);
        }
    }

    /***
     * 日期选择为开始时间和结束时间的菜单
     */
    private void startAndEndtimeSelect() {

        if (this instanceof AttendenceActivity) {
            mTvStartTimeView.setVisibility(View.VISIBLE);
            mTvEndTimeView.setVisibility(View.VISIBLE);
//            mTvStartTimeContent.setText(DataUtils.getFirstDayOfMonth());
//            mTvEndTimeContent.setText(DataUtils.getLastDayOfMonth());
            mSelectTimeView.setVisibility(View.GONE);
            mDateStyle = DIALOG_DATEALL_STYLE;
        } else {
            mTvStartTimeView.setVisibility(View.VISIBLE);
            mTvEndTimeView.setVisibility(View.VISIBLE);
//            long time = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000l);
//            Date date = new Date(time);
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            String str = sdf.format(date);
//            try {
//                str = str.substring(0, str.lastIndexOf("-") + 1) + getNowDate(true).split("-")[2];
//            } catch (Exception e) {
//                str = getNowDate(true);
//            }
            mSelectTimeView.setVisibility(View.GONE);
            mDateStyle = DIALOG_DATEALL_STYLE;
        }

    }

    public void setSearchStyle(String style) {

        if (TextUtils.equals(style, DIALOG_DATEMOUNTH_STYLE)) {
            singleSelect();
        } else {
            startAndEndtimeSelect();
        }
    }

    public String getNowDate(boolean isYmd) {
        SimpleDateFormat sdf;
        if (isYmd) {
            sdf = new SimpleDateFormat("yyyy-MM-dd");
        } else {
            sdf = new SimpleDateFormat("yyyy-MM");
        }
        long lDate = System.currentTimeMillis();
        return sdf.format(new Date(lDate));
    }

    /***
     * 考勤列表  和 打卡记录 显示数据
     *
     * @param type     刷新 加载 初始化 reload
     * @param datas    listview 绑定数据
     * @param nowDatas 当前加载出的数据
     * @param listView
     * @param infoView loadview
     * @param adapter
     */
    public void bsShowData(OperationType type, List datas, List nowDatas,
                           PullToRefreshListView listView, VancloudLoadingLayout infoView, BaseAdapter adapter) {
        if (type == OperationType.INIT) {
            if (nowDatas != null && nowDatas.isEmpty()) {
                datas.clear();
                adapter.notifyDataSetChanged();
                infoView.showEmptyView(listView, getString(R.string.vantop_no_list_data), true, true);
                return;
            } else if (nowDatas != null && !nowDatas.isEmpty()) {
                datas.clear();
                datas.addAll(nowDatas);
                adapter.notifyDataSetChanged();
                return;
            }
            //搜素
        } else if (type == OperationType.SEARCH) {
            if (nowDatas != null && nowDatas.isEmpty()) {
                datas.clear();
                adapter.notifyDataSetChanged();
                infoView.showEmptyView(listView, getString(R.string.vantop_no_list_data), true, true);
                return;
            } else if (nowDatas != null) {
                datas.clear();
                datas.addAll(nowDatas);
                adapter.notifyDataSetChanged();
                return;
            }
            //上拉加载
        } else if (type == OperationType.PULLDOWNLOAD) {
            listView.onRefreshComplete();
            if (nowDatas != null && nowDatas.isEmpty() && datas.isEmpty()) {
                infoView.showEmptyView(listView, getString(R.string.vantop_no_list_data), true, true);
                return;
            } else if (nowDatas != null && !datas.isEmpty() && nowDatas.isEmpty()) {
                Toast.makeText(this, getString(R.string.vantop_lastpage), Toast.LENGTH_SHORT).show();
                return;
            } else if (nowDatas != null && !nowDatas.isEmpty()) {
                datas.addAll(nowDatas);
                adapter.notifyDataSetChanged();
                return;
            }
            //下拉刷新
        } else if (type == OperationType.PULLTOREFRESHH) {
            listView.onRefreshComplete();
            if (nowDatas != null && datas.isEmpty() && nowDatas.isEmpty()) {
                infoView.showEmptyView(listView, getString(R.string.vantop_no_list_data), true, true);
                return;
            } else if (nowDatas != null && nowDatas.isEmpty() && !datas.isEmpty()) {
                return;
            } else if (nowDatas != null && !nowDatas.isEmpty()) {
                datas.clear();
                datas.addAll(nowDatas);
                adapter.notifyDataSetChanged();
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Class cls = BaseActivity.class;
        try {
            Field f = cls.getDeclaredField("mExitReceiver");
            f.setAccessible(true);
            BroadcastReceiver receiver = (BroadcastReceiver) f.get(this);
            if (receiver != null) {
                unregisterReceiver(receiver);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
