package com.wxm.controller;
import com.wxm.model.*;
import com.wxm.service.*;
import com.wxm.util.FileByte;
import com.wxm.util.HtmlProcess;
import com.wxm.util.Md5Utils;
import com.wxm.util.Word2Html;
import com.wxm.util.exception.OAException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private OAAttachmentService oaAttachmentService;

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
                formPropertiesService.deleteByTemplateId(id);
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
                             @RequestParam(value = "id", defaultValue = "0", required = true) Integer id,
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
            auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 获取合同模板字段列表",loginUser.getName())));
            result.put("rows", formPropertiesService.list(offset, limit, templateName,id));
            result.put("total", formPropertiesService.count(templateName,id));
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
            auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 获取所有合同模板列表",loginUser.getName())));
            result.put("rows", concactTemplateService.listTemplate());
            result.put("total", concactTemplateService.count());

            List<Deployment> deployments = repositoryService.createDeploymentQuery()
                    .orderByDeploymenTime().desc()
                    .list();

            List<com.wxm.entity.Deployment> list = new ArrayList<>();
            for(Deployment deployment: deployments){
                com.wxm.entity.Deployment deploy= new com.wxm.entity.Deployment();
                ProcessDefinition pf = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
                if(pf.isSuspended()) continue;
                deploy.setStatus(pf.isSuspended()?0:1);
                deploy.setVersion(new Integer(pf.getVersion()).toString());
                deploy.setId(deployment.getId());
                deploy.setName(deployment.getName());
                deploy.setCategory(deployment.getCategory());
                deploy.setDeploymentTime(deployment.getDeploymentTime());
                deploy.setTenantId(deployment.getTenantId());

                list.add(deploy);
            }
            result.put("deploys",list);
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
            auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 分页获取合同模板列表",loginUser.getName())));
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




    @RequestMapping(value = "/deleteAttachment", method = RequestMethod.GET)
    @ResponseBody
    public Object fileDeleteByName( HttpServletRequest request){
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", "failed");
        String name = request.getParameter("fileName");
        if(StringUtils.isNotBlank(name)) {
            oaAttachmentService.deleteByName(name);
            result.put("result", "success");
        }
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 删除附件",loginUser.getName())));
        return result;
    }
    @RequestMapping(value = "/fileDelete", method = RequestMethod.DELETE)
    @ResponseBody
    public Object fileDelete( HttpServletRequest request){
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        String id = request.getParameter("id");
        oaAttachmentService.delete(Integer.parseInt(id));
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 删除附件",loginUser.getName())));
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        return result;
    }
    private Object upload(MultipartFile file){
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        try {
            OAAttachment oaAttachment = new OAAttachment();
            oaAttachment.setFileName(file.getOriginalFilename());
            oaAttachment.setFileContent(file.getBytes());
            Integer id = oaAttachmentService.save(oaAttachment);
            result.put("id", id);
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result","failed");
        }
        return result;
    }

    //自定义文档处理
    @RequestMapping(value="/custom1",method= RequestMethod.POST)
    public Object custom1(@RequestParam("fileAttachment1")MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 附件:%s上传",file.getOriginalFilename(),loginUser.getName())));
        Map<String, Object> result = new HashMap<>();

        if(StringUtils.isBlank(file.getOriginalFilename())){
            result.put("result", "failed");
            return result;
        }
        result.put("result", "success");
        try{
//            String contract = request.getParameter("processInstanceId");
            String contract = request.getParameter("id");
            if(StringUtils.isBlank(contract)){
                contract =  request.getSession().getId();

            }
            List<OAAttachment> oaAttachmentList = oaAttachmentService.listByProcessId(contract);
            if(null != oaAttachmentList && oaAttachmentList.size()> 5){
                result.put("result", "failed");
                return result;
            }
            for(OAAttachment oaAttachment: oaAttachmentList){
                if(oaAttachment.getFileName().contains(file.getOriginalFilename())){
                    result.put("result", "failed");
                    return result;
                }
            }
            String docName =  String.format("%s_%s",new Date().getTime(),file.getOriginalFilename());
            OAAttachment oaAttachment = new OAAttachment();
            oaAttachment.setProcessId(contract);
            oaAttachment.setFileName(docName);
            oaAttachment.setFileContent(file.getBytes());
            Integer id = oaAttachmentService.save(oaAttachment);
//            result.put("id", id);

            result.put("file",docName);
            result.put("uid",contract);
            result.put("displayName", file.getOriginalFilename());

        }catch (Exception e){
            LOGGER.error("异常", e);
            result.put("result", "failed");
        }
        return result;
    }
    @RequestMapping(value="/custom2",method= RequestMethod.POST)
    public Object custom2(@RequestParam("fileAttachment2")MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 附件:%s上传",file.getOriginalFilename(),loginUser.getName())));
        Map<String, Object> result = new HashMap<>();
        if(StringUtils.isBlank(file.getOriginalFilename())){
            result.put("result", "failed");
            return result;
        }
        result.put("result", "success");
        try{
            String contract = request.getParameter("processInstanceId");
            if(StringUtils.isBlank(contract)){
                contract =  request.getSession().getId();
            }
            String docName =  String.format("%s_%s",new Date().getTime(),file.getOriginalFilename());
            OAAttachment oaAttachment = new OAAttachment();
            oaAttachment.setProcessId(contract);
            oaAttachment.setFileName(docName);
            oaAttachment.setFileContent(file.getBytes());
            Integer id = oaAttachmentService.save(oaAttachment);
//            result.put("id", id);
            result.put("file",docName);
            result.put("uid",contract);
            result.put("displayName", file.getOriginalFilename());


        }catch (Exception e){
            LOGGER.error("异常", e);
            result.put("result", "failed");
        }
        return result;
    }
    @RequestMapping(value="/custom3",method= RequestMethod.POST)
    public Object custom3(@RequestParam("fileAttachment3")MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 附件:%s上传",file.getOriginalFilename(),loginUser.getName())));
        Map<String, Object> result = new HashMap<>();
        if(StringUtils.isBlank(file.getOriginalFilename())){
            result.put("result", "failed");
            return result;
        }
        result.put("result", "success");
        try{
            String contract = request.getParameter("processInstanceId");
            if(StringUtils.isBlank(contract)){
                contract =  request.getSession().getId();
            }
            String docName =  String.format("%s_%s",new Date().getTime(),file.getOriginalFilename());
            OAAttachment oaAttachment = new OAAttachment();
            oaAttachment.setProcessId(contract);
            oaAttachment.setFileName(docName);
            oaAttachment.setFileContent(file.getBytes());
            Integer id = oaAttachmentService.save(oaAttachment);
//            result.put("id", id);
            result.put("file",docName);
            result.put("uid",contract);
            result.put("displayName", file.getOriginalFilename());


        }catch (Exception e){
            LOGGER.error("异常", e);
            result.put("result", "failed");
        }
//        String contract = request.getParameter("processInstanceId");
//        if(StringUtils.isNotBlank(contract)){
//            OAContractCirculation oaContractCirculation = contractCirculationService.selectBaseByProcessInstanceId(contract);
//            OAContractCirculationWithBLOBs oaContract = new OAContractCirculationWithBLOBs();
//            oaContract.setContractId(oaContractCirculation.getContractId());
//            oaContract.setAttachmentContent( file.getBytes());
//            contractCirculationService.update(oaContract);
//        }else {
//            try {
//                String docName =  String.format("%s_%s",new Date().getTime(),file.getOriginalFilename());
//                File desFile = new File(contractPath+docName);
//                if (!desFile.getParentFile().exists()) {
//                    desFile.mkdirs();
//                }
//                file.transferTo(desFile);
//                result.put("file", docName);
//                result.put("displayName", file.getOriginalFilename());
//            } catch (Exception e) {
//                LOGGER.error("异常", e);
//                result.put("result", "failed");
//            }
//        }
        return result;
    }
    @RequestMapping(value="/custom4",method= RequestMethod.POST)
    public Object custom4(@RequestParam("fileAttachment4")MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 附件:%s上传",file.getOriginalFilename(),loginUser.getName())));
        Map<String, Object> result = new HashMap<>();
        if(StringUtils.isBlank(file.getOriginalFilename())){
            result.put("result", "failed");
            return result;
        }
        result.put("result", "success");
        try{
            String contract = request.getParameter("processInstanceId");
            if(StringUtils.isBlank(contract)){
                contract =  request.getSession().getId();
            }
            String docName =  String.format("%s_%s",new Date().getTime(),file.getOriginalFilename());
            OAAttachment oaAttachment = new OAAttachment();
            oaAttachment.setProcessId(contract);
            oaAttachment.setFileName(docName);
            oaAttachment.setFileContent(file.getBytes());
            Integer id = oaAttachmentService.save(oaAttachment);
//            result.put("id", id);
            result.put("file",docName);
            result.put("uid",contract);
            result.put("displayName", file.getOriginalFilename());


        }catch (Exception e){
            LOGGER.error("异常", e);
            result.put("result", "failed");
        }
        return result;
    }
    @RequestMapping(value="/custom5",method= RequestMethod.POST)
    public Object custom5(@RequestParam("fileAttachment5")MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 附件:%s上传",file.getOriginalFilename(),loginUser.getName())));
        Map<String, Object> result = new HashMap<>();
        if(StringUtils.isBlank(file.getOriginalFilename())){
            result.put("result", "failed");
            return result;
        }
        result.put("result", "success");
        try{
            String contract = request.getParameter("processInstanceId");
            if(StringUtils.isBlank(contract)){
                contract =  request.getSession().getId();
            }
            String docName =  String.format("%s_%s",new Date().getTime(),file.getOriginalFilename());
            OAAttachment oaAttachment = new OAAttachment();
            oaAttachment.setProcessId(contract);
            oaAttachment.setFileName(docName);
            oaAttachment.setFileContent(file.getBytes());
            Integer id = oaAttachmentService.save(oaAttachment);
//            result.put("id", id);
            result.put("file",docName);
            result.put("uid",contract);
            result.put("displayName", file.getOriginalFilename());


        }catch (Exception e){
            LOGGER.error("异常", e);
            result.put("result", "failed");
        }
        return result;
    }
    @RequestMapping(value = "/downloadPdf", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public void ResponseBody(
            @RequestParam(value = "contractId", required = false, defaultValue = "") Integer contractId,
            @RequestParam(value = "fileName", required = false, defaultValue = "") String fileName,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        if(StringUtils.isBlank(fileName) && contractId != null ) {
            OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.querybyId(contractId);
            byte[] bytes = oaContractCirculationWithBLOBs.getAttachmentContent();
            if (null != bytes) {
                response.setContentType("application/pdf");
//                String codedfilename = java.net.URLEncoder.encode(oaContractCirculationWithBLOBs.getAttachmentName(), "UTF-8");
//                response.setHeader("Content-Disposition", "attachment;filename=" + codedfilename);
                response.setContentLength(bytes.length);
                response.getOutputStream().write(bytes);
                response.getOutputStream().flush();
            }
        }else {
            OAAttachment oaAttachment = oaAttachmentService.getByFileName(fileName);
            String file = fileName.substring(fileName.indexOf('_') + 1);
            byte[] bytes = oaAttachment.getFileContent();
            if (null != bytes) {
                response.setContentType("application/pdf");
//            String codedfilename = java.net.URLEncoder.encode(file, "gb2312");
//            response.setHeader("Content-Disposition", "attachment;filename=" + codedfilename);
                response.setContentLength(bytes.length);
                response.getOutputStream().write(bytes);
                response.getOutputStream().flush();
            }
        }
    }
    //文件下载
    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Object download(
            @RequestParam(value = "contractId", required = false, defaultValue = "") Integer contractId,
            @RequestParam(value = "fileName", required = false, defaultValue = "") String fileName,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 文件下载",loginUser.getName())));
        if(StringUtils.isBlank(fileName) && contractId != null ) {
            OAContractCirculationWithBLOBs oaContractCirculationWithBLOBs = contractCirculationService.querybyId(contractId);
            byte[] bytes = oaContractCirculationWithBLOBs.getAttachmentContent();
            if (null != bytes) {
                response.setContentType("application/x-download");
                String codedfilename = java.net.URLEncoder.encode(oaContractCirculationWithBLOBs.getAttachmentName(), "UTF-8");
                response.setHeader("Content-Disposition", "attachment;filename=" + codedfilename);
                response.setContentLength(bytes.length);
                response.getOutputStream().write(bytes);
                response.getOutputStream().flush();
                return null;
            } else {
                return "文件不存在！";
            }
        }else{
            OAAttachment oaAttachment = oaAttachmentService.getByFileName(fileName);
            String file = fileName.substring(fileName.indexOf('_')+1);
            byte[] bytes = oaAttachment.getFileContent();
            if (null != bytes) {
                response.setContentType("application/x-download");
                String codedfilename = java.net.URLEncoder.encode(file, "UTF-8");
                response.setHeader("Content-Disposition", "attachment;filename=" + codedfilename);
                response.setContentLength(bytes.length);
                response.getOutputStream().write(bytes);
                response.getOutputStream().flush();
                return null;
            }else {
                return "文件不存在！";
            }
        }
    }
    private  boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }
    @RequestMapping(value="/batchImportHtml",method= RequestMethod.POST)
    public Object saveThingsParse1(MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception{
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
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 模板导入，模板名称：%s",loginUser.getName(),docName)));
        try{

             htmlStr = new String( file.getBytes(),"gb2312");
            htmlStr=HtmlProcess.clearFormat(htmlStr,contractPath + "\\images");
            OAContractTemplate oaContractTemplate = new OAContractTemplate();
            oaContractTemplate.setTemplateCreatetime(new Timestamp(System.currentTimeMillis()));
            oaContractTemplate.setUserId(loginUser.getId());
            oaContractTemplate.setTemplateName(fileName);
            oaContractTemplate.setTemplateStatus(0);
            concactTemplateService.insert(oaContractTemplate);

            int id = oaContractTemplate.getTemplateId();
            Pattern pattern = Pattern.compile("@@([\\s\\S]*?)!!");
            Matcher matcher = pattern.matcher(htmlStr);

            String longTextBefore = "<textarea rows=\"50\" cols=\"30\" style=\"width: 100%;height: 100px; name=\"";
            String checkboxBefore = "<input type=\"checkbox\" style=\"display:none;height:10px;zoom:180%;\" name=\"";

            String before = "<input type=\"text\" style=\"border:none;border-bottom:1px solid #000;\" name=\"";
            String checkboxButton = "<input type=\"checkbox\" name=\"";
            String end = "\"/>";
            String longTextEnd = "\"></textarea>";
            Map<String,String> map = new LinkedHashMap<>();

            while(matcher.find()) {
                String tmp = matcher.group();
                OAFormProperties oaFormProperties = new OAFormProperties();
                if(StringUtils.isBlank(tmp) || !tmp.contains("##") || !tmp.contains("%%") || !tmp.contains("!!"))continue;
                String var = tmp.substring(2,tmp.indexOf("%%"));
                String type = tmp.substring(tmp.indexOf("%%")+2,tmp.indexOf("##"));
                String length = tmp.substring(tmp.indexOf("##")+2,tmp.indexOf("!!"));
                int start = matcher.start();
                String name = "name_" + Md5Utils.getMd5(String.format("%s%s%s%s%s",docName,var,type,length,start));
                String checkbox = "checkbox_" + Md5Utils.getMd5(String.format("%s%s%s%s%s",docName,var,type,length,start));
                oaFormProperties.setFieldName(var);
                oaFormProperties.setFieldMd5(name);
                oaFormProperties.setTemplateId(id);
                oaFormProperties.setFieldType(type);
                oaFormProperties.setFieldValid(length);
                oaFormProperties.setCreateTime(new Date());
                formPropertiesService.insert(oaFormProperties);
                if(length.contains("CC")) {
                    String text = String.format("%s%s\" id=\"%s%s %s%s\" id=\"%s%s", checkboxButton, name, name, end, checkboxBefore, checkbox, checkbox, end);
                    map.put(tmp,text);
                }else if(tmp.contains("多行数据")||length.contains("JJ") ){
                    String text = String.format("%s%s\" id=\"%s%s %s%s\" id=\"%s%s", longTextBefore, name, name, longTextEnd, checkboxBefore, checkbox, checkbox, end);
                    map.put(tmp,text);
                } else{
                    String text = String.format("%s%s\" id=\"%s%s %s%s\" id=\"%s%s", before, name, name, end, checkboxBefore, checkbox, checkbox, end);
                    map.put(tmp,text);
                }
            }
            for(Map.Entry<String,String> entry : map.entrySet()){
                htmlStr = htmlStr.replace(entry.getKey(),entry.getValue());
            }
            oaContractTemplate.setTemplateHtml(htmlStr);
            concactTemplateService.update(oaContractTemplate);
            auditService.audit(new OAAudit(loginUser.getName(), String.format("上传文件模板 %s",docName)));
        }catch (Exception e){
            LOGGER.error("异常",e);
            result.put("result","failed");
        }
        return "";
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
        auditService.audit(new OAAudit(loginUser.getName(),String.format("%s 模板导入，模板名称：%s",loginUser.getName(),docName)));
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
            oaContractTemplate.setTemplateStatus(0);
            concactTemplateService.insert(oaContractTemplate);

            int id = oaContractTemplate.getTemplateId();
            Pattern pattern = Pattern.compile("@@([\\s\\S]*?)!!");
            Matcher matcher = pattern.matcher(htmlStr);

            String longTextBefore = "<textarea rows=\"50\" cols=\"30\" style=\"width: 100%;height: 100px; name=\"";
            String checkboxBefore = "<input type=\"checkbox\" style=\"display:none;height:10px;zoom:180%;\" name=\"";

            String before = "<input type=\"text\" style=\"border:none;border-bottom:1px solid #000;\" name=\"";
            String beforeInput = "<input type=\"text\" style=\"border:none;border-bottom:1px solid #000;width:100%;\" name=\"";
            String checkboxButton = "<input type=\"checkbox\" name=\"";
            String end = "\"/>";
            String longTextEnd = "\"></textarea>";
            Map<String,String> map = new LinkedHashMap<>();
            while(matcher.find()) {
                String tmp = matcher.group();
                OAFormProperties oaFormProperties = new OAFormProperties();
                if(StringUtils.isBlank(tmp) || !tmp.contains("##") || !tmp.contains("%%") || !tmp.contains("!!"))continue;
                String var = tmp.substring(2,tmp.indexOf("%%"));
                String type = tmp.substring(tmp.indexOf("%%")+2,tmp.indexOf("##"));
                String length = tmp.substring(tmp.indexOf("##")+2,tmp.indexOf("!!"));
                if(length.contains("BB")){
                    StringBuilder sb = new StringBuilder("<U>");
                    for(int i=0;i<Integer.parseInt(length.substring(2));i++){
                        sb.append("&ensp;");
                    }
                    sb.append("</U>");
                    map.put(tmp,sb.toString());
                    continue;
                }
                int start = matcher.start();
                String name = "name_" + Md5Utils.getMd5(String.format("%s%s%s%s%s",docName,var,type,length,start));
                String checkbox = "checkbox_" + Md5Utils.getMd5(String.format("%s%s%s%s%s",docName,var,type,length,start));
                oaFormProperties.setFieldName(var);
                oaFormProperties.setFieldMd5(name);
                oaFormProperties.setTemplateId(id);
                oaFormProperties.setFieldType(type);
                oaFormProperties.setFieldValid(length);
                String size = length.substring(2);
                if(!isInteger(size)){
                    continue;
                }
                oaFormProperties.setCreateTime(new Date());
                formPropertiesService.insert(oaFormProperties);
                if(length.contains("CC")) {
                    String text = String.format("%s%s\" id=\"%s%s %s%s\" id=\"%s%s", checkboxButton, name, name, end, checkboxBefore, checkbox, checkbox, end);
                    map.put(tmp,text);
                }else if(tmp.contains("多行数据") ||length.contains("JJ") ){
                    String text = String.format("%s%s\" id=\"%s%s %s%s\" id=\"%s%s", longTextBefore, name, name, longTextEnd, checkboxBefore, checkbox, checkbox, end);
                    map.put(tmp,text);
                } else{
                    if(Integer.parseInt(size) < 80) {
                        String text = String.format("%s%s\" size=%s id=\"%s%s %s%s\" id=\"%s%s", before, name, size, name, end, checkboxBefore, checkbox, checkbox, end);
                        map.put(tmp, text);
                    }else{
                        String text = String.format("%s%s\" id=\"%s%s %s%s\" id=\"%s%s", beforeInput, name, name, end, checkboxBefore, checkbox, checkbox, end);
                        map.put(tmp, text);
                    }
                }
            }
            for(Map.Entry<String,String> entry : map.entrySet()){
                htmlStr = htmlStr.replace(entry.getKey(),entry.getValue());
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
