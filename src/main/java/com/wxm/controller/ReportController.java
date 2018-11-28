package com.wxm.controller;

import com.wxm.common.ExportExcelUtil;
import com.wxm.entity.ReportEntity;
import com.wxm.entity.ReportItem;
import com.wxm.entity.ReportResult;
import com.wxm.model.*;
import com.wxm.service.*;
import com.wxm.util.exception.OAException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
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
    private RuntimeService runtimeService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ContractCirculationService contractCirculationService;
    @Autowired
    private OAAttachmentService oaAttachmentService;
    @Autowired
    private ConcactTemplateService concactTemplateService;
    @Autowired
    private OAEnterpriseService oaEnterpriseService;
    @Autowired
    private UserService userService;
    @Autowired
    private ReportService reportService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);
    @RequestMapping(value = "/deleteTask",method = {RequestMethod.DELETE},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object deleteTask(HttpServletRequest request,
                             @RequestParam(value = "id", required = true) String id){
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{
            OAContractCirculation oaContractCirculation =  contractCirculationService.selectBySerialNumber(id);
            String processInstanceId = oaContractCirculation.getProcessInstanceId();
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if(pi==null){
                //该流程实例已经完成了
                historyService.deleteHistoricProcessInstance(processInstanceId);
            }else{
                //该流程实例未结束的
                runtimeService.deleteProcessInstance(processInstanceId,"");
                historyService.deleteHistoricProcessInstance(processInstanceId);//(顺序不能换)
            }
            oaAttachmentService.deleteByProcessId(processInstanceId);
            contractCirculationService.delete(oaContractCirculation.getContractId());
        }catch (Exception ex){
            result.put("result","failed");
        }
        return result;
    }
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
                                         @RequestParam(value = "loan", required = false) boolean loan,
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
        try {
            List<ReportResult> reportResults = reportService.parentEnterpriseReport(headOffice, subCompany, parentCompany,
                    contractPromoter, contractType, startTime, endTime);
            if (reportResults != null) {

                ExportExcelUtil util = new ExportExcelUtil();// 创建工具类.
                HSSFWorkbook wb = util.exportExcel("报表", reportResults,loan);
                response.setContentType("application/vnd.ms-excel");
                String filename = java.net.URLEncoder.encode("报表.xls", "UTF-8");
                response.setHeader("Content-Disposition", "attachment;filename=" + filename);
                wb.write(response.getOutputStream());
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
        }catch (Exception e){
            LOGGER.error("异常",e);
        }
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
                              @RequestParam(value = "templateType", required = false) String templateType,
                              @RequestParam(value = "field", required = false) String field,
                              @RequestParam(value = "condition", required = false) String condition,
                              @RequestParam(value = "startTime", required = true) Date startTime,
                              @RequestParam(value = "endTime", required = true) Date endTime)throws Exception {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        List<ReportResult> reportResults = reportService.fieldReport(headOffice,subCompany,parentCompany,contractPromoter,contractType,templateType,
                field,condition,startTime,endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        if(reportResults == null){
            result.put("result", "failed");
        }else {
            result.put("rows", reportResults);
            result.put("total", reportResults.size());
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
        List<ReportResult> reportResults = reportService.nonFormatReport(headOffice,subCompany,parentCompany,contractPromoter,
                contractType,startTime,endTime);
        if(null == reportResults){
            result.put("result", "failed");
        }else {
            result.put("rows", reportResults);
            result.put("total", reportResults.size());
        }
        return result;
    }
    //非格式合同统计报表
    @RequestMapping(value = "/nonFormatReportExcel",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object nonFormatReportExcel(HttpServletRequest request,HttpServletResponse response,
                                            @RequestParam(value = "headOffice", required = true) Integer headOffice,
                                            @RequestParam(value = "subCompany", required = false) boolean subCompany,
                                            @RequestParam(value = "parentCompany", required = false) Integer parentCompany,
                                            @RequestParam(value = "loan", required = false) boolean loan,
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

        try{
            List<ReportResult> reportResults = reportService.nonFormatReport(headOffice,subCompany,parentCompany,contractPromoter,
                    contractType,startTime,endTime);

            result.put("result", "success");
            if(reportResults != null){
                ExportExcelUtil util = new ExportExcelUtil();// 创建工具类.
                HSSFWorkbook wb = util.exportExcel("非格式合同", reportResults,loan);
                response.setContentType("application/vnd.ms-excel");
                String filename = java.net.URLEncoder.encode("非格式合同报表.xls", "UTF-8");
                response.setHeader("Content-Disposition", "attachment;filename=" + filename);
                wb.write(response.getOutputStream());
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
        }catch (Exception e){
            LOGGER.error("异常",e);
        }
        return null;
    }

    //导出区域统计报表
    @RequestMapping(value = "/fieldReportExcel",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object fieldReportExcel(HttpServletRequest request,HttpServletResponse response,
                                   @RequestParam(value = "headOffice", required = false) Integer headOffice,
                                   @RequestParam(value = "subCompany", required = false) boolean subCompany,
                                   @RequestParam(value = "parentCompany", required = false) Integer parentCompany,
                                   @RequestParam(value = "loan", required = false) boolean loan,
                                   @RequestParam(value = "contractPromoter", required = false) Integer contractPromoter,
                                   @RequestParam(value = "contractType", required = false) Integer contractType,
                                   @RequestParam(value = "templateType", required = false) String templateType,
                                   @RequestParam(value = "field", required = false) String field,
                                   @RequestParam(value = "condition", required = false) String condition,
                                   @RequestParam(value = "startTime", required = true) Date startTime,
                                   @RequestParam(value = "endTime", required = true) Date endTime){


        List<ReportResult> reportResults = reportService.fieldReport(headOffice,subCompany,parentCompany,contractPromoter,contractType,templateType,
                field,condition,startTime,endTime);
        Map<String, Object> result = new HashMap<>();
        try{
            result.put("result", "success");
            if(reportResults != null){
                ExportExcelUtil util = new ExportExcelUtil();// 创建工具类.
                HSSFWorkbook wb = util.exportExcel("报表", reportResults,loan);
                response.setContentType("application/vnd.ms-excel");
                String filename = java.net.URLEncoder.encode("统计报表.xls", "UTF-8");
                response.setHeader("Content-Disposition", "attachment;filename=" + filename);
                wb.write(response.getOutputStream());
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
        }catch (Exception e){
            LOGGER.error("异常",e);
        }
        return null;
    }
    //导出区域统计报表
    @RequestMapping(value = "/locationReportExcel",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object locationReport(HttpServletRequest request,HttpServletResponse response,
                                 @RequestParam(value = "location", required = false) String location,
                                 @RequestParam(value = "loan", required = false) boolean loan,
                                 @RequestParam(value = "province", required = false) String province,
                                 @RequestParam(value = "city", required = false) String city,
                                 @RequestParam(value = "contractType", required = false) Integer contractType,
                                 @RequestParam(value = "startTime", required = true) Date startTime,
                                 @RequestParam(value = "endTime", required = true) Date endTime){
    try{
        List<ReportResult> reportResults = reportService.locationReport(location,province,city,contractType,startTime,endTime);
        if(null != reportResults){
            ExportExcelUtil util = new ExportExcelUtil();// 创建工具类.
            HSSFWorkbook wb = util.exportExcel("报表", reportResults,loan);
            response.setContentType("application/vnd.ms-excel");
            String filename = java.net.URLEncoder.encode("区域统计报表.xls", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            wb.write(response.getOutputStream());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }

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

        List<ReportResult> reportResults = reportService.locationReport(location,province,city,contractType,startTime,endTime);
        if(null == reportResults){
            result.put("result", "failed");
        }else {
            result.put("rows", reportResults);
            result.put("total", reportResults.size());
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
        List<ReportResult> reportResults = reportService.parentEnterpriseReport(headOffice,subCompany,parentCompany,
                contractPromoter,contractType,startTime,endTime);
        if(reportResults == null){
            result.put("result","failed");
        }else {
            result.put("rows", reportResults);
            result.put("total", reportResults.size());
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
                ReportItem reportItem = contractCirculationService.total("completed",null,null,null,start,end);
                if(null != reportItem) {
                    reportEntity.setTotal(reportItem.getY());
                    reportEntity.setTotalPrice(reportItem.getZ());
                }

                //统计已完成合同总数量（自定义）
                reportItem = contractCirculationService.total("completed",null,null,"custom",start,end);
                if(null != reportItem) {
                    reportEntity.setCustomNum(reportItem.getY());
                }
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
