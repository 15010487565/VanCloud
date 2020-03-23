package com.vgtech.common.api;

import java.io.Serializable;
import java.util.List;

/**
 * @author xuanqiang
 */
public class UserAccount extends AbsApiData implements Serializable {
    private static final long serialVersionUID = -3363651327267153786L;
    public String tenant_id;
    public String tenant_name;
    public String tenant_name_en;

    public String user_name;
    public String area_code;
    public String mobile;
    public String sign;
    public int count;
    public String set_type;
    public String photo;
    public String step;

    public String xmpp_host;
    public int xmpp_port;
    public String service_host;

    public String token;
    public String user_id;

    public String logo;
  //  public List<Role> roles;
    public List<Auth> auths;//权限

    public String getUrl(String uri) {
        return service_host + uri;
    }

    public String nickname() {
        return user_name;
    }

    public String getUid() {
        return String.valueOf(user_id);
    }
}
