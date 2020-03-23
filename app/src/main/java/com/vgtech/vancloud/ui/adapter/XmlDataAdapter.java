package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.common.adapter.BasicArrayAdapter;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.utils.XMLResParser;


/**
 * 列表适配器
 *
 * @param <XMLResData>
 * @author zhangshaofang
 */
public class XmlDataAdapter<XMLResData> extends BasicArrayAdapter<XMLResData> {
    private static final int VIEWTYPE_JOBITEM = 1;


    public XmlDataAdapter(Context context) {
        super(context);
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
        XMLResData data = getItem(position);
        int viewType = getItemViewType(data);
        if (convertView == null) {
            view = getItemView(parent, viewType, true);
        } else {
            view = convertView;
        }
        fillItemView(view, viewType, data, position);
        return view;
    }

    public View getItemView(XMLResData data) {
        int viewType = getItemViewType(data);
        View view = getItemView(null, viewType, true);
        fillItemView(view, viewType, data, 0);
        return view;
    }

    private void fillItemView(View view, int type, XMLResData data, int position) {
        @SuppressWarnings("unchecked") final
        SparseArray<View> viewMap = (SparseArray<View>) view.getTag();


        int id = getViewResId(type);
        switch (id) {
            case R.layout.menu_item: {
                XMLResParser.MenuItem jobItem = (XMLResParser.MenuItem) data;
                TextView nameTv = (TextView) viewMap.get(R.id.tv_menu);
                ImageView iconIv = (ImageView) viewMap.get(R.id.ic_menu);
                try {
                    nameTv.setText(jobItem.getLabel());
                } catch (Exception e) {
                    e.printStackTrace();
                    nameTv.setText(jobItem.getLabel()+"");
                }
                iconIv.setImageResource(jobItem.getIcon());
            }
            break;
            default:
                /**
                 * ignore
                 */
                break;

        }
    }


    private View getItemView(ViewGroup parent, int type, boolean visible) {
        int id = getViewResId(type);
        View view = mInflater.inflate(id, parent, false);
        SparseArray<View> viewMap = new SparseArray<View>();
        switch (id) {
            case R.layout.menu_item:
                putViewMap(viewMap, view, R.id.ic_menu);
                putViewMap(viewMap, view, R.id.tv_menu);
                break;
            default:
                /**
                 * ignore
                 */
                break;

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
    private int getItemViewType(XMLResData data) {
        int result = -1;
        if (data instanceof XMLResParser.MenuItem) {
            result = VIEWTYPE_JOBITEM;
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
            case VIEWTYPE_JOBITEM:
                result = R.layout.menu_item;
                break;
            default:
                /**
                 * ignore
                 */
                break;
        }
        return result;
    }

}
