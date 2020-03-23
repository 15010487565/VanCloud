package com.vgtech.vancloud.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.AppModulePresenter;
import com.vgtech.vancloud.ui.beidiao.BeidiaoActivity;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.module.recruit.JobCreateActivity;
import com.vgtech.vancloud.utils.XMLResParser;
import com.vgtech.vantop.ui.overtime.CreatedOverTimeActivity;
import com.vgtech.vantop.ui.signedcard.SignedCardAddActivity;
import com.vgtech.vantop.ui.vacation.MyVacationActivity;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by vic on 2016/9/20.
 */
public class QuickOptionDialog {
    private Dialog addQuickDialog; //应用"+"快速入口
    private GridViewGallery mGallery;//存放布局的视图容器
    private LinearLayout mLayout;
    private SpringSystem mSpringSystem;
    private static final int DEFAULT_DURATION = 200;//动画时间
    private static final int DEFAULT_TENSION = 40;//拉力系数
    private static final int DEFAULT_FRICTION = 6;//摩擦力系数
    private int duration = DEFAULT_DURATION;
    private double tension = DEFAULT_TENSION;
    private double friction = DEFAULT_FRICTION;
    private Activity mActivity;

    public QuickOptionDialog(Activity activity) {
        mActivity = activity;
        mSpringSystem = SpringSystem.create();
    }

    public void initQuickDialog() {
        View itemLayout = LayoutInflater.from(mActivity).inflate(R.layout.quick_dialog, null);
        TextView dataTv = (TextView) itemLayout.findViewById(R.id.data_tv);
        TextView weekTv = (TextView) itemLayout.findViewById(R.id.week_tv);
        TextView yearTv = (TextView) itemLayout.findViewById(R.id.year_tv);
        LinearLayout closeLayout = (LinearLayout) itemLayout.findViewById(R.id.close_layout);
        closeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSubMenus(mLayout, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        addQuickDialog.dismiss();
                    }
                });
            }
        });

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
        String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
        String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
        String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        String[] weeks = mActivity.getResources().getStringArray(R.array.week);
        String week = "";
        if ("1".equals(mWay)) {
            week = weeks[0];
        } else if ("2".equals(mWay)) {
            week = weeks[1];
        } else if ("3".equals(mWay)) {
            week = weeks[2];
        } else if ("4".equals(mWay)) {
            week = weeks[3];
        } else if ("5".equals(mWay)) {
            week = weeks[4];
        } else if ("6".equals(mWay)) {
            week = weeks[5];
        } else if ("7".equals(mWay)) {
            week = weeks[6];
        }

        dataTv.setText(String.valueOf(mDay));
        weekTv.setText(week);
        yearTv.setText(String.valueOf(mMonth) + "/" + String.valueOf(mYear));
        mGallery = new GridViewGallery(mActivity, AppModulePresenter.getAppQuickMenu(mActivity));
        mGallery.setClickTypeListener(new GridViewGallery.ClickTypeListener() {
            @Override
            public void typeAction(AdapterView<?> parent, int position) {
                Object obj = parent.getItemAtPosition(position);
                if (obj instanceof XMLResParser.AppMenu) {
                    XMLResParser.AppMenu tmpName = (XMLResParser.AppMenu) obj;
                    if ("vancloud_flow".equals(tmpName.getTag())) {//审批
                        Intent intent = new Intent(mActivity, NewPublishedActivity.class);
                        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_FLOW);
                        mActivity.startActivity(intent);
                        addQuickDialog.dismiss();
                    } else if ("vancloud_holiday".equals(tmpName.getTag())) {//请假
                        Intent intent = new Intent(mActivity, NewPublishedActivity.class);
                        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_FLOW_LEAVE);
                        mActivity.startActivity(intent);
                        addQuickDialog.dismiss();
                    } else if ("vancloud_zhaopin".equals(tmpName.getTag())) {//招聘
                        Intent intent = new Intent(mActivity, JobCreateActivity.class);
                        intent.putExtra("type", "2");
                        mActivity.startActivity(intent);
                        addQuickDialog.dismiss();
                    } else if ("shenqing_extra_work".equals(tmpName.getTag())) {//加班
                        Intent intent = new Intent(mActivity, CreatedOverTimeActivity.class);
                        mActivity.startActivity(intent);
                        addQuickDialog.dismiss();
                    } else if ("shenqing_vantop_holiday".equals(tmpName.getTag())) {//vantop请假
                        Intent intent = new Intent(mActivity, MyVacationActivity.class);
                        mActivity.startActivity(intent);

                        //申请-休假
//                        Intent intent = new Intent(mActivity, NormalWebActivity.class);
//
//                        String url = "%sappstatic/vantop/jump.html?code=100571&area_code=%s&mobile=%s&tid=%s";
//                        String host = ApiUtils.getHost(mActivity);
//                        String areaCode = PrfUtils.getPrfparams(mActivity, "areaCode", "86");
//                        String userPhone = PrfUtils.getUserPhone(mActivity);
//                        String tenantId = PrfUtils.getTenantId(mActivity);
//                        url = String.format(url, host, areaCode, userPhone, tenantId);
//
//                        intent.putExtra("url", url);
//                        intent.putExtra("title",mActivity.getString(R.string.leave));
//                        mActivity.startActivity(intent);
                        addQuickDialog.dismiss();
                    } else if ("shenqing_sign_card".equals(tmpName.getTag())) {//签卡
                        Intent intent = new Intent(mActivity, SignedCardAddActivity.class);
                        mActivity.startActivity(intent);
                        addQuickDialog.dismiss();
                    } else if ("task".equals(tmpName.getTag())) {//任务
                        Intent intent = new Intent(mActivity, NewPublishedActivity.class);
                        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_TASK);
                        mActivity.startActivity(intent);
                        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        addQuickDialog.dismiss();
                    } else if ("calendar".equals(tmpName.getTag())) {//日程
                        Intent intent = new Intent(mActivity, NewPublishedActivity.class);
                        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_SCHEDULE);
                        mActivity.startActivity(intent);
                        addQuickDialog.dismiss();
                    } else if ("work_reportting".equals(tmpName.getTag())) {//工作汇报
                        Intent intent = new Intent(mActivity, NewPublishedActivity.class);
                        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_WORKREPORT);
                        mActivity.startActivity(intent);
                        addQuickDialog.dismiss();
                    } else if ("topic".equals(tmpName.getTag())) {//分享
                        Intent intent = new Intent(mActivity, NewPublishedActivity.class);
                        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_SHARED);
                        mActivity.startActivity(intent);
                        addQuickDialog.dismiss();
                    }  else if ("investigate:start".equals(tmpName.getTag())) {//背景调查
                        Intent intent = new Intent(mActivity, BeidiaoActivity.class);
                        mActivity.startActivity(intent);
                        addQuickDialog.dismiss();
                    } else if ("help".equals(tmpName.getTag())) {//求帮助
                        Intent intent = new Intent(mActivity, NewPublishedActivity.class);
                        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_HELP);
                        mActivity.startActivity(intent);
                        addQuickDialog.dismiss();
                    }
                }
            }
        });
        mLayout = (LinearLayout) itemLayout.findViewById(R.id.ll_gallery);
        android.widget.LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        mLayout.addView(mGallery, params);
        showSubMenus(mLayout);

        addQuickDialog = new Dialog(mActivity, R.style.Dialog_Fullscreen);
        addQuickDialog.setContentView(itemLayout);
        addQuickDialog.show();

    }

    /**
     * show动画
     *
     * @param linearLayout
     */
    private void showSubMenus(LinearLayout linearLayout) {
        if (linearLayout == null) return;
        int childCount = linearLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = linearLayout.getChildAt(i);
            animateViewDirection(view, 800, 0, tension, friction);
        }
    }

    /**
     * hide动画
     *
     * @param linearLayout
     * @param listener
     */
    private void hideSubMenus(LinearLayout linearLayout, final AnimatorListenerAdapter listener) {
        if (linearLayout == null) return;
        int childCount = linearLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = linearLayout.getChildAt(i);
            view.animate().translationY(800).setDuration(duration).setListener(listener).start();
        }
    }

    /**
     * 弹簧动画
     *
     * @param v        动画View
     * @param from
     * @param to
     * @param tension  拉力系数
     * @param friction 摩擦力系数
     */
    private void animateViewDirection(final View v, float from, float to, double tension, double friction) {
        Spring spring = mSpringSystem.createSpring();
        spring.setCurrentValue(from);
        spring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(tension, friction));
        spring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                v.setTranslationY((float) spring.getCurrentValue());
            }
        });
        spring.setEndValue(to);
    }

}
