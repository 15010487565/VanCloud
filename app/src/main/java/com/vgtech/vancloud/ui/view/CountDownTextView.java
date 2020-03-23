package com.vgtech.vancloud.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.widget.TextView;

import com.vgtech.vancloud.R;

/**
 * Created by code on 2015/10/27.
 */
public class CountDownTextView extends TextView {

    private static final int DEFAULT_SECONDS_IN_FUTURE = 60;
    private static final int DEFAULT_COUNT_DOWN_INTERVAL = 1;

    private long millisInFuture = DEFAULT_SECONDS_IN_FUTURE * 1000l;
    private long countDownInterval = DEFAULT_COUNT_DOWN_INTERVAL * 1000l;
    private int tickStringId = 0;
    private int beginStringId = 0;
    private int finishStringId = 0;
    private int tickBackgroundId = 0;
    private int beginBackgroundId = 0;
    private int finishBackgroundId = 0;
    private InnerCountDownTimer innerCountDownTimer;

    public CountDownTextView(Context context) {
        super(context);
    }

    public CountDownTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountDownTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CountDownTextView, 0, 0);
        millisInFuture = a.getInt(R.styleable.CountDownTextView_secondsInFuture
                , DEFAULT_SECONDS_IN_FUTURE) * 1000l;
        countDownInterval = a.getInt(R.styleable.CountDownTextView_countDownInterval
                , DEFAULT_COUNT_DOWN_INTERVAL) * 1000l;
        tickStringId = a.getResourceId(R.styleable.CountDownTextView_tickString, 0);
        beginStringId = a.getResourceId(R.styleable.CountDownTextView_beginString, 0);
        finishStringId = a.getResourceId(R.styleable.CountDownTextView_finishString, 0);
        tickBackgroundId = a.getResourceId(R.styleable.CountDownTextView_tickBackground, 0);
        beginBackgroundId = a.getResourceId(R.styleable.CountDownTextView_beginBackground, 0);
        finishBackgroundId = a.getResourceId(R.styleable.CountDownTextView_finishBackground, 0);
        a.recycle();

        setText(getContext().getResources().getString(beginStringId, millisInFuture / countDownInterval));
        setBackgroundResource(beginBackgroundId);
    }

    public void start(){
        if (innerCountDownTimer != null) {
            innerCountDownTimer.cancel();
        } else {
            innerCountDownTimer = new InnerCountDownTimer(millisInFuture, countDownInterval);
        }
        innerCountDownTimer.start();
        setClickable(false);
    }

    public void cancel(){
        if (innerCountDownTimer != null) {
            innerCountDownTimer.cancel();
            setClickable(true);
            setText(getContext().getResources().getString(finishStringId));
            setBackgroundResource(finishBackgroundId);
        }
    }

    public void setBeginStatus(){
        if (innerCountDownTimer != null) {
            innerCountDownTimer.cancel();
            setClickable(true);
            setText(getContext().getResources().getString(beginStringId));
            setBackgroundResource(beginBackgroundId);
        }
    }

    private class InnerCountDownTimer extends CountDownTimer {
        private long millisInFuture;
        private long countDownInterval;

        public InnerCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            this.millisInFuture = millisInFuture;
            this.countDownInterval = countDownInterval;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            CountDownTextView.this.setText(CountDownTextView.this.getContext().getResources()
                    .getString(tickStringId, millisUntilFinished / countDownInterval));
            CountDownTextView.this.setBackgroundResource(CountDownTextView.this.tickBackgroundId);
        }

        @Override
        public void onFinish() {
            CountDownTextView.this.setText(CountDownTextView.this.getContext().getResources()
                    .getString(finishStringId, millisInFuture / countDownInterval));
            CountDownTextView.this.setBackgroundResource(CountDownTextView.this.finishBackgroundId);
            CountDownTextView.this.setClickable(true);
        }
    }
}
