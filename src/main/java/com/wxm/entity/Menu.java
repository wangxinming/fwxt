package com.wxm.entity;

public class Menu implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String  code;
    private String name;
    private Boolean permission=true;
    public Menu(){};

    public Menu(String code,String name,Boolean permission){
        this.code = code;
        this.name = name;
        this.permission = permission;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Boolean getPermission() {
        return permission;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPermission(Boolean permission) {
        this.permission = permission;
    }
}
