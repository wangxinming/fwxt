package com.wxm.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxm.activiti.vo.DeploymentResponse;
import com.wxm.entity.WordEntity;
import com.wxm.service.WordTemplateService;
import com.wxm.util.Status;
import com.wxm.util.ToWeb;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/deployments")
public class DeployController {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private WordTemplateService wordTemplateService;

    @RequestMapping(value = "/saveUploadFile",method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object saveUploadFile(HttpServletRequest request)throws Exception {
        String id = request.getParameter("id");
        String name = request.getParameter("name");
        String des = request.getParameter("des");
        String html = request.getParameter("html");
        WordEntity wordEntity = new WordEntity();
        wordEntity.setId(Integer.parseInt(id));
        wordEntity.setName(name);
        wordEntity.setDes(des);
        wordEntity.setHtml(html);
//        WordEntity wordEntity = wordTemplateService.queryHtmlbyId(Integer.parseInt(id));
        wordTemplateService.update(wordEntity);
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
//        result.put("data",wordEntity);
        return result;
    }
    @RequestMapping(value = "/uploadFileInfo", method = RequestMethod.GET)
    @ResponseBody
    public Object uploadFileInfo(HttpServletRequest request)throws Exception {
        String id = request.getParameter("id");
        WordEntity wordEntity = wordTemplateService.queryHtmlbyId(Integer.parseInt(id));

        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        result.put("data",wordEntity);
        return result;
    }



    @RequestMapping(value = "/uploadFile", method = RequestMethod.GET)
    @ResponseBody
    public Map uploadFile(HttpServletRequest request)throws Exception {
        int count = wordTemplateService.count();
        List<WordEntity> list = wordTemplateService.queryHtmlTemplate();
        Map<String, Object> result = new HashMap<>();
        result.put("rows",list);
        result.put("total",count);
        return result;
    }

    @RequestMapping(value = "/removeModeler", method = RequestMethod.DELETE)
    @ResponseBody
    public Map deleteModeler(HttpServletRequest request)throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            String id = request.getParameter("id");
            repositoryService.deleteModel(id);
//        log.error("delete guid: "+id);
        }catch (Exception e){
            result.put("result", "failed");
        }
        return result;
    }


    @RequestMapping(value = "/remove", method = RequestMethod.DELETE)
    @ResponseBody
    public Map delete(HttpServletRequest request)throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            String id = request.getParameter("id");
            repositoryService.deleteDeployment(id, true);
//        log.error("delete guid: "+id);
        }catch (Exception e){
            result.put("result", "failed");
        }
        return result;
    }
    @RequestMapping(value = "/publish", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    @ResponseBody
    public Map publish(HttpServletRequest request)throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        String id= request.getParameter("id");
        try {
            //获取模型
            Model modelData = repositoryService.getModel(id);
            byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

            if (bytes == null) {
                result.put("result", "failed");
                return result;
            }

            JsonNode modelNode = new ObjectMapper().readTree(bytes);

            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            if(model.getProcesses().size()==0){
                result.put("result", "failed");
                return result;
            }
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

            //发布流程
            String processName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .addString(processName, new String(bpmnBytes, "UTF-8"))
                    .deploy();
            modelData.setDeploymentId(deployment.getId());
            repositoryService.saveModel(modelData);
        }catch (Exception e){
            result.put("result", "failed");
        }
        return result;
    }

    @RequestMapping(value = "html",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object html(){
        String tmp = " <form action=\"/workflow/process/process\" method=\"get\">\n" +
                "          <p>First name: <input type=\"text\" name=\"fname\" /></p>\n" +
                "          <p>Last name: <input type=\"text\" name=\"lname\" /></p>\n" +
                "          <input type=\"submit\" value=\"Submit\" />\n" +
                "        </form>";
        Map<String, Object> result = new HashMap<>();
        result.put("result",tmp);
        return result;
    }


    @RequestMapping(value = "deploymentList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object list (@RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset, @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit){
        List<Deployment> deployments = repositoryService.createDeploymentQuery()
                .listPage(offset, limit);
        long count = repositoryService.createDeploymentQuery().count();
        List<com.wxm.entity.Deployment> list = new ArrayList<>();
        for(Deployment deployment: deployments){
            com.wxm.entity.Deployment deploy= new com.wxm.entity.Deployment();
            deploy.setId(deployment.getId());
            deploy.setName(deployment.getName());
            deploy.setCategory(deployment.getCategory());
            deploy.setDeploymentTime(deployment.getDeploymentTime());
            deploy.setTenantId(deployment.getTenantId());
            list.add(deploy);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("rows",list);
        result.put("total",count);
        return  result;

    }

    @RequestMapping(value = "modelerList",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object getList(@RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset, @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit) {
        List<Model> list = repositoryService.createModelQuery().listPage(offset, limit);
        long count = repositoryService.createModelQuery().count();
        Map<String, Object> result = new HashMap<>();
        result.put("rows",list);
        result.put("total",count);
        return  result;
    }

}
