package com.wxm.service;


import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class AssigneeSearch implements TaskListener,ExecutionListener {
    @Autowired
    private UserService userService;
    @Override
    public void notify(DelegateTask delegateTask) {
        String userId = delegateTask.getVariable("applyUserId").toString();
        if ("user01".equalsIgnoreCase(userId) ) {
            delegateTask.setVariable("userId", "leaderuser");
        }
    }

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        if(null == userService){
            userService = ApplicationcontextHandler.getBean("userServiceImpl");
        }
        delegateExecution.setVariable("user","王五");
        System.out.println("xml流程：" + delegateExecution.getId() + " ActivitiListenner" + this.toString());
    }
}
