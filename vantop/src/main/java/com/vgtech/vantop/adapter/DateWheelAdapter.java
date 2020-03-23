package com.vgtech.vantop.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import kankan.wheel.widget.adapters.ArrayWheelAdapter;

/**
* @author xuanqiang
* @date 13-7-5
*/
public class DateWheelAdapter extends ArrayWheelAdapter<String> {
  int currentItem;
  int currentValue;
  public DateWheelAdapter(Context context, String[] items, int current) {
    super(context, items);
    this.currentValue = current;
    setTextSize(16);
  }
  @Override
  protected void configureTextView(TextView view) {
    super.configureTextView(view);
//    if (currentItem == currentValue) {
//      view.setTextColor(0xFF0000F0);
//    }
//      view.setTypeface(Typeface.SANS_SERIF);
  }
  @Override
  public View getItem(int index, View cachedView, ViewGroup parent) {
    currentItem = index;
    return super.getItem(index, cachedView, parent);
  }
}
