package com.vgtech.vantop.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.UserAccount;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.utils.DateTimeUtil;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.vantop.R;
import com.vgtech.vantop.moudle.PunchCardListData;
import com.vgtech.vantop.moudle.Record;
import com.vgtech.vantop.ui.punchcard.CardInfoActivity;
import com.vgtech.vantop.ui.punchcard.CardInfoByDayActivity;
import com.vgtech.vantop.ui.userinfo.VantopUserInfoActivity;
import com.vgtech.vantop.utils.PreferencesController;
import com.vgtech.vantop.utils.VanTopUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 打卡记录适配器
 * Created by shilec on 2016/9/6.
 */
public class PunchcardRecordListAdapter extends AbsViewAdapter<Record> implements AdapterView.OnItemClickListener,View.OnClickListener {
    public PunchcardRecordListAdapter(Context context, List<Record> datas) {
        super(context, datas);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        Holder holder = new Holder(itemView);
        holder.tvDate = (TextView) holder.itemView.findViewById(R.id.tv_date);
        holder.igView = (NoScrollGridview) holder.itemView.findViewById(R.id.gridview);
        holder.itemcard_byday = itemView.findViewById(R.id.itemcard_byday);
        holder.itemcard_byday.setOnClickListener(this);
        holder.igView.setItemClick(true);
        holder.igView.setOnItemClickListener(this);
        return holder;
    }

    @Override
    protected void onBindData(ViewHolder holder, int posistion) {
        Holder h = (Holder) holder;
        Record data = mDatas.get(posistion);
        h.tvDate.setTag(data);
        h.tvDate.setText(data.date + " " + DateTimeUtil.getWeekOfDate(mContext, data.date));
        List<PunchCardListData> punchCardListDatas = new ArrayList<>();
        punchCardListDatas.addAll(data.cards);
        Collections.reverse(punchCardListDatas);
        TimeAdapter imageGridviewAdapter = new TimeAdapter(mContext, punchCardListDatas);
        h.igView.setAdapter(imageGridviewAdapter);
    }

    @Override
    protected int onInflateItemView() {
        return R.layout.punchcardrecord_list_item;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PunchCardListData punch = (PunchCardListData) parent.getItemAtPosition(position);
        JSONObject jsonObject = punch.jsonObject;
        if (jsonObject != null) {
            Intent intent = new Intent(mContext, CardInfoActivity.class);
            intent.putExtra("cardinfo", jsonObject.toString());
            mContext.startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        Holder h = (Holder) v.getTag();
        Record record = (Record) h.tvDate.getTag();
        Intent intent = new Intent(mContext,CardInfoByDayActivity.class);
        intent.putExtra("date",record.date);
        intent.putParcelableArrayListExtra("cards",record.cards);
        mContext.startActivity(intent);
    }

    private class Holder extends ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }

        TextView tvDate;
        NoScrollGridview igView;
        View itemcard_byday;
    }
}
