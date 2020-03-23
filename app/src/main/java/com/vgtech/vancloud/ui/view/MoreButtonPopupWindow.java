package com.vgtech.vancloud.ui.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vgtech.common.api.ScheduleItem;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.utils.PublishUtils;

/**
 * 更多弹出框
 * Created by Duke on 2015/8/24.
 */
public class MoreButtonPopupWindow {

    public static final int MY_SCHEDULE_DEEP = 3;
    public static final int OTHERS_SCHEDULE_DEEP = 4;

    View mMenuView;
    RelativeLayout finishClickLayout;//完成
    RelativeLayout cancelClickLayout;//取消
    RelativeLayout reviseClickLayout;//修改

    private ImageView img01;
    private ImageView img02;
    private ImageView img03;

    public MoreButtonPopupWindow(Context context, View.OnClickListener itemsOnClick, int type) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.more_button_layout, null);

        finishClickLayout = (RelativeLayout) mMenuView.findViewById(R.id.finish_click);
        cancelClickLayout = (RelativeLayout) mMenuView.findViewById(R.id.cancel_click);
        reviseClickLayout = (RelativeLayout) mMenuView.findViewById(R.id.revise_click);
        finishClickLayout.setOnClickListener(itemsOnClick);
        cancelClickLayout.setOnClickListener(itemsOnClick);
        reviseClickLayout.setOnClickListener(itemsOnClick);
        switch (type) {
            case 1:
                finishClickLayout.setVisibility(View.VISIBLE);
                cancelClickLayout.setVisibility(View.GONE);
                reviseClickLayout.setVisibility(View.GONE);
                break;
            case 2:
                finishClickLayout.setVisibility(View.GONE);
                cancelClickLayout.setVisibility(View.VISIBLE);
                reviseClickLayout.setVisibility(View.VISIBLE);
                break;
        }
        initView(context, mMenuView);
    }

    private Dialog dialog;

    private void initView(Context context, View contentView) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        dialog = new Dialog(context, com.vgtech.common.R.style.ActionSheetDialogStyle);
        dialog.setContentView(contentView);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = display.getWidth();
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public MoreButtonPopupWindow(Context context, View.OnClickListener itemsOnClick, String type) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.more_button_layout, null);
        RelativeLayout commentClickLayout = (RelativeLayout) mMenuView.findViewById(R.id.comment_click);
        RelativeLayout cancelClickLayout = (RelativeLayout) mMenuView.findViewById(R.id.cancel_click);

        if ("0".equals(type)) {
            mMenuView.findViewById(R.id.finish_click).setVisibility(View.GONE);
            cancelClickLayout.setVisibility(View.GONE);
            mMenuView.findViewById(R.id.revise_click).setVisibility(View.GONE);
            commentClickLayout.setVisibility(View.VISIBLE);
        } else {
            commentClickLayout.setVisibility(View.GONE);
            mMenuView.findViewById(R.id.finish_click).setVisibility(View.GONE);
            cancelClickLayout.setVisibility(View.VISIBLE);
            mMenuView.findViewById(R.id.revise_click).setVisibility(View.GONE);
        }

        commentClickLayout.setOnClickListener(itemsOnClick);
        cancelClickLayout.setOnClickListener(itemsOnClick);
        initView(context, mMenuView);
    }


    public MoreButtonPopupWindow(final Context context, final CancelSchedule cancelSchedule, final EditSchedule editSchedule, final ScheduleItem s, int type) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.more_button_layout, null);

        img01 = (ImageView) mMenuView.findViewById(R.id.img01);
        img02 = (ImageView) mMenuView.findViewById(R.id.img02);
        img03 = (ImageView) mMenuView.findViewById(R.id.img03);

        finishClickLayout = (RelativeLayout) mMenuView.findViewById(R.id.finish_click);
        cancelClickLayout = (RelativeLayout) mMenuView.findViewById(R.id.cancel_click);
        reviseClickLayout = (RelativeLayout) mMenuView.findViewById(R.id.revise_click);
//        finishClickLayout.setOnClickListener(itemsOnClick);
//        cancelClickLayout.setOnClickListener(itemsOnClick);
//        reviseClickLayout.setOnClickListener(itemsOnClick);

        switch (type) {
            case OTHERS_SCHEDULE_DEEP:
//                “ 接受”，“拒绝”，“待定”。
                img01.setImageResource(R.drawable.more_button_accept_click);
                img02.setImageResource(R.drawable.more_button_refuse_click);
                img03.setImageResource(R.drawable.more_button_waiting_click);

                ((TextView) mMenuView.findViewById(R.id.txt01)).setText(context.getString(R.string.accept_the_schedule));
                ((TextView) mMenuView.findViewById(R.id.txt02)).setText(context.getString(R.string.refuse_the_schedule));
                ((TextView) mMenuView.findViewById(R.id.txt03)).setText(context.getString(R.string.wait_the_schedule));
//                1待定，2谢绝，3同意
                finishClickLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        PublishUtils.conductSchedule(context, 3, s.scheduleid + "");
                    }
                });
                cancelClickLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        PublishUtils.conductSchedule(context, 2, s.scheduleid + "");
                    }
                });
                reviseClickLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        PublishUtils.conductSchedule(context, 1, s.scheduleid + "");
                    }
                });

                break;
            case MY_SCHEDULE_DEEP:
//                “取消”、“修改”
                finishClickLayout.setVisibility(View.GONE);
//                img01.setImageResource(R.mipmap.more_button_finish);
                img02.setImageResource(R.drawable.more_button_cancel_click);
                img03.setImageResource(R.drawable.more_button_revise_click);

                cancelClickLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        if (cancelSchedule == null) {
                            dismiss();
                            return;
                        }
                        cancelSchedule.cancelScheule(s);
                    }
                });
                reviseClickLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        if (editSchedule == null) {
                            dismiss();
                            return;
                        }
                        editSchedule.editSchedule(s);
//                        Intent intent = new Intent(context, NewPublishedActivity.class);
//                        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_SCHEDULE_UPDATE);
//                        intent.putExtra("scheduleInfo", s.getJson().toString());
//                        context.startActivity(intent);
                    }
                });

                break;
        }
        initView(context, mMenuView);
    }

    private boolean isMine;
    private boolean isCollection;

    //分享的bottomBar
    public MoreButtonPopupWindow(final Activity context, final SharedBottomBar sharedBottomBar, final String id, final String json) {
        // 产生背景变暗效果
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.shared_list_item_more_button_layout, null);

        //分享到微信聊天
        mMenuView.findViewById(R.id.relative08).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedBottomBar.sharedToWeiXinSession(id);
            }
        });
        //分享到朋友圈
        mMenuView.findViewById(R.id.relative01).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedBottomBar.sharedToWeiXin(id);
            }
        });
        //分享到微博
        mMenuView.findViewById(R.id.relative02).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedBottomBar.sharedToWeiBo(id);
            }
        });
        //收藏
        mMenuView.findViewById(R.id.relative03).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedBottomBar.collection(id);
            }
        });
        //删除
        mMenuView.findViewById(R.id.relative04).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMine)
                    sharedBottomBar.deleted(id);
            }
        });
        //转发
        mMenuView.findViewById(R.id.relative05).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedBottomBar.forwardTo(id, json);

            }
        });

        initView(context, mMenuView);
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public interface CancelSchedule {
        void cancelScheule(ScheduleItem schedule);
    }

    public interface EditSchedule {
        void editSchedule(com.vgtech.common.api.ScheduleItem schedule);
    }

    public interface SharedBottomBar {
        //收藏
        void collection(String id);

        //删除
        void deleted(String id);

        //分享到微博
        void sharedToWeiBo(String id);

        //分享到朋友圈
        void sharedToWeiXinSession(String id);

        //分享到朋友圈
        void sharedToWeiXin(String id);

        //转发
        void forwardTo(String id, String json);
    }

    public void setIsMine(boolean isMine) {
        this.isMine = isMine;
        if (!isMine) {
            mMenuView.findViewById(R.id.relative04).setVisibility(View.INVISIBLE);
//            mMenuView.findViewById(R.id.relative03).setSelected(true);
        }

    }

    public void show() {
        dialog.show();
    }

    public void setIsCollection(Context mContext, boolean isCollection) {
        this.isCollection = isCollection;
        ImageView collectionIcon = (ImageView) mMenuView.findViewById(R.id.relative03).findViewById(R.id.img03);
        TextView collectinoText = (TextView) mMenuView.findViewById(R.id.relative03).findViewById(R.id.txt03);

        if (!isCollection) {
            collectionIcon.setBackgroundResource(R.drawable.more_popmenu_dis_collection);
            collectinoText.setText(R.string.shared_list_item_more_option_of_collection);
        } else {
            collectionIcon.setBackgroundResource(R.drawable.more_popmenu_collection);
            collectinoText.setText(R.string.shared_list_item_more_option_of_discollection);
        }

    }
}
