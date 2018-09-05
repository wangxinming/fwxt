package com.wxm.entity;

public class ReportResult {
    private String enterprise;
    private Integer enterpriseId;
    private Integer total;
    private Integer complete;
    private Integer refuse;
    private Long priceTotal;
    private Long priceComplete;
    private Long priceRefuse;

    private String rate;
    public ReportResult(){
        this.enterpriseId = -1;
        this.total = 0;
        this.complete = 0;
        this.refuse = 0;
        this.priceTotal = 0L;
        this.priceComplete = 0L;
        this.priceRefuse = 0L;
    }
    public ReportResult(String enterprise,Integer enterpriseId,Integer total,Integer complete,Integer refuse,Long priceTotal,Long priceComplete,Long priceRefuse,String rate){
        this.enterprise = enterprise;
        this.enterpriseId = enterpriseId;
        this.total = total==null?0:total;
        this.complete = complete==null?0:complete;
        this.refuse = refuse ==null?0:refuse;
        this.priceTotal = priceTotal == null?0:priceTotal;
        this.priceComplete = priceComplete == null?0:priceComplete;
        this.priceRefuse = priceRefuse == null?0:priceRefuse;
        this.rate = rate;
    }

    public ReportResult(String enterprise,Integer enterpriseId,ReportItem itemTotal,ReportItem itemComplete,ReportItem itemRefuse,String rate){
        this();
        this.enterprise = enterprise;
        this.enterpriseId = enterpriseId;
        this.rate = rate;
        if(null != itemTotal) {
            this.total = itemTotal.getY() == null?0:itemTotal.getY();
            this.priceTotal = itemTotal.getZ() == null?0:itemTotal.getZ();
        }
        if(null != itemComplete){
            this.complete = itemComplete.getY() == null?0:itemComplete.getY();
            this.priceComplete  = itemComplete.getZ() == null?0:itemComplete.getZ();
        }
        if(null != itemRefuse) {
            this.refuse = itemRefuse.getY() == null?0:itemRefuse.getY();
            this.priceRefuse = itemRefuse.getZ() == null?0:itemRefuse.getZ();
        }
    }


    public Long getPriceComplete() {
        return priceComplete;
    }

    public Long getPriceRefuse() {
        return priceRefuse;
    }

    public Long getPriceTotal() {
        return priceTotal;
    }

    public void setPriceComplete(Long priceComplete) {
        this.priceComplete = priceComplete==null?0:priceComplete;
    }

    public void setPriceRefuse(Long priceRefuse) {
        this.priceRefuse = priceRefuse == null?0:priceRefuse;
    }

    public void setPriceTotal(Long priceTotal) {
        this.priceTotal = priceTotal == null?0:priceTotal;
    }

    public void setEnterpriseId(Integer enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public Integer getEnterpriseId() {
        return enterpriseId;
    }

    public void setTotal(Integer total) {
        this.total = total == null?0:total;
    }
    public void setComplete(Integer complete) {
        this.complete = complete == null?0:complete;
    }
    public void setRefuse(Integer refuse) {
        this.refuse = refuse == null?0:refuse;
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

    public void setEnterprise(String enterprise) {
        this.enterprise = enterprise;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }


}
