package com.wxm.entity;

import com.wxm.model.OAPrivilege;

import java.util.List;

public class GroupInfo {
    private Integer groupId;
    private List<OAPrivilege> oaPrivileges;
    private Integer status;
    private String groupName;
    private String description;
    private String flow;
    private String task;
    private String attachment;

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public String getFlow() {
        return flow;
    }

    public String getTask() {
        return task;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public List<OAPrivilege> getOaPrivileges() {
        return oaPrivileges;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setOaPrivileges(List<OAPrivilege> oaPrivileges) {
        this.oaPrivileges = oaPrivileges;
    }
}
