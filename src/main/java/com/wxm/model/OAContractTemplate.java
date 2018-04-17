package com.wxm.model;

import java.util.Date;

public class OAContractTemplate {
    private Integer templateId;

    private String templateName;

    private Integer userId;

    private String userName;

    private Integer templateStatus;

    private String templateDes;

    private Date templateCreatetime;

    private String templateHtml;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName == null ? null : templateName.trim();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTemplateStatus() {
        return templateStatus;
    }

    public void setTemplateStatus(Integer templateStatus) {
        this.templateStatus = templateStatus;
    }

    public String getTemplateDes() {
        return templateDes;
    }

    public void setTemplateDes(String templateDes) {
        this.templateDes = templateDes == null ? null : templateDes.trim();
    }

    public Date getTemplateCreatetime() {
        return templateCreatetime;
    }

    public void setTemplateCreatetime(Date templateCreatetime) {
        this.templateCreatetime = templateCreatetime;
    }

    public String getTemplateHtml() {
        return templateHtml;
    }

    public void setTemplateHtml(String templateHtml) {
        this.templateHtml = templateHtml == null ? null : templateHtml.trim();
    }
}