package com.wxm.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxm.entity.*;
import com.wxm.model.*;
import com.wxm.service.*;
import com.wxm.util.*;
import com.wxm.util.exception.OAException;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping(value = "/workflow/process/")
public class ProcessController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessController.class);
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;
    @Value("${contract.template.path}")
    private String contractPath;
    @Value("${openoffice.org.path}")
    private String openOfficePath;
    @Autowired
    private ConcactTemplateService concactTemplateService;
    @Autowired
    private ManagementService managementService;
    @Autowired
    private FormPropertiesService formPropertiesService;

    @Autowired
    private OADeploymentTemplateService oaDeploymentTemplateService;

    @Autowired
    ProcessEngine processEngine;

    @Autowired
    private MailService mailService;

    @Autowired
    private OAAttachmentService oaAttachmentService;

    @Autowired
    private ContractCirculationService contractCirculationService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private TaskProcessService taskProcessService;
    private static String formatSeconds(long seconds) {
        String timeStr = seconds + "秒";
        if (seconds > 60) {
            long second = seconds % 60;
            long min = seconds / 60;
            timeStr = min + "分" + second + "秒";
            if (min > 60) {
                min = (seconds / 60) % 60;
                long hour = (seconds / 60) / 60;
                timeStr = hour + "小时" + min + "分" + second + "秒";
                if (hour > 24) {
                    hour = ((seconds / 60) / 60) % 24;
                    long day = (((seconds / 60) / 60) / 24);
                    timeStr = day + "天" + hour + "小时" + min + "分" + second + "秒";
                }
            }
        }
        return timeStr;
    }


    @RequestMapping(value = "/dashboard", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Object dashboard(HttpServletRequest request){
        //获取 我的待办任务，我的已办任务以及我发起的任务 数量
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        auditService.audit(new OAAudit(loginUser.getName(),String.format("获取首页数据")));
        Map<String, Object> result = new HashMap<>();
        try{
            long size = taskService.createTaskQuery().taskAssignee(loginUser.getName()).count();
            result.put("myPending",size);

            List<TaskInfo> taskInfos = new LinkedList<>();
            List<Task> list=taskService.createTaskQuery().taskAssignee(loginUser.getName())
                    .listPage(0,10);
            for(Task task:list){
                TaskInfo taskInfo = new TaskInfo();
                VariableInstance variableInstance = runtimeService.getVariableInstance(task.getExecutionId(),"title");
                if(null != variableInstance) {
                    taskInfo.setTitle(variableInstance.getTextValue());
                }
                taskInfo.setTimestamp(task.getCreateTime());
                taskInfo.setProcessDefinitionId(task.getProcessDefinitionId());
                taskInfos.add(taskInfo);
            }
            result.put("pendingList",taskInfos);

            if(loginUser.getName().equals("admin")){
                size = historyService.createHistoricProcessInstanceQuery()
                        .variableValueEquals("instanceStatus","completed")
                        .count();
                result.put("myComplete", size);
            }else {
                size = historyService.createHistoricProcessInstanceQuery()
                        .involvedUser(loginUser.getName()).variableValueEquals("instanceStatus","completed")
                        .count();
                result.put("myComplete", size);


            }

            size = historyService.createHistoricProcessInstanceQuery().startedBy(loginUser.getName()).count();
            result.put("initiator",size);

            taskInfos = new LinkedList<>();
            List<HistoricProcessInstance> historicProcessInstanceList = historyService.createHistoricProcessInstanceQuery()
                    .startedBy(loginUser.getName()).orderByProcessInstanceStartTime().desc()
                    .listPage(0,10);
            for(HistoricProcessInstance historicProcessInstance:historicProcessInstanceList){
                TaskInfo taskInfo = new TaskInfo();
                ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
                        .processInstanceId(historicProcessInstance.getId())//使用流程实例ID查询
                        .singleResult();
                if(pi == null){
                    taskInfo.setAssignee("审批结束");
                    HistoricVariableInstance historicVariableInstance =historyService.createHistoricVariableInstanceQuery().variableName("title").processInstanceId(historicProcessInstance.getId()).singleResult();
                    if(null != historicVariableInstance) {
                        taskInfo.setTitle(historicVariableInstance.getValue().toString());
                    }
                }else{
                    Task task = taskService.createTaskQuery().processInstanceId(historicProcessInstance.getId()).singleResult();
                    taskInfo.setAssignee(task.getAssignee());

                    taskInfo.setDuringTime(formatSeconds((new Date().getTime() - task.getCreateTime().getTime())/1000));
                    VariableInstance variableInstance = runtimeService.getVariableInstance(historicProcessInstance.getId(),"title");
                    if(null != variableInstance) {
                        taskInfo.setTitle(variableInstance.getTextValue());
                    }

                }


                taskInfo.setTimestamp(historicProcessInstance.getStartTime());
                taskInfo.setProcessDefinitionId(historicProcessInstance.getProcessDefinitionId());
                taskInfos.add(taskInfo);
            }
            result.put("initiatorList",taskInfos);

            result.put("result","success");
        }catch (Exception e){
            result.put("result","failed");
            LOGGER.info("异常",e);
        }
        return result;
    }


    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Object download(
            @RequestParam(value = "processId", required = false, defaultValue = "") String processId,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        try {
            OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processId);
            byte[] bytes = oaContractCirculationWithBLOBs.getContractPdf();
            response.setContentType("application/x-download");
//        String codedfilename = MimeUtility.encodeText( new String((oaContractCirculationWithBLOBs.getContractName()+".pdf").getBytes("UTF-8"), "ISO-8859-1"));
//        String codedfilename = MimeUtility.encodeText(new String((oaContractCirculationWithBLOBs.getContractName()+".pdf").getBytes(), "GB2312"),"GB2312","B");
//        strText = MimeUtility.encodeText(new String(strText.getBytes(), "GB2312"), "GB2312", "B");
            String codedfilename = java.net.URLEncoder.encode(oaContractCirculationWithBLOBs.getContractName() + ".pdf", "UTF-8");
            //        String codedfilename = oaContractCirculationWithBLOBs.getContractName()+".pdf";
            response.setHeader("Content-Disposition", "attachment;filename=" + codedfilename);
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
        }catch (Exception e){
            LOGGER.info("下载异常",e);
        }
        return null;
    }

    //modeler预览
    @RequestMapping("/modelerReviewInfo")
    public Object modelerReviewInfo(HttpServletRequest request,HttpServletResponse response) throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        String id = request.getParameter("modelerId");
        //获取模型
        Model modelData = repositoryService.getModel(id);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            return ToWeb.buildResult().status(Status.FAIL)
                    .msg("模型数据为空，请先设计流程并成功保存，再进行发布。");
        }

        JsonNode modelNode = new ObjectMapper().readTree(bytes);

        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
//        BpmnModel bpmnModel = (BpmnModel)repositoryService.createModelQuery().modelId(modelId).latestVersion().singleResult();
        Collection<FlowElement> flowElements = bpmnModel.getProcesses().get(0).getFlowElements();
        List<FlowElem> flowElems = new LinkedList<>();
        for(FlowElement flowElement:flowElements){
            if(flowElement instanceof UserTask && StringUtils.isNotBlank(((UserTask) flowElement).getAssignee() )) {
                FlowElem flowElem = new FlowElem(flowElement.getName(),((UserTask) flowElement).getAssignee());
                flowElems.add(flowElem);
            }
        }
        result.put("flows",flowElems);
        return result;

    }
    @RequestMapping("/modelerPreviewImage")
    public void modelerPreviewImage(HttpServletRequest request,HttpServletResponse response) throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        String id = request.getParameter("modelerId");
        //获取模型
        Model modelData = repositoryService.getModel(id);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
        if (bytes != null) {
            JsonNode modelNode = new ObjectMapper().readTree(bytes);
            BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);

            ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
            List<String> highLightedActivitis = new ArrayList<String>();
            InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitis, new ArrayList<String>(), "宋体", "宋体", "宋体", null, 1.0D);
            //单独返回流程图，不高亮显示
            //        InputStream imageStream = diagramGenerator.generatePngDiagram(bpmnModel);
            // 输出资源内容到相应对象
            byte[] b = new byte[1024];
            int len;
            while ((len = imageStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
        }


    }
    //预览
    @RequestMapping("/previewInfo")
    public Object previewInfo(HttpServletRequest request,HttpServletResponse response) throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        String docId = request.getParameter("depId");
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(docId).singleResult();
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
//        BpmnModel bpmnModel = (BpmnModel)repositoryService.createModelQuery().modelId(modelId).latestVersion().singleResult();
        Collection<FlowElement> flowElements = bpmnModel.getProcesses().get(0).getFlowElements();
        List<FlowElem> flowElems = new LinkedList<>();
        for(FlowElement flowElement:flowElements){
            if(flowElement instanceof UserTask && StringUtils.isNotBlank(((UserTask) flowElement).getAssignee() )) {
                FlowElem flowElem = new FlowElem(flowElement.getName(),((UserTask) flowElement).getAssignee());
                flowElems.add(flowElem);
            }
        }
        result.put("flows",flowElems);
        return result;

    }
    @RequestMapping("/previewImage")
    public void preview(HttpServletRequest request,HttpServletResponse response) throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        String docId = request.getParameter("depId");

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(docId).singleResult();
        Map<String, Object> result = new HashMap<>();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        List<String> highLightedActivitis = new ArrayList<String>();
        InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitis, new ArrayList<String>(), "宋体", "宋体", "宋体", null, 1.0D);
        //单独返回流程图，不高亮显示
        //        InputStream imageStream = diagramGenerator.generatePngDiagram(bpmnModel);
        // 输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len;
        while ((len = imageStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }
    @RequestMapping("/queryProPlan")
    public void queryProPlan(HttpServletRequest request,HttpServletResponse response) throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        String taskID = request.getParameter("TaskId");
        String processInstanceId = "";
        try {
            if (!StringUtils.isBlank(taskID)) {
                HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskID).singleResult();
                processInstanceId = historicTaskInstance.getProcessInstanceId();
            } else {
                processInstanceId = request.getParameter("processInstanceId");
            }
            HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            //获取流程图
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
            Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);
            ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
            //高亮环节id集合
            List<String> highLightedActivitis = new ArrayList<String>();
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
                    .processInstanceId(processInstanceId)//使用流程实例ID查询
                    .singleResult();

            //流程结束了
            if (pi == null) {
                highLightedActivitis.add(processInstance.getEndActivityId());
            } else {

                ProcessInstance processInstanceRun = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                Task task = taskService.createTaskQuery().processInstanceId(processInstanceRun.getProcessInstanceId()).singleResult();
                String taskDefinitionKey = task.getTaskDefinitionKey();
                if (StringUtils.isNotBlank(taskDefinitionKey)) {
                    highLightedActivitis.add(taskDefinitionKey);
                } else {
                    highLightedActivitis.add(processInstanceRun.getActivityId());
                }
            }

        //        commandContext.getProcessEngineConfiguration().getProcessDiagramGenerator().generateDiagram(bpmnModel,"png", highLightedActivitis,new ArrayList<String>(),"宋体","宋体","宋体",null,1.0D);
            InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitis, new ArrayList<String>(), "宋体", "宋体", "宋体", null, 1.0D);
            //单独返回流程图，不高亮显示
        //        InputStream imageStream = diagramGenerator.generatePngDiagram(bpmnModel);
            // 输出资源内容到相应对象
            byte[] b = new byte[1024];
            int len;
            while ((len = imageStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
        }catch (Exception e){
            LOGGER.error("异常",e);
        }
    }


    //处理审批跳转到已审批人
    @RequestMapping(value = "jump", method = RequestMethod.POST)
    @ResponseBody
    public Object jump(HttpServletRequest request,@RequestBody Map<String,String> map)throws  Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        String processInstanceId = map.get("processInstanceId");
        String info = "";
        if(StringUtils.isNotBlank(processInstanceId)) {
            try{
                String deploymentID = map.get("id");
//                OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(deploymentID);
                OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processInstanceId);
//                List<OAFormProperties> oaFormPropertiesList = formPropertiesService.listByTemplateId(oaContractCirculationWithBLOBs.getTemplateId());
//                for (OAFormProperties oaFormProperties : oaFormPropertiesList) {
//                    String md5 = oaFormProperties.getFieldMd5();
//                    String type = oaFormProperties.getFieldType();
//                    String length = oaFormProperties.getFieldValid();
//                    if( StringUtils.isBlank(md5) ||  StringUtils.isBlank(type) ||  StringUtils.isBlank(length)) continue;
//                    md5 = md5.trim();
//                    type = type.trim();
//                    length = length.trim();
//                    String value = map.get(md5).toString();
//                    if( StringUtils.isBlank(value))continue;
//                    if (value.length() > Integer.parseInt(length)) {
//                        info = oaFormProperties.getFieldMd5() + " 字段长度过长";
//                        result.put("info", info);
//                        break;
//                    }
//                    if (type.equals("D")) {
//                        if (!ValidType.isNumeric(value)) {
//                            info = oaFormProperties.getFieldMd5() + " 字段类型错误";
//                            result.put("info", info);
//                            break;
//                        }
//                    }
//                }
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();

                String attachmentRefuse = map.get("attachmentRefuse");
                runtimeService.setVariable(task.getProcessInstanceId(), task.getId(), "重新提交 "+attachmentRefuse);
                result.put("result", "success");
                String taskDefinitionKey = taskService.getVariable(task.getId(), "taskDefinitionKey").toString();
                taskService.setVariable(task.getId(), "taskDefinitionKey",null);
                taskProcessService.jump(taskDefinitionKey, task.getProcessInstanceId());
                map.put("init","");
                runtimeService.setVariables(processInstance.getProcessInstanceId(),map);
                oaContractCirculationWithBLOBs.setContractHtml(map.get("html"));
                contractCirculationService.update(oaContractCirculationWithBLOBs);
            }catch (Exception e){
                LOGGER.error("异常",e);
                result.put("result","failed");
            }
        }
        return result;
    }

    //拒绝任务到发起人，并记录当前节点，后续可以直接返回
    @RequestMapping(value = "reject", method = RequestMethod.POST)
    @ResponseBody
    public Object reject(HttpServletRequest request,@RequestBody Map<String,String> map)throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        String cause = map.get("cause");
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        String taskId = map.get("id");

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String taskDefinitionKey = task.getTaskDefinitionKey();
//        runtimeService.setVariable(task.getProcessInstanceId(), "refuseTask", "拒绝" );
        runtimeService.setVariable(task.getProcessInstanceId(), "refuseTask", cause );
        if(StringUtils.isNotBlank(cause)) {
            runtimeService.setVariable(task.getProcessInstanceId(), taskId, "拒绝:  " + cause);
        }else{
            runtimeService.setVariable(task.getProcessInstanceId(), taskId, "拒绝" );
        }
        List<HistoricTaskInstance> historicTaskInstanceList = historyService.createHistoricTaskInstanceQuery()
                .finished()
                .processInstanceId(task.getProcessInstanceId())
                .orderByTaskCreateTime().asc().listPage(0,1);
        if(null != historicTaskInstanceList){
            for(HistoricTaskInstance task1 : historicTaskInstanceList){
                if(task1.getAssignee() == null){
                    taskProcessService.jump(task1.getTaskDefinitionKey(), task.getProcessInstanceId());
                    runtimeService.setVariable(task.getProcessInstanceId(), "init", "start");
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom("xxxx@qq.com");
                    message.setTo("xxxx@qq.com");
                    message.setSubject("流程审批");
                    message.setText("简单邮件内容+url");
                    mailService.send(message);

                    task = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
                    taskService.setVariable(task.getId(),"taskDefinitionKey",taskDefinitionKey);
                    runtimeService.setVariable(task.getProcessInstanceId(),"taskDefinitionKeyShow",taskDefinitionKey);
                    break;
                }
            }
        }
        return result;
    }

    //开始发起任务
    @RequestMapping(value = "start", method = RequestMethod.POST)
    @ResponseBody
    public Object start(HttpServletRequest request, HttpServletResponse response,@RequestBody Map<String,String> map) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        String info = "";
        String processInstanceId = map.get("processInstanceId");
        String workStatus = map.get("workStatus");
        String custom = map.get("custom");
        String contract = map.get("contract");
        String contractName = map.get("contractName");
        OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(Integer.parseInt(contract));
        if(StringUtils.isNotBlank(processInstanceId)) {//草稿提交
            try{
                String deploymentID = map.get("id");
                OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processInstanceId);
//                List<OAFormProperties> oaFormPropertiesList = formPropertiesService.listByTemplateId(oaContractCirculationWithBLOBs.getTemplateId());
//                for (OAFormProperties oaFormProperties : oaFormPropertiesList) {
//                    String md5 = oaFormProperties.getFieldMd5();
//                    String type = oaFormProperties.getFieldType();
//                    String length = oaFormProperties.getFieldValid();
//                    if( StringUtils.isBlank(md5) ||  StringUtils.isBlank(type) ||  StringUtils.isBlank(length)) continue;
//                    md5 = md5.trim();
//                    type = type.trim();
//                    length = length.trim();
//                    String value = map.get(md5).toString();
//                    if( StringUtils.isBlank(value))continue;
//                    if (value.length() > Integer.parseInt(length)) {
//                        info = oaFormProperties.getFieldMd5() + " 字段长度过长";
//                        result.put("info", info);
//                        break;
//                    }
//                    if (type.equals("D")) {
//                        if (!ValidType.isNumeric(value)) {
//                            info = oaFormProperties.getFieldMd5() + " 字段类型错误";
//                            result.put("info", info);
//                            break;
//                        }
//                    }else if(type.equals("YYYY-MM-DD")){
//                        if (!ValidType.isDate(value)) {
//                            info = oaFormProperties.getFieldMd5() + " 字段类型错误";
//                            result.put("info", info);
//                        }
//                    }
//                }
                String index = map.get("index");
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                if(index.equals("1")){
                    Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentID).singleResult();
                    Date nowTime = new Date();
                    SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
                    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
                    taskService.addComment(task.getId(), processInstance.getId(), "提交");
                    taskService.complete(task.getId());
                    map.put("init","");
                    if(StringUtils.isNotBlank(workStatus) && workStatus.equals("true")) {
                        map.put("contractStatus","1");
                    }else{
                        map.put("contractStatus","0");
                    }
                    runtimeService.setVariables(processInstance.getProcessInstanceId(),map);
                    //附件处理
//                    if(oaContractTemplate.getTemplateName().contains("自定义")) {
                    OAContractCirculationWithBLOBs contractCirculationWithBLOBs = new OAContractCirculationWithBLOBs();
                    contractCirculationWithBLOBs.setContractId(oaContractCirculationWithBLOBs.getContractId());
                    if (StringUtils.isNotBlank(custom)) {
//                        OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processInstance.getProcessInstanceId());
                        // 获取word文件流，custom文件名称
                        String filePf = contractPath + custom;
                        byte[] word = FileByte.getByte(filePf);
                        contractCirculationWithBLOBs.setContractPdf(word);
                    }
                    if (StringUtils.isNotBlank(workStatus) && workStatus.equals("true")) {
                        contractCirculationWithBLOBs.setWorkStatus(1);
                    } else {
                        contractCirculationWithBLOBs.setWorkStatus(0);
                    }
                    contractCirculationWithBLOBs.setDescription("custom");
                    contractCirculationService.update(contractCirculationWithBLOBs);
//                    }
//                    OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processInstance.getProcessInstanceId());
//                    OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = new OAContractCirculationWithBLOBs();
//                    oaContractCirculationWithBLOBs.setTemplateId(oaDeploymentTemplateRelation.getRelationTemplateid());
//                    oaContractCirculationWithBLOBs.setContractName(deployment.getName() + "-" + time.format(nowTime));
//                    oaContractCirculationWithBLOBs.setProcessInstanceId(processInstance.getId());
//                    oaContractCirculationWithBLOBs.setContractHtml(map.get("html"));
//                    contractCirculationService.update(oaContractCirculationWithBLOBs);

                }else{
                    runtimeService.setVariables(processInstance.getProcessInstanceId(),map);
                }

            }catch (Exception e){
                result.put("result","failed");
            }
        }else {
            try {
                String deploymentID = map.get("id");
                String index = map.get("index");


//                OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(deploymentID);
//                List<OAFormProperties> oaFormPropertiesList = formPropertiesService.listByTemplateId(oaDeploymentTemplateRelation.getRelationTemplateid());
                List<OAFormProperties> oaFormPropertiesList = formPropertiesService.listByTemplateId(Integer.parseInt(contract));
//                WordEntity wordEntity = wordTemplateService.queryInfoRel(deploymentID);
//                List<WordTemplateField> list = wordTemplateFieldService.getWordTemplateFieldByTemplateId(wordEntity.getId());
//                for (OAFormProperties oaFormProperties : oaFormPropertiesList) {
//                    String md5 = oaFormProperties.getFieldMd5();
//                    String type = oaFormProperties.getFieldType();
//                    String length = oaFormProperties.getFieldValid();
//
//                    if (StringUtils.isBlank(md5) || StringUtils.isBlank(type) || StringUtils.isBlank(length)) continue;
//                    md5 = md5.trim();
//                    type = type.trim();
//                    length = length.trim();
//                    String value = map.get(md5).toString();
//                    if (StringUtils.isBlank(value)) continue;
//                    if (value.length() > Integer.parseInt(length)) {
//                        info = oaFormProperties.getFieldMd5() + " 字段长度过长";
//                        result.put("info", info);
//                        break;
//                    }
//                    if (type.equals("D")) {
//                        if (!ValidType.isNumeric(value)) {
//                            info = oaFormProperties.getFieldMd5() + " 字段类型错误";
//                            result.put("info", info);
//                            break;
//                        }
//                    }else if(type.equals("YYYY-MM-DD")){
//                        if (!ValidType.isDate(value)) {
//                            info = oaFormProperties.getFieldMd5() + " 字段类型错误";
//                            result.put("info", info);
//                        }
//                    }
//                }
                // 查找流程定义
                ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deploymentID).singleResult();

                Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentID).singleResult();
                Map<String, Object> mapInfo = new LinkedHashMap<>();
                mapInfo.put("user", loginUser.getName());
                mapInfo.put("init", "start");
                Date nowTime = new Date();
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
                if(StringUtils.isBlank(contractName)) {
                    mapInfo.put("title", deployment.getName() + "-" + time.format(nowTime));
                }else{
                    mapInfo.put("title", contractName);
                }
                mapInfo.put("timeStamp", new Date().getTime());
                if(StringUtils.isNotBlank(workStatus) && workStatus.equals("true")) {
                    mapInfo.put("contractStatus",1);
                }else{
                    mapInfo.put("contractStatus",0);
                }
                mapInfo.putAll(map);
                identityService.setAuthenticatedUserId(loginUser.getName());
                ProcessInstance processInstance = runtimeService.startProcessInstanceById(pd.getId(), mapInfo);
                OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = new OAContractCirculationWithBLOBs();
                oaContractCirculationWithBLOBs.setUserId(loginUser.getId());
                oaContractCirculationWithBLOBs.setTemplateId(Integer.parseInt(contract));
                if(StringUtils.isBlank(contractName)) {
                    oaContractCirculationWithBLOBs.setContractName(deployment.getName() + "-" + time.format(nowTime));
                }else{
                    oaContractCirculationWithBLOBs.setContractName(contractName);
                }

                oaContractCirculationWithBLOBs.setProcessInstanceId(processInstance.getId());
                oaContractCirculationWithBLOBs.setContractStatus("start");
                oaContractCirculationWithBLOBs.setCreateTime(new Date());
                if(StringUtils.isNotBlank(workStatus) && workStatus.equals("true")) {
                    oaContractCirculationWithBLOBs.setWorkStatus(1);
                }else{
                    oaContractCirculationWithBLOBs.setWorkStatus(0);
                }
                //自定义模板处理
//                if(oaContractTemplate.getTemplateName().contains("自定义")) {
                if (StringUtils.isNotBlank(custom)) {
                    oaContractCirculationWithBLOBs.setDescription("custom");
                    // 获取word文件流
                    String filePf = contractPath + custom;
                    byte[] word = FileByte.getByte(filePf);
                    oaContractCirculationWithBLOBs.setContractPdf(word);
                }
//                }
                if(null != map.get("html")) {
                    oaContractCirculationWithBLOBs.setDescription("template");
                    oaContractCirculationWithBLOBs.setContractHtml(map.get("html"));
                }
                contractCirculationService.insert(oaContractCirculationWithBLOBs);
                if (index.equals("1")) {
                    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
                    taskService.addComment(task.getId(), processInstance.getId(), "提交");
                    taskService.complete(task.getId());
                    runtimeService.setVariable(processInstance.getProcessInstanceId(), "init", "");
                }
            } catch (Exception e) {
                result.put("result", "failed");
            }
        }
        return result;
    }

    private String fillValue(String processInstancesId,StringBuilder text){

        Pattern pattern = Pattern.compile("<input([\\s\\S]*?)>");
        Matcher matcher = pattern.matcher(text);
        Map<String,String> map = new LinkedHashMap<>();
        while(matcher.find()) {
            String tmp = matcher.group();
            String name = tmp.substring(tmp.indexOf("id=")+4,tmp.indexOf(">")-1);
            map.put(name,tmp);
        }
//        for(String str : set){
//
//            int start = text.indexOf(str);
//            text.replace(start,start+str.length(),"hello");
//        }

        List<HistoricVariableInstance> historicVariableInstanceList =  historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstancesId).list();
        for(HistoricVariableInstance historicVariableInstance : historicVariableInstanceList){
            if(historicVariableInstance.getVariableName().contains("name_")) {
                int size = text.indexOf(historicVariableInstance.getVariableName());
                if (size > 0 && historicVariableInstance.getValue() != null && StringUtils.isNotBlank(historicVariableInstance.getValue().toString())) {
                    String inputValue = map.get(historicVariableInstance.getVariableName());
                    if(null == text || inputValue == null) continue;
                    int start = text.indexOf(inputValue);
                    text.replace(start,start+inputValue.length(),String.format("<u>%s</u>",historicVariableInstance.getValue().toString()));
                }
            }
        }
        return text.toString();
    }
    //完成任务
    @RequestMapping(value = "complete", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object complete(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        String taskId = request.getParameter("id");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
//利用任务对象，获取流程实例id
        String processInstancesId = task.getProcessInstanceId();
//         Authentication.setAuthenticatedUserId("cmc"); // 添加批注时候的审核人，通常应该从session获取
        taskService.addComment(taskId, processInstancesId, "同意");
        taskService.complete(taskId);

        ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
                .processInstanceId(processInstancesId)//使用流程实例ID查询
                .singleResult();
//        task = taskService.createTaskQuery().processInstanceId(processInstancesId).singleResult();
        OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processInstancesId);
        if(pi != null) {
            ActivityImpl activity = ((ProcessDefinitionEntity) repositoryService.getProcessDefinition(task.getProcessDefinitionId())).findActivity(pi.getActivityId());
//            if (null != activity && activity.getProperty("name").equals("归档")) {
//                //归档后 用户可以查
//
//            }
            if (null != activity && activity.getProperty("name").equals("核对")) {
            //归档后 用户可以查
                runtimeService.setVariable(processInstancesId,"instanceStatus","completed");
                HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstancesId).variableName("title").singleResult();
//            VariableInstance variableInstance = runtimeService.getVariableInstance(processInstancesId,"title");
                String html = oaContractCirculationWithBLOBs.getContractHtml();
                StringBuilder sb = new StringBuilder(html.length() + 300);
                sb.append("<!DOCTYPE html>");
                sb.append("<head>");
                sb.append("<title>");
                sb.append(historicVariableInstance.getValue().toString());
                sb.append("</title>");
                sb.append(" <META HTTP-EQUIV=\"CONTENT-TYPE\" CONTENT=\"text/html; charset=gb2312\"> ");
                sb.append("<body>");
                sb.append(html);
                sb.append("</body></html>");

                String data = fillValue(processInstancesId, sb);
                try {
//                String path = PropertyUtil.getValue("contract.template.path");
                    String fileHtml = contractPath + historicVariableInstance.getValue().toString() + ".html";
                    String filePf = contractPath + historicVariableInstance.getValue().toString() + ".pdf";

//                OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(fileHtml),"UTF-8");
//                BufferedWriter writer=new BufferedWriter(write);
//                writer.write(data);
//                writer.close();
                    PrintStream printStream = new PrintStream(new FileOutputStream(fileHtml));
                    printStream.println(data);
                    //转换成pdf文件
                    File htmlFile = Word2Html.html2pdf(fileHtml, openOfficePath);
                    // 获取pdf文件流
                    byte[] pdf = FileByte.getByte(filePf);
                    oaContractCirculationWithBLOBs.setContractStatus("completed");
                    // HTML文件字符串
                    oaContractCirculationWithBLOBs.setContractPdf(pdf);
                    contractCirculationService.update(oaContractCirculationWithBLOBs);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }else{
            //合同状态 为完成状态
//            oaContractCirculationWithBLOBs.setContractStatus("completed");
//            contractCirculationService.update(oaContractCirculationWithBLOBs);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        return result;
    }

    //流程定义查询,@PathVariable("start") int start,@PathVariable("offset",int offset)
    @RequestMapping(value = "process", method = RequestMethod.POST)
    @ResponseBody
    public void ProcessDef(HttpServletRequest request) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        long size = repositoryService.createProcessDefinitionQuery().count();
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion().asc()
                .listPage(0, 10);
        if (list != null && list.size() > 0) {
            for (ProcessDefinition processDefinition : list) {
                System.out.println("流程定义ID:" + processDefinition.getId());//流程定义的key+版本+随机生成数
                System.out.println("流程定义名称:" + processDefinition.getName());//对应HelloWorld.bpmn文件中的name属性值
                System.out.println("流程定义的key:" + processDefinition.getKey());//对应HelloWorld.bpmn文件中的id属性值
                System.out.println("流程定义的版本:" + processDefinition.getVersion());//当流程定义的key值相同的情况下，版本升级，默认从1开始
                System.out.println("资源名称bpmn文件:" + processDefinition.getResourceName());
                System.out.println("资源名称png文件:" + processDefinition.getDiagramResourceName());
                System.out.println("部署对象ID:" + processDefinition.getDeploymentId());
                System.out.println("################################");
            }
        }
    }

    //获取该人员代办任务
    @RequestMapping(value = "process", method = RequestMethod.GET)
    @ResponseBody
    public Object findTaskByName(@RequestParam(value = "user", required = false) String user,
                                 @RequestParam(value = "offset", required = true) int offset,
                                 @RequestParam(value = "limit", required = true) int limit,
                                 HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }

        List<TaskInfo> taskInfos = new LinkedList<>();
        List<Task> list = taskService.createTaskQuery()// 创建任务查询对象
                .orderByTaskCreateTime().desc()
                .taskAssignee(loginUser.getName())// 指定个人认为查询，指定办理人
                .listPage(offset,limit);

        long size = taskService.createTaskQuery().taskAssignee(loginUser.getName()).count();
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                ProcessDefinition pf = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
                Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(pf.getDeploymentId()).singleResult();
                TaskInfo taskInfo = new TaskInfo();
                taskInfo.setId(task.getId());
                taskInfo.setName(deployment.getName());
                taskInfo.setTimestamp(task.getCreateTime());
                taskInfo.setAssignee(task.getAssignee());
                VariableInstance variableInstance = runtimeService.getVariableInstance(task.getExecutionId(),"title");
                if(null != variableInstance) {
                    taskInfo.setTitle(variableInstance.getTextValue());
                }
                taskInfo.setProcessInstanceId(task.getProcessInstanceId());
                taskInfo.setExecutionId(task.getExecutionId());
                taskInfo.setProcessDefinitionId(task.getProcessDefinitionId());
                taskInfos.add(taskInfo);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("rows", taskInfos);
        result.put("total", size);
        return result;
    }

    //归档任务查询 获取该员工历史任务,参与过的任务
    @RequestMapping(value = "processHistory", method = RequestMethod.GET)
    @ResponseBody
    public Object findHistoryTaskByName(@RequestParam(value = "user", required = false) String user,
                                        @RequestParam(value = "offset", required = true ,defaultValue= "0" ) int offset,
                                        @RequestParam(value = "limit", required = true,defaultValue= "10" ) int limit,
                                        @RequestParam(value = "title", required = false  ) String title,
                                        @RequestParam(value = "contractId", required = false  ) String contractId,
                                        HttpServletRequest request)throws Exception {

        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser){
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }

        List<HistoricProcessInstance> listProcess = null;
        long size = 0;
        List<TaskInfo> taskInfos = new LinkedList<>();
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery();
        if(StringUtils.isNotBlank(title)) {
            historicProcessInstanceQuery =historicProcessInstanceQuery.variableValueLike("title", "%"+title+"%");
        }
        if(loginUser.getName().equals("admin")){
            if(StringUtils.isNotBlank(user)){
                historicProcessInstanceQuery =historicProcessInstanceQuery.involvedUser(user);
            }
            listProcess = historicProcessInstanceQuery
                    .variableValueEquals("instanceStatus","completed")
//                .variableValueEquals("user",loginUser.getName())
                    .orderByProcessInstanceStartTime().desc().listPage(offset,limit);
            size = historicProcessInstanceQuery
                    .variableValueEquals("instanceStatus","completed")
//                .variableValueEquals("user",loginUser.getName())
                    .count();
        }else{
            listProcess = historicProcessInstanceQuery
                    .involvedUser(loginUser.getName()).variableValueEquals("instanceStatus","completed")
//                .variableValueEquals("user",loginUser.getName())
                    .orderByProcessInstanceStartTime().desc().listPage(offset,limit);
            size = historicProcessInstanceQuery
                    .involvedUser(loginUser.getName()).variableValueEquals("instanceStatus","completed")
//                .variableValueEquals("user",loginUser.getName())
                    .count();
        }

        for(HistoricProcessInstance historicProcessInstance : listProcess){
            historicProcessInstance.getStartTime();
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(historicProcessInstance.getDeploymentId()).singleResult();
            TaskInfo taskInfo = new TaskInfo();
            taskInfos.add(taskInfo);
            List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery().processInstanceId(historicProcessInstance.getId()).list();
            if (list != null && list.size() > 0) {
                for (HistoricVariableInstance h : list) {

                    if(h.getVariableName().equals("title")){
                        taskInfo.setTitle(h.getValue().toString());
                    }else if(h.getVariableName().equals("user")){
                        taskInfo.setStarter(h.getValue().toString());
                    }else if(h.getVariableName().equals("contractStatus")){
                        taskInfo.setWorkStatus(Integer.parseInt(h.getValue().toString()));
                    }
                }
            }

            taskInfo.setId(historicProcessInstance.getId());
            taskInfo.setName(deployment.getName());
            taskInfo.setTimestamp(historicProcessInstance.getStartTime());
//            taskInfo.setAssignee(task.getAssignee());
            taskInfo.setProcessInstanceId(historicProcessInstance.getId());
            taskInfo.setExecutionId(historicProcessInstance.getId());
            taskInfo.setProcessDefinitionId(historicProcessInstance.getProcessDefinitionId());

        }
        Map<String, Object> result = new HashMap<>();
        result.put("rows", taskInfos);
        result.put("total", size);
        return result;
    }
    private Iterator<FlowElement> findFlow(String processDefId) {
        try {

            List<ProcessDefinition> lists = repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionId(processDefId)
                    .orderByProcessDefinitionVersion().desc().list();
            ProcessDefinition processDefinition = lists.get(0);
            processDefinition.getCategory();
            String resourceName = processDefinition.getResourceName();
            InputStream inputStream = repositoryService.getResourceAsStream(
                    processDefinition.getDeploymentId(), resourceName);
            BpmnXMLConverter converter = new BpmnXMLConverter();
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);
            BpmnModel bpmnModel = converter.convertToBpmnModel(reader);
            Process process = bpmnModel.getMainProcess();
            Collection<FlowElement> elements = process.getFlowElements();
            Iterator<FlowElement> iterator = elements.iterator();
            return iterator;
        }catch (Exception e){

        }
        return null;
    }

    //查询我提交的任务状态

    @RequestMapping(value = "commitedTask", method = RequestMethod.GET,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object commitedTask(HttpServletRequest request,
                               @RequestParam(value = "offset", required = true ,defaultValue= "0" ) int offset,
                               @RequestParam(value = "limit", required = true,defaultValue= "10" ) int limit
                               ) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        List<ProInstance> processInstanceList = new LinkedList<>();
        List<HistoricProcessInstance> historicProcessInstanceList = historyService.createHistoricProcessInstanceQuery()
                .orderByProcessInstanceStartTime().desc()
                .startedBy(loginUser.getName())
                .listPage(offset,limit);
        long count = historyService.createHistoricProcessInstanceQuery().startedBy(loginUser.getName()).count();
        for(HistoricProcessInstance processInstance : historicProcessInstanceList){
            //此处并联
            ProInstance proInstance = new ProInstance();
            processInstanceList.add(proInstance);
            HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("title").singleResult();

            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(processInstance.getDeploymentId()).singleResult();
            proInstance.setId(processInstance.getId());
            proInstance.setDeployName(deployment.getName());
            proInstance.setDeployId(deployment.getId());
            if(historicVariableInstance != null) {
                proInstance.setTitle(historicVariableInstance.getValue().toString());
            }

            proInstance.setStatus("发起申请");
            if(historicVariableInstance != null) {
                proInstance.setCreateTime(historicVariableInstance.getCreateTime());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("rows", processInstanceList);
        result.put("total", count);
        return result;
    }
    //查询我申请未提交的任务
    @RequestMapping(value = "myTask", method = RequestMethod.GET,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object getMyTask(HttpServletRequest request,
                            @RequestParam(value = "offset", required = true ,defaultValue= "0" ) int offset,
                            @RequestParam(value = "limit", required = true,defaultValue= "10" ) int limit) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        List<ProInstance> processInstanceList = new LinkedList<>();
        List<HistoricProcessInstance> historicProcessInstanceList = historyService.createHistoricProcessInstanceQuery()
                .orderByProcessInstanceStartTime().desc()
//                .variableValueEquals("user",loginUser.getName())
                .startedBy(loginUser.getName())
                .variableValueEquals("init","start")
                .unfinished()
                .listPage(offset,limit);
//        List<ProcessInstance> list = runtimeService.createProcessInstanceQuery().variableValueEquals("user",loginUser.getName())
//                .variableValueEquals("init","start").list();
//        long count = runtimeService.createProcessInstanceQuery().variableValueEquals("user",loginUser.getName())
//                .variableValueEquals("init","start").count();
        long count = historyService.createHistoricProcessInstanceQuery().variableValueEquals("init","start").startedBy(loginUser.getName()).unfinished().count();
//        long count = historyService.createHistoricProcessInstanceQuery().variableValueEquals("user",loginUser.getName()) .variableValueEquals("init","start").count();
        for(HistoricProcessInstance processInstance : historicProcessInstanceList){
            //此处并联
            ProInstance proInstance = new ProInstance();
            processInstanceList.add(proInstance);
            HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("title").singleResult();

//            Map<String, VariableInstance> stringVariableInstanceMap = runtimeService.getVariableInstances(processInstance.getId());
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(processInstance.getDeploymentId()).singleResult();
            proInstance.setId(processInstance.getId());
            proInstance.setDeployName(deployment.getName());
            proInstance.setDeployId(deployment.getId());
            if(historicVariableInstance != null) {
                proInstance.setTitle(historicVariableInstance.getValue().toString());
            }

            proInstance.setStatus("发起申请");
            if(historicVariableInstance != null) {
                proInstance.setCreateTime(historicVariableInstance.getCreateTime());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("rows", processInstanceList);
        result.put("total", count);
        return result;
    }


    //获取所有审批记录
    @RequestMapping(value = "approval", method = RequestMethod.POST)
    @ResponseBody
    public Object getRecord(@PathVariable(value = "id",required = false) String taskId,
                            HttpServletRequest request,HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        taskId = "42503";
        List<TaskComment> taskCommentList = new LinkedList<>();
        List<Comment> list = new ArrayList<>();
//使用当前的任务ID，查询当前流程对应的历史任务ID

//使用当前任务ID，获取当前任务对象
        Task task = taskService.createTaskQuery()//
                .taskId(taskId)//使用任务ID查询
                .singleResult();
        TaskComment taskComment = new TaskComment();
        Map<String, VariableInstance> stringVariableInstanceMap = runtimeService.getVariableInstances(task.getExecutionId());
        taskComment.setName( stringVariableInstanceMap.get("user").getTextValue());
        taskComment.setCreateTime(new Date(stringVariableInstanceMap.get("timeStamp").getLongValue()));
        taskComment.setDescription("提交");
        taskCommentList.add(taskComment);

//获取流程实例ID
        String processInstanceId = task.getProcessInstanceId();
//使用流程实例ID，查询历史任务，获取历史任务对应的每个任务ID
        List<HistoricTaskInstance> htiList = historyService.createHistoricTaskInstanceQuery()//历史任务表查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .list();
//遍历集合，获取每个任务ID
        if (htiList != null && htiList.size() > 0) {
            for (HistoricTaskInstance hti : htiList) {//任务ID
                String htaskId = hti.getId();//获取批注信息
                List<Comment> taskList = taskService.getTaskComments(htaskId);//对用历史完成后的任务ID
                if(taskList.size() >0) {
                    for (Comment comment : taskList) {
                        taskComment = new TaskComment();
                        taskComment.setName(hti.getAssignee());
                        taskComment.setCreateTime(comment.getTime());
                        taskComment.setDescription(comment.getFullMessage());
                        taskCommentList.add(taskComment);
                    }
                }else{
                    taskComment = new TaskComment();
                    taskComment.setName(hti.getAssignee());
                    taskComment.setCreateTime(hti.getStartTime());
                    taskComment.setDescription("待审批");
                    taskCommentList.add(taskComment);
                }
            }
        }
        return response;
    }



}
