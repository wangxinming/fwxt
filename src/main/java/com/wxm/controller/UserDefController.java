package com.wxm.controller;

import com.wxm.entity.*;
import com.wxm.model.OAAudit;
import com.wxm.model.OAOrganization;
import com.wxm.model.OAUser;
import com.wxm.service.AuditService;
import com.wxm.service.UserOrganizitionService;
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
import java.util.stream.Collectors;

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
    private UserOrganizitionService userOrganizitionService;

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

    @RequestMapping(value = "/createGroup",method = {RequestMethod.PUT},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object createGroup(@RequestBody OAOrganization oaOrganization, HttpServletRequest request){
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        auditService.audit(new OAAudit(loginUser.getName(),String.format("新建组织结构 %s",oaOrganization.getOrganizationName())));
//        Group group = identityService.newGroup("新用户ID");
//        identityService.saveGroup(group);
        try {
            userOrganizitionService.save(oaOrganization);
        }catch (Exception e){
            LOGGER.warn("异常",e);
            result.put("result", "failed");
        }
        return result;
    }
    @RequestMapping(value = "/updateGroup",method = {RequestMethod.PUT},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object updateGroup(@RequestBody OAOrganization oaOrganization, HttpServletRequest request){
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        auditService.audit(new OAAudit(loginUser.getName(),String.format("新建组织结构 %s",oaOrganization.getOrganizationName())));
//        Group group = identityService.newGroup("新用户ID");
//        identityService.saveGroup(group);
        try {
            userOrganizitionService.save(oaOrganization);
        }catch (Exception e){
            LOGGER.warn("异常",e);
            result.put("result", "failed");
        }
        return result;
    }

    @RequestMapping(value = "/deleteGroup",method = {RequestMethod.PUT},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object deleteGroup(@RequestBody OAOrganization oaOrganization, HttpServletRequest request){
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        auditService.audit(new OAAudit(loginUser.getName(),String.format("新建组织结构 %s",oaOrganization.getOrganizationName())));
//        Group group = identityService.newGroup("新用户ID");
//        identityService.saveGroup(group);
        try {
            userOrganizitionService.save(oaOrganization);
        }catch (Exception e){
            LOGGER.warn("异常",e);
            result.put("result", "failed");
        }
        return result;
    }

    @RequestMapping(value = "/groupList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object groupList(@RequestParam(value = "offset", required=true) Integer offset,
                            @RequestParam(value = "limit", required=true) Integer limit,
                            HttpServletRequest request){
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        return userOrganizitionService.getGroupList(offset,limit);
    }

    @RequestMapping(value = "/tree",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object tree(HttpServletRequest request){
        List<OAOrganization> list = userOrganizitionService.getOrganizition();

//        List<OAUser> res = new LinkedList<>();
        Map<Integer,OAUser> res = new LinkedHashMap<>();;
        Map<Integer,OAOrganization>  map = new LinkedHashMap<>();
        Map<Integer,OAUser>  tmp = new LinkedHashMap<>();
        List<OAUser> userList = userService.getAllUser();


        for(OAOrganization oaOrganization : list){
            OAUser oaUser = tmp.get(oaOrganization.getUserId());
            List<OAUser> oaUserList = userList.parallelStream().filter(p->p.getParentId() == oaOrganization.getUserId())
                    .peek(p -> p.setUserDepartment(oaOrganization.getOrganizationName()))
                    .collect(Collectors.toList());
            oaUser.setOaUser(oaUserList);

            map.put(oaOrganization.getUserId(),oaOrganization);
            res.put(oaOrganization.getUserId(),tmp.get(oaOrganization.getUserId()));
        }
        Map<Integer,OAUser> res2 = new LinkedHashMap<>();

        for(OAUser oaUser : res.values()){

        }
        List<OAUser> oaUserList = userList.parallelStream().filter(p->p.getParentId() == 0).collect(Collectors.toList());
        for(OAUser oaUser : oaUserList){
            OAOrganization oaOrganization = map.get(oaUser.getUserId());
            oaUser.setUserDepartment(oaOrganization.getOrganizationName());

            res.put(oaOrganization.getUserId(),tmp.get(oaOrganization.getUserId()));
        }

        for(OAUser oaUser : userList){
            tmp.put(oaUser.getUserId(),oaUser);
        }

        return res.values();
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

}
