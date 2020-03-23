package com.vgtech.vancloud.ui.adapter;

import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xuanqiang
 * @date 13-7-10
 */
public abstract class DataAdapter<T> extends BaseAdapter{
  public List<T> dataSource = new ArrayList<T>(0);
  @Override
  public int getCount(){
    return dataSource.size();
  }
  @Override
  public Object getItem(int position){
    return position;
  }
  @Override
  public long getItemId(int position){
    return position;
  }
}
