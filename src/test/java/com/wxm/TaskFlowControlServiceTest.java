package com.wxm;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskFlowControlServiceTest {
    public static ProcessEngine processEngine;
    public static class TaskFlowControlService{
        ProcessEngine _processEngine;
        private String _processId;

        public TaskFlowControlService(ProcessEngine processEngine, String processId)
        {
            this._processEngine = processEngine;
            this._processId = processId;
        }

        /**
         *  跳转至指定活动节点
         * @param targetTaskDefinitionKey
         */
        public void jump(String targetTaskDefinitionKey){
            TaskEntity currentTask = (TaskEntity)_processEngine.getTaskService()
                    .createTaskQuery()
                    .processInstanceId(_processId).singleResult();
            jump(currentTask,targetTaskDefinitionKey);
        }

        /**
         * @param currentTaskEntity 当前任务节点
         * @param targetTaskDefinitionKey  目标任务节点（在模型定义里面的节点名称）
         */
        private void jump(final TaskEntity currentTaskEntity, String targetTaskDefinitionKey){
            final ActivityImpl activity = getActivity(_processEngine,
                    currentTaskEntity.getProcessDefinitionId(),targetTaskDefinitionKey);
            final ExecutionEntity execution = (ExecutionEntity)_processEngine.getRuntimeService()
                    .createExecutionQuery().executionId(currentTaskEntity.getExecutionId()).singleResult();
            final TaskService taskService = _processEngine.getTaskService();
            ((RuntimeServiceImpl)_processEngine.getRuntimeService()).getCommandExecutor()
                    .execute(new Command<java.lang.Void>() {
                        public Void execute(CommandContext commandContext) {

                            //创建新任务
                            execution.setActivity(activity);
                            execution.executeActivity(activity);

                            //删除当前的任务
                            //不能删除当前正在执行的任务，所以要先清除掉关联
                            currentTaskEntity.setExecutionId(null);
                            taskService.saveTask(currentTaskEntity);
                            taskService.deleteTask(currentTaskEntity.getId(),true);
                            return null;
                        }
                    });
        }

        private ActivityImpl getActivity(ProcessEngine processEngine, String processDefId, String activityId)
        {
            ProcessDefinitionEntity pde = getProcessDefinition(processEngine, processDefId);
            return pde.findActivity(activityId);
        }

        private ProcessDefinitionEntity getProcessDefinition(ProcessEngine processEngine, String processDefId)
        {
            return (ProcessDefinitionEntity) ((RepositoryServiceImpl) processEngine.getRepositoryService())
                    .getDeployedProcessDefinition(processDefId);
        }
    }

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


    @Test
    public void testTaskSequence(){
        ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("请假");
        String instanceId = instance.getId();

        TaskService taskService = processEngine.getTaskService();
        Task task1 = taskService.createTaskQuery().processInstanceId(instanceId).singleResult();
        Assert.assertEquals("sid-7532006C-BDA3-4C3B-8A80-0C968B117819", task1.getTaskDefinitionKey());

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("vacationApproved", false);
        vars.put("numberOfDays", 10);
        vars.put("managerMotivation", "get sick");


//        String taskId = taskService.createTaskQuery().taskCandidateUser("kermit").singleResult().getId();
        taskService.complete(task1.getId(), vars);
        Task task2 = taskService.createTaskQuery().processInstanceId(instanceId).singleResult();
        String str2 = task2.getTaskDefinitionKey();
        taskService.complete(task2.getId());

        Task task3 = taskService.createTaskQuery().processInstanceId(instanceId).singleResult();
        String str3 = task3.getTaskDefinitionKey();

        TaskFlowControlService taskFlowControlService = new TaskFlowControlService(processEngine,instanceId);
        taskFlowControlService.jump("sid-7532006C-BDA3-4C3B-8A80-0C968B117819");




//        //确认权限都拷贝过来了
//        //management可以访问该task
//        Assert.assertEquals(1, taskService.createTaskQuery().taskCandidateGroup("management").count());
//        //engineering不可以访问该task
//        Assert.assertEquals(0, taskService.createTaskQuery().taskCandidateGroup("engineering").count());

        //确认历史轨迹里已保存
        List<HistoricActivityInstance> activities = processEngine.getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(instanceId).list();
//        Assert.assertEquals(5, activities.size());
//        Assert.assertEquals("step1", activities.get(0).getActivityId());
//        Assert.assertEquals("step2", activities.get(1).getActivityId());
//        Assert.assertEquals("requestApprovedDecision", activities.get(2).getActivityId());
//        Assert.assertEquals("adjustVacationRequestTask", activities.get(3).getActivityId());
//        Assert.assertEquals("step2", activities.get(4).getActivityId());

        //测试一下往前跳
        taskFlowControlService.jump(str3);
        Task task4 = taskService.createTaskQuery().processInstanceId(instanceId).singleResult();
        Assert.assertEquals("adjustVacationRequestTask", task4.getTaskDefinitionKey());

        activities = processEngine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(instanceId).list();
        Assert.assertEquals(6, activities.size());
        Assert.assertEquals("adjustVacationRequestTask", activities.get(5).getActivityId());
        processEngine.getRuntimeService().deleteProcessInstance(instanceId, "test");

    }
}
