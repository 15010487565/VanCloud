package com.vgtech.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.vgtech.common.R;

import java.util.Stack;

/**
 * Created by swj on 15/12/25.
 */
public class PasswordTextfiled extends View {

    private Paint paint;
    private Context mContext;

    private Stack<Character> data;
    private boolean enable = true;
    private boolean show = false; //true画数字、false画圆圈

    public void setShow(boolean show) {
        this.show = show;
    }

    public PasswordTextfiled(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        mContext = context;
        data = new Stack<Character>();
    }

    /*public PasswordTextfiled(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }*/

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawColor(Color.WHITE);

        int lineWidth = convertDipOrPx(mContext, 0.5f);
        int cellWidth = convertDipOrPx(mContext, 50);

        paint.setStrokeWidth(lineWidth);
        if (enable)
            paint.setColor(Color.rgb(0xcb, 0xcc, 0xcd));
        else
            paint.setColor(mContext.getResources().getColor(R.color.bg_title));

        canvas.drawLine(cellWidth - lineWidth / 2, 0, cellWidth - lineWidth / 2, getHeight(), paint);
        canvas.drawLine(cellWidth * 2 - lineWidth / 2, 0, cellWidth * 2 - lineWidth / 2, getHeight(), paint);
        canvas.drawLine(cellWidth * 3 - lineWidth / 2, 0, cellWidth * 3 - lineWidth / 2, getHeight(), paint);
        canvas.drawLine(cellWidth * 4 - lineWidth / 2, 0, cellWidth * 4 - lineWidth / 2, getHeight(), paint);
        canvas.drawLine(cellWidth * 5 - lineWidth / 2, 0, cellWidth * 5 - lineWidth / 2, getHeight(), paint);


        lineWidth = convertDipOrPx(mContext, 1f);
        paint.setStrokeWidth(lineWidth);
        canvas.drawLine(0, lineWidth / 2, getWidth(), lineWidth / 2, paint);
        canvas.drawLine(0, getHeight() - lineWidth, getWidth(), getHeight() - lineWidth, paint);
        canvas.drawLine(lineWidth / 2, 0, lineWidth / 2, getHeight(), paint);
        canvas.drawLine(getWidth() - lineWidth, 0, getWidth() - lineWidth, getHeight(), paint);

        //画圆圈 或者 数字
        final float y = getHeight() / 2;
        final float cx = getWidth() / 12;
        int size = data.size();
        float px = cx;
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        if (show) {
            paint.setTextSize(100);
            paint.setTextAlign(Paint.Align.CENTER);
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    canvas.drawText(data.get(i).toString(), px + 10, y + 30, paint);
                    px += cx * 2;
                }
            }
        } else {
            while (size > 0) {
                canvas.drawCircle(px, y, convertDipOrPx(mContext, 7), paint);
                px += cx * 2;
                size--;
            }
        }
    }

    public int pushChar(Character c) {
        if (!enable)
            return -1;

        if (data.size() < 6)
            data.push(c);
        invalidate();
        return data.size();
    }

    public int pop() {
        if (!enable)
            return -1;
        if (data.size() > 0)
            data.pop();
        invalidate();
        return data.size();
    }

    public Stack<Character> getData() {
        return data;
    }

    public void clearAll() {
        data.clear();
        invalidate();
    }

    public void setEnable(boolean isEnable) {
        if (isEnable) {
            clearAll();
            invalidate();
        } else {
            for (int i = 0; i < 6; i++) {
                pushChar(',');
            }
        }
        enable = isEnable;
    }

    public int convertDipOrPx(Context context, float dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }
}
