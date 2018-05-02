package com.wxm;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Before;
import org.junit.Test;

public class AutoTest {

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

    @Test
    public void test(){
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("process");
        Task task = processEngine.getTaskService().newTask();
    }
}
