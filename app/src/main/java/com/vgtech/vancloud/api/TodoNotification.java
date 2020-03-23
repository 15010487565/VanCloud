package com.vgtech.vancloud.api;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.vgtech.common.api.AbsApiData;

/**
 * 新版待办
 * Created by Duke on 2016/9/19.
 */
public class TodoNotification extends AbsApiData implements MultiItemEntity {

    public String todo_id;

    public String res_id;

    public String title;

    public String type;

    public long timestamp;

    public String state;

    public String is_read;

    public String is_can_delete;

    public String process_msg;

    public String process_status;

    public String create_user_no;

    @Override
    public int getItemType() {
//        Log.e("TAG_待办modler","type="+type);
        return Integer.valueOf(type);
    }
}
