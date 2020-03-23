package com.vgtech.vancloud.ui.module.luntan;

import java.io.Serializable;
import java.util.List;

/**
 * Data:  2017/8/4
 * Auther: 陈占洋
 * Description:
 */

public class LuntanList {


    /**
     * result : true
     * code : 200
     * msg :
     * data : {"pageSize":20,"total":7,"pageNo":0,"rows":[{"id":"2","tenantId":"735893790648176640","userId":"609126835896193024","title":"第二次发帖","content":"哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈","visitorCount":0,"replyCount":1,"createTime":"1501841777323","updateTime":"1501841777323","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"},{"id":"1","tenantId":"735893790648176640","userId":"609126835896193024","title":"第一次发帖","content":"哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈","visitorCount":3,"replyCount":6,"createTime":"1501840932098","updateTime":"1501840932098","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"}],"pageCount":1,"fromIndex":-20}
     */

    private boolean result;
    private int code;
    private String msg;
    private DataBean data;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * pageSize : 20
         * total : 7
         * pageNo : 0
         * rows : [{"id":"2","tenantId":"735893790648176640","userId":"609126835896193024","title":"第二次发帖","content":"哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈","visitorCount":0,"replyCount":1,"createTime":"1501841777323","updateTime":"1501841777323","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"},{"id":"1","tenantId":"735893790648176640","userId":"609126835896193024","title":"第一次发帖","content":"哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈","visitorCount":3,"replyCount":6,"createTime":"1501840932098","updateTime":"1501840932098","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"}]
         * pageCount : 1
         * fromIndex : -20
         */

        private int pageSize;
        private int total;
        private int pageNo;
        private int pageCount;
        private int fromIndex;
        private List<RowsBean> rows;

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getPageNo() {
            return pageNo;
        }

        public void setPageNo(int pageNo) {
            this.pageNo = pageNo;
        }

        public int getPageCount() {
            return pageCount;
        }

        public void setPageCount(int pageCount) {
            this.pageCount = pageCount;
        }

        public int getFromIndex() {
            return fromIndex;
        }

        public void setFromIndex(int fromIndex) {
            this.fromIndex = fromIndex;
        }

        public List<RowsBean> getRows() {
            return rows;
        }

        public void setRows(List<RowsBean> rows) {
            this.rows = rows;
        }

        public static class RowsBean implements Serializable{
            /**
             * id : 2
             * tenantId : 735893790648176640
             * userId : 609126835896193024
             * title : 第二次发帖
             * content : 哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈
             * visitorCount : 0
             * replyCount : 1
             * createTime : 1501841777323
             * updateTime : 1501841777323
             * logo : http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg
             * username : 程露
             */

            private String id;
            private String tenantId;
            private String userId;
            private String title;
            private String content;
            private int visitorCount;
            private int replyCount;
            private String createTime;
            private String updateTime;
            private String logo;
            private String username;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getTenantId() {
                return tenantId;
            }

            public void setTenantId(String tenantId) {
                this.tenantId = tenantId;
            }

            public String getUserId() {
                return userId;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public int getVisitorCount() {
                return visitorCount;
            }

            public void setVisitorCount(int visitorCount) {
                this.visitorCount = visitorCount;
            }

            public int getReplyCount() {
                return replyCount;
            }

            public void setReplyCount(int replyCount) {
                this.replyCount = replyCount;
            }

            public String getCreateTime() {
                return createTime;
            }

            public void setCreateTime(String createTime) {
                this.createTime = createTime;
            }

            public String getUpdateTime() {
                return updateTime;
            }

            public void setUpdateTime(String updateTime) {
                this.updateTime = updateTime;
            }

            public String getLogo() {
                return logo;
            }

            public void setLogo(String logo) {
                this.logo = logo;
            }

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }
        }
    }
}
