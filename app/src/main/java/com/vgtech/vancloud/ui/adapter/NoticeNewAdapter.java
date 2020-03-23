package com.vgtech.vancloud.ui.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vgtech.common.utils.DataUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.Notice;

import java.util.List;

/**
 * 打卡记录适配器
 * Created by shilec on 2016/9/6.
 */
public class NoticeNewAdapter extends BaseQuickAdapter<Notice, BaseViewHolder> {


    public NoticeNewAdapter(int layoutResId, @Nullable List<Notice> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Notice notice) {

        helper.setText(R.id.tv_info_time, DataUtils.dateFormat(notice.create_time));
        helper.setText(R.id.tv_info_title, notice.subject);


    }
}
