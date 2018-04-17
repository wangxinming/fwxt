package com.wxm.controller;

import com.wxm.entity.WordEntity;
import com.wxm.entity.WordTemplateField;
import com.wxm.model.OAContractTemplate;
import com.wxm.model.OAFormProperties;
import com.wxm.service.ConcactTemplateService;
import com.wxm.service.FormPropertiesService;
import com.wxm.service.WordTemplateFieldService;
import com.wxm.service.WordTemplateService;
import com.wxm.util.HtmlProcess;
import com.wxm.util.Md5Utils;
import com.wxm.util.PropertyUtil;
import com.wxm.util.Word2Html;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping(value = "/template")
public class WordTemplateController {

    @Autowired
    private ConcactTemplateService concactTemplateService;

    @Autowired
    private FormPropertiesService formPropertiesService;

    @Autowired
    private WordTemplateService wordTemplateService;
    @Autowired
    private WordTemplateFieldService wordTemplateFieldService;

    @RequestMapping(value="/deleteWordTemplate",method= RequestMethod.DELETE,produces="application/json;charset=UTF-8")
    public Object deleteWordTemplate( HttpServletRequest request,
                                      @RequestParam(value = "id", defaultValue = "0", required = true) Integer id) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
//        String id = request.getParameter("id");
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{
            if(id != 0) {
                concactTemplateService.delete(id);
                formPropertiesService.delete(id);
//                wordTemplateService.removeRelationByTmpId(Integer.parseInt(id));
//                wordTemplateService.delete(Integer.parseInt(id));
            }
        }catch (Exception e){
            result.put("result","failed");
        }
        return result;
    }

    @RequestMapping(value="/fieldList",method= RequestMethod.GET,produces="application/json;charset=UTF-8")
    public Object fieldList( HttpServletRequest request,
                             @RequestParam(value = "offset", defaultValue = "0", required = true) Integer offset,
                             @RequestParam(value = "limit", defaultValue = "10", required = true) Integer limit,
                             @RequestParam(value = "templateName", defaultValue = "", required = false) String templateName
                             )throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
//        List<WordTemplateField> list =  wordTemplateFieldService.getWordTemplateField();
//        for(WordTemplateField wordTemplateField : list){
//            WordEntity  wordEntity = wordTemplateService.queryHtmlbyId(wordTemplateField.getTemplateId());
//            if(null != wordEntity)
//                wordTemplateField.setName(wordEntity.getName());
//        }
//        List<WordTemplateField> listTmp = new LinkedList<>();
//        for(int i=0;i< 10;i++){
//            listTmp.add(list.get(i));
//        }
//        int count = wordTemplateFieldService.countTotal();

        Map<String, Object> result = new HashMap<>();
        result.put("rows",formPropertiesService.list(offset,limit,templateName));
        result.put("total",formPropertiesService.count(templateName));
        return result;
    }
    @RequestMapping(value="/updateFieldInfo",method= RequestMethod.POST,produces="application/json;charset=UTF-8")
    public Object updateFieldInfo(@RequestBody OAFormProperties oaFormProperties,HttpServletRequest request) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try {
//            String id = request.getParameter("id");
//            WordTemplateField wordTemplateField = new WordTemplateField();
//            wordTemplateField.setId(Integer.parseInt(id));
//            wordTemplateField.setType(request.getParameter("type"));
//            wordTemplateField.setLength(request.getParameter("length"));
//            wordTemplateFieldService.update(wordTemplateField);

            formPropertiesService.update(oaFormProperties);
        }catch (Exception e){
            result.put("result","failed");
        }
        return result;
    }

    @RequestMapping(value="/templateListTotal",method= RequestMethod.GET,produces="application/json;charset=UTF-8")
    public Object templateList( HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("rows",concactTemplateService.listTemplate());
        result.put("total",concactTemplateService.count());

        return result;
    }

    @RequestMapping(value="/templateList",method= RequestMethod.GET,produces="application/json;charset=UTF-8")
    public Object templateList( HttpServletRequest request,
                                @RequestParam(value = "offset", required=true) Integer offset,
                                @RequestParam(value = "limit", required=true) Integer limit
                                ) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
//        List<WordEntity> list =  wordTemplateService.queryHtmlTemplate();
//        int count = wordTemplateService.count();
//        Map<String, Object> result = new HashMap<>();
//        result.put("rows",list);
//        result.put("total",count);
        Map<String, Object> result = new HashMap<>();
        result.put("rows",concactTemplateService.list(offset,limit));
        result.put("total",concactTemplateService.count());

        return result;
    }
//    @RequestMapping(value="/templateUpdate",method= RequestMethod.GET)
//    public Object templateUpdate( HttpServletRequest request) {
//        String id = request.getParameter("id");
//        int ids = Integer.parseInt(id);
//        String name = request.getParameter("name");
//        String des = request.getParameter("des");
//        String html = request.getParameter("html");
//        WordEntity wordEntity =  wordTemplateService.queryHtmlbyId(ids);
//        if(name != null) {
//            wordEntity.setName(name);
//        }
//        if(des != null){
//            wordEntity.setDes(des);
//        }
//        if(html != null) {
//            wordEntity.setHtml(html);
//        }
//        wordTemplateService.update(wordEntity);
//        Map<String, Object> result = new HashMap<>();
//
//        concactTemplateService.update();
//        return result;
//    }

    @RequestMapping(value="/formCommit",method= RequestMethod.POST)
    public Object formCommit( HttpServletRequest request, HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
            Map map = request.getParameterMap();
            return response;
    }
    private LinkedHashMap<String,String> getField(String html){
        Pattern p=Pattern.compile("@@");
        Matcher m=p.matcher(html);
        while(m.find()) {
            System.out.println(m.group());
        }
        return null;
    }
    @RequestMapping(value="/batchImport",method= RequestMethod.POST)
    public Object saveThingsParse(MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
        Map<String, Object> result = new HashMap<>();
        result.put("result", "");
        String htmlStr = "";
        String docName =  file.getOriginalFilename();
        String fileName = file.getOriginalFilename().substring(0,file.getOriginalFilename().indexOf("."));
        try {

            String path = PropertyUtil.getValue("contract.template.path");
//            File desFile = new File("F:\\tmp\\oa\\demo.doc");
            File desFile = new File(path + docName);
            if(!desFile.getParentFile().exists()){
                desFile.mkdirs();
            }
            file.transferTo(desFile);
            File htmlFile = Word2Html.office2pdf(path + docName);
            // 获取html文件流
            StringBuilder htmlSb = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(htmlFile),"GBK"));
                while (br.ready()) {
                    htmlSb.append(br.readLine());
                }
                br.close();
                // 删除临时文件
//            htmlFile.delete();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // HTML文件字符串
            htmlStr = htmlSb.toString();
            htmlStr=HtmlProcess.clearFormat(htmlStr,path + "\\images");
//            htmlStr=HtmlProcess.clearFormat(htmlStr,path + "F:\\tmp\\images");
//            WordEntity wordEntity = new WordEntity();
//            wordEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
//            wordEntity.setName(fileName);
//            wordEntity.setHtml(htmlStr);
//            wordTemplateService.insert(wordEntity);
            OAContractTemplate oaContractTemplate = new OAContractTemplate();
            oaContractTemplate.setTemplateCreatetime(new Timestamp(System.currentTimeMillis()));
            oaContractTemplate.setUserId(loginUser.getId());
//            oaContractTemplate.setTemplateHtml(htmlStr);
            oaContractTemplate.setTemplateName(fileName);
            oaContractTemplate.setTemplateStatus(1);
            concactTemplateService.insert(oaContractTemplate);

            int id = oaContractTemplate.getTemplateId();
            Pattern pattern = Pattern.compile("<U>([\\s\\S]*?)</U>");
            Matcher matcher = pattern.matcher(htmlStr);

            String before = "<input type=\"text\" style=\"border-bottom: 1px solid #dbdbdb;border-top:0px;border-left:0px;border-right:0px;\" name=\"";
            String end = "\"/>";
            while(matcher.find()) {
                String tmp = matcher.group();
                OAFormProperties oaFormProperties = new OAFormProperties();
                String var = tmp.substring(5,tmp.indexOf("%%"));
                String type = tmp.substring(tmp.indexOf("%%")+2,tmp.indexOf("##"));
                String length = tmp.substring(tmp.indexOf("##")+2,tmp.indexOf("</"));
                int start = matcher.start();
                String name = "name_" + Md5Utils.getMd5(String.format("%s%s%s%s",var,type,length,start));
                oaFormProperties.setFieldName(var);
                oaFormProperties.setFieldMd5(name);
                oaFormProperties.setTemplateId(id);
                oaFormProperties.setFieldType(type);
                oaFormProperties.setFieldValid(length);
                oaFormProperties.setCreateTime(new Date());
//                wordTemplateField.setField(var);
//                wordTemplateField.setFieldMd5(name);
//                wordTemplateField.setLength(length);
//                wordTemplateField.setStart(String.format("%s",start));
//                wordTemplateField.setType(type);
//                wordTemplateField.setTemplateId(id);
//                wordTemplateFieldService.insert(wordTemplateField);
                formPropertiesService.insert(oaFormProperties);
                String text = before+name+"\" id=\""+name+end;
                htmlStr = htmlStr.replace(tmp,text);
            }
            oaContractTemplate.setTemplateHtml(htmlStr);
//            wordEntity.setHtml(htmlStr);
//            wordTemplateService.update();
            concactTemplateService.update(oaContractTemplate);
//            wordTemplateService.updateHtml(wordEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
//    @RequestMapping(value="/batchImport11",method= RequestMethod.POST)
//    public Object saveThingsParse11(MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("result", "");
//        String htmlStr = "";
//        String docName =  file.getOriginalFilename();
//        String fileName = docName.split(".")[0];
//        try {
//
//            String path = PropertyUtil.getValue("contract.template.path");
////            File desFile = new File("F:\\tmp\\oa\\demo.doc");
//            File desFile = new File(path + docName);
//            if(!desFile.getParentFile().exists()){
//                desFile.mkdirs();
//            }
//            file.transferTo(desFile);
//            File htmlFile = Word2Html.office2pdf(path + docName);
//            // 获取html文件流
//            StringBuilder htmlSb = new StringBuilder();
//            try {
//                BufferedReader br = new BufferedReader(new InputStreamReader(
//                        new FileInputStream(htmlFile),"GBK"));
//                while (br.ready()) {
//                    htmlSb.append(br.readLine());
//                }
//                br.close();
//                // 删除临时文件
////            htmlFile.delete();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            // HTML文件字符串
//            htmlStr = htmlSb.toString();
//            htmlStr=HtmlProcess.clearFormat(htmlStr,path + "\\images");
////            htmlStr=HtmlProcess.clearFormat(htmlStr,path + "F:\\tmp\\images");
//            WordEntity wordEntity = new WordEntity();
//            wordEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
//            wordEntity.setName(fileName);
//            wordEntity.setHtml(htmlStr);
//            wordTemplateService.insert(wordEntity);
//
//
//            int id = wordEntity.getId();
//            Pattern pattern = Pattern.compile("<U>([\\s\\S]*?)</U>");
//            Matcher matcher = pattern.matcher(htmlStr);
//
//            String before = "<input type=\"text\" style=\"border-bottom: 1px solid #dbdbdb;border-top:0px;border-left:0px;border-right:0px;\" name=\"";
//            String end = "\"/>";
//            while(matcher.find()) {
//                String tmp = matcher.group();
//                WordTemplateField wordTemplateField = new WordTemplateField();
//                String var = tmp.substring(5,tmp.indexOf("%%"));
//                String type = tmp.substring(tmp.indexOf("%%")+2,tmp.indexOf("##"));
//                String length = tmp.substring(tmp.indexOf("##")+2,tmp.indexOf("</"));
//                int start = matcher.start();
//                String name = "name_" + Md5Utils.getMd5(String.format("%s%s%s%s",var,type,length,start));
//                wordTemplateField.setField(var);
//                wordTemplateField.setFieldMd5(name);
//                wordTemplateField.setLength(length);
//                wordTemplateField.setStart(String.format("%s",start));
//                wordTemplateField.setType(type);
//                wordTemplateField.setTemplateId(id);
//                wordTemplateFieldService.insert(wordTemplateField);
//                String text = before+name+"\" id=\""+name+end;
//                htmlStr = htmlStr.replace(tmp,text);
//            }
//            wordEntity.setHtml(htmlStr);
////            wordTemplateService.update();
//            wordTemplateService.updateHtml(wordEntity);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return response;
//    }
}
