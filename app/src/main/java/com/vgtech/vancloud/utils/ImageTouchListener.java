package com.vgtech.vancloud.utils;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public final class ImageTouchListener implements OnTouchListener {
	private PointF startPoint = new PointF();
	private ImageView imageView;
	private Matrix matrix = new Matrix();
	private Matrix currentMatrix = new Matrix();
	private int mode = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private float startDist;// 开始时的矩离
	private PointF midPoint;// 记录两手指间的中间点，以它为参考点进行缩放
	private int mWidth, mHeight, mMoveX, mMoveY;

	public ImageTouchListener(Context context, ImageView imageView) {
		this.imageView = imageView;
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		mWidth = dm.widthPixels;
		mHeight = dm.heightPixels;
	}

	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {// 32 01111110
																// 10010000
																// & 0000000
																// 11111111
																// = 0000000
																// 10010000
		case MotionEvent.ACTION_DOWN:
			mode = DRAG;
			currentMatrix.set(matrix);// 把图片目前的位置保存起来
			startPoint.set(event.getX(), event.getY());// 记录开始坐标
			break;

		case MotionEvent.ACTION_MOVE:// 移动过程，该事件会不断被触发
			if (mode == ZOOM) {
				float endDist = distance(event);
				if (endDist > 10f) {
					float scale = endDist / startDist;
					matrix.set(currentMatrix);
					matrix.postScale(scale, scale, midPoint.x, midPoint.y);
				}
			} else if (mode == DRAG) {
				float dx = event.getX() - startPoint.x;
				float dy = event.getY() - startPoint.y;
				matrix.set(currentMatrix);
				matrix.postTranslate(dx, dy);
			}
			break;

		case MotionEvent.ACTION_POINTER_DOWN:// 如果已经有手指压住屏幕，再有手指压下屏幕，就会触发该事件
			mode = ZOOM;
			startDist = distance(event);
			if (startDist > 10f) {
				currentMatrix.set(matrix);// 把图片目前的缩放数据保存起来
				midPoint = midPoint(event);
			}
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:// 有手指离开屏幕，但还有手指压住屏幕，就会触发该事件
			mode = 0;
			break;
		}
		imageView.setImageMatrix(matrix);
		return true;
	}

	private static float distance(MotionEvent event) {
		float dx = event.getX(1) - event.getX();
		float dy = event.getY(1) - event.getY();
//		return FloatMath.sqrt(dx * dx + dy * dy);
		return  (float)Math.sqrt(dx * dx + dy * dy);
	}

	private static PointF midPoint(MotionEvent event) {
		float midx = (event.getX(1) + event.getX()) / 2;
		float midy = (event.getY(1) + event.getY()) / 2;
		return new PointF(midx, midy);
	}
}
