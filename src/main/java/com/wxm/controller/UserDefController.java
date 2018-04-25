package com.wxm.controller;

import com.wxm.entity.*;
import com.wxm.model.OAAudit;
import com.wxm.model.OAUser;
import com.wxm.service.AuditService;
import com.wxm.service.UserService;
import com.wxm.util.Md5Utils;
import com.wxm.util.exception.OAException;
import org.activiti.bpmn.converter.EndEventXMLConverter;
import org.activiti.engine.*;
import org.activiti.engine.identity.Group;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping(value = "/user")
public class UserDefController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDefController.class);
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private FormService formService;
    @Autowired
    private UserService userService;

    @Autowired
    private AuditService auditService;

    @RequestMapping(value = "/loginMenus",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object loginMenus(HttpServletRequest request) throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            throw new OAException(1101, "用户未登录");
        }
        List<Menu> list = new LinkedList<>();
        if(loginUser.getName().equals("admin")){
            Menu menu = new Menu("user","用户界面",true);
            list.add(menu);
            menu = new Menu("audit","审计界面",true);
            list.add(menu);
            menu = new Menu("modeler","流程设计界面",true);
            list.add(menu);
            menu = new Menu("upload","合同模板界面",true);
            list.add(menu);
            menu = new Menu("form","表单项界面",true);
            list.add(menu);
            menu = new Menu("deployment","部署界面",true);
            list.add(menu);
            menu = new Menu("process","流程申请",false);
            list.add(menu);
            menu = new Menu("myProcess","我的申请",false);
            list.add(menu);
            menu = new Menu("pending","待办任务",false);
            list.add(menu);
            menu = new Menu("complete","已办任务",false);
            list.add(menu);
            menu = new Menu("report","报表",true);
            list.add(menu);


        }else{
            Menu menu = new Menu("user","用户界面",false);
            list.add(menu);
            menu = new Menu("audit","审计界面",true);
            list.add(menu);
            menu = new Menu("modeler","流程设计界面",false);
            list.add(menu);
            menu = new Menu("upload","合同模板界面",false);
            list.add(menu);
            menu = new Menu("form","表单项界面",false);
            list.add(menu);
            menu = new Menu("deployment","部署界面",false);
            list.add(menu);
            menu = new Menu("process","流程申请",true);
            list.add(menu);
            menu = new Menu("myProcess","我的申请",true);
            list.add(menu);
            menu = new Menu("pending","待办任务",true);
            list.add(menu);
            menu = new Menu("complete","已办任务",true);
            list.add(menu);
            menu = new Menu("report","报表",true);
            list.add(menu);
        }
        return list;

    }
    @RequestMapping(value = "/processList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object processList(HttpServletRequest request,
                              @RequestParam(value = "offset", required = true) int offset,
                              @RequestParam(value = "limit", required = true) int limit
                              ) throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
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
                .listPage(offset,limit);//返回一个集合列表，封装流程定义
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
    @RequestMapping(value = "/create",method = {RequestMethod.PUT},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object createUser(@RequestBody OAUser oaUser, HttpServletRequest request)throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        auditService.audit(new OAAudit(loginUser.getName(),String.format("新建用户 %s",oaUser.getUserName())));
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            oaUser.setUserPwd(Md5Utils.getMd5(oaUser.getUserPwd()));
            userService.create(oaUser);
        }catch (Exception e){
            LOGGER.warn("异常",e);
            result.put("result", "failed");
        }
        return result;
    }

    //修改密码
    @RequestMapping(value = "/updatePassword",method = {RequestMethod.POST},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object updatePassword(HttpServletRequest request,@RequestBody Map map)throws  Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");

        String oldPassword = map.get("userPwd").toString();
        String newPassword = map.get("userPwdNew").toString();
        String userId = map.get("userId").toString();

//        auditService.audit(new OAAudit(loginUser.getName(),String.format("修改密码 %s",oaUser.getUserName())));
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("result", "failed");
            OAUser oaUser = userService.getUserById(Integer.parseInt(userId));
            auditService.audit(new OAAudit(loginUser.getName(),String.format("修改密码 %s",oaUser.getUserName())));
            if(oaUser.getUserPwd().equals(Md5Utils.getMd5(oldPassword))){
                result.put("result", "success");
                oaUser.setUserPwd(Md5Utils.getMd5(newPassword));
                userService.update(oaUser);
            }
        }catch (Exception e){
            LOGGER.warn("异常",e);
            result.put("result", "failed");
        }
        return result;
    }
    //更新用户信息
    @RequestMapping(value = "/update",method = {RequestMethod.POST},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object updateUser(@RequestBody OAUser oaUser,HttpServletRequest request)throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        auditService.audit(new OAAudit(loginUser.getName(),String.format("更新用户信息 %s",oaUser.getUserName())));
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            userService.update(oaUser);
        }catch (Exception e){
            LOGGER.warn("异常",e);
            result.put("result", "failed");
        }
        return result;
    }

    //通过USR_ID获取USER信息
    @RequestMapping(value = "/userInfo",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object getUserInfo(@RequestParam(value = "userId", required=true) Integer userId,
                              HttpServletRequest request,
                              HttpServletResponse response
                                )throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        return userService.getUserById(userId);
    }
    @RequestMapping(value = "/userList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object userList(HttpServletRequest request
                           )throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            throw new OAException(1101, "用户未登录");
        }
        List<com.wxm.entity.User> list = new LinkedList<>();
        String offset = request.getParameter("offset");
        String limit = request.getParameter("limit");
        String userName = request.getParameter("userName");
        if(StringUtils.isBlank(userName)){
            userName = null;
        }
        return userService.getUserList(Integer.parseInt(offset),Integer.parseInt(limit),userName);
    }
//    /**
//     * 启动流程
//     */
//
//    @RequestMapping(value = "start", method = RequestMethod.POST)
//    @ResponseBody
//    public AjaxRes startWorkflow(Userdef o, HttpServletRequest request) throws Exception{
//
//        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
//        if(null == loginUser) throw new OAException(1101,"用户未登录");
//
////        auditService.audit(new OAAudit(loginUser.getName(),String.format("启动流程 %s",oaUser.getUserName())));
//        AjaxRes ar =  new AjaxRes();
//        try{
//            Map<String, String> formProperties = new HashMap<String, String>();
//
//            // 从request中读取参数然后转换
//            Map<String, String[]> parameterMap = request.getParameterMap();
//            Set<Map.Entry<String, String[]>> entrySet = parameterMap.entrySet();
//            for (Map.Entry<String, String[]> entry : entrySet) {
//                String key = entry.getKey();
//
//                // fp_的意思是form paremeter
//                if (StringUtils.defaultString(key).startsWith("fp_")) {
//                    formProperties.put(key.split("_")[1], entry.getValue()[0]);
//                }
//            }
//            String key = "new-process";
//            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key);
//            String pId = processInstance.getId();
//            System.out.println("流程梳理所属流程定义id："
//                    + processInstance.getProcessDefinitionId());
//            System.out.println("流程实例的id：" + processInstance.getProcessInstanceId());
//            System.out.println("流程实例的执行id：" + processInstance.getId());
//            System.out.println("流程当前的活动（结点）id：" + processInstance.getActivityId());
//            System.out.println("业务标识：" + processInstance.getBusinessKey());
//        }catch (Exception e){
//
//        }
//        return ar;
//    }
}
