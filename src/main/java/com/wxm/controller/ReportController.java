package com.wxm.controller;

import com.wxm.entity.ReportEntity;
import com.wxm.entity.ReportItem;
import com.wxm.model.OAAudit;
import com.wxm.service.ContractCirculationService;
import com.wxm.util.exception.OAException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("report/")
public class ReportController {

    @Autowired
    private HistoryService historyService;
    @Autowired
    private ContractCirculationService contractCirculationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);

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
