package com.wxm.controller;

import com.wxm.mapper.OAAuditMapper;
import com.wxm.model.OAAudit;
import com.wxm.service.AuditService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("audit/")
public class AuditController {
    @Autowired
    private AuditService auditService;

    @RequestMapping(value = "/auditList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object userList(HttpServletRequest request,
                           @RequestParam(value = "offset", required = false) int offset,
                           @RequestParam(value = "limit", required = false) int limit,
                           @RequestParam(value = "userName", required = false) String userName,
                           @RequestParam(value = "startTime", required = true) String startTime,
                           @RequestParam(value = "endTime", required = true) String endTime
                           )throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
        Date start=null,end=null;
        if(StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)){
            start = null;
            end = null;
        }else{
            try {
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                start = time.parse(startTime);
                end = time.parse(endTime);
            }catch (Exception e){
                start = null;
                end = null;
            }
        }
        Map<String, Object> result = new HashMap<>();
        if(StringUtils.isBlank(userName)){
            userName = null;
        }
        result.put("rows",auditService.list(offset,limit,userName,start,end));
        result.put("total",auditService.count(userName,start,end));
        return result;
    }
}
