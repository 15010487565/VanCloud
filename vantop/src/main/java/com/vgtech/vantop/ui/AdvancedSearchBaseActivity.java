package com.vgtech.vantop.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.view.DateFullDialogView;
import com.vgtech.vantop.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by code on 2016/7/19.
 */
public class AdvancedSearchBaseActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout topTypeClickLayout;
    private TextView titleTextView;
    public ImageView arrowView;
    private ImageView searchView;
    private TextView cancelView;
    public ImageView addView;


    public RelativeLayout advancedSearchLayout;
    //public View shadeView;
    public View titleShadeView;

    public TextView startTimeView;
    public TextView endTimeView;
    public LinearLayout new_option;
    public TextView new_option_content;
    public LinearLayout new_option1;
    public TextView new_option_content1;

    /**
     * 初始化view
     */
    public void initTitleLayout() {
        topTypeClickLayout = (LinearLayout) findViewById(R.id.top_type_click);
        titleTextView = (TextView) findViewById(R.id.title_text);
        arrowView = (ImageView) findViewById(R.id.arrow);

        searchView = (ImageView) findViewById(R.id.search);
        cancelView = (TextView) findViewById(R.id.cancel);

        addView = (ImageView) findViewById(R.id.add);


        //shadeView = findViewById(R.id.shade_view);
        advancedSearchLayout = (RelativeLayout) findViewById(R.id.advanced_search_layout);
        advancedSearchLayout.setTag(true);
        startTimeView = (TextView) findViewById(R.id.start_time);
        endTimeView = (TextView) findViewById(R.id.end_time);
        initDate(true);
        initDate(false);

        new_option = (LinearLayout) findViewById(R.id.new_option);
        new_option_content = (TextView) findViewById(R.id.new_option_content);

        new_option1 = (LinearLayout) findViewById(R.id.new_option1);
        new_option_content1 = (TextView) findViewById(R.id.new_option_content1);

        titleShadeView = findViewById(R.id.title_shade);


        findViewById(R.id.cancle_button).setOnClickListener(this);
        findViewById(R.id.confirm_button).setOnClickListener(this);

        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.search).setOnClickListener(this);

        cancelView.setOnClickListener(this);
        topTypeClickLayout.setOnClickListener(this);
        titleShadeView.setOnClickListener(this);
        addView.setOnClickListener(this);
        //shadeView.setOnClickListener(this);
        startTimeView.setOnClickListener(this);
        endTimeView.setOnClickListener(this);
        new_option_content.setOnClickListener(this);
        new_option_content1.setOnClickListener(this);
    }


    private void initDate(boolean type) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (type) {
            calendar.add(Calendar.MONTH, -1);
            startTimeView.setText(dateFormat.format(calendar.getTime()));
        } else {
            endTimeView.setText(dateFormat.format(calendar.getTime()));
        }
    }

    /**
     * 设置标题
     */
    public void setTitleText(String titleText) {
        titleTextView.setText(titleText);
    }

    /**
     * 控制新选项标题是否显示
     */
    public void isShowNewOptionTirle(boolean isShow, String indexe) {
        if ("0".equals(indexe)) {
            if (isShow) {
                new_option.setVisibility(View.VISIBLE);
            } else {
                new_option.setVisibility(View.GONE);
            }
        } else if ("1".equals(indexe)) {
            if (isShow) {
                new_option1.setVisibility(View.VISIBLE);
            } else {
                new_option1.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 点击新选项要执行的任务
     */
    public void newOptionTask() {
    }

    public void newOptionTask1() {
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            hideKeyboard();
            onBackPressed();
        } else if (v.getId() == R.id.search) {
            if (advancedSearchLayout.getVisibility() == View.VISIBLE) {
                hideAdvancedSearchLayout();
            } else {
                showAdvancedSearchLayout();
            }
            addView.setVisibility(View.INVISIBLE);
            searchView.setVisibility(View.INVISIBLE);
            cancelView.setVisibility(View.VISIBLE);
            topTypeClickLayout.setVisibility(View.INVISIBLE);
        } else if (v.getId() == R.id.cancel) {
            if (advancedSearchLayout.getVisibility() == View.VISIBLE) {
                hideAdvancedSearchLayout();
            }
            addView.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.VISIBLE);
            cancelView.setVisibility(View.GONE);
            topTypeClickLayout.setVisibility(View.VISIBLE);

            if (cancelView.getText().equals(getString(R.string.vantop_search))) {
                searchRequest();
            }
            hideKeyboard();
        } else if (v.getId() == R.id.start_time) {
            hideKeyboard();
            showDateDialogview(startTimeView, false, endTimeView);
        } else if (v.getId() == R.id.end_time) {
            hideKeyboard();
            showDateDialogview(endTimeView, true, startTimeView);
        } else if (v.getId() == R.id.new_option_content) {
            newOptionTask();
        } else if (v.getId() == R.id.new_option_content1) {
            newOptionTask1();
        } else if (v.getId() == R.id.cancle_button) {
            startTimeView.setText(getResources().getString(R.string.vantop_nothing));
            startTimeView.setTextColor(getResources().getColor(R.color.comment_grey));
            endTimeView.setText(getResources().getString(R.string.vantop_nothing));
            endTimeView.setTextColor(getResources().getColor(R.color.comment_grey));
            new_option_content.setText(getResources().getString(R.string.vantop_all));
            new_option_content.setTextColor(getResources().getColor(R.color.comment_grey));
            new_option_content1.setText(getResources().getString(R.string.vantop_please_select));
            new_option_content1.setTextColor(getResources().getColor(R.color.comment_grey));
        } else if (v.getId() == R.id.confirm_button) {
            addView.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.VISIBLE);
            cancelView.setVisibility(View.GONE);
            topTypeClickLayout.setVisibility(View.VISIBLE);

            hideAdvancedSearchLayout();
            hideKeyboard();
            searchRequest();

            v.postDelayed(new Runnable() {
                public void run() {
                    initDate(true);
                    initDate(false);
                    //startTimeView.setText(getResources().getString(R.string.vantop_nothing));
                    startTimeView.setTextColor(getResources().getColor(R.color.comment_grey));
                    //endTimeView.setText(getResources().getString(R.string.vantop_nothing));
                    endTimeView.setTextColor(getResources().getColor(R.color.comment_grey));
                    new_option_content.setText(getResources().getString(R.string.vantop_all));
                    new_option_content.setTextColor(getResources().getColor(R.color.comment_grey));
                    new_option_content1.setText(getResources().getString(R.string.vantop_please_select));
                    new_option_content1.setTextColor(getResources().getColor(R.color.comment_grey));

                }
            }, 200);
        } else {
            super.onClick(v);
        }
    }


    /**
     * 展开动画
     *
     * @param translateAnimationView
     * @param listener
     */
    public void openAnimation(View translateAnimationView, Animation.AnimationListener listener) {

        if ((boolean) translateAnimationView.getTag()) {
            int height = translateAnimationView.getHeight();
            Animation translateAnimation = new TranslateAnimation(0, 0,
                    -height, 0);
            translateAnimation.setDuration(300);
            translateAnimation.setAnimationListener(listener);
            translateAnimationView.startAnimation(translateAnimation);
        }
    }

    public void closeAnimation(View translateAnimationView, Animation.AnimationListener listener) {

        if ((boolean) translateAnimationView.getTag()) {
            int height = translateAnimationView.getHeight();
            Animation translateAnimation = new TranslateAnimation(0, 0,
                    0, -height);
            translateAnimation.setDuration(300);
            translateAnimation.setAnimationListener(listener);
            translateAnimationView.startAnimation(translateAnimation);
        }
    }

    /**
     * 搜索请求
     */
    public void searchRequest() {
    }

    /**
     * 显示高级搜索布局
     */
    public void showAdvancedSearchLayout() {

        openAnimation(advancedSearchLayout, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                advancedSearchLayout.setTag(false);
                advancedSearchLayout.setVisibility(View.VISIBLE);
                //shadeView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                advancedSearchLayout.setTag(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 隐藏高级搜索布局
     */
    public void hideAdvancedSearchLayout() {


        closeAnimation(advancedSearchLayout, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                advancedSearchLayout.setTag(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                advancedSearchLayout.setTag(true);
                advancedSearchLayout.setVisibility(View.INVISIBLE);
                //shadeView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(titleTextView.getWindowToken(), 0);
        }
    }


    /**
     * @param textView
     * @param type     false 开始时间，true 结束时间
     */
    public void showDateDialogview(TextView textView, boolean type, TextView startTimeView) {
        String dateS = textView.getText().toString();
        String startTime = startTimeView.getText().toString();
        Calendar calendar = null;
        Calendar otherCalendar = null;

        if (!TextUtils.isEmpty(startTime) && !getString(R.string.vantop_nothing).equals(startTime)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date parse = dateFormat.parse(startTime);
                otherCalendar = Calendar.getInstance();
                otherCalendar.setTime(parse);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(dateS) && !getString(R.string.vantop_nothing).equals(dateS)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
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

        String dialogtype = "YMD";
        String dateType = "";


        if (!TextUtils.isEmpty(startTime) && !getString(R.string.vantop_nothing).equals(startTime)) {

            if (type) {
                dialogtype = "EndTime_YMD";
            } else {
                dialogtype = "StartTime_YMD";
            }
        }

        if (type) {
            dateType = "EndTimeYMD";
        } else {
            dateType = "StartTimeYMD";
        }

        DateFullDialogView dateDialogview = new DateFullDialogView(this,
                textView, dialogtype, "date", calendar, getResources().getColor(R.color.text_black), otherCalendar);//年月日时分秒 当前日期之后选择
        dateDialogview.show(textView);

    }
}
