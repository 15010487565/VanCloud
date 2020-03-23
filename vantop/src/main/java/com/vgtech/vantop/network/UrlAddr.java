package com.vgtech.vantop.network;

/**
 * Created by vic on 2016/7/13.
 */
public interface UrlAddr {
    String URL_LOGIN = "login";
    String URL_USERS_SHOW = "users/show";
    //请求全部为POST
    /**
     * 参数：date password
     * method:GET
     * 工资查询
     */
    String URL_SALARY_QUERY = "salaries";
    /**
     * 参数：password
     * method:POST
     * 密码验证
     */
    String URL_PSW_VERIFY = "users/pwd-verify";
    /**
     * 参数：
     * method:GET
     * 获取工资记录存在的时间
     */
    String URL_SALARY_DATES = "salaries/dates";
    /***
     * 参数:
     * method:GET
     * 获取工资的项目分类
     */
    String URL_SALARY_ITEMS = "salaries/items";
    /***
     * 参数：
     * method:POST
     * 工资中按年查询的年份列表
     */
    String URL_SALARY_YEARS = "salaries/years";
    /***
     * 参数：year password
     * method:GET
     * 工资中的年工资查询
     */
    String URL_SALARY_YEAR_PRICES = "salaries/yearReport";

    /****
     * 参数：y password
     * method:GET
     * 工资中按项目查询
     */
    String URL_SALARY_YEAR_ITEM_PRICE = "salaries/items-year-prices";


    String URL_TASKLIST = "taskList";//待办列表

    /**
     * 参数：
     * method：GET
     * 打卡模块界面初始化所需数据
     */
    String URL_PUNCHCARD_INITDATA = "attendances/times";

    /***
     * 参数：GET:
     * remark 随便说点什么
     * POST:
     * cardNo 卡号
     * termNo
     * longitude
     * latitude
     * address
     */
    String URL_PUNCHCARD_POSTDATA = "saveAttendance";

    /***
     * 获取打卡记录
     * method: POST
     */
    String URL_PUNCHCARD_LOADHISTORY = "attendanceList";

    /***
     * 获取签卡记录
     * method: POST
     */
    String URL_SIGNEDCARD_LOADHISTORY = "signCardList";

    /***
     * 签卡详情
     * method：POST
     * 参数： taskId
     */
    String URL_SIGNEDCARD_DETAILS = "signCardDetail";

    /**
     * 新建签卡
     * method:GET
     */
    String URL_SIGNEDCARD_NEW = "signCard/new";

    /***
     * 提交签卡
     * method:POST
     */
    String URL_SIGNEDCARD_SUBMIT = "signCardSubmit";

    /**
     * 删除签卡
     * method:POST
     */
    String URL_SIGNEDCARD_DESTROY = "signcardApply/destroy";

    /**
     * 考勤列表
     * method:POST
     * 参数:date nextId
     */
    String URL_CLOCKIN_LIST = "clockIn/list";
    /**
     * 排班列表
     * method:POST
     * 参数:date nextId
     */
    String URL_ATTDETAIL_SEARCH = "attdetail/search";

    /***
     * 考勤详细信息
     * method: POST
     * 参数: staffNo date shiftCode
     */
    String URL_CLOCKIN_DETAIL = "clockIn/detail";

    /***
     * 考勤申诉原因条目
     * method: GET
     * 参数:
     */
    String URL_CLOCKIN_APPEAL_REASON = "clockIn/appealReason";

    /**
     * 考勤申诉
     * method: POST
     * staffNo date shiftCode
     * isFixed explain fixedExplainKey
     */
    String URL_CLOCKIN_APPEAL = "clockIn/appeal";

    /***
     * 调查问卷
     * method: POST
     * nextId
     */
    String URL_QUESTIONNAIRE = "questionnaire/list";

    /***
     * VANTOP 个人信息
     * method: GET
     * staff_no
     */
    String URL_VANTOP_USERINFO = "users/show";

    /***
     * 修改个人信息
     * GET : 获取可修改的个人信息
     * POST : 提交修改的个人信息
     */
    String URL_VANTOP_USEREDIT = "users-edit";
    String URL_VANTOP_USEREDIT_SHOW = "/users-editshow";
    String URL_VANTOP_USERAVATAR = "users/avatar";


    /*************
     * 审批模块接口
     ************************/
    String URL_OVERTIMEAPPLY_NEW = "overtimeApply/new";//加班相关数据
    String TIME_SLICE = "timeslice";//休息时长
    String URL_OVERTIMEAPPLY_SUBMIT = "overtimeApply/submit";//申请加班
    String URL_OVERTIMEAPPLY_DETAIL = "overtimeApply/detail";//申请详情

    String URL_OVERTIMEAPPLY_BACKOUT = "overtimeApply/backout";//加班撤销
    String URL_OVERTIMEAPPLY_DESTROY = "overtimeApply/destroy";//加班删除

    String URL_OVERTIMEAPPROVAL_DETAIL = "overtimeApproval/detail";//加班审批详情
    String URL_OVERTIMEAPPROVAL_OPERATION = "overtimeApproval/operation";//加班审批


    String URL_VACATIONS = "vacations";//我的假期（get）
    String URL_VACATIONS_BALANCES = "vacations/balances";//假期结余详情（get）
    String URL_VACATIONS_BY_CODE_ADJUSTS = "vacations-by-code/adjusts";//假期调整数（get）
    String URL_VACATIONS_BY_CODE_USES = "vacations-by-code/uses";//假期使用数（get）
    String URL_VACATIONS_BY_CODE_APPLIES = "vacations-by-code/applies";//假期审批列表（get）
    String URL_VACATIONS_APPLY = "vacations/apply";//假期审批详情（get）
    String URL_VACATIONS_ROLLBACK = "vacations/rollback";//假期申请撤销
    String URL_VACATIONS_DESTROY = "vacations/destroy";//假期申请删除
    String URL_VACATIONS_CODES = "vacations/codes";//获取休假类型（get）
    String URL_APPLIES_NEW = "applies/new";//.休假申请控制参数（get）
    String URL_APPLIES_DURATION = "applies/duration";//.休假申请时长获取（get）

    String URL_SIGNEDCARD_APPROVAL_DETAILS = "approvals/signcard/details";//签卡审批详情
    String URL_SIGNEDCARD_APPROVAL_OPERATION = "signcardApprovals/operation";//签卡审批同意拒绝
    String URL_APPLIES = "applies";//假期申请
    String URL_USERS_SEARCH = "users/search";//搜素用户（get）

    String URL_APPROVECOMMON = "approveCommon";//我审批的
    String URL_APPLYCOMMON = "applyCommon";//我发起的
    String URL_APPROVECOMMONNUM = "approveCommonNum";//待审批数量
    String URL_APPROVALS_SHOW = "approvals/show";//假期审批详情
    String URL_OVERTIME_APPROVALS_BATCH_SHOW = "overtimeApproval/operationbatch";//加班批量审批
    String URL_APPROVALS_BATCH_SHOW = "approvalsbatch";//休假批量审批
    String URL_APPROVALS_BATCH_OPERATION = "signcardApprovals/operationbatch";//签卡批量审批
    String URL_APPROVALS = "approvals";//假期审批详情
    String URL_MYNOTICE_DETAILS = "mynotice/details";//通知详情
    String URL_MYNOTICE_CONFIRM = "mynotice/confirm";//通知确认


    String URL_ORGS = "orgs";
    String URL_ORGS_STAFFS = "orgs/staffs";
    String URL_ORGS_SEARCH = "orgs/search";
    String URL_ORGS_STAFFLIST = "orgs/staffList";
    String URL_USER_CHANGELANGUAGE = "user/changeLanguage";

    String URL_USERID_TO_STAFFNO = "v%1$d/user/staffno_uid_conver";//登录"

    String URL_PC_VER_NUM = "pcVersionNumber";//获取服务器版本号
    String URL_IS_SHOW_PWD = "is_showpwdview";//是否弹窗
    String URL_WORK_LOG_COST_CENTER = "work_log/cost_center";//成本中心
    String URL_ADD_WORK_LOG = "work_log/add";//添加日志
    String URL_UPDATE_WORK_LOG = "work_log/update";//添加日志
    String URL_DEL_WORK_LOG = "work_log/delete";//删除日志
    String URL_MINE_WORK_LOG_LIST = "work_log/list";//获取日志列表
    String URL_SUBMIT_MINE_WORK_LOG = "work_log/submit";//提交工作日志
    String URL_REVOKE_SUB_WORK_LOG = "/work_log/backout";//撤销下属工作日志
    String URL_SUB_SUM_WORK_LOG_LIST = "work_log/sub_sum_list";//获去下属工作日志总数
    String URL_SUB_WORK_LOG_LIST = "work_log/sub_list";//获取下属日志列表
    String URL_WORK_LOG_IMAGE = "work_log/image";//获取日志的图片
    String URL_MINE_SEARCH_WORK_LOG_LIST = "work_log/search";//搜索当前日志列表
    String URL_SUB_SEARCH_WORK_LOG_LIST = "work_log/sub_search";//搜索下属日志列表
    String URL_WORK_LOG_EXIST_DATAS = "work_log/get_dates";//获取员工可用日期
    String URL_WORK_LOG_SUB_EXIST_DATAS = "work_log/get_sub_dates";//获取下属员工可用日期
}
