package com.wxm.entity;

public class ReportResult {
    private String enterprise;
    private Integer enterpriseId;
    private Integer total;
    private Integer complete;
    private Integer refuse;
    private String rate;
    public ReportResult(){

    }
    public ReportResult(String enterprise,Integer enterpriseId,Integer total,Integer complete,Integer refuse,String rate){
        this.enterprise = enterprise;
        this.enterpriseId = enterpriseId;
        this.total = 0;
        this.complete = 0;
        this.refuse = 0;
        this.rate = rate;
    }

    public void setEnterpriseId(Integer enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public Integer getEnterpriseId() {
        return enterpriseId;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getTotal() {
        return total;
    }

    public Integer getComplete() {
        return complete;
    }

    public Integer getRefuse() {
        return refuse;
    }

    public String getEnterprise() {
        return enterprise;
    }

    public String getRate() {
        return rate;
    }

    public void setComplete(Integer complete) {
        this.complete = complete;
    }

    public void setEnterprise(String enterprise) {
        this.enterprise = enterprise;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public void setRefuse(Integer refuse) {
        this.refuse = refuse;
    }
}
