package com.wxm;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

public class AutoTest {

    public static ProcessEngine processEngine;
    public static void test111(){
        String str = "<input type=\"checkbox\" name=\"name_750C6E3C4EAB34A2F328D9A893F2172D\" id=\"name_750C6E3C4EAB34A2F328D9A893F2172D\"/>";
        String name = str.substring(str.indexOf("id=")+4);
        name = name.substring(0,name.indexOf("\""));
        int yy = str.length();
        System.out.print("rrrrr");
    }
    public static void main(String[] args) {
        try {
            new AutoTest().test111();
            PDFMergerUtility mergePdf = new PDFMergerUtility();
            mergePdf.addSource(new File("C:\\Users\\Administrator\\Desktop\\technology-en.pdf"));
            mergePdf.addSource(new File("C:\\Users\\Administrator\\Desktop\\微服务框架.pdf"));
            mergePdf.addSource(new File("C:\\Users\\Administrator\\Desktop\\功能设计.pdf"));
            mergePdf.setDestinationFileName("C:\\Users\\Administrator\\Desktop\\test.pdf");
            mergePdf.mergeDocuments();
            int i = 0;
        }catch (Exception e){

        }

        String contentString = "sdfsd abc---abc <a href='http://www.hao123.com'>" +
                "http://www.hao123.com</a><img title='img' src='abc' >" +
                "sdfsdfds";
        contentString=contentString.replaceAll("<a href[^>]*>", "");
        contentString=contentString.replaceAll("</a>", "");
        contentString=contentString.replaceAll("<img[^>]*>", " ");
        System.out.println(contentString);
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
    public void test(){
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("process");
        Task task = processEngine.getTaskService().newTask();
    }
}
