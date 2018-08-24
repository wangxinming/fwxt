package com.wxm.service.impl;

import com.wxm.entity.ReportItem;
import com.wxm.entity.ReportResult;
import com.wxm.model.OAEnterprise;
import com.wxm.service.ContractCirculationService;
import com.wxm.service.OAEnterpriseService;
import com.wxm.service.ReportService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ContractCirculationService contractCirculationService;
    @Autowired
    private OAEnterpriseService oaEnterpriseService;


    @Override
    public ReportResult getTotal(Date startTime, Date endTime,Integer templateId) {
        Integer totalContract = contractCirculationService.total(null,templateId,null,"template",startTime,endTime);
        Integer totalCompleted = contractCirculationService.total("completed",templateId,null,"template",startTime,endTime);
        Integer totalRefused = contractCirculationService.total(null,templateId,1,"template",startTime,endTime);
        return new ReportResult("总计",-1,totalContract,totalCompleted,totalRefused,"0.00%");
    }

    @Override
    public ReportResult caculateTotal(List<ReportResult> reportResults) {
        Integer total = 0,complete = 0,refuse = 0;
        for(ReportResult reportResult:reportResults){
            if(null != reportResult.getTotal())
                total += reportResult.getTotal();
            if(null != reportResult.getComplete())
                complete += reportResult.getComplete();
            if(null != reportResult.getRefuse())
                refuse += reportResult.getRefuse();
        }
        return new ReportResult("总计",-1,total,complete,refuse,"");
    }

    @Override
    public ReportResult getCurrent(final Map<Integer,OAEnterprise> map,String name,Date startTime, Date endTime, Integer templateId) {
        List<ReportResult> reportResults = getReportList(startTime,endTime,templateId);
        ReportResult reportResultCur = new ReportResult(name,-1,0,0,0,"0.00%");
        for(ReportResult reportResult :reportResults) {
            if(!map.containsKey(reportResult.getEnterpriseId())) continue;
            reportResultCur.setTotal(reportResultCur.getTotal() + reportResult.getTotal());
            reportResultCur.setRefuse(reportResultCur.getRefuse() + reportResult.getRefuse());
            reportResultCur.setComplete(reportResultCur.getComplete() + reportResult.getComplete());
        }
        return reportResultCur;
    }

//    @Override
//    public Map<Integer, OAEnterprise> listEnterprise(String location, String province, String city) {
//        List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.listEnterprise(location,province,city);
//        Map<Integer,OAEnterprise> map = new LinkedHashMap<>();
//        for(OAEnterprise oaEnterprise : oaEnterpriseList){
//            map.put(oaEnterprise.getEnterpriseId(),oaEnterprise);
//        }
//        return map;
//    }


    public Map<Integer, OAEnterprise> listEnterprise( List<OAEnterprise> oaEnterpriseList,boolean subCompany) {
        Map<Integer,OAEnterprise> map = new LinkedHashMap<>();
        for(OAEnterprise oaEnterprise : oaEnterpriseList){
            map.put(oaEnterprise.getEnterpriseId(),oaEnterprise);
            if(subCompany){
                oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(oaEnterprise.getEnterpriseId());
                for(OAEnterprise oaEnterprise1 : oaEnterpriseList){
                    map.put(oaEnterprise1.getEnterpriseId(),oaEnterprise1);
                }
            }
        }
        return map;
    }

    @Override
    public List<ReportResult> getReportList(Date startTime, Date endTime, Integer templateId) {
        // 归档数量
        List<ReportItem> reportItemList1 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"template","completed",null,templateId,null);
        // 退回数量
        List<ReportItem> reportItemList2 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"template",null,null,templateId,1);
        //发起数量
        List<ReportItem> reportItemList3 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"template",null,null,templateId,null);
        List<ReportResult> reportResults = new LinkedList<>();
        //计算公司报告
        for(ReportItem ri:reportItemList3 ){
            ReportResult reportResult = new ReportResult();
            reportResults.add(reportResult);
            reportResult.setEnterpriseId(ri.getId());
            reportResult.setTotal(ri.getY());
            for(ReportItem reportItem:reportItemList1) {
                if(reportItem.getId() == ri.getId()) {
                    reportResult.setComplete(reportItem.getY());
                }else{
                    reportResult.setComplete(0);
                }
            }
            for(ReportItem reportItem:reportItemList2) {
                if(reportItem.getId() == ri.getId()) {
                    reportResult.setRefuse(reportItem.getY());
                }else{
                    reportResult.setRefuse(0);
                }
            }
        }
        return reportResults;
    }

    @Override
    public List<ReportResult> caculateRate(List<ReportResult> reportResults) {
        for(ReportResult reportResult :reportResults){
            if(reportResult.getTotal() > 0 && reportResult.getComplete() != null){
                NumberFormat numberFormat = NumberFormat.getInstance();
                // 设置精确到小数点后2位
                numberFormat.setMaximumFractionDigits(2);
                reportResult.setRate(numberFormat.format((float)reportResult.getComplete()/(float)reportResult.getTotal()*100)+"%");
            }else{
                reportResult.setRate("0.00%");
            }
        }
        return reportResults;
    }

    @Override
    public List<ReportResult> typeBylocation(Map<Integer, OAEnterprise> map, List<ReportResult> reportResults) {
        for(ReportResult reportResult :reportResults){
            if(map.containsKey(reportResult.getEnterpriseId())){
                OAEnterprise tmp = map.get(reportResult.getEnterpriseId());
                if(StringUtils.isBlank(tmp.getCompanyProvince())){
                    reportResult.setEnterprise(tmp.getLocation());
                }else {
                    reportResult.setEnterprise(tmp.getLocation()+"-"+tmp.getCompanyProvince());
                }
            }
        }

        Map<String, Integer> sumTotal = reportResults.stream().collect(Collectors.groupingBy(ReportResult::getEnterprise,
                Collectors.summingInt(ReportResult::getTotal)));

        Map<String, Integer> sumComplete = reportResults.stream().collect(Collectors.groupingBy(ReportResult::getEnterprise,
                Collectors.summingInt(ReportResult::getComplete)));

        Map<String, Integer> sumRefuse = reportResults.stream().collect(Collectors.groupingBy(ReportResult::getEnterprise,
                Collectors.summingInt(ReportResult::getRefuse)));

        reportResults.clear();
        for(Map.Entry entry : sumTotal.entrySet()){
            ReportResult reportResult = new ReportResult((String) entry.getKey(),-1,
                    (Integer) entry.getValue(),
                    sumComplete.get(entry.getKey()).intValue(),sumRefuse.get(entry.getKey()).intValue(),"0.00%");
            reportResults.add(reportResult);
        }
        return reportResults;
    }
}
