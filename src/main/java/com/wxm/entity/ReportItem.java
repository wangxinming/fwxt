package com.wxm.entity;

public class ReportItem {
    private String  name;
    private Integer id;
    private Integer y;
    private long sum;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }

    public long getSum() {
        return sum;
    }
}
