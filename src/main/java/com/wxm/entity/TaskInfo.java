package com.wxm.entity;

import java.util.Date;

public class TaskInfo {
    private String id;//任务ID
    private String name;//任务名称
    private String title;//任务名称
    private String assignee;//任务的办理人
    private Date timestamp; //任务的创建时间
    private String processInstanceId;//流程实例ID
    private String executionId;//执行对象ID
    private String processDefinitionId;//流程定义ID

    public void setId(String id){
        this.id = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setAssignee(String assignee){
        this.assignee = assignee;
    }
    public void setTimestamp(Date timestamp){
        this.timestamp = timestamp;
    }
    public void setProcessInstanceId(String processDefinitionId){
        this.processDefinitionId = processDefinitionId;
    }
    public void setExecutionId(String executionId){
        this.executionId = executionId;
    }
    public void setProcessDefinitionId(String processDefinitionId){
        this.processDefinitionId = processDefinitionId;
    }
    public String getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public Date getTimestamp(){
        return timestamp;
    }
    public String getTitle(){
        return title;
    }
    public String getAssignee(){
        return assignee;
    }
    public String getProcessInstanceId(){
        return processInstanceId;
    }
    public String getExecutionId(){
        return executionId;
    }
    public String getProcessDefinitionId(){
        return processDefinitionId;
    }
}
