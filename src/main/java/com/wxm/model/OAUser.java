package com.wxm.model;


import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class OAUser {
    private Integer userId;

    private Integer groupId;

    private String userName;

    private int parentId;

    private String userMobile;

    private String userEmail;

    private String userPwd;

    private Integer enterpriseId;

    private String userCompany;

    private String userDepartment;

    private String userPosition;

    private String userAddress;

    private String userPostcode;

    private String userWeixin;

    private Integer userStatus;

    private List<OAUser> children;

    private Date userCreatetime;

    public OAUser(){

    }

    public OAUser(String userName){
        this.userName = userName;
        userAddress="";
        userCompany="";
        groupId = 0;
        userCreatetime= new Date(System.currentTimeMillis());
        userDepartment="";
        userEmail = "";
        userMobile="";
        userPosition = "";
        userPwd = "";
        enterpriseId = 0;
        userStatus = 1;
        userWeixin ="";
    }

    public Integer getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Integer enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public List<OAUser> getOaUser() {
        return children;
    }

    public void setOaUser(List<OAUser> list) {
        if(children == null){
            children = list;
        }else {
            this.children.addAll(list);
        }
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile == null ? null : userMobile.trim();
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail == null ? null : userEmail.trim();
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd == null ? null : userPwd.trim();
    }

    public String getUserCompany() {
        return userCompany;
    }

    public void setUserCompany(String userCompany) {
        this.userCompany = userCompany == null ? null : userCompany.trim();
    }

    public String getUserDepartment() {
        return userDepartment;
    }

    public void setUserDepartment(String userDepartment) {
        this.userDepartment = userDepartment == null ? null : userDepartment.trim();
    }

    public String getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(String userPosition) {
        this.userPosition = userPosition == null ? null : userPosition.trim();
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress == null ? null : userAddress.trim();
    }

    public String getUserPostcode() {
        return userPostcode;
    }

    public void setUserPostcode(String userPostcode) {
        this.userPostcode = userPostcode == null ? null : userPostcode.trim();
    }

    public String getUserWeixin() {
        return userWeixin;
    }

    public void setUserWeixin(String userWeixin) {
        this.userWeixin = userWeixin == null ? null : userWeixin.trim();
    }

    public Integer getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }

    public Date getUserCreatetime() {
        return userCreatetime;
    }

    public void setUserCreatetime(Date userCreatetime) {
        this.userCreatetime = userCreatetime;
    }
}