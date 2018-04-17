package com.wxm.model;

import java.util.Date;

public class OAAudit {

    private Integer auditId;

    private String userName;

    private String content;

    private Date createTime;

    public OAAudit(){

    }
    public OAAudit(String userName,String content){
        this.userName = userName;
        this.content = content;
        this.createTime = new Date();
    }
    public Integer getAuditId() {
        return auditId;
    }

    public void setAuditId(Integer auditId) {
        this.auditId = auditId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}