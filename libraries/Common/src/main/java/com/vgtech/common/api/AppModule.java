package com.vgtech.common.api;

import java.util.List;

/**
 * Created by vic on 2016/9/21.
 */
public class AppModule extends AbsApiData {
    public String id;
    public String name;
    public String explain;
    public int level;
    public String tag;
    public String url;
    public int flag;
    public  int count;//审批--休假、加班、签卡 未审批数字  1.5.1版本新增字段
    public int resName;
    public int resIcon;
    public int resColor;
    public boolean isOpen() {
        return flag == 1;
    }
    public List<NotifyNoticePermissions> permissions;

    @Override
    public String toString() {
        return "AppModule{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", explain='" + explain + '\'' +
                ", tag='" + tag + '\'' +
                ", resName=" + resName +
                '}';
    }
}
