package com.wxm.entity;


import java.util.Date;

public class ProInstance {
    private String id;
    private String deployName;
    private String deployId;
    private String title;
    private Date createTime;
    private String status;
    public void setDeployId(String deployId){
        this.deployId = deployId;
    }
    public String getDeployId(){
        return deployId;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return id;
    }
    public void setDeployName(String deployName){
        this.deployName = deployName;
    }
    public String getDeployName(){
        return deployName;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return title;
    }
    public void setCreateTime(Date createTime){
        this.createTime = createTime;
    }
    public Date getCreateTime(){
        return createTime;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public String getStatus(){
        return status;
    }
}
