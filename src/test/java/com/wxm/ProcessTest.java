package com.wxm;

import com.wxm.entity.TaskComment;
import com.wxm.util.Md5Utils;
import com.wxm.util.Word2Html;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.history.*;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ProcessTest {
    public static ProcessEngine processEngine;

    @Before
    public void setUp() {
        processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE)
                .setJdbcUrl("jdbc:sqlserver://127.0.0.1:1433;DatabaseName=oa")
                .setJdbcDriver("com.microsoft.sqlserver.jdbc.SQLServerDriver")
                .setJdbcUsername("sa")
                .setJdbcPassword("123456")
                .setDatabaseSchemaUpdate("true")
                .setJobExecutorActivate(false)
                .buildProcessEngine();
    }

    //5.1、查询历史记录ACT_HI_PROCINST
    @Test
    public void queryProcessTaskHistory() throws Exception {
        HistoricProcessInstanceQuery query = processEngine.getHistoryService().createHistoricProcessInstanceQuery().finished();
        query.orderByProcessDefinitionId().desc();
        query.orderByProcessInstanceEndTime().asc();
        List<HistoricProcessInstance> list = query.list();
        for (HistoricProcessInstance hpi : list){
            System.out.println(hpi.getId() + ":" + hpi.getName());
        }

    }


    //5.2、查询历史活动记录ACT_HI_ACTINST
    @Test
    public void queryProcessTaskActHistory() throws Exception {
        HistoricActivityInstanceQuery query = processEngine.getHistoryService().createHistoricActivityInstanceQuery();
        List<HistoricActivityInstance> list = query.list();
        for (HistoricActivityInstance hpi : list){
            System.out.println(hpi.getId() + ":" + hpi.getActivityName() + ":" + hpi.getActivityType());
        }

    }

    //创建流程
    @Test
    public void test01() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("user","张三");
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("process",map);
        System.out.println("流程梳理所属流程定义id："
                + processInstance.getProcessDefinitionId());
        System.out.println("流程实例的id：" + processInstance.getProcessInstanceId());
        System.out.println("流程实例的执行id：" + processInstance.getId());
        System.out.println("流程当前的活动（结点）id：" + processInstance.getActivityId());
        System.out.println("业务标识：" + processInstance.getBusinessKey());

    }

    @Test
    public void getProcessInstance(){
        List<ProcessInstance> list = processEngine.getRuntimeService().createProcessInstanceQuery().variableValueEquals("user","张三").list();
        int i = 0;
    }
      @Test
      public void getComments(){
          String taskId = "42503";
          List<TaskComment> taskCommentList = new LinkedList<>();
          List<Comment> list = new ArrayList<>();
//使用当前的任务ID，查询当前流程对应的历史任务ID

//使用当前任务ID，获取当前任务对象
          Task task = processEngine.getTaskService().createTaskQuery()//
                  .taskId(taskId)//使用任务ID查询
                  .singleResult();

          Map<String, VariableInstance> stringVariableInstanceMap = processEngine.getRuntimeService().getVariableInstances(task.getExecutionId());
//获取流程实例ID
          String processInstanceId = task.getProcessInstanceId();
//使用流程实例ID，查询历史任务，获取历史任务对应的每个任务ID
          List<HistoricTaskInstance> htiList = processEngine.getHistoryService().createHistoricTaskInstanceQuery()//历史任务表查询
                  .processInstanceId(processInstanceId)//使用流程实例ID查询
                  .list();
//遍历集合，获取每个任务ID
          if (htiList != null && htiList.size() > 0) {
              for (HistoricTaskInstance hti : htiList) {//任务ID
                  String htaskId = hti.getId();//获取批注信息
                  List<Comment> taskList = processEngine.getTaskService().getTaskComments(htaskId);//对用历史完成后的任务ID
                  for(Comment comment :taskList){

                  }
//                list.addAll(taskList);
              }
          }
          list = processEngine.getTaskService().getProcessInstanceComments(processInstanceId);

          for (Comment com : list) {
              System.out.println("ID:" + com.getId());
              System.out.println("Message:" + com.getFullMessage());
              System.out.println("TaskId:" + com.getTaskId());
              System.out.println("ProcessInstanceId:" + com.getProcessInstanceId());
              System.out.println("UserId:" + com.getUserId());
          }

          System.out.println(list);
      }
    //查询流程
    @Test
    public void test02() throws IOException {
        List<ProcessDefinition> list = processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                .createProcessDefinitionQuery()//创建一个流程定义查询
                /*指定查询条件,where条件*/
                //.deploymentId(deploymentId)//使用部署对象ID查询
                //.processDefinitionId(processDefinitionId)//使用流程定义ID查询
                //.processDefinitionKey(processDefinitionKey)//使用流程定义的KEY查询
                //.processDefinitionNameLike(processDefinitionNameLike)//使用流程定义的名称模糊查询

                /*排序*/
                .orderByProcessDefinitionVersion().asc()//按照版本的升序排列
                //.orderByProcessDefinitionName().desc()//按照流程定义的名称降序排列

                .list();//返回一个集合列表，封装流程定义

        long size = processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                .createProcessDefinitionQuery().count();
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
    @Test
    public void add() {
//        User user = processEngine.getIdentityService().newUser("张三");
//        processEngine.getIdentityService().saveUser(user);
        processEngine.getIdentityService().createUserQuery().list();
        User user = processEngine.getIdentityService().newUser("李四");
        processEngine.getIdentityService().saveUser(user);
    }
    @Test
    public void complete() {
        processEngine.getTaskService().addComment("40004", "40001", "同意");
        processEngine.getTaskService().complete("40004");
    }
    @Test
    public void findMyPersonTask() {
        String assignee = "李四";
        List<Task> list = processEngine.getTaskService()// 与正在执行的认为管理相关的Service
                .createTaskQuery()// 创建任务查询对象
                .taskAssignee(assignee)// 指定个人认为查询，指定办理人
                .list();

        if (list != null && list.size() > 0) {
            for (Task task:list) {
                System.out.println("任务ID:"+task.getId());
                System.out.println("任务名称:"+task.getName());
                System.out.println("任务的创建时间"+task);
                System.out.println("任务的办理人:"+task.getAssignee());
                System.out.println("流程实例ID:"+task.getProcessInstanceId());
                System.out.println("执行对象ID:"+task.getExecutionId());
                System.out.println("流程定义ID:"+task.getProcessDefinitionId());
                System.out.println("#################################");
            }
        }

    }
    @Test
    public void testHtml2Pdf(){
        File htmlFile = Word2Html.html2pdf("F:\\tmp\\oa\\demo.html","C:/Program Files (x86)/OpenOffice 4");
    }
    @Test
    public void testHtml(){


        // 要验证的字符串
        String str = "baike.xsoftlab.net<U>@@次数%%D##2</U>nihao";
        String testString = "java怎么利用<a href=\"https://www.baidu.com/s?wd=%E6%AD%A3%E5%88%99%E8%A1%A8%E8%BE%BE%E5%BC%8F&tn=44039180_cpr&fenlei=mv6quAkxTZn0IZRqIHckPjm4nH00T1Y3PH63nj7hm163PHbkuWT10ZwV5Hcvrjm3rH6sPfKWUMw85HfYnjn4nH6sgvPsT6KdThsqpZwYTjCEQLGCpyw9Uz4Bmy-bIi4WUvYETgN-TLwGUv3EnW0LP1bvnWfYPj0vP1bLPjTsr0\" target=\"_blank\" class=\"baidu-highlight\">正则表达式</a>从给定的字符串中取出匹配规则字符串";
//        Pattern pattern = Pattern.compile("<U>([^</U>]*)");
        Pattern pattern = Pattern.compile("<U>([\\s\\S]*?)</U>");

        Matcher matcher = pattern.matcher(str);
        while(matcher.find()) {
            String tmp = matcher.group();
            String varable = tmp.substring(5,tmp.indexOf("%%"));
            String type = tmp.substring(tmp.indexOf("%%")+2,tmp.indexOf("##"));
            String length = tmp.substring(tmp.indexOf("##")+2,tmp.indexOf("</"));
            int start = matcher.start();
            String name = "name_" + Md5Utils.getMd5(String.format("%s%s%s%s",varable,type,length,start));
            int i= 0;
        }
    }
}
