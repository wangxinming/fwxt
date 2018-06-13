package com.wxm.model;

import java.util.Date;

public class OAFormProperties {
    private Integer propertiesId;

    private Integer templateId;

    private String templateName;

    private String fieldMd5;

    private String fieldName;

    private String fieldType;

    private String fieldValid;

    private Integer  status;

    private Date createTime;

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getFieldMd5() {
        return fieldMd5;
    }

    public void setFieldMd5(String fieldMd5) {
        this.fieldMd5 = fieldMd5;
    }

    public Integer getPropertiesId() {
        return propertiesId;
    }

    public void setPropertiesId(Integer propertiesId) {
        this.propertiesId = propertiesId;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName == null ? null : fieldName.trim();
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType == null ? null : fieldType.trim();
    }

    public String getFieldValid() {
        return fieldValid;
    }

    public void setFieldValid(String fieldValid) {
        this.fieldValid = fieldValid == null ? null : fieldValid.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}