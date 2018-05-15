package com.wxm.entity;

import java.util.List;

public class ReportEntity {
    private long completedCount;
    private long involveCount;
    private Integer total;
    private Integer customNum;
    private Integer templateNum;
    private List<ReportItem> reportItemList;


    public long getCompletedCount() {
        return completedCount;
    }

    public long getInvolveCount() {
        return involveCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public void setInvolveCount(long involveCount) {
        this.involveCount = involveCount;
    }

    public Integer getCustomNum() {
        return customNum;
    }

    public Integer getTemplateNum() {
        return templateNum;
    }

    public void setCustomNum(Integer customNum) {
        this.customNum = customNum;
    }

    public void setTemplateNum(Integer templateNum) {
        this.templateNum = templateNum;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<ReportItem> getReportItemList() {
        return reportItemList;
    }

    public void setReportItemList(List<ReportItem> reportItemList) {
        this.reportItemList = reportItemList;
    }
}
