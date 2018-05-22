package com.wxm.controller;
import com.wxm.model.OAAudit;
import com.wxm.model.OAContractCirculationWithBLOBs;
import com.wxm.model.OAContractTemplate;
import com.wxm.model.OAFormProperties;
import com.wxm.service.*;
import com.wxm.util.HtmlProcess;
import com.wxm.util.Md5Utils;
import com.wxm.util.Word2Html;
import com.wxm.util.exception.OAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(WordTemplateController.class);
    @Autowired
    private ConcactTemplateService concactTemplateService;
    @Value("${contract.template.path}")
    private String contractPath;
    @Value("${openoffice.org.path}")
    private String openOfficePath;
    @Autowired
    private FormPropertiesService formPropertiesService;
    @Autowired
    private ContractCirculationService contractCirculationService;
    @Autowired
    private AuditService auditService;

    @RequestMapping(value="/deleteWordTemplate",method= RequestMethod.DELETE,produces="application/json;charset=UTF-8")
    public Object deleteWordTemplate( HttpServletRequest request,
                                      @RequestParam(value = "id", defaultValue = "0", required = true) Integer id) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try{
            if(id != 0) {
                concactTemplateService.delete(id);
                formPropertiesService.delete(id);
                auditService.audit(new OAAudit(loginUser.getName(),String.format("删除合同模板 %s",id)));
            }
        }catch (Exception e){
            LOGGER.error("异常",e);
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
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try {
            result.put("rows", formPropertiesService.list(offset, limit, templateName));
            result.put("total", formPropertiesService.count(templateName));
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result","failed");
        }
        return result;
    }
    @RequestMapping(value="/updateFieldInfo",method= RequestMethod.POST,produces="application/json;charset=UTF-8")
    public Object updateFieldInfo(@RequestBody OAFormProperties oaFormProperties,HttpServletRequest request) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try {
            formPropertiesService.update(oaFormProperties);
            auditService.audit(new OAAudit(loginUser.getName(), String.format("更新字段信息")));
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result","failed");
        }
        return result;
    }

    @RequestMapping(value="/templateListTotal",method= RequestMethod.GET,produces="application/json;charset=UTF-8")
    public Object templateList( HttpServletRequest request)throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try {
            result.put("rows", concactTemplateService.listTemplate());
            result.put("total", concactTemplateService.count());
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result","failed");
        }
        return result;
    }

    @RequestMapping(value="/templateList",method= RequestMethod.GET,produces="application/json;charset=UTF-8")
    public Object templateList( HttpServletRequest request,
                                @RequestParam(value = "offset", required=true) Integer offset,
                                @RequestParam(value = "limit", required=true) Integer limit
                                ) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try {
            result.put("rows", concactTemplateService.list(offset, limit));
            result.put("total", concactTemplateService.count());
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result","failed");
        }
        return result;
    }

    @RequestMapping(value="/formCommit",method= RequestMethod.POST)
    public Object formCommit( HttpServletRequest request, HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new OAException(1101,"用户未登录");
            LOGGER.error("用户未登录");
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
    //自定义文档处理
    @RequestMapping(value="/custom",method= RequestMethod.POST)
    public Object custom(MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            String docName = file.getOriginalFilename();
            File desFile = new File(contractPath + docName);
            if (!desFile.getParentFile().exists()) {
                desFile.mkdirs();
            }
            file.transferTo(desFile);
            result.put("file", docName);
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result","failed");
        }
        return result;
    }
    //文件下载
    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Object download(
            @RequestParam(value = "contractId", required = false, defaultValue = "") Integer contractId,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.querybyId(contractId);
        byte[] bytes = oaContractCirculationWithBLOBs.getContractPdf();
        if(null != bytes) {
            response.setContentType("application/x-download");
            String codedfilename = java.net.URLEncoder.encode(oaContractCirculationWithBLOBs.getContractName() + ".doc", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + codedfilename);
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            return null;
        }else {
            return "文件不存在！";
        }
    }


    @RequestMapping(value="/batchImport",method= RequestMethod.POST)
    public Object saveThingsParse(MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        String htmlStr = "";
        String docName =  file.getOriginalFilename();
        String fileName = file.getOriginalFilename().substring(0,file.getOriginalFilename().indexOf("."));
        try {
            File desFile = new File(contractPath + docName);
            if(!desFile.getParentFile().exists()){
                desFile.mkdirs();
            }
            file.transferTo(desFile);
            File htmlFile = Word2Html.office2pdf(contractPath + docName,openOfficePath);
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
                LOGGER.error("异常",e);
                result.put("result","failed");
            } catch (IOException e) {
                LOGGER.error("异常",e);
                result.put("result","failed");
            }
            // HTML文件字符串
            htmlStr = htmlSb.toString();
            htmlStr=HtmlProcess.clearFormat(htmlStr,contractPath + "\\images");
            OAContractTemplate oaContractTemplate = new OAContractTemplate();
            oaContractTemplate.setTemplateCreatetime(new Timestamp(System.currentTimeMillis()));
            oaContractTemplate.setUserId(loginUser.getId());
            oaContractTemplate.setTemplateName(fileName);
            oaContractTemplate.setTemplateStatus(1);
            concactTemplateService.insert(oaContractTemplate);

            int id = oaContractTemplate.getTemplateId();
            Pattern pattern = Pattern.compile("@@([\\s\\S]*?)!!");
            Matcher matcher = pattern.matcher(htmlStr);

            String before = "<input type=\"text\" style=\"border:none;border-bottom:1px solid #000;\" name=\"";
            String end = "\"/>";
            while(matcher.find()) {
                String tmp = matcher.group();
                OAFormProperties oaFormProperties = new OAFormProperties();
                String var = tmp.substring(2,tmp.indexOf("%%"));
                String type = tmp.substring(tmp.indexOf("%%")+2,tmp.indexOf("##"));
                String length = tmp.substring(tmp.indexOf("##")+2,tmp.indexOf("!!"));
                int start = matcher.start();
                String name = "name_" + Md5Utils.getMd5(String.format("%s%s%s%s",var,type,length,start));
                oaFormProperties.setFieldName(var);
                oaFormProperties.setFieldMd5(name);
                oaFormProperties.setTemplateId(id);
                oaFormProperties.setFieldType(type);
                oaFormProperties.setFieldValid(length);
                oaFormProperties.setCreateTime(new Date());
                formPropertiesService.insert(oaFormProperties);
                String text = before+name+"\" id=\""+name+end;
                htmlStr = htmlStr.replace(tmp,text);
            }
            oaContractTemplate.setTemplateHtml(htmlStr);
            concactTemplateService.update(oaContractTemplate);
            auditService.audit(new OAAudit(loginUser.getName(), String.format("上传文件模板 %s",docName)));
        } catch (Exception e) {
            LOGGER.error("异常",e);
            result.put("result","failed");
        }
        return "";
    }
}
