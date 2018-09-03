package com.wxm.entity;

public class ReportItem {
    private String  name;
    private Integer id;
    private Integer y;
    private Long z;


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

    public Long getZ() {
        return z;
    }

    public void setZ(Long z) {
        this.z = z;
    }

    public void setY(Integer y) {
        this.y = y;
    }

}
