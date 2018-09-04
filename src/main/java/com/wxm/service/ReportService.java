package com.wxm.service;

import com.wxm.entity.ReportResult;
import com.wxm.model.OAEnterprise;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ReportService {
    ReportResult getTotal(Date startTime, Date endTime,Integer templateId);
    ReportResult caculateTotal(List<ReportResult> reportResults,String name);
    ReportResult getCurrent(final Map<Integer,OAEnterprise> map,String name,Date startTime, Date endTime,Integer templateId);
    Map<Integer, OAEnterprise> listEnterprise( List<OAEnterprise> oaEnterpriseList,boolean subCompany);
    List<ReportResult> getReportList(Date startTime, Date endTime,Integer templateId);
    List<ReportResult> caculateRate(List<ReportResult> reportResults);
    List<ReportResult> typeBylocation(final Map<Integer,OAEnterprise> map,List<ReportResult> reportResults);
    List<ReportResult> typeByCompanyLevel(final Map<Integer,OAEnterprise> map,List<ReportResult> reportResults,Integer level,boolean subCompany);

    List<ReportResult> parentEnterpriseReport(Integer headOffice,boolean subCompany,Integer parentCompany,Integer contractPromoter,
                                              Integer contractType,Date startTime,Date endTime);

    List<ReportResult> fieldReport(Integer headOffice,boolean subCompany, Integer parentCompany,Integer contractPromoter,Integer contractTyp,String templateType,
                                   String field,String condition,Date startTime,Date endTime);

    List<ReportResult> nonFormatReport(Integer headOffice,boolean subCompany,Integer parentCompany,Integer contractPromoter,Integer contractType,
                                       Date startTime,Date endTime);

    List<ReportResult> locationReport(String location,String province,String city,Integer contractType, Date startTime, Date endTime);

}
