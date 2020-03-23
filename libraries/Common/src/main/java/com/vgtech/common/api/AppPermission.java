package com.vgtech.common.api;

/**
 * Created by vic on 2016/9/21.
 */
public class AppPermission extends AbsApiData {
    public enum Type {
        order, meeting, beidiao, zhaopin,settings,clock_out,kaoqin
    }

    public enum Order {
        my("orders:my"), all("orders:all"), account("orders:account");
        private String name;

        private Order(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum Meeting {
        join("meeting:join"), call("meeting:call"), appointment("meeting:appointment"), record("meeting:record"), room("meeting:room"), pay("meeting:pay");
        private String name;

        private Meeting(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum Beidiao {
        start("investigate:start"), batch("investigate:batch"), my("investigate:my"), all("investigate:all"), pay("investigate:pay");
        private String name;

        private Beidiao(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    public enum ClockOut {
        punch("clock_out:punch"),//打卡
        punch_record("clock_out:punch_record"); //打卡记录
        private String name;

        private ClockOut(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    public enum Kaoqin {
        wodekaoqin("kaoqin:wodekaoqin"),//我的考勤
        wodepaiban("kaoqin:wodepaiban");//我的排班
        private String name;

        private Kaoqin(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum Zhaopin {
        position("zhaopin:position"), resume("zhaopin:resume"), allresume("zhaopin:allresume"), account("zhaopin:account"), pay("zhaopin:pay");
        private String name;

        private Zhaopin(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    public enum Setting {
        employee("settings:employee"),//编辑员工信息
        company("settings:company"),//编辑公司信息
        organization("settings:organization"),//编辑组织架构
        employeeAdd("settings:employeeAdd"),//添加员工
        employeeInvite ("settings:employeeInvite"),//员工申请管理
        position("settings:position"),//职位管理
        open_permission("open_permission");//职位管理
        private String name;

        private Setting(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum WorkFlow {
        vancloud_flow, vancloud_holiday, vancloud_zhaopin, extra_work, sign_card, vantop_holiday, unknow;

        public static WorkFlow getType(String tag) {
            WorkFlow[] types = WorkFlow.values();
            for (WorkFlow type : types) {
                if (type.toString().equals(tag)) {
                    return type;
                }
            }
            return WorkFlow.unknow;
        }
    }
    public enum Shenqing {
        shenqing_vantop_holiday,shenqing_extra_work,shenqing_sign_card,unknow;

        public static Shenqing getType(String tag) {
            Shenqing[] types = Shenqing.values();
            for (Shenqing type : types) {
                if (type.toString().equals(tag)) {
                    return type;
                }
            }
            return Shenqing.unknow;
        }
    }


    public String id;
    public String name;
    public String explain;
    public String tag;

}
/**
 * [
 * {
 * "id": "8",
 * "name": "订单管理",
 * "explain": "订单管理",
 * "tag": "order",
 * "url": "",
 * "permissions": [
 * {
 * "id": "10009",
 * "name": "我的订单",
 * "explain": "我的订单",
 * "tag": "orders:my",
 * "url": ""
 * },
 * {
 * "id": "10010",
 * "name": "全部订单",
 * "explain": "全部订单",
 * "tag": "orders:all",
 * "url": ""
 * },
 * {
 * "id": "10011",
 * "name": "账户明细",
 * "explain": "账户明细",
 * "tag": "orders:account",
 * "url": ""
 * }
 * ]
 * },
 * {
 * "id": "9",
 * "name": "视频会议",
 * "explain": "视频会议",
 * "tag": "meeting",
 * "url": "",
 * "permissions": [
 * {
 * "id": "10012",
 * "name": "加入会议",
 * "explain": "加入会议",
 * "tag": "meeting:join",
 * "url": ""
 * },
 * {
 * "id": "10013",
 * "name": "召开会议",
 * "explain": "召开会议",
 * "tag": "meeting:call",
 * "url": ""
 * },
 * {
 * "id": "10014",
 * "name": "会议预约",
 * "explain": "会议预约",
 * "tag": "meeting:appointment",
 * "url": ""
 * },
 * {
 * "id": "10015",
 * "name": "会议记录",
 * "explain": "会议记录",
 * "tag": "meeting:record",
 * "url": ""
 * },
 * {
 * "id": "10016",
 * "name": "余额支付",
 * "explain": "余额支付",
 * "tag": "meeting:pay",
 * "url": ""
 * }
 * ]
 * },
 * {
 * "id": "12",
 * "name": "背景调查",
 * "explain": "背景调查",
 * "tag": "beidiao",
 * "url": "",
 * "permissions": [
 * {
 * "id": "10000",
 * "name": "开始调查",
 * "explain": "开始调查",
 * "tag": "investigate:start",
 * "url": ""
 * },
 * {
 * "id": "10002",
 * "name": "批量调查",
 * "explain": "批量调查",
 * "tag": "investigate:batch",
 * "url": ""
 * },
 * {
 * "id": "10003",
 * "name": "我的调查记录",
 * "explain": "我的调查记录",
 * "tag": "investigate:my",
 * "url": ""
 * },
 * {
 * "id": "10004",
 * "name": "全部调查记录",
 * "explain": "全部调查记录",
 * "tag": "investigate:all",
 * "url": ""
 * },
 * {
 * "id": "10005",
 * "name": "余额支付",
 * "explain": "余额支付",
 * "tag": "investigate:pay",
 * "url": ""
 * }
 * ]
 * },
 * {
 * "id": "13",
 * "name": "招聘管理",
 * "explain": "招聘管理",
 * "tag": "zhaopin",
 * "url": "",
 * "permissions": [
 * {
 * "id": "10006",
 * "name": "简历管理",
 * "explain": "简历管理",
 * "tag": "zhaopin:resume",
 * "url": ""
 * },
 * {
 * "id": "10007",
 * "name": "简历库",
 * "explain": "简历库",
 * "tag": "zhaopin:allresume",
 * "url": ""
 * },
 * {
 * "id": "10008",
 * "name": "账户管理",
 * "explain": "账户管理",
 * "tag": "zhaopin:account",
 * "url": ""
 * }
 * ]
 * }
 * ]
 */
