package com.wxm.controller;

import com.wxm.entity.ReportEntity;
import com.wxm.model.OAAudit;
import com.wxm.util.exception.OAException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("report/")
public class ReportController {

    @Autowired
    private HistoryService historyService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);

    @RequestMapping(value = "/data",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
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
                        .involvedUser(loginUser.getName())
                        .count());
                //个人总提交任务数量

                result.put("result","success");
                result.put("data",reportEntity);
            }catch (Exception e){
                LOGGER.error("参数异常",e);
            }
        }
        return result;
    }
}
