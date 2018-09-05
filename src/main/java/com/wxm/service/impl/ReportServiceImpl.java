package com.wxm.service.impl;

import com.wxm.entity.ReportItem;
import com.wxm.entity.ReportResult;
import com.wxm.model.OAEnterprise;
import com.wxm.model.OAUser;
import com.wxm.service.ContractCirculationService;
import com.wxm.service.OAEnterpriseService;
import com.wxm.service.ReportService;
import com.wxm.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.LongLongSeqHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Autowired
    private ContractCirculationService contractCirculationService;
    @Autowired
    private OAEnterpriseService oaEnterpriseService;
    @Autowired
    private UserService userService;


    @Override
    public ReportResult getTotal(Date startTime, Date endTime,Integer templateId) {
        ReportItem totalContract = contractCirculationService.total(null,templateId,null,"template",startTime,endTime);
        ReportItem totalCompleted = contractCirculationService.total("completed",templateId,null,"template",startTime,endTime);
        ReportItem totalRefused = contractCirculationService.total(null,templateId,1,"template",startTime,endTime);
        return new ReportResult("总计",-1,totalContract,totalCompleted,totalRefused,"0.00%");
    }

    @Override
    public ReportResult caculateTotal(List<ReportResult> reportResults,String name) {
        Integer total = 0,complete = 0,refuse = 0;
        long totalPrice = 0,completePrice = 0,refusePrice = 0;
        for(ReportResult reportResult:reportResults){
            if(null != reportResult.getTotal())
                total += reportResult.getTotal();
            if(null != reportResult.getComplete())
                complete += reportResult.getComplete();
            if(null != reportResult.getRefuse())
                refuse += reportResult.getRefuse();
            totalPrice+=reportResult.getPriceTotal();
            completePrice+= reportResult.getPriceComplete();
            refusePrice+=reportResult.getPriceRefuse();
        }
        return new ReportResult(name,-1,total,complete,refuse,totalPrice,completePrice,refusePrice,"");
    }

    @Override
    public ReportResult getCurrent(final Map<Integer,OAEnterprise> map,String name,Date startTime, Date endTime, Integer templateId) {
        List<ReportResult> reportResults = getReportList(startTime,endTime,templateId);
        ReportResult reportResultCur = new ReportResult(name,-1,0,0,0,0L,0L,0L,"0.00%");
        for(ReportResult reportResult :reportResults) {
            if(!map.containsKey(reportResult.getEnterpriseId())) continue;
            reportResultCur.setTotal(reportResultCur.getTotal() + reportResult.getTotal());
            reportResultCur.setRefuse(reportResultCur.getRefuse() + reportResult.getRefuse());
            reportResultCur.setComplete(reportResultCur.getComplete() + reportResult.getComplete());

            reportResultCur.setPriceTotal(reportResultCur.getPriceTotal() + reportResult.getPriceTotal());
            reportResultCur.setPriceRefuse(reportResultCur.getPriceRefuse() + reportResult.getPriceRefuse());
            reportResultCur.setPriceComplete(reportResultCur.getPriceComplete() + reportResult.getPriceComplete());
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
            reportResult.setTotal(ri.getY() == null?0:ri.getY());
            reportResult.setPriceTotal(ri.getZ());
            for(ReportItem reportItem:reportItemList1) {
                if(reportItem.getId() == ri.getId()) {
                    reportResult.setComplete(reportItem.getY());
                    reportResult.setPriceComplete(reportItem.getZ());
                }
            }
            for(ReportItem reportItem:reportItemList2) {
                if(reportItem.getId() == ri.getId()) {
                    reportResult.setRefuse(reportItem.getY());
                    reportResult.setPriceRefuse(reportItem.getZ());
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

        Map<String, Long> sumTotalPrice = reportResults.stream().collect(Collectors.groupingBy(ReportResult::getEnterprise,
                Collectors.summingLong(ReportResult::getPriceTotal)));

        Map<String,Long> sumCompletePrice = reportResults.stream().collect(Collectors.groupingBy(ReportResult::getEnterprise,
                Collectors.summingLong(ReportResult::getPriceComplete)));

        Map<String, Long> sumRefusePrice = reportResults.stream().collect(Collectors.groupingBy(ReportResult::getEnterprise,
                Collectors.summingLong(ReportResult::getPriceRefuse)));

        reportResults.clear();
        for(Map.Entry entry : sumTotal.entrySet()){
            Integer complete = 0,refuse = 0;
            long totalPrice = 0,completePrice = 0,refusePrise = 0;

            if(sumComplete.containsKey(entry.getKey())){
                complete = sumComplete.get(entry.getKey()).intValue();
            }
            if(sumRefuse.containsKey(entry.getKey())){
                refuse = sumRefuse.get(entry.getKey()).intValue();
            }
            if(sumTotalPrice.containsKey(entry.getKey())){
                totalPrice = sumTotalPrice.get(entry.getKey()).longValue();
            }
            if(sumCompletePrice.containsKey(entry.getKey())){
                completePrice = sumCompletePrice.get(entry.getKey()).longValue();
            }
            if(sumRefusePrice.containsKey(entry.getKey())){
                refusePrise = sumRefusePrice.get(entry.getKey()).longValue();
            }

            ReportResult reportResult = new ReportResult((String) entry.getKey(),-1,
                    (Integer) entry.getValue(),
                    complete,
                    refuse,
                    totalPrice,
                    completePrice,
                    refusePrise,
                    "0.00%");
            reportResults.add(reportResult);
        }
        reportResults.add(caculateTotal(reportResults,"总计"));

        return reportResults;
    }

    @Override
    public List<ReportResult> typeByCompanyLevel(Map<Integer, OAEnterprise> map, List<ReportResult> reportResults, Integer level,boolean subCompany) {
        for(ReportResult reportResult :reportResults){
            if(level == 1){
                if(map.get(reportResult.getEnterpriseId()).getCompanyLevel() != 1){
                    continue;
                }
                reportResult.setEnterprise(map.get(reportResult.getEnterpriseId()).getCompanyName());
            }else if(level == 2){
                if(map.get(reportResult.getEnterpriseId()).getCompanyLevel() == 1){
                    continue;
                }
                if(subCompany){
                    if(map.get(reportResult.getEnterpriseId()).getCompanyLevel() == 3) {
                        reportResult.setEnterprise(map.get(map.get(reportResult.getEnterpriseId()).getCompanyParent()).getCompanyName());
                    }else{
                        reportResult.setEnterprise(map.get(reportResult.getEnterpriseId()).getCompanyName());
                    }
                }else{
                    if(map.get(reportResult.getEnterpriseId()).getCompanyLevel() != 2){
                        continue;
                    }
                    reportResult.setEnterprise(map.get(reportResult.getEnterpriseId()).getCompanyName());
                }
            }else{
                if(map.get(reportResult.getEnterpriseId()).getCompanyLevel() != 3){
                    continue;
                }
                reportResult.setEnterprise(map.get(reportResult.getEnterpriseId()).getCompanyName());
            }
        }

        Map<String, Integer> sumTotal = reportResults.stream().collect(Collectors.groupingBy(ReportResult::getEnterprise,
                Collectors.summingInt(ReportResult::getTotal)));

        Map<String, Integer> sumComplete = reportResults.stream().collect(Collectors.groupingBy(ReportResult::getEnterprise,
                Collectors.summingInt(ReportResult::getComplete)));

        Map<String, Integer> sumRefuse = reportResults.stream().collect(Collectors.groupingBy(ReportResult::getEnterprise,
                Collectors.summingInt(ReportResult::getRefuse)));


        Map<String, Long> sumTotalPrice = reportResults.stream().collect(Collectors.groupingBy(ReportResult::getEnterprise,
                Collectors.summingLong(ReportResult::getPriceTotal)));

        Map<String,Long> sumCompletePrice = reportResults.stream().collect(Collectors.groupingBy(ReportResult::getEnterprise,
                Collectors.summingLong(ReportResult::getPriceComplete)));

        Map<String, Long> sumRefusePrice = reportResults.stream().collect(Collectors.groupingBy(ReportResult::getEnterprise,
                Collectors.summingLong(ReportResult::getPriceRefuse)));

        reportResults.clear();
        for(Map.Entry entry : sumTotal.entrySet()){
            Integer complete = 0,refuse = 0;
            long totalPrice = 0,completePrice = 0,refusePrise = 0;

            if(sumComplete.containsKey(entry.getKey())){
                complete = sumComplete.get(entry.getKey()).intValue();
            }
            if(sumRefuse.containsKey(entry.getKey())){
                refuse = sumRefuse.get(entry.getKey()).intValue();
            }
            if(sumTotalPrice.containsKey(entry.getKey())){
                totalPrice = sumTotalPrice.get(entry.getKey()).longValue();
            }
            if(sumCompletePrice.containsKey(entry.getKey())){
                completePrice = sumCompletePrice.get(entry.getKey()).longValue();
            }
            if(sumRefusePrice.containsKey(entry.getKey())){
                refusePrise = sumRefusePrice.get(entry.getKey()).longValue();
            }

            ReportResult reportResult = new ReportResult((String) entry.getKey(),-1,
                    (Integer) entry.getValue(),
                    complete,
                    refuse,
                    totalPrice,
                    completePrice,
                    refusePrise,
                    "0.00%");
            reportResults.add(reportResult);

        }
        reportResults.add(caculateTotal(reportResults,"总计"));
        return reportResults;
    }

    @Override
    public List<ReportResult> parentEnterpriseReport(Integer headOffice, boolean subCompany, Integer parentCompany, Integer contractPromoter, Integer contractType, Date startTime, Date endTime) {

        List<ReportResult> reportResults = new LinkedList<>();
        try{
            //查询 指定人
            if(null != contractPromoter && contractPromoter > 0){
                OAEnterprise enterprise = oaEnterpriseService.getEnterpriseById(parentCompany);
                if(contractType != null && contractType >0){
                    // 归档数量
                    ReportItem reportItemList1 = contractCirculationService.groupUserReport(startTime,endTime,"template","completed",contractPromoter,contractType,null);
                    // 退回数量
                    ReportItem reportItemList2 = contractCirculationService.groupUserReport(startTime,endTime,"template",null,contractPromoter,contractType,1);
                    //发起数量
                    ReportItem reportItemList3 = contractCirculationService.groupUserReport(startTime,endTime,"template",null,contractPromoter,contractType,null);
                    ReportResult reportResult = new ReportResult(enterprise.getCompanyName(),enterprise.getEnterpriseId(),reportItemList3,reportItemList1,reportItemList2,"");
                    reportResults.add(reportResult);

                }else{

                    // 归档数量
                    ReportItem reportItemList1 = contractCirculationService.groupUserReport(startTime,endTime,"template","completed",contractPromoter,contractType,null);
                    // 退回数量
                    ReportItem reportItemList2 = contractCirculationService.groupUserReport(startTime,endTime,"template",null,contractPromoter,contractType,1);
                    //发起数量
                    ReportItem reportItemList3 = contractCirculationService.groupUserReport(startTime,endTime,"template",null,contractPromoter,contractType,null);
                    ReportResult reportResult = new ReportResult(enterprise.getCompanyName(),enterprise.getEnterpriseId(),reportItemList3,reportItemList1,reportItemList2,"");
                    reportResults.add(reportResult);
                }
            }else{
                if(null != parentCompany && parentCompany >0){
                    Map<Integer,OAEnterprise> map = new LinkedHashMap<>();
                    Map<Integer,OAEnterprise> mapSub = new LinkedHashMap<>();
                    OAEnterprise oaEnterprise = oaEnterpriseService.getEnterpriseById(parentCompany);
                    map.put(oaEnterprise.getEnterpriseId(),oaEnterprise);
                    mapSub.put(oaEnterprise.getEnterpriseId(),oaEnterprise);
                    List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByLevel(headOffice);
                    for(OAEnterprise oaEnterprise1 : oaEnterpriseList){
                        map.put(oaEnterprise1.getEnterpriseId(),oaEnterprise1);
                        if(subCompany){
                            oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(oaEnterprise1.getEnterpriseId());
                            for(OAEnterprise oaEnterprise2 : oaEnterpriseList){
                                map.put(oaEnterprise2.getEnterpriseId(),oaEnterprise2);
                                if(parentCompany == oaEnterprise2.getCompanyParent()){
                                    mapSub.put(oaEnterprise2.getEnterpriseId(),oaEnterprise2);
                                }
                            }
                        }
                    }
                    // 归档数量
                    List<ReportItem> reportItemList1 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"template","completed",null,contractType,null);
                    // 退回数量
                    List<ReportItem> reportItemList2 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"template",null,null,contractType,1);
                    //发起数量
                    List<ReportItem> reportItemList3 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"template",null,null,contractType,null);

                    //计算公司报告
                    ReportResult reportResultEnter = new ReportResult(oaEnterprise.getCompanyName(),oaEnterprise.getEnterpriseId(),0,0,0,0L,0L,0L,"0.00%");
                    for(ReportItem ri:reportItemList3 ){
                        if(!map.containsKey(ri.getId())) continue;
                        if(mapSub.containsKey(ri.getId())){
                            reportResultEnter.setTotal(ri.getY()+reportResultEnter.getTotal());
                            reportResultEnter.setPriceTotal(ri.getZ()+reportResultEnter.getPriceTotal());
                        }
                        ReportResult reportResult = new ReportResult();
                        reportResults.add(reportResult);
                        reportResult.setEnterprise(map.get(ri.getId()).getCompanyName());
                        reportResult.setEnterpriseId(ri.getId());
                        reportResult.setTotal(ri.getY());
                        reportResult.setPriceTotal(ri.getZ());
                        for(ReportItem reportItem:reportItemList1) {
                            if(reportItem.getId() == ri.getId()) {
                                if(mapSub.containsKey(ri.getId())) {
                                    reportResultEnter.setComplete(reportItem.getY()+reportResultEnter.getComplete());
                                    reportResultEnter.setPriceComplete(reportItem.getZ()+reportResultEnter.getPriceComplete());
                                }
                                reportResult.setComplete(reportItem.getY());
                                reportResult.setPriceComplete(reportItem.getZ());
                            }
                        }
                        for(ReportItem reportItem:reportItemList2) {
                            if(reportItem.getId() == ri.getId()) {
                                if(mapSub.containsKey(ri.getId())) {
                                    reportResultEnter.setRefuse(reportItem.getY()+reportResultEnter.getRefuse());
                                    reportResultEnter.setPriceRefuse(reportItem.getZ()+reportResultEnter.getPriceRefuse());
                                }
                                reportResult.setRefuse(reportItem.getY());
                                reportResult.setPriceRefuse(reportItem.getZ());
                            }
                        }
                    }


                    List<ReportResult> reportResults1 = new LinkedList<>();
                    reportResults1.add(reportResultEnter);

                    Integer total =0,refuse=0,complete = 0;
                    Long totalPrice =0L,refusePrice =0L,completePrice= 0L;

                    Integer totalOther =0,refuseOther =0,completeOther  = 0;
                    Long totalOtherPrice =0L,refuseOtherPrice =0L,completeOtherPrice  = 0L;
                    for(ReportResult reportResult:reportResults){
                        if(reportResult.getTotal() != null)
                            total+=reportResult.getTotal();
                        if(reportResult.getRefuse() != null)
                            refuse+=reportResult.getRefuse();
                        if(reportResult.getComplete() != null)
                            complete+=reportResult.getComplete();
                        totalPrice+= reportResult.getPriceTotal();
                        refusePrice+= reportResult.getPriceRefuse();
                        completePrice+=reportResult.getPriceComplete();

                        if(!mapSub.containsKey(reportResult.getEnterpriseId())){
                            if(reportResult.getTotal() != null)
                                totalOther+=reportResult.getTotal();
                            if(reportResult.getRefuse() != null)
                                refuseOther+=reportResult.getRefuse();
                            if(reportResult.getComplete() != null)
                                completeOther+=reportResult.getComplete();
                            totalOtherPrice+=reportResult.getPriceTotal();
                            refuseOtherPrice+=reportResult.getPriceRefuse();
                            completeOtherPrice+=reportResult.getPriceComplete();
                        }
                    }

                    ReportResult reportResult1 = new ReportResult("其他",-1,totalOther,completeOther,refuseOther,totalOtherPrice,completeOtherPrice,refuseOtherPrice,"");
                    reportResults1.add(reportResult1);
                    if(headOffice == 1) {
                        reportResult1.setEnterprise("其他部门合计");
                    }else if(headOffice == 2){
                        reportResult1.setEnterprise("其他二级公司合计");
                    }else if(headOffice == 3){
                        reportResult1.setEnterprise("其他三级公司合计");
                    }else{
                        reportResult1.setEnterprise("其他");
                    }
                    ReportResult reportResult2 = new ReportResult("总计",-1,total,complete,refuse,totalPrice,completePrice,refusePrice,"");
                    reportResults1.add(reportResult2);
                    reportResults = reportResults1;

                }else {
                    //查询 所有公司
//                    List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseList("总公司",0,500);
                    List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByLevel(headOffice);
                    Map<Integer,OAEnterprise> map = listEnterprise(oaEnterpriseList,subCompany);
                    Map<Integer,ReportItem> mapCompleted = new LinkedHashMap<>();
                    // 归档数量
                    List<ReportItem> reportItemList1 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"template","completed",null,contractType,null);
                    for(ReportItem reportItem:reportItemList1){
                        if(map.containsKey(reportItem.getId())){
                            mapCompleted.put(reportItem.getId(),reportItem);
                        }
                    }
                    // 退回数量
                    Map<Integer,ReportItem> mapRefused = new LinkedHashMap<>();
                    List<ReportItem> reportItemList2 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"template",null,null,contractType,1);
                    for(ReportItem reportItem:reportItemList2){
                        if(map.containsKey(reportItem.getId())){
                            mapRefused.put(reportItem.getId(),reportItem);
                        }
                    }
                    //发起数量
                    List<ReportItem> reportItemList3 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"template",null,null,contractType,null);
                    for(ReportItem ri:reportItemList3 ){
                        if(!map.containsKey(ri.getId())) continue;
                        ReportResult reportResult = new ReportResult();
                        reportResults.add(reportResult);
                        reportResult.setEnterpriseId(ri.getId());
                        reportResult.setEnterprise(map.get(ri.getId()).getCompanyName());
                        reportResult.setTotal(ri.getY());
                        reportResult.setPriceTotal(ri.getZ());
                        if(mapCompleted.containsKey(ri.getId())) {
                            reportResult.setComplete(mapCompleted.get(ri.getId()).getY());
                            reportResult.setPriceComplete(mapCompleted.get(ri.getId()).getZ());
                        }
                        if(mapRefused.containsKey(ri.getId())) {
                            reportResult.setRefuse(mapRefused.get(ri.getId()).getY());
                            reportResult.setPriceRefuse(mapRefused.get(ri.getId()).getZ());
                        }
                    }

                    reportResults = typeByCompanyLevel(map,reportResults,headOffice,subCompany);
                }
            }
            reportResults = caculateRate(reportResults);


        }catch (Exception e){
            LOGGER.error("参数异常",e);
            return null;
        }
        return reportResults;
    }

    @Override
    public List<ReportResult> fieldReport(Integer headOffice, boolean subCompany, Integer parentCompany, Integer contractPromoter, Integer contractType,String templateType,
                                          String field, String condition, Date startTime, Date endTime) {
        List<ReportResult> reportResults = new LinkedList<>();
        if(StringUtils.isBlank(templateType)){
            templateType = "template";
        }
        try{
            //查询 所有公司
            List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByLevel(headOffice);
            if(null == contractPromoter || contractPromoter < 1) {
                Map<Integer, OAEnterprise> map = null;
                if(null == parentCompany || parentCompany < 1) {
                    map = listEnterprise(oaEnterpriseList, true);
                }else{
                    if(subCompany) {
                        oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(parentCompany);
                    }else{
                        OAEnterprise oa = oaEnterpriseService.getEnterpriseById(parentCompany);
                        oaEnterpriseList.clear();
                        oaEnterpriseList.add(oa);
                    }
                    map = listEnterprise(oaEnterpriseList, subCompany);
                }
                // 归档数量
                List<ReportItem> reportItemList1 = contractCirculationService.groupFieldEnterpriseReport(startTime, endTime,templateType, field, condition, "completed", null, contractType, null);
                List<ReportItem> reportItemList2 = contractCirculationService.groupFieldEnterpriseReport(startTime, endTime, templateType,field, condition, null, null, contractType, 1);
                List<ReportItem> reportItemList3 = contractCirculationService.groupFieldEnterpriseReport(startTime, endTime,templateType, field, condition, null, null, contractType, null);

                //总计
                ReportResult reportResultTotal = new ReportResult("总计",-1,0,0,0,0L,0L,0L,"");
                Map<Integer, ReportItem> mapCompleted = new LinkedHashMap<>();
                for (ReportItem reportItem : reportItemList1) {
                    reportResultTotal.setComplete(reportResultTotal.getComplete() + reportItem.getY());
                    reportResultTotal.setPriceComplete(reportResultTotal.getPriceComplete()+reportItem.getZ());
                    if (map.containsKey(reportItem.getId())) {
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapCompleted.put(reportItem.getId(), reportItem);
                    }
                }
                // 退回数量
                Map<Integer, ReportItem> mapRefused = new LinkedHashMap<>();

                for (ReportItem reportItem : reportItemList2) {
                    reportResultTotal.setRefuse(reportResultTotal.getRefuse() + reportItem.getY());
                    reportResultTotal.setPriceRefuse(reportResultTotal.getPriceRefuse() + reportItem.getZ());
                    if (map.containsKey(reportItem.getId())) {
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapRefused.put(reportItem.getId(), reportItem);
                    }
                }
                //发起数量
                Map<Integer, ReportItem> mapTotal = new LinkedHashMap<>();

                for (ReportItem reportItem : reportItemList3) {
                    reportResultTotal.setTotal(reportResultTotal.getTotal() + reportItem.getY());
                    reportResultTotal.setPriceTotal(reportResultTotal.getPriceTotal() + reportItem.getZ());
                    if (map.containsKey(reportItem.getId())) {
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapTotal.put(reportItem.getId(), reportItem);
                    }
                }
                for (ReportItem ri : mapTotal.values()) {
                    ReportResult reportResult = new ReportResult();
                    reportResults.add(reportResult);
                    reportResult.setTotal(ri.getY());
                    reportResult.setPriceTotal(ri.getZ());
                    reportResult.setEnterpriseId(ri.getId());
                    reportResult.setEnterprise(ri.getName());
                    if (mapCompleted.containsKey(ri.getId())) {
                        reportResult.setComplete(mapCompleted.get(ri.getId()).getY());
                    }
                    if (mapRefused.containsKey(ri.getId())) {
                        reportResult.setRefuse(mapRefused.get(ri.getId()).getY());
                    }
                }


                if(null == parentCompany || parentCompany < 1) {
                    reportResults = typeByCompanyLevel(map,reportResults,headOffice,true);
                }else {
                    OAEnterprise oaEnterprise = oaEnterpriseService.getEnterpriseById(parentCompany);
                    //当前
                    ReportResult reportResultCur = caculateTotal(reportResults,oaEnterprise.getCompanyName());

                    //其他
                    ReportResult reportResultOther = new ReportResult("其他",-1,
                            reportResultTotal.getTotal()-reportResultCur.getTotal(),
                            reportResultTotal.getComplete()-reportResultCur.getComplete(),
                            reportResultTotal.getRefuse()-reportResultCur.getRefuse(),
                            reportResultTotal.getPriceTotal()-reportResultCur.getPriceTotal(),
                            reportResultTotal.getPriceComplete()-reportResultCur.getPriceComplete(),
                            reportResultTotal.getPriceRefuse()-reportResultCur.getPriceRefuse(),
                            "");
                    reportResults.clear();
                    reportResults.add(reportResultCur);
                    reportResults.add(reportResultOther);
                    reportResults.add(reportResultTotal);
                }
            }else{
                Map<Integer,OAEnterprise> map = listEnterprise(oaEnterpriseList,subCompany);
                Map<Integer, ReportItem> mapCompleted = new LinkedHashMap<>();
                // 归档数量
                List<ReportItem> reportItemList1 = contractCirculationService.groupFieldUserReport(startTime, endTime, templateType,field, condition, "completed", null, contractType, null);
                for (ReportItem reportItem : reportItemList1) {
                    if (map.containsKey(reportItem.getId())) {
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapCompleted.put(reportItem.getId(), reportItem);
                    }
                }

                // 退回数量
                Map<Integer, ReportItem> mapRefused = new LinkedHashMap<>();
                List<ReportItem> reportItemList2 = contractCirculationService.groupFieldUserReport(startTime, endTime,templateType, field, condition, null, null, contractType, 1);
                for (ReportItem reportItem : reportItemList2) {
                    if (map.containsKey(reportItem.getId())) {
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapRefused.put(reportItem.getId(), reportItem);
                    }
                }
                //发起数量
                Map<Integer, ReportItem> mapTotal = new LinkedHashMap<>();
                List<ReportItem> reportItemList3 = contractCirculationService.groupFieldUserReport(startTime, endTime,templateType, field, condition, null, null, contractType, null);
                for (ReportItem reportItem : reportItemList3) {
                    if (map.containsKey(reportItem.getId())) {
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapTotal.put(reportItem.getId(), reportItem);
                    }
                }

                for (ReportItem ri : mapTotal.values()) {
                    ReportResult reportResult = new ReportResult();
                    reportResults.add(reportResult);
                    reportResult.setEnterprise(map.get(ri.getId()).getCompanyName());
                    reportResult.setTotal(ri.getY());
                    if (mapCompleted.containsKey(ri.getId())) {
                        reportResult.setComplete(mapCompleted.get(ri.getId()).getY());
                    }
                    if (mapRefused.containsKey(ri.getId())) {
                        reportResult.setRefuse(mapRefused.get(ri.getId()).getY());
                    }
                }
            }
            reportResults = caculateRate(reportResults);

        }catch (Exception e){
            LOGGER.error("异常",e);
            return null;
        }

        return reportResults;
    }

    @Override
    public List<ReportResult> nonFormatReport(Integer headOffice, boolean subCompany, Integer parentCompany, Integer contractPromoter, Integer contractType, Date startTime, Date endTime) {
        List<ReportResult> reportResults = new LinkedList<>();
        try{
            if(parentCompany == null || parentCompany < 1 ){
                List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByLevel(headOffice);
                Map<Integer,OAEnterprise> map = listEnterprise(oaEnterpriseList,subCompany);
                Map<Integer,ReportItem> mapCompleted = new LinkedHashMap<>();
                // 归档数量
                List<ReportItem> reportItemList1 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"custom","completed",null,contractType,null);
                for(ReportItem reportItem:reportItemList1){
                    if(map.containsKey(reportItem.getId())){
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapCompleted.put(reportItem.getId(),reportItem);
                    }
                }

                // 退回数量
                Map<Integer,ReportItem> mapRefused = new LinkedHashMap<>();
                List<ReportItem> reportItemList2 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"custom",null,null,contractType,1);
                for(ReportItem reportItem:reportItemList2){
                    if(map.containsKey(reportItem.getId())){
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapRefused.put(reportItem.getId(),reportItem);
                    }
                }
                //发起数量
                Map<Integer,ReportItem> mapTotal = new LinkedHashMap<>();
                List<ReportItem> reportItemList3 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"custom",null,null,contractType,null);
                for(ReportItem reportItem:reportItemList3){
                    if(map.containsKey(reportItem.getId())){
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapTotal.put(reportItem.getId(),reportItem);
                    }
                }
                for(ReportItem ri:mapTotal.values() ){
                    ReportResult reportResult = new ReportResult();
                    reportResults.add(reportResult);
                    reportResult.setEnterprise(map.get(ri.getId()).getCompanyName());
                    reportResult.setEnterpriseId(ri.getId());
                    reportResult.setTotal(ri.getY());
                    reportResult.setPriceTotal(ri.getZ());
                    if(mapCompleted.containsKey(ri.getId())) {
                        reportResult.setComplete(mapCompleted.get(ri.getId()).getY());
                        reportResult.setPriceComplete(mapCompleted.get(ri.getId()).getZ());
                    }
                    if(mapRefused.containsKey(ri.getId())) {
                        reportResult.setRefuse(mapRefused.get(ri.getId()).getY());
                        reportResult.setPriceRefuse(mapRefused.get(ri.getId()).getZ());
                    }
                }
                reportResults = typeByCompanyLevel(map,reportResults,headOffice,subCompany);
            }else{
                if(null == contractPromoter || contractPromoter < 1){
                    //查询 所有公司
                    List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(parentCompany);
                    Map<Integer,OAEnterprise> map = listEnterprise(oaEnterpriseList,subCompany);
                    Map<Integer,ReportItem> mapCompleted = new LinkedHashMap<>();
                    OAEnterprise oaEnterprise = oaEnterpriseService.getEnterpriseById(parentCompany);
                    ReportResult reportResultTotal = new ReportResult("总计",-1,0,0,0,0L,0L,0L,"");
                    // 归档数量
                    List<ReportItem> reportItemList1 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"custom","completed",null,contractType,null);
                    for(ReportItem reportItem:reportItemList1){
                        reportResultTotal.setComplete(reportItem.getY()+reportResultTotal.getComplete());
                        reportResultTotal.setPriceComplete(reportItem.getZ()+reportResultTotal.getPriceComplete());
                        if(map.containsKey(reportItem.getId())){
                            reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                            mapCompleted.put(reportItem.getId(),reportItem);
                        }
                    }

                    // 退回数量
                    Map<Integer,ReportItem> mapRefused = new LinkedHashMap<>();
                    List<ReportItem> reportItemList2 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"custom",null,null,contractType,1);
                    for(ReportItem reportItem:reportItemList2){
                        reportResultTotal.setRefuse(reportItem.getY()+reportResultTotal.getRefuse());
                        reportResultTotal.setPriceRefuse(reportItem.getZ()+reportResultTotal.getPriceRefuse());
                        if(map.containsKey(reportItem.getId())){
                            reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                            mapRefused.put(reportItem.getId(),reportItem);
                        }
                    }
                    //发起数量
                    Map<Integer,ReportItem> mapTotal = new LinkedHashMap<>();
                    List<ReportItem> reportItemList3 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"custom",null,null,contractType,null);
                    for(ReportItem reportItem:reportItemList3){
                        reportResultTotal.setTotal(reportItem.getY()+reportResultTotal.getTotal());
                        reportResultTotal.setPriceTotal(reportItem.getZ()+reportResultTotal.getPriceTotal());
                        if(map.containsKey(reportItem.getId())){
                            reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                            mapTotal.put(reportItem.getId(),reportItem);
                        }
                    }
                    for(ReportItem ri:mapTotal.values() ){
                        ReportResult reportResult = new ReportResult();
                        reportResults.add(reportResult);
                        reportResult.setEnterprise(map.get(ri.getId()).getCompanyName());
                        reportResult.setTotal(ri.getY());
                        reportResult.setPriceTotal(ri.getZ());
                        if(mapCompleted.containsKey(ri.getId())) {
                            reportResult.setComplete(mapCompleted.get(ri.getId()).getY());
                            reportResult.setPriceComplete(mapCompleted.get(ri.getId()).getZ());
                        }
                        if(mapRefused.containsKey(ri.getId())) {
                            reportResult.setRefuse(mapRefused.get(ri.getId()).getY());
                            reportResult.setPriceRefuse(mapRefused.get(ri.getId()).getZ());
                        }
                    }
                    ReportResult reportResult = caculateTotal(reportResults,oaEnterprise.getCompanyName());
                    reportResults.clear();
                    reportResults.add(reportResult);
                    ReportResult reportResultOther = new ReportResult("其他公司",-1,reportResultTotal.getTotal()-reportResult.getTotal(),
                            reportResultTotal.getComplete()-reportResult.getComplete(),reportResultTotal.getRefuse()-reportResult.getRefuse(),
                            reportResultTotal.getPriceTotal()-reportResult.getPriceTotal(),
                            reportResultTotal.getPriceComplete()-reportResult.getPriceComplete(),
                            reportResultTotal.getPriceRefuse()-reportResult.getPriceRefuse(),
                            "");
                    reportResults.add(reportResultOther);

                    reportResults.add(reportResultTotal);
                }else{
                    OAUser oaUser = userService.getUserById(contractPromoter);
                    OAEnterprise enterprise = oaEnterpriseService.getEnterpriseById(oaUser.getEnterpriseId());
                    // 归档数量
                    ReportItem reportItemList1 = contractCirculationService.groupUserReport(startTime,endTime,"custom","completed",contractPromoter,null,null);
                    // 退回数量
                    ReportItem reportItemList2 = contractCirculationService.groupUserReport(startTime,endTime,"custom",null,contractPromoter,null,1);
                    //发起数量
                    ReportItem reportItemList3 = contractCirculationService.groupUserReport(startTime,endTime,"custom",null,contractPromoter,null,null);
                    ReportResult reportResult = new ReportResult(enterprise.getCompanyName(),enterprise.getEnterpriseId(),reportItemList3,reportItemList1,reportItemList2,"");
                    reportResults.add(reportResult);
                    reportResults = caculateRate(reportResults);

                }
            }
            reportResults = caculateRate(reportResults);
        }catch (Exception e){
            LOGGER.error("异常",e);
            return null;
        }
        return reportResults;
    }

    @Override
    public List<ReportResult> locationReport(String location, String province, String city, Integer contractType, Date startTime, Date endTime) {
        List<ReportResult> reportResults = new LinkedList<>();
        try{
            //total
            ReportResult reportResultTotal = getTotal(startTime,endTime,contractType);
            List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.listEnterprise(location,province,city);
            Map<Integer,OAEnterprise> map = listEnterprise(oaEnterpriseList,false);

            if(StringUtils.isBlank(location)){
                reportResults = getReportList(startTime,endTime,contractType);
                reportResults = typeBylocation(map,reportResults);
            }else{
                String name = location;
                if(StringUtils.isNotBlank(province)){
                    name+="-";
                    name+=province;
                }
                if(StringUtils.isNotBlank(city)){
                    name+="-";
                    name+=city;
                }
                ReportResult reportResultCur = getCurrent(map,name,startTime,endTime,contractType);
                reportResults.add(reportResultCur);
                ReportResult reportResultOther = new ReportResult("其他总和",-1,
                        reportResultTotal.getTotal() - reportResultCur.getTotal(),
                        reportResultTotal.getComplete() - reportResultCur.getComplete(),
                        reportResultTotal.getRefuse() - reportResultCur.getRefuse(),
                        reportResultTotal.getPriceTotal() - reportResultCur.getPriceTotal(),
                        reportResultTotal.getPriceComplete() - reportResultCur.getPriceComplete(),
                        reportResultTotal.getPriceRefuse() - reportResultCur.getPriceRefuse(),
                        "0.00%");

                reportResults.add(reportResultOther);
                reportResults.add(reportResultTotal);
            }
            reportResults = caculateRate(reportResults);

        }catch (Exception e){
            LOGGER.error("异常",e);
            return null;
        }
        return reportResults;
    }
}
