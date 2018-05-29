package com.wxm.controller;

import com.wxm.mapper.OAAuditMapper;
import com.wxm.model.OAAudit;
import com.wxm.model.OAUser;
import com.wxm.service.AuditService;
import com.wxm.service.UserService;
import com.wxm.util.exception.OAException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditController.class);
    @Autowired
    private AuditService auditService;
    @Autowired
    private UserService userService;
    @RequestMapping(value = "/auditList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object userList(HttpServletRequest request,
                           @RequestParam(value = "offset", required = true) int offset,
                           @RequestParam(value = "limit", required = true) int limit,
                           @RequestParam(value = "userName", required = false) String userName,
                           @RequestParam(value = "startTime", required = true) String startTime,
                           @RequestParam(value = "endTime", required = true) String endTime
                           )throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }else{
            OAUser oaUser = userService.getUserById(loginUser.getId());
            if(oaUser != null && oaUser.getParentId() != loginUser.getParentId()){
                LOGGER.error("用户在其他地址登录");
                throw new OAException(1101,"用户在其他地址登录");
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","failed");
        LOGGER.info("查询审计日志，参数：{}，{}，{}，{}，{}",offset,limit,userName,startTime,endTime);
        try {
            if (StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
                return auditService.getAudits(offset, limit, userName, null, null);
            } else {
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return auditService.getAudits(offset, limit, userName, time.parse(startTime), time.parse(endTime));
            }
        }catch (Exception e){
            LOGGER.error("异常",e);
        }
        return result;

    }
}
