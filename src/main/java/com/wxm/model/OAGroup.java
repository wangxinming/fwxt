package com.wxm.model;

import java.util.Date;

public class OAGroup {
    private Integer groupId;

    private String groupName;

    private Integer userId;

    private String privilegeids;

    private String describe;
    private Integer status;

    private Date createTime;

    public OAGroup(){

    }
    public OAGroup(String groupName,Integer userId,String privilegeids,String describe,int status){
        this.createTime = new Date();
        this.groupName = groupName;
        this.userId = userId;
        this.privilegeids = privilegeids;
        this.describe = describe;
        this.status = status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName == null ? null : groupName.trim();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getPrivilegeids() {
        return privilegeids;
    }

    public void setPrivilegeids(String privilegeids) {
        this.privilegeids = privilegeids == null ? null : privilegeids.trim();
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe == null ? null : describe.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}