package com.wxm.controller;

import com.alibaba.druid.support.json.JSONUtils;
import com.wxm.common.ImportExcelUtil;
import com.wxm.common.JsonUtils;
import com.wxm.entity.*;
import com.wxm.mapper.OAGroupMapper;
import com.wxm.mapper.OAPrivilegeMapper;
import com.wxm.model.*;
import com.wxm.service.AuditService;
import com.wxm.service.OAEnterpriseService;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@RestController
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
    private OAGroupMapper oaGroupMapper;
    @Autowired
    private AuditService auditService;
    @Autowired
    private OAEnterpriseService oaEnterpriseService;
    @Autowired
    private OAPrivilegeMapper oaPrivilegeMapper;

    //企业信息查询
    @RequestMapping(value = "/queryCompany",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object getEnterprise(@RequestParam(value = "id", required=true) Integer id,
                                HttpServletRequest request) throws OAException{
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String,Object> res = new LinkedHashMap<>();
        res.put("result","success");
        try{
            res.put("data",oaEnterpriseService.getEnterpriseById(id));
        }catch (Exception e){
            res.put("result","failed");
        }
        return res;
    }
    //企业信息列表查询
    @RequestMapping(value = "/listEnterprise",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object listEnterprise(@RequestParam(value = "enterpriseName", required=false) String enterpriseName,
                            @RequestParam(value = "offset", required=true) Integer offset,
                            @RequestParam(value = "limit", required=true) Integer limit,
                            HttpServletRequest request) throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            throw new OAException(1101, "用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        try {
            List<OAEnterprise> list = oaEnterpriseService.getEnterpriseList(enterpriseName,offset, limit);
            Integer count = oaEnterpriseService.count(enterpriseName);
            result.put("rows", list);
            result.put("total", count);
        }catch (Exception e){

        }
        return result;
    }
    //企业信息创建
    @RequestMapping(value = "/enterprise",method = {RequestMethod.PUT},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object enterprise(@RequestBody OAEnterprise oaEnterprise, HttpServletRequest request) throws OAException {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String,String> res = new LinkedHashMap<>();
        res.put("result","success");
        try{
            oaEnterprise.setCreateTime(new Date());
            oaEnterpriseService.create(oaEnterprise);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("创建公司 公司名称:%s",oaEnterprise.getCompanyName())));
        }catch (Exception e){
            res.put("result","failed");
        }
        return res;
    }
    //企业信息修改
    @RequestMapping(value = "/enterprise",method = {RequestMethod.POST},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object enterpriseUpdate(@RequestBody OAEnterprise oaEnterprise, HttpServletRequest request) throws OAException {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String,String> res = new LinkedHashMap<>();
        res.put("result","success");
        try{
            oaEnterpriseService.update(oaEnterprise);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("修改公司 公司名称:%s",oaEnterprise.getCompanyName())));
        }catch (Exception e){
            res.put("result","failed");
        }
        return res;
    }
    //企业状态激活
    @RequestMapping(value = "/enterpriseStatus",method = {RequestMethod.POST},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object enterpriseActive(@RequestBody OAEnterprise oaEnterprise, HttpServletRequest request) throws OAException {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String,String> res = new LinkedHashMap<>();
        res.put("result","success");
        try{
            OAEnterprise oaEnterprise1 = new OAEnterprise();
            oaEnterprise1.setEnterpriseId(oaEnterprise.getEnterpriseId());
            oaEnterprise1.setCompanyStatus(oaEnterprise.getCompanyStatus());
            oaEnterpriseService.update(oaEnterprise1);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("更新公司状态 id:%s",oaEnterprise1.getEnterpriseId())));
        }catch (Exception e){
            res.put("result","failed");
        }
        return res;
    }
    //企业数据删除
    @RequestMapping(value = "/enterprise",method = {RequestMethod.DELETE},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object enterpriseDelete(@RequestParam (value = "enterpriseId", required=true)Integer enterpriseId, HttpServletRequest request) throws OAException {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String,String> res = new LinkedHashMap<>();
        res.put("result","success");
        try{
            OAEnterprise oaEnterprise1 = new OAEnterprise();
            oaEnterprise1.setEnterpriseId(enterpriseId);
            oaEnterprise1.setCompanyStatus(0);
            oaEnterpriseService.update(oaEnterprise1);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("删除企业 id:%s",enterpriseId)));
        }catch (Exception e){
            res.put("result","failed");
        }
        return res;
    }
    //excel导入
    @RequestMapping(value = "/excel",method = {RequestMethod.POST},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object excel(MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        InputStream in =null;
        List<OAUser> listob = null;
        Map<String, String> result = new HashMap<>();
        try {
            in = file.getInputStream();
            listob = new ImportExcelUtil().getBankListByExcel(in,file.getOriginalFilename());
            in.close();
            //该处可调用service相应方法进行数据保存到数据库中，现只对数据输出
            for (OAUser oaUser : listob) {
                if(null == oaUser || StringUtils.isBlank(oaUser.getUserName())) continue;
                OAUser tmp = userService.selectByName(oaUser.getUserName());
                if(tmp == null) {
                    oaUser.setUserPwd(Md5Utils.getMd5(oaUser.getUserPwd()));
                    userService.create(oaUser);
                }
            }
            result.put("result","success");
        } catch (Exception e) {
            LOGGER.error("异常",e);
            result.put("result","failed");
        }
        return result;
    }

    //创建组
    @RequestMapping(value = "/group",method = {RequestMethod.PUT},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object group(@RequestBody GroupInfo groupInfo, HttpServletRequest request) throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        OAGroup oaGroup = new OAGroup();
        Map<String,String> res = new LinkedHashMap<>();
        try {
            res.put("result", "success");
            Map<String, Object> map = new LinkedHashMap<>();
            //工作流
            map.put("flow",groupInfo.getFlow());
            //任务
            map.put("task", groupInfo.getTask());
            map.put("attachment", groupInfo.getAttachment());

            Map menu = new LinkedHashMap();
            for (OAPrivilege oaPrivilege : groupInfo.getOaPrivileges()) {
                menu.put(oaPrivilege.getPrivilegeId(), oaPrivilege.getName());
            }
            map.put("menu", menu);
            oaGroup.setGroupName(groupInfo.getGroupName());
            oaGroup.setDescribe(groupInfo.getDescription());
            oaGroup.setPrivilegeids(JsonUtils.toJsonString(map));
            oaGroup.setUserId(loginUser.getId());
            oaGroup.setCreateTime(new Date());
            oaGroupMapper.insertSelective(oaGroup);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("新建组 %s",oaGroup.getGroupName())));
        }catch (Exception e){
            LOGGER.error("异常",e);
            res.put("result", "failed");
        }
        return res;

    }
    //更新组 状态
    @RequestMapping(value = "/updateStatus",method = {RequestMethod.POST},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object updateStatus(@RequestBody GroupInfo groupInfo, HttpServletRequest request){
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String,Object> res = new LinkedHashMap<>();
        res.put("result","success");
        OAGroup oaGroup = new OAGroup();
        try{
            oaGroup.setGroupId(groupInfo.getGroupId());
            oaGroup.setStatus(groupInfo.getStatus());
            oaGroupMapper.updateByPrimaryKeySelective(oaGroup);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("更新组状态 %s",oaGroup.getGroupName())));
        }catch (Exception e){
            res.put("result","failed");
        }
        return res;
    }
    //组明细
    @RequestMapping(value = "/getGroupById",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object getGroupById(@RequestParam(value = "groupId", required=true) Integer groupId,
                               HttpServletRequest request) throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try {
            OAGroup oaGroup = oaGroupMapper.selectByPrimaryKey(groupId);
            Map map = JsonUtils.jsonToMap(oaGroup.getPrivilegeids());
            Map mapMenu = (Map) map.get("menu");

            List<OAPrivilege> oaPrivileges = oaPrivilegeMapper.list("menu");
            result.put("groupName", oaGroup.getGroupName());
            result.put("describe", oaGroup.getDescribe());
            for (OAPrivilege oaPrivilege : oaPrivileges) {
                oaPrivilege.setId(oaPrivilege.getPrivilegeId());
                if (mapMenu.containsKey(oaPrivilege.getPrivilegeId().toString())) {
                    oaPrivilege.setChecked(true);
                } else {
                    oaPrivilege.setChecked(false);
                }
            }
            result.put("flow", map.get("flow"));
            result.put("attachment", map.get("attachment"));
            result.put("task", map.get("task"));

            result.put("data", oaPrivileges);
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result", "failed");
        }
        return result;
    }
    //更新组
    @RequestMapping(value = "/group",method = {RequestMethod.POST},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object updateGroup(@RequestBody GroupInfo groupInfo, HttpServletRequest request) throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
//        OAGroup oaGroup = oaGroupMapper.selectByPrimaryKey(groupInfo.getGroupId());
        OAGroup oaGroup = new OAGroup();
        Map<String,Object> res = new LinkedHashMap<>();
        Map<String,Object> map  = new LinkedHashMap<>();
        try {
            //工作流
            map.put("flow",groupInfo.getFlow());
            map.put("task", groupInfo.getTask());
            //任务
            map.put("attachment", groupInfo.getAttachment());

            Map menu = new LinkedHashMap();
            for (OAPrivilege oaPrivilege : groupInfo.getOaPrivileges()) {
                menu.put(oaPrivilege.getPrivilegeId(), oaPrivilege.getName());
            }

            map.put("menu", menu);
            oaGroup.setGroupId(groupInfo.getGroupId());
            oaGroup.setDescribe(groupInfo.getDescription());
            oaGroup.setGroupName(groupInfo.getGroupName());
            oaGroup.setPrivilegeids(JsonUtils.toJsonString(map));
            oaGroup.setUserId(loginUser.getId());
            oaGroupMapper.updateByPrimaryKeySelective(oaGroup);
            auditService.audit(new OAAudit(loginUser.getName(), String.format("更新用户组 %s",oaGroup.getGroupName())));
        }catch (Exception e){
            LOGGER.error("异常",e);
            res.put("result", "failed");
        }
        return res;
    }

    //删除组
    @RequestMapping(value = "/group",method = {RequestMethod.DELETE},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object deleteGroup(@RequestParam (value = "id", required=true)Integer id, HttpServletRequest request) throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }

        Map<String,String> res = new LinkedHashMap<>();
        res.put("result","success");
        try {
            oaGroupMapper.deleteByPrimaryKey(id);
            auditService.audit(new OAAudit(loginUser.getName(), String.format("删除组")));
        }catch (Exception e){
            LOGGER.error("异常",e);
            res.put("result", "failed");
        }
        return res;
    }
    //组列表
    @RequestMapping(value = "/list",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object listGroup(@RequestParam(value = "groupName", required=false) String groupName,
                            @RequestParam(value = "offset", required=true) Integer offset,
                            @RequestParam(value = "limit", required=true) Integer limit,
                             HttpServletRequest request) throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            throw new OAException(1101, "用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        List<OAGroup> list = oaGroupMapper.list(groupName,offset,limit,null,null);
        Integer count = oaGroupMapper.count(groupName,null,null);
        result.put("rows",list);
        result.put("total",count);
        return result;
    }
    //添加组时，权限下发
    @RequestMapping(value = "/getPrivileges",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object getPrivileges(HttpServletRequest request) throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            List<OAPrivilege> oaPrivileges = oaPrivilegeMapper.list("menu");
            for (OAPrivilege oaPrivilege : oaPrivileges) {
                oaPrivilege.setId(oaPrivilege.getPrivilegeId());
            }
            result.put("data", oaPrivileges);
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result", "failed");
        }
        return result;
    }


    //用户菜单
    @RequestMapping(value = "/bars",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object bars(HttpServletRequest request) throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String,Boolean> res = new LinkedHashMap<>();
        if(loginUser.getName().equals("admin")){
            res.put("user",true);
            res.put("password",true);
            res.put("group",true);
            res.put("enterprise",true);
            res.put("upload",true);
            res.put("form",true);
            res.put("modeler",true);
            res.put("deployment",true);
            res.put("process",true);
            res.put("myProcess",true);
            res.put("initiator",true);
            res.put("pending",true);
            res.put("complete",true);
            res.put("privateReport",true);
            res.put("fawuReport",true);
            res.put("audit",true);
            return res;
        }
        OAUser oaUser = userService.getUserById(loginUser.getId());
        if(null != oaUser && null != oaUser.getGroupId() && oaUser.getGroupId() > 0){
            OAGroup oaGroup = oaGroupMapper.selectByPrimaryKey(oaUser.getGroupId());
            if(null != oaGroup && oaGroup.getStatus()== 1){
                Map map = JsonUtils.jsonToMap(oaGroup.getPrivilegeids());
                Map<String,String> map1 = (Map<String,String>)map.get("menu");
                for(Map.Entry<String,String> entry:map1.entrySet()){
                    res.put(entry.getValue(),true);
                }
            }
        }
        if(res.size()  == 0 ){
            res.put("user",true);
            res.put("password",true);
            res.put("enterprise",true);
            res.put("group",false);
            res.put("upload",false);
            res.put("form",false);
            res.put("modeler",false);
            res.put("deployment",false);
            res.put("process",true);
            res.put("myProcess",true);
            res.put("initiator",true);
            res.put("pending",true);
            res.put("complete",true);
            res.put("privateReport",true);
            res.put("fawuReport",false);
            res.put("audit",false);
        }
        return res;
    }
    //用户页面按钮
    @RequestMapping(value = "/loginMenus",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object loginMenus(HttpServletRequest request) throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        List<Menu> list = new LinkedList<>();
        if(loginUser.getName().equals("admin")){
            Menu menu = new Menu("user","用户界面",true);
            list.add(menu);
            menu = new Menu("group","用户组",true);
            list.add(menu);
            menu = new Menu("enterprise","公司管理",true);
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
            menu = new Menu("privateReport","报表",true);
            list.add(menu);
            menu = new Menu("fawuReport","报表",true);
            list.add(menu);
            return list;
        }

        OAUser oaUser = userService.getUserById(loginUser.getId());
        if(null != oaUser && oaUser.getGroupId()!= null && oaUser.getGroupId()> 0){
            OAGroup oaGroup = oaGroupMapper.selectByPrimaryKey(oaUser.getGroupId());
            if(null != oaGroup && oaGroup.getStatus() == 1){
                Map map = JsonUtils.jsonToMap(oaGroup.getPrivilegeids());
                String flow = map.get("flow").toString();
                String task = map.get("task").toString();
                if(flow.equals("0")){
                    Menu menu = new Menu("user","用户界面",true);
                    list.add(menu);
                    menu = new Menu("group","用户组",true);
                    list.add(menu);
                    menu = new Menu("enterprise","公司管理",true);
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

                }else{
                    Menu menu = new Menu("user","用户界面",false);
                    list.add(menu);
                    menu = new Menu("group","用户组",false);
                    list.add(menu);
                    menu = new Menu("enterprise","公司管理",false);
                    list.add(menu);
                    menu = new Menu("audit","审计界面",false);
                    list.add(menu);
                    menu = new Menu("modeler","流程设计界面",false);
                    list.add(menu);
                    menu = new Menu("upload","合同模板界面",false);
                    list.add(menu);
                    menu = new Menu("form","表单项界面",false);
                    list.add(menu);
                    menu = new Menu("deployment","部署界面",false);
                    list.add(menu);

                }
                if(task.equals("0")){
                    Menu menu = new Menu("process","流程申请",true);
                    list.add(menu);
                    menu = new Menu("myProcess","我的申请",true);
                    list.add(menu);
                    menu = new Menu("pending","待办任务",true);
                    list.add(menu);
                    menu = new Menu("complete","已办任务",true);
                    list.add(menu);
                    menu = new Menu("privateReport","报表",true);
                    list.add(menu);
                    menu = new Menu("fawuReport","报表",true);
                    list.add(menu);
                }else{
                    Menu menu = new Menu("process","流程申请",false);
                    list.add(menu);
                    menu = new Menu("myProcess","我的申请",false);
                    list.add(menu);
                    menu = new Menu("pending","待办任务",false);
                    list.add(menu);
                    menu = new Menu("complete","已办任务",false);
                    list.add(menu);
                    menu = new Menu("privateReport","报表",false);
                    list.add(menu);
                    menu = new Menu("fawuReport","报表",false);
                    list.add(menu);

                }
            }
        }
        if(list.size() == 0){
            Menu menu = new Menu("user","用户界面",false);
            list.add(menu);
            menu = new Menu("group","用户组",false);
            list.add(menu);
            menu = new Menu("enterprise","公司管理",false);
            list.add(menu);
            menu = new Menu("audit","审计界面",false);
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
            menu = new Menu("privateReport","报表",true);
            list.add(menu);
            menu = new Menu("fawuReport","报表",false);
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
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
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
                    .listPage(offset, limit);//返回一个集合列表，封装流程定义
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

            result.put("rows", processDefList);
            result.put("total", size);
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result", "failed");
        }
        return result;
    }

    @RequestMapping(value = "/createGroup",method = {RequestMethod.PUT},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object createGroup(@RequestBody OAOrganization oaOrganization, HttpServletRequest request){
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser){
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            userOrganizitionService.save(oaOrganization);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("新建组 %s",oaOrganization.getOrganizationName())));
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
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        auditService.audit(new OAAudit(loginUser.getName(),String.format("新建组织结构 %s",oaOrganization.getOrganizationName())));
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
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            userOrganizitionService.delete(oaOrganization);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("新建组织结构 %s",oaOrganization.getOrganizationName())));
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
        if(null == loginUser){
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        return userOrganizitionService.getGroupList(offset,limit);
    }

    @RequestMapping(value = "/tree",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object tree(HttpServletRequest request){
        List<OAOrganization> list = userOrganizitionService.getOrganizition();
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
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            oaUser.setUserPwd(Md5Utils.getMd5(oaUser.getUserPwd()));
            userService.create(oaUser);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("新建用户 %s",oaUser.getUserName())));
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
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }

        Object oldPassword = map.get("userPwd");
        Object newPassword = map.get("userPwdNew");
        Object userId = map.get("userId");
        Integer userIdServer ;

        if(null != userId && StringUtils.isNotBlank(userId.toString())){
            userIdServer = Integer.parseInt(userId.toString());
        }else{
            userIdServer = loginUser.getId();
        }

        Map<String, Object> result = new HashMap<>();
        try {
            OAUser oaUser = userService.getUserById(userIdServer);

            auditService.audit(new OAAudit(loginUser.getName(), String.format("修改密码 %s", oaUser.getUserName())));
            if(loginUser.getName().equals("admin")){
                result.put("result", "success");
                oaUser.setUserPwd(Md5Utils.getMd5(newPassword.toString()));
                userService.updatePsw(oaUser);
            }else {
                result.put("result", "failed");
                if (oaUser.getUserPwd().equals(Md5Utils.getMd5(oldPassword.toString()))) {
                    result.put("result", "success");
                    oaUser.setUserPwd(Md5Utils.getMd5(newPassword.toString()));
                    userService.updatePsw(oaUser);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("异常", e);
            result.put("result", "failed");
        }

        return result;
    }
    //更新用户信息
    @RequestMapping(value = "/update",method = {RequestMethod.POST},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object updateUser(@RequestBody OAUser oaUser,HttpServletRequest request)throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            userService.update(oaUser);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("更新用户信息 %s",oaUser.getUserName())));
        }catch (Exception e){
            LOGGER.error("异常",e);
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
        if(null == loginUser){
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String,Object> map =  new LinkedHashMap<>();
        map.put("result","success");
        try {
            map.put("data", userService.getUserById(userId));
            map.put("group", oaGroupMapper.total());
            map.put("company", oaEnterpriseService.total());
        }catch (Exception e){
            LOGGER.error("异常",e);
            map.put("result", "failed");
        }
        return map;
    }
    //通过USR_ID获取USER信息
    @RequestMapping(value = "/userGroup",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object userGroup(
                              HttpServletRequest request,
                              HttpServletResponse response
    )throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser){
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String,Object> map =  new LinkedHashMap<>();
        map.put("result","success");
        try {
            map.put("group", oaGroupMapper.total());
            map.put("company",oaEnterpriseService.total());
        }catch (Exception e){
            LOGGER.error("异常",e);
            map.put("result", "failed");
        }
        return map;
    }


    @RequestMapping(value = "/userList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object userList(HttpServletRequest request
                           )throws OAException{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
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
