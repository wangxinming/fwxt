package com.wxm.controller;

import com.wxm.entity.ReportEntity;
import com.wxm.entity.ReportItem;
import com.wxm.entity.ReportResult;
import com.wxm.model.OAAudit;
import com.wxm.model.OAContractTemplate;
import com.wxm.model.OAEnterprise;
import com.wxm.model.OAUser;
import com.wxm.service.ConcactTemplateService;
import com.wxm.service.ContractCirculationService;
import com.wxm.service.OAEnterpriseService;
import com.wxm.service.UserService;
import com.wxm.util.exception.OAException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);

    //获取公司部门列表，合同类别，
    @RequestMapping(value = "/parentEnterpriseList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object parentEnterpriseList(HttpServletRequest request,
                       @RequestParam(value = "startTime", required = false) String startTime,
                       @RequestParam(value = "endTime", required = false) String endTime)throws Exception{
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
            List<OAEnterprise> enterpriseList = oaEnterpriseService.getEnterpriseList("盾构工程分公司",0,100);
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
                                       @RequestParam(value = "company", required = true) String company)throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{
            List<OAUser> oaUserList = userService.listUserByCompany(company,null);
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
    //报表统计,统计发起合同数量、被打回合同数量、存档合同数量、存档/发起比例
    @RequestMapping(value = "/parentEnterpriseReport",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object parentEnterpriseReport(HttpServletRequest request,
                                         @RequestParam(value = "headOffice", required = true) String headOffice,
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
            if(null != contractPromoter && contractPromoter > 0){
//                OAUser oaUser = userService.selectByName(contractPromoter);
                OAEnterprise enterprise = oaEnterpriseService.getEnterpriseById(parentCompany);

                if(contractType != null && contractType >0){
                    // 归档数量
                    Integer reportItemList1 = contractCirculationService.groupUserReport(startTime,endTime,"completed",contractPromoter,contractType,null);
                    // 退回数量
                    Integer reportItemList2 = contractCirculationService.groupUserReport(startTime,endTime,null,contractPromoter,contractType,1);
                    //发起数量
                    Integer reportItemList3 = contractCirculationService.groupUserReport(startTime,endTime,null,contractPromoter,contractType,null);
                    ReportResult reportResult = new ReportResult();
                    List<ReportResult> reportResults = new LinkedList<>();
                    reportResults.add(reportResult);
                    reportResult.setEnterprise(enterprise.getCompanyName());
                    reportResult.setComplete(reportItemList1);
                    reportResult.setTotal(reportItemList3);
                    reportResult.setRefuse(reportItemList2);
                    if(reportItemList3 < 1){
                        reportResult.setRate("0.00%");
                    }else {
                        NumberFormat numberFormat = NumberFormat.getInstance();
                        // 设置精确到小数点后2位
                        numberFormat.setMaximumFractionDigits(2);
                        reportResult.setRate(numberFormat.format((float)reportItemList1/(float)reportItemList3*100));
                    }
                    result.put("rows",reportResults);
                    result.put("total",reportResults.size());
                    /*
                          -- 归档数量
                    select count(*) from OA_CONTRACT_CIRCULATION where CONTRACT_STATUS='completed'
                                                                                   and USER_ID = oaUser.getUserId() and TEMPLATE_ID=contractType
                    -- 退回数量
                    select count(*) from OA_CONTRACT_CIRCULATION where CONTRACT_REOPEN =1
                                                                                   and USER_ID = oaUser.getUserId() and TEMPLATE_ID=contractType

                    -- 发起数量
                    select count(*) from OA_CONTRACT_CIRCULATION where
                                                                                     USER_ID = oaUser.getUserId() and TEMPLATE_ID=contractType

                        * */
                }else{

                    // 归档数量
                    Integer reportItemList1 = contractCirculationService.groupUserReport(startTime,endTime,"completed",contractPromoter,null,null);
                    // 退回数量
                    Integer reportItemList2 = contractCirculationService.groupUserReport(startTime,endTime,null,contractPromoter,null,1);
                    //发起数量
                    Integer reportItemList3 = contractCirculationService.groupUserReport(startTime,endTime,null,contractPromoter,null,null);
                    ReportResult reportResult = new ReportResult();
                    List<ReportResult> reportResults = new LinkedList<>();
                    reportResults.add(reportResult);
                    reportResult.setEnterprise(enterprise.getCompanyName());
                    reportResult.setComplete(reportItemList1);
                    reportResult.setTotal(reportItemList3);
                    reportResult.setRefuse(reportItemList2);
                    if(reportItemList3 < 1){
                        reportResult.setRate("0.00%");
                    }else {
                        NumberFormat numberFormat = NumberFormat.getInstance();
                        // 设置精确到小数点后2位
                        numberFormat.setMaximumFractionDigits(2);
                        reportResult.setRate(numberFormat.format((float)reportItemList1/(float)reportItemList3*100));
                    }
                    result.put("rows",reportResults);
                    result.put("total",reportResults.size());
                        /*
                          -- 归档数量
                    select count(*) from OA_CONTRACT_CIRCULATION where CONTRACT_STATUS='completed'
                                                                                   and USER_ID = oaUser.getUserId()
                    -- 退回数量
                    select count(*) from OA_CONTRACT_CIRCULATION where CONTRACT_REOPEN =1
                                                                                   and USER_ID = oaUser.getUserId()

                    -- 发起数量
                    select count(*) from OA_CONTRACT_CIRCULATION where
                                                                                    and USER_ID = oaUser.getUserId()

                        * */
                }
            }else{
                if(null != parentCompany && parentCompany >0){
                    OAEnterprise oaEnterprise = oaEnterpriseService.getEnterpriseById(parentCompany);

                    // 归档数量
                    List<ReportItem> reportItemList1 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"completed",parentCompany,null,null);
                    // 退回数量
                    List<ReportItem> reportItemList2 = contractCirculationService.groupEnterpriseReport(startTime,endTime,null,parentCompany,null,1);
                    //发起数量
                    List<ReportItem> reportItemList3 = contractCirculationService.groupEnterpriseReport(startTime,endTime,null,parentCompany,null,null);


                    List<ReportResult> reportResults = new LinkedList<>();
                    for(ReportItem ri:reportItemList3 ){
                        ReportResult reportResult = new ReportResult();
                        reportResults.add(reportResult);
                        reportResult.setEnterprise(oaEnterprise.getCompanyName());
                        reportResult.setTotal(ri.getY());
                        for(ReportItem reportItem:reportItemList1) {
                            if(reportItem.getId() == ri.getId())
                                reportResult.setComplete(reportItem.getY());
                        }
                        for(ReportItem reportItem:reportItemList2) {
                            if(reportItem.getId() == ri.getId())
                                reportResult.setRefuse(reportItem.getY());
                        }

                        if(reportResult.getTotal() > 0 && reportResult.getComplete() != null){
                            NumberFormat numberFormat = NumberFormat.getInstance();
                            // 设置精确到小数点后2位
                            numberFormat.setMaximumFractionDigits(2);
                            reportResult.setRate(numberFormat.format((float)reportResult.getComplete()/(float)reportResult.getTotal()*100));
                        }else{
                            reportResult.setRate("0.00%");
                        }
                    }

                    result.put("rows",reportResults);
                    result.put("total",reportResults.size());
                        /*

                    -- 归档数量
                    select count(*) from OA_CONTRACT_CIRCULATION where CONTRACT_STATUS='completed'
                                                                                   and ENTEERPRISE_ID = parentCompany
                    -- 退回数量
                    select TEMPLATE_ID,count(*) from OA_CONTRACT_CIRCULATION where CONTRACT_REOPEN =1
                                                                                   and ENTEERPRISE_ID = parentCompany group by TEMPLATE_ID
                    -- 发起数量
                    select TEMPLATE_ID,count(*) from OA_CONTRACT_CIRCULATION where
                                                                                    ENTEERPRISE_ID = parentCompany group by TEMPLATE_ID

                        * */
                }else {
//                    List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseList("总公司",0,500);
                    List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.listByName(headOffice);
                    Map<Integer,OAEnterprise> map = new LinkedHashMap<>();
                    for(OAEnterprise oaEnterprise : oaEnterpriseList){
                        map.put(oaEnterprise.getEnterpriseId(),oaEnterprise);
                    }
                    Map<Integer,ReportItem> mapCompleted = new LinkedHashMap<>();
                    // 归档数量
                    List<ReportItem> reportItemList1 = contractCirculationService.groupEnterpriseReport(startTime,endTime,"completed",null,null,null);
                    for(ReportItem reportItem:reportItemList1){
                        if(map.containsKey(reportItem.getId())){
                            reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                            mapCompleted.put(reportItem.getId(),reportItem);
                        }
                    }

                    // 退回数量
                    Map<Integer,ReportItem> mapRefused = new LinkedHashMap<>();
                    List<ReportItem> reportItemList2 = contractCirculationService.groupEnterpriseReport(startTime,endTime,null,null,null,1);
                    for(ReportItem reportItem:reportItemList2){
                        if(map.containsKey(reportItem.getId())){
                            reportItem.setName(map.get(reportItem.getId()).getCompanyName());
                            mapRefused.put(reportItem.getId(),reportItem);
                        }
                    }
                    //发起数量
                    Map<Integer,ReportItem> mapTotal = new LinkedHashMap<>();
                    List<ReportItem> reportItemList3 = contractCirculationService.groupEnterpriseReport(startTime,endTime,null,null,null,null);
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
                        }
                        if(mapRefused.containsKey(ri.getId())) {
                            reportResult.setRefuse(mapRefused.get(ri.getId()).getY());
                        }
                        if(reportResult.getTotal() > 0 && reportResult.getComplete() != null){
                            NumberFormat numberFormat = NumberFormat.getInstance();
                            // 设置精确到小数点后2位
                            numberFormat.setMaximumFractionDigits(2);
                            reportResult.setRate(numberFormat.format((float)reportResult.getComplete()/(float)reportResult.getTotal()*100));
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
                        reportResult.setRate(numberFormat.format((float)reportResult.getComplete()/(float)reportResult.getTotal()*100));
                    }else{
                        reportResult.setRate("0.00%");
                    }
                    reportResults.add(reportResult);
                    result.put("rows",reportResults);
                    result.put("total",reportResults.size());
                    /*
                    -- 归档数量
                    select ENTEERPRISE_ID,count(*) from OA_CONTRACT_CIRCULATION where CONTRACT_STATUS='completed'
                                                                                   and ENTEERPRISE_ID = parentCompany group by ENTEERPRISE_ID
                    -- 退回数量
                    select ENTEERPRISE_ID,count(*) from OA_CONTRACT_CIRCULATION where CONTRACT_REOPEN =1
                                                                                   and ENTEERPRISE_ID = parentCompany group by ENTEERPRISE_ID
                    -- 发起数量
                    select ENTEERPRISE_ID,count(*) from OA_CONTRACT_CIRCULATION where
                                                                                    ENTEERPRISE_ID = parentCompany group by ENTEERPRISE_ID

                        * */
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
                reportEntity.setTotal(contractCirculationService.total("completed",null,start,end));
                //统计已完成合同总数量（自定义）
                reportEntity.setCustomNum(contractCirculationService.total("completed","custom",start,end));
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
