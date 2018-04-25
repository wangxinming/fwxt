package com.wxm.model;

public class OAContractCirculationWithBLOBs extends OAContractCirculation {
    private String contractHtml;

    private byte[] contractPdf;

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