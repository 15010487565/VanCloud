package com.vgtech.vancloud.ui;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vgtech.common.PrfUtils;
import com.vgtech.common.utils.KeyboardUtil;
import com.vgtech.common.view.DateFullDialogView;
import com.vgtech.vancloud.Actions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.module.financemanagement.TradeListActivity;
import com.vgtech.vancloud.wxapi.WXEntryActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 带有搜索布局的Activity父类
 * Created by John on 2015/9/9.
 */
public class SearchBaseActivity extends WXEntryActivity {


    private LinearLayout topTypeClickLayout;
    private TextView titleTextView;
    public ImageView arrowView;
    private ImageView searchView;
    protected TextView cancelView;
    private RelativeLayout searchLayout;
    private ImageView searchCancelView;
    private ImageView addView;


    public RelativeLayout advancedSearchLayout;
    private ImageView advancedArrowView;
    public View shadeView;
    public View titleShadeView;

    TextView advancedView;

    public EditText serchContextView;
    public TextView startTimeView;
    public TextView endTimeView;

    public TextView typeResultView;

    @Override
    protected void onDestroy() {
        topTypeClickLayout = null;
        titleTextView = null;
        arrowView = null;
        searchView = null;
        cancelView = null;
        searchLayout = null;
        searchCancelView = null;
        addView = null;
        advancedSearchLayout = null;
        advancedArrowView = null;
        shadeView = null;
        titleShadeView = null;
        advancedView = null;
        serchContextView = null;
        startTimeView = null;
        endTimeView = null;
        typeResultView = null;
        super.onDestroy();
    }

    /**
     * 初始化view
     */
    public void initTitleLayout() {

        topTypeClickLayout = (LinearLayout) findViewById(R.id.top_type_click);
        titleTextView = (TextView) findViewById(R.id.title_text);
        arrowView = (ImageView) findViewById(R.id.arrow);

        searchView = (ImageView) findViewById(R.id.search);
        cancelView = (TextView) findViewById(R.id.cancel);
        searchLayout = (RelativeLayout) findViewById(R.id.search_layout);
        searchCancelView = (ImageView) findViewById(R.id.search_cancel);

        addView = (ImageView) findViewById(R.id.add);

        shadeView = findViewById(R.id.shade_view);
        advancedSearchLayout = (RelativeLayout) findViewById(R.id.advanced_search_layout);
        advancedSearchLayout.setTag(true);
        advancedView = (TextView) findViewById(R.id.advanced);
        advancedArrowView = (ImageView) findViewById(R.id.advanced_arrow);

        serchContextView = (EditText) findViewById(R.id.serch_context);
        startTimeView = (TextView) findViewById(R.id.start_time);
        endTimeView = (TextView) findViewById(R.id.end_time);

        titleShadeView = findViewById(R.id.title_shade);

        typeResultView = (TextView) findViewById(R.id.type_result);


        findViewById(R.id.cancle_button).setOnClickListener(this);
        findViewById(R.id.confirm_button).setOnClickListener(this);

        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.search).setOnClickListener(this);
        findViewById(R.id.search_cancel).setOnClickListener(this);

        advancedView.setOnClickListener(this);
        cancelView.setOnClickListener(this);
        topTypeClickLayout.setOnClickListener(this);
        titleShadeView.setOnClickListener(this);
        addView.setOnClickListener(this);
        shadeView.setOnClickListener(this);
        startTimeView.setOnClickListener(this);
        endTimeView.setOnClickListener(this);


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
                    cancelView.setText(getResources().getString(R.string.search));

                } else {
                    searchCancelView.setVisibility(View.INVISIBLE);
                    cancelView.setText(getResources().getString(R.string.cancel));
                }
            }
        });

    }


    /**
     * 设置标题
     */
    public void setTitleText(String titleText) {

        titleTextView.setText(titleText);

    }

    public void setTitleCenter(){
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        topTypeClickLayout.setLayoutParams(layoutParams);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                hideKeyboard();
                onBackPressed();
                break;

            case R.id.search:
                searchLayout.setVisibility(View.VISIBLE);
                addView.setVisibility(View.GONE);
                searchView.setVisibility(View.GONE);
                cancelView.setVisibility(View.VISIBLE);
                topTypeClickLayout.setVisibility(View.INVISIBLE);
                KeyboardUtil.showSoftInput(serchContextView);
                sendBroadcast(new Intent(Actions.ACTION_HIDE_MORE_VIEW));
                break;

            case R.id.cancel:

                if (advancedSearchLayout.getVisibility() == View.VISIBLE) {
                    hideAdvancedSearchLayout();
                }
                searchLayout.setVisibility(View.GONE);
                addView.setVisibility(View.VISIBLE);
                searchView.setVisibility(View.VISIBLE);
                cancelView.setVisibility(View.GONE);
                topTypeClickLayout.setVisibility(View.VISIBLE);

                if (cancelView.getText().equals(getString(R.string.search))) {
                    searchRequest();
                    serchContextView.setText("");
                }
                hideKeyboard();

                if (this instanceof TradeListActivity) {
                    findViewById(R.id.add).setVisibility(View.GONE);
                }
                KeyboardUtil.hideSoftInput(this);
                sendBroadcast(new Intent(Actions.ACTION_SHOW_MORE_VIEW));
                break;

            case R.id.advanced:

                if (advancedSearchLayout.getVisibility() == View.VISIBLE) {
                    hideAdvancedSearchLayout();
                } else {
                    showAdvancedSearchLayout();
                }

                break;

            case R.id.search_cancel:
                serchContextView.setText("");
                break;

            case R.id.start_time:
                hideKeyboard();
                showDateDialogview(startTimeView, false, endTimeView);
                break;

            case R.id.end_time:
                hideKeyboard();
                showDateDialogview(endTimeView, true, startTimeView);
                break;

            case R.id.cancle_button:
                startTimeView.setText(getResources().getString(R.string.no_time));
                startTimeView.setTextColor(getResources().getColor(R.color.comment_grey));
                endTimeView.setText(getResources().getString(R.string.no_time));
                endTimeView.setTextColor(getResources().getColor(R.color.comment_grey));
                typeResultView.setText(getResources().getString(R.string.all));
                break;

            case R.id.confirm_button:

                doSearch();
//                }
//                hideKeyboard();
//                searchRequest();
//                serchContextView.setDettailText("");
                break;
            case R.id.shade_view:
                hideAdvancedSearchLayout();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    protected void doSearch() {
        searchLayout.setVisibility(View.GONE);
        addView.setVisibility(View.VISIBLE);
        searchView.setVisibility(View.VISIBLE);
        cancelView.setVisibility(View.GONE);
        topTypeClickLayout.setVisibility(View.VISIBLE);

//                if (advancedSearchLayout.getVisibility() == View.VISIBLE) {
        hideAdvancedSearchLayout();
        searchLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideKeyboard();
                searchRequest();
                serchContextView.setText("");
                startTimeView.setText(getResources().getString(R.string.no_time));
                startTimeView.setTextColor(getResources().getColor(R.color.comment_grey));
                endTimeView.setText(getResources().getString(R.string.no_time));
                endTimeView.setTextColor(getResources().getColor(R.color.comment_grey));
                typeResultView.setText(getResources().getString(R.string.all));
            }
        }, 300);

    }

    /**
     * 展开动画
     *
     * @param translateAnimationView
     * @param rotateAnimationView
     * @param listener
     */
    public void openAnimation(View translateAnimationView, View rotateAnimationView, Animation.AnimationListener listener) {

        if ((boolean) translateAnimationView.getTag()) {
            int height = translateAnimationView.getHeight();
            Animation translateAnimation = new TranslateAnimation(0, 0,
                    -height, 0);
            translateAnimation.setDuration(300);
            translateAnimation.setAnimationListener(listener);
            Animation rotateAnimation = new RotateAnimation(0f, 180f, rotateAnimationView.getWidth() / 2, rotateAnimationView.getHeight() / 2);
            rotateAnimation.setDuration(300);
            rotateAnimation.setFillAfter(true);
            rotateAnimationView.startAnimation(rotateAnimation);
            translateAnimationView.startAnimation(translateAnimation);
        }
    }

    public void closeAnimation(View translateAnimationView, View rotateAnimationView, Animation.AnimationListener listener) {

        if ((boolean) translateAnimationView.getTag()) {
            int height = translateAnimationView.getHeight();
            Animation translateAnimation = new TranslateAnimation(0, 0,
                    0, -height);
            translateAnimation.setDuration(300);
            translateAnimation.setAnimationListener(listener);
            Animation rotateAnimation = new RotateAnimation(180f, 360f, rotateAnimationView.getWidth() / 2, rotateAnimationView.getHeight() / 2);
            rotateAnimation.setDuration(300);
            rotateAnimation.setFillAfter(true);
            rotateAnimationView.startAnimation(rotateAnimation);
            translateAnimationView.startAnimation(translateAnimation);
        }
    }

    /**
     * 搜索请求
     */
    public void searchRequest() {
    }

    /**
     * 搜索请求
     */
    public void goToSearchRequest() {

        if (TextUtils.isEmpty(serchContextView.getText().toString())
                && getString(R.string.no_time).equals(startTimeView.getText().toString())
                && getString(R.string.no_time).equals(endTimeView.getText().toString()))
            searchRequest();


    }


    /**
     * 显示高级搜索布局
     */
    public void showAdvancedSearchLayout() {

        openAnimation(advancedSearchLayout, advancedArrowView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                advancedSearchLayout.setTag(false);
                advancedSearchLayout.setVisibility(View.VISIBLE);
                shadeView.setVisibility(View.VISIBLE);
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


        closeAnimation(advancedSearchLayout, advancedArrowView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                advancedSearchLayout.setTag(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                advancedSearchLayout.setTag(true);
                advancedSearchLayout.setVisibility(View.INVISIBLE);
                shadeView.setVisibility(View.GONE);
                advancedArrowView.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    protected void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(serchContextView.getWindowToken(), 0);
        }
    }

    public static final int NOMAL = 1;
    public static final int TRADELIST = 2;

    /**
     * @param textView
     * @param type     false 开始时间，true 结束时间
     */
    public void showDateDialogview(TextView textView, boolean type, TextView startTimeView) {
        String dateS = textView.getText().toString();
        String startTime = startTimeView.getText().toString();
        Calendar calendar = null;
        Calendar otherCalendar = null;

        if (!TextUtils.isEmpty(startTime) && !getString(R.string.no_time).equals(startTime)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date parse = dateFormat.parse(startTime);
                otherCalendar = Calendar.getInstance();
                otherCalendar.setTime(parse);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(dateS) && !getString(R.string.no_time).equals(dateS)) {
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


        if (!TextUtils.isEmpty(startTime) && !getString(R.string.no_time).equals(startTime)) {

            if (type) {
                dialogtype = "EndTime_YMD";
            } else {
                dialogtype = "StartTime_YMD";
            }
        }

        DateFullDialogView dateDialogview = new DateFullDialogView(this,
                textView, dialogtype, "date", calendar, getResources().getColor(R.color.text_black), otherCalendar);//年月日时分秒 当前日期之后选择
        dateDialogview.show(textView);


    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(PrfUtils.setLocal(newBase));
    }

    public void hidAdvancedSearch() {
        findViewById(R.id.advancedlayout).setVisibility(View.GONE);
    }

}
