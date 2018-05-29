package com.wxm.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class User implements Serializable {
    private static final long serialVersionUID = 8809101560720973267L;
    private Integer id;
    private String name;
    private String password;
    private String email;
    private String mobile;
    private String company;
    private String position;
    private String department;
    private String address;
    private String weixin;
    private Integer age;
    private String realName;
    private Integer parentId;
    private boolean active;
    private Timestamp createTime;

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setCompany(String company){
        this.company = company;
    }

    public String getCompany() {
        return company;
    }
    public void setPosition(String position){
        this.position = position;
    }
    public String getPosition(){
        return position;
    }

    public void setDepartment(String department){
        this.department = department;
    }
    public String getDepartment(){
        return department;
    }
    public void setAddress(String address){
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setWeixin(String weixin) {
        this.weixin = weixin;
    }
    public String getWeixin(){
        return weixin;
    }

    public void  setActive(boolean active){
        this.active = active;
    }
    public boolean getActive(){
        return active;
    }
    public void setCreateTime(Timestamp createTime){
        this.createTime = createTime;
    }
    public Timestamp getCreateTime(){
        return createTime;
    }
    public void  setRealName(String realName){
        this.realName = realName;
    }
    public String getRealName(){
        return realName;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public String getEmail(){
        return email;
    }
    public void setMobile(String mobile){
        this.mobile = mobile;
    }
    public String getMobile(){
        return mobile;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public String getPassword(){
        return password;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
