package com.vgtech.vancloud.api;

import com.vgtech.common.api.AbsApiData;

/**
 * Created by vic on 2017/3/29.
 */
public class NoticeInfo extends AbsApiData{
    public String content;
    public String subject;
    public boolean is_confim;
    public String mod_dates;
    public String attachment;
    public String attachment_ext;
    public String confirm_dates;
}
