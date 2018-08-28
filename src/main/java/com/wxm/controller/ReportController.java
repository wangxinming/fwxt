package com.wxm.controller;

import com.wxm.common.ExportExcelUtil;
import com.wxm.entity.ReportEntity;
import com.wxm.entity.ReportItem;
import com.wxm.entity.ReportResult;
import com.wxm.model.OAAudit;
import com.wxm.model.OAContractTemplate;
import com.wxm.model.OAEnterprise;
import com.wxm.model.OAUser;
import com.wxm.service.*;
import com.wxm.util.exception.OAException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("report/")
public class ReportController {

    @Autowired
    private HistoryService historyService;
    @Autowired
    private ContractCirculationService contractCirculationService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ConcactTemplateService concactTemplateService;
    @Autowired
    private OAEnterpriseService oaEnterpriseService;
    @Autowired
    private UserService userService;
    @Autowired
    private ReportService reportService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);

    //获取公司部门列表，合同类别，
    @RequestMapping(value = "/parentEnterpriseList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object parentEnterpriseList(HttpServletRequest request,
                       @RequestParam(value = "level", required = true) Integer level)throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{
            List<OAContractTemplate> templateList = concactTemplateService.listTemplate();
            result.put("templates",templateList);

            List<OAEnterprise> enterpriseList = oaEnterpriseService.getEnterpriseByLevel(level);
//            List<OAEnterprise> enterpriseList = oaEnterpriseService.getEnterpriseList("盾构工程分公司",0,100);
            result.put("enterprises",enterpriseList);

        }catch (Exception e){
            result.put("result","failed");
            LOGGER.error("参数异常",e);
        }
        return result;
    }

    @RequestMapping(value = "/getContractPromoter",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object getContractPromoter(HttpServletRequest request,
                                       @RequestParam(value = "company", required = false) Integer company,
                                      @RequestParam(value = "subCompany", required = false) boolean subCompany)throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        if(company == null ||  company < 1) {
            result.put("result","failed");
            return result;
        }
        try{
            List<OAUser> oaUserList = userService.listUserByCompany(company,null);
            if(subCompany){
                List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(company);
                for(OAEnterprise oaEnterprise : oaEnterpriseList){
                    oaUserList.addAll(userService.listUserByCompany(oaEnterprise.getEnterpriseId(),null));
                }
            }
            result.put("users",oaUserList);

        }catch (Exception e){
            result.put("result","failed");
            LOGGER.error("参数异常",e);
        }
        return result;
    }
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), true));
    }

    //获取区域信息
    @RequestMapping(value = "/locationList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object locationList(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{
            List<OAEnterprise> enterpriseList = oaEnterpriseService.getEnterpriseByLoction();
            result.put("locations",enterpriseList);
            List<OAContractTemplate> templateList = concactTemplateService.listTemplate();
            result.put("templates",templateList);

        }catch (Exception e){
            result.put("result","failed");
            LOGGER.error("参数异常",e);
        }
        return result;
    }
    //获取省区
    @RequestMapping(value = "/provinceList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object provinceList(HttpServletRequest request,
                               @RequestParam(value = "location", required = false) String location)throws Exception {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{
            List<OAEnterprise> enterpriseList = oaEnterpriseService.getEnterpriseByProvince(location);
            result.put("provinces",enterpriseList);
        }catch (Exception e){
            result.put("result","failed");
            LOGGER.error("参数异常",e);
        }
        return result;
    }
    //获取市区
    @RequestMapping(value = "/cityList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object cityList(HttpServletRequest request,
                           @RequestParam(value = "location", required = false) String location,
                           @RequestParam(value = "province", required = false) String province)throws Exception {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{
            List<OAEnterprise> enterpriseList = oaEnterpriseService.getEnterpriseByCity(location,province);
            result.put("cities",enterpriseList);
        }catch (Exception e){
            result.put("result","failed");
            LOGGER.error("参数异常",e);
        }
        return result;
    }
    //excel导出
    @RequestMapping(value = "/export",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object parentEnterpriseReport(HttpServletRequest request,
                                         @RequestParam(value = "headOffice", required = true) Integer headOffice,
                                         @RequestParam(value = "subCompany", required = false) boolean subCompany,
                                         @RequestParam(value = "parentCompany", required = false) Integer parentCompany,
                                         @RequestParam(value = "contractPromoter", required = false) Integer contractPromoter,
                                         @RequestParam(value = "contractType", required = false) Integer contractType,
                                         @RequestParam(value = "startTime", required = true) Date startTime,
                                         @RequestParam(value = "endTime", required = true) Date endTime,
                                         HttpServletResponse response)throws Exception {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }

        //查询 所有公司
        List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByLevel(headOffice);
        Map<Integer,OAEnterprise> map = new LinkedHashMap<>();
        for(OAEnterprise oaEnterprise : oaEnterpriseList){
            map.put(oaEnterprise.getEnterpriseId(),oaEnterprise);
            if(true){
                oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(oaEnterprise.getEnterpriseId());
                for(OAEnterprise oaEnterprise1 : oaEnterpriseList){
                    map.put(oaEnterprise1.getEnterpriseId(),oaEnterprise1);
                }
            }
        }

        Map<Integer,ReportItem> mapCompleted = new LinkedHashMap<>();
        // 归档数量
        List<ReportItem> reportItemList1 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"template","completed",null,contractType,null);
        for(ReportItem reportItem:reportItemList1){
            if(map.containsKey(reportItem.getId())){
                reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                mapCompleted.put(reportItem.getId(),reportItem);
            }
        }

        // 退回数量
        Map<Integer,ReportItem> mapRefused = new LinkedHashMap<>();
        List<ReportItem> reportItemList2 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"template",null,null,contractType,1);
        for(ReportItem reportItem:reportItemList2){
            if(map.containsKey(reportItem.getId())){
                reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                mapRefused.put(reportItem.getId(),reportItem);
            }
        }
        //发起数量
        Map<Integer,ReportItem> mapTotal = new LinkedHashMap<>();
        List<ReportItem> reportItemList3 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"template",null,null,contractType,null);
        for(ReportItem reportItem:reportItemList3){
            if(map.containsKey(reportItem.getId())){
                reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                mapTotal.put(reportItem.getId(),reportItem);
            }
        }

        List<ReportResult> reportResults = new LinkedList<>();
        for(ReportItem ri:mapTotal.values() ){
            ReportResult reportResult = new ReportResult();
            reportResults.add(reportResult);
            reportResult.setEnterprise(map.get(ri.getId()).getCompanyName());
            reportResult.setTotal(ri.getY());
            if(mapCompleted.containsKey(ri.getId())) {
                reportResult.setComplete(mapCompleted.get(ri.getId()).getY());
            }else{
                reportResult.setComplete(0);
            }
            if(mapRefused.containsKey(ri.getId())) {
                reportResult.setRefuse(mapRefused.get(ri.getId()).getY());
            }else{
                reportResult.setRefuse(0);
            }
            if(reportResult.getTotal() > 0 && reportResult.getComplete() != null){
                NumberFormat numberFormat = NumberFormat.getInstance();
                // 设置精确到小数点后2位
                numberFormat.setMaximumFractionDigits(2);
                reportResult.setRate(numberFormat.format((float)reportResult.getComplete()/(float)reportResult.getTotal()*100)+"%");
            }else{
                reportResult.setRate("0.00%");
            }
        }

        Integer total = 0,complete = 0,refuse = 0;
        for(ReportResult reportResult:reportResults){
            if(null != reportResult.getTotal())
                total += reportResult.getTotal();
            if(null != reportResult.getComplete())
                complete += reportResult.getComplete();
            if(null != reportResult.getRefuse())
                refuse += reportResult.getRefuse();

        }
        ReportResult reportResult = new ReportResult();
        reportResult.setEnterprise("总计");
        reportResult.setTotal(total);
        reportResult.setComplete(complete);
        reportResult.setRefuse(refuse);
        if(reportResult.getTotal() > 0 && reportResult.getComplete() != null){
            NumberFormat numberFormat = NumberFormat.getInstance();
            // 设置精确到小数点后2位
            numberFormat.setMaximumFractionDigits(2);
            reportResult.setRate(numberFormat.format((float)reportResult.getComplete()/(float)reportResult.getTotal()*100)+"%");
        }else{
            reportResult.setRate("0.00%");
        }
        reportResults.add(reportResult);

        ExportExcelUtil util = new ExportExcelUtil();// 创建工具类.

        HSSFWorkbook wb = util.exportExcel("报表",reportResults);

        response.setContentType("application/vnd.ms-excel");
        String filename = java.net.URLEncoder.encode("报表.xls", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);
//        response.setContentLength(bytes.length);
        wb.write(response.getOutputStream());
        response.getOutputStream().flush();
        response.getOutputStream().close();
//        util.exportExcel(userVOList, "用户信息", 65536, out);// 导出
        return null;
    }
    //特殊字段统计报表
    @RequestMapping(value = "/fieldReport",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object fieldReport(HttpServletRequest request,
                              @RequestParam(value = "headOffice", required = false) Integer headOffice,
                              @RequestParam(value = "subCompany", required = false) boolean subCompany,
                              @RequestParam(value = "parentCompany", required = false) Integer parentCompany,
                              @RequestParam(value = "contractPromoter", required = false) Integer contractPromoter,
                              @RequestParam(value = "contractType", required = false) Integer contractType,
                              @RequestParam(value = "field", required = false) String field,
                              @RequestParam(value = "condition", required = false) String condition,
                              @RequestParam(value = "startTime", required = true) Date startTime,
                              @RequestParam(value = "endTime", required = true) Date endTime)throws Exception {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try{
            //查询 所有公司
            List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByLevel(headOffice);
            if(null == contractPromoter || contractPromoter < 1) {
                List<ReportResult> reportResults = new LinkedList<>();
                Map<Integer, OAEnterprise> map = null;
                if(null == parentCompany || parentCompany < 1) {
                    map = reportService.listEnterprise(oaEnterpriseList, true);
                }else{
                    if(subCompany) {
                        oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(parentCompany);
                    }else{
                        OAEnterprise oa = oaEnterpriseService.getEnterpriseById(parentCompany);
                        oaEnterpriseList.clear();
                        oaEnterpriseList.add(oa);
                    }
                    map = reportService.listEnterprise(oaEnterpriseList, subCompany);
                }
                // 归档数量
                List<ReportItem> reportItemList1 = contractCirculationService.groupFieldEnterpriseReport(startTime, endTime,"template", field, condition, "completed", null, contractType, null);
                List<ReportItem> reportItemList2 = contractCirculationService.groupFieldEnterpriseReport(startTime, endTime, "template",field, condition, null, null, contractType, 1);
                List<ReportItem> reportItemList3 = contractCirculationService.groupFieldEnterpriseReport(startTime, endTime,"template", field, condition, null, null, contractType, null);

                //总计
                ReportResult reportResultTotal = new ReportResult("总计",-1,0,0,0,"");
                Map<Integer, ReportItem> mapCompleted = new LinkedHashMap<>();
                for (ReportItem reportItem : reportItemList1) {
                    reportResultTotal.setComplete(reportResultTotal.getComplete() + reportItem.getY());
                    if (map.containsKey(reportItem.getId())) {
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapCompleted.put(reportItem.getId(), reportItem);
                    }
                }
                // 退回数量
                Map<Integer, ReportItem> mapRefused = new LinkedHashMap<>();

                for (ReportItem reportItem : reportItemList2) {
                    reportResultTotal.setRefuse(reportResultTotal.getRefuse() + reportItem.getY());
                    if (map.containsKey(reportItem.getId())) {
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapRefused.put(reportItem.getId(), reportItem);
                    }
                }
                //发起数量
                Map<Integer, ReportItem> mapTotal = new LinkedHashMap<>();

                for (ReportItem reportItem : reportItemList3) {
                    reportResultTotal.setTotal(reportResultTotal.getTotal() + reportItem.getY());
                    if (map.containsKey(reportItem.getId())) {
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapTotal.put(reportItem.getId(), reportItem);
                    }
                }
                for (ReportItem ri : mapTotal.values()) {
                    ReportResult reportResult = new ReportResult();
                    reportResults.add(reportResult);
                    reportResult.setTotal(ri.getY());
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
                    reportResults = reportService.typeByCompanyLevel(map,reportResults,headOffice,true);
                }else {
                    OAEnterprise oaEnterprise = oaEnterpriseService.getEnterpriseById(parentCompany);
                    //当前
                    ReportResult reportResultCur = reportService.caculateTotal(reportResults,oaEnterprise.getCompanyName());

                    //其他
                    ReportResult reportResultOther = new ReportResult("其他",-1,
                            reportResultTotal.getTotal()-reportResultCur.getTotal(),reportResultTotal.getComplete()-reportResultCur.getComplete(),reportResultTotal.getRefuse()-reportResultCur.getRefuse(),"");
                    reportResults.clear();
                    reportResults.add(reportResultCur);
                    reportResults.add(reportResultOther);
                    reportResults.add(reportResultTotal);

                }
                reportResults = reportService.caculateRate(reportResults);

                result.put("rows",reportResults);
                result.put("total",reportResults.size());
            }else{
                Map<Integer,OAEnterprise> map = reportService.listEnterprise(oaEnterpriseList,subCompany);
                Map<Integer, ReportItem> mapCompleted = new LinkedHashMap<>();
                // 归档数量
                List<ReportItem> reportItemList1 = contractCirculationService.groupFieldUserReport(startTime, endTime, "template",field, condition, "completed", null, contractType, null);
                for (ReportItem reportItem : reportItemList1) {
                    if (map.containsKey(reportItem.getId())) {
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapCompleted.put(reportItem.getId(), reportItem);
                    }
                }

                // 退回数量
                Map<Integer, ReportItem> mapRefused = new LinkedHashMap<>();
                List<ReportItem> reportItemList2 = contractCirculationService.groupFieldUserReport(startTime, endTime,"template", field, condition, null, null, contractType, 1);
                for (ReportItem reportItem : reportItemList2) {
                    if (map.containsKey(reportItem.getId())) {
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapRefused.put(reportItem.getId(), reportItem);
                    }
                }
                //发起数量
                Map<Integer, ReportItem> mapTotal = new LinkedHashMap<>();
                List<ReportItem> reportItemList3 = contractCirculationService.groupFieldUserReport(startTime, endTime,"template", field, condition, null, null, contractType, null);
                for (ReportItem reportItem : reportItemList3) {
                    if (map.containsKey(reportItem.getId())) {
                        reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                        mapTotal.put(reportItem.getId(), reportItem);
                    }
                }

                List<ReportResult> reportResults = new LinkedList<>();
                for (ReportItem ri : mapTotal.values()) {
                    ReportResult reportResult = new ReportResult();
                    reportResults.add(reportResult);
                    reportResult.setEnterprise(map.get(ri.getId()).getCompanyName());
                    reportResult.setTotal(ri.getY());
                    if (mapCompleted.containsKey(ri.getId())) {
                        reportResult.setComplete(mapCompleted.get(ri.getId()).getY());
                    } else {
                        reportResult.setComplete(0);
                    }
                    if (mapRefused.containsKey(ri.getId())) {
                        reportResult.setRefuse(mapRefused.get(ri.getId()).getY());
                    } else {
                        reportResult.setRefuse(0);
                    }
                    if (reportResult.getTotal() > 0 && reportResult.getComplete() != null) {
                        NumberFormat numberFormat = NumberFormat.getInstance();
                        // 设置精确到小数点后2位
                        numberFormat.setMaximumFractionDigits(2);
                        reportResult.setRate(numberFormat.format((float) reportResult.getComplete() / (float) reportResult.getTotal() * 100) + "%");
                    } else {
                        reportResult.setRate("0.00%");
                    }
                }
                result.put("rows",reportResults);
                result.put("total",reportResults.size());
            }


//            List<HistoricVariableInstance> historicVariableInstanceList = historyService
//                    .createHistoricVariableInstanceQuery()
//                    .variableValueLikeIgnoreCase(field,condition)
//                    .list();


        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result", "failed");
        }
        return result;
    }

    //非格式合同统计报表
    @RequestMapping(value = "/nonFormatReport",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object nonFormatReport(HttpServletRequest request,
                                    @RequestParam(value = "headOffice", required = true) Integer headOffice,
                                    @RequestParam(value = "subCompany", required = false) boolean subCompany,
                                    @RequestParam(value = "parentCompany", required = false) Integer parentCompany,
                                    @RequestParam(value = "contractPromoter", required = false) Integer contractPromoter,
                                    @RequestParam(value = "contractType", required = false) Integer contractType,
                                    @RequestParam(value = "startTime", required = true) Date startTime,
                                 @RequestParam(value = "endTime", required = true) Date endTime)throws Exception {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try{
            //查询 所有公司
//                    List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseList("总公司",0,500);
            List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByLevel(headOffice);
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

            List<ReportResult> reportResults = new LinkedList<>();
            for(ReportItem ri:mapTotal.values() ){
                ReportResult reportResult = new ReportResult();
                reportResults.add(reportResult);
                reportResult.setEnterprise(map.get(ri.getId()).getCompanyName());
                reportResult.setTotal(ri.getY());
                if(mapCompleted.containsKey(ri.getId())) {
                    reportResult.setComplete(mapCompleted.get(ri.getId()).getY());
                }else{
                    reportResult.setComplete(0);
                }
                if(mapRefused.containsKey(ri.getId())) {
                    reportResult.setRefuse(mapRefused.get(ri.getId()).getY());
                }else{
                    reportResult.setRefuse(0);
                }
                if(reportResult.getTotal() > 0 && reportResult.getComplete() != null){
                    NumberFormat numberFormat = NumberFormat.getInstance();
                    // 设置精确到小数点后2位
                    numberFormat.setMaximumFractionDigits(2);
                    reportResult.setRate(numberFormat.format((float)reportResult.getComplete()/(float)reportResult.getTotal()*100)+"%");
                }else{
                    reportResult.setRate("0.00%");
                }
            }

            Integer total = 0,complete = 0,refuse = 0;
            for(ReportResult reportResult:reportResults){
                if(null != reportResult.getTotal())
                    total += reportResult.getTotal();
                if(null != reportResult.getComplete())
                    complete += reportResult.getComplete();
                if(null != reportResult.getRefuse())
                    refuse += reportResult.getRefuse();

            }
            ReportResult reportResult = new ReportResult();
            reportResult.setEnterprise("总计");
            reportResult.setTotal(total);
            reportResult.setComplete(complete);
            reportResult.setRefuse(refuse);
            if(reportResult.getTotal() > 0 && reportResult.getComplete() != null){
                NumberFormat numberFormat = NumberFormat.getInstance();
                // 设置精确到小数点后2位
                numberFormat.setMaximumFractionDigits(2);
                reportResult.setRate(numberFormat.format((float)reportResult.getComplete()/(float)reportResult.getTotal()*100)+"%");
            }else{
                reportResult.setRate("0.00%");
            }
            reportResults.add(reportResult);
            result.put("rows",reportResults);
            result.put("total",reportResults.size());
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result", "failed");
        }
        return result;
    }


    //导出区域统计报表
    @RequestMapping(value = "/fieldReportExcel",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object fieldReportExcel(HttpServletRequest request,HttpServletResponse response,
                                 @RequestParam(value = "startTime", required = true) Date startTime,
                                 @RequestParam(value = "endTime", required = true) Date endTime){
        List<ReportResult> reportResults = new LinkedList<>();
        try {
            List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByLevel(2);
            Map<Integer,OAEnterprise> map = reportService.listEnterprise(oaEnterpriseList,true);
            Map<Integer, ReportItem> mapCompleted = new LinkedHashMap<>();
            // 归档数量
            List<ReportItem> reportItemList1 = contractCirculationService.groupFieldUserReport(startTime, endTime, "template",null, null, "completed", null, null, null);
            for (ReportItem reportItem : reportItemList1) {
                if (map.containsKey(reportItem.getId())) {
                    reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                    mapCompleted.put(reportItem.getId(), reportItem);
                }
            }
            // 退回数量
            Map<Integer, ReportItem> mapRefused = new LinkedHashMap<>();
            List<ReportItem> reportItemList2 = contractCirculationService.groupFieldUserReport(startTime, endTime,"template", null, null, null, null, null, 1);
            for (ReportItem reportItem : reportItemList2) {
                if (map.containsKey(reportItem.getId())) {
                    reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                    mapRefused.put(reportItem.getId(), reportItem);
                }
            }
            //发起数量
            Map<Integer, ReportItem> mapTotal = new LinkedHashMap<>();
            List<ReportItem> reportItemList3 = contractCirculationService.groupFieldUserReport(startTime, endTime,"template", null, null, null, null, null, null);
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
                } else {
                    reportResult.setComplete(0);
                }
                if (mapRefused.containsKey(ri.getId())) {
                    reportResult.setRefuse(mapRefused.get(ri.getId()).getY());
                } else {
                    reportResult.setRefuse(0);
                }
            }
            reportResults = reportService.caculateRate(reportResults);

            ExportExcelUtil util = new ExportExcelUtil();// 创建工具类.
            HSSFWorkbook wb = util.exportExcel("报表", reportResults);
            response.setContentType("application/vnd.ms-excel");
            String filename = java.net.URLEncoder.encode("区域统计报表.xls", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            wb.write(response.getOutputStream());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }catch (Exception e){
            LOGGER.error("异常",e);
        }
        return null;
    }
    //导出区域统计报表
    @RequestMapping(value = "/locationReportExcel",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object locationReport(HttpServletRequest request,HttpServletResponse response,
                                 @RequestParam(value = "startTime", required = true) Date startTime,
                                 @RequestParam(value = "endTime", required = true) Date endTime){
        List<ReportResult> reportResults = new LinkedList<>();
        try {
            List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.listEnterprise(null, null, null);
            Map<Integer, OAEnterprise> map = reportService.listEnterprise(oaEnterpriseList,false);
            reportResults = reportService.getReportList(startTime, endTime, null);
            reportResults = reportService.typeBylocation(map, reportResults);

            ExportExcelUtil util = new ExportExcelUtil();// 创建工具类.
            HSSFWorkbook wb = util.exportExcel("报表", reportResults);
            response.setContentType("application/vnd.ms-excel");
            String filename = java.net.URLEncoder.encode("区域统计报表.xls", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            wb.write(response.getOutputStream());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }catch (Exception e){
            LOGGER.error("异常",e);
        }
        return null;
    }
    //区域统计报表
    @RequestMapping(value = "/locationReport",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object locationReport(HttpServletRequest request,

                                         @RequestParam(value = "location", required = false) String location,
                                         @RequestParam(value = "province", required = false) String province,
                                         @RequestParam(value = "city", required = false) String city,
                                         @RequestParam(value = "contractType", required = false) Integer contractType,
                                         @RequestParam(value = "startTime", required = true) Date startTime,
                                         @RequestParam(value = "endTime", required = true) Date endTime)throws Exception {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try{
            //total
            ReportResult reportResultTotal = reportService.getTotal(startTime,endTime,contractType);
            List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.listEnterprise(location,province,city);
            Map<Integer,OAEnterprise> map = reportService.listEnterprise(oaEnterpriseList,false);
            List<ReportResult> reportResults = new LinkedList<>();
            if(StringUtils.isBlank(location)){
                reportResults = reportService.getReportList(startTime,endTime,contractType);
                reportResults = reportService.typeBylocation(map,reportResults);
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
                ReportResult reportResultCur = reportService.getCurrent(map,name,startTime,endTime,contractType);
                reportResults.add(reportResultCur);
                ReportResult reportResultOther = new ReportResult("其他总和",-1,
                        reportResultTotal.getTotal() - reportResultCur.getTotal(),
                        reportResultTotal.getComplete() - reportResultCur.getComplete(),
                        reportResultTotal.getRefuse() - reportResultCur.getRefuse(),"0.00%");
                reportResults.add(reportResultOther);
                reportResults.add(reportResultTotal);
            }

            reportResults = reportService.caculateRate(reportResults);
            result.put("rows",reportResults);
            result.put("total",reportResults.size());
        }catch (Exception e){
            LOGGER.error("异常",e);
        }
        return result;
    }


    //报表统计,统计发起合同数量、被打回合同数量、存档合同数量、存档/发起比例
    @RequestMapping(value = "/parentEnterpriseReport",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object parentEnterpriseReport(HttpServletRequest request,
                                         @RequestParam(value = "headOffice", required = true) Integer headOffice,
                                         @RequestParam(value = "subCompany", required = false) boolean subCompany,
                                         @RequestParam(value = "parentCompany", required = false) Integer parentCompany,
                                         @RequestParam(value = "contractPromoter", required = false) Integer contractPromoter,
                                         @RequestParam(value = "contractType", required = false) Integer contractType,
                                         @RequestParam(value = "startTime", required = true) Date startTime,
                                         @RequestParam(value = "endTime", required = true) Date endTime)throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{
            //查询 指定人
            if(null != contractPromoter && contractPromoter > 0){
                OAEnterprise enterprise = oaEnterpriseService.getEnterpriseById(parentCompany);
                if(contractType != null && contractType >0){
                    // 归档数量
                    Integer reportItemList1 = contractCirculationService.groupUserReport(startTime,endTime,"template","completed",contractPromoter,contractType,null);
                    // 退回数量
                    Integer reportItemList2 = contractCirculationService.groupUserReport(startTime,endTime,"template",null,contractPromoter,contractType,1);
                    //发起数量
                    Integer reportItemList3 = contractCirculationService.groupUserReport(startTime,endTime,"template",null,contractPromoter,contractType,null);

                    ReportResult reportResult = new ReportResult(enterprise.getCompanyName(),enterprise.getEnterpriseId(),reportItemList1,reportItemList2,reportItemList3,"");
                    List<ReportResult> reportResults = new LinkedList<>();
                    reportResults.add(reportResult);
                    reportResult.setEnterprise(enterprise.getCompanyName());
                    reportResult.setComplete(reportItemList1);
                    reportResult.setTotal(reportItemList3);
                    reportResult.setRefuse(reportItemList2);

                    reportResults = reportService.caculateRate(reportResults);
                    result.put("rows",reportResults);
                    result.put("total",reportResults.size());

                }else{

                    // 归档数量
                    Integer reportItemList1 = contractCirculationService.groupUserReport(startTime,endTime,"template","completed",contractPromoter,contractType,null);
                    // 退回数量
                    Integer reportItemList2 = contractCirculationService.groupUserReport(startTime,endTime,"template",null,contractPromoter,contractType,1);
                    //发起数量
                    Integer reportItemList3 = contractCirculationService.groupUserReport(startTime,endTime,"template",null,contractPromoter,contractType,null);
                    ReportResult reportResult = new ReportResult();
                    List<ReportResult> reportResults = new LinkedList<>();
                    reportResults.add(reportResult);
                    reportResult.setEnterprise(enterprise.getCompanyName());
                    reportResult.setComplete(reportItemList1);
                    reportResult.setTotal(reportItemList3);
                    reportResult.setRefuse(reportItemList2);

                    reportResults = reportService.caculateRate(reportResults);
                    result.put("rows",reportResults);
                    result.put("total",reportResults.size());

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


                    List<ReportResult> reportResults = new LinkedList<>();
                    //计算公司报告
                    ReportResult reportResultEnter = new ReportResult(oaEnterprise.getCompanyName(),oaEnterprise.getEnterpriseId(),0,0,0,"0.00%");
                    for(ReportItem ri:reportItemList3 ){
                        if(!map.containsKey(ri.getId())) continue;
                        if(mapSub.containsKey(ri.getId())) reportResultEnter.setTotal(ri.getY()+reportResultEnter.getTotal());
                        ReportResult reportResult = new ReportResult();
                        reportResults.add(reportResult);
                        reportResult.setEnterprise(map.get(ri.getId()).getCompanyName());
                        reportResult.setEnterpriseId(ri.getId());
                        reportResult.setTotal(ri.getY());
                        for(ReportItem reportItem:reportItemList1) {
                            if(reportItem.getId() == ri.getId()) {
                                if(mapSub.containsKey(ri.getId())) reportResultEnter.setComplete(reportItem.getY()+reportResultEnter.getComplete());
                                reportResult.setComplete(reportItem.getY());
                            }else{
                                reportResult.setComplete(0);
                            }
                        }
                        for(ReportItem reportItem:reportItemList2) {
                            if(reportItem.getId() == ri.getId()) {
                                if(mapSub.containsKey(ri.getId())) reportResultEnter.setRefuse(reportItem.getY()+reportResultEnter.getRefuse());
                                reportResult.setRefuse(reportItem.getY());
                            }else{
                                reportResult.setRefuse(0);
                            }
                        }
                    }


                    List<ReportResult> reportResults1 = new LinkedList<>();
                    reportResults1.add(reportResultEnter);

                    Integer total =0,refuse=0,complete = 0;
                    Integer totalOther =0,refuseOther =0,completeOther  = 0;
                    for(ReportResult reportResult:reportResults){
                        if(reportResult.getTotal() != null)
                            total+=reportResult.getTotal();
                        if(reportResult.getRefuse() != null)
                            refuse+=reportResult.getRefuse();
                        if(reportResult.getComplete() != null)
                            complete+=reportResult.getComplete();

                        if(!mapSub.containsKey(reportResult.getEnterpriseId())){
                            if(reportResult.getTotal() != null)
                                totalOther+=reportResult.getTotal();
                            if(reportResult.getRefuse() != null)
                                refuseOther+=reportResult.getRefuse();
                            if(reportResult.getComplete() != null)
                                completeOther+=reportResult.getComplete();
                        }
                    }

                    ReportResult reportResult1 = new ReportResult("其他",-1,totalOther,refuseOther,completeOther,"");
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
                    ReportResult reportResult2 = new ReportResult("总计",-1,total,refuse,complete,"");
                    reportResults1.add(reportResult2);

                    reportResults1 = reportService.caculateRate(reportResults1);
                    result.put("rows",reportResults1);
                    result.put("total",reportResults1.size());

                }else {
                    //查询 所有公司
//                    List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseList("总公司",0,500);
                    List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByLevel(headOffice);
                    Map<Integer,OAEnterprise> map = reportService.listEnterprise(oaEnterpriseList,subCompany);
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
                    List<ReportResult> reportResults = new LinkedList<>();
                    for(ReportItem ri:reportItemList3 ){
                        if(!map.containsKey(ri.getId())) continue;
                        ReportResult reportResult = new ReportResult();
                        reportResults.add(reportResult);
                        reportResult.setEnterprise(map.get(ri.getId()).getCompanyName());
                        reportResult.setTotal(ri.getY());
                        if(mapCompleted.containsKey(ri.getId())) {
                            reportResult.setComplete(mapCompleted.get(ri.getId()).getY());
                        }
                        if(mapRefused.containsKey(ri.getId())) {
                            reportResult.setRefuse(mapRefused.get(ri.getId()).getY());
                        }
                    }
                    ReportResult reportResult = reportService.caculateTotal(reportResults,"总计");
                    reportResults.add(reportResult);
                    reportResults = reportService.caculateRate(reportResults);
                    result.put("rows",reportResults);
                    result.put("total",reportResults.size());

                }
            }

        }catch (Exception e){
            result.put("result","failed");
            LOGGER.error("参数异常",e);
        }
        return result;
    }

    @RequestMapping(value = "/myReport",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object data(HttpServletRequest request,
                       @RequestParam(value = "startTime", required = true) String startTime,
                       @RequestParam(value = "endTime", required = true) String endTime)throws Exception{

        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        Date start=null,end=null;
        if(StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)){
            start = null;
            end = null;
        }else{
            try {
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                start = time.parse(startTime);
                end = time.parse(endTime);
                ReportEntity reportEntity = new ReportEntity();
                //个人提交任务，并完成了审批数量
                reportEntity.setCompletedCount ( historyService.createHistoricProcessInstanceQuery()
                        .startedAfter(start)
                        .startedBefore(end)
                        .finished()
                        .variableValueEquals("user",loginUser.getName())
                        .count());
                //参与审批任务数量
                reportEntity.setInvolveCount(historyService.createHistoricProcessInstanceQuery()
                        .finished()
                        .startedAfter(start)
                        .startedBefore(end)
                        .involvedUser(loginUser.getName())
                        .count());
                //个人总提交任务数量

                result.put("result","success");
                result.put("data",reportEntity);
            }catch (Exception e){
                result.put("result","failed");
                LOGGER.error("参数异常",e);
            }
        }
        return result;
    }

    @RequestMapping(value = "/fawuReport",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object fawu(HttpServletRequest request,
                       @RequestParam(value = "startTime", required = true) String startTime,
                       @RequestParam(value = "endTime", required = true) String endTime)throws Exception{

        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        Date start=null,end=null;
        if(StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)){
            start = null;
            end = null;
        }else{
            try {
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                start = time.parse(startTime);
                end = time.parse(endTime);
                ReportEntity reportEntity = new ReportEntity();
                LinkedList<ReportItem> reportItems = new LinkedList<>();
                reportEntity.setReportItemList(reportItems);
                //统计已完成合同总数量（模板、自定义）
                reportEntity.setTotal(contractCirculationService.total("completed",null,null,null,start,end));
                //统计已完成合同总数量（自定义）
                reportEntity.setCustomNum(contractCirculationService.total("completed",null,null,"custom",start,end));
                reportEntity.setTemplateNum(reportEntity.getTotal() - reportEntity.getCustomNum());
                reportEntity.setReportItemList(contractCirculationService.count(start,end));

                result.put("result","success");
                result.put("data",reportEntity);
            }catch (Exception e){
                result.put("result","failed");
                LOGGER.error("参数异常",e);
            }
        }
        return result;
    }


    @RequestMapping(value = "/rejectReport",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object rejectReport(HttpServletRequest request,
                       @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
                       @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit,
                       @RequestParam(value = "startTime", required = true) String startTime,
                       @RequestParam(value = "endTime", required = true) String endTime)throws Exception{

        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        Date start=null,end=null;
        if(StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)){
            start = null;
            end = null;
        }else{
            try {
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                start = time.parse(startTime);
                end = time.parse(endTime);
                List<ReportItem> reportItems = contractCirculationService.group(start,end,offset,limit);
                result.put("rows",reportItems);
                result.put("total",contractCirculationService.groupCount(start,end));
            }catch (Exception e){
                result.put("result","failed");
                LOGGER.error("参数异常",e);
            }
        }
        return result;
    }
}
