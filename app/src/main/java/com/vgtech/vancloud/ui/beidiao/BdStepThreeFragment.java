package com.vgtech.vancloud.ui.beidiao;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.vgtech.common.api.AppPermission;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.presenter.AppPermissionPresenter;
import com.vgtech.vancloud.ui.BaseFragment;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by vic on 2016/10/14.
 */
public class BdStepThreeFragment extends BaseFragment {
    @Override
    protected int initLayoutId() {
        return R.layout.beidiao_step_three;
    }

    private BdStepListener stepListener;

    public void setStepListener(BdStepListener stepListener) {
        this.stepListener = stepListener;
    }

    private TextView mTvTimes;
    private int mTimes;
    private Timer mTimer;
    private TimerTask mTimerTask;

    public void start() {
        mTimes = 10;
        mTvTimes.setText(String.valueOf(mTimes));
        mTimer = new Timer();
        mTimerTask = new TimerTask() {

            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        mTimer.schedule(mTimerTask, 1000, 1000); // 1s后执行task,经过1s再次执行
    }

    @Override
    protected void initView(View view) {
        mTvTimes = (TextView) view.findViewById(R.id.tv_times);
    }

    @Override
    public void onDestroyView() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
        }
        if (mTimer != null)
            mTimer.cancel();
        super.onDestroyView();
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                mTimes--;
                if (mTimes <= 0) {
                    mTimerTask.cancel();
                    mTimer.cancel();
                    if (AppPermissionPresenter.hasPermission(getActivity(), AppPermission.Type.beidiao, AppPermission.Beidiao.my.toString())
                            ||AppPermissionPresenter.hasPermission(getActivity(), AppPermission.Type.beidiao, AppPermission.Beidiao.all.toString())) {
                        Intent intent = new Intent(getActivity(), BdListActivity.class);
                        startActivity(intent);
                        stepListener.reset();
                    }
                } else {
                    mTvTimes.setText(String.valueOf(mTimes));
                }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void initData() {

    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }
}
