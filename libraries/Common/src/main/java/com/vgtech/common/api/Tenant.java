package com.vgtech.common.api;

/**
 * Created by Jackson on 2015/10/20.
 * Version : 1
 * Details :
 */
public class Tenant extends AbsApiData {

    public String tenant_id;
    public String tenant_name;

    public Tenant() {
    }

    public Tenant(String tenant_id, String tenant_name) {
        this.tenant_id = tenant_id;
        this.tenant_name = tenant_name;
    }
}
