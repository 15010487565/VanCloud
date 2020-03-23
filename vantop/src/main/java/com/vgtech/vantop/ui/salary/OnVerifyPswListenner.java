package com.vgtech.vantop.ui.salary;

/**
 * 该接口为密码验证成功后的操作，初始化情况下密码验证完成后跳转为默认页
 * 以下几种情况需要为SalaryActivity设置该接口
 * 1.当默认进入工资为不需要密码验证时，当选择项目查询时，后台修改为需要密码
 * 验证，则验证完密码继续之前的搜索操作
 * 2.当选择年份查询时同上
 * Created by shilec on 2016/10/19.
 */
public interface OnVerifyPswListenner {
    /***
     * 参数arg[0] 为type
     * arg[0] = 0 日期查询
     * arg[0] = 1 项目查询
     * arg[0] = 2 年份查询
     * 参数arg[1-n] 为参数
     * 日期查询 showDateSearchFragment(String date, String psw)
     *         arg[1] = date;
     *         arg[2] = psw;
     * 项目查询 showYearsSearchFragment(String year, String psw)
     *         arg[1] = year;
     *         arg[2] = psw;
     * 年份查询 showItemSearchFragment(String year, String item, String psw)
     *         arg[1] = year;
     *         arg[2] = item;
     *         arg[3] = psw;
     * @return
     */
    String[] onVerifyFinished();
}
