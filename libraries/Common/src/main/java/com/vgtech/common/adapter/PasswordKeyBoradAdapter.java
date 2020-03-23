package com.vgtech.common.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vgtech.common.R;
import com.vgtech.common.api.Item;

/**
 * Created by Nick on 2015/12/28.
 */
public class PasswordKeyBoradAdapter extends BaseAdapter {

    private final String speals[]=new String []{"ABC","DEF","GHI","JKL","MNO","PQRS","TUV","WZYV"};

    private Activity mContext;
    private LayoutInflater inflater;

    private PressListener listener;

    public PasswordKeyBoradAdapter(Activity mContext,PressListener listener) {
        this.mContext = mContext;
        this.listener=listener;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return 12;
    }

    @Override
    public Item getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.keyborad_item, null);

            mViewHolder.num = (TextView)convertView.findViewById(R.id.num);
            mViewHolder.delete = (ImageView)convertView.findViewById(R.id.delete);
            mViewHolder.speal= (TextView)convertView.findViewById(R.id.speal);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        if(position>=0 && position<9){
            mViewHolder.num.setVisibility(View.VISIBLE);
            mViewHolder.delete.setVisibility(View.GONE);
            mViewHolder.num.setText((position + 1) + "");
            if(position>0 && position<9)
                mViewHolder.speal.setText(speals[position-1]);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.pressKey(((position + 1) + "").charAt(0));
                }
            });
        }else if(position==10){
            mViewHolder.num.setVisibility(View.VISIBLE);
            mViewHolder.delete.setVisibility(View.GONE);
            mViewHolder.num.setText("0");
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.pressKey('0');
                }
            });
        }else if(position==9){
            convertView.setBackgroundColor(Color.rgb(0xed,0xee,0xef));
            convertView.setClickable(false);
        }else if(position==11){
            mViewHolder.num.setVisibility(View.GONE);
            mViewHolder.delete.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(Color.rgb(0xed, 0xee, 0xef));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.pressKey('-');
                }
            });
        }


        return convertView;
    }

    class ViewHolder {
        TextView num;
        TextView speal;
        ImageView delete;
    }

    public  interface PressListener{
        void pressKey(Character character);
    }
}
