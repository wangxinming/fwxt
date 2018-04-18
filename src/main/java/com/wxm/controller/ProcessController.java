package com.wxm.controller;

import com.wxm.entity.*;
import com.wxm.model.OAContractCirculationWithBLOBs;
import com.wxm.model.OADeploymentTemplateRelation;
import com.wxm.model.OAFormProperties;
import com.wxm.service.*;
import com.wxm.util.PropertyUtil;
import com.wxm.util.ValidType;
import com.wxm.util.Word2Html;
import com.wxm.util.exception.OAException;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.*;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

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

    @Autowired
    private ConcactTemplateService concactTemplateService;

    @Autowired
    private FormPropertiesService formPropertiesService;

    @Autowired
    private OADeploymentTemplateService oaDeploymentTemplateService;

    @Autowired
    ProcessEngine processEngine;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ContractCirculationService contractCirculationService;

    @Autowired
    private TaskProcessService taskProcessService;

    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Object download(
            @RequestParam(value = "processId", required = false, defaultValue = "") String processId,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processId);
        byte[] bytes = oaContractCirculationWithBLOBs.getContractPdf().getBytes();
        response.setContentType("application/x-download");
        String codedfilename = MimeUtility.encodeText(oaContractCirculationWithBLOBs.getContractName()+".pdf", "UTF-8", "B");
        response.setHeader("Content-Disposition", "attachment;filename=" + codedfilename);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        return null;
    }

    @RequestMapping("/queryProPlan")
    public void queryProPlan(HttpServletRequest request,HttpServletResponse response) throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        String taskID = request.getParameter("TaskId");
        String processInstanceId = "";

        if(!StringUtils.isBlank(taskID)){
            Task task = taskService.createTaskQuery().taskId(taskID).singleResult();
            processInstanceId = task.getProcessInstanceId();
        }else{
            processInstanceId = request.getParameter("ProcessInstanceId");
        }
        ProcessInstance  processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
//        HistoricProcessInstance processInstance =  historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);
        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
//        ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());
//        HistoricActivityInstance highLightedActivitList =  historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).singleResult();
        //高亮环节id集合
        List<String> highLightedActivitis = new ArrayList<String>();

//        //高亮线路id集合
//        List<String> highLightedFlows = getHighLightedFlows(definitionEntity,highLightedActivitList);
//        String activityId = "";
//
//        for(HistoricActivityInstance tempActivity : highLightedActivitList){
//             activityId = tempActivity.getActivityId();
//        }
        highLightedActivitis.add(processInstance.getActivityId());
//        commandContext.getProcessEngineConfiguration().getProcessDiagramGenerator().generateDiagram(bpmnModel,"png", highLightedActivitis,new ArrayList<String>(),"宋体","宋体","宋体",null,1.0D);
        InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel,"png", highLightedActivitis,new ArrayList<String>(),"宋体","宋体","宋体",null,1.0D);
        //单独返回流程图，不高亮显示
//        InputStream imageStream = diagramGenerator.generatePngDiagram(bpmnModel);
        // 输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len;
        while ((len = imageStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }


    //处理审批跳转到已审批人
    @RequestMapping(value = "jump", method = RequestMethod.POST)
    @ResponseBody
    public Object jump(HttpServletRequest request,@RequestBody Map<String,String> map)throws  Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        Map<String, Object> result = new HashMap<>();
        String processInstanceId = map.get("processInstanceId");
        String info = "";
        if(StringUtils.isNotBlank(processInstanceId)) {
            try{
                String deploymentID = map.get("id");
                OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(deploymentID);
                List<OAFormProperties> oaFormPropertiesList = formPropertiesService.listByTemplateId(oaDeploymentTemplateRelation.getRelationTemplateid());
//                WordEntity wordEntity = wordTemplateService.queryInfoRel(deploymentID);
//                List<WordTemplateField> list = wordTemplateFieldService.getWordTemplateFieldByTemplateId(wordEntity.getId());
                for (OAFormProperties oaFormProperties : oaFormPropertiesList) {
                    String md5 = oaFormProperties.getFieldMd5();
                    String type = oaFormProperties.getFieldType();
                    String length = oaFormProperties.getFieldValid();
                    if( StringUtils.isBlank(md5) ||  StringUtils.isBlank(type) ||  StringUtils.isBlank(length)) continue;
                    md5 = md5.trim();
                    type = type.trim();
                    length = length.trim();
                    String value = map.get(md5).toString();
                    if( StringUtils.isBlank(value))continue;
                    if (value.length() > Integer.parseInt(length)) {
                        info = oaFormProperties.getFieldMd5() + " 字段长度过长";
                        result.put("info", info);
                        break;
                    }
                    if (type.equals("D")) {
                        if (!ValidType.isNumeric(value)) {
                            info = oaFormProperties.getFieldMd5() + " 字段类型错误";
                            result.put("info", info);
                            break;
                        }
                    }
                }
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
                taskService.addComment(task.getId(), processInstance.getId(), "重新提交");

                result.put("result", "success");
                String taskId = request.getParameter("taskId");
                String taskDefinitionKey = taskService.getVariable(task.getId(), "taskDefinitionKey").toString();
                taskProcessService.jump(taskDefinitionKey, task.getProcessInstanceId());
//                taskService.complete(task.getId());
                map.put("init","");
                runtimeService.setVariables(processInstance.getProcessInstanceId(),map);
                OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processInstance.getProcessInstanceId());
//                oaContractCirculationWithBLOBs.setTemplateId(oaDeploymentTemplateRelation.getRelationTemplateid());
//                oaContractCirculationWithBLOBs.setProcessInstanceId(processInstance.getId());
                oaContractCirculationWithBLOBs.setContractHtml(map.get("html"));
                contractCirculationService.update(oaContractCirculationWithBLOBs);
            }catch (Exception e){
                result.put("result","failed");
            }


        }
        return result;
    }

    //拒绝任务到发起人，并记录当前节点，后续可以直接返回
    @RequestMapping(value = "reject", method = RequestMethod.POST)
    @ResponseBody
    public Object reject(HttpServletRequest request)throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        String taskId = request.getParameter("id");
        String userName = request.getParameter("userName");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String taskDefinitionKey = task.getTaskDefinitionKey();
//        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        List<HistoricTaskInstance> historicTaskInstanceList = historyService.createHistoricTaskInstanceQuery().finished().
                processInstanceId(task.getProcessInstanceId())
                .orderByTaskCreateTime().asc().list();
        if(null != historicTaskInstanceList){
            for(HistoricTaskInstance task1 : historicTaskInstanceList){
                if(task1.getAssignee() == null){
//                    HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery()
//                            .processInstanceId(task.getProcessInstanceId()).variableName("user").singleResult();
                    taskProcessService.jump(task1.getTaskDefinitionKey(), task.getProcessInstanceId());
                    task = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
                    taskService.setVariable(task.getId(),"taskDefinitionKey",taskDefinitionKey);
                    break;
                }
//                        HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery()
//                            .processInstanceId(task.getProcessInstanceId()).variableName("user").singleResult();
//                    if(historicVariableInstance.getValue().toString().equals(userName)) {
//                        taskProcessService.jump(task1.getTaskDefinitionKey(), taskId);
//                        task = taskService.createTaskQuery().taskId(taskId).singleResult();
//                        taskService.setVariable(task.getId(),"taskDefinitionKey",taskDefinitionKey);
//                        break;
//                    }
            }
        }
        return result;
    }

    //开始发起任务
    @RequestMapping(value = "start", method = RequestMethod.POST)
    @ResponseBody
    public Object start(HttpServletRequest request, HttpServletResponse response,@RequestBody Map<String,String> map) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        String info = "";
        String processInstanceId = map.get("processInstanceId");
        if(StringUtils.isNotBlank(processInstanceId)) {
            try{
                String deploymentID = map.get("id");
                OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(deploymentID);
                List<OAFormProperties> oaFormPropertiesList = formPropertiesService.listByTemplateId(oaDeploymentTemplateRelation.getRelationTemplateid());
//                WordEntity wordEntity = wordTemplateService.queryInfoRel(deploymentID);
//                List<WordTemplateField> list = wordTemplateFieldService.getWordTemplateFieldByTemplateId(wordEntity.getId());
                for (OAFormProperties oaFormProperties : oaFormPropertiesList) {
                    String md5 = oaFormProperties.getFieldMd5();
                    String type = oaFormProperties.getFieldType();
                    String length = oaFormProperties.getFieldValid();
                    if( StringUtils.isBlank(md5) ||  StringUtils.isBlank(type) ||  StringUtils.isBlank(length)) continue;
                    md5 = md5.trim();
                    type = type.trim();
                    length = length.trim();
                    String value = map.get(md5).toString();
                    if( StringUtils.isBlank(value))continue;
                    if (value.length() > Integer.parseInt(length)) {
                        info = oaFormProperties.getFieldMd5() + " 字段长度过长";
                        result.put("info", info);
                        break;
                    }
                    if (type.equals("D")) {
                        if (!ValidType.isNumeric(value)) {
                            info = oaFormProperties.getFieldMd5() + " 字段类型错误";
                            result.put("info", info);
                            break;
                        }
                    }
                }
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
                taskService.addComment(task.getId(), processInstance.getId(), "提交");
                taskService.complete(task.getId());
                map.put("init","");
                runtimeService.setVariables(processInstance.getProcessInstanceId(),map);
                OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = new OAContractCirculationWithBLOBs();
                oaContractCirculationWithBLOBs.setTemplateId(oaDeploymentTemplateRelation.getRelationTemplateid());
                oaContractCirculationWithBLOBs.setProcessInstanceId(processInstance.getId());
                oaContractCirculationWithBLOBs.setContractHtml(map.get("html"));
                contractCirculationService.insert(oaContractCirculationWithBLOBs);
            }catch (Exception e){
                result.put("result","failed");
            }
        }else {
            try {
                String deploymentID = map.get("id");
                String index = map.get("index");
                OADeploymentTemplateRelation oaDeploymentTemplateRelation = oaDeploymentTemplateService.selectByDeploymentId(deploymentID);
                List<OAFormProperties> oaFormPropertiesList = formPropertiesService.listByTemplateId(oaDeploymentTemplateRelation.getRelationTemplateid());
//                WordEntity wordEntity = wordTemplateService.queryInfoRel(deploymentID);
//                List<WordTemplateField> list = wordTemplateFieldService.getWordTemplateFieldByTemplateId(wordEntity.getId());
                for (OAFormProperties oaFormProperties : oaFormPropertiesList) {
                    String md5 = oaFormProperties.getFieldMd5();
                    String type = oaFormProperties.getFieldType();
                    String length = oaFormProperties.getFieldValid();

                    if (StringUtils.isBlank(md5) || StringUtils.isBlank(type) || StringUtils.isBlank(length)) continue;
                    md5 = md5.trim();
                    type = type.trim();
                    length = length.trim();
                    String value = map.get(md5).toString();
                    if (StringUtils.isBlank(value)) continue;
                    if (value.length() > Integer.parseInt(length)) {
                        info = oaFormProperties.getFieldMd5() + " 字段长度过长";
                        result.put("info", info);
                        break;
                    }
                    if (type.equals("D")) {
                        if (!ValidType.isNumeric(value)) {
                            info = oaFormProperties.getFieldMd5() + " 字段类型错误";
                            result.put("info", info);
                            break;
                        }
                    }
                }
                // 查找流程定义
                ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deploymentID).singleResult();

                Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentID).singleResult();
                Map<String, Object> mapInfo = new LinkedHashMap<>();
                mapInfo.put("user", loginUser.getName());
                mapInfo.put("init", "start");
                Date nowTime = new Date();
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
                mapInfo.put("title", deployment.getName() + "-" + time.format(nowTime));
                mapInfo.put("timeStamp", new Date().getTime());
                mapInfo.putAll(map);
                identityService.setAuthenticatedUserId(loginUser.getName());
                ProcessInstance processInstance = runtimeService.startProcessInstanceById(pd.getId(), mapInfo);
                if (index.equals("1")) {
                    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
                    taskService.addComment(task.getId(), processInstance.getId(), "提交");
                    taskService.complete(task.getId());
                    runtimeService.setVariable(processInstance.getProcessInstanceId(), "init", "");

                    OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = new OAContractCirculationWithBLOBs();
                    oaContractCirculationWithBLOBs.setTemplateId(oaDeploymentTemplateRelation.getRelationTemplateid());
                    oaContractCirculationWithBLOBs.setProcessInstanceId(processInstance.getId());
                    oaContractCirculationWithBLOBs.setContractHtml(map.get("html"));
                    contractCirculationService.insert(oaContractCirculationWithBLOBs);
                }
            } catch (Exception e) {
                result.put("result", "failed");
            }
        }
        return result;
    }

    //完成任务
    @RequestMapping(value = "complete", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object complete(HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
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
        //流程结束了
        if(pi==null){
            OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.selectByProcessInstanceId(processInstancesId);
            HistoricVariableInstance historicVariableInstance =  historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstancesId).variableName("title").singleResult();
//            VariableInstance variableInstance = runtimeService.getVariableInstance(processInstancesId,"title");
            String html = oaContractCirculationWithBLOBs.getContractHtml();
            StringBuilder sb = new StringBuilder(html.length()+200);
            sb.append("<html>");
            sb.append("<head>");
            sb.append("<title>");
            sb.append(historicVariableInstance.getValue().toString());
            sb.append("</title>");
            sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
            sb.append("<body>");
            sb.append(html);
            sb.append("</body></html>");
            try {
                String path = PropertyUtil.getValue("contract.template.path");
                String fileHtml = path+ historicVariableInstance.getValue().toString()+".html";
                String filePf = path+ historicVariableInstance.getValue().toString()+".pdf";
                PrintStream printStream = new PrintStream(new FileOutputStream(fileHtml));
                printStream.println(sb.toString());
                //转换成pdf文件
                File htmlFile = Word2Html.html2pdf(fileHtml);
                // 获取pdf文件流
                StringBuilder pdf = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            new FileInputStream(htmlFile),"GBK"));
                    while (br.ready()) {
                        pdf.append(br.readLine());
                    }
                    br.close();
                    // 删除临时文件
//            htmlFile.delete();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // HTML文件字符串
                oaContractCirculationWithBLOBs.setContractPdf(pdf.toString());
                contractCirculationService.update(oaContractCirculationWithBLOBs);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
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
        if(null == loginUser) throw new OAException(1101,"用户未登录");
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
    public Object findTaskByName(@PathVariable(value = "user", required = false) String user, HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        List<TaskInfo> taskInfos = new LinkedList<>();
        List<Task> list = taskService.createTaskQuery()// 创建任务查询对象
                .taskAssignee(loginUser.getName())// 指定个人认为查询，指定办理人
                .list();

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



    //获取该员工历史任务
    @RequestMapping(value = "processHistory", method = RequestMethod.GET)
    @ResponseBody
    public Object findHistoryTaskByName(@PathVariable(value = "user", required = false) String user,
                                        HttpServletRequest request)throws Exception {

        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        List<TaskInfo> taskInfos = new LinkedList<>();
        List<HistoricProcessInstance> listProcess = historyService.createHistoricProcessInstanceQuery().finished()
                .variableValueEquals("user",loginUser.getName())
                .orderByProcessInstanceStartTime().desc().list();
        long size = historyService.createHistoricProcessInstanceQuery().finished()
                .variableValueEquals("user",loginUser.getName())
                .count();
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
                        break;
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
//        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()// 创建任务查询对象
//                .taskAssignee(loginUser.getName())// 指定个人认为查询，指定办理人
//                .list();
//        long size = historyService.createHistoricTaskInstanceQuery().taskAssignee(loginUser.getName()).count();
//        if (list != null && list.size() > 0) {
//            for (HistoricTaskInstance task : list) {
//                TaskInfo taskInfo = new TaskInfo();
//                taskInfo.setId(task.getId());
//                taskInfo.setName(task.getName());
//                taskInfo.setTimestamp(task.getStartTime());
//                taskInfo.setAssignee(task.getAssignee());
//                taskInfo.setProcessInstanceId(task.getProcessInstanceId());
//                taskInfo.setExecutionId(task.getExecutionId());
//                taskInfo.setProcessDefinitionId(task.getProcessDefinitionId());
//                taskInfos.add(taskInfo);
//            }
//        }
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
    //查询我申请未提交的任务
    @RequestMapping(value = "myTask", method = RequestMethod.GET,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object getMyTask(HttpServletRequest request) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
        List<ProInstance> processInstanceList = new LinkedList<>();
        List<HistoricProcessInstance> historicProcessInstanceList = historyService.createHistoricProcessInstanceQuery()
//                .variableValueEquals("user",loginUser.getName())
                .startedBy(loginUser.getName())
                .unfinished()
//                .variableValueEquals("init","start")
                .list();
//        List<ProcessInstance> list = runtimeService.createProcessInstanceQuery().variableValueEquals("user",loginUser.getName())
//                .variableValueEquals("init","start").list();
//        long count = runtimeService.createProcessInstanceQuery().variableValueEquals("user",loginUser.getName())
//                .variableValueEquals("init","start").count();
        long count = historyService.createHistoricProcessInstanceQuery().variableValueEquals("user",loginUser.getName()) .variableValueEquals("init","start").count();
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
//            VariableInstance variableInstance = historicVariableInstance.getValue().get("title");
            if(historicVariableInstance != null) {
                proInstance.setTitle(historicVariableInstance.getValue().toString());
            }

            proInstance.setStatus("发起申请");
//            historicVariableInstance = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("timeStamp").singleResult();
//            variableInstance = stringVariableInstanceMap.get("timeStamp");
            if(historicVariableInstance != null) {
                proInstance.setCreateTime(historicVariableInstance.getCreateTime());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("rows", processInstanceList);
        result.put("total", count);
//        List<Task> taskList1 = taskService.createTaskQuery()
//                .taskAssignee(loginUser.getName()).list();
//        if (taskList1 != null && taskList1.size() > 0) {
//            for (Task task : taskList1) {
////                    TaskModel taskModel = new TaskModel();
//                // 获取部署名
//                String processdefintionId = task.getProcessDefinitionId();
//                ProcessDefinition processDefinition = repositoryService
//                        .createProcessDefinitionQuery()
//                        .processDefinitionId(processdefintionId)
//                        .singleResult();
//                // 根据taskname和节点判断是否是第一个
//                String taskName = task.getName();
//                Iterator<FlowElement> iterator = findFlow(processdefintionId);
//                String row0 = null;
//                String eleName0 = null;
//                while (iterator.hasNext()) {
//                    FlowElement flowElement0 = iterator.next();
//                    // 下一个节点
//                    FlowElement flowElement = iterator.next();
//                    String eleName = flowElement.getName();
//                    if (taskName.equals(eleName)) {
//                        row0 = flowElement0.getXmlRowNumber() + "";
//                        eleName0 = flowElement0.getClass().getSimpleName();
//                        break;
//                    }
//                }
//            }
//        }
        return result;
    }


    //获取所有审批记录
    @RequestMapping(value = "approval", method = RequestMethod.POST)
    @ResponseBody
    public Object getRecord(@PathVariable(value = "id",required = false) String taskId,
                            HttpServletRequest request,HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
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
//        list = taskService.getProcessInstanceComments(processInstanceId);
//        for (Comment com : list) {
//            System.out.println("ID:" + com.getId());
//            System.out.println("Message:" + com.getFullMessage());
//            System.out.println("TaskId:" + com.getTaskId());
//            System.out.println("ProcessInstanceId:" + com.getProcessInstanceId());
//            System.out.println("UserId:" + com.getUserId());
//        }
//
//        System.out.println(list);
        return response;
    }



}
