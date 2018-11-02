package com.wxm.util;

import com.wxm.model.OAAttachment;
import com.wxm.model.OAContractCirculationWithBLOBs;
import com.wxm.service.ContractCirculationService;
import com.wxm.service.OAAttachmentService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Html2PdfTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Html2PdfTask.class);
    private static Object object = new Object();
    private StringBuilder html;
    private ContractCirculationService contractCirculationService;
    private Map<String,Integer> linkedHashMap;
    private String processInstancesId;
    private OAContractCirculationWithBLOBs tmp;
    private String contractPath;
    private String openoffice;
    private HistoryService historyService;
    private OAAttachmentService oaAttachmentService;

    public Html2PdfTask(StringBuilder html, ContractCirculationService contractCirculationService, Map<String,Integer> linkedHashMap,
                        String processInstancesId, OAContractCirculationWithBLOBs tmp, String contractPath, String openoffice,
                        HistoryService historyService, OAAttachmentService oaAttachmentService){
     this.html = html;
     this.contractCirculationService = contractCirculationService;
     this.linkedHashMap = linkedHashMap;
     this.processInstancesId = processInstancesId;
     this.tmp = tmp;
     this.contractPath = contractPath;
     this.openoffice = openoffice;
     this.historyService =  historyService;
     this.oaAttachmentService = oaAttachmentService;
    }
    private String completeWithBlank(String src,Integer size){
        size = size - src.length();
        StringBuffer tmp = new StringBuffer(src);
        for (int i = 0; i < size; i++) {
            tmp.append("&ensp;");
        }
        return tmp.toString();

    }
    private String fillValue(String processInstancesId,StringBuilder text,Map<String,Integer> linkedHashMap){
        Pattern pattern = Pattern.compile("<input([\\s\\S]*?)>");
        Matcher matcher = pattern.matcher(text);
        List<String> stringList = new LinkedList<>();
        while(matcher.find()) {
            try {
                String tmp = matcher.group();
                String name = tmp.substring(tmp.indexOf("id=") + 4);
                name = name.substring(0, name.indexOf("\""));
                stringList.add(tmp);
            }catch (Exception e){
                LOGGER.info("",e);
            }
//            map.put(name,tmp);
        }
        pattern = Pattern.compile("<textarea([\\s\\S]*?)></textarea>");
        matcher = pattern.matcher(text);
        while(matcher.find()){
            try {
                String tmp = matcher.group();
                String name = tmp.substring(tmp.indexOf("id=") + 4);
                name = name.substring(0, name.indexOf("\""));
                stringList.add(tmp);
            }catch (Exception e){
                LOGGER.info("",e);
            }
        }
        List<HistoricVariableInstance> historicVariableInstanceList =  historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstancesId).list();
        Map<String,String> stringStringMap = new LinkedHashMap<>();
        for(HistoricVariableInstance historicVariableInstance : historicVariableInstanceList){
            if(historicVariableInstance.getVariableName().contains("name_") && historicVariableInstance.getValue() != null && StringUtils.isNotBlank(historicVariableInstance.getValue().toString()) ) {
                stringStringMap.put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue().toString());
            }
        }
        for(String str:stringList){
            if(null == str) continue;
            String name = str.substring(str.indexOf("id=")+4);
            name = name.substring(0,name.indexOf("\""));
            int start = text.indexOf(str);
//            if (size > 0 && historicVariableInstance.getValue() != null && StringUtils.isNotBlank(historicVariableInstance.getValue().toString())) {
//                String inputValue = map.get(historicVariableInstance.getVariableName());
//                if(null == text || inputValue == null) continue;
//                int start = text.indexOf(inputValue);
//                String value = historicVariableInstance.getValue().toString();
//                value = completeWithBlank(value,linkedHashMap.get(historicVariableInstance.getVariableName()));
//                text.replace(start,start+inputValue.length(),String.format("<u>%s</u>",value));
//            }

            String value;
            if(stringStringMap.containsKey(name)){
                value =  stringStringMap.get(name).toString();

            }else{
                value= "无";
            }
            if(linkedHashMap.containsKey(name)) {
                if(str.contains("checkbox")){
                    if(!value.equals("无")) {
                        text.replace(start, start + str.length(), "<span>√</span>");
//                        text.insert(start + 6, "<span style=\"font-family:'Wingdings 2'; font-size:12pt\">\uF052</span>");
                    }
                    else{
                        text.replace(start, start + str.length(), "<span style=\"font-family:宋体; font-size:12pt\">□</span>");
                    }
                }else {
                    value = completeWithBlank(value, linkedHashMap.get(name));
                    text.replace(start, start + str.length(), String.format("<u>%s</u>", value));
                }
            }
        }
//        for(String str : set){
//
//            int start = text.indexOf(str);
//            text.replace(start,start+str.length(),"hello");
//        }



//        for(HistoricVariableInstance historicVariableInstance : historicVariableInstanceList){
//            if(historicVariableInstance.getVariableName().contains("name_")) {
//                int size = text.indexOf(historicVariableInstance.getVariableName());
//                if (size > 0 && historicVariableInstance.getValue() != null && StringUtils.isNotBlank(historicVariableInstance.getValue().toString())) {
//                    String inputValue = map.get(historicVariableInstance.getVariableName());
//                    if(null == text || inputValue == null) continue;
//                    int start = text.indexOf(inputValue);
//                    String value = historicVariableInstance.getValue().toString();
//                    value = completeWithBlank(value,linkedHashMap.get(historicVariableInstance.getVariableName()));
//                    text.replace(start,start+inputValue.length(),String.format("<u>%s</u>",value));
//                }
//            }
//        }
        return text.toString();
    }
    @Override
    public void run() {
        try {
            LOGGER.info("begin create pdf");

            String data = fillValue(processInstancesId, html,linkedHashMap);
//                String path = PropertyUtil.getValue("contract.template.path");
            String fileHtml = contractPath + ".html";
            String filePf = contractPath + ".pdf";

//                OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(fileHtml),"UTF-8");
//                BufferedWriter writer=new BufferedWriter(write);
//                writer.write(data);
//                writer.close();
            PrintStream printStream = new PrintStream(new FileOutputStream(fileHtml));
            printStream.println(data);
            //转换成pdf文件
            synchronized (object) {
                File htmlFile = Word2Html.html2pdf(fileHtml, openoffice);
            }

//            InputStream input = new ByteArrayInputStream(bytesDB);
            PDFMergerUtility mergePdf = new PDFMergerUtility();
            mergePdf.addSource(filePf);
            List<OAAttachment> oaAttachmentList = oaAttachmentService.listBlobByProcessId(tmp.getProcessInstanceId());
            for(OAAttachment oaAttachment:oaAttachmentList){
                if(oaAttachment.getFileContent() != null)
                    mergePdf.addSource(new ByteArrayInputStream(oaAttachment.getFileContent()));
            }
            mergePdf.setDestinationFileName(contractPath+"new.pdf");
            mergePdf.mergeDocuments();

            // 获取pdf文件流
            byte[] pdf = FileByte.getByte(contractPath+"new.pdf");
            tmp.setContractStatus("completed");
            // HTML文件字符串
            tmp.setContractPdf(pdf);
            contractCirculationService.update(tmp);
            LOGGER.info("end create pdf");
        } catch (Exception e) {
            LOGGER.error("异常",e);
        }
    }
}
