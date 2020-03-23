package com.vgtech.vancloud.ui.view;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.vgtech.vancloud.utils.Utils;


@SuppressLint("WrongCall")
public class BottomTitle extends View {

	private Context mContext;
	private Paint paint;
	
	private ArrayList<Rect> indexs;
	private int pointCount;
	private int currentPointIndex=0;
	private int distant=100;
	int startX;
	
	private int rectLength=16;
	
	private String title="";
//	private int mesureHeight=0;
//	private boolean isFrist=true;
	
	public BottomTitle(Context context) {
		super(context);
		mContext=context;
		init();
	}
	public BottomTitle(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext=context;
		init();
	}
	public BottomTitle(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext=context;
		init();
	}
	
	private void init(){
		paint=new Paint();
		
		indexs=new ArrayList<Rect>();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		MeasureSpec.getSize(heightMeasureSpec);

		int height=Utils.convertDipOrPx(mContext,24);
//		if(isFrist){
//			mesureHeight=height;
//			isFrist=false;
//		}
		
		int measuredHeight=MeasureSpec.makeMeasureSpec(height, MeasureSpec.getMode(heightMeasureSpec));
		super.onMeasure(widthMeasureSpec, measuredHeight);
	}
	
	@Override
	public void layout(int l, int t, int r, int b) {
		// TODO Auto-generated method stub

		super.layout(l, t, r, b);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		Log.i("swj", "left:" + left + "     top:" + top + "     right:" + right + "     bottom:" + bottom);
		super.onLayout(changed, left, top, right, bottom);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		if(pointCount==0)
			 return;
		canvas.drawColor(Color.TRANSPARENT);
		paint.setTextSize(Utils.convertDipOrPx(mContext,58));
		paint.setColor(Color.WHITE);
		
//		indexs.clear();
		if(indexs.size()==0)
			calcDistance();
		canvas.drawText(caclTitle(paint, title), 0, getHeight() / 2, paint);
		
		
		
//		paint.setColor(Color.rgb(0x80, 0x47, 0xc6));
		paint.setColor(Color.TRANSPARENT);
		Rect rect=new Rect();
		rect.bottom=indexs.get(0).bottom+4;
		rect.top=indexs.get(0).top-4;
		rect.left=indexs.get(0).left-12;

		rect.right = indexs.get(indexs.size() - 1).right + 12;

		canvas.drawCircle(rect.left, (rect.top + rect.bottom) / 2, (rectLength + 8) / 2, paint);
		canvas.drawCircle(rect.right, (rect.top + rect.bottom) / 2, (rectLength + 8) / 2, paint);
		canvas.drawRect(rect, paint);

//		paint.setTextAlign(Paint.Align.RIGHT);


//		paint.setColor(Color.GRAY);
//		paint.setStyle(Style.STROKE);
//		paint.setStrokeWidth(1);
		paint.setColor(Color.rgb(0xee,0xee,0xee));
//		paint.setAlpha((int) (0xff * 0.8));
		for (Rect r : indexs) {
//			canvas.drawRect(r, paint);
			canvas.drawCircle((r.right+r.left)/2,(r.bottom+r.top)/2, (r.right-r.left)/2,paint);
		}
		paint.setColor(Color.WHITE);
		paint.setAlpha(0xff);
		Rect currentRect=indexs.get(currentPointIndex);

		canvas.drawCircle((currentRect.right + currentRect.left) / 2, (currentRect.bottom + currentRect.top) / 2, (currentRect.right - currentRect.left)/2,paint);
	}
	
	private void calcDistance(){
		if(pointCount==0)
			return;
		for(int i=2;i<=pointCount;i++){
			distant-=distant/i;
		}
		
		
		int sumLength=(pointCount-1)*distant+rectLength*pointCount;
		
//		int middle=getWidth()-200;
		
		startX=(getWidth()-sumLength)/2;
		for (int i=0;i<pointCount;i++) {
			Rect r=new Rect();
			r.bottom=getHeight()-5;
			r.top=r.bottom-rectLength;
			r.left=startX+i*distant+i*rectLength;
			r.right=r.left+rectLength;
			indexs.add(r);
		}
	}
	
	private String caclTitle(Paint paint,String title){
		float []point=new float[1];
		paint.getTextWidths(".", point);
		int add=(int)Math.ceil(point[0])*3;
		
		if(title.length()<1)
			return title;
		
		int textWidth=0;
		float[] charWidth=new float[title.length()];
		paint.getTextWidths(title, charWidth);
		for (int i=0;i<charWidth.length;i++) {
			textWidth+=(int)Math.ceil(charWidth[i]);
			if(textWidth>startX && textWidth+add<=startX)
				return title.substring(0,i)+"...";
			else if(textWidth>startX && textWidth+add>startX)
				return title.substring(0,i-1)+"...";
		}
		return title;
		
	}
	
	public void setCurrentIndex(String title,int i){
		this.title=title;
		currentPointIndex=i;
		invalidate();
	}
	public void setPointCount(int count){
		pointCount=count;
		invalidate();
	}
	
	public void setScroll(int top){
		layout(0, top, getWidth(), 0);
		int widthMeasureSpec=MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
		int heightMeasureSpec=MeasureSpec.makeMeasureSpec(top, MeasureSpec.EXACTLY);
		onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
