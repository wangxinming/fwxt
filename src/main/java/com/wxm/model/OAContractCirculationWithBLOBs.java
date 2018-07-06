package com.wxm.model;

public class OAContractCirculationWithBLOBs extends OAContractCirculation {
    private String contractHtml;

    private byte[] contractPdf;

    private byte[] attachmentContent;

    public void setAttachmentContent(byte[] attachmentContent) {
        this.attachmentContent = attachmentContent;
    }

    public byte[] getAttachmentContent() {
        return attachmentContent;
    }

    public String getContractHtml() {
        return contractHtml;
    }

    public void setContractHtml(String contractHtml) {
        this.contractHtml = contractHtml == null ? null : contractHtml.trim();
    }

    public byte[] getContractPdf() {
        return contractPdf;
    }

    public void setContractPdf(byte[] contractPdf) {
        this.contractPdf = contractPdf;
    }
}