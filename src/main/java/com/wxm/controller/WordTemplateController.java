package com.wxm.controller;

import com.wxm.entity.WordEntity;
import com.wxm.service.WordTemplateService;
import com.wxm.util.HtmlProcess;
import com.wxm.util.Word2Html;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WordTemplateController {

    @Autowired
    private WordTemplateService wordTemplateService;

    @RequestMapping(value="/templateList",method= RequestMethod.GET)
    public Object templateList( HttpServletRequest request) {

        List<WordEntity> list =  wordTemplateService.queryHtmlTemplate();
        int count = wordTemplateService.count();
        Map<String, Object> result = new HashMap<>();
        result.put("rows",list);
        result.put("total",count);
        return result;
    }
    @RequestMapping(value="/templateUpdate",method= RequestMethod.GET)
    public Object templateUpdate( HttpServletRequest request) {
        String id = request.getParameter("id");
        int ids = Integer.parseInt(id);
        String name = request.getParameter("name");
        String des = request.getParameter("des");
        String html = request.getParameter("html");
        WordEntity wordEntity =  wordTemplateService.queryHtmlbyId(ids);
        if(name != null) {
            wordEntity.setName(name);
        }
        if(des != null){
            wordEntity.setDes(des);
        }
        if(html != null) {
            wordEntity.setHtml(html);
        }
        wordTemplateService.update(wordEntity);
        Map<String, Object> result = new HashMap<>();

        return result;
    }

    @RequestMapping(value="/batchImport",method= RequestMethod.POST)
    public Object saveThingsParse(MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        result.put("result", "");
        String htmlStr = "";
        try {
            File desFile = new File("F:\\tmp\\oa\\demo.doc");
            if(!desFile.getParentFile().exists()){
                desFile.mkdirs();
            }

            file.transferTo(desFile);

            File htmlFile = Word2Html.office2pdf("F:\\tmp\\oa\\demo.doc");

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
            htmlStr=HtmlProcess.clearFormat(htmlStr,"F:\\tmp\\images");
            WordEntity wordEntity = new WordEntity();
            wordTemplateService.insert(new Timestamp(System.currentTimeMillis()),htmlStr);
//            response.put("result", htmlStr);
//            OutputStream outputStream = response.getOutputStream();
//            response.setHeader("content-type", "text/html;charset=UTF-8");
//            byte[] dataByteArr = htmlStr.getBytes("UTF-8");//将字符转换成字节数组，指定以UTF-8编码进行转换
//            outputStream.write(dataByteArr);//使用OutputStream流向客户端输出字节数组
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
