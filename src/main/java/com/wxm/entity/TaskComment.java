package com.wxm.entity;


import java.util.Date;

public class TaskComment {
    private String name;
    private String description;
    private Date createTime;
    private String content;

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setName(String name){
        this.name = name;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public void setCreateTime(Date createTime){
        this.createTime = createTime;
    }
    public String getName(){
        return name;
    }
    public String getDescription(){
        return description;
    }
    public Date getCreateTime(){
        return createTime;
    }
}
