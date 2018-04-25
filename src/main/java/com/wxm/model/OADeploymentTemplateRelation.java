package com.wxm.model;

import java.util.Date;

public class OADeploymentTemplateRelation {
    private Integer relationId;

    private String relationDeploymentid;

    private Integer relationTemplateid;

    private Date relationCreatetime;

    public Integer getRelationId() {
        return relationId;
    }

    public void setRelationId(Integer relationId) {
        this.relationId = relationId;
    }

    public String getRelationDeploymentid() {
        return relationDeploymentid;
    }

    public void setRelationDeploymentid(String relationDeploymentid) {
        this.relationDeploymentid = relationDeploymentid == null ? null : relationDeploymentid.trim();
    }

    public Integer getRelationTemplateid() {
        return relationTemplateid;
    }

    public void setRelationTemplateid(Integer relationTemplateid) {
        this.relationTemplateid = relationTemplateid;
    }

    public Date getRelationCreatetime() {
        return relationCreatetime;
    }

    public void setRelationCreatetime(Date relationCreatetime) {
        this.relationCreatetime = relationCreatetime;
    }
}