package com.vgtech.common.api;

/**
 * Created by Duke on 2015/9/15.
 */
public class WorkReport extends AbsApiData {

    public String workreportid;
    public String type;
    public long startdate;
    public long enddate;
    public String title;
    public String content;
    public long timestamp;
    public int comments;
    public int praises;
    public int subtype; //（0我点评的，1我发出的，2抄送给我的）
    public boolean ispraise;
    public String repealstate;//"2", 代表撤销  1 代表正常使用

}
