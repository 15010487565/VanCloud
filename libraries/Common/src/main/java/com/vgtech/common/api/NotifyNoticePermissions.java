package com.vgtech.common.api;

import java.io.Serializable;
import java.util.List;

/**
 * Data:  2019/4/24
 * Auther: xcd
 * Description:
 */
public class NotifyNoticePermissions extends AbsApiData {

    /**
     * id : 10066
     * name : 公告评论
     * explain : 公告评论
     * tag : notice:comments
     * url :
     * permissions : []
     */

    private String id;
    private String name;
    private String explain;
    private String tag;
    private String url;
//    private List<?> permissions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

//    public List<?> getPermissions() {
//        return permissions;
//    }
//
//    public void setPermissions(List<?> permissions) {
//        this.permissions = permissions;
//    }
}
