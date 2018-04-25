package com.wxm.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxm.activiti.vo.DeploymentResponse;
import com.wxm.entity.KeyValue;
import com.wxm.entity.TaskComment;
import com.wxm.entity.WordEntity;
import com.wxm.model.OAAudit;
import com.wxm.model.OAContractTemplate;
import com.wxm.model.OADeploymentTemplateRelation;
import com.wxm.service.*;
import com.wxm.util.Status;
import com.wxm.util.ToWeb;
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
    private FormPropertiesService formPropertiesService;

    @Autowired
    private OADeploymentTemplateService oaDeploymentTemplateService;
    @Autowired
    private WordTemplateService wordTemplateService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private HistoryService historyService;

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
//            int count = oaDeploymentTemplateService.coutRelByDeploymentId(dID);
            OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(dID);
//            OADeploymentTemplateRelation oaDeploymentTemplateRelation = new OADeploymentTemplateRelation();
            if(oaDeploymentTemplateRelation != null ){
                oaDeploymentTemplateRelation.setRelationTemplateid(Integer.parseInt(rID));
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
            LOGGER.error("参数异常",e);
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
            oaDeploymentTemplateService.delete(Integer.parseInt(id));
            auditService.audit(new OAAudit(loginUser.getName(),String.format("删除模板关系")));
        }catch (Exception e){
            LOGGER.error("参数异常",e);
            result.put("result","failed");
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
            WordEntity wordEntity = new WordEntity();
            wordEntity.setId(Integer.parseInt(id));
            wordEntity.setName(name);
            wordEntity.setDes(des);
            wordEntity.setHtml(html);
            wordTemplateService.update(wordEntity);
            auditService.audit(new OAAudit(loginUser.getName(), String.format("更新合同模板")));
        }catch (Exception e){
            LOGGER.error("参数异常",e);
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
            List<KeyValue> list = new LinkedList<>();
            //部署ID
            String id = request.getParameter("id");
            String template_id = request.getParameter("template_id");
            int tmp_id = 0;

            String processInstanceId = request.getParameter("processInstanceId");
            if (null == template_id) {
                OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(id);
                tmp_id = oaDeploymentTemplateRelation.getRelationTemplateid();
            } else {
                tmp_id = Integer.parseInt(template_id);
            }
            result.put("showCommit", true);
            if(null != processInstanceId) {
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
                Object object = taskService.getVariable(task.getId(), "taskDefinitionKey");
                if (object == null || StringUtils.isBlank(object.toString())) {
                    result.put("showCommit", true);
                } else {
                    result.put("showCommit", false);
                }
            }
            OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(tmp_id);
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

        }catch (Exception e){
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
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        int count = concactTemplateService.count();
        List<OAContractTemplate> oaContractTemplateList = concactTemplateService.list(offset,limit);
        Map<String, Object> result = new HashMap<>();
        result.put("rows",oaContractTemplateList);
        result.put("total",count);
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
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            String id = request.getParameter("id");
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
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        String id= request.getParameter("id");
        try {
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
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        //历史遗留
        String processInstanceId = request.getParameter("taskId");
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(historicProcessInstance.getDeploymentId()).singleResult();
        OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(deployment.getId());
        OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(oaDeploymentTemplateRelation.getRelationTemplateid());

        List<KeyValue> list = new LinkedList<>();
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        if(StringUtils.isNotBlank(historicProcessInstance.getId())){
            List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().processInstanceId(historicProcessInstance.getId()).list();

            for (HistoricVariableInstance historicVariableInstance : historicVariableInstances) {
                if(null == historicVariableInstance || null == historicVariableInstance.getValue() || null == historicVariableInstance.getVariableName())continue;
                if(historicVariableInstance.getVariableName().startsWith("name")) {
                    KeyValue keyValue = new KeyValue(historicVariableInstance.getVariableName(), historicVariableInstance.getValue().toString());
                    list.add(keyValue);
                }
            }
            result.put("rows",list);
        }

        List<TaskComment> taskCommentList = new LinkedList<>();
        List<HistoricTaskInstance> historicTaskInstanceList = historyService.createHistoricTaskInstanceQuery().finished().processInstanceId(historicProcessInstance.getId()).orderByTaskCreateTime().desc().list();

        if(null != historicTaskInstanceList){
            for(HistoricTaskInstance task1 : historicTaskInstanceList){
                List<Comment> taskList1 = taskService.getTaskComments(task1.getId());
                if(taskList1.size() >0) {
                    for (Comment comment : taskList1) {
                        TaskComment taskComment = new TaskComment();
                        if(task1.getAssignee() == null){
                            taskComment.setName(comment.getUserId());
                        }else {
                            taskComment.setName(task1.getAssignee());
                        }
                        taskComment.setCreateTime(comment.getTime());
                        taskComment.setDescription(comment.getFullMessage());
                        taskCommentList.add(taskComment);
                    }
                }else{
                    TaskComment taskComment = new TaskComment();
                    taskComment.setName(task1.getAssignee());
                    taskComment.setCreateTime(task1.getCreateTime());
                    taskComment.setDescription("待审批");
                    taskCommentList.add(taskComment);
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
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        String taskId = request.getParameter("taskId");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getExecutionId()).singleResult();
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(processInstance.getDeploymentId()).singleResult();

        OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(deployment.getId());
        OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(oaDeploymentTemplateRelation.getRelationTemplateid());

        List<KeyValue> list = new LinkedList<>();
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        if(StringUtils.isNotBlank(processInstance.getId())){
            Map<String, VariableInstance> stringVariableInstanceMap = runtimeService.getVariableInstances(processInstance.getId());
            for (Map.Entry<String, VariableInstance> entry : stringVariableInstanceMap.entrySet()) {
                if(null == entry.getValue() || StringUtils.isBlank(entry.getValue().getTextValue()))continue;
                if(entry.getKey().startsWith("name")) {
                    KeyValue keyValue = new KeyValue(entry.getKey(), entry.getValue().getTextValue());
                    list.add(keyValue);
                }
            }
            result.put("rows",list);
        }
        List<TaskComment> taskCommentList = new LinkedList<>();
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
                }else{
                    //当前节点没有审批信息，表明处于当前节点用户审批状态下
                    TaskComment taskComment = new TaskComment();
                    if(historicActivityInstance.getAssignee() == null){
                        VariableInstance variableInstance = runtimeService.getVariableInstance(processInstance.getId(),"user");
                        taskComment.setName(variableInstance.getValue().toString());
                        taskComment.setCreateTime(historicActivityInstance.getStartTime());
                        taskComment.setDescription("提交");
                    }else{
                        taskComment.setName(historicActivityInstance.getAssignee());
                        taskComment.setCreateTime(historicActivityInstance.getStartTime());
                        taskComment.setDescription("待审批");
                    }

                    taskCommentList.add(taskComment);
                }
            }
            result.put("comments",taskCommentList);
        }
        result.put("info",oaContractTemplate.getTemplateHtml());
        return result;
    }

    //获取部署模板列表，对应word审批模板
    @RequestMapping(value = "deploymentList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object list (HttpServletRequest request,
                        @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
                        @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit)throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        List<Deployment> deployments = repositoryService.createDeploymentQuery()
                .orderByDeploymenTime().desc()
                .listPage(offset, limit);
        long count = repositoryService.createDeploymentQuery().count();
        List<com.wxm.entity.Deployment> list = new ArrayList<>();
        for(Deployment deployment: deployments){
            com.wxm.entity.Deployment deploy= new com.wxm.entity.Deployment();

            OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(deployment.getId());
            if(null != oaDeploymentTemplateRelation) {
                OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(oaDeploymentTemplateRelation.getRelationTemplateid());
                deploy.setOAContractTemplate(oaContractTemplate);
            }
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
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        List<Model> list = repositoryService.createModelQuery().orderByCreateTime().desc().listPage(offset, limit);
        long count = repositoryService.createModelQuery().count();
        Map<String, Object> result = new HashMap<>();
        result.put("rows",list);
        result.put("total",count);
        return  result;
    }

}
