package com.vgtech.vantop.ui.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Data:  2017/6/19
 * Auther: 陈占洋
 * Description:
 */

public class ArrowRectDrawable extends Drawable {

    private Bitmap mBitmap;
    private Path mTrianglePath;
    private int mRoundHalfRadio;
    private RectF mRectF;
    private int mRectTopMargin;
    private Paint mPaint;
    private RectF mInnerRectF;

    public ArrowRectDrawable() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);

        mRectF = new RectF();
        mRectTopMargin = 20;
        mRoundHalfRadio = 10;
        mTrianglePath = new Path();

    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mRectF.left = bounds.left;
        mRectF.top = bounds.top + mRectTopMargin;
        mRectF.right = bounds.right;
        mRectF.bottom = bounds.bottom;

        mInnerRectF = new RectF();

        mTrianglePath.moveTo(mRectF.right * 0.7f, mRectF.top);
        mTrianglePath.rLineTo(mRectTopMargin / 2, -mRectTopMargin);
        mTrianglePath.rLineTo(mRectTopMargin / 2, mRectTopMargin);
        mTrianglePath.close();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawPath(mTrianglePath, mPaint);

        canvas.drawRoundRect(mRectF, mRoundHalfRadio, mRoundHalfRadio, mPaint);
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
