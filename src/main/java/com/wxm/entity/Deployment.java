package com.wxm.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wxm.model.OAContractTemplate;
import org.activiti.rest.common.util.DateToStringSerializer;

import java.util.Date;

public class Deployment {
    private String id;//编号
    private String name;//名称
    private Date deploymentTime;//部署时间
    private String category;//
    private String tenantId;//
    private String version;
    private Integer status;
    private OAContractTemplate oaContractTemplate;

    public void setOAContractTemplate(OAContractTemplate oaContractTemplate){
        this.oaContractTemplate = oaContractTemplate;
    }
    public OAContractTemplate getOAContractTemplate() {
        return oaContractTemplate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public  Date getDeploymentTime() {
        return deploymentTime;
    }
    public void setDeploymentTime( Date deploymentTime) {
        this.deploymentTime = deploymentTime;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    public String getTenantId() {
        return tenantId;
    }
}
