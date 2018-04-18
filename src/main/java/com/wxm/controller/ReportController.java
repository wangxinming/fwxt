package com.wxm.controller;

import com.wxm.model.OAAudit;
import com.wxm.util.exception.OAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("report/")
public class ReportController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);

    @RequestMapping(value = "/data",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object data(HttpServletRequest request)throws Exception{

        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        String offset = request.getParameter("offset");
        String limit = request.getParameter("limit");
        //历史提交数量

        //审批数量

        //


        Map<String, Object> result = new HashMap<>();
        result.put("rows",null);
        result.put("total",0);
        return result;
    }
}
