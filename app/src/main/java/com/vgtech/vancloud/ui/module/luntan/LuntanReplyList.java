package com.vgtech.vancloud.ui.module.luntan;

import java.util.List;

/**
 * Data:  2017/8/9
 * Auther: 陈占洋
 * Description:
 */

public class LuntanReplyList {

    /**
     * result : true
     * code : 200
     * msg :
     * data : {"pageSize":20,"total":6,"pageNo":1,"rows":[{"id":"1","tenantId":"735893790648176640","userId":"609126835896193024","topicId":"1","replyContent":"啦啦啦啦啦啦啦啦啦","floorNum":1,"createTime":"1501841343523","updateTime":"1501841343523","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"},{"id":"2","tenantId":"735893790648176640","userId":"609126835896193024","topicId":"1","replyContent":"啦啦啦啦啦啦啦啦啦","floorNum":2,"createTime":"1501841377970","updateTime":"1501841377970","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"},{"id":"3","tenantId":"735893790648176640","userId":"609126835896193024","topicId":"1","replyContent":"啦啦啦啦啦啦啦啦啦","floorNum":3,"createTime":"1501841580602","updateTime":"1501841580602","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"},{"id":"4","tenantId":"735893790648176640","userId":"609126835896193024","topicId":"1","replyContent":"啦啦啦啦啦啦啦啦啦","floorNum":4,"createTime":"1501841755990","updateTime":"1501841755990","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"},{"id":"5","tenantId":"735893790648176640","userId":"609126835896193024","topicId":"1","replyContent":"恢复回复啦","floorNum":5,"createTime":"1502078315484","updateTime":"1502078315484","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"},{"id":"6","tenantId":"735893790648176640","userId":"609126835896193024","topicId":"1","replyContent":"恢复回复啦","floorNum":6,"createTime":"1502078510462","updateTime":"1502078510462","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"}],"pageCount":1,"fromIndex":0}
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
         * total : 6
         * pageNo : 1
         * rows : [{"id":"1","tenantId":"735893790648176640","userId":"609126835896193024","topicId":"1","replyContent":"啦啦啦啦啦啦啦啦啦","floorNum":1,"createTime":"1501841343523","updateTime":"1501841343523","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"},{"id":"2","tenantId":"735893790648176640","userId":"609126835896193024","topicId":"1","replyContent":"啦啦啦啦啦啦啦啦啦","floorNum":2,"createTime":"1501841377970","updateTime":"1501841377970","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"},{"id":"3","tenantId":"735893790648176640","userId":"609126835896193024","topicId":"1","replyContent":"啦啦啦啦啦啦啦啦啦","floorNum":3,"createTime":"1501841580602","updateTime":"1501841580602","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"},{"id":"4","tenantId":"735893790648176640","userId":"609126835896193024","topicId":"1","replyContent":"啦啦啦啦啦啦啦啦啦","floorNum":4,"createTime":"1501841755990","updateTime":"1501841755990","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"},{"id":"5","tenantId":"735893790648176640","userId":"609126835896193024","topicId":"1","replyContent":"恢复回复啦","floorNum":5,"createTime":"1502078315484","updateTime":"1502078315484","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"},{"id":"6","tenantId":"735893790648176640","userId":"609126835896193024","topicId":"1","replyContent":"恢复回复啦","floorNum":6,"createTime":"1502078510462","updateTime":"1502078510462","logo":"http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg","username":"程露"}]
         * pageCount : 1
         * fromIndex : 0
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

        public static class RowsBean {
            /**
             * id : 1
             * tenantId : 735893790648176640
             * userId : 609126835896193024
             * topicId : 1
             * replyContent : 啦啦啦啦啦啦啦啦啦
             * floorNum : 1
             * createTime : 1501841343523
             * updateTime : 1501841343523
             * logo : http://app.vancloud.com/resource/802315035345948672/user/images/804401354071085056.jpg
             * username : 程露
             */

            private String id;
            private String tenantId;
            private String userId;
            private String topicId;
            private String replyContent;
            private int floorNum;
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

            public String getTopicId() {
                return topicId;
            }

            public void setTopicId(String topicId) {
                this.topicId = topicId;
            }

            public String getReplyContent() {
                return replyContent;
            }

            public void setReplyContent(String replyContent) {
                this.replyContent = replyContent;
            }

            public int getFloorNum() {
                return floorNum;
            }

            public void setFloorNum(int floorNum) {
                this.floorNum = floorNum;
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
