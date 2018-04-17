package com.wxm.entity;

import java.util.Date;

public class SearchQuery {

    private String userName;
    private Date startTime;
    private Date endTime;
    private int limit;
    private int offset;
    private String orderBy;
    private String orderByType;

    public Date getEndTime() {
        return endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public String getOrderByType() {
        return orderByType;
    }

    public String getUserName() {
        return userName;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public void setOrderByType(String orderByType) {
        this.orderByType = orderByType;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
