package com.vgtech.common.view;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.PopupWindow;

import com.vgtech.common.R;


/**
 * Created by code on 2015/11/5.
 * 分享view
 */
public class ShareActionSheet extends PopupWindow {
    private View contentView;
    private static Activity context;
    private boolean isDismissed = false;
    private View layoutContent;
    private Button sinaBtn;
    private Button qqBtn;
    private Button wetcheBtn;
    private Button friendsBtn;
    private Button messageBtn;
    private Button cancelBtn;

    private ShareActionSheet(View contentView, int width, int height,boolean focusable) {
        super(contentView, width, height, focusable);
    }
    private static final int GONE_MSG = 1;
    private int type;
    public static ShareActionSheet getInstanceGoneMsg(final Activity context,final IListener listener) {
        ShareActionSheet shareActionSheet =  getInstance(context,listener);
        shareActionSheet.contentView.findViewById(R.id.msg_view).setVisibility(View.GONE);
        return shareActionSheet;
    }
    public static ShareActionSheet getInstance(final Activity context,final IListener listener) {
        View contentView = LayoutInflater.from(context).inflate(
                R.layout.share_action_sheet, null);
        final ShareActionSheet actionSheet = new ShareActionSheet(contentView,
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT, true);

        actionSheet.context = context;
        actionSheet.contentView = contentView;

        actionSheet.layoutContent = contentView.findViewById(R.id.layout_content);
        actionSheet.wetcheBtn = (Button) contentView.findViewById(R.id.share_wetch);
        actionSheet.friendsBtn = (Button) contentView.findViewById(R.id.share_friends);
        actionSheet.sinaBtn = (Button) contentView.findViewById(R.id.share_sina);
        actionSheet.qqBtn = (Button) contentView.findViewById(R.id.share_qq);
        actionSheet.messageBtn = (Button) contentView.findViewById(R.id.share_message);
        actionSheet.cancelBtn = (Button) contentView.findViewById(R.id.share_bottom);
        actionSheet.contentView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    actionSheet.dismiss();
                }
                return true;
            }
        });
        actionSheet.layoutContent.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        actionSheet.cancelBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                actionSheet.dismiss();
            }
        });

        actionSheet.wetcheBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                actionSheet.dismiss();
                listener.wetchAction();
            }
        });

        actionSheet.friendsBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                actionSheet.dismiss();
                listener.friendAction();
            }
        });

        actionSheet.sinaBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                actionSheet.dismiss();
                listener.sinaAction();
            }
        });

        actionSheet.qqBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                actionSheet.dismiss();
            }
        });

        //短信分享
        actionSheet.messageBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                actionSheet.dismiss();
                listener.msmAction();
//                Uri smsToUri = Uri.parse("smsto:");
//                Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
//                intent.putExtra("sms_body", message);//信息内容，如果没有可以直接删除
//                context.startActivity(intent);
            }
        });


        actionSheet.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        actionSheet.setAnimationStyle(R.style.action_sheet_no_animation);

        return actionSheet;
    }
    public void show() {
        if (context.getWindow().isActive()) {
            showAtLocation(context.getWindow().getDecorView(), Gravity.BOTTOM,0, 0);
            Animation animation = AnimationUtils.loadAnimation(context,R.anim.actionsheet_in);
            animation.setFillEnabled(true);
            animation.setFillAfter(true);
            layoutContent.startAnimation(animation);
        } else {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (context.getWindow().isActive()) {
                        showAtLocation(context.getWindow().getDecorView(),
                                Gravity.BOTTOM, 0, 0);

                        Animation animation = AnimationUtils.loadAnimation(
                                context, R.anim.actionsheet_in);
                        animation.setFillEnabled(true);
                        animation.setFillAfter(true);
                        layoutContent.startAnimation(animation);
                    }
                }
            }, 600);
        }

    }

    public interface IListener {
        void msmAction();
        void wetchAction();
        void friendAction();
        void sinaAction();
    }

    @Override
    public void dismiss() {
        if (isDismissed) {
            return;
        }
        isDismissed = true;
        Animation animation = AnimationUtils.loadAnimation(context,
                R.anim.actionsheet_out);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                layoutContent.setVisibility(View.INVISIBLE);
                new Handler().post(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            superDismiss();
                        } catch (Exception e) {

                        }
                    }
                });
            }
        });
        layoutContent.startAnimation(animation);
    }

    public void superDismiss() {
        super.dismiss();
    }
}
