package com.vgtech.vancloud.ui.common.group;


import com.vgtech.common.api.TreeNodeDepartment;
import com.vgtech.common.api.TreeNodeId;
import com.vgtech.common.api.TreeNodeJob;
import com.vgtech.common.api.TreeNodeLabel;
import com.vgtech.common.api.TreeNodePid;

public class Bean {
    @TreeNodeId
    private String id;
    @TreeNodePid
    private String pId;
    @TreeNodeLabel
    private String label;
    @TreeNodeJob
    private String job;
    @TreeNodeDepartment
    public String department;
    private String version;

    public Bean() {
    }

    public Bean(String id, String pId, String label) {
        this.id = id;
        this.pId = pId;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
