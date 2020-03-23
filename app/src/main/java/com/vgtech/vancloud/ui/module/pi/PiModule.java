package com.vgtech.vancloud.ui.module.pi;

import com.vgtech.common.api.AbsApiData;

import java.io.Serializable;

/**
 * Data:  2019/3/15
 * Auther: xcd
 * Description:
 */

public class PiModule extends AbsApiData {

    /**
     * login_surl : http://www.neigou.com
     * login_token_expire_at : 1552641181
     * external_company_id :
     * login_furl : http://neigou.com/?err=NG_ERRCODE
     * login_token : b3BlbmFwaS1lYTRlYjRhNjIxNzQ1ODljOThkMjljNTE1NmE1YWY1Zg==
     * external_user_id : 608694301886517249_1039578377054326784_v1
     * login_url : https://openapi.neigou.com/ChannelInterop/v1/Standard/Web/gateway?surl=http://www.neigou.com&furl=http://neigou.com/?err=NG_ERRCODE&login_token=b3BlbmFwaS1lYTRlYjRhNjIxNzQ1ODljOThkMjljNTE1NmE1YWY1Zg==&force=true
     */

    private String login_surl;
    private int login_token_expire_at;
    private String external_company_id;
    private String login_furl;
    private String login_token;
    private String external_user_id;
    private String login_url;

    public String getLogin_surl() {
        return login_surl;
    }

    public void setLogin_surl(String login_surl) {
        this.login_surl = login_surl;
    }

    public int getLogin_token_expire_at() {
        return login_token_expire_at;
    }

    public void setLogin_token_expire_at(int login_token_expire_at) {
        this.login_token_expire_at = login_token_expire_at;
    }

    public String getExternal_company_id() {
        return external_company_id;
    }

    public void setExternal_company_id(String external_company_id) {
        this.external_company_id = external_company_id;
    }

    public String getLogin_furl() {
        return login_furl;
    }

    public void setLogin_furl(String login_furl) {
        this.login_furl = login_furl;
    }

    public String getLogin_token() {
        return login_token;
    }

    public void setLogin_token(String login_token) {
        this.login_token = login_token;
    }

    public String getExternal_user_id() {
        return external_user_id;
    }

    public void setExternal_user_id(String external_user_id) {
        this.external_user_id = external_user_id;
    }

    public String getLogin_url() {
        return login_url;
    }

    public void setLogin_url(String login_url) {
        this.login_url = login_url;
    }
}
