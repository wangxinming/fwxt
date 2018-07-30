package com.wxm.model;

import java.util.Date;

public class OAPositionRelation {
    private Integer positionRelationId;

    private String company;

    private String positionName;

    private String highCompany;

    private String highPositionName;

    private Date createTime;

    public Integer getPositionRelationId() {
        return positionRelationId;
    }

    public void setPositionRelationId(Integer positionRelationId) {
        this.positionRelationId = positionRelationId;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company == null ? null : company.trim();
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName == null ? null : positionName.trim();
    }

    public String getHighCompany() {
        return highCompany;
    }

    public void setHighCompany(String highCompany) {
        this.highCompany = highCompany == null ? null : highCompany.trim();
    }

    public String getHighPositionName() {
        return highPositionName;
    }

    public void setHighPositionName(String highPositionName) {
        this.highPositionName = highPositionName == null ? null : highPositionName.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}