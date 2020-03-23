package com.vgtech.vancloud.ui.common.record;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.common.publish.internal.Recorder;
import com.vgtech.vancloud.utils.Utils;

import java.util.List;

public class RecorderAdapter extends ArrayAdapter<Recorder> {


    private LayoutInflater inflater;

    private View.OnClickListener mListener;
    private List<Recorder> datas;
    private int mMaxInner;
    private int mMaxOutter;
    private int mMinLength;
    public RecorderAdapter(Context context, List<Recorder> dataList, View.OnClickListener listener) {
        super(context, -1, dataList);
        int maxWidth = context.getResources().getDisplayMetrics().widthPixels - Utils.convertDipOrPx(context, 160);
        mMinLength = Utils.convertDipOrPx(context,25);
        mMaxInner = (int) (maxWidth*0.7f/10);
        mMaxOutter = (int) (maxWidth*0.3f/50);
        datas = dataList;
        inflater = LayoutInflater.from(context);
        mListener = listener;
        // 获取系统宽度
        WindowManager wManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wManager.getDefaultDisplay().getMetrics(outMetrics);
    }

    public List<Recorder> getData() {
        return datas;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_layout, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.seconds = (TextView) convertView.findViewById(R.id.recorder_time);
            viewHolder.length = convertView.findViewById(R.id.recorder_length);
            viewHolder.deleteView = convertView.findViewById(R.id.btn_voice_delete);
            viewHolder.deleteView.setOnClickListener(mListener);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.deleteView.setTag(getItem(position));
        viewHolder.seconds.setText(Math.round(getItem(position).getTime()) + "\"");
        ViewGroup.LayoutParams lParams = viewHolder.length.getLayoutParams();
        int duration = getItem(position).getTime();
        if (duration>60)
            duration = 60;
        int inner=0,outter=0;
        if(duration<=10)
        {
            inner = duration;
        }else
        {
            inner = 10;
            outter = duration-10;
        }

        lParams.width = inner*mMaxInner+outter*mMaxOutter+mMinLength;
        viewHolder.length.setLayoutParams(lParams);

        return convertView;
    }

    class ViewHolder {
        TextView seconds;// 时间
        View length;// 对话框长度
        View deleteView;
    }

}
