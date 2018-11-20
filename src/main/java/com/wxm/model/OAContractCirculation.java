package com.wxm.model;

import java.math.BigDecimal;
import java.util.Date;

public class OAContractCirculation {
    private Integer contractId;

    private Integer templateId;
    private Integer enterpriseId;
    private Integer contractReopen;

    private String processInstanceId;
    private String attachmentName;
    private String contractName;
    private String contractBuyer;
    private String contractSeller;
    private BigDecimal contractMoney;
    private Integer userId;

    private  Integer workStatus;

    private String contractStatus;

    private String description;
    private String workDate;
    private String contractSerialNumber;
    private String archiveSerialNumber;

    private Date createTime;

    public String getContractBuyer() {
        return contractBuyer;
    }

    public void setContractMoney(BigDecimal contractMoney) {
        this.contractMoney = contractMoney;
    }

    public BigDecimal getContractMoney() {
        return contractMoney;
    }

    public String getContractSeller() {
        return contractSeller;
    }

    public void setContractBuyer(String contractBuyer) {
        this.contractBuyer = contractBuyer;
    }



    public void setContractSeller(String contractSeller) {
        this.contractSeller = contractSeller;
    }

    public Integer getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Integer enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getArchiveSerialNumber() {
        return archiveSerialNumber;
    }

    public String getContractSerialNumber() {
        return contractSerialNumber;
    }

    public String getWorkDate() {
        return workDate;
    }

    public void setArchiveSerialNumber(String archiveSerialNumber) {
        this.archiveSerialNumber = archiveSerialNumber;
    }

    public void setContractSerialNumber(String contractSerialNumber) {
        this.contractSerialNumber = contractSerialNumber;
    }

    public void setWorkDate(String workDate) {
        this.workDate = workDate;
    }

    public Integer getContractReopen() {
        return contractReopen;
    }

    public void setContractReopen(Integer contractReopen) {
        this.contractReopen = contractReopen;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Integer getContractId() {
        return contractId;
    }

    public void setContractId(Integer contractId) {
        this.contractId = contractId;
    }

    public Integer getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(Integer workStatus) {
        this.workStatus = workStatus;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName == null ? null : contractName.trim();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(String contractStatus) {
        this.contractStatus = contractStatus == null ? null : contractStatus.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}