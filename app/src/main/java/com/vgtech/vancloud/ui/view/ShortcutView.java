package com.vgtech.vancloud.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.utils.Utils;

/**
 * Created by swj on 16/1/14.
 */
public class ShortcutView extends View {

    private Context mContext;
    private Paint paint;
    private int color;

    private boolean isPressed=false;

    private MotionEvent pressPoint;

    public ShortcutView(Context context) {
        super(context);
        mContext=context;
        init();
    }

    public ShortcutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;

//        xmlns:app="http://schemas.android.com/apk/res-auto"
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.ShortcutView);
        color = a.getColor(R.styleable.ShortcutView_bgcolor, Color.BLACK);
        a.recycle();
        init();
    }

    public ShortcutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext=context;


        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.ShortcutView);
        color = a.getColor(R.styleable.ShortcutView_bgcolor, Color.BLACK);
        a.recycle();
        init();
    }

    private void init(){
        paint=new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_HOVER_ENTER:
                        press();
                        pressPoint=event;
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                        cancel();
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_HOVER_MOVE:
                        double sqrt=Math.pow((pressPoint.getX()-event.getX()),2)+Math.pow((pressPoint.getY()-event.getY()),2);
                        double distance=Math.sqrt(sqrt);
                        if(distance> Utils.convertDipOrPx(mContext,10))
                            cancel();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (hasOnClickListeners() && isPressed)
                        callOnClick();
                        isPressed=false;
                        paint.setAlpha(0xff);
                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(color);
                        invalidate();

                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width=getWidth()>getHeight()?getHeight():getWidth();
        if(isPressed){
            paint.setAlpha(0x7f);
            canvas.drawCircle(width / 2, width / 2, width / 2, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.rgb(0x98, 0x98, 0x98));
            paint.setAlpha(0xff);
            canvas.drawCircle(width / 2, width / 2, width / 2, paint);
        }else{
            paint.setAlpha(0xff);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            canvas.drawCircle(width / 2, width / 2, width / 2, paint);
        }



    }

    public void setBgColor(int color){
        this.color=color;
        paint.setColor(color);
        invalidate();
    }

    private void press(){

        isPressed=true;
        invalidate();
    }

    private void cancel(){
        isPressed=false;

        invalidate();
    }

}
