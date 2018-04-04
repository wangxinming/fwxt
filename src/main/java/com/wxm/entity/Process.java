package com.wxm.entity;

public class Process implements java.io.Serializable  {
    private int id;
    private int offset;
    private int limit;

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
    public int getOffset() {
        return offset;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
    public int getLimit() {
        return limit;
    }
}
