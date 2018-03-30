package com.wxm.activiti.controller;

import com.wxm.entity.AjaxRes;
import com.wxm.entity.Userdef;
import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping(value = "/backstage/workflow/online/userdef/")
public class UserDefController {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    FormService formService;
    /**
     * 启动流程
     */

    @RequestMapping(value = "start", method = RequestMethod.POST)
    @ResponseBody
    public AjaxRes startWorkflow(Userdef o, HttpServletRequest request) {
        AjaxRes ar =  new AjaxRes();
        try{
            Map<String, String> formProperties = new HashMap<String, String>();

            // 从request中读取参数然后转换
            Map<String, String[]> parameterMap = request.getParameterMap();
            Set<Map.Entry<String, String[]>> entrySet = parameterMap.entrySet();
            for (Map.Entry<String, String[]> entry : entrySet) {
                String key = entry.getKey();

                // fp_的意思是form paremeter
                if (StringUtils.defaultString(key).startsWith("fp_")) {
                    formProperties.put(key.split("_")[1], entry.getValue()[0]);
                }
            }
            String key = "new-process";
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key);
            String pId = processInstance.getId();
            System.out.println("流程梳理所属流程定义id："
                    + processInstance.getProcessDefinitionId());
            System.out.println("流程实例的id：" + processInstance.getProcessInstanceId());
            System.out.println("流程实例的执行id：" + processInstance.getId());
            System.out.println("流程当前的活动（结点）id：" + processInstance.getActivityId());
            System.out.println("业务标识：" + processInstance.getBusinessKey());
        }catch (Exception e){

        }
        return ar;
    }
}
