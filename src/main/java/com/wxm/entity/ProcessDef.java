package com.wxm.entity;

public class ProcessDef implements java.io.Serializable  {
    private String id; //流程定义ID
    private String name; //流程定义名称
    private String key; //流程定义的key
    private int version;//流程定义的版本
    private String resourceName;//资源名称bpmn文件
    private String diagramResourceName;//资源名称png文件
    private String deploymentId;//部署对象ID

    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public String getKey() {
        return key;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    public int getVersion() {
        return version;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    public String getResourceName() {
        return resourceName;
    }

    public void setDiagramResourceName(String diagramResourceName) {
        this.diagramResourceName = diagramResourceName;
    }
    public String getDiagramResourceName() {
        return diagramResourceName;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }
    public String getDeploymentId() {
        return deploymentId;
    }



}
