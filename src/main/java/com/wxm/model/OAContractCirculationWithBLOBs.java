package com.wxm.model;

public class OAContractCirculationWithBLOBs extends OAContractCirculation {
    private String contractHtml;

    private String contractPdf;

    public String getContractHtml() {
        return contractHtml;
    }

    public void setContractHtml(String contractHtml) {
        this.contractHtml = contractHtml == null ? null : contractHtml.trim();
    }

    public String getContractPdf() {
        return contractPdf;
    }

    public void setContractPdf(String contractPdf) {
        this.contractPdf = contractPdf == null ? null : contractPdf.trim();
    }
}