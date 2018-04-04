package com.wxm.controller;

import com.wxm.entity.TaskInfo;
import org.activiti.engine.FormService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/workflow/process/")
public class ProcessController {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    FormService formService;
     //完成任务
     @RequestMapping(value = "complete/{id}", method = RequestMethod.POST)
     @ResponseBody
    public void complete(@PathVariable("id") String taskId,HttpServletRequest request){
        taskService.complete(taskId);
    }
    @RequestMapping(value = "process", method = RequestMethod.POST)
    @ResponseBody
    //流程定义查询,@PathVariable("start") int start,@PathVariable("offset",int offset)
    public void ProcessDef( ){
        long size = repositoryService.createProcessDefinitionQuery().count();
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion().asc()
                .listPage(0,10);
        if(list != null && list.size()>0){
            for(ProcessDefinition processDefinition:list){
                System.out.println("流程定义ID:"+processDefinition.getId());//流程定义的key+版本+随机生成数
                System.out.println("流程定义名称:"+processDefinition.getName());//对应HelloWorld.bpmn文件中的name属性值
                System.out.println("流程定义的key:"+processDefinition.getKey());//对应HelloWorld.bpmn文件中的id属性值
                System.out.println("流程定义的版本:"+processDefinition.getVersion());//当流程定义的key值相同的情况下，版本升级，默认从1开始
                System.out.println("资源名称bpmn文件:"+processDefinition.getResourceName());
                System.out.println("资源名称png文件:"+processDefinition.getDiagramResourceName());
                System.out.println("部署对象ID:"+processDefinition.getDeploymentId());
                System.out.println("################################");
            }
        }
    }
    @RequestMapping(value = "process", method = RequestMethod.GET)
    @ResponseBody
    public Object findTaskByName(@PathVariable(value="user",required = false) String user ,HttpServletRequest request){
        List<TaskInfo> taskInfos = new LinkedList<>();
        List<Task> list = taskService.createTaskQuery()// 创建任务查询对象
                .taskAssignee(user)// 指定个人认为查询，指定办理人
                .list();
        long size = taskService.createTaskQuery().taskAssignee(user).count();
        if (list != null && list.size() > 0) {
            for (Task task:list) {
                TaskInfo taskInfo = new TaskInfo();
                taskInfo.setId(task.getId());
                taskInfo.setName(task.getName());
                taskInfo.setTimestamp(task.getCreateTime());
                taskInfo.setAssignee(task.getAssignee());
                taskInfo.setProcessInstanceId(task.getProcessInstanceId());
                taskInfo.setExecutionId(task.getExecutionId());
                taskInfo.setProcessDefinitionId(task.getProcessDefinitionId());
                taskInfos.add(taskInfo);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("rows",taskInfos);
        result.put("total",size);
        return  request;
    }
}
