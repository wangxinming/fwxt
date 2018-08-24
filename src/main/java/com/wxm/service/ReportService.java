package com.wxm.service;

import com.wxm.entity.ReportResult;
import com.wxm.model.OAEnterprise;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ReportService {
    ReportResult getTotal(Date startTime, Date endTime,Integer templateId);
    ReportResult caculateTotal(List<ReportResult> reportResults);
    ReportResult getCurrent(final Map<Integer,OAEnterprise> map,String name,Date startTime, Date endTime,Integer templateId);
//    Map<Integer,OAEnterprise> listEnterprise(String location,String province,String city);
    Map<Integer, OAEnterprise> listEnterprise( List<OAEnterprise> oaEnterpriseList,boolean subCompany);
    List<ReportResult> getReportList(Date startTime, Date endTime,Integer templateId);
    List<ReportResult> caculateRate(List<ReportResult> reportResults);

    List<ReportResult> typeBylocation(final Map<Integer,OAEnterprise> map,List<ReportResult> reportResults);

}
