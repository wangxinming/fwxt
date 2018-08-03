package com.wxm.entity;

public class ReportResult {
    private String enterprise;
    private Integer total;
    private Integer complete;
    private Integer refuse;
    private String rate;

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
