package com.wxm.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxm.activiti.vo.DeploymentResponse;
import com.wxm.entity.KeyValue;
import com.wxm.entity.TaskComment;
import com.wxm.entity.WordEntity;
import com.wxm.model.OAContractTemplate;
import com.wxm.model.OADeploymentTemplateRelation;
import com.wxm.service.ConcactTemplateService;
import com.wxm.service.FormPropertiesService;
import com.wxm.service.OADeploymentTemplateService;
import com.wxm.service.WordTemplateService;
import com.wxm.util.Status;
import com.wxm.util.ToWeb;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("api/deployments")
public class DeployController {
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
    private HistoryService historyService;

    @RequestMapping(value = "/updateTemRelation",method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object updateTemRelation(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{
            String dID = request.getParameter("dID");
            String rID = request.getParameter("rID");

//            int count = wordTemplateService.coutRelByDID(dID);
//            if(count > 0){
//                wordTemplateService.updateRelation(dID,Integer.parseInt(rID));
//            }else{
//                wordTemplateService.insertRel(dID,Integer.parseInt(rID));
//            }
            int count = oaDeploymentTemplateService.coutRelByDeploymentId(dID);
            OADeploymentTemplateRelation oaDeploymentTemplateRelation = new OADeploymentTemplateRelation();
            if(count > 0){
//                wordTemplateService.updateRelation(dID,Integer.parseInt(rID));
                oaDeploymentTemplateRelation.setRelationTemplateid(Integer.parseInt(rID));
                oaDeploymentTemplateRelation.setRelationDeploymentid(dID);
                oaDeploymentTemplateService.update(oaDeploymentTemplateRelation);
            }else{

                oaDeploymentTemplateRelation.setRelationCreatetime(new Date());
                oaDeploymentTemplateRelation.setRelationDeploymentid(dID);
                oaDeploymentTemplateRelation.setRelationTemplateid(Integer.parseInt(rID));
                oaDeploymentTemplateService.insert(oaDeploymentTemplateRelation);
            }
        }catch (Exception e){
            result.put("result","failed");
        }
        return result;
    }

    @RequestMapping(value = "/removeRelation",method = RequestMethod.DELETE,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object removeRelation(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{
            String id = request.getParameter("id");
            oaDeploymentTemplateService.delete(Integer.parseInt(id));
//            wordTemplateService.removeRelation(Integer.parseInt(id));
        }catch (Exception e){
            result.put("result","failed");
        }
        return result;
    }
    @RequestMapping(value = "/saveUploadFile",method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object saveUploadFile(HttpServletRequest request,@RequestBody Map<String,String> map )throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
        String id = map.get("id");
        String name = map.get("name");
        String des = map.get("des");
        String html = map.get("html");
        WordEntity wordEntity = new WordEntity();
        wordEntity.setId(Integer.parseInt(id));
        wordEntity.setName(name);
        wordEntity.setDes(des);
        wordEntity.setHtml(html);
//        WordEntity wordEntity = wordTemplateService.queryHtmlbyId(Integer.parseInt(id));
        wordTemplateService.update(wordEntity);
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
//        result.put("data",wordEntity);
        return result;
    }
    @RequestMapping(value = "/uploadFileInfo", method = RequestMethod.GET)
    @ResponseBody
    public Object uploadFileInfo(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        List<KeyValue> list = new LinkedList<>();
        //部署ID
        String id = request.getParameter("id");
        String template_id = request.getParameter("template_id");
        int tmp_id = 0;

        String processInstanceId = request.getParameter("processInstanceId");
        if(null == template_id) {
            OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(id);
//            WordEntity wordEntity = wordTemplateService.queryInfoRel(id);
//            tmp_id = wordEntity.getId();
            tmp_id = oaDeploymentTemplateRelation.getRelationTemplateid();
        }else{
            tmp_id = Integer.parseInt(template_id);
        }
        OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(tmp_id);
//        WordEntity wordEntity = wordTemplateService.queryHtmlbyId(tmp_id);
        //        WordEntity wordEntity = wordTemplateService.queryHtmlbyId(Integer.parseInt(id));
        if(StringUtils.isNotBlank(processInstanceId)){
            Map<String, VariableInstance> stringVariableInstanceMap = runtimeService.getVariableInstances(processInstanceId);
            for (Map.Entry<String, VariableInstance> entry : stringVariableInstanceMap.entrySet()) {
                if(null == entry.getValue() || StringUtils.isBlank(entry.getValue().getTextValue()))continue;
                if(entry.getKey().startsWith("name")) {
                    KeyValue keyValue = new KeyValue(entry.getKey(), entry.getValue().getTextValue());
                    list.add(keyValue);
                }
            }
            result.put("rows",list);
        }
        //获取
        result.put("data",oaContractTemplate);

//        result.put("approvalList",)
        return result;
    }



    @RequestMapping(value = "/uploadFile", method = RequestMethod.GET)
    @ResponseBody
    public Map uploadFile(HttpServletRequest request,
                          @RequestParam(value = "offset", required = true) int offset,
                          @RequestParam(value = "limit", required = true) int limit
                          )throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
        int count = concactTemplateService.count();
        List<OAContractTemplate> oaContractTemplateList = concactTemplateService.list(offset,limit);
//        int count = wordTemplateService.count();
//        List<WordEntity> list = wordTemplateService.queryHtmlTemplate();
        Map<String, Object> result = new HashMap<>();
        result.put("rows",oaContractTemplateList);
        result.put("total",count);
        return result;
    }

    @RequestMapping(value = "/removeModeler", method = RequestMethod.DELETE)
    @ResponseBody
    public Map deleteModeler(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            String id = request.getParameter("id");
            repositoryService.deleteModel(id);
//        log.error("delete guid: "+id);
        }catch (Exception e){
            result.put("result", "failed");
        }
        return result;
    }


    @RequestMapping(value = "/remove", method = RequestMethod.DELETE)
    @ResponseBody
    public Map delete(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            String id = request.getParameter("id");
            repositoryService.deleteDeployment(id, true);
//        log.error("delete guid: "+id);
        }catch (Exception e){
            result.put("result", "failed");
        }
        return result;
    }
    @RequestMapping(value = "/publish", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Map publish(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
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
            result.put("result", "failed");
        }
        return result;
    }

    //查看历史任务详情
    @RequestMapping(value = "htmlHistory",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object htmlHistory(HttpServletRequest request) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
        String processInstanceId = request.getParameter("taskId");
//        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().finished().taskId(taskId).singleResult();
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(historicProcessInstance.getDeploymentId()).singleResult();
        OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(deployment.getId());
        OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(oaDeploymentTemplateRelation.getRelationTemplateid());
//        WordEntity wordEntity = wordTemplateService.queryInfoRel(deployment.getId());
//        wordEntity = wordTemplateService.queryHtmlbyId(wordEntity.getId());


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
                        taskComment.setName(task1.getAssignee());
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
        if(null == loginUser) throw new Exception("用户未登录");
        String taskId = request.getParameter("taskId");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getExecutionId()).singleResult();
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(processInstance.getDeploymentId()).singleResult();

        OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(deployment.getId());
        OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(oaDeploymentTemplateRelation.getRelationTemplateid());
//        WordEntity wordEntity = wordTemplateService.queryInfoRel(deployment.getId());
//        wordEntity = wordTemplateService.queryHtmlbyId(wordEntity.getId());

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
                        taskComment.setName(historicActivityInstance.getAssignee());
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
        if(null == loginUser) throw new Exception("用户未登录");
        List<Deployment> deployments = repositoryService.createDeploymentQuery()
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
//            WordEntity wordEntity = wordTemplateService.queryInfoRel(deployment.getId());
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
        if(null == loginUser) throw new Exception("用户未登录");
        List<Model> list = repositoryService.createModelQuery().listPage(offset, limit);
        long count = repositoryService.createModelQuery().count();
        Map<String, Object> result = new HashMap<>();
        result.put("rows",list);
        result.put("total",count);
        return  result;
    }

}
