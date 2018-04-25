package com.wxm.service.impl;

import com.wxm.service.TaskProcessService;
import org.activiti.engine.*;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TaskProcessServiceImpl implements TaskProcessService {
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void jump(String targetTaskDefinitionKey,String processId) {
        TaskEntity currentTask = (TaskEntity)taskService
                .createTaskQuery()
                .processInstanceId(processId).singleResult();
        jump(currentTask,targetTaskDefinitionKey);
    }
    /**
     * @param currentTaskEntity 当前任务节点
     * @param targetTaskDefinitionKey  目标任务节点（在模型定义里面的节点名称）
     */
    private void jump(final TaskEntity currentTaskEntity, String targetTaskDefinitionKey){
        final ActivityImpl activity = getActivity(currentTaskEntity.getProcessDefinitionId(),targetTaskDefinitionKey);
        final ExecutionEntity execution = (ExecutionEntity)runtimeService
                .createExecutionQuery().executionId(currentTaskEntity.getExecutionId()).singleResult();
        ((RuntimeServiceImpl)runtimeService).getCommandExecutor()
                .execute(new Command<Void>() {
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

    private ActivityImpl getActivity(String processDefId, String activityId)
    {
        ProcessDefinitionEntity pde = getProcessDefinition(processDefId);
        return pde.findActivity(activityId);
    }

    private ProcessDefinitionEntity getProcessDefinition( String processDefId)
    {
        return (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(processDefId);
    }
}
