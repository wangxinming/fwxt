package com.wxm.controller;

import com.wxm.entity.*;
import com.wxm.service.WordTemplateFieldService;
import com.wxm.service.WordTemplateService;
import com.wxm.util.ValidType;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping(value = "/workflow/process/")
public class ProcessController {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private FormService formService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private WordTemplateFieldService wordTemplateFieldService;
    @Autowired
    private HistoryService historyService;

    @Autowired
    private WordTemplateService wordTemplateService;
    //开始发起任务
    @RequestMapping(value = "start", method = RequestMethod.POST)
    @ResponseBody
    public Object start(HttpServletRequest request, HttpServletResponse response,@RequestBody Map<String,String> map) {
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try {
            String info = "";
            com.wxm.entity.User loginUser = (com.wxm.entity.User) request.getSession().getAttribute("loginUser");
            String deploymentID = map.get("id");

            WordEntity wordEntity = wordTemplateService.queryInfoRel(deploymentID);
            List<WordTemplateField> list = wordTemplateFieldService.getWordTemplateFieldByTemplateId(wordEntity.getId());
            for (WordTemplateField wordTemplateField : list) {
                String md5 = wordTemplateField.getFieldMd5().trim();
                String type = wordTemplateField.getType().trim();
                String length = wordTemplateField.getLength().trim();
                String value = map.get(md5).toString();
                if (value.length() > Integer.parseInt(length)) {
                    info = wordTemplateField.getFieldMd5() + " 字段长度过长";
                    result.put("info", info);
                    break;
                }
                if (type.equals("D")) {
                    if (!ValidType.isNumeric(value)) {
                        info = wordTemplateField.getFieldMd5() + " 字段类型错误";
                        result.put("info", info);
                        break;
                    }
                }
            }
            // 查找流程定义
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deploymentID).singleResult();

//        User loginUser=(User)request.getSession().getAttribute("loginUser");
//         taskService.complete(taskId);

//        identityService.setAuthenticatedUserId(userId);
//        String key = "new-process";


            Map<String, Object> mapInfo = new LinkedHashMap<>();
            mapInfo.put("user", loginUser.getName());
            mapInfo.put("timeStamp", new Date().getTime());
            ProcessInstance processInstance = runtimeService.startProcessInstanceById(pd.getId(), mapInfo);
        }catch (Exception e){
            result.put("result","failed");
        }
        //TODO 获取数据库模板字段，并校验是否符合
//        return "<div><a href=\"javascript:window.opener=null;window.open('','_self');window.close();\">提交成功，请确认</a></div>";
        return result;
    }

    //完成任务
    @RequestMapping(value = "complete", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object complete(HttpServletRequest request) {
        String taskId = request.getParameter("id");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
//利用任务对象，获取流程实例id
        String processInstancesId = task.getProcessInstanceId();
//         Authentication.setAuthenticatedUserId("cmc"); // 添加批注时候的审核人，通常应该从session获取
        taskService.addComment(taskId, processInstancesId, "同意");
        taskService.complete(taskId);
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        return result;
    }

    //流程定义查询,@PathVariable("start") int start,@PathVariable("offset",int offset)
    @RequestMapping(value = "process", method = RequestMethod.POST)
    @ResponseBody
    public void ProcessDef() {
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

    //获取该人员在处理任务
    @RequestMapping(value = "process", method = RequestMethod.GET)
    @ResponseBody
    public Object findTaskByName(@PathVariable(value = "user", required = false) String user, HttpServletRequest request) {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
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
    public Object findHistoryTaskByName(@PathVariable(value = "user", required = false) String user, HttpServletRequest request) {

        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        List<TaskInfo> taskInfos = new LinkedList<>();
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()// 创建任务查询对象
                .taskAssignee(loginUser.getName())// 指定个人认为查询，指定办理人
                .list();
        long size = historyService.createHistoricTaskInstanceQuery().taskAssignee(loginUser.getName()).count();
        if (list != null && list.size() > 0) {
            for (HistoricTaskInstance task : list) {
                TaskInfo taskInfo = new TaskInfo();
                taskInfo.setId(task.getId());
                taskInfo.setName(task.getName());
                taskInfo.setTimestamp(task.getStartTime());
                taskInfo.setAssignee(task.getAssignee());
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
    //获取所有审批记录

    @RequestMapping(value = "approval", method = RequestMethod.POST)
    @ResponseBody
    public Object getRecord(@PathVariable(value = "id",required = false) String taskId,HttpServletResponse response) {
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
