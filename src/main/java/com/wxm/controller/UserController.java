package com.wxm.controller;

import com.wxm.entity.ProcessDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping({"/home"})
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @RequestMapping(value = "/demo",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object demo(HttpServletRequest request){
        List<ProcessDef> processDefList = new LinkedList<>();
        for(int i =1;i<4;i++){
            ProcessDef processDef = new ProcessDef();
//            processDef.setId(i);
//            processDef.setLimit(1);
//            processDef.setOffset(10);
//            Process process = new Process();
//            processDef.setProcess(process);
//            process.setId(1);
//            process.setLimit(10);
//            process.setOffset(10);
            processDefList.add(processDef);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("rows",processDefList);
        result.put("total",3);
        return result;
    }
    @RequestMapping(value = "/process",method = {RequestMethod.POST},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object process(@RequestParam(value = "start",required = false)Integer start,@RequestParam(value = "offset",required = false)Integer offset){
        String str = "{\n" +
                "  \"draw\": 1,\n" +
                "  \"recordsTotal\": 57,\n" +
                "  \"recordsFiltered\": 57,\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"first_name\": \"Airi\",\n" +
                "      \"last_name\": \"Satou\",\n" +
                "      \"position\": \"Accountant\",\n" +
                "      \"office\": \"Tokyo\",\n" +
                "      \"start_date\": \"28th Nov 08\",\n" +
                "      \"salary\": \"$162,700\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"first_name\": \"Angelica\",\n" +
                "      \"last_name\": \"Ramos\",\n" +
                "      \"position\": \"Chief Executive Officer (CEO)\",\n" +
                "      \"office\": \"London\",\n" +
                "      \"start_date\": \"9th Oct 09\",\n" +
                "      \"salary\": \"$1,200,000\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"first_name\": \"Ashton\",\n" +
                "      \"last_name\": \"Cox\",\n" +
                "      \"position\": \"Junior Technical Author\",\n" +
                "      \"office\": \"San Francisco\",\n" +
                "      \"start_date\": \"12th Jan 09\",\n" +
                "      \"salary\": \"$86,000\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"first_name\": \"Bradley\",\n" +
                "      \"last_name\": \"Greer\",\n" +
                "      \"position\": \"Software Engineer\",\n" +
                "      \"office\": \"London\",\n" +
                "      \"start_date\": \"13th Oct 12\",\n" +
                "      \"salary\": \"$132,000\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"first_name\": \"Brenden\",\n" +
                "      \"last_name\": \"Wagner\",\n" +
                "      \"position\": \"Software Engineer\",\n" +
                "      \"office\": \"San Francisco\",\n" +
                "      \"start_date\": \"7th Jun 11\",\n" +
                "      \"salary\": \"$206,850\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"first_name\": \"Brielle\",\n" +
                "      \"last_name\": \"Williamson\",\n" +
                "      \"position\": \"Integration Specialist\",\n" +
                "      \"office\": \"New York\",\n" +
                "      \"start_date\": \"2nd Dec 12\",\n" +
                "      \"salary\": \"$372,000\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"first_name\": \"Bruno\",\n" +
                "      \"last_name\": \"Nash\",\n" +
                "      \"position\": \"Software Engineer\",\n" +
                "      \"office\": \"London\",\n" +
                "      \"start_date\": \"3rd May 11\",\n" +
                "      \"salary\": \"$163,500\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"first_name\": \"Caesar\",\n" +
                "      \"last_name\": \"Vance\",\n" +
                "      \"position\": \"Pre-Sales Support\",\n" +
                "      \"office\": \"New York\",\n" +
                "      \"start_date\": \"12th Dec 11\",\n" +
                "      \"salary\": \"$106,450\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"first_name\": \"Cara\",\n" +
                "      \"last_name\": \"Stevens\",\n" +
                "      \"position\": \"Sales Assistant\",\n" +
                "      \"office\": \"New York\",\n" +
                "      \"start_date\": \"6th Dec 11\",\n" +
                "      \"salary\": \"$145,600\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"first_name\": \"Cedric\",\n" +
                "      \"last_name\": \"Kelly\",\n" +
                "      \"position\": \"Senior Javascript Developer\",\n" +
                "      \"office\": \"Edinburgh\",\n" +
                "      \"start_date\": \"29th Mar 12\",\n" +
                "      \"salary\": \"$433,060\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        return str;
    }
//
//    @RequestMapping(value = "/user")
//    @ResponseBody
//    public String user(){
//        WordEntity wordEntity = new WordEntity();
//        wordEntity.setName("模板");
//        wordEntity.setDes("描述");
//        wordEntity.setCreateTime(new Date());
//        wordEntity.setHtml("<div>hello</div>");
//
//        int i = wordTemplateService.insert(wordEntity);
//        WordEntity temp = wordTemplateService.queryHtmlbyName("模板");
//
//        wordEntity.setDes("描述1");
//        wordTemplateService.update(wordEntity);
//
//
//        wordTemplateService.delete(temp.getId());
////        User user = userMapper.findUserByName("王伟");
////        return user.getName()+"-----"+user.getAge();
//        return null;
//    }
}