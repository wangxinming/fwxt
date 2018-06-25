package com.wxm.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxm.common.JsonUtils;
import com.wxm.entity.KeyValue;
import com.wxm.entity.TaskComment;
import com.wxm.model.*;
import com.wxm.service.*;
import com.wxm.util.exception.OAException;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("api/deployments")
public class DeployController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployController.class);
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ConcactTemplateService concactTemplateService;
    @Autowired
    private ContractCirculationService contractCirculationService;
    @Autowired
    private FormPropertiesService formPropertiesService;

    @Autowired
    private OADeploymentTemplateService oaDeploymentTemplateService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private UserService userService;
    @Autowired
    private GroupService groupService;

    @RequestMapping(value = "/updateTemRelation",method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object updateTemRelation(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{
            String dID = request.getParameter("dID");
            String rID = request.getParameter("rID");
            LOGGER.info("修改模板关系，参数：{}，{}",dID,rID);
            OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(dID);
            if(oaDeploymentTemplateRelation != null ){
                if(StringUtils.isBlank(rID)){
                    oaDeploymentTemplateRelation.setRelationTemplateid(0);
                }else{
                    oaDeploymentTemplateRelation.setRelationTemplateid(Integer.parseInt(rID));
                }
                oaDeploymentTemplateRelation.setRelationDeploymentid(dID);
                oaDeploymentTemplateService.update(oaDeploymentTemplateRelation);
                auditService.audit(new OAAudit(loginUser.getName(),String.format("修改模板关系")));
            }else{
                oaDeploymentTemplateRelation = new OADeploymentTemplateRelation();
                oaDeploymentTemplateRelation.setRelationCreatetime(new Date());
                oaDeploymentTemplateRelation.setRelationDeploymentid(dID);
                oaDeploymentTemplateRelation.setRelationTemplateid(Integer.parseInt(rID));
                oaDeploymentTemplateService.insert(oaDeploymentTemplateRelation);
                auditService.audit(new OAAudit(loginUser.getName(),String.format("新增模板关系")));
            }
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result","failed");
        }
        return result;
    }

    @RequestMapping(value = "/removeRelation",method = RequestMethod.DELETE,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object removeRelation(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{
            String id = request.getParameter("id");
            LOGGER.info("删除关系，参数：{}",id);
            oaDeploymentTemplateService.delete(Integer.parseInt(id));
            auditService.audit(new OAAudit(loginUser.getName(),String.format("删除模板关系")));
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result","failed");
        }
        return result;
    }


    @RequestMapping(value = "/templateUpdate",method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object templateUpdate(HttpServletRequest request,@RequestBody Map<String,String> map )throws Exception {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try{
            String id = map.get("id");
            String status = map.get("status");
            LOGGER.info("更新模板，参数：{} {}",id,status);
            OAContractTemplate oaContractTemplate = new OAContractTemplate();
            oaContractTemplate.setTemplateId(Integer.parseInt(id));
            oaContractTemplate.setTemplateStatus(Integer.parseInt(status));
            concactTemplateService.update(oaContractTemplate);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("更新模板")));
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result", "failed");
        }
        return result;

    }

    @RequestMapping(value = "/saveUploadFile",method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object saveUploadFile(HttpServletRequest request,@RequestBody Map<String,String> map )throws Exception {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            String id = map.get("id");
            String name = map.get("name");
            String des = map.get("des");
            String html = map.get("html");

            LOGGER.info("保存文件，参数：{} {} {} {}",id,name,des,html);
            OAContractTemplate oaContractTemplate = new OAContractTemplate();
//            WordEntity wordEntity = new WordEntity();
            oaContractTemplate.setTemplateName(name);
            oaContractTemplate.setTemplateId(Integer.parseInt(id));
            oaContractTemplate.setTemplateHtml(html);
            oaContractTemplate.setTemplateDes(des);

            concactTemplateService.update(oaContractTemplate);
//            wordTemplateService.update(wordEntity);
            //合并字段
            Map<String,String> mapField = new LinkedHashMap<>();
            Map<String,String> mapFieldCheck = new LinkedHashMap<>();
            for(Map.Entry<String,String> entry:map.entrySet()){
                if(entry.getKey().contains("name_")){
                    mapField.put(entry.getKey(),entry.getValue());
                }
                if(entry.getKey().contains("checkbox_")){
                    mapFieldCheck.put(entry.getKey(),entry.getValue());
                }
            }

            List<OAFormProperties> oaFormPropertiesList = formPropertiesService.listByTemplateId(Integer.parseInt(id));
            Map<String,OAFormProperties> mapDBField = new LinkedHashMap<>();
            for(OAFormProperties oaFormProperties:oaFormPropertiesList){
                mapDBField.put(oaFormProperties.getFieldMd5(),oaFormProperties);
            }
            for(Map.Entry<String,String> entry : mapFieldCheck.entrySet()){
                String key = "name"+entry.getKey().substring(8);
                OAFormProperties oaFormPropertie = mapDBField.get(key);
                if(null == oaFormPropertie ) continue;
                OAFormProperties oaFormProperties = new OAFormProperties();
                oaFormProperties.setPropertiesId(oaFormPropertie.getPropertiesId());
                oaFormProperties.setStatus(entry.getValue().equals("on")?1:0);
                formPropertiesService.update(oaFormProperties);
            }
            //数据库中没有的字段，需要增加
            for(Map.Entry<String,String> entry:mapField.entrySet()){
                if(!mapDBField.containsKey(entry.getKey())){
                    OAFormProperties oaFormProperties = new OAFormProperties();
                    oaFormProperties.setFieldMd5(entry.getKey());
                    oaFormProperties.setFieldName("自定义");
                    oaFormProperties.setTemplateId(Integer.parseInt(id));
                    oaFormProperties.setTemplateName(name);
                    oaFormProperties.setCreateTime(new Date());
                    formPropertiesService.insert(oaFormProperties);
                }
            }
            //数据库中多余的字段，需要删除
            for(Map.Entry<String,OAFormProperties> entry:mapDBField.entrySet()){
                if(!mapField.containsKey(entry.getKey())){
                    formPropertiesService.delete(entry.getValue().getPropertiesId());
                }
            }
            auditService.audit(new OAAudit(loginUser.getName(), String.format("更新合同模板 %s",name)));
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result", "failed");
        }
        return result;
    }
    @RequestMapping(value = "/concatFile", method = RequestMethod.GET)
    @ResponseBody
    public Object concatFile(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        String template_id = request.getParameter("template_id");
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{
            LOGGER.info("合同模板ID，参数：{}",template_id);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("查询合同，合同编号%s",template_id)));
            OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(Integer.parseInt(template_id));
            List<OAFormProperties> oaFormPropertiesList = formPropertiesService.listByTemplateId(Integer.parseInt(template_id));
            result.put("data",oaContractTemplate);
            result.put("fields",oaFormPropertiesList);
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result", "failed");
        }
        return result;
    }

    @RequestMapping(value = "/uploadFileInfo", method = RequestMethod.GET)
    @ResponseBody
    public Object uploadFileInfo(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try {
            auditService.audit(new OAAudit(loginUser.getName(),String.format("查询文件信息")));
            List<KeyValue> list = new LinkedList<>();
            //部署ID
            String id = request.getParameter("id");
            String processInstanceId = request.getParameter("processInstanceId");
            LOGGER.info("获取文件信息，参数：{}",processInstanceId);

            OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processInstanceId);
            result.put("workStatus",oaContractCirculationWithBLOBs.getWorkStatus());
            result.put("title",oaContractCirculationWithBLOBs.getContractName());
            result.put("showCommit", true);
            if(null != processInstanceId) {
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
                Object object = taskService.getVariable(task.getId(), "taskDefinitionKey");
                if (object == null && StringUtils.isBlank(object.toString())) {
                    result.put("showCommit", true);
                } else {
                    result.put("showCommit", false);
                    Object refuseTask = runtimeService.getVariable(task.getExecutionId(),"refuseTask");
                    result.put("refuse",refuseTask);

                }
            }
            OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(oaContractCirculationWithBLOBs.getTemplateId());
            if (StringUtils.isNotBlank(processInstanceId)) {
                Map<String, VariableInstance> stringVariableInstanceMap = runtimeService.getVariableInstances(processInstanceId);
                for (Map.Entry<String, VariableInstance> entry : stringVariableInstanceMap.entrySet()) {
                    if (null == entry.getValue() || StringUtils.isBlank(entry.getValue().getTextValue())) continue;
                    if (entry.getKey().startsWith("name")) {
                        KeyValue keyValue = new KeyValue(entry.getKey(), entry.getValue().getTextValue());
                        list.add(keyValue);
                    }
                }
                result.put("rows", list);
            }
            result.put("data",oaContractTemplate);
            List<OAFormProperties> oaFormPropertiesList = formPropertiesService.listByTemplateId(oaContractTemplate.getTemplateId());
            result.put("fields",oaFormPropertiesList);

        }catch (Exception e){
            result.put("result","failed");
            LOGGER.error("参数异常",e);
        }
        return result;
    }

    @RequestMapping(value = "/uploadFileInfoAdd", method = RequestMethod.GET)
    @ResponseBody
    public Object uploadFileInfoAdd(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try {
            //部署ID
            String contract = request.getParameter("contract");
            LOGGER.info("合同ID号，参数：{}",contract);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("查询模板以及表单信息")));
            result.put("showCommit", true);
            OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(Integer.parseInt(contract));
            result.put("data",oaContractTemplate);
            List<OAFormProperties> oaFormPropertiesList = formPropertiesService.listByTemplateId(oaContractTemplate.getTemplateId());
            result.put("fields",oaFormPropertiesList);
        }catch (Exception e){
            result.put("result","failed");
            LOGGER.error("参数异常",e);
        }
        return result;
    }


    @RequestMapping(value = "/uploadFile", method = RequestMethod.GET)
    @ResponseBody
    public Map uploadFile(HttpServletRequest request,
                          @RequestParam(value = "offset", required = true) int offset,
                          @RequestParam(value = "limit", required = true) int limit
                          )throws Exception {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        try{
            int count = concactTemplateService.count();
            List<OAContractTemplate> oaContractTemplateList = concactTemplateService.list(offset, limit);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("分页查询模板列表")));
            result.put("result","success");
            result.put("rows", oaContractTemplateList);
            result.put("total", count);
        }catch (Exception e){
            result.put("result","failed");
            LOGGER.error("异常",e);
        }
        return result;
    }

    @RequestMapping(value = "/removeModeler", method = RequestMethod.DELETE)
    @ResponseBody
    public Map deleteModeler(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            String id = request.getParameter("id");
            LOGGER.info("删除流程ID，参数：{}",id);
            repositoryService.deleteModel(id);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("删除流程模板")));
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result", "failed");
        }
        return result;
    }


    @RequestMapping(value = "/remove", method = RequestMethod.DELETE)
    @ResponseBody
    public Map delete(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser){
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            String id = request.getParameter("id");
            LOGGER.info("删除已经部署流程ID，参数：{}",id);
            repositoryService.deleteDeployment(id, true);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("删除部署流程")));
//        log.error("delete guid: "+id);
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result", "failed");
        }
        return result;
    }
    @RequestMapping(value = "/publish", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Map publish(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        String id= request.getParameter("id");
        try {
            LOGGER.info("发布流程，参数：{}",id);
            //获取模型
            Model modelData = repositoryService.getModel(id);
            byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

            if (bytes == null) {
                result.put("result", "failed");
                return result;
            }

            JsonNode modelNode = new ObjectMapper().readTree(bytes);

            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            if(model.getProcesses().size()==0){
                result.put("result", "failed");
                return result;
            }
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

            //发布流程
            String processName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .addString(processName, new String(bpmnBytes, "UTF-8"))
                    .deploy();
            modelData.setDeploymentId(deployment.getId());
            repositoryService.saveModel(modelData);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("发布流程： "+processName)));
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result", "failed");
        }
        return result;
    }

    //查看历史任务详情
    @RequestMapping(value = "htmlHistory",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object htmlHistory(HttpServletRequest request) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser){
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        auditService.audit(new OAAudit(loginUser.getName(),String.format("历史任务详情查询")));
        //历史遗留
        String processInstanceId = request.getParameter("taskId");
        LOGGER.info("工作流实例ID，参数：{}",processInstanceId);
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processInstanceId);
        OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(oaContractCirculationWithBLOBs.getTemplateId());

        Map<String,KeyValue> map = new LinkedHashMap();
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        //判断当前合同是否自定义合同
        if(null != oaContractCirculationWithBLOBs.getContractId() && oaContractCirculationWithBLOBs.getContractPdf() != null){
            result.put("download",oaContractCirculationWithBLOBs.getContractId());
        }
        if(StringUtils.isNotBlank(historicProcessInstance.getId())){
            List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().processInstanceId(historicProcessInstance.getId()).list();
            for (HistoricVariableInstance historicVariableInstance : historicVariableInstances) {
                if(null == historicVariableInstance || null == historicVariableInstance.getValue() || null == historicVariableInstance.getVariableName())continue;
                if(historicVariableInstance.getVariableName().startsWith("name")) {
                    KeyValue keyValue = new KeyValue(historicVariableInstance.getVariableName(), historicVariableInstance.getValue().toString());
                    map.put(historicVariableInstance.getVariableName(),keyValue);
                }
            }
            result.put("rows",map.values());
        }

        //关键字展现
        StringBuilder sb = new StringBuilder("<div align=\"CENTER\"><b>关键信息</b></div><div class=\"cjk\" align=\"LEFT\">　　</div><div align=\"LEFT\">");
        List<OAFormProperties> oaFormPropertiesList = formPropertiesService.listByTemplateId(oaContractTemplate.getTemplateId());
        for(OAFormProperties oaFormProperties : oaFormPropertiesList){
            if(null == oaFormProperties  || null == oaFormProperties.getStatus()|| oaFormProperties.getStatus() != 1) continue;
            sb.append("<div align=\"LEFT\">");
            sb.append(oaFormProperties.getFieldName());
            sb.append(":");
            if(map.containsKey(oaFormProperties.getFieldMd5()))
                sb.append(map.get(oaFormProperties.getFieldMd5()).getValue());
            sb.append("</div>");

        }
        result.put("keyword",sb.toString());

        List<TaskComment> taskCommentList = new LinkedList<>();
//        List<HistoricTaskInstance> historicTaskInstanceList = historyService.createHistoricTaskInstanceQuery().finished().processInstanceId(historicProcessInstance.getId()).orderByTaskCreateTime().desc().list();
        boolean flag = false;
        OAUser oaUser = userService.getUserById(loginUser.getId());
        if(null == oaUser.getGroupId() || oaUser.getGroupId() < 1){
            flag = false;
        }else {
            OAGroup oaGroup = groupService.getGroupById(oaUser.getGroupId());
            Map mapGroup = JsonUtils.jsonToMap(oaGroup.getPrivilegeids());

            if (mapGroup.get("attachment") != null && mapGroup.get("attachment").equals("true")) {
                flag = true;
            }
        }
        List<HistoricActivityInstance> hais = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(historicProcessInstance.getId())
                .activityType("userTask")
                .orderByHistoricActivityInstanceStartTime().desc()
                .list();

        if(null != hais){
            for(HistoricActivityInstance historicActivityInstance : hais){
                List<Comment> taskList1 = taskService.getTaskComments(historicActivityInstance.getTaskId());
                if(taskList1.size() >0) {
                    for (Comment comment : taskList1) {
                        TaskComment taskComment = new TaskComment();
                        if(StringUtils.isBlank(comment.getUserId())) {
                            taskComment.setName(historicActivityInstance.getAssignee());
                        }else{
                            taskComment.setName(comment.getUserId());
                        }
                        taskComment.setCreateTime(comment.getTime());
                        taskComment.setDescription(comment.getFullMessage());
                        taskCommentList.add(taskComment);
                    }
                }else {
                    HistoricVariableInstance hi = historyService.createHistoricVariableInstanceQuery().processInstanceId(historicActivityInstance.getProcessInstanceId()).variableName(historicActivityInstance.getTaskId()).singleResult();
                    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery().processInstanceId(historicActivityInstance.getProcessInstanceId()).variableName("user").singleResult();
                    if (null == hi) {
                        TaskComment taskComment = new TaskComment();
                        if (historicActivityInstance.getAssignee() == null) {
                            taskComment.setName(variableInstance.getValue().toString());
                            taskComment.setCreateTime(historicActivityInstance.getStartTime());
                            taskComment.setDescription("提交");
                        } else {
                            taskComment.setName(historicActivityInstance.getAssignee());
                            taskComment.setCreateTime(historicActivityInstance.getStartTime());
                            taskComment.setDescription("待审批");
                        }
                        taskCommentList.add(taskComment);
                    }else{
                        //当前节点没有审批信息，表明处于当前节点用户审批状态下
                        TaskComment taskComment = new TaskComment();
                        if (historicActivityInstance.getAssignee() == null) {
//                            VariableInstance variableInstance = runtimeService.getVariableInstance(processInstance.getId(), "user");
                            taskComment.setName(variableInstance.getValue().toString());
                            taskComment.setCreateTime(historicActivityInstance.getStartTime());
                            if(flag) {
                                taskComment.setDescription(hi.getValue().toString());
                            }else{
                                taskComment.setDescription("通过");
                            }
                        } else {
                            taskComment.setName(historicActivityInstance.getAssignee());
                            taskComment.setCreateTime(historicActivityInstance.getStartTime());
                            if(flag) {
                                taskComment.setDescription(hi.getValue().toString());
                            }else{
                                taskComment.setDescription("通过");
                            }
                        }
                        taskCommentList.add(taskComment);
                    }
                }
            }
            result.put("comments",taskCommentList);
        }
        result.put("info",oaContractTemplate.getTemplateHtml());
        return result;

    }

    //查看当前任务详情
    @RequestMapping(value = "html",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object html(HttpServletRequest request)throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        auditService.audit(new OAAudit(loginUser.getName(),String.format("查看当前任务详情")));
        String taskId = request.getParameter("taskId");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(task.getProcessInstanceId());
        OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(oaContractCirculationWithBLOBs.getTemplateId());

        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        Map<String,KeyValue> map = new LinkedHashMap();
        //判断当前合同是否自定义合同
        if(oaContractCirculationWithBLOBs.getContractId() != null && oaContractCirculationWithBLOBs.getContractPdf() != null ) {
            result.put("download", oaContractCirculationWithBLOBs.getContractId());
        }
        if(StringUtils.isNotBlank(processInstance.getId())){
            Map<String, VariableInstance> stringVariableInstanceMap = runtimeService.getVariableInstances(processInstance.getId());
            for (Map.Entry<String, VariableInstance> entry : stringVariableInstanceMap.entrySet()) {
                if(null == entry.getValue() || StringUtils.isBlank(entry.getValue().getTextValue()))continue;
                if(entry.getKey().startsWith("name")) {
                    KeyValue keyValue = new KeyValue(entry.getKey(), entry.getValue().getTextValue());
                    map.put(entry.getKey(),keyValue);
                }
            }
            result.put("rows",map.values());
        }

        //关键字展现
        StringBuilder sb = new StringBuilder("<div align=\"CENTER\"><b>关键信息</b></div><div class=\"cjk\" align=\"LEFT\">　　</div><div align=\"LEFT\">");
        List<OAFormProperties> oaFormPropertiesList = formPropertiesService.listByTemplateId(oaContractTemplate.getTemplateId());
        for(OAFormProperties oaFormProperties : oaFormPropertiesList){
            if(null == oaFormProperties || null == oaFormProperties.getStatus()|| oaFormProperties.getStatus() != 1) continue;
            sb.append("<div align=\"LEFT\">");
            sb.append(oaFormProperties.getFieldName());
            sb.append(":");
            if(map.containsKey(oaFormProperties.getFieldMd5()))
                sb.append(map.get(oaFormProperties.getFieldMd5()).getValue());
            sb.append("</div>");

        }
        result.put("keyword",sb.toString());
        List<TaskComment> taskCommentList = new LinkedList<>();
        boolean flag = false;
        OAUser oaUser = userService.getUserById(loginUser.getId());
        if(null == oaUser.getGroupId() || oaUser.getGroupId() < 1){
            flag = false;
        }else {
            OAGroup oaGroup = groupService.getGroupById(oaUser.getGroupId());
            Map mapGroup = JsonUtils.jsonToMap(oaGroup.getPrivilegeids());

            if (mapGroup.get("attachment") != null && mapGroup.get("attachment").equals("0")) {
                flag = true;
            }
        }
        List<HistoricActivityInstance> hais = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstance.getId())
                .activityType("userTask")
                .orderByHistoricActivityInstanceStartTime().desc()
                .list();
//        List<HistoricActivityInstance> hais = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).list();
//        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskCreateTime().desc().list();
        if(null != hais){
            for(HistoricActivityInstance historicActivityInstance : hais){
                List<Comment> taskList1 = taskService.getTaskComments(historicActivityInstance.getTaskId());
                if(taskList1.size() >0) {
                    for (Comment comment : taskList1) {
                        TaskComment taskComment = new TaskComment();
                        if(StringUtils.isBlank(comment.getUserId())) {
                            taskComment.setName(historicActivityInstance.getAssignee());
                        }else{
                            taskComment.setName(comment.getUserId());
                        }
                        taskComment.setCreateTime(comment.getTime());
                        taskComment.setDescription(comment.getFullMessage());
                        taskCommentList.add(taskComment);
                    }
                }else {
                    Object object = runtimeService.getVariable(processInstance.getId(), historicActivityInstance.getTaskId());
                    if (null == object) {
                        //当前节点没有审批信息，表明处于当前节点用户审批状态下
                        TaskComment taskComment = new TaskComment();

                        if (historicActivityInstance.getAssignee() == null) {
                            VariableInstance variableInstance = runtimeService.getVariableInstance(processInstance.getId(), "user");
                            taskComment.setName(variableInstance.getValue().toString());
                            taskComment.setCreateTime(historicActivityInstance.getStartTime());
                            taskComment.setDescription("提交");
                        } else {
                            taskComment.setName(historicActivityInstance.getAssignee());
                            taskComment.setCreateTime(historicActivityInstance.getStartTime());
                            taskComment.setDescription("待审批");
                        }
                        taskCommentList.add(taskComment);
                    }else{
                        //当前节点没有审批信息，表明处于当前节点用户审批状态下
                        TaskComment taskComment = new TaskComment();
                        if (historicActivityInstance.getAssignee() == null) {
                            VariableInstance variableInstance = runtimeService.getVariableInstance(processInstance.getId(), "user");
                            taskComment.setName(variableInstance.getValue().toString());
                            taskComment.setCreateTime(historicActivityInstance.getStartTime());
                            if(flag) {
                                taskComment.setDescription(object.toString());
                            }else{
                                taskComment.setDescription("通过");
                            }
                        } else {
                            taskComment.setName(historicActivityInstance.getAssignee());
                            taskComment.setCreateTime(historicActivityInstance.getStartTime());
                            if(flag) {
                                taskComment.setDescription(object.toString());
                            }else{
                                taskComment.setDescription("通过");
                            }
                        }
                        taskCommentList.add(taskComment);
                    }
                }
            }
            result.put("comments",taskCommentList);
        }
        result.put("info",oaContractTemplate.getTemplateHtml());
        return result;
    }

    //更新模板启用状态
    @RequestMapping(value = "/updateProcessStatus", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object updateStatus(HttpServletRequest request){
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try{
            String id = request.getParameter("id");
            String status = request.getParameter("status");
            ProcessDefinition pf = repositoryService.createProcessDefinitionQuery().deploymentId(id).singleResult();
            if(!repositoryService.isProcessDefinitionSuspended(pf.getId()) && Integer.parseInt(status) == 0) {
                repositoryService.suspendProcessDefinitionById(pf.getId());
            }else{
                repositoryService.activateProcessDefinitionById(pf.getId());
            }
            auditService.audit(new OAAudit(loginUser.getName(), String.format("更新合同模板状态 %s",status)));

        }catch (Exception e){
            LOGGER.error("异常",e);
        }
        return result;
    }

    //获取部署模板列表，用于用户提交合同
    @RequestMapping(value = "deploymentContract",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object deploymentContract (HttpServletRequest request,
                        @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
                        @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit)throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        List<ProcessDefinition> pfList = repositoryService.createProcessDefinitionQuery().active()
                .orderByDeploymentId().desc().listPage(offset,limit);
        long count = repositoryService.createProcessDefinitionQuery().active().count();
        List<com.wxm.entity.Deployment> list = new ArrayList<>();
        auditService.audit(new OAAudit(loginUser.getName(),String.format("获取部署模板列表，用于用户提交合同")));
        for(ProcessDefinition pf:pfList){
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(pf.getDeploymentId()).singleResult();
            com.wxm.entity.Deployment deploy= new com.wxm.entity.Deployment();
            OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(deployment.getId());
            if(null != oaDeploymentTemplateRelation) {
                OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(oaDeploymentTemplateRelation.getRelationTemplateid());
                deploy.setOAContractTemplate(oaContractTemplate);
            }
            deploy.setStatus(pf.isSuspended()?0:1);
            deploy.setVersion(new Integer(pf.getVersion()).toString());
            deploy.setId(deployment.getId());
            deploy.setName(deployment.getName());
            deploy.setCategory(deployment.getCategory());
            deploy.setDeploymentTime(deployment.getDeploymentTime());
            deploy.setTenantId(deployment.getTenantId());

            list.add(deploy);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("rows",list);
        result.put("total",count);
        return  result;

    }
    //获取部署模板列表，对应word审批模板
    @RequestMapping(value = "deploymentList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object list (HttpServletRequest request,
                        @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
                        @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit)throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        auditService.audit(new OAAudit(loginUser.getName(),String.format("获取部署模板列表，对应审批模板")));
        List<Deployment> deployments = repositoryService.createDeploymentQuery()
                .orderByDeploymenTime().desc()
                .listPage(offset, limit);

        long count = repositoryService.createDeploymentQuery().count();
        List<com.wxm.entity.Deployment> list = new ArrayList<>();
        for(Deployment deployment: deployments){
            com.wxm.entity.Deployment deploy= new com.wxm.entity.Deployment();
            ProcessDefinition pf = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
            OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(deployment.getId());
            if(null != oaDeploymentTemplateRelation) {
                OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(oaDeploymentTemplateRelation.getRelationTemplateid());
                deploy.setOAContractTemplate(oaContractTemplate);
            }
            deploy.setStatus(pf.isSuspended()?0:1);
            deploy.setVersion(new Integer(pf.getVersion()).toString());
            deploy.setId(deployment.getId());
            deploy.setName(deployment.getName());
            deploy.setCategory(deployment.getCategory());
            deploy.setDeploymentTime(deployment.getDeploymentTime());
            deploy.setTenantId(deployment.getTenantId());

            list.add(deploy);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("rows",list);
        result.put("total",count);
        return  result;

    }

    @RequestMapping(value = "modelerList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object getList(HttpServletRequest request,
                          @RequestParam(value = "offset", defaultValue = "0", required = true) Integer offset,
                          @RequestParam(value = "limit", defaultValue = "10", required = true) Integer limit) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        auditService.audit(new OAAudit(loginUser.getName(),String.format("获取流程列表")));
        List<Model> list = repositoryService.createModelQuery().orderByCreateTime().desc().listPage(offset, limit);
        long count = repositoryService.createModelQuery().count();
        Map<String, Object> result = new HashMap<>();
        result.put("rows",list);
        result.put("total",count);
        return  result;
    }

}
