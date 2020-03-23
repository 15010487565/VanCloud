package com.vgtech.common.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vgtech.common.R;
import com.vgtech.common.api.Dict;
import com.vgtech.common.api.ScheduleItem;

import java.util.ArrayList;
import java.util.List;


/**
 * 列表适配器
 *
 * @param <AbsApiData>
 * @author zhangshaofang
 */
public class ComAdapter<AbsApiData> extends BasicArrayAdapter<AbsApiData> implements View.OnClickListener {
    private static final int VIEWTYPE_AREA = 1;
    public ComAdapter(Context context) {
        super(context);
        mSelectData = new ArrayList<AbsApiData>();
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        AbsApiData data = getItem(position);
        int viewType = getItemViewType(data);
        if (convertView == null) {
            view = getItemView(parent, viewType, true);
        } else {
            view = convertView;
        }
        fillItemView(view, viewType, data, position);
        return view;
    }

    public View getItemView(AbsApiData data) {
        int viewType = getItemViewType(data);
        View view = getItemView(null, viewType, true);
        fillItemView(view, viewType, data, 0);
        return view;
    }

    private void fillItemView(View view, int type, AbsApiData data, int position) {
        @SuppressWarnings("unchecked") final
        SparseArray<View> viewMap = (SparseArray<View>) view.getTag();
        int id = getViewResId(type);
        if (id == R.layout.area_item) {
            Dict area = (Dict) data;
            TextView nameTv = (TextView) viewMap.get(R.id.area_name);
            nameTv.setText(area.name);
            View arrow = viewMap.get(R.id.ic_arrow);
            arrow.setVisibility(area.getArrayData(Dict.class).isEmpty()?View.GONE:View.VISIBLE);
            View selectedView = viewMap.get(R.id.iv_selected);
            selectedView.setVisibility(mSelectData.contains(area)?View.VISIBLE:View.GONE);
        }
    }

    /**
     * 选中数据数组
     */
    private List<AbsApiData> mSelectData;

    public List<AbsApiData> getSelectData() {
        return mSelectData;
    }
    public void addSelected(AbsApiData data)
    {
        mSelectData.add(data);
        notifyDataSetChanged();
    }

    /**
     * 初始化页面
     *
     * @param parent
     * @param type
     * @param visible
     * @return
     */
    private View getItemView(ViewGroup parent, int type, boolean visible) {
        int id = getViewResId(type);
        View view = mInflater.inflate(id, parent, false);
        SparseArray<View> viewMap = new SparseArray<View>();
        if (id == R.layout.area_item) {
            putViewMap(viewMap, view, R.id.area_name);
            putViewMap(viewMap, view, R.id.ic_arrow);
            putViewMap(viewMap, view, R.id.line);
            putViewMap(viewMap, view, R.id.iv_selected);
        }
        view.setTag(viewMap);
        return view;
    }

    private View putViewMap(SparseArray<View> viewMap, View view, int id) {
        View v = view.findViewById(id);
        viewMap.put(id, v);
        return v;
    }

    /**
     * 配置数据页面类型
     *
     * @param data
     * @return
     */
    private int getItemViewType(AbsApiData data) {
        int result = -1;
        if (data instanceof Dict) {
            result = VIEWTYPE_AREA;
        }
        return result;
    }

    /**
     * 根据类型配置页面
     *
     * @param type
     * @return
     */
    private int getViewResId(int type) {
        int result = 1;
        switch (type) {
            case VIEWTYPE_AREA:
                result = R.layout.area_item;
                break;

            default:
                /**
                 * ignore
                 */
                break;
        }
        return result;
    }

    private ScheduleItem mScheduleItem;


    @Override
    public void onClick(View v) {
        switch (v.getId()) {


        }
    }
}
