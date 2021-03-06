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
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
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
    private OAEnterpriseService oaEnterpriseService;
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
    @Autowired
    private OAPositionRelationService oaPositionRelationService;
    @Autowired
    private OAAttachmentService oaAttachmentService;

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

            OAContractCirculation oaContractCirculation = contractCirculationService.selectBaseByProcessInstanceId(processInstanceId);
            if(null != oaContractCirculation) {
                result.put("contractStatus", oaContractCirculation.getContractStatus());
                result.put("workStatus", oaContractCirculation.getWorkStatus());
                result.put("workDate", oaContractCirculation.getWorkDate());
                result.put("title", oaContractCirculation.getContractName());
                result.put("showCommit", true);
                result.put("buyer", oaContractCirculation.getContractBuyer());
                result.put("seller", oaContractCirculation.getContractSeller());
                result.put("money", oaContractCirculation.getContractMoney());
            }
            List<OAAttachment> oaAttachments = oaAttachmentService.listByProcessId(oaContractCirculation.getProcessInstanceId());
            result.put("download",oaAttachments);

            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(id).singleResult();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
            Collection<FlowElement> flowElements = bpmnModel.getProcesses().get(0).getFlowElements();
            List<FlowElem> flowElems = new LinkedList<>();
            Map<String,String> map = new LinkedHashMap<>();
            Map<String,String> mapUserTask = new LinkedHashMap<>();
            Map<String,String> mapSid = new LinkedHashMap<>();
            for(FlowElement flowElement:flowElements){
                if(flowElement instanceof SequenceFlow){
                    map.put(((SequenceFlow) flowElement).getSourceRef(),((SequenceFlow) flowElement).getTargetRef());
                }
                if(flowElement instanceof UserTask) {
                    mapUserTask.put(flowElement.getId(),flowElement.getName());
                    mapSid.put(flowElement.getName(),flowElement.getId());
                }
            }
            String sid = mapSid.get("提交任务");
            sid = map.get(sid);
            String res = mapUserTask.get(sid);


            OAEnterprise oaEnterprise = oaEnterpriseService.getEnterpriseById(loginUser.getEnterpriseId());
//            List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(oaEnterprise.getCompanyParent());
//            Map<Integer,OAEnterprise> map1 = new LinkedHashMap<>();
//            for(OAEnterprise oaEnterprise1:oaEnterpriseList){
//                map1.put(oaEnterprise1.getEnterpriseId(),oaEnterprise1);
//            }

            Map<Integer,OAEnterprise> map1 = new LinkedHashMap<>();
            if(oaEnterprise.getCompanyParent() == 0){
                map1.put(oaEnterprise.getEnterpriseId(),oaEnterprise);
                List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(oaEnterprise.getEnterpriseId());
                for (OAEnterprise oaEnterprise1 : oaEnterpriseList) {
                    map1.put(oaEnterprise1.getEnterpriseId(), oaEnterprise1);
                }
            }else {
                OAEnterprise oaEnterpriseBak = oaEnterpriseService.getEnterpriseById(oaEnterprise.getCompanyParent());
                map1.put(oaEnterpriseBak.getEnterpriseId(), oaEnterpriseBak);
                List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(oaEnterprise.getCompanyParent());
                for (OAEnterprise oaEnterprise1 : oaEnterpriseList) {
                    map1.put(oaEnterprise1.getEnterpriseId(), oaEnterprise1);
                }
            }


            List<OAUser> oaUserList = userService.listUserLeader(null,res);
            List<OAUser> oaUserListRes = new LinkedList<>();
            for(OAUser oaUser:oaUserList){
                if(map1.containsKey(oaUser.getEnterpriseId())){
                    oaUserListRes.add(oaUser);
                }
            }
            result.put("pms", oaUserListRes);

//            result.put("pms", userService.getPMUser());

            if(null != processInstanceId) {
                Object pm = runtimeService.getVariable(processInstanceId, "pmApprove");
                result.put("pm", pm);
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
                Object object = taskService.getVariable(task.getId(), "taskDefinitionKey");
                Object restart = taskService.getVariable(task.getId(), "init");
                if (object != null || (restart != null && restart.equals("restart"))) {
                    result.put("showCommit", false);
                    Object refuseTask = runtimeService.getVariable(task.getExecutionId(),"refuseTask");
                    result.put("refuse",refuseTask);
                } else {
                    result.put("showCommit", true);
                }
            }
            OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(oaContractCirculation.getTemplateId());
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
            String id = request.getParameter("id");
            String contract = request.getParameter("contract");
            LOGGER.info("合同ID号，参数：{}",contract);
            auditService.audit(new OAAudit(loginUser.getName(),String.format("查询模板以及表单信息")));
            result.put("showCommit", true);
            OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(Integer.parseInt(contract));
            result.put("data",oaContractTemplate);

            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(id).singleResult();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
            Collection<FlowElement> flowElements = bpmnModel.getProcesses().get(0).getFlowElements();
            List<FlowElem> flowElems = new LinkedList<>();
            Map<String,String> map = new LinkedHashMap<>();
            Map<String,String> mapUserTask = new LinkedHashMap<>();
            Map<String,String> mapSid = new LinkedHashMap<>();
            for(FlowElement flowElement:flowElements){
                if(flowElement instanceof SequenceFlow){
                    map.put(((SequenceFlow) flowElement).getSourceRef(),((SequenceFlow) flowElement).getTargetRef());
                }
                if(flowElement instanceof UserTask) {
                    mapUserTask.put(flowElement.getId(),flowElement.getName());
                    mapSid.put(flowElement.getName(),flowElement.getId());
                }
            }
            String sid = mapSid.get("提交任务");
            sid = map.get(sid);
            String res = mapUserTask.get(sid);

            LOGGER.info("loginUser.getEnterpriseId:{}",loginUser.getEnterpriseId());
            OAEnterprise oaEnterprise = oaEnterpriseService.getEnterpriseById(loginUser.getEnterpriseId());
            Map<Integer,OAEnterprise> map1 = new LinkedHashMap<>();
            if(oaEnterprise.getCompanyParent() == 0){
                map1.put(oaEnterprise.getEnterpriseId(),oaEnterprise);
                List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(oaEnterprise.getEnterpriseId());
                for (OAEnterprise oaEnterprise1 : oaEnterpriseList) {
                    map1.put(oaEnterprise1.getEnterpriseId(), oaEnterprise1);
                }
            }else {
                OAEnterprise oaEnterpriseBak = oaEnterpriseService.getEnterpriseById(oaEnterprise.getCompanyParent());
                map1.put(oaEnterpriseBak.getEnterpriseId(), oaEnterpriseBak);
                List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(oaEnterprise.getCompanyParent());
                for (OAEnterprise oaEnterprise1 : oaEnterpriseList) {
                    map1.put(oaEnterprise1.getEnterpriseId(), oaEnterprise1);
                }
            }
            List<OAUser> oaUserList = userService.listUserLeader(null,res);
            List<OAUser> oaUserListRes = new LinkedList<>();
            for(OAUser oaUser:oaUserList){
                if(map1.containsKey(oaUser.getEnterpriseId())){
                    oaUserListRes.add(oaUser);
                }
            }
            result.put("pms", oaUserListRes);
//            result.put("pms", userService.getPMUser());
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
        LOGGER.info("获取合同信息");
        Map<String,KeyValue> map = new LinkedHashMap();
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        //判断当前合同是否自定义合同
//        if(null != oaContractCirculationWithBLOBs.getContractId() && oaContractCirculationWithBLOBs.getAttachmentContent() != null){
//            result.put("download",oaContractCirculationWithBLOBs.getContractId());
//        }
        List<OAAttachment> oaAttachments = oaAttachmentService.listByProcessId(oaContractCirculationWithBLOBs.getProcessInstanceId());
        result.put("download",oaAttachments);
        if(StringUtils.isNotBlank(historicProcessInstance.getId())){
            List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().processInstanceId(historicProcessInstance.getId()).list();
            for (HistoricVariableInstance historicVariableInstance : historicVariableInstances) {
//                if(null == historicVariableInstance || null == historicVariableInstance.getValue() || null == historicVariableInstance.getVariableName())continue;
                if(null != historicVariableInstance && null != historicVariableInstance.getVariableName() && historicVariableInstance.getVariableName().startsWith("name")) {
                    if(null == historicVariableInstance || null == historicVariableInstance.getValue() ){
                        map.put(historicVariableInstance.getVariableName(), new KeyValue(historicVariableInstance.getVariableName(),null));
                    }else {
                        KeyValue keyValue = new KeyValue(historicVariableInstance.getVariableName(), historicVariableInstance.getValue().toString());
                        map.put(historicVariableInstance.getVariableName(), keyValue);
                    }
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
        LOGGER.info("获取合同关键字段信息");
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
        LOGGER.info("获取合同附件信息");
        List<HistoricActivityInstance> hais = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(historicProcessInstance.getId())
                .activityType("userTask")
                .orderByHistoricActivityInstanceStartTime().desc()
                .list();
        LOGGER.info("获取流程信息");
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
                        if(flag) {
                            taskComment.setDescription(comment.getFullMessage());
                        }else{
                            String[] tm = comment.getFullMessage().split(" ");
                            taskComment.setDescription(tm[0]);
                        }
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
                            taskComment.setDescription("待提交");
                        } else {
                            taskComment.setName(historicActivityInstance.getAssignee());
                            taskComment.setCreateTime(historicActivityInstance.getStartTime());
                            taskComment.setDescription("待审批");
                        }
                        taskCommentList.add(taskComment);
                    }else{
                        TaskComment taskComment = new TaskComment();
                        if(hi.getValue() instanceof  Map){
                            Map<String,String> mapComment = (Map)hi.getValue();
                            taskComment.setName(mapComment.get("user"));
                            taskComment.setCreateTime(historicActivityInstance.getStartTime());
                            if (flag) {
                                taskComment.setDescription(mapComment.get("cause"));
                            } else {
                                String[] tm = mapComment.get("cause").split(" ");
                                taskComment.setDescription(tm[0]);
                            }
                        }else {
                            //当前节点没有审批信息，表明处于当前节点用户审批状态下
                            if (historicActivityInstance.getAssignee() == null) {
//                            VariableInstance variableInstance = runtimeService.getVariableInstance(processInstance.getId(), "user");
                                taskComment.setName(variableInstance.getValue().toString());
                                taskComment.setCreateTime(historicActivityInstance.getStartTime());
                                if (flag) {
                                    taskComment.setDescription(hi.getValue().toString());
                                } else {
                                    String[] tm = hi.getValue().toString().split(" ");
                                    taskComment.setDescription(tm[0]);
                                }
                            } else {
                                taskComment.setName(historicActivityInstance.getAssignee());
                                taskComment.setCreateTime(historicActivityInstance.getStartTime());
                                if (flag) {
                                    taskComment.setDescription(hi.getValue().toString());
                                } else {
                                    String[] tm = hi.getValue().toString().split(" ");
                                    taskComment.setDescription(tm[0]);
                                }
                            }
                        }
                        taskCommentList.add(taskComment);
                    }
                }
            }
            result.put("comments",taskCommentList);
            LOGGER.info("获取批注信息");
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
        LOGGER.info("合同详情");
        auditService.audit(new OAAudit(loginUser.getName(),String.format("查看当前任务详情")));
        String taskId = request.getParameter("taskId");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(task.getProcessInstanceId());
        OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(oaContractCirculationWithBLOBs.getTemplateId());
        LOGGER.info("完成合同详情");
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        Map<String,KeyValue> map = new LinkedHashMap();
        //判断当前合同是否自定义合同
//        if(oaContractCirculationWithBLOBs.getContractId() != null && oaContractCirculationWithBLOBs.getAttachmentContent() != null ) {
//            result.put("download", oaContractCirculationWithBLOBs.getContractId());
//        }
        List<OAAttachment> oaAttachments = oaAttachmentService.listByProcessId(oaContractCirculationWithBLOBs.getProcessInstanceId());
        result.put("download",oaAttachments);
        if(StringUtils.isNotBlank(processInstance.getId())){
            Map<String, VariableInstance> stringVariableInstanceMap = runtimeService.getVariableInstances(processInstance.getId());
            for (Map.Entry<String, VariableInstance> entry : stringVariableInstanceMap.entrySet()) {
                if(null!=entry && null != entry.getKey() && entry.getKey().startsWith("name")) {
                    if(null == entry.getValue() || StringUtils.isBlank(entry.getValue().getTextValue())){
                        map.put(entry.getKey(), new KeyValue(entry.getKey(), null));
                    }else {
                        KeyValue keyValue = new KeyValue(entry.getKey(), entry.getValue().getTextValue());
                        map.put(entry.getKey(), keyValue);
                    }
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
        LOGGER.info("关键信息填写");
        result.put("keyword",sb.toString());
        List<TaskComment> taskCommentList = new LinkedList<>();
        boolean flag = false;
        OAUser oaUser = userService.getUserById(loginUser.getId());
        if(null == oaUser.getGroupId() || oaUser.getGroupId() < 1){
            flag = false;
        }else {
            OAGroup oaGroup = groupService.getGroupById(oaUser.getGroupId());
            if(oaGroup == null){
                flag = false;
            }else {
                Map mapGroup = JsonUtils.jsonToMap(oaGroup.getPrivilegeids());
                if (mapGroup.get("attachment") != null && mapGroup.get("attachment").equals("0")) {
                    flag = true;
                }
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
//                        if(flag) {
                            taskComment.setDescription(comment.getFullMessage());
//                        }else{
//                            String[] tm = comment.getFullMessage().split(" ");
//                            taskComment.setDescription(tm[0]);
//                        }
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
                            taskComment.setDescription("待提交");
                        } else {
                            taskComment.setName(historicActivityInstance.getAssignee());
                            taskComment.setCreateTime(historicActivityInstance.getStartTime());
                            taskComment.setDescription("待审批");
                        }
                        taskCommentList.add(taskComment);
                    }else{
                        TaskComment taskComment = new TaskComment();
                        if(object instanceof  Map){
                            Map<String,String> mapComment = (Map)object;
                            taskComment.setName(mapComment.get("user"));
                            taskComment.setCreateTime(historicActivityInstance.getStartTime());
//                            if (flag) {
                                taskComment.setDescription(mapComment.get("cause"));
//                            }else{
//                                String[] tm = mapComment.get("cause").split(" ");
//                                taskComment.setDescription(tm[0]);
//                            }
                        }else {
                            //当前节点没有审批信息，表明处于当前节点用户审批状态下
                            if (historicActivityInstance.getAssignee() == null) {
                                VariableInstance variableInstance = runtimeService.getVariableInstance(processInstance.getId(), "user");
                                taskComment.setName(variableInstance.getValue().toString());
                                taskComment.setCreateTime(historicActivityInstance.getStartTime());
//                                if (flag) {
                                    taskComment.setDescription(object.toString());
//                                } else {
////                                taskComment.setDescription("通过");
//                                    String[] tm = object.toString().split(" ");
//                                    taskComment.setDescription(tm[0]);
//                                }
                            } else {
                                taskComment.setName(historicActivityInstance.getAssignee());
                                taskComment.setCreateTime(historicActivityInstance.getStartTime());
//                                if (flag) {
                                    taskComment.setDescription(object.toString());
//                                } else {
//                                    String[] tm = object.toString().split(" ");
//                                    taskComment.setDescription(tm[0]);
//                                }
                            }
                        }
                        taskCommentList.add(taskComment);
                    }
                }
            }
            result.put("comments",taskCommentList);
            LOGGER.info("批注信息填写");
            result.put("approve_last",false);
            if(processInstance != null) {
                ActivityImpl activity = ((ProcessDefinitionEntity) repositoryService.getProcessDefinition(task.getProcessDefinitionId())).findActivity(processInstance.getActivityId());
                if (null != activity && activity.getProperty("name").toString().contains("核对")) {
                    result.put("approve_last",true);
                }
            }
            OAEnterprise oaEnterprise = oaEnterpriseService.getEnterpriseById(loginUser.getEnterpriseId());
            List<OAPositionRelation> oaPositionRelations = oaPositionRelationService.getByCompanyPosition(null,loginUser.getPosition());
//            List<OAPositionRelation> oaPositionRelations = oaPositionRelationService.getByCompanyPosition(oaEnterprise.getCompanyName(),loginUser.getPosition());
//
//            if(oaPositionRelations.size() == 1){
//                for(OAPositionRelation oaPositionRelation: oaPositionRelations){
//                    if(oaPositionRelation.getHighPositionName().contains("法务")){
//                        oaPositionRelations =  oaPositionRelationService.getByCompanyPosition(null,loginUser.getPosition());
//                    }
//                }
//            }
//            if(null == oaPositionRelations || oaPositionRelations.size() ==0){
//                oaPositionRelations =  oaPositionRelationService.getByCompanyPosition(null,loginUser.getPosition());
//            }
            LinkedHashMap<String,OAUser> oaUserMap = new LinkedHashMap<>();


            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(processInstance.getDeploymentId()).singleResult();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
            Collection<FlowElement> flowElements = bpmnModel.getProcesses().get(0).getFlowElements();
            List<FlowElem> flowElems = new LinkedList<>();
            Map<String,String> mapSeq = new LinkedHashMap<>();
            Map<String,String> mapUserTask = new LinkedHashMap<>();
            Map<String,String> mapSid = new LinkedHashMap<>();
            for(FlowElement flowElement:flowElements){
                if(flowElement instanceof SequenceFlow){
                    LOGGER.info("mapSeq:{} {}",((SequenceFlow) flowElement).getSourceRef(),((SequenceFlow) flowElement).getTargetRef());
                    mapSeq.put(((SequenceFlow) flowElement).getSourceRef(),((SequenceFlow) flowElement).getTargetRef());
                }
                if(flowElement instanceof UserTask) {
                    LOGGER.info("mapUserTask:{} {}",flowElement.getId(),flowElement.getName());
                    mapUserTask.put(flowElement.getId(),flowElement.getName());
                    mapSid.put(flowElement.getName(),flowElement.getId());
                }
            }
            LOGGER.info("res:{}",task.getTaskDefinitionKey());
            String sid = mapSeq.get(task.getTaskDefinitionKey());
            String res = mapUserTask.get(sid);
//            OAEnterprise oaEnterprise = oaEnterpriseService.getEnterpriseById(loginUser.getEnterpriseId());
//            List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(oaEnterprise.getCompanyParent());
//            Map<Integer,OAEnterprise> map1 = new LinkedHashMap<>();
//            for(OAEnterprise oaEnterprise1:oaEnterpriseList){
//                map1.put(oaEnterprise1.getEnterpriseId(),oaEnterprise1);
//            }

            Map<Integer,OAEnterprise> map1 = new LinkedHashMap<>();
            if(oaEnterprise.getCompanyParent() == 0){
                map1.put(oaEnterprise.getEnterpriseId(),oaEnterprise);
                LOGGER.info("map1:{} {}",oaEnterprise.getEnterpriseId(),oaEnterprise.getCompanyName());
                List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(oaEnterprise.getEnterpriseId());
                for (OAEnterprise oaEnterprise1 : oaEnterpriseList) {
                    map1.put(oaEnterprise1.getEnterpriseId(), oaEnterprise1);
                    LOGGER.info("map1:{} {}",oaEnterprise1.getEnterpriseId(),oaEnterprise1.getCompanyName());
                }
            }else {
                OAEnterprise oaEnterpriseBak = oaEnterpriseService.getEnterpriseById(oaEnterprise.getCompanyParent());
                map1.put(oaEnterpriseBak.getEnterpriseId(), oaEnterpriseBak);
                List<OAEnterprise> oaEnterpriseList = oaEnterpriseService.getEnterpriseByParentId(oaEnterprise.getCompanyParent());
                for (OAEnterprise oaEnterprise1 : oaEnterpriseList) {
                    map1.put(oaEnterprise1.getEnterpriseId(), oaEnterprise1);
                }
            }

//            List<OAUser> oaUserList = userService.listUserLeader(null,res);
//            List<OAUser> oaUserListRes = new LinkedList<>();
//            for(OAUser oaUser1:oaUserList){
//                if(map1.containsKey(oaUser1.getEnterpriseId())){
//                    oaUserListRes.add(oaUser1);
//                }
//            }

            for(OAPositionRelation oaPositionRelation:oaPositionRelations) {
                LOGGER.info("OAPositionRelation:{} {}",oaPositionRelation.getHighCompany(),oaPositionRelation.getHighPositionName());
                LOGGER.info("res:{}",res);
                if (null == res || res.contains("法务")) {
                    List<OAUser> userList = userService.listUserLeader(null, "法务");
                    for (OAUser oaUser1 : userList) {
                        LOGGER.info("oaUser1.getUserName():{}",oaUser1.getUserName());
                        oaUserMap.put(oaUser1.getUserName(), oaUser1);
                    }
                }else if (res.contains(oaPositionRelation.getHighPositionName())) {
                    List<OAUser> userList = userService.listUserLeader(oaPositionRelation.getHighCompany(), oaPositionRelation.getHighPositionName());
                    for (OAUser oaUser1 : userList) {
                        LOGGER.info("userList:{} {}",oaUser1.getEnterpriseId(),oaUser1.getUserName());
                        if (map1.containsKey(oaUser1.getEnterpriseId())) {
                            oaUserMap.put(oaUser1.getUserName(), oaUser1);
                        }
                    }
                }
            }
            result.put("leader",oaUserMap.values());
            LOGGER.info("下级审批人信息");
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
