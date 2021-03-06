package com.wxm.controller;

import com.alibaba.druid.support.json.JSONUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxm.entity.*;
import com.wxm.model.*;
import com.wxm.service.*;
import com.wxm.util.*;
import com.wxm.util.exception.OAException;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
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
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping(value = "/workflow/process/")
public class ProcessController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessController.class);
    private ExecutorService threadPoolService = Executors.newFixedThreadPool(1,new NameThreadFactory("html2pdf"));
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;
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
    private ContractCirculationService contractCirculationService;
    @Autowired
    private OADeploymentTemplateService oaDeploymentTemplateService;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private MailService mailService;

    @Autowired
    private OAAttachmentService oaAttachmentService;
//    @Autowired
//    private ContractCirculationService contractCirculationService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private OANotifyService oaNotifyService;
    @Autowired
    private TaskProcessService taskProcessService;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), true));
    }
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

    //获取首页合同统计
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
            long size = taskService.createTaskQuery().processVariableValueNotEquals("init","start").taskAssignee(loginUser.getName()).count();
            result.put("myPending",size);

            List<TaskInfo> taskInfos = new LinkedList<>();
            List<Task> list=taskService.createTaskQuery().processVariableValueNotEquals("init","start").taskAssignee(loginUser.getName()).orderByTaskCreateTime().desc()
                    .listPage(0,5);
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
            NativeHistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createNativeHistoricProcessInstanceQuery();
            StringBuilder sb = new StringBuilder("from (ACT_HI_PROCINST H LEFT OUTER JOIN OA_CONTRACT_CIRCULATION contract on H.PROC_INST_ID_ = contract.PROCESSINSTANCE_ID)  where contract.CONTRACT_STATUS='completed' ");

            if(loginUser.getName().equals("admin")){
//                size = historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc()
//                        .variableValueEquals("instanceStatus","completed")
//                        .count();
                size = historicProcessInstanceQuery.sql(String.format("%s%s","select count(*) ",sb.toString())).count();
//                ReportItem reportItem = contractCirculationService.total("completed",null,null,null,null,null);
                result.put("myComplete", size);
            }else {
                historicProcessInstanceQuery.parameter("assign","%"+loginUser.getName()+"%");
                sb.append("and H.PROC_INST_ID_ in (SELECT DISTINCT PROC_INST_ID_ from ACT_HI_TASKINST where ASSIGNEE_ like #{assign}) ");
                size = historicProcessInstanceQuery.sql(String.format("%s%s","select count(*) ",sb.toString())).count();
//                size = historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc()
//                        .involvedUser(loginUser.getName()).variableValueEquals("instanceStatus","completed")
//                        .count();
                result.put("myComplete", size);


            }
            List<OANotify> oaNotifyList = oaNotifyService.list(null,0,5,null,null);
            result.put("notify",oaNotifyList);
            size = historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("init","start").startedBy(loginUser.getName()).count();
            result.put("initiator",size);

            taskInfos = new LinkedList<>();
            List<HistoricProcessInstance> historicProcessInstanceList = historyService.createHistoricProcessInstanceQuery()
                    .variableValueNotEquals("init","start")
                    .startedBy(loginUser.getName()).orderByProcessInstanceStartTime().desc()
                    .listPage(0,5);
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

    @RequestMapping(value = "/downloadPdf", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Object downloadPdf(
            @RequestParam(value = "processId", required = false, defaultValue = "") String processId,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        try {

            OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processId);
//            auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 下载文件：%s",loginUser.getName(),oaContractCirculationWithBLOBs.getContractName())));
            byte[] bytes = oaContractCirculationWithBLOBs.getContractPdf();
            if(null == bytes){
                return "请重新生成pdf";
            }
            response.setContentType("application/pdf");
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
        }catch (Exception e){
            LOGGER.info("下载异常",e);
        }
        return null;
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Object download(
            @RequestParam(value = "processId", required = false, defaultValue = "") String processId,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
//        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
//        if(null == loginUser) {
//            LOGGER.error("用户未登录");
//            throw new OAException(1101,"用户未登录");
//        }
        try {

            OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processId);
//            auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 下载文件：%s",loginUser.getName(),oaContractCirculationWithBLOBs.getContractName())));
            byte[] bytes = oaContractCirculationWithBLOBs.getContractPdf();
            response.setContentType("application/x-download");
//        String codedfilename = MimeUtility.encodeText( new String((oaContractCirculationWithBLOBs.getContractName()+".pdf").getBytes("UTF-8"), "ISO-8859-1"));
//        String codedfilename = MimeUtility.encodeText(new String((oaContractCirculationWithBLOBs.getContractName()+".pdf").getBytes(), "GB2312"),"GB2312","B");
//        strText = MimeUtility.encodeText(new String(strText.getBytes(), "GB2312"), "GB2312", "B");
            if(bytes == null){

            }

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

    //删除合同
    @RequestMapping(value = "/deleteContract",method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Object deleteContract(HttpServletRequest request,
                                 @RequestParam(value = "id", defaultValue = "0", required = true) String id,
                                 HttpServletResponse response) throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{

            LOGGER.info("删除合同，参数：{}",id);
            OAContractCirculation oaContractCirculation = contractCirculationService.selectByProcessInstanceId(id);
            oaAttachmentService.deleteByProcessId(oaContractCirculation.getProcessInstanceId());
            runtimeService.deleteProcessInstance(oaContractCirculation.getProcessInstanceId(),"草稿");
            contractCirculationService.delete(oaContractCirculation.getContractId());
            auditService.audit(new OAAudit(loginUser.getName(),String.format("删除合同")));
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result","failed");
        }
        return result;

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

        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 模型预览，modelerId：%s",loginUser.getName(),id)));
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
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 模型预览，modelerId：%s",loginUser.getName(),id)));
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

    //通过界面配置名称 获取下一级审批人 列表
    @RequestMapping("/userLeader")
    public Object userLeader(HttpServletRequest request,HttpServletResponse response) throws Exception {
        com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
        if (null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101, "用户未登录");
        }

        String docId = request.getParameter("depId");
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 模型预览，部署模型编号：%s",loginUser.getName(),docId)));
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(docId).singleResult();
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
//        BpmnModel bpmnModel = (BpmnModel)repositoryService.createModelQuery().modelId(modelId).latestVersion().singleResult();
        Collection<FlowElement> flowElements = bpmnModel.getProcesses().get(0).getFlowElements();
        List<FlowElem> flowElems = new LinkedList<>();
        Map<String,String> map = new LinkedHashMap<>();
        Map<String,String> mapUserTask = new LinkedHashMap<>();
        Map<String,String> mapSid = new LinkedHashMap<>();
        for(FlowElement flowElement:flowElements){
            if(flowElement instanceof SequenceFlow){
                map.put(((SequenceFlow) flowElement).getSourceRef(),((SequenceFlow) flowElement).getTargetRef());
            }
            if(flowElement instanceof UserTask ) {
                mapUserTask.put(flowElement.getId(),flowElement.getName());
                mapSid.put(flowElement.getName(),flowElement.getId());
            }
        }
        String sid = "";
        String res = "";
        LinkedList<String> linkedList = new LinkedList();
        if(StringUtils.isBlank(sid)){
            sid = mapSid.get("提交任务");
            sid = mapUserTask.get(sid);
            res = mapUserTask.get(sid);
        }else{
            sid = mapUserTask.get(sid);
            res = mapUserTask.get(sid);
        }
        List<OAUser> oaUserList = userService.listUserLeader(null,res);
        return null;

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
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 模型预览，部署模型编号：%s",loginUser.getName(),docId)));
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
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 模型图片预览，部署模型编号：%s",loginUser.getName(),docId)));
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
            auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 查询任务处理进度，实例编号：%s",loginUser.getName(),processInstanceId)));
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

        identityService.setAuthenticatedUserId(loginUser.getName());
        Map<String, Object> result = new HashMap<>();
        String processInstanceId = map.get("processInstanceId");
        String info = "";
        if(StringUtils.isNotBlank(processInstanceId)) {
            try{
                String custom = map.get("custom");
                String deploymentID = map.get("id");
//                OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(deploymentID);
                OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processInstanceId);
                auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 任务跳转到审批人，实例编号：%s",loginUser.getName(),processInstanceId)));
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
                if(StringUtils.isNotBlank(custom) && !custom.equals(processInstanceId)) {
                    oaAttachmentService.updateByProcessId(custom,processInstanceId);
                }
                String attachmentRefuse = map.get("attachmentRefuse");
                runtimeService.setVariable(task.getProcessInstanceId(), task.getId(), "重新提交 "+attachmentRefuse);
                taskService.addComment(task.getId(), task.getProcessInstanceId(), "重新提交 "+attachmentRefuse);
                result.put("result", "success");
                Object o = taskService.getVariable(task.getId(), "taskDefinitionKey");
                if(null == o){
                    taskService.setAssignee(task.getId(),map.get("pmList"));
                }else {
                    String taskDefinitionKey = o.toString();
                    taskService.setVariable(task.getId(), "taskDefinitionKey", null);
                    taskProcessService.jump(taskDefinitionKey, task.getProcessInstanceId());
                }
                map.put("init", "");
                runtimeService.setVariables(processInstance.getProcessInstanceId(), map);
                oaContractCirculationWithBLOBs.setContractHtml(map.get("html"));
                contractCirculationService.update(oaContractCirculationWithBLOBs);

            }catch (Exception e){
                LOGGER.error("异常",e);
                result.put("result","failed");
            }
        }
        return result;
    }
    //获取所有相关任务


    //拒绝任务到发起人，并记录当前节点，后续可以直接返回
    @RequestMapping(value = "reject", method = RequestMethod.POST)
    @ResponseBody
    public Object reject(HttpServletRequest request,@RequestBody Map<String,String> map)throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        identityService.setAuthenticatedUserId(loginUser.getName());
        String cause = map.get("cause");
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        String taskId = map.get("id");
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 拒绝任务，任务编号：%s",loginUser.getName(),taskId)));
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String taskDefinitionKey = task.getTaskDefinitionKey();
//        runtimeService.setVariable(task.getProcessInstanceId(), "refuseTask", "拒绝" );
        runtimeService.setVariable(task.getProcessInstanceId(), "refuseTask", "拒绝 "+ cause );
        Map<String,String> mapComment = new LinkedHashMap();
        mapComment.put("user",loginUser.getName());
        if(StringUtils.isNotBlank(cause)) {
            mapComment.put("cause", "拒绝 "+cause);
        }else{
            mapComment.put("cause", "拒绝");
        }
        taskService.addComment(task.getId(), task.getProcessInstanceId(), "拒绝 "+ cause);
        runtimeService.setVariable(task.getProcessInstanceId(), taskId, mapComment);

        OAContractCirculation oaContractCirculation = contractCirculationService.selectBaseByProcessInstanceId(task.getProcessInstanceId());
        OAContractCirculationWithBLOBs oa = new OAContractCirculationWithBLOBs();
        oa.setContractId(oaContractCirculation.getContractId());
        oa.setContractReopen(1);
        contractCirculationService.update(oa);
        List<HistoricTaskInstance> historicTaskInstanceList = historyService.createHistoricTaskInstanceQuery()
                .finished()
                .processInstanceId(task.getProcessInstanceId())
                .orderByTaskCreateTime().asc().listPage(0,1);
        runtimeService.setVariable(task.getProcessInstanceId(), "init", "restart");
        if(null != historicTaskInstanceList && historicTaskInstanceList.size() > 0 ){
            for(HistoricTaskInstance task1 : historicTaskInstanceList){
                taskProcessService.jump(task1.getTaskDefinitionKey(), task.getProcessInstanceId());
                task = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
                taskService.setVariable(task.getId(),"taskDefinitionKey",taskDefinitionKey);
                String userStart = runtimeService.getVariable(task.getProcessInstanceId(),"user").toString();
                taskService.setAssignee(task.getId(),userStart);
                runtimeService.setVariable(task.getProcessInstanceId(),"taskDefinitionKeyShow",taskDefinitionKey);
                break;
            }
        }else{
            String userStart = runtimeService.getVariable(task.getProcessInstanceId(),"user").toString();
            taskService.setAssignee(task.getId(),userStart);
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
        String custom = map.get("custom");//附件processID编号，或者没有processID用 用户名替换
        String buyer =  map.get("buyer");
        String seller =  map.get("seller");
        String money =  map.get("money");

        String workDate = map.get("dateStartwork");
        String contract = map.get("contract");
        String contractName = map.get("contractName");
        String pm = map.get("pm");

        Map<String, Object> mapApprove = new HashMap<String, Object>();
        if(StringUtils.isNotBlank(pm)){
            mapApprove.put("user_approve", pm);
        }
        identityService.setAuthenticatedUserId(loginUser.getName());
        OAContractTemplate oaContractTemplate = concactTemplateService.querybyId(Integer.parseInt(contract));
        if(StringUtils.isNotBlank(processInstanceId)) {//草稿提交s
            try{

                String deploymentID = map.get("id");
                OAContractCirculation oaContractCirculation = contractCirculationService.selectBaseByProcessInstanceId(processInstanceId);

                String index = map.get("index");
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                if(index.equals("1")){
                    if(StringUtils.isNotBlank(oaContractCirculation.getContractStatus()) && oaContractCirculation.getContractStatus().equals("start")){
                        result.put("result", "duplicate");
                        return result;
                    }
                    auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 提交合同工单",loginUser.getName())));
//                    Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentID).singleResult();
//                    Date nowTime = new Date();
//                    SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
                    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
                    taskService.addComment(task.getId(), processInstance.getId(), "提交");
                    if(StringUtils.isBlank(pm) ){
                        result.put("result","failed");
                        return result;
                    }
                    taskService.complete(task.getId(),mapApprove);
                    map.put("init","");
                    if(StringUtils.isNotBlank(workStatus) && workStatus.equals("true")) {
                        map.put("contractStatus","1");
                    }else{
                        map.put("contractStatus","0");
                    }
                    map.put("title",contractName);
                    runtimeService.setVariables(processInstance.getProcessInstanceId(),map);
                    //附件处理
                    OAContractCirculationWithBLOBs contractCirculationWithBLOBs = new OAContractCirculationWithBLOBs();
                    contractCirculationWithBLOBs.setContractId(oaContractCirculation.getContractId());
                    if (StringUtils.isNotBlank(custom) && !custom.equals(processInstance.getId())) {//存在附件
                        // 获取word文件流，custom文件名称
                        oaAttachmentService.updateByProcessId(custom,processInstance.getId());
                    }
                    if (StringUtils.isNotBlank(workStatus) && workStatus.equals("true")) {
                        contractCirculationWithBLOBs.setWorkStatus(1);
                    } else {
                        contractCirculationWithBLOBs.setWorkStatus(0);
                    }
                    contractCirculationWithBLOBs.setContractStatus("start");
                    contractCirculationWithBLOBs.setWorkDate(workDate);
                    contractCirculationWithBLOBs.setContractName(contractName);
                    //自定义模板处理
                    if(oaContractTemplate.getTemplateName().contains("自定义")) {
                        contractCirculationWithBLOBs.setDescription("custom");
                    }else{
                        contractCirculationWithBLOBs.setDescription("template");
                    }
                    contractCirculationWithBLOBs.setContractBuyer(buyer);
                    contractCirculationWithBLOBs.setContractSeller(seller);
                    BigDecimal decimal = new BigDecimal(money);
                    BigDecimal setScale1 = decimal.setScale(2,BigDecimal.ROUND_HALF_UP);
                    contractCirculationWithBLOBs.setContractMoney(setScale1);
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
                    auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 草稿更新",loginUser.getName())));
                    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
                    taskService.addComment(task.getId(), processInstance.getId(), "保存草稿");
//                    taskService.addComment(task.getId(), processInstance.getId(), "提交");
                    runtimeService.setVariables(processInstance.getProcessInstanceId(),map);
                    taskService.setAssignee(task.getId(),loginUser.getName());
                    OAContractCirculationWithBLOBs contractCirculationWithBLOBs = new OAContractCirculationWithBLOBs();
                    contractCirculationWithBLOBs.setContractId(oaContractCirculation.getContractId());
                    contractCirculationWithBLOBs.setContractName(contractName);
                    contractCirculationWithBLOBs.setWorkDate(workDate);
                    contractCirculationWithBLOBs.setContractBuyer(buyer);
                    contractCirculationWithBLOBs.setContractSeller(seller);
                    BigDecimal decimal = new BigDecimal(money);
                    BigDecimal setScale1 = decimal.setScale(2,BigDecimal.ROUND_HALF_UP);
                    contractCirculationWithBLOBs.setContractMoney(setScale1);
                    contractCirculationService.update(contractCirculationWithBLOBs);
                    map.put("title",contractName);
                    runtimeService.setVariables(processInstance.getProcessInstanceId(),map);
                }

            }catch (Exception e){
                result.put("result","failed");
            }
        }else {
            try {
                String deploymentID = map.get("id");
                String index = map.get("index");

                auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 提交工单",loginUser.getName())));
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

                ProcessInstance processInstance = runtimeService.startProcessInstanceById(pd.getId(), mapInfo);
                OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = new OAContractCirculationWithBLOBs();
                oaContractCirculationWithBLOBs.setUserId(loginUser.getId());
                oaContractCirculationWithBLOBs.setEnterpriseId(loginUser.getEnterpriseId());
                oaContractCirculationWithBLOBs.setTemplateId(Integer.parseInt(contract));
                oaContractCirculationWithBLOBs.setWorkDate(workDate);
                if(StringUtils.isBlank(contractName)) {
                    oaContractCirculationWithBLOBs.setContractName(deployment.getName() + "-" + time.format(nowTime));
                }else{
                    oaContractCirculationWithBLOBs.setContractName(contractName);
                }

                oaContractCirculationWithBLOBs.setProcessInstanceId(processInstance.getId());

                oaContractCirculationWithBLOBs.setCreateTime(new Date());
                if(StringUtils.isNotBlank(workStatus) && workStatus.equals("true")) {
                    oaContractCirculationWithBLOBs.setWorkStatus(1);
                }else{
                    oaContractCirculationWithBLOBs.setWorkStatus(0);
                }
                //自定义模板处理
                if(oaContractTemplate.getTemplateName().contains("自定义")) {
                    oaContractCirculationWithBLOBs.setDescription("custom");
                }else{
                    oaContractCirculationWithBLOBs.setDescription("template");
                }

//                }
                if(null != map.get("html")) {
                    oaContractCirculationWithBLOBs.setContractHtml(map.get("html"));
                }
                oaContractCirculationWithBLOBs.setContractBuyer(buyer);
                oaContractCirculationWithBLOBs.setContractSeller(seller);
                BigDecimal decimal = new BigDecimal(money);
                BigDecimal setScale1 = decimal.setScale(2,BigDecimal.ROUND_HALF_UP);
                oaContractCirculationWithBLOBs.setContractMoney(setScale1);
                OAContractCirculation max = contractCirculationService.selectByMaxId();
                SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
                if(null == max || StringUtils.isBlank(max.getContractSerialNumber())){
                    oaContractCirculationWithBLOBs.setContractSerialNumber(date.format(new Date())+"01");
                }else{
                    Integer serial = Integer.parseInt(max.getContractSerialNumber().substring("yyyyMMdd".length()));
                    oaContractCirculationWithBLOBs.setContractSerialNumber(date.format(new Date())+String.format("%02d", ++serial));
                }


                if (StringUtils.isNotBlank(custom) && !custom.equals(processInstance.getId())) {
                    oaAttachmentService.updateByProcessId(custom,processInstance.getId());
                    // 获取word文件流
//                    String filePf = contractPath + custom;
//                    byte[] word = FileByte.getByte(filePf);
//                    oaContractCirculationWithBLOBs.setAttachmentName(custom.substring(14));
//                    oaContractCirculationWithBLOBs.setAttachmentContent(word);
                }
                Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
                if (index.equals("1")) {
                    taskService.addComment(task.getId(), processInstance.getId(), "提交");
                    oaContractCirculationWithBLOBs.setContractStatus("start");
//                    taskService.setAssignee(task.getId(),"wxm");
                    if(StringUtils.isBlank(pm) ) {
                        result.put("result", "failed");
                        return result;

                    }
                    taskService.complete(task.getId(), mapApprove);
                    runtimeService.setVariable(processInstance.getProcessInstanceId(), "init", "");
                }else{
                    taskService.addComment(task.getId(), processInstance.getId(), "草稿");
//                    String userStart = runtimeService.getVariable(task.getProcessInstanceId(),"user").toString();
                    taskService.setAssignee(task.getId(),loginUser.getName());
                    runtimeService.setVariable(processInstance.getProcessInstanceId(), "pmApprove", pm);
                }
                contractCirculationService.insert(oaContractCirculationWithBLOBs);
            } catch (Exception e) {
                LOGGER.error("异常",e);
                result.put("result", "failed");
            }
        }
        return result;
    }

    //重新生成pdf文件
    @RequestMapping(value = "reGenerate", method = RequestMethod.GET,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object reGenerate(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try {
            String processInstancesId = request.getParameter("processId");
            OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processInstancesId);
            OAContractTemplate oa = concactTemplateService.querybyId(oaContractCirculationWithBLOBs.getTemplateId());
            OAContractCirculationWithBLOBs tmp = new OAContractCirculationWithBLOBs();
            tmp.setContractId(oaContractCirculationWithBLOBs.getContractId());
            tmp.setProcessInstanceId(oaContractCirculationWithBLOBs.getProcessInstanceId());
            HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstancesId).variableName("title").singleResult();
            String html = oa.getTemplateHtml();
            StringBuilder sb = new StringBuilder(html.length() + 300);
            sb.append("<!DOCTYPE html>");
            sb.append("<head>");
            sb.append("<title>");
            sb.append(historicVariableInstance.getValue().toString());
            sb.append("</title>");
            sb.append(" <META HTTP-EQUIV=\"CONTENT-TYPE\" CONTENT=\"text/html; charset=gb2312\"> ");
//                sb.append(" <META HTTP-EQUIV=\"CONTENT-TYPE\" CONTENT=\"text/html; charset=utf-8\"> ");
            sb.append("<body>");
            sb.append(html);
            sb.append("</body></html>");
            List<OAFormProperties> formPropertiesList = formPropertiesService.listByTemplateId(oaContractCirculationWithBLOBs.getTemplateId());

            Map<String, Integer> linkedHashMap = new LinkedHashMap();
            for (OAFormProperties oaFormProperties : formPropertiesList) {
                try {
                    linkedHashMap.put(oaFormProperties.getFieldMd5(), Integer.parseInt(oaFormProperties.getFieldValid().substring(2)));
                } catch (Exception e) {
                    linkedHashMap.put(oaFormProperties.getFieldMd5(), 20);
                    LOGGER.info("字段：{}", oaFormProperties.getFieldValid());
                    LOGGER.error("异常", e);
                }
            }
            Html2PdfTask html2PdfTask = new Html2PdfTask(sb, contractCirculationService, linkedHashMap, processInstancesId, tmp
                    , contractPath + historicVariableInstance.getValue().toString(), openOfficePath, historyService, oaAttachmentService);
            html2PdfTask.doRun();
        }catch (Exception e){
            result.put("result","failed");
            LOGGER.error("异常",e);
        }
        return result;
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
        String approve = request.getParameter("approve");
        Map<String, Object> map = new HashMap<String, Object>();
        if(StringUtils.isNotBlank(approve)){
            map.put("user_approve", approve);
        }
        identityService.setAuthenticatedUserId(loginUser.getName());
//        else{
//            Map<String, Object> result = new HashMap<>();
//            result.put("result","failed");
//            return result;
//        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
//利用任务对象，获取流程实例id
        String processInstancesId = task.getProcessInstanceId();
//         Authentication.setAuthenticatedUserId("cmc"); // 添加批注时候的审核人，通常应该从session获取
        taskService.addComment(taskId, processInstancesId, "同意");
        taskService.complete(taskId,map);

        ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
                .processInstanceId(processInstancesId)//使用流程实例ID查询
                .singleResult();
//        task = taskService.createTaskQuery().processInstanceId(processInstancesId).singleResult();
        OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processInstancesId);
        if(pi != null) {
            ActivityImpl activity = ((ProcessDefinitionEntity) repositoryService.getProcessDefinition(task.getProcessDefinitionId())).findActivity(pi.getActivityId());
            if (null != activity && activity.getProperty("name").toString().contains("归档")) {
                OAContractCirculationWithBLOBs tmp = new OAContractCirculationWithBLOBs();
                tmp.setContractId(oaContractCirculationWithBLOBs.getContractId());
                //归档后 用户可以查
                SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
                if(StringUtils.isBlank(oaContractCirculationWithBLOBs.getContractSerialNumber())){
                    tmp.setArchiveSerialNumber(date.format(new Date())+"01");
                }else{
                    Integer serial = Integer.parseInt(oaContractCirculationWithBLOBs.getContractSerialNumber().substring("yyyyMMdd".length()));
                    tmp.setArchiveSerialNumber(date.format(new Date())+String.format("%02d", serial));
                }
                contractCirculationService.update(tmp);
            }
            if (null != activity && activity.getProperty("name").toString().contains("核对")) {
                OAContractCirculationWithBLOBs tmp = new OAContractCirculationWithBLOBs();
                tmp.setContractId(oaContractCirculationWithBLOBs.getContractId());
                tmp.setProcessInstanceId(oaContractCirculationWithBLOBs.getProcessInstanceId());
                auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 核对合同",loginUser.getName())));
            //归档后 用户可以查
                runtimeService.setVariable(processInstancesId,"instanceStatus","completed");
                HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstancesId).variableName("title").singleResult();
//            VariableInstance variableInstance = runtimeService.getVariableInstance(processInstancesId,"title");
//                String html = oaContractCirculationWithBLOBs.getContractHtml();

                OAContractTemplate oa = concactTemplateService.querybyId(oaContractCirculationWithBLOBs.getTemplateId());
                String html = oa.getTemplateHtml();
                StringBuilder sb = new StringBuilder(html.length() + 300);
                sb.append("<!DOCTYPE html>");
                sb.append("<head>");
                sb.append("<title>");
                sb.append(historicVariableInstance.getValue().toString());
                sb.append("</title>");
                sb.append(" <META HTTP-EQUIV=\"CONTENT-TYPE\" CONTENT=\"text/html; charset=gb2312\"> ");
//                sb.append(" <META HTTP-EQUIV=\"CONTENT-TYPE\" CONTENT=\"text/html; charset=utf-8\"> ");
                sb.append("<body>");
                sb.append(html);
                sb.append("</body></html>");
                List<OAFormProperties> formPropertiesList = formPropertiesService.listByTemplateId(oaContractCirculationWithBLOBs.getTemplateId());

                Map<String,Integer> linkedHashMap = new LinkedHashMap();
                for(OAFormProperties oaFormProperties:formPropertiesList){
                    try {
                        linkedHashMap.put(oaFormProperties.getFieldMd5(), Integer.parseInt(oaFormProperties.getFieldValid().substring(2)));
                    }catch (Exception e){
                        linkedHashMap.put(oaFormProperties.getFieldMd5(), 20);
                        LOGGER.info("字段：{}",oaFormProperties.getFieldValid());
                        LOGGER.error("异常",e);
                    }
                }

                Html2PdfTask html2PdfTask = new Html2PdfTask(sb,contractCirculationService,linkedHashMap,processInstancesId,tmp
                ,contractPath + historicVariableInstance.getValue().toString(),openOfficePath,historyService,oaAttachmentService);
                threadPoolService.execute(html2PdfTask);
//                try {
//                    String data = fillValue(processInstancesId, sb,linkedHashMap);
//                    String fileHtml = contractPath + historicVariableInstance.getValue().toString() + ".html";
//                    String filePf = contractPath + historicVariableInstance.getValue().toString() + ".pdf";
//                    PrintStream printStream = new PrintStream(new FileOutputStream(fileHtml));
//                    printStream.println(data);
//                    //转换成pdf文件
//                    File htmlFile = Word2Html.html2pdf(fileHtml, openOfficePath);
//                    // 获取pdf文件流
//                    byte[] pdf = FileByte.getByte(filePf);
//                    tmp.setContractStatus("completed");
//                    // HTML文件字符串
//                    tmp.setContractPdf(pdf);
//                    contractCirculationService.update(tmp);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
            }
        }else{
            auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 完成任务审批",loginUser.getName())));
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
                                 @RequestParam(value = "init", required = false) String init,
                                 @RequestParam(value = "offset", required = true) int offset,
                                 @RequestParam(value = "limit", required = true) int limit,
                                 HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }

        List<TaskInfo> taskInfos = new LinkedList<>();
        TaskQuery taskQuery = taskService.createTaskQuery();// 创建任务查询对象
        if(null!= init && init.equals("start")){
            taskQuery.processVariableValueEquals("init","start");
        }else{
            taskQuery.processVariableValueNotEquals("init","start");
        }
        List<Task> list = taskQuery
                .orderByTaskCreateTime().desc()
                .taskAssignee(loginUser.getName())// 指定个人认为查询，指定办理人
                .listPage(offset,limit);

        long size = taskService.createTaskQuery().taskAssignee(loginUser.getName()).count();
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 获取待办任务",loginUser.getName())));
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                ProcessDefinition pf = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
                Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(pf.getDeploymentId()).singleResult();
                TaskInfo taskInfo = new TaskInfo();
                taskInfo.setId(task.getId());
                taskInfo.setName(deployment.getName());
                taskInfo.setDeployId(deployment.getId());
                taskInfo.setTimestamp(task.getCreateTime());
                taskInfo.setAssignee(task.getAssignee());
                VariableInstance variableInstance = runtimeService.getVariableInstance(task.getExecutionId(),"title");
                if(null != variableInstance) {
                    taskInfo.setTitle(variableInstance.getTextValue());
                }
                variableInstance = runtimeService.getVariableInstance(task.getExecutionId(),"init");
                if(null == variableInstance || null == variableInstance.getTextValue() || variableInstance.getTextValue().equals("start") || variableInstance.getTextValue().equals("restart")){
                    taskInfo.setOrder(1);
                }else{
                    taskInfo.setOrder(0);
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

    @RequestMapping(value = "myTaskRefuse", method = RequestMethod.GET)
    @ResponseBody
    public Object myTask(
            @RequestParam(value = "refuse", required = true ,defaultValue= "0" ) String refuse,
            @RequestParam(value = "startTime", required = true) Date startTime,
            @RequestParam(value = "endTime", required = true) Date endTime,
            @RequestParam(value = "offset", required = true ,defaultValue= "0" ) int offset,
            @RequestParam(value = "limit", required = true,defaultValue= "10" ) int limit,
            HttpServletRequest request)throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser){
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }

        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery();
        historicProcessInstanceQuery.variableValueNotEquals("init","start");
        if(refuse.equals("refuse")){
            historicProcessInstanceQuery = historicProcessInstanceQuery.involvedUser(loginUser.getName()).variableValueLike("refuseTask","%拒绝%");
        }else{
            historicProcessInstanceQuery = historicProcessInstanceQuery.involvedUser(loginUser.getName());
        }
        List<HistoricProcessInstance> listProcess = historicProcessInstanceQuery
                .startedAfter(startTime)
                .startedBefore(endTime)
                .orderByProcessInstanceStartTime().desc().listPage(offset,limit);
        long size = historicProcessInstanceQuery
                .startedAfter(startTime)
                .startedBefore(endTime)
                .count();


        List<TaskInfo> taskInfos = new LinkedList<>();
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
            OAContractCirculation oaContractCirculation = contractCirculationService.selectBaseByProcessInstanceId(historicProcessInstance.getId());
            if(null != oaContractCirculation) {
                taskInfo.setArchiveSerialNumber(oaContractCirculation.getArchiveSerialNumber());
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
    //归档任务查询 获取该员工历史任务,参与过的任务
    @RequestMapping(value = "processHistory", method = RequestMethod.GET)
    @ResponseBody
    public Object findHistoryTaskByName(@RequestParam(value = "user", required = false) String user,
                                        @RequestParam(value = "offset", required = true ,defaultValue= "0" ) int offset,
                                        @RequestParam(value = "limit", required = true,defaultValue= "10" ) int limit,
                                        @RequestParam(value = "archiveNumber", required = false  ) String archiveNumber,
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
//        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery();
        NativeHistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createNativeHistoricProcessInstanceQuery();
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 归档任务查询",loginUser.getName())));


//        List<HistoricProcessInstance> list = historyService.createNativeHistoricProcessInstanceQuery()
//
//                .sql("select top 100 percent H.* from (ACT_HI_PROCINST H LEFT OUTER JOIN OA_CONTRACT_CIRCULATION contract " +
//                        "on H.PROC_INST_ID_ = contract.PROCESSINSTANCE_ID) " +
//                        "where H.START_TIME_ is not null and contract.ARCHIVE_SERIAL_NUMBER like #{number} and H.PROC_INST_ID_ in " +
//                        "(SELECT DISTINCT PROC_INST_ID_ from ACT_HI_TASKINST where ASSIGNEE_ like #{assign}) " +
//                        "order by H.START_TIME_ desc")
//                .parameter("number","%201811%")
//                .parameter("assign","2018110264")
//                .listPage(offset,limit);

//        if(StringUtils.isNotBlank(archiveNumber)) {
//            if(StringUtils.isNotBlank(title)){
//                if(StringUtils.isNotBlank(user)){
//                    list = historyService.createNativeHistoricProcessInstanceQuery()
//                            .sql("select top 100 percent H.* from (ACT_HI_PROCINST H LEFT OUTER JOIN OA_CONTRACT_CIRCULATION contract " +
//                                    "on H.PROC_INST_ID_ = contract.PROCESSINSTANCE_ID)  where H.START_TIME_ is not null and contract.ARCHIVE_SERIAL_NUMBER=#{number} " +
//                                    "order by H.START_TIME_ desc")
//                            .parameter("number","2018110264")
//                            .listPage(0,10);
//                }else{
//
//                }
//            }
//        }
//        StringBuilder sb = new StringBuilder("select top 100 percent H.* from (ACT_HI_PROCINST H LEFT OUTER JOIN OA_CONTRACT_CIRCULATION contract on H.PROC_INST_ID_ = contract.PROCESSINSTANCE_ID)  where H.START_TIME_ is not null ");
        StringBuilder sb = new StringBuilder("from (ACT_HI_PROCINST H LEFT OUTER JOIN OA_CONTRACT_CIRCULATION contract on H.PROC_INST_ID_ = contract.PROCESSINSTANCE_ID)  where contract.CONTRACT_STATUS='completed' ");

        if(StringUtils.isNotBlank(archiveNumber)) {
            historicProcessInstanceQuery.parameter("number","%"+StringUtils.trim(archiveNumber)+"%");
            sb.append("and contract.ARCHIVE_SERIAL_NUMBER like #{number} ");
        }
        if(StringUtils.isNotBlank(title)) {
            historicProcessInstanceQuery.parameter("title","%"+StringUtils.trim(title)+"%");
            sb.append("and H.PROC_INST_ID_ in (SELECT DISTINCT PROC_INST_ID_ from ACT_HI_VARINST where NAME_ = 'title' and TEXT_ like #{title} ) ");
//            sb.append("and H.PROC_INST_ID_ in (SELECT DISTINCT PROC_INST_ID_ from ACT_HI_VARINST where NAME_ = 'instanceStatus' and TEXT_ = 'completed') ");
//            historicProcessInstanceQuery =historicProcessInstanceQuery
//                    .variableValueLike("title", "%"+title+"%")
//                    .variableValueEquals("","");
        }
        if(loginUser.getName().equals("admin")){
            if(StringUtils.isNotBlank(user)){
                historicProcessInstanceQuery.parameter("assign","%"+StringUtils.trim(user)+"%");
                sb.append("and H.PROC_INST_ID_ in (SELECT DISTINCT PROC_INST_ID_ from ACT_HI_TASKINST where ASSIGNEE_ like #{assign}) ");
//                historicProcessInstanceQuery =historicProcessInstanceQuery.involvedUser(user);
            }
            size = historicProcessInstanceQuery
                    .sql(String.format("%s%s","select count(*) ",sb.toString()))
//                    .variableValueEquals("instanceStatus","completed")
//                .variableValueEquals("user",loginUser.getName())
                    .count();
            historicProcessInstanceQuery.parameter("orderBy","START_TIME_ desc");
//            sb.append("order by H.START_TIME_ desc");
            listProcess = historicProcessInstanceQuery
                    .sql(String.format("%s%s%s","select top 100 percent H.* ",sb.toString(),"order by H.START_TIME_ desc"))
//                    .variableValueEquals("instanceStatus","completed")
//                .variableValueEquals("user",loginUser.getName())
//                    .orderByProcessInstanceStartTime().desc()
                    .listPage(offset,limit);

        }else{
            historicProcessInstanceQuery.parameter("assign","%"+loginUser.getName()+"%");
            sb.append("and H.PROC_INST_ID_ in (SELECT DISTINCT PROC_INST_ID_ from ACT_HI_TASKINST where ASSIGNEE_ like #{assign}) ");
//            sb.append("order by H.START_TIME_ desc");
            size = historicProcessInstanceQuery
                    .sql(String.format("%s%s","select count(*) ",sb.toString()))
//                    .involvedUser(loginUser.getName()).variableValueEquals("instanceStatus","completed")
//                .variableValueEquals("user",loginUser.getName())
                    .count();
            historicProcessInstanceQuery.parameter("orderBy","START_TIME_ desc");
            listProcess = historicProcessInstanceQuery
                    .sql(String.format("%s%s%s","select top 100 percent H.* ",sb.toString(),"order by H.START_TIME_ desc"))
//                    .involvedUser(loginUser.getName()).variableValueEquals("instanceStatus","completed")
//                .variableValueEquals("user",loginUser.getName())
//                    .orderByProcessInstanceStartTime().desc()
                    .listPage(offset,limit);

        }

        for(HistoricProcessInstance historicProcessInstance : listProcess){
            historicProcessInstance.getStartTime();
//            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(historicProcessInstance.getDeploymentId()).singleResult();
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
            OAContractCirculation oaContractCirculation = contractCirculationService.selectBaseByProcessInstanceId(historicProcessInstance.getId());
            if(null != oaContractCirculation) {
                taskInfo.setArchiveSerialNumber(oaContractCirculation.getArchiveSerialNumber());
            }
            taskInfo.setId(historicProcessInstance.getId());
//            taskInfo.setName(deployment.getName());
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
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 查询任务状态",loginUser.getName())));
        List<ProInstance> processInstanceList = new LinkedList<>();
        List<HistoricProcessInstance> historicProcessInstanceList = historyService.createHistoricProcessInstanceQuery()
                .variableValueNotEquals("init","start")
                .orderByProcessInstanceStartTime().desc()
                .startedBy(loginUser.getName())
                .listPage(offset,limit);
        long count = historyService.createHistoricProcessInstanceQuery().startedBy(loginUser.getName()).count();
        for(HistoricProcessInstance processInstance : historicProcessInstanceList){
            //此处并联
            ProInstance proInstance = new ProInstance();
            processInstanceList.add(proInstance);

            OAContractCirculation oaContractCirculation = contractCirculationService.selectBaseByProcessInstanceId(processInstance.getId());

            if(oaContractCirculation != null){
                proInstance.setContractSerial(oaContractCirculation.getContractSerialNumber());
                proInstance.setTitle(oaContractCirculation.getContractName());
                proInstance.setCreateTime(oaContractCirculation.getCreateTime());
            }else{
                HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("title").singleResult();
                if(historicVariableInstance != null) {
                    proInstance.setTitle(historicVariableInstance.getValue().toString());
                }
                if(historicVariableInstance != null) {
                    proInstance.setCreateTime(historicVariableInstance.getCreateTime());
                }
            }
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(processInstance.getDeploymentId()).singleResult();
            proInstance.setId(processInstance.getId());
            proInstance.setDeployName(deployment.getName());
            proInstance.setDeployId(deployment.getId());
//            if(historicVariableInstance != null) {
//                proInstance.setTitle(historicVariableInstance.getValue().toString());
//            }

            proInstance.setStatus("发起申请");
//            if(historicVariableInstance != null) {
//                proInstance.setCreateTime(historicVariableInstance.getCreateTime());
//            }

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
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 查询未提交任务",loginUser.getName())));
        List<ProInstance> processInstanceList = new LinkedList<>();
        List<HistoricProcessInstance> historicProcessInstanceList = historyService.createHistoricProcessInstanceQuery()
                .orderByProcessInstanceStartTime().desc()
//                .variableValueEquals("user",loginUser.getName())
                .startedBy(loginUser.getName())
                .variableValueLike("init","start")
                .unfinished()
                .listPage(offset,limit);
//        List<ProcessInstance> list = runtimeService.createProcessInstanceQuery().variableValueEquals("user",loginUser.getName())
//                .variableValueEquals("init","start").list();
//        long count = runtimeService.createProcessInstanceQuery().variableValueEquals("user",loginUser.getName())
//                .variableValueEquals("init","start").count();
        long count = historyService.createHistoricProcessInstanceQuery().variableValueLike("init","start").startedBy(loginUser.getName()).unfinished().count();
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
