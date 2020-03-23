package com.vgtech.common;

/**
 * Created by zhangshaofang on 2015/8/5.
 */
public interface URLAddr {

    String SCHEME = "https";
    String HOST_DICT = SCHEME + "://public.vgsaas.com/";
    String PORT = "80";
//    String IP = "app.vancloud.com"; //测试环境
//    String IP = "uat.vancloud.com"; //uat环境
    String IP = "app.vgsaas.com"; //正式环境

    String URL_ACCOUNTS_BALANCE = "v%1$d/accounts/balance";//获取帐户信息（余额）
    String URL_ACCOUNTS_SETTINGS_PASSWORD = "v%1$d/accounts/settings/password";//修改支付密码
    String URL_ACCOUNTS_VALIDATING_PASSWORD = "v%1$d/accounts/validating/password";//验证支付密码
    String URL_ACCOUNTS_GET_VALIDATECODE = "v%1$d/accounts/get_validatecode";//忘记密码，获取手机验证码

    String URL_ACCOUNTS_SETTINGS_PERSONAL_PASSWORD = "v%1$d/accounts/settings/personal_password";//修改个人钱包支付密码
    String URL_ACCOUNTS_VALIDATING_PERSONAL_PASSWORD = "v%1$d/accounts/validating/personal_password";//验证个人钱包支付密码

    String URL_AD = "v%1$d/ad";//获取首页广告图片
    String URL_USER_MODELS = "v%1$d/user/models";//获取租户模块

    String URL_CUSTOMERSERVICE = "v%1$d/common/customerservice";//获取客服信息
    String URL_SUPPORT_FEEDBACK = "v%1$d/common/feedback";//意见反馈
    String URL_COMMOM_VERSION = "v%1$d/common/version";//版本更新

    String URL_GROUP_INFO = "v%1$d/group/info";//获取部门人员信息
    String URL_GROUP_LIST = "v%1$d/group/list";//获取组织信息

    String URL_HELP_LIST = "v%1$d/help/list";
    String URL_HELP_INFO = "v%1$d/help/info";
    String URL_HELP_COLLECTION = "v%1$d/help/favorite";
    String URL_HELP_CREATE = "v%1$d/help/create";
    String URL_HELP_FORWARD = "v%1$d/help/forward";
    String URL_HELP_DELETE = "v%1$d/help/remove";

    String URL_ANNOUNCEMENT_LIST = "v%1$d/notify/list";//公告列表
    String URL_ANNOUNCEMENT_DETAIL = "v%1$d/notify/info";
    String URL_ANNOUNCEMENT_CREATE = "v%1$d/notify/create";


    String URL_ORDERS_CANCEL = "v%1$d/orders/cancel";//取消订单
    String URL_ORDERS_DETAIL = "v%1$d/orders/detail";//订单详情
    String URL_ORDERS_MANAGEMENTS = "v%1$d/orders/managements";//订单管理列表
    String URL_ORDERS_MY = "v%1$d/orders/my";//我的订单列表
    String URL_ORDERS_PAYMENT = "v%1$d/orders/payment";//支付订单
    String URL_ORDERS_TRANSACTIONS = "v%1$d/orders/transactions";//获取交易记录
    String URL_ORDERS_UNPAIDS = "v%1$d/orders/unpaids";//订单支付列表(待支付)
    String URL_ORDERS_ORDERTYPES = "v%1$d/orders/ordertypes";//获取订单类型列表
    String URL_ORDERS_TYPE = "v%1$d/orders/logtypes";//获取操作日志类型列表
    String URL_PI = "v%1$d/paicoin/neigou_login";//获取pi币二级页面
    /**
     * 流程撤销
     */
    String URL_PROCESS_CANCEL = "v%1$d/process/cancel";
    /**
     * 创建流程
     */
    String URL_PROCESS_CREATE = "v%1$d/process/create";
    /**
     * 流程详情
     */
    String URL_PROCESS_INFO = "v%1$d/process/info";
    /**
     * 流程列表(索引)
     */
    String URL_PROCESS_LIST = "v%1$d/process/list";
    /**
     * 流程处理
     */
    String URL_PROCESS_PROCESSING = "v%1$d/process/processing";
    /**
     * 流程招聘计划预览
     */
    String URL_PROCESS_JOB_PREVIEW = "v%1$d/process/job_preview";

    String URL_CRATE_SUBCOMPANY = "v%1$d/register/branchcompanys/create";//创建分公司
    String URL_GET_COMPANY_INFO = "v%1$d/register/companydetails";     //获取企业详细信息
    String URL_SET_COPANY_LOGO = "v%1$d/register/settings/companylogos";//修改企业LOGO
    String URL_SET_COMPANY_INFO = "v%1$d/register/settings/companymessages"; //修改企业信息
    String URL_SETNEWSTAFFSDEPARTMENT = "v%1$d/register/settings_batch/staffs_departments";//批量设置新员工所属部门
    String URL_REGISTER_SET_NEW_STAFFS_POSITION = "v%1$d/register/settings_batch/staffs_positions"; //批量设置新员工职位
    String URL_SETTINGS_ROLES = "v%1$d/register/settings/roles";//设置员工用户角色
    String URL_USER_MESSAGE = "v%1$d/user/message";//获取员工信息
    String URL_MESSAGE_EDIT = "v%1$d/user/message_edit";//个人信息编辑

    String URL_SCHEDULE_LIST = "v%1$d/schedule/list";
    String URL_SCHEDULE_DETAIL = "v%1$d/schedule/detail";
    String URL_SCHEDULE_CREATE = "v%1$d/schedule/create";
    String URL_SCHEDULE_CONDUCT = "v%1$d/schedule/processing";
    String URL_SCHEDULE_CANCEL = "v%1$d/schedule/cancel";
    String URL_SCHEDULE_ISEXIST = "v%1$d/schedule/isexist";
    String URL_SCHEDULE_CLOCKINPARAM = "v%1$d/schedule/clockinparam";

    /**
     * 高级搜索-日程列表
     */
    String URL_SEARCH_CALENDAR = "v%1$d/search/calendar";
    /**
     * 高级搜索-帮帮列表
     */
    String URL_SEARCH_HELP = "v%1$d/search/help";
    /**
     * 高级搜索-任务列表
     */
    String URL_SEARCH_TASK = "v%1$d/search/task";
    /**
     * 高级搜索-分享列表
     */
    String URL_SEARCH_TOPIC = "v%1$d/search/topic";
    /**
     * 高级搜索-流程列表
     */
    String URL_SEARCH_WORKFLOW = "v%1$d/search/workflow";
    /**
     * 高级搜索-工作汇报列表
     */
    String URL_SEARCH_WORKREPORT = "v%1$d/search/workreport";

    String URL_SUPPORT_DOPRAISE = "v%1$d/support/dig";
    String URL_SUPPORT_PRAISELIST = "v%1$d/support/praiselist";
    String URL_SUPPORT_ADDCOMMENT = "v%1$d/support/addComment";
    String URL_SUPPORT_COMMENTLIST = "v%1$d/support/commentlist";

    String URL_TASK_CREATE = "v%1$d/task/create";
    String URL_TASK_CONDUCT = "v%1$d/task/processing";
    String URL_TASK_LIST = "v%1$d/task/list";
    String URL_TASK_INFO = "v%1$d/task/info";
    String URL_TASK_BACKOUT = "v%1$d/task/cancel";

    String URL_SHARED_DETAIL = "v%1$d/topic/info";
    String URL_SHARED_LIST = "v%1$d/topic/list";
    String URL_SHARED_COLLECTION = "v%1$d/topic/favorite";
    String URL_SHARED_CREATE = "v%1$d/topic/create";
    String URL_SHARED_FORWARD = "v%1$d/topic/forward";
    String URL_SHARED_REMOVE = "v%1$d/topic/remove";

    String URL_IMAGE = "upload/image";
    String URL_AUDIO = "upload/audio";
    String URL_ATTACHMENT = "upload/attachment";

    String URL_REGISTER_GETCOMPANYDEPARTINFO = "v%1$d/user/departments";//获取企业部门信息
    String URL_GETCOMPANYDEPARTINFO = "v%1$d/user/departments";//获取企业部门信息
    String URL_USER_PERMISSIONS = "v%1$d/user/permissions";//获取企业部门信息
    String URL_DELETEDEPART = "v%1$d/user/departments/destroy";//删除组织机构部门
    String URL_INDUSDEPARTRINFO = "v%1$d/user/departments_template";//获取行业及行业部门信息

    String URL_REGISTER_GET_COMPANY_POSITION_INFO = "v%1$d/user/positions";//获取企业职位信息
    String URL_GET_COMPANY_POSITION = "v%1$d/user/positions";//获取企业职位信息
    String URL_DELETE_POSITION = "v%1$d/user/positions/destroy";//删除职位
    String URL_GET_INDUS_POSITION = "v%1$d/user/positions_template";//获取行业职位信息
    String URL_GET_LEAVE_REASON = "v%1$d/user/profiles/leave_reasons";//获取离职原因
    String URL_DEALLEAVER = "v%1$d/user/profiles/settings/leavers"; //员工离职处理
    String URL_REGISTER_GET_ROLE_INFO = "v%1$d/user/roles";//获取角色信息
    String URL_UPDATEDEPARTNAME = "v%1$d/user/settings/departments";//增加修改组织机构 [type项 0:修改部门名称 1：增加部门 2：修改部门关系]
    String URL_UPDATECOMPANYDEPART = "v%1$d/user/settings/departments_template";//设置企业部门(完成企业部门保存)
    String URL_UPDATEPHOTO = "v%1$d/user/settings/photos";//头像修改
    String URL_UPDATE_POSITION = "v%1$d/user/settings/positions";//增加修改职位[position_id传:修改,不传:增加]
    String URL_SAVE_COMPANY__POSITION = "v%1$d/user/settings/positions_template";//设置企业职位
    String URL_USER_UPDATESIGN = "v%1$d/user/settings/signs";//签名修改

    String URL_VCHAT_PNS = "v%1$d/vchat/pns";
    String URL_VCHAT_GROUPMEMBERS = "v%1$d/vchat/xmpp/groupmembers";
    String URL_VCHAT_MUCOWNER = "v%1$d/vchat/xmpp/mucowner";

    String URL_WORKGROUP_LIST = "v%1$d/workgroup/list";
    String URL_WORKGROUP_CREATE = "v%1$d/workgroup/create";
    String URL_WORKGROUP_DELETE = "v%1$d/workgroup/delete";
    String URL_WORKGROUP_INSERT = "v%1$d/workgroup/insert";
    String URL_WORKGROUP_UPDATE = "v%1$d/workgroup/update";
    String URL_WORKGROUP_DELETEUSER = "v%1$d/workgroup/deleteuser";//工作组删除


    String URL_ASSIST_CALENDAR = "assist/calendar.html";
    String URL_ASSIST_CHAT = "assist/chat.html";
    String URL_ASSIST_NOTICE = "assist/notice.html";
    String URL_ASSIST_TASK = "assist/task.html";
    String URL_ASSIST_TOPIC = "assist/topic.html";
    String URL_ASSIST_WORKFLOW = "assist/workflow.html";
    String URL_ASSIST_WORKREPORT = "assist/workreport.html";
    String URL_ASSIST_RECRUIT = "assist/recruit.html";
    String URL_ASSIST_FINANCE = "assist/finance.html";
    String URL_ASSIST_BEIDIAO = "assist/beidiao.html";
    String URL_ASSIST_MEETING = "assist/meeting.html";

    String URL_WORKREPORT_LIST = "v%1$d/workreport/list";
    String URL_WORKREPORT_DETAIL = "v%1$d/workreport/detail";
    String URL_WORKREPORT_TEMPLATE = "v%1$d/workreport/template";
    String URL_WORKREPORT_CREATE = "v%1$d/workreport/create";
    String URL_WORKREPORT_STATISTICS = "v%1$d/workreport/statistics";
    String URL_WORKREPORT_CONDUCT = "v%1$d/workreport/processing";
    String URL_WORKREPORT_BACKOUT = "v%1$d/workreport/cancel";


    String SHARE_URL = "v%1$d/link/";
    String URL_APPLIST = "http://download.vgsaas.com/applist.html";

    String URL_PROPERTY_TEMPLATES = "v%1$d/property/templates";// 获取模板
    String URL_REFEREE_RECORDS = "v%1$d/referee/records";// 推荐记录

    /**
     * ------------2.0新改版招聘模块接口------------
     */
    String URL_ENTERPRISE_JOB_ADD_JOB_WELFARES = "v%1$d/enterprise_job/add_job_welfare";//添加福利待遇
    String URL_ENTERPRISE_JOB_CREATE_JOB = "v%1$d/enterprise_job/create_job";//招聘模块-创建职位
    String URL_ENTERPRISE_JOB_UPDATE_JOB = "v%1$d/enterprise_job/update_job";//招聘模块-修改职位
    String URL_ENTERPRISE_JOB_FINISH_JOB = "v%1$d/enterprise_job/finish_job";//完成招聘
    String URL_SEARCH_RESUME = "v%1$d/search/resume";//简历搜索
    String URL_ENTERPRISE_RESUME_LIST = "v%1$d/enterprise_resume/list";//简历列表
    String URL_ENTERPRISE_RESUME_DELETE = "v%1$d/enterprise_resume/delete";//删除简历
    String URL_ENTERPRISE_RESUME_BUY = "v%1$d/enterprise_resume/buy";//简历购买
    String URL_ENTERPRISE_RESUME_DETAIL_V2 = "v%1$d/enterprise_resume/detail_v2";//万客收件箱-简历详情V2
    String URL_PERSONAL_RESUME_DETAIL_V2 = "v%1$d/personal_resume/resume/detail_v2";//万客搜索收件箱-简历详情V2
    String URL_ENTERPRISE_RESUME_PAID_RESUME_DETAIL_V2 = "v%1$d/enterprise_resume/paid_resume_detail_v2";//万客查看购买的简历详情V2

    /**
     * -----------2.1.0企业版招聘模块新增接口--------------------------------
     */
    String URL_VANCLOUD_JOB_CHANNEL_STATUS = "v%1$d/vancloud_job/channel_status";//招聘渠道及状态
    String URL_VANCLOUD_JOB_DELETE_JOBS = "v%1$d/vancloud_job/delete_jobs";//删除职位
    String URL_VANCLOUD_JOB_FRESH_JOBS = "v%1$d/vancloud_job/fresh_jobs";//刷新职位信息
    String URL_VANCLOUD_JOB_JOB_TEMPLATES = "v%1$d/vancloud_job/job_templates";//职位模板列表
    String URL_VANCLOUD_JOB_JOBS = "v%1$d/vancloud_job/jobs";//职位列表（招聘信息）
    String URL_VANCLOUD_JOB_PAUSE_JOBS = "v%1$d/vancloud_job/pause_jobs";//暂停职位
    String URL_VANCLOUD_JOB_APPLYING_PAUSE_JOBS = "v%1$d/vancloud_job/applying_pause_jobs";//51申请中-暂停职位
    String URL_VANCLOUD_JOB_PUBLISH_JOBS = "v%1$d/vancloud_job/publish_jobs";//发布职位
    String URL_VANCLOUD_JOB_NO_PUBLISH_JOBS = "v%1$d/vancloud_job/no_publish_jobs";//51、智联未发布-发布职位
    String URL_VANCLOUD_JOB_REPUBLISH_JOBS = "v%1$d/vancloud_job/republish_jobs";//再发布职位
    String URL_VANCLOUD_JOB_RENEW_JOBS = "v%1$d/vancloud_job/renew_jobs";//恢复
    String URL_VANCLOUD_JOB_RESUME_CHANNEL_STATUS = "v%1$d/vancloud_job/resume_channel_status";//简历管理渠道及状态
    String URL_VANCLOUD_JOB_RESUMES = "v%1$d/vancloud_job/resumes";//简历管理列表
    String URL_TENANT_PHONE_LOGIN = "v%1$d/property/templates";//获取扫码链接地址


    //招聘模块接口
    String URL_PLUGIN_RECRUIT_FAVORITES_CREATE = "v%1$d/plugin/recruit/favorites/create";//简历收藏

    String URL_RECRUIT_PURCHASE_APPLICATION_CREATE = "v%1$d/plugin/recruit/purchase_application/create";//简历购买申请
    String URL_RECRUIT_WORKFLOW_PROCESSING = "v%1$d/plugin/recruit/workflow/processing"; // 招聘流程审批
    String URL_RECRUIT_TASK_PROCESSING = "v%1$d/plugin/recruit/task/processing"; // 招聘任务处理

    String URL_POSITIONS_PLACES = "v%1$d/plugin/recruit/positions/places";//职位发布地点

    String URL_SYSTEM_NOTIFY = "v%1$d/common/notifications";//系统通知

    //    String HOST_BEIDIAO = "http://appbeidiao.vancloud.com/";
    String URL_BG_INVEST_INDEX = "app_html/bjdx/index.html";
    String URL_BG_INVEST_RESULT = "app_html/bjdx/result.html";
    String URL_BG_INVEST_RECORD_LIST = "app_html/bjdx/record_list.html";
    String URL_BG_INVEST_MYRECORD_LIST = "app_html/bjdx/myrecord_list.html";
    String URL_BG_SEARCH = "app_html/bjdx/search.html";

    //zsf以下接口无需用户验证
    String URL_LOGIN = "v%1$d/user/login";//登录
    String URL_REGISTER_TENANT = "v%1$d/user/register";//注册企业
    String URL_REGISTER_UPDATEPWD = "v%1$d/user/settings/passwords";//设置登陆密码
    String URL_USER_UPDATEPASSWORD = "v%1$d/user/settings/passwords";
    String URL_CODE_CHECK_VALIDATECODE = "v%1$d/code/check_validatecode";//校验手机验证码
    String URL_CODE_GET_VALIDATECODE = "v%1$d/code/get_validatecode";//获取验证码 （不检验手机号是否在系统中存在）
    String URL_CODE_GET_ACCESS_VALIDATECODE = "v%1$d/code/get_access_validatecode";//获取验证码（检验手机号是否在系统中存在）

    String URL_INVESTIGATES = "v%1$d/app/investigates/detail_app";//背景调查详情
    String URL_INVESTIGATES_RECORDS = "v%1$d/app/investigates/list";//背景调查-调查记录（全部）
    String URL_PERSONAL_LIST = "v%1$d/app/investigates/personal_list";//背景调查-调查记录(我的)
    String URL_INVESTIGATES_CHECK = "v%1$d/app/investigates/check";//背景调查
    String URL_INVESTIGATES_TO_PAYMENT = "v%1$d/app/investigates/to_payment";//背景调查
    String URL_WXPAY_PREPAY = "v%1$d/wxpay/prepay";//获取微信支付信息
    String URL_ALIPAY_PREPAY = "v%1$d/alipay/prepay";//获取支付宝支付信息
    String URL_WXPAY_APP_QUERY_TRADE = "v%1$d/wxpay/app_query_trade";//获取微信支付状态
    String URL_ALIPAY_APP_QUERY_TRADE = "v%1$d/alipay/app_query_trade";//获取支付宝支付状态

    String URL_ENTERPRISE_RESUME_RESUME_PAYINFO = "v%1$d/enterprise_resume/resume_payinfo";//生成购买简历支付信息
    String URL_ENTERPRISE_RESUME_PAID_RESUME_DETAIL = "v%1$d/enterprise_resume/paid_resume_detail";//查看购买的简历详情

    /**
     * ----------万客基础数据-------------------------------------------------------------------
     */
    String URL_DICT_VANCLOUD_CITYS = HOST_DICT + "dict/vancloud_citys";//万客-发布城市
    String URL_DICT_VANCLOUD_DEGREE = HOST_DICT + "dict/vancloud_degree";//万客-学历
    String URL_DICT_VANCLOUD_FUNCTIONS = HOST_DICT + "dict/vancloud_functions";//万客-职能类别
    String URL_DICT_VANCLOUD_LANGUAGE = HOST_DICT + "dict/vancloud_language";//万客-语言
    String URL_DICT_VANCLOUD_LANGUAGE_LEVEL = HOST_DICT + "dict/vancloud_language_level";//万客-语言级别
    String URL_DICT_VANCLOUD_MAJORS = HOST_DICT + "dict/vancloud_majors";//万客-专业
    String URL_DICT_VANCLOUD_SALARY = HOST_DICT + "dict/vancloud_salary";//万客-职位月薪
    String URL_DICT_VANCLOUD_TERM = HOST_DICT + "dict/vancloud_term";//万客-工作性质
    String URL_DICT_VANCLOUD_WELFARE = HOST_DICT + "dict/vancloud_welfare";//万客-福利待遇
    String URL_DICT_VANCLOUD_WORK_YEAR = HOST_DICT + "dict/vancloud_work_year";//万客-工作年限

    /**
     * ----------万客三方帐号管理-------------------------------------------------------------------
     */
    String URL_VANCLOUD_JOB_BIND_ACCOUNT = "v%1$d/vancloud_job/bind_account";//绑定账户
    String URL_VANCLOUD_JOB_ACCOUNTS = "v%1$d/vancloud_job/accounts";//绑定账户列表

    String URL_RESUME_DETAIL_HTML = "v%1$d/enterprise_resume/detail_html";//企业版第三方简历详情-html
    String URL_AREA = HOST_DICT + "dict/area";//职位地点
    String URL_PHONE_LOGIN = "v%1$d/personal/phone_login";//二维码登陆
    String URL_RESUME_DEGREE = HOST_DICT + "dict/resume_degree";//简历-学历
    String URL_INDUSTRY = HOST_DICT + "dict/industry";//职位行业
    String URL_RESUME_SITUATION = HOST_DICT + "dict/resume_situation";//简历-求职状态
    String URL_FUNCTIONS = HOST_DICT + "dict/functions";//职位职能
    String URL_RESUME_COSIZE = HOST_DICT + "dict/resume_cosize";//简历-公司规模
    String URL_RESUME_COMPANYTYPE = HOST_DICT + "dict/resume_companytype";//简历-公司性质

    /**
     * ----------v3.0接口-------------------------------------------------------------------
     */

    String URL_TODO_LIST = "v%1$d/todo/index/list";//待办列表
    String URL_INDEX_LASTDATA = "v%1$d/index/lastdata";//首页最后一条
    String URL_INDEX_SEARCH = "v%1$d/index/search";//搜索公共，通知，代办

    String URL_TODO_MARK = "v%1$d/todo/index/mark";//待办已读标记
    String URL_CHATGROUP_LIST = "v%1$d/vchat/xmpp/mucgroups";//聊天群组
    String URL_STAFFAPPLY_LIST = "v%1$d/user/invite_list";//员工申请列表
    String URL_INVITE_OPTION = "v%1$d/user/invite_option";//员工申请列表-删除-拒绝
    String URL_AGREED_OPTIO = "v%1$d/user/agreed_option";//员工申请列表-同意
    String URL_ADD_STAFF = "v%1$d/user/add"; //添加用户
    String URL_PROCESS_LIST_VANCLOUD = "v%1$d/process/list/vancloud";//审批列表（万客）
    String URL_PROCESS_HASTEN = "v%1$d/process/hasten";//催办审批（万客）

    String URL_SEARCH_WORKFLOW_VANCLOUD = "v%1$d/search/workflow/vancloud";//高级搜索-流程列表(万客)


    String URL_PROCESS_HASTEN_VANTOP = "v%1$d/process/hasten/vantop";//催办审批（vantop）

    String URL_INVITE_TEMPLATE = "v%1$d/user/invite_template";// 获取邀请模板
    String URL_USER_HEAD_PHOTO = "v%1$d/user/head_photo";// 根据员工编号获取缩略图片
    String URL_CC_LIST = "v%1$d/cc/list";// 获取抄送人

    String URL_TODO_DELETE = "v%1$d/todo/index/delete";//待办删除
    String URL_TODO_INDEX_VANTOP_DELETE = "v%1$d/todo/index/vantop/delete";// 待办删除(vantop 审批)

    String URL_VANCLOUD_JOB_UNBIND_ACCOUNT = "v%1$d/vancloud_job/unbind_account";//解除绑定账户


    String URL_PARAM_OID = "oid";

    String URL_USER_CHANGE_PHONENO = "v%1$d/user/change_phoneno";//更换手机号
    String URL_NEIGOU_NEIGOU_LOGIN = "v%1$d/neigou/neigou_login";//内购
    String URL_PSYCHOLOGY_XINLITIJIAN_LOGIN = "v%1$d/psychology/link";//心理体检
    String URL_MYNOTICE_LIST = "v%1$d/mynotice/list";//通知列表
    String URL_MYNOTICE_CONFIRM = "v%1$d/mynotice/confirm";//通知确认
    /**
     * static--------------------分割线---------------
     */
    String URL_EWM = "appstatic/images/ewm.png";
    String URL_GUIDE = "appstatic/v%1$d/guide/%2$s.html";
    String URL_NOTIFICATIONS = "appstatic/notifications/android/%1$s.html";//功能介绍
    String URL_NOTIFICATIONS_PRIVACY = "appstatic/notifications/privacy/%1$s.html";//隐私条款
    String URL_PRIVACY_POLICY = "/appstatic/privacy_policy.html";//隐私政策
    /**
     * static--------------------分割线---------------
     */
    String URL_BEHAVIOR_STATISTIC = "v%1$d/oplog/add";//行为统计
    /**
     * static--------------------分割线---------------
     */
    String URL_SPLASH_AD = "v%1$d/advertisement/advertisement_shanping";//闪屏页广告
    String URL_HORIZONTAL_AD = "v%1$d/advertisement/advertisement_hengping";//主页横向广告
    /**
     * static--------------------论坛---------------
     */
    String TOPIC_LIST = "v%1$d/forum/topic/list";
    String POST = "v%1$d/forum/topic/create";
    String REPLY_LIST = "v%1$d/forum/reply/list";
    String REPLY_CREATE = "v%1$d/forum/reply/create";
    String LAST_POST = "v%1$d/forum/topic/lastsummary";
    String POST_ADD_VISITOR_COUNT = "v%1$d/forum/topic/addvisitorcount";
    //无权限派商城
    String POST_PAI_UNPERMISSION = "appstatic/paicoin/introduction.html";
    /**
     * 个人π币
     */
    String SIGNIN_LOGIN = "v%1$d/individual/login";//登录
    String SIGNIN_REGISTER_REGISTER = "v%1$d/individual/register";//注册校验手机号
    String SIGNIN_REGISTER_PWD = "v%1$d/individual/isexist";//忘记密码校验手机号
    String SIGNIN_RESETPWD = "v%1$d/individual/resetpwd";//重置密码
    String SIGNIN_GETVALIDATECODE = "v%1$d/individual/get_validatecode";//手机号验证码
    //个人信息保护指引确认
    String URL_USERAUTH_CONFIRM = "v%1$d/user/userauth_confirm";
}
