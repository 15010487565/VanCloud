package com.vgtech.vancloud.ui.module.ad;

import java.util.List;

/**
 * Data:  2017/7/12
 * Auther: 陈占洋
 * Description:
 */

public class HorizontalAD {

    /**
     * result : true
     * code : 200
     * msg :
     * data : {"data":[{"id":"3","type":"2","type_msg":"广告类型：1闪屏广告，2横屏广告","img_url":"http://app.vgsaas.com/appstatic/advertisement/000485-2-1080-1.png","img_href":"https://www.vgsaas.com/","sort":5,"msg":"万客默认横屏广告","show":true},{"id":"4","type":"2","type_msg":"广告类型：1闪屏广告，2横屏广告","img_url":"http://app.vgsaas.com/appstatic/advertisement/000485-2-1080-2.png","img_href":"https://www.vgsaas.com/","sort":10,"msg":"万客默认横屏广告","show":true},{"id":"5","type":"2","type_msg":"广告类型：1闪屏广告，2横屏广告","img_url":"http://app.vgsaas.com/appstatic/advertisement/000485-2-1080-3.png","img_href":"https://www.vgsaas.com/","sort":15,"msg":"万客默认横屏广告","show":true},{"id":"6","type":"2","type_msg":"广告类型：1闪屏广告，2横屏广告","img_url":"http://app.vgsaas.com/appstatic/advertisement/000485-2-1080-4.png","img_href":"https://www.vgsaas.com/","sort":20,"msg":"万客默认横屏广告","show":true},{"id":"7","type":"2","type_msg":"广告类型：1闪屏广告，2横屏广告","img_url":"http://app.vgsaas.com/appstatic/advertisement/000485-2-1080-5.png","img_href":"https://www.vgsaas.com/","sort":25,"msg":"万客默认横屏广告","show":true}],"is_show":true}
     */

    private boolean result;
    private int code;
    private String msg;
    private DataBeanX data;

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

    public DataBeanX getData() {
        return data;
    }

    public void setData(DataBeanX data) {
        this.data = data;
    }

    public static class DataBeanX {
        /**
         * data : [{"id":"3","type":"2","type_msg":"广告类型：1闪屏广告，2横屏广告","img_url":"http://app.vgsaas.com/appstatic/advertisement/000485-2-1080-1.png","img_href":"https://www.vgsaas.com/","sort":5,"msg":"万客默认横屏广告","show":true},{"id":"4","type":"2","type_msg":"广告类型：1闪屏广告，2横屏广告","img_url":"http://app.vgsaas.com/appstatic/advertisement/000485-2-1080-2.png","img_href":"https://www.vgsaas.com/","sort":10,"msg":"万客默认横屏广告","show":true},{"id":"5","type":"2","type_msg":"广告类型：1闪屏广告，2横屏广告","img_url":"http://app.vgsaas.com/appstatic/advertisement/000485-2-1080-3.png","img_href":"https://www.vgsaas.com/","sort":15,"msg":"万客默认横屏广告","show":true},{"id":"6","type":"2","type_msg":"广告类型：1闪屏广告，2横屏广告","img_url":"http://app.vgsaas.com/appstatic/advertisement/000485-2-1080-4.png","img_href":"https://www.vgsaas.com/","sort":20,"msg":"万客默认横屏广告","show":true},{"id":"7","type":"2","type_msg":"广告类型：1闪屏广告，2横屏广告","img_url":"http://app.vgsaas.com/appstatic/advertisement/000485-2-1080-5.png","img_href":"https://www.vgsaas.com/","sort":25,"msg":"万客默认横屏广告","show":true}]
         * is_show : true
         */

        private boolean is_show;
        private List<DataBean> data;

        public boolean isIs_show() {
            return is_show;
        }

        public void setIs_show(boolean is_show) {
            this.is_show = is_show;
        }

        public List<DataBean> getData() {
            return data;
        }

        public void setData(List<DataBean> data) {
            this.data = data;
        }

        public static class DataBean {
            /**
             * id : 3
             * type : 2
             * type_msg : 广告类型：1闪屏广告，2横屏广告
             * img_url : http://app.vgsaas.com/appstatic/advertisement/000485-2-1080-1.png
             * img_href : https://www.vgsaas.com/
             * sort : 5
             * msg : 万客默认横屏广告
             * show : true
             */

            private String id;
            private String type;
            private String type_msg;
            private String img_url;
            private String img_href;
            private int sort;
            private String msg;
            private boolean show;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getType_msg() {
                return type_msg;
            }

            public void setType_msg(String type_msg) {
                this.type_msg = type_msg;
            }

            public String getImg_url() {
                return img_url;
            }

            public void setImg_url(String img_url) {
                this.img_url = img_url;
            }

            public String getImg_href() {
                return img_href;
            }

            public void setImg_href(String img_href) {
                this.img_href = img_href;
            }

            public int getSort() {
                return sort;
            }

            public void setSort(int sort) {
                this.sort = sort;
            }

            public String getMsg() {
                return msg;
            }

            public void setMsg(String msg) {
                this.msg = msg;
            }

            public boolean isShow() {
                return show;
            }

            public void setShow(boolean show) {
                this.show = show;
            }
        }
    }
}
