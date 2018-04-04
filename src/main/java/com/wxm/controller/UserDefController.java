package com.wxm.controller;

import com.wxm.entity.AjaxRes;
import com.wxm.entity.Process;
import com.wxm.entity.ProcessDef;
import com.wxm.entity.Userdef;
import org.activiti.engine.*;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping(value = "/user")
public class UserDefController {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    FormService formService;

    @RequestMapping(value = "/processList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object processList(HttpServletRequest request){
        List<ProcessDef> processDefList = new LinkedList<>();
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()//创建一个流程定义查询
                /*指定查询条件,where条件*/
                //.deploymentId(deploymentId)//使用部署对象ID查询
                //.processDefinitionId(processDefinitionId)//使用流程定义ID查询
                //.processDefinitionKey(processDefinitionKey)//使用流程定义的KEY查询
                //.processDefinitionNameLike(processDefinitionNameLike)//使用流程定义的名称模糊查询
                /*排序*/
                .orderByProcessDefinitionVersion().asc()//按照版本的升序排列
                //.orderByProcessDefinitionName().desc()//按照流程定义的名称降序排列
                .list();//返回一个集合列表，封装流程定义


        if (list != null && list.size() > 0) {
            for (ProcessDefinition processDefinition : list) {
                ProcessDef processDef = new ProcessDef();
                processDef.setId(processDefinition.getId());
                processDef.setName(processDefinition.getName());
                processDef.setKey(processDefinition.getKey());
                processDef.setVersion(processDefinition.getVersion());
                processDef.setResourceName(processDefinition.getResourceName());
                processDef.setDiagramResourceName(processDefinition.getDiagramResourceName());
                processDef.setDeploymentId(processDefinition.getDeploymentId());
            }
        }

        long size = repositoryService//与流程定义和部署对象相关的Service
                .createProcessDefinitionQuery().count();
        Map<String, Object> result = new HashMap<>();
        result.put("rows",processDefList);
        result.put("total",size);
        return result;
    }


    public void createGroup(){
        Group group = identityService.newGroup("新用户ID");
        identityService.saveGroup(group);
    }

    public void createUser(){
        User user = identityService.newUser("1");
        identityService.saveUser(user);
    }


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

    /*
    * 查询流程
    * */


}
