package com.vgtech.common.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.R;
import com.vgtech.common.api.Course;
import com.vgtech.common.api.CourseItem;
import com.vgtech.common.api.Dict;
import com.vgtech.common.api.IdName;
import com.vgtech.common.api.Keyword;
import com.vgtech.common.ui.ItemClickListener;
import com.vgtech.common.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 列表适配器
 *
 * @param <AbsApiData>
 * @author zhangshaofang
 */
public class DataAdapter<AbsApiData> extends BasicArrayAdapter<AbsApiData> implements View.OnClickListener {
    private static final int VIEWTYPE_IDNAME = 1;
    private static final int VIEWTYPE_DICT = 2;
    private static final int VIEWTYPE_COURSE = 3;
    private static final int VIEWTYPE_COURSE_ITEM = 4;
    private static final int VIEWTYPE_KEYWORD = 5;
    private int mWh;

    public DataAdapter(Context context) {
        super(context);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        mWh = (width - CommonUtils.convertDipOrPx(context, 15)) / 2;
    }

    private ItemClickListener mItemClickListener;

    public DataAdapter(Context context, ItemClickListener listener) {
        super(context);
        mItemClickListener = listener;
        int width = context.getResources().getDisplayMetrics().widthPixels;
        mWh = (width - CommonUtils.convertDipOrPx(context, 15)) / 2;
    }

    private List<String> mSelectedList = new ArrayList<>();

    public void setSelectedId(String selectedId) {
        mSelectedList.clear();
        mSelectedList.add(selectedId);
    }

    public List<String> getSelectedList() {
        return mSelectedList;
    }

    public void addSelectedId(String selectedId) {
        mSelectedList.add(selectedId);
    }

    public void addSelected(List<String> ids) {
        mSelectedList.addAll(ids);
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
        if (id == R.layout.data_item) {
            IdName area = (IdName) data;
            TextView nameTv = (TextView) viewMap.get(R.id.area_name);
            nameTv.setText(area.name);
            View arrow = viewMap.get(R.id.ic_arrow);
            arrow.setVisibility(area.getArrayData(Dict.class).isEmpty() ? View.GONE : View.VISIBLE);
            View line = viewMap.get(R.id.line);
            line.setVisibility(position == getCount() - 1 ? View.GONE : View.VISIBLE);
            View selectIv = viewMap.get(R.id.iv_selected);
            selectIv.setVisibility(mSelectedList.contains(area.getId()) ? View.VISIBLE : View.GONE);
        } else if (id == R.layout.dict_item) {
            Dict area = (Dict) data;
            View item_spit = viewMap.get(R.id.item_spit);
            View lineView = viewMap.get(R.id.line);
            TextView titleTv = (TextView) viewMap.get(R.id.item_title);
            TextView subTitleTv = (TextView) viewMap.get(R.id.sub_title);
            subTitleTv.setText(area.subSelect <= 0 ? "" : "已选" + area.subSelect + "个");
            if (!TextUtils.isEmpty(area.title)) {
                item_spit.setVisibility(View.VISIBLE);
                titleTv.setVisibility(View.VISIBLE);
                titleTv.setText(area.title);
            } else {
                item_spit.setVisibility(View.GONE);
                titleTv.setVisibility(View.GONE);
            }
            lineView.setVisibility("all".equals(area.getId()) ? View.GONE : View.VISIBLE);
            TextView nameTv = (TextView) viewMap.get(R.id.area_name);
            nameTv.setText(area.name);
            nameTv.setTag(area);
            View selectIv = viewMap.get(R.id.iv_selected);
            selectIv.setVisibility(mSelectedList.contains(area.getId()) ? View.VISIBLE : View.GONE);
            View arrowIv = viewMap.get(R.id.ic_arrow);
            arrowIv.setVisibility(area.getArrayData(Dict.class).isEmpty() || area.isAll ? View.GONE : View.VISIBLE);
            if (area.subSelect == -1) {
                subTitleTv.setText("全部");
                selectIv.setVisibility(View.GONE);
            }

        } else if (id == R.layout.course_item) {
            SimpleDraweeView imgIv = (SimpleDraweeView) viewMap.get(R.id.img);
            TextView titleTv = (TextView) viewMap.get(R.id.title);
            Course course = (Course) data;
            titleTv.setText(course.title);
            imgIv.setLayoutParams(new LinearLayout.LayoutParams(mWh, mWh));
            imgIv.setImageURI(course.img);

        } else if (id == R.layout.courseitem) {
            TextView titleTv = (TextView) viewMap.get(R.id.title);
            TextView timeTv = (TextView) viewMap.get(R.id.time);
            CourseItem course = (CourseItem) data;
            titleTv.setText(course.title);
            timeTv.setText(course.length);
        } else if (id == R.layout.keyword_item) {
            Keyword keyword = (Keyword) data;
            TextView titleTv = (TextView) viewMap.get(R.id.tv_keyword);
            titleTv.setText(keyword.title);
        }
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
        if (id == R.layout.data_item) {
            putViewMap(viewMap, view, R.id.area_name);
            putViewMap(viewMap, view, R.id.ic_arrow);
            putViewMap(viewMap, view, R.id.line);
            putViewMap(viewMap, view, R.id.iv_selected);
        } else if (id == R.layout.dict_item) {
            putViewMap(viewMap, view, R.id.item_spit);
            putViewMap(viewMap, view, R.id.item_title);
            putViewMap(viewMap, view, R.id.sub_title);
            putViewMap(viewMap, view, R.id.line);
            View nameView = putViewMap(viewMap, view, R.id.area_name);
            if (mItemClickListener != null)
                nameView.setOnClickListener(this);
            putViewMap(viewMap, view, R.id.iv_selected);
            putViewMap(viewMap, view, R.id.ic_arrow);
        } else if (id == R.layout.course_item) {
            putViewMap(viewMap, view, R.id.img);
            putViewMap(viewMap, view, R.id.title);
        } else if (id == R.layout.courseitem) {
            putViewMap(viewMap, view, R.id.time);
            putViewMap(viewMap, view, R.id.title);
        } else if (id == R.layout.keyword_item) {
            putViewMap(viewMap, view, R.id.tv_keyword);
            putViewMap(viewMap, view, R.id.ic_arrow);
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
        if (data instanceof IdName) {
            result = VIEWTYPE_IDNAME;
        } else if (data instanceof Dict) {
            result = VIEWTYPE_DICT;
        } else if (data instanceof Course) {
            result = VIEWTYPE_COURSE;
        } else if (data instanceof CourseItem) {
            result = VIEWTYPE_COURSE_ITEM;
        } else if (data instanceof Keyword) {
            result = VIEWTYPE_KEYWORD;
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
            case VIEWTYPE_KEYWORD:
                result = R.layout.keyword_item;
                break;
            case VIEWTYPE_IDNAME:
                result = R.layout.data_item;
                break;
            case VIEWTYPE_DICT:
                result = R.layout.dict_item;
                break;
            case VIEWTYPE_COURSE:
                result = R.layout.course_item;
                break;
            case VIEWTYPE_COURSE_ITEM:
                result = R.layout.courseitem;
                break;
            default:
                /**
                 * ignore
                 */
                break;
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        int resId = v.getId();
        if (resId == R.id.area_name) {
            if (mItemClickListener != null)
                mItemClickListener.onItemClick(v.getTag());
        }
    }
}
