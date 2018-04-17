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
    private OAContractTemplate oaContractTemplate;

    public void setOAContractTemplate(OAContractTemplate oaContractTemplate){
        this.oaContractTemplate = oaContractTemplate;
    }
    public OAContractTemplate getOAContractTemplate() {
        return oaContractTemplate;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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
